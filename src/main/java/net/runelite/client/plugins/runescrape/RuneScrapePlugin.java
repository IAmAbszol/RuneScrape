package net.runelite.client.plugins.runescrape;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.runescrape.listeners.BaseListener;
import net.runelite.client.plugins.runescrape.listeners.ClientSpecificsListener;
import net.runelite.client.plugins.runescrape.listeners.InventoryListener;
import net.runelite.client.plugins.runescrape.listeners.MouseListener;
import net.runelite.client.plugins.runescrape.listeners.mouse.RuneScrapeMouseAdapter;
import net.runelite.client.plugins.runescrape.output.OutputManager;
import org.json.JSONObject;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@PluginDescriptor(
    name = "RuneScrape",
    description = "Scrapes together information and writes to file/socket.",
    tags = {"runescrape", "json", "write"},
    enabledByDefault = false
)
@Slf4j
public class RuneScrapePlugin extends Plugin
{
    private final String VERSION = "0.1";

    private final int GAME_TICK_MS = 600;
    private int TOLERANCE_MS = 10; // TODO: Make configurable?
    private int PACKETS_PER_TICK = 4;
    private int UPDATE_RATE_MS = GAME_TICK_MS / PACKETS_PER_TICK;

    private Instant gameTickSync = null;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSSSSS");

    private boolean runDispatcher = false;
    private Thread dispatcherThread = null;

    private List<BaseListener> listeners = null;
    private Queue<Task> taskQueue = null;
    private ExecutorService service = null;
    private OutputManager outputManager = null;

    @Inject
    private Client client;

    @Inject
    private RuneScrapeConfig config;

    @Inject
    private RuneScrapeMouseAdapter mouseAdapter;

    @Inject
    private MouseManager mouseManager;

    @Override
    protected void startUp() throws Exception
    {
        mouseManager.registerMouseListener(mouseAdapter);
        listeners = new ArrayList<>(
                Arrays.asList(
                        new InventoryListener(config, client, "InventoryListener"),
                        new ClientSpecificsListener(config, client, "ClientSpecificsListener"),
                        new MouseListener(config, mouseAdapter, client, "MouseListener")
                )
        );
        taskQueue = new ConcurrentLinkedQueue<>();
        service = Executors.newCachedThreadPool();
        outputManager = new OutputManager(service, config);
        runDispatcher = true;
        log.info("RuneScrape started!");
    }

    @Override
    protected void shutDown() throws Exception
    {
        runDispatcher = false;
        for(Task task : taskQueue)
        {
            task.shutdown();
        }
        if (dispatcherThread != null && dispatcherThread.isAlive())
        {
            dispatcherThread.join();
        }
        mouseManager.unregisterMouseListener(mouseAdapter);
        service.shutdownNow();
        outputManager.shutDown();
        taskQueue.clear();
        log.info("RuneScrape stopped!");
    }

    @Provides
    RuneScrapeConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(RuneScrapeConfig.class);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        PACKETS_PER_TICK = config.ppt();
    }

    @Subscribe
    public void onGameTick(GameTick gameTick)
    {
        // Grab config changes as I swear onConfigChanged doesn't actually do this :D
        PACKETS_PER_TICK = config.ppt();
        TOLERANCE_MS = config.tolerance();
        // Synchronize RuneScrape for the next GameTick (Thread-safe, phew).
        gameTickSync = Instant.now();
        if(runDispatcher &&
                (dispatcherThread == null || !dispatcherThread.isAlive()))
        {
            dispatcherThread = new Thread(() -> taskDispatcher());
            dispatcherThread.start();
        }
    }

    private void taskDispatcher()
    {
        while(runDispatcher)
        {
            // Complete a whole gametick even after requesting a shutdown or packet rate change
            int timeSlot = 0;
            float bucketSizeMs = GAME_TICK_MS / PACKETS_PER_TICK;
            while(timeSlot < PACKETS_PER_TICK && runDispatcher)
            {
                long currentTickWindow = Math.abs(Duration.between(gameTickSync, Instant.now()).toMillis() % GAME_TICK_MS); // [0, GAME_TICK_MS)
                int slot = (int) (currentTickWindow / bucketSizeMs);
//                System.out.println("slot " + slot + ", timeSlot " + timeSlot + ", ppt " + PACKETS_PER_TICK);
                timeSlot = (slot < timeSlot) ? timeSlot : slot;
//                System.out.println("(" + timeSlot + " * " + bucketSizeMs + ") - " + currentTickWindow);

                // We're either exactly on time or already in the new window, collect!
                // Sleep for the entire window, accounting for tolerance and the current time we're inside this window.
                long timeOut = Math.max((long) (((timeSlot + 1) * bucketSizeMs) - currentTickWindow - TOLERANCE_MS), 0);
//                System.out.println("GameTickSync: " + gameTickSync.toString() + ", (" + (timeSlot + 1) + " * " + bucketSizeMs + ") - " + currentTickWindow + " - " + TOLERANCE_MS + " = " + timeOut);
                for(BaseListener listener : listeners)
                {
                    taskQueue.add(new Task<JSONObject>(
                            service, listener, timeOut
                    ));
                }
                // Queue will be terminated by timeout
                JSONObject message = new JSONObject();
                while(taskQueue.size() > 0)
                {
                    Task task = taskQueue.poll();
                    JSONObject result = null;
                    try
                    {
                        result = (JSONObject) task.get();
                    }
                    catch (java.util.concurrent.CancellationException jucc)
                    {
                        // Future was held up and didn't finish execution, report down below.
                        log.error(jucc.toString());
                    }
                    if(result != null)
                    {
                        Iterator<String> childKeys = result.keys();
                        while(childKeys.hasNext())
                        {
                            String childKey = childKeys.next();
                            message.put(childKey, result.get(childKey));
                        }
                    }
                    else
                    {
                        log.warn("Listener (" + task.getCallable().getName() + ") timed out after " + timeOut + "ms.");
                    }
                }
                // Apply timestamp onto message
                message.put("timestamp", formatter.format(Instant.now()));

                // Send messages out
                outputManager.sendMessage(message, timeOut);

                // Sleep until next tick window. Example: 150ms window, currently 5ms, wait 145ms until next window is ready.
                currentTickWindow = Math.abs(Duration.between(gameTickSync, Instant.now()).toMillis() % GAME_TICK_MS); // [0, GAME_TICK_MS)
                long waitingTimeMs = (long) ((timeSlot + 1) * bucketSizeMs);
                if(waitingTimeMs > currentTickWindow)
                {
                    // We're ahead of schedule, sleep!
                    try
                    {
//                        System.out.println("Sleeping for " + (waitingTimeMs - currentTickWindow) + "ms.");
                        Thread.sleep((waitingTimeMs - currentTickWindow));
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                // Handle sending out, don't run
                timeSlot++;
            }
        }
    }
}
