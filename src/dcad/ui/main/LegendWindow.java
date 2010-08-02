package dcad.ui.main;

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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

//import net.sourceforge.jnlp.security.SecurityWarningDialog.DialogType;

public class LegendWindow extends JFrame
{
	private JPanel m_panel = null; 
	JPanel panel = new JPanel(null);
    JScrollPane scrollpane = null;

    public void createAndShowGUI()
	{
		setDefaultLookAndFeelDecorated(false);

		setTitle("Legend");
		
	    setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		setContentPane(scrollpane);

		//Display the window.
		setSize(300, 225);
		setVisible(true);
	}

	public JPanel createPanel()
	{
	    JPanel panel = new JPanel(new BorderLayout());

	    JTable table = new JTable(new MyTableModel());
	    table.setDefaultRenderer(Color.class, new ColorRenderer(true));

	    this.setBackground(Color.WHITE);

	    //Add the scroll pane to this panel.
	    panel.add(table);
	    return panel;
	}
	
	public LegendWindow()
	{
		m_panel = createPanel();
		scrollpane = new JScrollPane(m_panel);
		
	}
	
	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */

	
}

class MyTableModel extends AbstractTableModel 
{
    private String[] columnNames = {"Color","Element"};
    private Object[][] data = 
    {
    		// color codes for drawing
    		{Color.WHITE, "Background"},
    		{new Color(135,205,250), "Anchor-Point"},
    		{Color.BLACK, "Segments"},
    		{Color.LIGHT_GRAY, "Raw-stroke"},
    		{new Color(255, 210, 180), "Line mid-points"},
//    		{new Color(255,165,00), "Soft constraint"},
    		{new Color(110, 110, 110), "Hard constraint"},
    		{Color.LIGHT_GRAY, "Fixed"},
    		{new Color(0,255,127), "Selection"},
    		{Color.MAGENTA, "Highlight"},
    		{Color.PINK, "Selection + Highlight"},
    		{Color.RED, "Selection + Fixed"},
    };

    public int getColumnCount() 
    {
        return columnNames.length;
    }

    public int getRowCount() 
    {
        return data.length;
    }

    public String getColumnName(int col) 
    {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) 
    {
        return data[row][col];
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) 
    {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) 
    {
            return false;
    }
}
