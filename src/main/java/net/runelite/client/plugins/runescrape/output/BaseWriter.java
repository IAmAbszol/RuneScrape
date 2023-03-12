package net.runelite.client.plugins.runescrape.output;

import net.runelite.client.plugins.runescrape.BaseScrape;
import net.runelite.client.plugins.runescrape.RuneScrapeConfig;
import org.json.JSONObject;

import java.util.Queue;
import java.util.concurrent.Callable;

public abstract class BaseWriter extends BaseScrape implements Callable<Boolean>
{

    protected RuneScrapeConfig config = null;
    protected JSONObject message = null;

    private String writerName = "BaseWriter";

    public BaseWriter(RuneScrapeConfig config, String writerName)
    {
        this.config = config;
        this.writerName = writerName;
    }

    public abstract void shutDown();

    public void update(JSONObject message) {
        this.message = message;
    }

    @Override
    public String getName()
    {
        return writerName;
    }

}
