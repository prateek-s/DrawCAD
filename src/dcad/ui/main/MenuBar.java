package dcad.ui.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;


public class MenuBar extends JMenuBar implements ActionListener
{
	private final static String FILE_MENU = "File";
	private final static String EDIT_MENU = "Edit";
	private final static String SOURCE_MENU = "Source";
	
	private final static String OPENFILE_MENUITEM = "Open...";
	private final static String RELOADFILE_MENUITEM = "Reload";
	private final static String NEWFILE_MENUITEM = "New File";
	private final static String SAVE_MENUITEM = "Save";
	private final static String SAVEAS_MENUITEM = "Save As...";
	private static final String REPLAY = "Replay";
	private static final String CLEAR = "Clear";
	private static final String UNDO = "Undo";
	private static final String REDO = "Redo";
	private static final String ERASE = "Erase";
	private static final String LOGS = "Logs";
	private static final String LEGEND = "Legend";

	private WindowActions winAct = null;
	private static MenuBar m_menuBar;
	JMenu menu, submenu;
	JMenuItem menuItem;
	JRadioButtonMenuItem rbMenuItem;
	JCheckBoxMenuItem cbMenuItem;

	public static MenuBar getInstance()
	{
		if (m_menuBar == null) m_menuBar = new MenuBar();
		return m_menuBar;
	}

	private MenuBar()
	{
		winAct = WindowActions.getInstance();
		init();
	}
	
	public void init()
	{
		// Build the first menu.
		menu = new JMenu(FILE_MENU);
		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription("Contains the basic window actions");
		this.add(menu);

		// file menu ----------------------------
		menuItem = new JMenuItem(OPENFILE_MENUITEM);
		menuItem.addActionListener(this);
		menu.add(menuItem);

		menuItem = new JMenuItem(RELOADFILE_MENUITEM);
		menuItem.addActionListener(this);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
		menu.add(menuItem);

		menuItem = new JMenuItem(NEWFILE_MENUITEM);
		menuItem.addActionListener(this);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		menu.add(menuItem);
		
		menuItem = new JMenuItem(SAVE_MENUITEM);
		menuItem.addActionListener(this);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		menu.add(menuItem);

		menuItem = new JMenuItem(SAVEAS_MENUITEM);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		

		// Build the Edit menu. ------------------------
		menu = new JMenu(EDIT_MENU);
		menu.setMnemonic(KeyEvent.VK_E);
		menu.getAccessibleContext().setAccessibleDescription("Contains the basic editing actions");
		this.add(menu);

		// Edit menu ----------------------------
		menuItem = new JMenuItem(CLEAR);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		menuItem.addActionListener(this);
		menu.add(menuItem);

		menuItem = new JMenuItem(ERASE);
		menuItem.addActionListener(this);
		menu.add(menuItem);

		menuItem = new JMenuItem(UNDO);
		menuItem.addActionListener(this);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		menu.add(menuItem);

		menuItem = new JMenuItem(REDO);
		menuItem.addActionListener(this);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
		menu.add(menuItem);

		menuItem = new JMenuItem(REPLAY);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		

		// Build the Source menu. ------------------------
		menu = new JMenu(SOURCE_MENU);
		menu.getAccessibleContext().setAccessibleDescription("Contains the basic input information");
		this.add(menu);

		menuItem = new JMenuItem(LOGS);
		menuItem.addActionListener(this);
		menu.add(menuItem);

		menuItem = new JMenuItem(LEGEND);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
	}

	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();
		if(cmd.equals(OPENFILE_MENUITEM))
			winAct.openExistingFileAction();
		else if(cmd.equals(RELOADFILE_MENUITEM))
			winAct.reloadFileAction();
		else if(cmd.equals(NEWFILE_MENUITEM))
			winAct.newFileAction();
		else if(cmd.equals(SAVE_MENUITEM))
			winAct.saveMIAction();
		else if(cmd.equals(SAVEAS_MENUITEM))
			winAct.saveAsMIAction();
		else if(CLEAR.equals(cmd))
		{
			if(JOptionPane.showConfirmDialog(this, "The Drawing View will be cleared. \n  This action cannot be undone!\n               Continue?") ==  JOptionPane.OK_OPTION)
				winAct.clearDrawingData();
		} 
		else if(UNDO.equals(cmd))
		{
			winAct.undo();
//			JOptionPane.showMessageDialog(this, UNDO);
		}
		else if(REDO.equals(cmd))
		{
			winAct.redo();
//			JOptionPane.showMessageDialog(this, REDO);
		}
		else if(ERASE.equals(cmd))
		{
			winAct.deleteSelection();			
//			JOptionPane.showMessageDialog(this, ERASE);
		}
		else if(LOGS.equals(cmd))
			winAct.showCommandsWindow();			
		else if(LEGEND.equals(cmd))
			winAct.showLegend();			
		else if(REPLAY.equals(cmd))
			winAct.replay();			
	}
}
