package net.runelite.client.plugins.runescrape;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("runescrape")
public interface RuneScrapeConfig extends Config {

    @ConfigItem(
            keyName = "write_to_disk",
            name = "Write to Disk",
            description = "Begin writing messages to disk alongside the JAR."
    )
    default boolean writeToDisk() { return false; }

    @ConfigItem(
            keyName = "send_message",
            name = "Send Messages",
            description = "Begin sending messages to listening UDP socket."
    )
    default boolean sendMessage() { return false; }

    @ConfigItem(
            keyName = "address",
            name = "IP Address",
            description = "Address to send information over a UDP socket."
    )
    default String address()
    {
        return "localhost";
    }

    @ConfigItem(
            keyName = "port",
            name = "Port",
            description = "Port to send information over a UDP socket."
    )
    default int port()
    {
        return 4200;
    }

    @ConfigItem(
            keyName = "ppt",
            name = "Packets Per Tick",
            description = "Number of packets to send during a single game tick."
    )
    default int ppt() { return 2; }

    @ConfigItem(
            keyName = "object_bandwidth",
            name = "Object Bandwidth",
            description = "Number of objects to send over the socket, sorted by distance relative to actor."
    )
    default int objectBandwidth() { return 10; }

    @ConfigItem(
            keyName = "npc_bandwidth",
            name = "NPC Bandwidth",
            description = "Number of npcs to send over the socket, sorted by distance relative to actor."
    )
    default int npcBandwidth() { return 10; }

    @ConfigItem(
            keyName = "tolerance",
            name = "Tolerance (ms)",
            description = "Restrict listener times to not be exactly on next bucket time."
    )
    default int tolerance() { return 10; }

}
