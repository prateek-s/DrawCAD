package dcad.ui.drawing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import dcad.process.io.Command;
import dcad.process.io.CommandQueue;

public class CommandView extends JPanel
{
    private JList list;
    private DefaultListModel listModel;

	public CommandView(CommandQueue cq)
	{
		super(new BorderLayout());
		init(cq);
	}
	
	private void init(CommandQueue cq)
	{
		setAutoscrolls(true);
		this.setBackground(Color.WHITE);

        listModel = new DefaultListModel();

        //Create the list and put it in a scroll pane.
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(list, BorderLayout.CENTER);
        
        Iterator iter = cq.getM_commands().iterator();
        while (iter.hasNext())
		{
			Command comm = (Command) iter.next();
			addCommand(comm.toString());
		}
	}
	
	public void addCommand(String str)
	{
		listModel.addElement(str);
	}
	
	public void clear()
	{
		listModel.removeAllElements();
		this.updateUI();
	}
}
