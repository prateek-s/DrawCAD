

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
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.model.marker.Marker;
import dcad.model.marker.MarkerEquality;
import dcad.model.marker.MarkerLineAngle;
import dcad.model.marker.MarkerParallel;
import dcad.model.marker.MarkerPerpendicular;
import dcad.process.ProcessManager;
import dcad.process.io.Command;
import dcad.process.recognition.RecognitionManager;
import dcad.process.recognition.marker.MarkerRecognizer;
import dcad.process.recognition.segment.ConvertSegment;
import dcad.process.recognition.stroke.ConvertStrokeType;
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
				jlabel1 = new JLabel("Length");
				jfield1 = new JTextField(5);
				jChange1 = new JButton("Change"); 	jChange1.setActionCommand("lChange") ; 
				jReset1 = new JButton("Reset");		jReset1.setActionCommand("lReset" ) ; 

				jlabel2 = new JLabel("Angle");
				jfield2 = new JTextField(5);
				jChange2 = new JButton("Change"); 	jChange2.setActionCommand("aChange") ; 
				jReset2 = new JButton("Reset");   	jReset2.setActionCommand("aReset") ; 
			}

			else if(type==Segment.CIRCLE) 
			{
				label1 = "Centre" ;
				label2 = "Radius" ;

				jlabel1 = new JLabel("Radius");
				jfield1 = new JTextField(10);
				jChange1 = new JButton("Change"); 	jChange1.setActionCommand("rChange") ;
				jReset1 = new JButton("Reset");		jReset1.setActionCommand("rReset") ;

				jlabel2 = new JLabel("Arc Angle");
				jfield2 = new JTextField(5);
				jChange2 = new JButton("Change");	jChange2.setActionCommand("aaChange") ;
				jReset2 = new JButton("Reset");     jReset2.setActionCommand("aaReset") ;
			}	
			jChange1.addActionListener(ev) ; jReset1.addActionListener(ev) ; jChange2.addActionListener(ev) ;  jReset2.addActionListener(ev) ;

		}

		/**Fill in the text boxes */
		public void update() 
		{

		}
		
		/**
		 * Assign the value to the display field. 
		 * @param field_num
		 * @param field_value
		 * @param level
		 */
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
		if (this.seg==null) { 
			//Try to get the segment through other means. 
			this.seg = dv.getGeoElementClicked();
		}
		this.markers = null ;
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
		updateUI();
		dv.repaint();
	}	


	/******************************************************************************/



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

 	/*********************************************************************************/

 	public void getCircleProperties ()
 	{
		String radius = "" ;
		String angle = "" ;
		String centre = "" ;
		int level1 = PROPERTY ;
		int level2 = PROPERTY ;
		
		Vector constraints = new Vector();
		constraints = seg.getM_constraints();
		Iterator itr = constraints.iterator();
		while (itr.hasNext())
		{
			String cons = itr.next().toString();
			String parsedCons[];
			
			parsedCons = cons.split("[ ]+");
			for(int i =0; i < parsedCons.length ; i++)
			{
				///System.out.println(parsedCons[i]);
				if((parsedCons[1].compareToIgnoreCase("HARD") == 0) && (parsedCons[3].compareToIgnoreCase("Arc") == 0) && (parsedCons[4].compareToIgnoreCase("Radius") == 0)){
					radius = parsedCons[5];
					level1 = HARD ;
				}
				if((parsedCons[1].compareToIgnoreCase("HARD") == 0) && (parsedCons[3].compareToIgnoreCase("Arc") == 0) && (parsedCons[4].compareToIgnoreCase("Angle") == 0)){
					angle = parsedCons[5];
					level2 = HARD ;
				}
			} //NOT HARD
			
		}
		

		SegCircleCurve circle = (SegCircleCurve) this.seg ;
		centre = circle.getM_center().getM_point().toString();
		DecimalFormat twoDForm = new DecimalFormat("#.##");

		if(level1==HARD && level2==HARD) {

			seg_properties.set(1,radius,level1); 
			seg_properties.set(2,angle,level2) ;
			return ;
		}

		if(level1!=HARD) {
			radius = Double.valueOf(twoDForm.format(circle.getM_radius()/GConstants.cmScaleDrawingRatio)).toString(); 
			level1=PROPERTY ;
		}
 
		if(level2!=HARD) {
			angle = Double.valueOf(twoDForm.format(circle.getM_arcAngle())).toString(); 
			level2=PROPERTY;
		}

		seg_properties.set(1,radius,level1); 
		seg_properties.set(2,angle,level2) ;
		
 	}
 

	/************************************************************************************/

	public void ChangeProperty2(int seg_type,String property)
	{
		String val="" ;
		if (property=="length" || property =="radius") {
 			val = seg_properties.jfield1.getText().trim();
 		}
		else if (property == "angle" || property == "angle") {
			val = seg_properties.jfield2.getText().trim() ;
		}
	

		dv.A.A_change_Seg_property(seg, property, val) ;

		UIUpdate() ;
	}

 	/*********************************************************************************/
	
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
		dv.Process_Stroke(strk) ;
	//	displayOptions(strk.getM_segList().elementAt(0), pt) ;
		UIUpdate() ;

	}

 	/*********************************************************************************/
 
	/**
	 * Change stroke to marker. Whether it really is a marker is difficult to guarantee.
	 * @param seg
	 * @return
	 */
	public int ChangeToMarker(Segment seg) 
	{
		Stroke strk = seg.getM_parentStk() ;

//		ConvertStrokeType convStrokeType = new ConvertStrokeType();
//		convStrokeType.ConvertLastDrawnStroke();
//		convStrokeType.ConvertStroke(seg.getM_parentStk()) ;
//		ConvertStrokeType convStrokeType = new ConvertStrokeType();
//		convStrokeType.ConvertLastDrawnStroke();
//		convStrokeType.ConvertStroke(seg.getM_parentStk()) ;
 
		RecognitionManager recogMan = ProcessManager.getInstance().getRecogManager();
		MarkerRecognizer mrkrecog =  recogMan.getMarkerRecognitionMan().getMarkerRecognizer();
		Vector markers = mrkrecog.user_specified_marker(strk) ;
		System.out.println("POSSIBLE MARKERS") ;
		
		for (Object m: markers) { 
			Marker mm = (Marker) m;
			System.out.println(mm.getClass().getSimpleName()) ;
		}
		
		display_marker_options(markers) ;

		return 1;
	}

	
	
	private void display_marker_options(Vector markers)
	{
		if (markers ==null) return;
	CheckboxGroup markergroup = new CheckboxGroup();
	Vector <Checkbox> checkboxes = new Vector<Checkbox>() ;
	    
	for (Object o:markers) 
	{
		Marker m = (Marker) o;
		Checkbox cm = new Checkbox(m.getClass().getSimpleName() , markergroup , false) ;
		cm.addItemListener(this) ; 		//super.add(cm) ;

		checkboxes.add(cm);
	}
	for (Checkbox b: checkboxes) {
		super.add(b) ;
	}
	UIUpdate() ;
	this.markers = markers ;
	}

	Vector markers = new Vector() ;
	
	public int set_as_marker(int type,Vector markers)
	{
		Stroke strk = seg.getM_parentStk() ;
		Marker marker = null ;
		//strk.deleteSegments() ;
		for (Object o: markers) {
			Marker m = (Marker)o ;
			if(m.getM_type() == type) {
				marker  = m ;
			}
		}
		RecognitionManager recogMan = ProcessManager.getInstance().getRecogManager();
		MarkerRecognizer mrkrecog =  recogMan.getMarkerRecognitionMan().getMarkerRecognizer();
		mrkrecog.choose_marker(marker, markers); //sets m_marker.
		
		Vector c = dv.A.Recognize_Constraints(strk , marker.getM_type()) ;
		 // dv.A.Refresh_Drawing(stroke, markers) ;
		dv.repaint();
		dv.A.Refresh_Drawing(strk, c);
		dv.repaint() ;
		return 1;
	}
	
	
	public int set_as_marker3(int type,Vector markers)
	{
		Stroke strk = seg.getM_parentStk() ;
		Marker marker = null ;
		strk.deleteSegments() ;
		strk.setM_type(Stroke.TYPE_MARKER);
		
		for (Object o: markers) {
			Marker m = (Marker)o ;
			if(m.getM_type() == type) {
				marker  = m ;
			}
		}
		RecognitionManager recogMan = ProcessManager.getInstance().getRecogManager();
		MarkerRecognizer mrkrecog =  recogMan.getMarkerRecognitionMan().getMarkerRecognizer();
		mrkrecog.choose_marker(marker, markers); //sets m_marker.
		
		Vector c = dv.A.Recognize_Constraints(strk , marker.getM_type()) ;
		 // dv.A.Refresh_Drawing(stroke, markers) ;
		dv.repaint();
		dv.A.Refresh_Drawing(strk, c);
		dv.repaint() ;
		return 1;
	}
	
	Segment markerSeg1 =null;
	Segment markerSeg2 = null ;
	int MarkerType = 0;
	
	
	public int set_as_marker2(int type,Vector markers)
	{
		if(MarkerType==0)
			MarkerType = type ;
		
		else if (type!=MarkerType) return 0;
		
		Stroke strk = seg.getM_parentStk() ;

		//strk.deleteSegments() ;
		dv.A.A_delete_Element(strk); 
		
		dv.repaint() ;
		
		Marker marker = null ;
		for (Object o: markers) {
			Marker m = (Marker)o ;
			if(m.getM_type() == type) {
				marker  = m ;
			}
		}
		
		Segment s1;
		Segment s2; 
		Vector segvec = new Vector () ;
		
		if(type==Marker.TYPE_EQUALITY)
		{
			MarkerEquality meq = (MarkerEquality)marker ;
			s1 = meq.getM_seg();	
			
			int ready = fill_seg(s1) ;
			if(ready > 0)
			{
				System.out.println("please work"+markerSeg1.toString()+markerSeg2.toString()) ;
				segvec.add(markerSeg1) ; segvec.add(markerSeg2) ;
				dv.A.A_add_markers_simple(type,segvec) ;
			}
		}
		else if (type == Marker.TYPE_PARALLEL) 
		{
			MarkerParallel meq = (MarkerParallel)marker ;
			s1 = meq.getM_seg();	
			
			int ready = fill_seg(s1) ;
			if(ready > 0)
			{
				segvec.add(markerSeg1) ; segvec.add(markerSeg2) ;
				dv.A.A_add_markers_simple(type,segvec) ;
			}
		}

		else if(type == Marker.TYPE_RIGHT_ANGLE)
		{
			MarkerPerpendicular meq = (MarkerPerpendicular)marker ;
			s1 = meq.getM_seg1() ;
			s2 = meq.getM_seg2() ;

			segvec.add(s1) ; segvec.add(s2) ;
			dv.A.A_add_markers_simple(type,segvec) ;
			
		}
		
		dv.repaint() ;
		return 1;
	}
	
	int fill_seg(Segment s)
	{
		if(this.markerSeg1==null || this.markerSeg1==s){
			markerSeg1 = s;
			return 0;
		}
		else 
		{
			markerSeg2 = s;
			return 1 ;
		}
		
	}
	
	/****************************************************************************/
	
	public void UIUpdate() 
	{
		updateUI() ;
		dv.repaint();
		this.markers = null;
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

		}
		else if(cmd.compareToIgnoreCase("aChange")==0) {
			ChangeProperty2(Segment.LINE,"angle") ;
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
		 
		else if(cmd.compareToIgnoreCase("aaChange")==0) {
			ChangeProperty2(Segment.CIRCLE,"angle") ;
		}
		else if(cmd.compareToIgnoreCase("rChange")==0) {
			ChangeProperty2(Segment.CIRCLE,"radius") ;
		}
		else if (cmd.compareToIgnoreCase("rReset")==0) {
			String original = seg_properties.field1 ;
			seg_properties.reset(1) ;
		}
		else if (cmd.compareToIgnoreCase("aaReset")==0) {
			String original = seg_properties.field2 ;   
			seg_properties.reset(2) ;
		}

		else if(cmd.compareToIgnoreCase("Cancel") == 0){
			//dv.logEvent("closeParamsLine();");
			System.out.println("Cancel Clicked");
			//	performCancelActionLineParam();
			//	dv.setParameterWinBitSet(false);
		}
	UIUpdate();
	displayOptions(seg, pt);
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
			ChangeToMarker(this.seg) ;
		}
		
		else if (item.endsWith("Equality")) {
			set_as_marker3(Marker.TYPE_EQUALITY, this.markers); 
		}
		else if (item.endsWith("Perpendicular")) {
			set_as_marker2(Marker.TYPE_RIGHT_ANGLE, this.markers); 
		}
		else if (item.endsWith("Parallel")) {
			set_as_marker2(Marker.TYPE_PARALLEL, this.markers); 
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

	/**
	 * clear highlighted element when mouse exits..
	 */
	public void mouseExited(MouseEvent e)
	{
		//	clearHighlighting();
		GMethods.getCurrentView().repaint();
		removeKeyListener(this);
		DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
		MainWindow.getM_statusBar().setCoordLabelText("");
		this.dv.A.clear_highlighted() ;
		//	helpDrawView.unselectRows();
	}

	public void mouseDragged(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {}

	public void mouseClicked(MouseEvent e) {}

	/**
	 * On mouse enter,show the last drawn stroke and its segments.
	 */
	public void mouseEntered(MouseEvent e) 
	{
		requestFocusInWindow();
		removeKeyListener(this);
		addKeyListener(this);
		DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

		this.dv.A.Highlight_last_stroke() ;
		
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


} // END OF CLASS


/****************************************************************************/

