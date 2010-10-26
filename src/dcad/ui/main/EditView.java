

package dcad.ui.main;

import java.awt.CheckboxGroup;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyEventDispatcher;
import java.awt.Point;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;

import static javax.swing.GroupLayout.Alignment.*;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.IndependentConstraint;
import dcad.model.constraint.IndependentPointConstraints;
import dcad.model.constraint.PointSegmentConstraint;
import dcad.model.constraint.RelativeConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.process.io.Command;
import dcad.ui.drawing.*;
import dcad.util.GConstants;
import dcad.util.GMethods;


/**********************************************************************************/

/**
 * This is the pane for editing the segment properties. It's updated when:
 * 1. Stroke is drawn [Recognition etc all done]
 * 2. Segment is selected.
 */

public class EditView extends JPanel implements ActionListener,MouseListener,MouseMotionListener, KeyListener,KeyEventDispatcher, ItemListener
{
	DrawingView dv = null;
	
	private double ANGLE_TEXT_BOX_NULL = -1.0 ;
	private double LENGTH_TEXT_BOX_NULL = 0.0 ;
	private double length = LENGTH_TEXT_BOX_NULL ;
	private double angle = ANGLE_TEXT_BOX_NULL ;
	private String lengthString = "";
	private String angleString = "";

	private Segment seg = null;
	private Stroke stroke = null ;

	Point pt ; //needed for 'command'. 
	int changed_to = 0 ; //segment changed to what type?

	int HARD=0;
	int SOFT=1;
	int PROPERTY=2;

	int seg_type ; //line or arc or something else?

	/****************************************************************************/
	/**Polymorphism etc will be overkill here.
	 */

	class SegmentProperties 
	{
		int seg_type ;
		String label1 ;
		String field1 ;
		int change1 ; //change button clicked by user...
		int reset1 ;
		int level1 ; //type of constraint: property,soft,hard.

		String label2 ;
		String field2 ;
		int change2 ;
		int reset2 ;
		int level2;

		JLabel jlabel1 ;
		JTextField jfield1 ;
		JButton jChange1;
		JButton jReset1 ;

		JLabel jlabel2 ;
		JTextField jfield2 ;
		JButton jChange2 ;
		JButton jReset2 ;

		EditView ev ;

		public SegmentProperties(EditView ev, int type) 
		{
			this.ev = ev ;

			if(type==Segment.LINE) 
			{
				label1 = "Length" ;
				label2 = "Angle" ; 

				// UI now
				jlabel1 = new JLabel("Length :");
				jfield1 = new JTextField(5);
				jChange1 = new JButton("Change"); 	jChange1.setActionCommand("lChange") ; 
				jReset1 = new JButton("Reset");		jReset1.setActionCommand("lReset" ) ; 

				jlabel2 = new JLabel("Angle :");
				jfield2 = new JTextField(5);
				jChange2 = new JButton("Change"); 	jChange2.setActionCommand("aChange") ; 
				jReset2 = new JButton("Reset");   	jReset2.setActionCommand("aReset") ; 
			}

			else if(type==Segment.CIRCLE) 
			{
				label1 = "Centre" ;
				label2 = "Radius" ;

				jlabel1 = new JLabel("Centre :");
				jfield1 = new JTextField(10);
				jChange1 = new JButton("Change"); 	jChange1.setActionCommand("cChange") ;
				jReset1 = new JButton("Reset");		jReset1.setActionCommand("cReset") ;

				jlabel2 = new JLabel("Radius");
				jfield2 = new JTextField(5);
				jChange2 = new JButton("Change");	jChange2.setActionCommand("rChange") ;
				jReset2 = new JButton("Reset");     jReset2.setActionCommand("rReset") ;
			}	
			jChange1.addActionListener(ev) ; jReset1.addActionListener(ev) ; jChange2.addActionListener(ev) ;  jReset2.addActionListener(ev) ;

		}

		/**Fill in the text boxes */
		public void update() 
		{

		}

		public void set(int field_num,String field_value,int level) 
		{
			if(field_num==1) {
				field1 = field_value ;
				level1 = level ;
				jfield1.setText(field1) ; jfield1.setForeground(get_color(level));
			}
			else if(field_num==2) {
				field2 = field_value ;
				level2 = level ;
				jfield2.setText(field2) ;
			}
		}

