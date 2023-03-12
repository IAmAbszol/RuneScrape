package net.runelite.client.plugins.runescrape.listeners;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.plugins.runescrape.RuneScrapeConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;

public class InventoryListener extends BaseListener
{

    private final int INVENTORY_SLOTS = 28;

    public InventoryListener(RuneScrapeConfig config, Client client, String listenerName)
    {
        super(config, client, listenerName);
    }

    @Override
    public JSONObject call() throws Exception
    {
        JSONObject inventory = new JSONObject();
        JSONArray inventoryArray = new JSONArray();
        ItemContainer inventoryContainer = this.client.getItemContainer(InventoryID.INVENTORY);
        Item item = null;
        for(int slot = 0; slot < INVENTORY_SLOTS; slot++)
        {
            int id = -1;
            int stack = -1;
            JSONObject inventoryInfo = new JSONObject();
            try
            {
                item = inventoryContainer.getItem(slot);
            }
            catch (NullPointerException npe)
            {
                item = null;
            }
            if(item != null)
            {
                id = item.getId();
                stack = item.getQuantity();
            }
            inventoryInfo.put("id", id);
            inventoryInfo.put("stack", stack);
            inventoryArray.put(inventoryInfo);
        }
        inventory.put("inventory", inventoryArray);
        return inventory;
    }
}
