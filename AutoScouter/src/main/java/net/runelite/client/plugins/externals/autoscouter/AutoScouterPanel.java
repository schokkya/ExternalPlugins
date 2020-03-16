package net.runelite.client.plugins.autoscouter;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import java.awt.LayoutManager;

@Slf4j
@Singleton
class AutoScouterPanel extends PluginPanel
{

	@Inject
	private Client client;
	@Inject
	private AutoScouterPlugin autoScouterPlugin;
	private final JButton scoutButton;
	private final JButton layoutButton;
	private Boolean initOnce = false;

	AutoScouterPanel()
	{
		this.scoutButton = new JButton("New Scout");
		this.layoutButton = new JButton("Send layout to CC");
	}

	void init()
	{
		if (!initOnce)
		{
			this.setLayout((LayoutManager) new GridLayout(2, 1));
			this.setBackground(ColorScheme.DARK_GRAY_COLOR);
			this.setBorder((Border) new EmptyBorder(10, 10, 10, 10));

			JPanel raidsPanel = new JPanel();
			raidsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			raidsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
			raidsPanel.setLayout(new GridLayout(2, 1));

			JLabel title = new JLabel(htmlLabel("<center>Automatic CoX Scouter</center>"), SwingConstants.CENTER);
			title.setFont(FontManager.getRunescapeFont());

			JLabel subTitle = new JLabel(htmlLabel("Pressing the New Scout button will automatically scout a new layout for you"));
			subTitle.setFont(FontManager.getRunescapeSmallFont());
			raidsPanel.add(title);
			raidsPanel.add(subTitle);

			JPanel buttonPanel = new JPanel();
			buttonPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
			buttonPanel.setLayout(new GridLayout(2, 1, 0, 5));
			this.scoutButton.addActionListener(e ->
			{
				autoScouterPlugin.scout();
				return;
			});
			this.layoutButton.addActionListener(e ->
			{
				this.client.getCanvas().requestFocus();
				autoScouterPlugin.type("/!layout");
				return;
			});

			buttonPanel.add(this.scoutButton);
			buttonPanel.add(this.layoutButton);

			add(raidsPanel);
			add(buttonPanel);
			initOnce = true;
		}
	}

	private static String htmlLabel(String text)
	{
		return "<html><body><span style = 'color:white'>" + text + "</span></body></html>";
	}

}