		/**
		 * Reset the value present in the text box by replacing it with the field value. Level, and any other state, remains unchanged?
		 * @param field_num
		 */
		public void reset (int field_num) 
		{
			if(field_num==1) {
				jfield1.setText(field1) ;
			}
			if(field_num==2) {
				jfield2.setText(field2) ;
			}
		}

		public Color get_color(int level) 
		{
			Color c = new Color(100,100,100) ;
			if(level==0) {
				c = new Color(100,0,0) ;
			}
			return c;
			
		}
		
		public String toString() 
		{
			String s = field1 + "  +   " + field2 ;
			return s ;
		}
	}


	/******************************END CLASS*********************************************/

	private SegmentProperties seg_properties ;

	public EditView() 
	{
		super();
		init();	    
		//displayOptions(seg) ;
	}

	/**********************************************************************************/

	public void init() 
	{
		dv = MainWindow.getDv();
		//	pt = new Point();
		//pt.setLocation(dv.getMousePointerLocation()); //moved over to displayOptions
		
		if (this.seg==null) { 
			//Try to get the segment through other means. 
			this.seg = dv.getGeoElementClicked();
		}
	}	

	/******************************************************************************/

	public void displayCheckboxes(int seg_type) 
	{
		CheckboxGroup group = new CheckboxGroup();
	    Checkbox isLine = new Checkbox("Line", group, false);
	    Checkbox isCircle = new Checkbox("Circle", group, false);
	    Checkbox isMarker =new Checkbox("Marker", group, false);
	    isLine.addItemListener(this) ; isCircle.addItemListener(this) ; isMarker.addItemListener(this) ;
	    
	    super.add(isLine) ; super.add(isCircle) ; super.add(isMarker) ;
	}


	/*********************************************************************************************/
	/**
	 * Display all the options/properties for the given segment. This method is called by drawing view.
	 * @param seg
	 */
	public void displayOptions(Segment seg, Point pt) 
	{
		this.seg = seg ;
		pt = new Point();
//Determining the correct mouse pointer location is tricky. dv.getmouseptrlocation gives null values when it doesnt for the pop-up case. 
		//Because when extra-clicked the mouse-moved listener is not called so the co-ordinates are "correct"! aah!
		/**
		 * Basically pt location is crucial for writetext to work. Can we just choose a point on the current segment? 
		 */
		
		//this.pt = pt ;
		//this.pt = dv.getMousePointerLocation() ;
		this.pt = (Point) seg.getRawPoints().elementAt(0) ; //first point of the stroke? 
		
		if(pt==null) {
			System.out.println("POINT VALUE IS NULLLLLLLLLLLLLLLLLLL") ;
		}
		System.out.println("POINT VALUE IS"+pt.toString()) ;
		if(seg==null) 
		{
			//called spuriously, display 'nothing' and exit.
			return ;

		}

		seg_type = seg.getM_type() ;
		this.seg_properties = new SegmentProperties(this, this.seg_type) ;

		//clear out the UI components.
		super.removeAll();

		//pt.setLocation(dv.getMousePointerLocation());	//TODO: is null

		/****** Initialize UI now */
		JButton jDelete = new JButton("Delete"); 
		super.add(jDelete) ; jDelete.setActionCommand("Delete") ; jDelete.addActionListener(this) ;
		
		/* Check boxes */
		displayCheckboxes(seg_type) ;

		/*Segment option ui components */
		if(seg_type==Segment.LINE) {

		}

		else if (seg_type == Segment.CIRCLE) {

		}

		super.add(seg_properties.jlabel1) ; super.add(seg_properties.jfield1) ; super.add(seg_properties.jChange1) ; super.add(seg_properties.jReset1) ;
		super.add(seg_properties.jlabel2) ; super.add(seg_properties.jfield2) ; super.add(seg_properties.jChange2) ; super.add(seg_properties.jReset2) ;   

		/*********** UI is initialized now. FILL IN ALL PROPERTIES */

		getProperties() ;
	}


