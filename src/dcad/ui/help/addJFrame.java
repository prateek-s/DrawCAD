package dcad.ui.help;

import dcad.Prefs;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

//import net.sourceforge.jnlp.security.SecurityWarningDialog.DialogType;

public class addJFrame extends JFrame
{
	public void createAndShowGUI( JScrollPane scrollpane)
	{
		
		setContentPane(scrollpane);
		//setContentPane(contentPane)
		//Display the window.
		//setSize(300, 225);
		setVisible(true);
	}
}