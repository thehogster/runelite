package net.runelite.client.plugins.hideexternalmanager;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.ArrayList;

@Extension
@PluginDescriptor(
        name = "Hide External Manager",
        description = "Removes the OPRS external manager icon",
        tags = {"Spoon", "oprs", "external", "manager"},
        enabledByDefault = false
)

@Slf4j
public class HideExternalManagerPlugin extends Plugin
{
    @Inject
    private HideExternalManagerConfig config;

    @Inject
    private Client client;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ClientThread clientThread;

    ArrayList<NavigationButton> removedButtons = new ArrayList<>();
    public boolean buttonsSet = false;

    @Provides
    HideExternalManagerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(HideExternalManagerConfig.class);
    }

    @Override
    protected void startUp()
    {
        removeTabs();
    }

    @Override
    protected void shutDown()
    {
        readdTabs(true);
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equalsIgnoreCase("hideManagers")) {
            if (event.getKey().equals("hideOPRS")) {
                if (config.hideOPRS()) {
                    removeTabs();
                } else {
                    readdTabs(false);
                }
            } /*else if (event.getKey().equals("hideSpoon")) {
                if (config.hideSpoon()) {
                    removeTabs();
                } else {
                    readdTabs(false);
                }
            }*/
        }
    }

    private void removeTabs()
    {
        if (config.hideOPRS()) {
            configManager.setConfiguration("openosrs", "hideOprsManager", true);
        }
        /*if (config.hideSpoon()) {
            configManager.setConfiguration("openosrs", "hideSpoonManager", true);
        }*/
    }

    private void readdTabs(boolean onShutdown)
    {
        if (onShutdown) {
            configManager.setConfiguration("openosrs", "hideOprsManager", false);
            configManager.setConfiguration("openosrs", "hideSpoonManager", false);
        } else {
            if (!config.hideOPRS()) {
                configManager.setConfiguration("openosrs", "hideOprsManager", false);
            }
            /*if (!config.hideSpoon()) {
                configManager.setConfiguration("openosrs", "hideSpoonManager", false);
            }*/
        }
    }
}
