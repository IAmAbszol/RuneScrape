package net.runelite.client.plugins.runescrape.listeners;

import net.runelite.api.Client;
import net.runelite.client.plugins.runescrape.BaseScrape;
import net.runelite.client.plugins.runescrape.RuneScrapeConfig;
import org.json.JSONObject;

import java.util.concurrent.Callable;

public abstract class BaseListener extends BaseScrape implements Callable<JSONObject>
{

    protected RuneScrapeConfig config = null;
    protected Client client = null;

    private String listenerName = "BaseListener";
    private boolean runListener = true;

    public BaseListener(RuneScrapeConfig config, Client client, String listenerName)
    {
        this.config = config;
        this.listenerName = listenerName;
        this.client = client;
    }

    @Override
    public String getName()
    {
        return listenerName;
    }

}
