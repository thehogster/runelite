package net.runelite.client.plugins.modelreplacer;

import com.google.inject.Provides;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.overlay.OverlayIndex;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@PluginDescriptor(name = "[b] Model Replacer", description = "Replace in-game models", enabledByDefault = false)
public class ModelReplacer extends Plugin {
    private static final Logger log = LoggerFactory.getLogger(ModelReplacer.class);
    private static final int MODEL_INDEX = 7;
    private static final int MODEL_INDEX_SHIFTED = 458752;
    private static final Set<Integer> overlays;

    @Inject
    private PluginManager pluginManager;

    @Inject
    private ModelReplacerConfig config;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private Client client;

    static {
        Set<Integer> ref = null;
        try {
            Field f = OverlayIndex.class.getDeclaredField("overlays");
            f.setAccessible(true);
            ref = (Set<Integer>)f.get(null);
        } catch (NoSuchFieldException|IllegalAccessException e) {
            log.warn("Couldn't find field overlays for class OverlayIndex", e);
        }
        overlays = ref;
    }

    @Provides
    ModelReplacerConfig provideConfig(ConfigManager configManager) {
        return (ModelReplacerConfig)configManager.getConfig(ModelReplacerConfig.class);
    }

    protected void startUp() throws Exception {
        if (overlays == null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    this.pluginManager.stopPlugin(this);
                } catch (PluginInstantiationException ex) {
                    log.error("error stopping plugin", (Throwable)ex);
                }
            });
        } else {
            Text.fromCSV(this.config.modelsToReplace()).forEach(id -> add(Integer.parseInt(id)));
        }
    }

    protected void shutDown() throws Exception {
        if (overlays != null) {
            Text.fromCSV(this.config.modelsToReplace()).forEach(id -> remove(Integer.parseInt(id)));
            ChatMessageBuilder message = (new ChatMessageBuilder()).append(Color.MAGENTA, "Re-log to load the original models, hopping does not work!");
            this.chatMessageManager.queue(QueuedMessage.builder()
                    .type(ChatMessageType.ITEM_EXAMINE)
                    .runeLiteFormattedMessage(message.build())
                    .build());
        }
    }

    private int colorToRs2hsb(Color color) {
        float[] hsbVals = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hsbVals[2] = hsbVals[2] - Math.min(hsbVals[1], hsbVals[2] / 2.0F);
        int encode_hue = (int)(hsbVals[0] * 63.0F);
        int encode_saturation = (int)(hsbVals[1] * 7.0F);
        int encode_brightness = (int)(hsbVals[2] * 127.0F);
        return (encode_hue << 10) + (encode_saturation << 7) + encode_brightness;
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        if (config.raveScytheSwing() == ModelReplacerConfig.raveScytheSwingMode.OFF) {
            GraphicsObject obj = event.getGraphicsObject();
            String scytheType = "";
            if (config.scytheSwing() && (obj.getId() == 506 || obj.getId() == 478 || obj.getId() == 1231 || obj.getId() == 1172)) {
                scytheType = "regular";
            } else if (config.bestSwing() && (obj.getId() == 1891 || obj.getId() == 1892 || obj.getId() == 1893 || obj.getId() == 1894)) {
                scytheType = "sang";
            } else if (config.shittySwing() && (obj.getId() == 1895 || obj.getId() == 1896 || obj.getId() == 1897 || obj.getId() == 1898)) {
                scytheType = "holy";
            }

            Color configColor;
            if (!scytheType.equals("")) {
                if (scytheType.equals("regular")) {
                    configColor = config.scytheSwingColor();
                } else if (scytheType.equals("sang")) {
                    configColor = config.bestSwingColor();
                } else {
                    configColor = config.shittySwingColor();
                }

                Color color1;
                Color color2;
                Color color3;
                if (configColor.getRed() > configColor.getGreen() && configColor.getRed() > configColor.getBlue()) {
                    if (configColor.getRed() > 230) {
                        color1 = new Color(255, configColor.getGreen(), configColor.getBlue(), 200);
                        color2 = new Color(configColor.getRed() - 5, configColor.getGreen(), configColor.getBlue(), 200);
                        color3 = new Color(configColor.getRed() - 15, configColor.getGreen(), configColor.getBlue(), 200);
                    } else {
                        color1 = new Color(configColor.getRed() + 15, configColor.getGreen(), configColor.getBlue(), 200);
                        color2 = new Color(configColor.getRed(), configColor.getGreen(), configColor.getBlue(), 200);
                        if(configColor.getRed() < 15) {
                            color3 = new Color(0, configColor.getGreen(), configColor.getBlue(), 200);
                        }else {
                            color3 = new Color(configColor.getRed() - 15, configColor.getGreen(), configColor.getBlue(), 200);
                        }
                    }
                }else if (configColor.getGreen() > configColor.getRed() && configColor.getGreen() > configColor.getBlue()) {
                    if (configColor.getGreen() > 230) {
                        color1 = new Color(configColor.getRed(), 255, configColor.getBlue(), 200);
                        color2 = new Color(configColor.getRed(), configColor.getGreen() - 5, configColor.getBlue(), 200);
                        color3 = new Color(configColor.getRed(), configColor.getGreen() - 15, configColor.getBlue(), 200);
                    } else {
                        color1 = new Color(configColor.getRed(), configColor.getGreen() + 15, configColor.getBlue(), 200);
                        color2 = new Color(configColor.getRed(), configColor.getGreen(), configColor.getBlue(), 200);
                        if(configColor.getGreen() < 15) {
                            color3 = new Color(configColor.getRed(), 0, configColor.getBlue(), 200);
                        }else {
                            color3 = new Color(configColor.getRed(), configColor.getGreen() - 15, configColor.getBlue(), 200);
                        }
                    }
                }else {
                    if (configColor.getBlue() > 230) {
                        color1 = new Color(configColor.getRed(), configColor.getGreen(), 255, 200);
                        color2 = new Color(configColor.getRed(), configColor.getGreen(), configColor.getBlue() - 5, 200);
                        color3 = new Color(configColor.getRed(), configColor.getGreen(), configColor.getBlue() - 15, 200);
                    } else {
                        color1 = new Color(configColor.getRed(), configColor.getGreen(), configColor.getBlue() + 15, 200);
                        color2 = new Color(configColor.getRed(), configColor.getGreen(), configColor.getBlue(), 200);
                        if(configColor.getBlue() < 15) {
                            color3 = new Color(configColor.getRed(), configColor.getGreen(), 0, 200);
                        }else {
                            color3 = new Color(configColor.getRed(), configColor.getGreen(), configColor.getBlue() - 15, 200);
                        }
                    }
                }
                Arrays.fill(obj.getModel().getFaceColors1(), colorToRs2hsb(color1));
                Arrays.fill(obj.getModel().getFaceColors2(), colorToRs2hsb(color2));
                Arrays.fill(obj.getModel().getFaceColors3(), colorToRs2hsb(color3));
            }
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("modelreplacer")) {
            if (event.getOldValue() == null || event.getNewValue() == null)
                return;
            if (event.getKey().equals("modelsToReplace")) {
                List<String> oldList = Text.fromCSV(event.getOldValue());
                List<String> newList = Text.fromCSV(event.getNewValue());
                ArrayList<String> removed = oldList.stream().filter(s -> !newList.contains(s)).collect(Collectors.toCollection(ArrayList::new));
                ArrayList<String> added = newList.stream().filter(s -> !oldList.contains(s)).collect(Collectors.toCollection(ArrayList::new));
                removed.forEach(id -> remove(Integer.parseInt(id)));
                added.forEach(id -> add(Integer.parseInt(id)));
                ChatMessageBuilder message = (new ChatMessageBuilder()).append(Color.MAGENTA, "Re-log to load any replaced models, hopping does not work!");
                this.chatMessageManager.queue(QueuedMessage.builder()
                        .type(ChatMessageType.ITEM_EXAMINE)
                        .runeLiteFormattedMessage(message.build())
                        .build());
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if(config.raveScytheSwing() == ModelReplacerConfig.raveScytheSwingMode.RAVE){
            for(GraphicsObject obj : this.client.getGraphicsObjects()) {
                if(obj.getId() == 506 || obj.getId() == 478 || obj.getId() == 1271 || obj.getId() == 1891 || obj.getId() == 1892 || obj.getId() == 1893 || obj.getId() == 1895
                        || obj.getId() == 1896 || obj.getId() == 1897) {
                    int red = new Random().nextInt(255);
                    int green = new Random().nextInt(255);
                    int blue = new Random().nextInt(255);
                    if(red > 205)
                        red = 205;
                    if(green > 205)
                        green = 205;
                    if(blue > 205)
                        blue = 205;
                    Arrays.fill(obj.getModel().getFaceColors1(), colorToRs2hsb(new Color(red, green, blue, 200)));
                    Arrays.fill(obj.getModel().getFaceColors2(), colorToRs2hsb(new Color(red, green, blue, 210)));
                    Arrays.fill(obj.getModel().getFaceColors3(), colorToRs2hsb(new Color(red, green, blue, 200)));
                }
            }
        }
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        if(config.raveScytheSwing() == ModelReplacerConfig.raveScytheSwingMode.EPILEPSY){
            for(GraphicsObject obj : this.client.getGraphicsObjects()) {
                if(obj.getId() == 506 || obj.getId() == 478 || obj.getId() == 1271 || obj.getId() == 1891 || obj.getId() == 1892 || obj.getId() == 1893 || obj.getId() == 1895
                        || obj.getId() == 1896 || obj.getId() == 1897) {
                    int red = new Random().nextInt(255);
                    int green = new Random().nextInt(255);
                    int blue = new Random().nextInt(255);
                    if(red > 205)
                        red = 205;
                    if(green > 205)
                        green = 205;
                    if(blue > 205)
                        blue = 205;
                    Arrays.fill(obj.getModel().getFaceColors1(), colorToRs2hsb(new Color(red, green, blue, 200)));
                    Arrays.fill(obj.getModel().getFaceColors2(), colorToRs2hsb(new Color(red, green, blue, 210)));
                    Arrays.fill(obj.getModel().getFaceColors3(), colorToRs2hsb(new Color(red, green, blue, 200)));
                }
            }
        }
    }

    private static void add(int modelId) {
        if (overlays != null) {
            int model_overlay_index = 0x70000 | modelId;
            if (!overlays.contains(model_overlay_index))
                if (modelFileExists(modelId))
                    overlays.add(model_overlay_index);
        }
    }

    private static boolean modelFileExists(int modelId) {
        String path = String.format("/runelite/%s/%s", 7, modelId);
        URL resourceHash = ModelReplacer.class.getResource(path + ".hash");
        if (resourceHash != null) {
            String hash = resourceHash.getFile();
            URL resourceModel = ModelReplacer.class.getResource(path);
            if (resourceModel != null){
                String model = resourceModel.getFile();
                return (hash != null && !hash.equals("") && model != null && !model.equals(""));
            }
            return false;
        }
        return false;
    }

    private static void remove(int modelId) {
        if (overlays != null) {
            int model_overlay_index = 0x70000 | modelId;
            overlays.remove(model_overlay_index);
        }
    }
}
