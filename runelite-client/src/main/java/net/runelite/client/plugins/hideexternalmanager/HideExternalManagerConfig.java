package net.runelite.client.plugins.hideexternalmanager;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hideManagers")
public interface HideExternalManagerConfig extends Config {
    @ConfigItem(
            position = 0,
            keyName = "hideOPRS",
            name = "Hide OPRS External Manager",
            description = "Hides the OPRS external manager to look less sus."
    )
    default boolean hideOPRS()
    {
        return false;
    }

    /*@ConfigItem(
            position = 1,
            keyName = "hideSpoon",
            name = "Hide SpoonLite External Manager",
            description = "Hides the SpoonLite external manager to look less sus."
    )
    default boolean hideSpoon()
    {
        return false;
    }*/
}
