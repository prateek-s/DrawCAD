
package dcad.ui.main;
import java.awt.Component;
import java.awt.Point;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;

import static javax.swing.GroupLayout.Alignment.*;

import dcad.model.constraint.Constraint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.process.io.Command;
import dcad.ui.drawing.*;
import dcad.util.GMethods;

/**Class to set line's parameters through GUI. This needs to be called whenever there is a need to display/edit 
 * the properties of a segment. The properties are hard-coded (line:len/angle , etc). This is called by only 
 * one method in drawingView.
* @author Sunil Kumar, Prateek
*/
// 21-04-10
public class LineParameterWindow implements ActionListener
{
	/* UI components*/
	JLabel labelLineLength = new JLabel("Length :");;
	JTextField textLineLength = new JTextField();
	JLabel labelLineAngle = new JLabel("Angle :");;
    JTextField textLineAngle = new JTextField();     
    JButton buttonSubmit = new JButton("Submit");
    JButton buttonCancel = new JButton("Cancel");

    private static JFrame jf= null;
	DrawingView dv = null;
	
	/* Defaults*/
	private double ANGLE_TEXT_BOX_NULL = -1.0 ;
	private double LENGTH_TEXT_BOX_NULL = 0.0 ;
	private double length = LENGTH_TEXT_BOX_NULL ;
	private double angle = ANGLE_TEXT_BOX_NULL ;
	private String lengthString = "";
	private String angleString = "";

	static Segment seg = null;
	Point pt = null;
	
