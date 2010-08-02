package dcad.ui.drawing;

import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JScrollPane;


public class CommandWindow extends JFrame
{
	private CommandView m_panel;
    JScrollPane scrollpane = null;

	public CommandWindow(CommandView panel)
	{
		scrollpane = new JScrollPane(panel);
		m_panel = panel;
	}
	
	private Container createContentPane()
	{
		return scrollpane;
	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */
	public void createAndShowGUI()
	{
		setDefaultLookAndFeelDecorated(false);

		setTitle("Commands");
		
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		setContentPane(createContentPane());

		//Display the window.
		setSize(400, 250);
		setVisible(true);
	}
	
	public CommandView getM_panel()
	{
		return m_panel;
	}

	public void setM_panel(CommandView m_panel)
	{
		this.m_panel = m_panel;
	}
}
