package net.runelite.client.plugins.runescrape;

import net.runelite.client.plugins.runescrape.listeners.BaseListener;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class Task<T>
{

    private BaseScrape callableObj = null;
    private Future<T> promise = null;
    private Timer executionTimer = null;

    public Task(ExecutorService service,
                Callable<T> callable,
                long timeOutMs)
    {
        callableObj = (BaseScrape) callable;
        promise = service.submit(callable);
        executionTimer = new Timer(true);
        executionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
            if(!(promise.isDone() && promise.isCancelled())) {
                promise.cancel(true);
            }
            }
        }, timeOutMs);
    }

    public T get()
    {
        T returnedPromise = null;
        try
        {
            returnedPromise = promise.get();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return returnedPromise;
    }

    public BaseScrape getCallable() {
        return callableObj;
    }

    public boolean isAlive() {
        return !(promise.isDone() || promise.isCancelled());
    }

    public void shutdown()
    {
        executionTimer.cancel();
    }
}