	public LineParameterWindow() 
	{
		if(jf == null)
		{
		jf = new JFrame();
		
		jf.setTitle("Set Properties of Line");
		dv = MainWindow.getDv();
		pt = new Point();
		pt.setLocation(dv.getMousePointerLocation());
		jf.setLocation((int)pt.getX(),(int) pt.getY());
		jf.setSize(210,140);
		jf.setResizable(false);
		//jf.setState(JFrame.NORMAL);
		//jf.setUndecorated(true);
		//jf.setDefaultCloseOperation(jf.EXIT_ON_CLOSE);
		jf.setVisible(true);
		//jf.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		seg = dv.getGeoElementClicked();
		setPropertiesWhileLoading();
	
		/*Layout*/
		GroupLayout layout = new GroupLayout(jf.getContentPane());
			jf.getContentPane().setLayout(layout);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
        
			layout.setHorizontalGroup(layout.createSequentialGroup()
        	    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        	        .addComponent(labelLineLength)
        	        .addComponent(labelLineAngle)
        	        .addComponent(buttonSubmit))
        	    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        	        .addComponent(textLineLength)
        	        .addComponent(textLineAngle)
        	        .addComponent(buttonCancel))
        	);
        
        
			layout.setVerticalGroup(layout.createSequentialGroup()
        	    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        	        .addComponent(labelLineLength)
        	        .addComponent(textLineLength))
        	    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)        	                
        	                .addComponent(labelLineAngle)
        	                .addComponent(textLineAngle))
        	    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        	                .addComponent(buttonCancel)
        	                .addComponent(buttonSubmit))
        	        
        	);
        /*Layout end*/
			buttonSubmit.addActionListener(this);
			buttonCancel.addActionListener(this);
			
			jf.addWindowListener (new WindowAdapter() {
	            public void windowClosing(WindowEvent e) {
	            	dv.setParameterWinBitSet(false);
	            	jf.dispose();
	            	jf=null;   	
	            }
	          }
			);
		}
		else
		{
			;
		}
	}
	/**Function to get particular constraint on an element if it exists
	 * @author Sunil Kumar
	 */
	/*String representation of constraints everywhere. AAaaargh */
	public static Constraint getSegmentConstraint(String element, String constraint, Segment seg)
	{
		Vector constraints = seg.getM_constraints();
		Iterator itr = constraints.iterator();
		while (itr.hasNext())
		{
			Constraint cons = (Constraint)itr.next();
			String constraintString = cons.toString();
			String parsedCons[];
			
			parsedCons = constraintString.split("[ ]+");
			
			for(int i =0; i < parsedCons.length ; i++){
				///System.out.println(parsedCons[i]);
				if((parsedCons[1].compareToIgnoreCase("HARD") == 0) && (parsedCons[3].compareToIgnoreCase(element) == 0) && (parsedCons[4].compareToIgnoreCase(constraint) == 0)){
						return cons;
				}
			}
		}
		return null;
	}
	
	/**Function to get index of a particular constraint in the
	 * Constraint list (in constraint window) 
	 * @author Sunil Kumar
	 */
	public static int getListConstraintIndex(Constraint cons)
	{
		Vector listConstraints = new Vector();
		listConstraints = MainWindow.getRecognizedView().getListConstraints();
		int index =-1;
		Iterator itr = listConstraints.iterator();
		while (itr.hasNext()){
			index++;
			if(cons.toString().trim().compareToIgnoreCase(itr.next().toString().trim()) == 0){
				return index;
			}
		}
		return -1;
	}
	
	/**Function to perform various checks (add/remove constraints)
	 * after user clicks the submit button
	 * @author Sunil Kumar
	 */
	public void performSubmitActionLineParam(JTextField textLineLength, JTextField textLineAngle)
	{
		
	//	dv.logEvent("LineParameterWindow|performSubmitActionLineParam({java.awt.TextField}" 
	//			+ textLineLength + ", {java.awt.TextField}" + textLineAngle + ");");
		
		Constraint cons = null;
		int listIndex = -1;
		
		
		
		double textAngle = ANGLE_TEXT_BOX_NULL;
		if((textLineAngle.getText().trim().isEmpty()) ){
			textAngle = ANGLE_TEXT_BOX_NULL;  // -1 saying that textBox is NULL 
		}
		else{
			textAngle = Double.parseDouble(textLineAngle.getText().trim()); 
		}
		
		// Checks whether textbox is empty
		double textLength = LENGTH_TEXT_BOX_NULL;
		if((textLineLength.getText().trim().isEmpty()) || Double.compare(Double.parseDouble(textLineLength.getText().trim()),0.0) ==0){
			textLength = LENGTH_TEXT_BOX_NULL;  // -1 saying that textBox is NULL 
		}
		else{
			textLength = Double.parseDouble(textLineLength.getText().trim()); 
		}
		
		// 29-04-10
		// If there is any length constraint on a line and we try to add angle constraint it could not
		// add it. This condition is to check whether a length constraint exits, delete it and again
		// it in order - angle then length 
		if((Double.compare(length,textLength) == 0) && (Double.compare(length, LENGTH_TEXT_BOX_NULL) !=0) && (Double.compare(angle,ANGLE_TEXT_BOX_NULL) == 0) && (!(textLineAngle.getText().trim().isEmpty()))){
			length = LENGTH_TEXT_BOX_NULL;
			cons = getSegmentConstraint("line", "length",seg);
			// find the index of constraint in recognize view constraint list
			if(cons != null){
				listIndex = getListConstraintIndex(cons);
				if(listIndex !=-1){
					MainWindow.getRecognizedView().deleteConstraint(listIndex);
				}
			}
		}
		
		// if angle is same or (angle text box is blank and previously also it was blank )
		if((Double.compare(angle,textAngle) == 0)  
				|| ((Double.compare(textAngle, ANGLE_TEXT_BOX_NULL) == 0 ) 
						&& (Double.compare(angle,ANGLE_TEXT_BOX_NULL) ==0))){
			///System.out.println("Angle is same");
		}
		// if angle text box is now blank and previously it was not blank then remove angle constraint 
		else if((Double.compare(textAngle, ANGLE_TEXT_BOX_NULL)==0) && (Double.compare(angle,ANGLE_TEXT_BOX_NULL) !=0)){
			///System.out.println("remove angle constraint");
			cons = getSegmentConstraint("line", "angle",seg);
			if(cons == null){
				cons = getSegmentConstraint("vertical", "line",seg);
				if(cons == null){
					cons = getSegmentConstraint("horizontal", "line",seg);
				}
			}
			
			// find the index of constraint in recognize view constraint list
			listIndex = getListConstraintIndex(cons);
			if(listIndex !=-1){
				MainWindow.getRecognizedView().deleteConstraint(listIndex);
				dv.repaint();
			}
			
		}
		else{
			angleString = textLineAngle.getText().trim() + "a";
			//dv.writeText((int)seg.getSegStart().getX(), (int)seg.getSegStart().getY(), angleString);
			dv.writeText((int)pt.getX(),(int) pt.getY(), angleString);
			dv.repaint();
			///System.out.println("Angle is changed to " +  angleString);
		}
		
		// 24-05-10
		//this was causing problems
		// once the angle is shifted 
		// line's length constraint was not set because point was not near to the segment 
		pt.setLocation((seg.getSegStart().getX()+seg.getSegEnd().getX())/2, (seg.getSegStart().getY()+seg.getSegEnd().getY())/2);
		
		// checks if the value text box contains and the previous value are same
		// 1. if the textbox's value is same as previous value
		// or 2. if Textbox is null and previous it was also null
		// do nothing
		if((Double.compare(length,textLength) == 0)  
				|| ((Double.compare(textLength, LENGTH_TEXT_BOX_NULL) == 0 ) 
						&& (Double.compare(length,LENGTH_TEXT_BOX_NULL) ==0))){
			///System.out.println("Line is same");
		}
		// if textbox is currently null and previously it had some length
		// then we need to remove that length constraint
		else if((Double.compare(textLength, LENGTH_TEXT_BOX_NULL)==0) && (Double.compare(length,0.0) !=0)){
			///System.out.println("remove constraint");
				// get the actual constraint
				cons = getSegmentConstraint("line", "length",seg);
				// find the index of constraint in recognize view constraint list
				listIndex = getListConstraintIndex(cons);
				if(listIndex !=-1){
					MainWindow.getRecognizedView().deleteConstraint(listIndex);
					dv.repaint();
					///System.out.println("constraint removed ");
				}
		}
		// simply add the constraint and remove if any previous length constraint added
		else{
			lengthString = textLineLength.getText().trim();
			//dv.writeText((int)seg.getSegStart().getX(), (int)seg.getSegStart().getY(), lengthString);
			dv.writeText((int)pt.getX(),(int) pt.getY(), lengthString);
			dv.repaint();
			///System.out.println("Length is changed to " +  lengthString);
		}
		
		//dv.logEvent(Command.PAUSE);
		///System.out.println("Submit Clicked");
		jf.dispose();
		jf = null;
	}
	
	/**Function to close the window
	 * @author Sunil Kumar
	 */
	public void performCancelActionLineParam(){
		
		//dv.logEvent("LineParameterWindow|performCancelActionLineParam();" );
		jf.dispose();
		jf = null;
	}
	
	
	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();

		
		if(cmd.compareToIgnoreCase("Submit") == 0){
			//dv.logEvent("setParamsLine();");
			performSubmitActionLineParam(textLineLength,textLineAngle);
			dv.setParameterWinBitSet(false);
		}
		else if(cmd.compareToIgnoreCase("Cancel") == 0){
			//dv.logEvent("closeParamsLine();");
			///System.out.println("Cancel Clicked");
			performCancelActionLineParam();
			dv.setParameterWinBitSet(false);
		}
		//dv.logEvent(Command.PAUSE);
	}
	
	
	// set the textboxes with the values of currently existing constraints
	/**Function to show previously added constraints in the GUI 
	 * while loading
	 * @author Sunil Kumar
	 */
	public void setPropertiesWhileLoading()
	{
		Vector constraints = new Vector();
		constraints = seg.getM_constraints();
		Iterator itr = constraints.iterator();
		while (itr.hasNext())
		{
			String cons = itr.next().toString();
			String parsedCons[];
			
			parsedCons = cons.split("[ ]+");
			for(int i =0; i < parsedCons.length ; i++){
				///System.out.println(parsedCons[i]);
				if((parsedCons[1].compareToIgnoreCase("HARD") == 0) && (parsedCons[3].compareToIgnoreCase("Line") == 0) && (parsedCons[4].compareToIgnoreCase("length") == 0)){
					length = Double.parseDouble(parsedCons[5]);
				}
				if((parsedCons[1].compareToIgnoreCase("HARD") == 0) && (parsedCons[3].compareToIgnoreCase("Line") == 0) && (parsedCons[4].compareToIgnoreCase("angle") == 0)){
					angle = Double.parseDouble(parsedCons[5]);
				}
				else if((parsedCons[1].compareToIgnoreCase("HARD") == 0) && (parsedCons[3].compareToIgnoreCase("Vertical") == 0) && (parsedCons[4].compareToIgnoreCase("line") == 0)){
					angle = 90.0;
				}
				else if((parsedCons[1].compareToIgnoreCase("HARD") == 0) && (parsedCons[3].compareToIgnoreCase("Horizontal") == 0) && (parsedCons[4].compareToIgnoreCase("line") == 0)){
					angle = 0.0;
				}
			}
			
			if(Double.compare(length, 0.0) == 0){
				// do nothing
			}
			else{
				textLineLength.setText(Double.toString(length));
			}
				
			if(Double.compare(angle, -1.0) == 0){
				// do nothing
			}
			else{
				textLineAngle.setText(Double.toString(angle));
			}
		}
	}
}

