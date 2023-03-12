package net.runelite.client.plugins.runescrape.listeners;

import net.runelite.api.Client;
import net.runelite.client.plugins.runescrape.RuneScrapeConfig;
import net.runelite.client.plugins.runescrape.listeners.mouse.MouseInteraction;
import net.runelite.client.plugins.runescrape.listeners.mouse.RuneScrapeMouseAdapter;
import org.json.JSONObject;

public class MouseListener extends BaseListener
{

    private int previous_mouse_x = -1;
    private int previous_mouse_y = -1;

    private RuneScrapeMouseAdapter mouseAdapter = null;

    public MouseListener(RuneScrapeConfig config, RuneScrapeMouseAdapter mouseAdapter, Client client, String listenerName)
    {
        super(config, client, listenerName);
        this.mouseAdapter = mouseAdapter;
    }

    @Override
    public JSONObject call() throws Exception
    {
        int mx = previous_mouse_x;
        int my = previous_mouse_y;
        int mclick = 0;
        boolean drag = false;

        JSONObject mouseObject = new JSONObject();
        JSONObject interaction = new JSONObject();
        if(mouseAdapter.interactions.size() > 0)
        {
            MouseInteraction mouseInteraction = mouseAdapter.interactions.get(0);
            if(mouseInteraction != null)
            {
                mx = previous_mouse_x = mouseInteraction.x;
                my = previous_mouse_y = mouseInteraction.y;
                mclick = mouseInteraction.interaction.ordinal();
                drag = mouseInteraction.isDragging;
            }
        }
        mouseAdapter.interactions.clear();

        interaction.put("mx", mx);
        interaction.put("my", my);
        interaction.put("mclick", mclick);
        interaction.put("drag", drag);

        mouseObject.put("mouse", interaction);
        mouseObject.put("mouse_midle_ms", mouseAdapter.getMouseIdleTimeMs());
        return mouseObject;
    }
}
