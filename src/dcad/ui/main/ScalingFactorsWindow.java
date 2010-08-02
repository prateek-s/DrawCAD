package dcad.ui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.text.*;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.Caret;
import javax.swing.event.*;

import dcad.Prefs;
/**Class to show GUI for setting of Segmentation scheme
 * and scaling factors of speed and curvature based techniques
 * @author Sunil Kumar
 */
public class ScalingFactorsWindow extends JDialog implements ActionListener
{
	private JPanel m_panel;
	private static final String SET = "set";
	private String segScheme[] = {"Curvature","Speed","Speed+Curvature"};
	ToolBar tbBar = ToolBar.getInstance();
	JPanel panel = new JPanel(null);
    JScrollPane scrollpane = null;
    
    JLabel jtMain = new JLabel();
    JLabel jlCurv = new JLabel();
    JTextField jtCurv = new JTextField();
    JLabel jlSpeed = new JLabel();
    JTextField jtSpeed = new JTextField();
    JLabel jlSegScheme = new JLabel();
    JComboBox jcSegScheme = new JComboBox(segScheme); 
    
	public ScalingFactorsWindow() {
		tbBar.setTestCaseNull();
		m_panel = createPanel();
		scrollpane = new JScrollPane(m_panel);
		setModal(true);
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
	}
	
	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 * @author Sunil Kumar
	 */
	
	public void createAndShowGUI()
	{
		setDefaultLookAndFeelDecorated(false);

		setTitle("Scaling Factors Editor");
		
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		setContentPane(scrollpane);

		//Display the window.
		setSize(650, 600);
		setVisible(true);
	}
	

	public JPanel createPanel()
	{
	  
	    jtMain.setText("Please enter positive integer values");
	    jtMain.setSize(300, 15);
	    jtMain.setLocation(250, 120);
	    jtMain.setBackground(Color.lightGray);    
	    
	    
	   
	    jlCurv.setText("Curvature Scaling Factor");
	    jlCurv.setSize(200, 30);
	    jlCurv.setLocation(200,200);
	    
	   
	    jtCurv.setSize(50, 20);
	    jtCurv.setLocation(450,205);
	    jtCurv.setText(Double.toString(Prefs.getCurvatureScalingFactor()));
	    addWindowListener( new WindowAdapter() {
	          public void windowOpened( WindowEvent e ){
	               jtCurv.requestFocus();
	            }
	          } ); 
	    
	   
	    jlSpeed.setText("Speed Scaling Factor");
	    jlSpeed.setSize(200, 30);
	    jlSpeed.setLocation(200, 250);
	    
	   
	    jtSpeed.setSize(50, 20);
	    jtSpeed.setLocation(450, 255);
	    jtSpeed.setText(Double.toString(Prefs.getSpeedScalingFactor()));
	    jlSegScheme.setText("Segmentation Scheme");
	    jlSegScheme.setSize(200, 30);
	    jlSegScheme.setLocation(200, 300);
	    
	    jcSegScheme.setSize(150, 20);
	    jcSegScheme.setLocation(450, 305);
	    
	    int index = 0;
	    int segSchemeIndex = Prefs.getSegScheme();
	    if(segSchemeIndex == 3){
	    	index = 1;
	    }
	    else if(segSchemeIndex == 4){
	    	index = 2;
	    }
	  
	    jcSegScheme.setSelectedIndex(index);
	    
	    JButton jbSet = new JButton();
	    jbSet.setLocation(275,450);
	    jbSet.setText("Set Values");
	    jbSet.setSize(150,30);
	    jbSet.setActionCommand("set");
		jbSet.addActionListener(this);
	    this.setBackground(Color.WHITE);
	    
        panel.add(jtMain);
        panel.add(jlCurv);
        panel.add(jlSpeed);
        panel.add(jtCurv);
        panel.add(jtSpeed);
        panel.add(jbSet);
        panel.add(jlSegScheme);
        panel.add(jcSegScheme);
        return panel;
	}

	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();
		if(SET.equals(cmd)){ 
			Prefs.setCurvatureScalingFactor(Double.parseDouble(jtCurv.getText()));
			Prefs.setSpeedScalingFactor(Double.parseDouble(jtSpeed.getText()));
			 int index = jcSegScheme.getSelectedIndex();
		
			    if(index == 0){
			    	Prefs.setSegScheme(1);
			    }
			    else if(index == 1){
			    	Prefs.setSegScheme(3);
			    }
			    else {
			    	Prefs.setSegScheme(4);
			    }
			setModal(false);
			this.setVisible(false);
		}
	}
	
	public void setCursorPosition(){
		jtCurv.requestFocus();
	}
		    

}

