package net.runelite.client.plugins.modelreplacer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup("modelreplacer")
public interface ModelReplacerConfig extends Config {
    @ConfigSection(
            name = "Scythe",
            description = "Scythe settings",
            position = 0,
            closedByDefault = true
    )
    String scythe = "scythe";

    @ConfigItem(
            position = 0,
            keyName = "scytheSwing",
            name = "Scythe Swing",
            description = "Changes the color of the regular scythe swing",
            section = scythe
    )
    default boolean scytheSwing() {
        return false;
    }

    @ConfigItem(
            position = 1,
            keyName = "scytheSwingColor",
            name = "Scythe Swing Color",
            description = "Sets the color of Scythe Swing",
            section = scythe,
            hidden = true,
            unhide = "scytheSwing"
    )
    default Color scytheSwingColor() {return new Color(0, 15, 225);}

    @ConfigItem(
            position = 2,
            keyName = "shittySwing",
            name = "Holy Scythe Swing",
            description = "Changes the color of the shittiest scythe swing",
            section = scythe
    )
    default boolean shittySwing() {
        return false;
    }

    @ConfigItem(
            position = 3,
            keyName = "shittySwingColor",
            name = "Holy Swing Color",
            description = "Sets the color of Holy Scythe Swing",
            section = scythe,
            hidden = true,
            unhide = "shittySwing"
    )
    default Color shittySwingColor() {return new Color(0, 15, 225);}

    @ConfigItem(
            position = 4,
            keyName = "bestSwing",
            name = "Sang Scythe Swing",
            description = "Changes the color of the best scythe swing",
            section = scythe
    )
    default boolean bestSwing() {
        return false;
    }

    @ConfigItem(
            position = 5,
            keyName = "bestSwingColor",
            name = "Sang Swing Color",
            description = "Sets the color of Sang Scythe Swing",
            section = scythe,
            hidden = true,
            unhide = "bestSwing"
    )
    default Color bestSwingColor() {return new Color(0, 15, 225);}

    @ConfigItem(
            position = 99,
            keyName = "raveScytheSwing",
            name = "Rave Scythe Swing",
            description = "All the colors of the rainbow",
            section = scythe
    )
    default raveScytheSwingMode raveScytheSwing() {return raveScytheSwingMode.OFF;}

    @ConfigItem(
            position = 1,
            keyName = "modelsToReplace",
            name = "Model to replace",
            description = "The ids separated by commas of the models to replace"
    )
    default String modelsToReplace() {
        return "";
    }

    @ConfigItem(
            position = 99,
            keyName = "showList",
            name = "Show all IDs to replace",
            description = "Shows the list of IDs to replace certain models"
    )
    default boolean showList() {
        return false;
    }

    @ConfigItem(
            position = 100,
            keyName = "list",
            name = "Complete ID List",
            description = "All of the models and IDs used to replace them",
            hidden = true,
            unhide = "showList"
    )
    default String list() {
        return "Arma Helm:\n" + "female 27639\n" + "\n" +
                "Dark Tbow:\n" + "male 32674\n" + "female 39561\n" + "\n" +
                "SpoonLite Sang Scythe:\n" + "male 42279\n" + "female 42272\n" + "\n" +
                "Steroid Blue Scythe:\n" + "male 35371\n" + "female 32906\n" + "\n" +
                "Neitiznot Faceguard:\n" + "male 38857\n" + "female 38858\n" + "\n" +
                "Skotizo Helmet:\n" + "male 42673\n" + "female 42685\n" + "\n" +
                "Kbd Helmet\n" + "male 42676\n" + "female 42684\n" + "\n" +
                "Vorkath Helmet\n" + "male 42680\n" + "female 42690\n" + "\n" +
                "Abyssal Demon Helmet\n" + "male 42674\n" + "female 42689\n" + "\n" +
                "Kq Helmet\n" + "male 42681\n" + "female 42682\n" + "\n" +
                "Holy Scythe:\n" + "male 42277\n" + "female 42270\n" + "\n" +
                "Booger Helm:\n" + "female 14398";
    }

    public enum raveScytheSwingMode {
        OFF, RAVE, EPILEPSY
    }
}
