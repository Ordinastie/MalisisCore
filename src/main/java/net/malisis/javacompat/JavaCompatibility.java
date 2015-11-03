package net.malisis.javacompat;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.FMLLaunchHandler;

/**
 * <p>
 * Utility class for outdated Java installations.
 * </p>
 *
 * @author diesieben07
 */

public final class JavaCompatibility implements Runnable, HyperlinkListener, ActionListener
{
	private final boolean isWindowsClient = SystemUtils.IS_OS_WINDOWS && FMLLaunchHandler.side().isClient();
	private final Object mutex = new Object();
	final JFrame frame = new JFrame("Java 7 required");

	public JavaCompatibility()
	{}

	private void check()
	{
		if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_7))
			return;

		printLog();

		if (!GraphicsEnvironment.isHeadless())
			displayWindow();

		exit();
	}

	private void printLog()
	{
		Logger logger = LogManager.getLogger("MalisisCore");
		logger.error("");
		logger.error(StringUtils.repeat('=', 80));
		logger.error("MalisisCore requires Java 7 to be installed.");
		logger.error("Please install the latest Java 8 appropriate for your System from https://java.com/download/"
				+ (isWindowsClient ? " or use the latest launcher from https://minecraft.net/" : ""));
		logger.error("If Java 7 is already installed, please make sure the right Java version is for the current profile in the Minecraft launcher.");
		logger.error("Thank you. The game will exit now.");
		logger.error(StringUtils.repeat('=', 80));
		logger.error("");
	}

	private void displayWindow()
	{
		SwingUtilities.invokeLater(this);
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (mutex)
		{
			try
			{
				mutex.wait();
			}
			catch (InterruptedException e)
			{
				//ignore
			}
		}
	}

	@Override
	public void run()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception ignored)
		{}

		JLabel label = new JLabel();
		Font font = label.getFont();

		JTextPane text = new JTextPane();
		text.setContentType("text/html");
		text.setText(getHtml(font));

		text.setEditable(false);
		text.setHighlighter(null);
		text.setBackground(label.getBackground());

		text.setMargin(new Insets(20, 20, 20, 20));

		text.addHyperlinkListener(this);

		JButton button = new JButton("Exit");
		button.addActionListener(this);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(text);
		panel.add(button);
		panel.add(Box.createVerticalStrut(20));

		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		//frame.setResizable(false);
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent e)
			{
				synchronized (mutex)
				{
					mutex.notify();
				}
			}
		});

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.toFront();
	}

	private String getHtml(Font font)
	{
		// create some css from the label's font
		StringBuilder style = new StringBuilder("font-family:" + font.getFamily() + ";").append("font-weight:")
				.append(font.isBold() ? "bold" : "normal").append(";").append("font-size:").append(font.getSize()).append("pt;");

		return "<html><body style=\""
				+ style
				+ "\">"
				+ "<strong>MalisisCore requires Java 7 to be used.</strong><br /><br />"
				+ "Please install the latest Java 7 appropriate for your system from <a href=\"https://java.com/download/\">java.com/download</a>"
				+ (isWindowsClient ? "or use the latest launcher from <a href=\"https://minecraft.net/\">minecraft.net</a>" : "")
				+ "<br /><br />"
				+ "If Java 8 is already installed, please make sure the right Java version is used for the current profile in the Minecraft launcher.<br /><br />"
				+ "The game will exit now." + "</body></html>";
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent e)
	{
		if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
		{
			try
			{
				Desktop.getDesktop().browse(e.getURL().toURI());
			}
			catch (Exception ignored)
			{}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		frame.dispose();
	}

	private void exit()
	{
		try
		{
			Class<?> clazz = Class.forName("java.lang.Shutdown");
			Method method = clazz.getDeclaredMethod("exit", int.class);
			method.setAccessible(true);
			method.invoke(null, -1);
		}
		catch (Throwable t)
		{
			FMLCommonHandler.instance().exitJava(-1, false);
		}
	}

	public static void checkVersion()
	{
		new JavaCompatibility().check();
	}
}
