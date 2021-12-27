/*
 * Copyright (c) 2019, TheStonedTurtle <https://github.com/TheStonedTurtle>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.openosrs.client.ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import com.openosrs.client.ui.OpenOSRSSplashScreen;
import net.runelite.client.ui.components.CustomScrollBarUI;

@Getter
public class MessagePanel extends JPanel
{
	private static final Dimension PANEL_SIZE = new Dimension(OpenOSRSSplashScreen.FRAME_SIZE.width - InfoPanel.PANEL_SIZE.width, OpenOSRSSplashScreen.FRAME_SIZE.height);
	private static final Dimension BAR_SIZE = new Dimension(PANEL_SIZE.width, 30);
	private static final int MESSAGE_AREA_PADDING = 15;

	private final JLabel titleLabel = new JLabel("Welcome to SpoonLite");
	private final JLabel messageArea;
	private final JLabel barLabel = new JLabel("Connecting with gameserver (try 1/10)");
	private final JProgressBar bar = new JProgressBar(0, 100);

	@Getter(AccessLevel.NONE)
	private final JScrollPane scrollPane;

	public MessagePanel()
	{
		this.setPreferredSize(PANEL_SIZE);
		this.setLayout(new GridBagLayout());
		this.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTH;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.ipady = 25;

		// main message
		titleLabel.setFont(new Font(FontManager.getRunescapeFont().getName(), FontManager.getRunescapeFont().getStyle(), 32));
		titleLabel.setHorizontalAlignment(JLabel.CENTER);
		titleLabel.setForeground(Color.WHITE);
		this.add(titleLabel, c);
		c.gridy++;

		// alternate message action
		//messageArea = new JLabel("<html><div style='text-align:center;'>Open-source client for Old School RuneScape with more functionality and less restrictions.</div></html>")
		messageArea = new JLabel("<html><div style='text-align:center;'>Holy fucking shit. I want to grill so goddamn bad. I can't stand it anymore. Every time I go to my yard I get a massive spatula." +
				" I've seen every charcoal review post there is online. My dreams are nothing but constant fucking grilling with My grill. " +
				"I'm sick of waking up every morning with six hotdogs in my boxers and knowing that those are hotdogs that should've been grilled inside of my grill. " +
				"I want it to have burgers/hotdogs.\n" + "\n" + "Fuck, my fucking mom caught me with the neighbors grill. " +
				"I'd used my own propane and went to fucking town. She hasn't said a word to me in 10 hours and I'm worried she's gonna take away my grill. " +
				"I might not ever get to see my grill again..</div></html>")
		{
			@Override
			public Dimension getPreferredSize()
			{
				final Dimension results = super.getPreferredSize();
				results.width = PANEL_SIZE.width - MESSAGE_AREA_PADDING;
				return results;
			}
		};
		messageArea.setFont(new Font(FontManager.getRunescapeFont().getName(), FontManager.getRunescapeSmallFont().getStyle(), 16));
		messageArea.setForeground(Color.WHITE);
		messageArea.setBorder(new EmptyBorder(0, MESSAGE_AREA_PADDING, 0, MESSAGE_AREA_PADDING));

		scrollPane = new JScrollPane(messageArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
		final JViewport viewport = scrollPane.getViewport();
		viewport.setForeground(Color.WHITE);
		viewport.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		viewport.setOpaque(true);

		c.weighty = 1;
		c.fill = 1;
		this.add(scrollPane, c);
		c.gridy++;

		c.weighty = 0;
		c.weightx = 1;
		c.ipady = 5;

		barLabel.setFont(FontManager.getRunescapeFont());
		barLabel.setHorizontalAlignment(JLabel.CENTER);
		barLabel.setForeground(Color.WHITE);
		barLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
		this.add(barLabel, c);
		c.gridy++;

		//bar.setBackground(ColorScheme.BRAND_BLUE_TRANSPARENT.darker()); //RuneLite colors
		//bar.setForeground(ColorScheme.BRAND_BLUE); //RuneLite colors
		bar.setBackground(ColorScheme.BRAND_SPOON_TRANSPARENT.darker()); //SpoonLite colors
		bar.setForeground(ColorScheme.BRAND_SPOON); //SpoonLite colors
		bar.setMinimumSize(BAR_SIZE);
		bar.setMaximumSize(BAR_SIZE);
		bar.setBorder(new MatteBorder(0, 0, 0, 0, Color.LIGHT_GRAY));
		bar.setUI(new BasicProgressBarUI()
		{
			protected Color getSelectionBackground()
			{
				return ColorScheme.DARKER_GRAY_COLOR;
			}

			protected Color getSelectionForeground()
			{
				return ColorScheme.DARKER_GRAY_COLOR;
			}
		});
		bar.setFont(FontManager.getRunescapeFont());
		bar.setVisible(true);
		this.add(bar, c);
		c.gridy++;
	}

	public void setMessageContent(String content)
	{
		if (!content.startsWith("<html"))
		{
			content = "<html><div style='width: 100%; text-align:center;'>" + content + "</div></html>";
		}

		messageArea.setText(content);
		messageArea.revalidate();
		messageArea.repaint();
	}

	public void setMessageTitle(String text)
	{
		titleLabel.setText(text);
		titleLabel.revalidate();
		titleLabel.repaint();
	}
}