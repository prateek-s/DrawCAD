/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 


package dcad.ui.main;
import java.awt.Component;
import java.awt.Point;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;

import static javax.swing.GroupLayout.Alignment.*;

import dcad.model.constraint.Constraint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.Segment;
import dcad.process.io.Command;
import dcad.ui.drawing.*;
import dcad.util.GMethods;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
/**Class to set Circular arc's parameters through GUI 
 * @author Sunil Kumar
 */
public class CircularArcParameterWindow implements ActionListener{
	JLabel labelArcRadius = new JLabel("Radius :");;
	JTextField textArcRadius = new JTextField();
	JLabel labelArcAngle = new JLabel("Arc Angle :");;
    JTextField textArcAngle = new JTextField();
     
    JButton buttonSubmit = new JButton("Submit");
    JButton buttonCancel = new JButton("Cancel");

     
    private static JFrame jf= null;
	DrawingView dv = null;
	
	private double TEXT_BOX_NULL = -1.0;
	private double radius = 0.0;
	private double angle = 0.0;
	
	private String radiusString = "";
	private String angleString = "";
	private Segment seg = null;
	Point pt = null;
	
	public CircularArcParameterWindow(){
		if(jf == null){
			jf = new JFrame();
			jf.setTitle("Set Properties of Circular Arc");
			dv = MainWindow.getDv();
			//pt = dv.getLocation();
			pt = new Point();
			pt.setLocation(dv.getMousePointerLocation());
			jf.setLocation((int)pt.getX(),(int) pt.getY());
			jf.setSize(210,140);
			jf.setResizable(false);
			
			//jf.setState(JFrame.NORMAL);
			//jf.setUndecorated(true);
			//jf.setDefaultCloseOperation(jf.EXIT_ON_CLOSE);
			jf.setVisible(true);
		
			seg = dv.getGeoElementClicked();
			setPropertiesWhileLoading();
		
			GroupLayout layout = new GroupLayout(jf.getContentPane());
				jf.getContentPane().setLayout(layout);
				layout.setAutoCreateGaps(true);
				layout.setAutoCreateContainerGaps(true);
	        
				layout.setHorizontalGroup(layout.createSequentialGroup()
	        	    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	        	        .addComponent(labelArcRadius)
	        	        .addComponent(labelArcAngle)
	        	        .addComponent(buttonSubmit))
	        	    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	        	        .addComponent(textArcRadius)
	        	        .addComponent(textArcAngle)
	        	        .addComponent(buttonCancel))
	        	);
	        
	        
				layout.setVerticalGroup(layout.createSequentialGroup()
	        	    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	        	        .addComponent(labelArcRadius)
	        	        .addComponent(textArcRadius))
	        	    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)        	                
	        	                .addComponent(labelArcAngle)
	        	                .addComponent(textArcAngle))
	        	    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	        	                .addComponent(buttonCancel)
	        	                .addComponent(buttonSubmit))
	        	        
	        	);
	        
	        buttonSubmit.addActionListener(this);
	        buttonCancel.addActionListener(this);
	       
	        jf.addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent e) {
	            	dv.setParameterWinBitSet(false);
	            	jf.dispose();
	            	jf=null;
	            	
	            }
	          });
		}
		else{
			
		}
	}
	/**Function to perform various checks (add/remove constraints)
	 * after user clicks the submit button
	 * @author Sunil Kumar
	 */
	public void performSubmitActionCirArcParam(){
		Constraint cons = null;
		int listIndex = -1;
		boolean parameterChanged = false;
		
		// checks whether text box of angle is empty or not
		double textAngle = 0.0;
		if((textArcAngle.getText().trim().isEmpty()) || Double.compare(Double.parseDouble(textArcAngle.getText().trim()),0.0) ==0  ){
			textAngle = TEXT_BOX_NULL;  // -1 saying that textBox is NULL 
		}
		else{
			textAngle = Double.parseDouble(textArcAngle.getText().trim()); 
		}
		
		
		// Checks whether textbox is empty
		double textradius = 0.0;
		if((textArcRadius.getText().trim().isEmpty()) || Double.compare(Double.parseDouble(textArcRadius.getText().trim()),0.0) ==0){
			textradius = TEXT_BOX_NULL;  // -1 saying that textBox is NULL 
		}
		else{
			textradius = Double.parseDouble(textArcRadius.getText().trim()); 
		}
		
		// 29-04-10
		// If there is any radius constraint on a circular arc and we try to add angle constraint it could not
		// add it. This condition is to check whether a radius constraint exits, delete it and again
		// it in order - angle then radius 
		if((Double.compare(radius,textradius) == 0) && (Double.compare(radius, TEXT_BOX_NULL) !=0) && (Double.compare(angle,0.0) == 0) && (!(textArcAngle.getText().trim().isEmpty()))){
			radius = 0.0;
			cons = LineParameterWindow.getSegmentConstraint("arc", "radius",seg);
			// find the index of constraint in recognize view constraint list
			listIndex = LineParameterWindow.getListConstraintIndex(cons);
			MainWindow.getRecognizedView().deleteConstraint(listIndex);
		}
		
		
		if((Double.compare(angle,textAngle) == 0)  
				|| ((Double.compare(textAngle, TEXT_BOX_NULL) == 0 ) 
						&& (Double.compare(angle,0.0) ==0))){
			System.out.println("Angle is same");
		}
		else if((Double.compare(textAngle, TEXT_BOX_NULL)==0) && (Double.compare(angle,0.0) !=0)){
			System.out.println("remove angle constraint");
			cons = LineParameterWindow.getSegmentConstraint("arc", "angle",seg);
			// find the index of constraint in recognize view constraint list
			listIndex = LineParameterWindow.getListConstraintIndex(cons);
			MainWindow.getRecognizedView().deleteConstraint(listIndex);
			dv.repaint();
		}
		else{
			angleString = textArcAngle.getText().trim() + "a";
			//dv.writeText((int)seg.getSegStart().getX(), (int)seg.getSegStart().getY(), angleString);
			dv.writeText((int)pt.getX(),(int) pt.getY(), angleString);
			dv.repaint();
			System.out.println("Angle is changed to " +  angleString);
			parameterChanged = true;
		}
		
		
		// 24-05-10
		//this was causing problems
		// once the angle is shifted 
		// line's length constraint was not set because point was not near to the segment 
	/*	SegCircleCurve segCC = (SegCircleCurve)seg;
		Vector segCirCurvePts = segCC.getM_impPoints();
		Point2D centPoint = segCC.getM_center().getM_point();
		Point2D startPoint = segCC.getM_start().getM_point();
		Point2D endPoint = segCC.getM_end().getM_point();
		
		double radius = segCC.getM_radius();
		*/
		if(parameterChanged){
		pt.setLocation((seg.getSegStart().getX()+seg.getSegEnd().getX())/2, (seg.getSegStart().getY()+seg.getSegEnd().getY())/2);
		}
		
		
		// checks if the value text box contains and the previous value are same
		// 1. if the textbox's value is same as previous value
		// or 2. if Textbox is null and previous it was also null
		// do nothing
		if((Double.compare(radius,textradius) == 0)  
				|| ((Double.compare(textradius, TEXT_BOX_NULL) == 0 ) 
						&& (Double.compare(radius,0.0) ==0))){
			System.out.println("Radius is same");
		}
		// if textbox is currently null and previously it had some length
		// then we need to remove that length constraint
		else if((Double.compare(textradius, TEXT_BOX_NULL)==0) && (Double.compare(radius,0.0) !=0)){
			System.out.println("remove constraint");
				// get the actual constraint
				cons = LineParameterWindow.getSegmentConstraint("arc", "radius",seg);
				// find the index of constraint in recognize view constraint list
				listIndex = LineParameterWindow.getListConstraintIndex(cons);
				MainWindow.getRecognizedView().deleteConstraint(listIndex);
				dv.repaint();
				System.out.println("constraint removed ");
			
		}
		// simply add the constraint and remove if any previous length constraint added
		else{
			radiusString = textArcRadius.getText().trim();
			//dv.writeText((int)seg.getSegStart().getX(), (int)seg.getSegStart().getY(), radiusString);
			dv.writeText((int)pt.getX(),(int) pt.getY(), radiusString);
			dv.repaint();
			System.out.println("radius is changed to " +  radiusString);
		}
		
	
		//dv.logEvent(Command.PAUSE);
		System.out.println("Submit Clicked");
		jf.dispose();
		jf = null;
		
	}
	/**Function to close the parameter window
	 * @author Sunil Kumar
	 */
	public void performCancelActionCirArcParam(){
		
		//
		jf.dispose();
		jf = null;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();
	
		
		if(cmd.compareToIgnoreCase("Submit") == 0){
			//dv.logEvent("setParamsCircularArc();");
			performSubmitActionCirArcParam();
			dv.setParameterWinBitSet(false);
		}
		else if(cmd.compareToIgnoreCase("Cancel") == 0){
			//dv.logEvent("closeParamsCircularArc();");
			System.out.println("Cancel Clicked");
			performCancelActionCirArcParam();
			dv.setParameterWinBitSet(false);
		}
		//dv.logEvent(Command.PAUSE);
	}
	
	/**Function to show previously added constraints in the GUI 
	 * while loading
	 * @author Sunil Kumar
	 */
	public void setPropertiesWhileLoading(){
		Vector constraints = new Vector();
		constraints = seg.getM_constraints();
		Iterator itr = constraints.iterator();
		while (itr.hasNext())
		{
			String cons = itr.next().toString();
			String parsedCons[];
			
			parsedCons = cons.split("[ ]+");
			for(int i =0; i < parsedCons.length ; i++){
				System.out.println(parsedCons[i]);
				if((parsedCons[1].compareToIgnoreCase("HARD") == 0) && (parsedCons[3].compareToIgnoreCase("Arc") == 0) && (parsedCons[4].compareToIgnoreCase("Radius") == 0)){
					radius = Double.parseDouble(parsedCons[5]);
				}
				if((parsedCons[1].compareToIgnoreCase("HARD") == 0) && (parsedCons[3].compareToIgnoreCase("Arc") == 0) && (parsedCons[4].compareToIgnoreCase("Angle") == 0)){
					angle = Double.parseDouble(parsedCons[5]);
				}
			}
			
			if(Double.compare(radius, 0.0) == 0){
				// do nothing
			}
			else{
				textArcRadius.setText(Double.toString(radius));
			}
				
			if(Double.compare(angle, 0.0) == 0){
				// do nothing
			}
			else{
				textArcAngle.setText(Double.toString(angle));
			}
		}
	}
}

