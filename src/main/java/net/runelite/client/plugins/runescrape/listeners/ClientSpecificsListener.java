package net.runelite.client.plugins.runescrape.listeners;

import net.runelite.api.*;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.runescrape.RuneScrapeConfig;
import org.json.JSONObject;

public class ClientSpecificsListener extends BaseListener
{

    private int numberOfAnimations = 0;
    private int numberOfItems = 0;
    private int numberOfObjects = 0;

    public ClientSpecificsListener(RuneScrapeConfig config, Client client, String listenerName)
    {
        super(config, client, listenerName);
        numberOfAnimations = AnimationID.class.getDeclaredFields().length;
        numberOfItems = ItemID.class.getDeclaredFields().length;
        numberOfObjects = ObjectID.class.getDeclaredFields().length;
    }

    @Override
    public JSONObject call() throws Exception
    {
        JSONObject clientObject = new JSONObject();
        clientObject.put("viewport_width", client.getViewportWidth());
        clientObject.put("viewport_height", client.getViewportHeight());
        clientObject.put("total_animations", numberOfAnimations);			// Dynamically grab to ensure any new animations added are tracked.
        clientObject.put("total_items", numberOfItems);
        clientObject.put("total_objects", numberOfObjects);
        clientObject.put("tab_id", client.getVarcIntValue(VarClientInt.INVENTORY_TAB));
        clientObject.put("bank_open", client.getWidget(WidgetInfo.BANK_CONTAINER) != null);
        clientObject.put("npc_bandwidth", config.npcBandwidth());
        clientObject.put("object_bandwidth", config.objectBandwidth());
        clientObject.put("update_rate", config.ppt());
        return new JSONObject().put("client_specifics", clientObject);
    }
}
