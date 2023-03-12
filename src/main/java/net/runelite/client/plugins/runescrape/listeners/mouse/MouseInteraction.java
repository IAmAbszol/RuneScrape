package net.runelite.client.plugins.runescrape.listeners.mouse;

public class MouseInteraction {

    public MouseType interaction = null;

    public int x;
    public int y;

    public long mouseIdleMs = 0;

    public boolean isDragging = false;

    public MouseInteraction(MouseType interaction, int x, int y, long mouseIdleMs, boolean isDragging) {
        this.interaction = interaction;
        this.x = x;
        this.y = y;
        this.mouseIdleMs = mouseIdleMs;
        this.isDragging = isDragging;
    }

}
