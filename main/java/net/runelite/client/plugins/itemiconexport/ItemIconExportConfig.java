package net.runelite.client.plugins.itemiconexport;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("itemiconexport")
public interface ItemIconExportConfig extends Config
{
    @ConfigItem(
            position = 1,
            keyName = "exportPath",
            name = "Export Path",
            description = "Path to export icons to"
    )
    default String exportPath() {
        return "itemiconexport-item-icons/";
    }
}

