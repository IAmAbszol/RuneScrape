package net.runelite.client.plugins.runescrape.output;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.runescrape.RuneScrapeConfig;
import net.runelite.client.plugins.runescrape.Task;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

// TODO: Add data processor here before sending message
@Slf4j
public class OutputManager
{

    private ExecutorService service = null;
    private RuneScrapeConfig config = null;
    private List<SocketWriter> writers = null;
    private Queue<Task> writerQueue = null;

    public OutputManager(ExecutorService service, RuneScrapeConfig config)
    {
        this.service = service;
        this.config = config;
        writers = Arrays.asList(
                new SocketWriter(config, "SocketWriter")
        );
        writerQueue = new ConcurrentLinkedQueue<>();
    }

    public void sendMessage(JSONObject message, long timeOutMS)
    {
        while(writerQueue.size() > 0)
        {
            Task writerTask = writerQueue.poll();
            boolean result = true;
            try
            {
                result = (Boolean) writerTask.get();
            }
            catch (java.util.concurrent.CancellationException jucc)
            {
                log.warn("Writer (" + writerTask.getCallable().getName() + ") did not complete before next execution.");
            }
            if(!result)
            {
                log.error("Writer (" + writerTask.getCallable().getName() + ") returned false.");
            }
        }
        for(BaseWriter writer : writers) {
            writer.update(message);
            writerQueue.add(new Task<Boolean>(service, writer, timeOutMS));
        }
    }

    public void shutDown()
    {
        for(BaseWriter writer : writers) {
            writer.shutDown();
        }
//        writers.clear();
    }

}
