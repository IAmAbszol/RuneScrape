package net.runelite.client.plugins.runescrape.listeners.mouse;

import net.runelite.api.Client;
import net.runelite.client.input.MouseAdapter;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class RuneScrapeMouseAdapter extends MouseAdapter
{

    private Instant mouseIdleTimeMs = Instant.now();
    private Client client;

    public ArrayList<MouseInteraction> interactions = new ArrayList<>();

    @Inject
    public RuneScrapeMouseAdapter(Client client)
    {
        super();
        this.client = client;
    }

    public long getMouseIdleTimeMs()
    {
        return Duration.between(mouseIdleTimeMs, Instant.now()).toMillis() % Long.MAX_VALUE;
    }

    @Override
    public MouseEvent mouseClicked(MouseEvent event)
    {
        return event;
    }

    @Override
    public MouseEvent mousePressed(MouseEvent event)
    {
        MouseType type = MouseType.NONE;
        if(SwingUtilities.isMiddleMouseButton(event)) {
            type = MouseType.MIDDLE;
        }

        if(SwingUtilities.isLeftMouseButton(event)) {
            type = MouseType.LEFT;
        }

        if(SwingUtilities.isRightMouseButton(event)) {
            type = MouseType.RIGHT;
        }

        if(type != MouseType.NONE) {
            interactions.add(new MouseInteraction(type, (int) client.getMouseCanvasPosition().getX(), (int) client.getMouseCanvasPosition().getY(), getMouseIdleTimeMs(), false));
        }
        mouseIdleTimeMs = Instant.now();
        return event;
    }

    @Override
    public MouseEvent mouseReleased(MouseEvent event) { return event;}

    @Override
    public MouseEvent mouseDragged(MouseEvent event)
    {
        MouseType type = MouseType.NONE;
        if(SwingUtilities.isMiddleMouseButton(event)) {
            type = MouseType.MIDDLE;
        }

        if(SwingUtilities.isLeftMouseButton(event)) {
            type = MouseType.LEFT;
        }

        if(SwingUtilities.isRightMouseButton(event)) {
            type = MouseType.RIGHT;
        }

        if(type != MouseType.NONE) {
            interactions.add(new MouseInteraction(type, (int) client.getMouseCanvasPosition().getX(), (int) client.getMouseCanvasPosition().getY(), getMouseIdleTimeMs(), true));
        }
        mouseIdleTimeMs = Instant.now();
        return event;
    }

}
