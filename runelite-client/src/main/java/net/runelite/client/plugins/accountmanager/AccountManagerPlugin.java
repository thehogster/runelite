package net.runelite.client.plugins.accountmanager;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ScheduledExecutorService;


@PluginDescriptor(
		name = "[S] Account Manager",
		description = "Easily log into the game",
		tags = {"Login"},
		enabledByDefault = false
)
@Slf4j
@Singleton
public class AccountManagerPlugin extends Plugin
{
	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private KeyManager keyManager;

	@Inject
	private AccountManagerConfig config;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ScheduledExecutorService executorService;

	private AccountManagerPluginPanel panel;
	private NavigationButton button;
	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.hotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			panel.setLoginDefault();
		}
	};

	@Provides
	AccountManagerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AccountManagerConfig.class);
	}

	@Override
	protected void startUp()
	{
		panel = injector.getInstance(AccountManagerPluginPanel.class);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");

		button = NavigationButton.builder()
				.tooltip("Account Manager")
				.icon(icon)
				.priority(1)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(button);

		hotkeyListener.setEnabledOnLoginScreen(true);
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(button);
	}

	@Subscribe
	protected void onGameStateChanged(GameStateChanged event) {
		if (event.getGameState() == GameState.LOGIN_SCREEN) {
			keyManager.registerKeyListener(hotkeyListener);
			if (config.switchPanel()) {
				if (!button.isSelected()) {
					openPanel();
				}
			}
		} else {
			keyManager.unregisterKeyListener(hotkeyListener);
		}
	}

	private void openPanel() {
		if (config.switchPanel()) {
			clientThread.invokeLater(() -> {
				if (!ClientUI.getFrame().isVisible()) {
					return false;
				}

				if (button.isSelected()) {
					return true;
				}

				SwingUtilities.invokeLater(() -> executorService.submit(() -> button.getOnSelect().run()));
				System.out.println("button selected");
				return true;
			});
		}
	}
}
