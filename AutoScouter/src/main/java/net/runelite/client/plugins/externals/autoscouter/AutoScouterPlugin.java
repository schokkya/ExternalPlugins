
package net.runelite.client.plugins.autoscouter;


import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.VarClientInt;
import net.runelite.api.events.GameTick;
import net.runelite.api.vars.InterfaceTab;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.externals.utils.ExtUtilsCustom;
import net.runelite.client.plugins.externals.utils.Tab;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import java.awt.image.BufferedImage;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "CoX Auto Scouter",
	description = "CoX Auto Scouter",
	tags = {"CoX", "raids", "scouter", "auto"},
	type = PluginType.MISCELLANEOUS
)
@Slf4j
@SuppressWarnings("unused")
@PluginDependency(ExtUtilsCustom.class)
public class AutoScouterPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private ExtUtilsCustom utils;
	private NavigationButton navButton;
	private Robot robot;
	private ScheduledExecutorService executor;
	private static final int recBoardID = 29776;
	private static final int chambersID = 29777;
	private static final int exitStairsID = 29778;
	private Boolean checkWhere = false;
	private Boolean doBoard = false;
	private Boolean doParty = false;
	private Boolean doCloseInterface = false;
	private Boolean doEnterChambers = false;
	private Boolean doStartRaid = false;
	private Boolean doLeaveRaid = false;
	private Boolean doConfirmLeave = false;
	private Boolean doConfirmStart = false;

	public AutoScouterPlugin()
	{
		this.client = client;
	}

	@Override
	protected void startUp() throws AWTException
	{
		AutoScouterPanel autoScouterPanel = injector.getInstance(AutoScouterPanel.class);
		autoScouterPanel.init();
		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(this.getClass(), "icon.png");
		navButton = NavigationButton.builder()
			.tooltip("CoX Auto Scouter")
			.icon(icon)
			.priority(8)
			.panel(autoScouterPanel)
			.build();
		clientToolbar.addNavigation(navButton);
		robot = new Robot();
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
		robot = null;
	}

	protected void enterChambers()
	{
		final Runnable delayClicks = new Runnable()
		{
			public void run()
			{
				if (doEnterChambers)
				{
					GameObject chambers = utils.findNearestGameObject(chambersID);
					if (chambers != null)
					{
						log.info("Entering Chambers..");
						utils.click(chambers.getConvexHull().getBounds());
						reset();
					}
				}
			}
		};
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.schedule(delayClicks, ExtUtilsCustom.randomDelay(400, 900), TimeUnit.MILLISECONDS);
	}

	private void reset()
	{
		checkWhere = false;
		doBoard = false;
		doParty = false;
		doCloseInterface = false;
		doEnterChambers = false;
		doStartRaid = false;
		doLeaveRaid = false;
		doConfirmLeave = false;
		doConfirmStart = false;
	}

	protected void scout()
	{
		checkWhere = true;
	}

	private void clickBoard()
	{
		DecorativeObject board = utils.findNearestDecorObject(recBoardID);
		if (board != null)
		{
			final Runnable delayClicks = new Runnable()
			{
				public void run()
				{
					if (doBoard)
					{
						log.info("Clicking board");
						utils.click(board.getConvexHull().getBounds());
						doBoard = false;
						doParty = true;
						executor.shutdown();
					}
				}
			};
			executor = Executors.newSingleThreadScheduledExecutor();
			executor.schedule(delayClicks, ExtUtilsCustom.randomDelay(500, 1100), TimeUnit.MILLISECONDS);
		}
	}

	private void makeParty()
	{
		Widget widget = client.getWidget(499, 1); //recruiting board window
		if (widget != null)
		{
			Widget partyButton = client.getWidget(499, 58).getChild(0); //'make party' button on the board
			if (partyButton != null)
			{
				final Runnable delayClicks = new Runnable()
				{
					public void run()
					{
						if (doParty)
						{
							Rectangle bounds = partyButton.getBounds();
							log.info("Clicking party button");
							utils.click(bounds);
							doParty = false;
							doCloseInterface = true;
							executor.shutdown();
						}
					}
				};
				executor = Executors.newSingleThreadScheduledExecutor();
				executor.schedule(delayClicks, ExtUtilsCustom.randomDelay(500, 800), TimeUnit.MILLISECONDS);
			}
		}
	}

	private void closeInterface()
	{
		log.info("Attempting to close interface...");
		final Runnable delayClicks = new Runnable()
		{
			public void run()
			{
				if (doCloseInterface)
				{

					Widget closeButton = client.getWidget(507, 2).getChild(11); //close button on board interface
					if (closeButton != null)
					{
						client.getCanvas().requestFocus();
						robot.keyPress(KeyEvent.VK_ESCAPE);
						robot.keyRelease(KeyEvent.VK_ESCAPE);
						//Rectangle bounds = closeButton.getBounds();
						log.info("Closing interface");
						//utils.click(bounds);
						doCloseInterface = false;
						doEnterChambers = true;
						executor.shutdown();
					}
				}
			}
		};
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.schedule(delayClicks, ExtUtilsCustom.randomDelay(500, 800), TimeUnit.MILLISECONDS);
	}

	private void startRaid()
	{
		final Runnable delayClicks = new Runnable()
		{
			public void run()
			{
				if (doStartRaid)
				{
					if (client.getVar(VarClientInt.INTERFACE_TAB) != InterfaceTab.QUEST.getId())
					{
						client.getCanvas().requestFocus();
						robot.keyPress(utils.getTabHotkey(Tab.QUESTS));
					}
					try
					{
						Thread.sleep(20);
					}
					catch (InterruptedException e)
					{
						return;
					}
					Widget raidButton = client.getWidget(500, 14).getChild(0);
					if (raidButton != null)
					{
						Rectangle bounds = raidButton.getBounds();
						log.info("Starting raid..");
						utils.click(bounds);
						doStartRaid = false;
						doConfirmStart = true;
						executor.shutdown();
					}
				}
			}
		};
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.schedule(delayClicks, ExtUtilsCustom.randomDelay(500, 750), TimeUnit.MILLISECONDS);
	}

	private void leaveRaid()
	{
		final Runnable delayClicks = new Runnable()
		{
			public void run()
			{
				GameObject chambersExit = utils.findNearestGameObject(exitStairsID);
				if (chambersExit != null)
				{
					if (doLeaveRaid)
					{
						utils.click(chambersExit.getConvexHull().getBounds());
						doLeaveRaid = false;
						doConfirmLeave = true;
						executor.shutdown();
					}
				}
			}
		};
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.schedule(delayClicks, ExtUtilsCustom.randomDelay(500, 750), TimeUnit.MILLISECONDS);
	}

	private void confirmLeave()
	{
		final Runnable delayClicks = new Runnable()
		{
			public void run()
			{
				Widget confirmLeave = client.getWidget(219, 1).getChild(1);
				if (confirmLeave != null)
				{
					if (doConfirmLeave)
					{
						client.getCanvas().requestFocus();
						type("1");/*
						Rectangle bounds = confirmLeave.getBounds();
						log.info("Leaving raid..");
						utils.click(bounds);*/
						doConfirmLeave = false;
						doBoard = true;
						executor.shutdown();
					}
				}
			}
		};
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.schedule(delayClicks, ExtUtilsCustom.randomDelay(500, 800), TimeUnit.MILLISECONDS);
	}

	private void confirmStart()
	{
		final Runnable delayClicks = new Runnable()
		{
			public void run()
			{
				Widget confirmStart = client.getWidget(219, 1).getChild(1);
				if (confirmStart != null)
				{
					if (doConfirmStart)
					{
						client.getCanvas().requestFocus();
						type("1");
						//Rectangle bounds = confirmStart.getBounds();
						log.info("Confirming start raid..");
						//utils.click(bounds);
						doConfirmStart = false;
						doLeaveRaid = true;
						executor.shutdown();
					}
				}
			}
		};
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.schedule(delayClicks, ExtUtilsCustom.randomDelay(500, 800), TimeUnit.MILLISECONDS);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (checkWhere)
		{
			switch (whereAmI())
			{
				case "outside":
					doBoard = true;
					checkWhere = false;
					break;
				case "inside":
					doStartRaid = true;
					checkWhere = false;
					break;
				case "unknown":
					checkWhere = false;
					break;
			}
		}
		else if (doBoard)
		{
			clickBoard();
		}
		else if (doParty)
		{
			makeParty();
		}
		else if (doCloseInterface)
		{
			closeInterface();
		}
		else if (doEnterChambers)
		{
			enterChambers();
		}
		else if (doStartRaid)
		{
			startRaid();
		}
		else if (doConfirmStart)
		{
			confirmStart();
		}
		else if (doLeaveRaid)
		{
			leaveRaid();
		}
		else if (doConfirmLeave)
		{
			confirmLeave();
		}
	}

	private String whereAmI()
	{
		DecorativeObject board = utils.findNearestDecorObject(recBoardID);
		GameObject exitStairs = utils.findNearestGameObject(exitStairsID);
		if (board != null)
		{
			log.info("outside");
			return "outside";
		}
		else if (exitStairs != null)
		{
			log.info("inside");
			return "inside";
		}
		else
		{
			log.info("unknown");
			return "unknown";
		}
	}

	protected void type(String str)
	{
		for (char ch : str.toCharArray())
		{
			if (ch == '!')
			{
				robot.keyPress(KeyEvent.VK_SHIFT);
				robot.keyPress(KeyEvent.VK_1);
				robot.keyRelease(KeyEvent.VK_SHIFT);
				robot.keyRelease(KeyEvent.VK_1);
			}
			else if (ch == '/')
			{
				robot.keyPress(KeyEvent.VK_SLASH);
				robot.keyRelease(KeyEvent.VK_SLASH);
			}
			else
			{
				char upCh = Character.toUpperCase(ch);
				robot.keyPress((int) upCh);
				robot.keyRelease((int) upCh);
			}
		}
		robot.keyPress(KeyEvent.VK_ENTER);
		robot.keyRelease(KeyEvent.VK_ENTER);
	}

}