	/*************************************************************************/
	/**
	 *  set the textboxes with the values of currently existing constraints
	 */

	public void getProperties()
	{	
		if (seg_type == Segment.LINE) {
			getLineProperties() ;
		}
		else if(seg_type==Segment.CIRCLE) {
			//CIRCLE.
			getCircleProperties() ;
		} 
		UIUpdate();
	}	


	/******************************************************************************/


	public void getCircleProperties ()
	{

	}


	/*********************************************************************************/

	public void getLineProperties() 
	{
		Vector constraints = seg.getM_constraints();
		Iterator itr = constraints.iterator();

		String length = "" ;
		String angle = "" ;
		int level1 = PROPERTY ;	//By default, assume that it's not a hard/soft constraint.
		int level2 = PROPERTY ;

		while (itr.hasNext()) {

			String cons = itr.next().toString();
			//System.out.println("CONSTRAINT IS >>>>>>"+cons) ;
			String parsedCons[];
			parsedCons = cons.split("[ ]+");

			for(int i =0; i < parsedCons.length ; i++)
			{

				if((parsedCons[3].compareToIgnoreCase("Line") == 0) && (parsedCons[4].compareToIgnoreCase("length") == 0))
				{
					length = parsedCons[5] ;
					level1 =  HARD ;
					//seg_properties.set(1, length, level1)  ;

					//length = Double.parseDouble(parsedCons[5]);
				}

				//(parsedCons[1].compareToIgnoreCase("HARD") == 0) && 
				if((parsedCons[3].compareToIgnoreCase("Line") == 0) && (parsedCons[4].compareToIgnoreCase("angle") == 0))
				{
					angle = parsedCons[5] ;
					//seg_properties.field2 = angle ; 
					level2 = SOFT ;
					//seg_properties.set(2,angle,level2) ;
					//  angle = Double.parseDouble(parsedCons[5]) ;
					if(parsedCons[1].compareToIgnoreCase("HARD") == 0)  {
						level2=HARD ;
					}
					//seg_properties.set(2,angle, level2) ;
				}
				else if((parsedCons[3].compareToIgnoreCase("Vertical") == 0) && (parsedCons[4].compareToIgnoreCase("line") == 0))
				{	
					angle = "90.0" ; level2 = HARD ;
					//seg_properties.set(2, angle,level2) ;

				}
				else if((parsedCons[3].compareToIgnoreCase("Horizontal") == 0) && (parsedCons[4].compareToIgnoreCase("line") == 0))
				{	
					angle = "00.0" ; level2 = HARD ;
					// seg_properties.set(2, angle,level2) ;
				}
			} //FOR
		}// WHILE
		/* Constraints have been parsed. Now try getting properties through other means */

		if(length.compareTo("") ==0) {
			Double len =  seg.getM_length();
			len = len/GConstants.cmScaleDrawingRatio; //convert from point-distances to cm
			String lengthp = Double.toString(len) ;
			length = lengthp ;
			level1 = PROPERTY ;
			// seg_properties.set(1, length, 1)  ;
		}

		if (angle.compareTo("")==0) {
			SegLine sl = (SegLine) seg ;
			Double a = sl.getM_angle() ;
			angle = Double.toString(a) ;
			level2 = PROPERTY ;
		}

		Double dl = new Double(length);
		Double da = new Double(angle) ;
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		length = Double.valueOf(twoDForm.format(dl)).toString();
		angle =  Double.valueOf(twoDForm.format(da)).toString();
		
		seg_properties.set(1,length,level1); 
		seg_properties.set(2,angle,level2) ;

	}

