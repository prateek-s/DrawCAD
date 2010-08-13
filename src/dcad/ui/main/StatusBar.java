package dcad.ui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatusBar extends JPanel
{
	private JLabel leftLabel, centerLabel, rightLabel;
	private static StatusBar statusBar;

	public static StatusBar getInstance()
	{
		if (statusBar == null)
			statusBar = new StatusBar();
		return statusBar;
	}

	private StatusBar()
	{
		setLayout(new GridLayout(1, 4));
		setPreferredSize(new Dimension(10, 25));
		leftLabel = new JLabel();
		centerLabel = new JLabel();
		rightLabel = new JLabel();
		init();
	}

	public void init()
	{
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(rightLabel, BorderLayout.SOUTH);
		rightPanel.setOpaque(false);

		JPanel leftLabelPanel = new JPanel(new BorderLayout());
		leftLabelPanel.add(leftLabel, BorderLayout.CENTER);
		leftLabelPanel.setOpaque(false);

		JPanel centerLabelPanel = new JPanel(new BorderLayout());
		centerLabelPanel.add(centerLabel, BorderLayout.CENTER);
		centerLabelPanel.setOpaque(false);

		add(leftLabelPanel);
		add(new JLabel(""));
		add(centerLabelPanel);
		add(rightLabel);
		setBackground(Color.LIGHT_GRAY);

		reset();
	}

	public void reset()
	{
		setCoordLabelText("");
		setIDLabelText("");
		setRightLabelText("");
	}
	
	public void setIDLabelText(String text)
	{
		this.centerLabel.setText(text);
	}

	public void setCoordLabelText(String text)
	{
		this.leftLabel.setText(text);
	}

	public void setRightLabelText(String text)
	{
		this.rightLabel.setText(text);
	}
	
}