	/************************************************************************************/
	/**
	 * apply field values to the segment/stroke. Done using the command-queue .
	 * 
	 */
	public void ChangeProperty(int seg_type,String property)
	{
		String cmd = "" ;
		Point pt = this.pt ;
		/*****LINE****/
		if(seg_type==Segment.LINE) 
		{

			if(property=="length")
			{
				String length ;
				length = seg_properties.jfield1.getText().trim() ;
				if (length.compareTo(seg_properties.field1)==0) //field hasnt changed 
				{
					System.out.println("NOT CHANGED") ;
				}
				else 
				{
					cmd = length ;
					// magic happens!
					System.out.println("POINT VALUE IS"+pt.toString()) ;
					dv.writeText((int)pt.getX(),(int) pt.getY(), cmd) ;
					dv.repaint();
					//Update Properties ?
				}
			}

			else if (property == "angle") 
			{
				String angle ;
				angle = seg_properties.jfield2.getText().trim();
				if (angle.compareTo(seg_properties.field2)==0) //field hasnt changed 
				{
					System.out.println("NOT CHANGED") ;
				}
				else 
				{
					cmd = angle+"a" ;
					dv.writeText((int)pt.getX(),(int) pt.getY(), cmd);
				}
			}
			//CLEAR everything?
		}

		/*****CIRCLE*******/
		else if (seg_type==Segment.CIRCLE) 
		{
			if(property=="centre") {
			}
			else if (property=="radius") {
			}
		}
	}

	public void ChangeProperty2(int seg_type,String property)
	{
		String val="" ;
		if (property=="length") {
			val = seg_properties.jfield1.getText().trim();
		}
		dv.A.A_change_Seg_property(seg, property, val) ;
		Vector c= new Vector() ;

		UIUpdate() ;
	}

	
	/**
	 * Change to type. Processes stroke all over again, but side-steps the recognition process. 
	 * See Stroke.recognizeSegment
	 * @param seg
	 * @param type
	 */
	public void ChangeSegment(Segment seg , int type) 
	{
		Stroke strk ;

		strk = seg.getM_parentStk() ;
		strk.user_given = type ;
		/** Try processing the stroke all over again. deletes the segment currently drawn and runs the segmentation and constraint
		 * algorithms all over again. Bypasses segmentation algorithm in Stroke.recognizesegment because user_given flag is set.
		 */
		dv.ProcessStroke(strk) ;

	}

	/****************************************************************************/
	
	public void UIUpdate() 
	{
		updateUI() ;
		dv.repaint();
	}

	/**************************************************************************/


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
	 */
	public void ChangeLineProperties (JTextField textLineLength, JTextField textLineAngle)
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
		// add it. This condition is to check whether a length constraint exists, delete it and again
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
			System.out.println("Angle is same");
		}
		// if angle text box is now blank and previously it was not blank then remove angle constraint 
		else if((Double.compare(textAngle, ANGLE_TEXT_BOX_NULL)==0) && (Double.compare(angle,ANGLE_TEXT_BOX_NULL) !=0)){
			System.out.println("remove angle constraint");
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
		else
		{
			angleString = textLineAngle.getText().trim() + "a";
			//dv.writeText((int)seg.getSegStart().getX(), (int)seg.getSegStart().getY(), angleString);
			dv.writeText((int)pt.getX(),(int) pt.getY(), angleString);
			dv.repaint();
			System.out.println("Angle is changed to " +  angleString);
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
			System.out.println("Line is same");
		}
		// if textbox is currently null and previously it had some length
		// then we need to remove that length constraint
		else if((Double.compare(textLength, LENGTH_TEXT_BOX_NULL)==0) && (Double.compare(length,0.0) !=0)){
			System.out.println("remove constraint");
			// get the actual constraint
			cons = getSegmentConstraint("line", "length",seg);
			// find the index of constraint in recognize view constraint list
			listIndex = getListConstraintIndex(cons);
			if(listIndex !=-1){
				MainWindow.getRecognizedView().deleteConstraint(listIndex);
				dv.repaint();
				System.out.println("constraint removed ");
			}
		}
		// simply add the constraint and remove if any previous length constraint added
		else{
			lengthString = textLineLength.getText().trim();
			//dv.writeText((int)seg.getSegStart().getX(), (int)seg.getSegStart().getY(), lengthString);
			dv.writeText((int)pt.getX(),(int) pt.getY(), lengthString);
			dv.repaint();
			System.out.println("Length is changed to " +  lengthString);
		}

		//dv.logEvent(Command.PAUSE);
		System.out.println("Submit Clicked");

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
				System.out.println(parsedCons[i]);
				if((parsedCons[1].compareToIgnoreCase("HARD") == 0) && (parsedCons[3].compareToIgnoreCase(element) == 0) && (parsedCons[4].compareToIgnoreCase(constraint) == 0)){
					return cons;
				}
			}
		}
		return null;
	}


	/*************************** ACTION LISTENERS **************************/

	public void actionPerformed(ActionEvent e)
	{
		System.out.println("EVENT"+e.paramString());
		String cmd = e.getActionCommand();

		if(cmd.compareToIgnoreCase("Delete")==0) {
			dv.A.A_delete_Element(seg) ;
			dv.repaint();
		}
		
		else if (cmd.compareToIgnoreCase("lChange")==0) {
			ChangeProperty2(Segment.LINE,"length") ;
			UIUpdate();
			displayOptions(seg, pt);
		}
		else if(cmd.compareToIgnoreCase("aChange")==0) {
			ChangeProperty(Segment.LINE,"angle") ;
		}
		else if (cmd.compareToIgnoreCase("lReset")==0) {
			String original = seg_properties.field1 ;
			seg_properties.reset(1) ;
		}
		else if (cmd.compareToIgnoreCase("aReset")==0) {
			String original = seg_properties.field2 ;
			seg_properties.reset(2) ;
		}
		 
		/******************* CIRCLE *********************/
		 
		else if(cmd.compareToIgnoreCase("cChange")==0) {
			ChangeProperty(Segment.CIRCLE,"centre") ;
		}
		else if(cmd.compareToIgnoreCase("rChange")==0) {
			ChangeProperty(Segment.CIRCLE,"radius") ;
		}
		else if (cmd.compareToIgnoreCase("cReset")==0) {
			String original = seg_properties.field1 ;
			seg_properties.reset(1) ;
		}
		else if (cmd.compareToIgnoreCase("rReset")==0) {
			String original = seg_properties.field2 ;   
			seg_properties.reset(2) ;
		}

		else if(cmd.compareToIgnoreCase("Cancel") == 0){
			//dv.logEvent("closeParamsLine();");
			System.out.println("Cancel Clicked");
			//	performCancelActionLineParam();
			//	dv.setParameterWinBitSet(false);
		}
		//dv.logEvent(Command.PAUSE);
	}
	

	/**
	 * Implementation of the checkbox action listener
	 */
	public void itemStateChanged(ItemEvent arg0) {
	
		String item = arg0.getItem().toString();
		System.out.println(item) ;
			
		if(item.compareTo("Line")==0) {
			ChangeSegment(seg,Segment.LINE) ;
		}

		else if (item.compareTo("Circle")==0) {
			ChangeSegment(seg,Segment.CIRCLE) ;
		}
		else if (item.compareTo("Marker")==0) {
			;
		}

	}


	public void mouseReleased(MouseEvent e)
	{
		if(e.getButton() == MouseEvent.BUTTON2){

		}
		if(e.getButton() == MouseEvent.BUTTON1){

		}
	}

	public void mouseMoved(MouseEvent e)
	{
		//	
	}

	public int SetOption(String property, String val)
	{
		return 0;
	}

	public void mouseExited(MouseEvent e)
	{
		//	clearHighlighting();
		GMethods.getCurrentView().repaint();
		removeKeyListener(this);
		DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
		MainWindow.getM_statusBar().setCoordLabelText("");
		//	helpDrawView.unselectRows();
	}

	public void mouseDragged(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {}

	public void mouseClicked(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) 
	{
		requestFocusInWindow();
		removeKeyListener(this);
		addKeyListener(this);
		DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

		//	if(helpDrawView == null){
		//		helpDrawView = MainWindow.getHelpDrawingView();
		//	}
		//	helpDrawView.selectRows(GConstants.CONSTRAINT_VIEW);
	}

	public void keyTyped(KeyEvent e)
	{
		char c = e.getKeyChar();
		switch (c){
		case KeyEvent.VK_DELETE:
			// Delete the selected elements
			deleteKeyPressed();
			break;
		default:
			break;
		}
	}

	private void deleteKeyPressed()
	{
	}

	public void keyPressed(KeyEvent e) {}

	public void keyReleased(KeyEvent e) {}

	public boolean dispatchKeyEvent(KeyEvent e){
		processKeyEvent(e);
		return true;
	}


}


/****************************************************************************/

