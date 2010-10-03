package dcad.ui.drawing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;
import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.lang.Math;

import dcad.Prefs;
import dcad.model.BoundingBox;
import dcad.model.constraint.Constraint;
import dcad.model.constraint.RelativeConstraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.circleArc.circularArcConstraint;
import dcad.model.constraint.collinearity.CollinearLinesConstraint;
import dcad.model.constraint.collinearity.CollinearPointsConstraint;
import dcad.model.constraint.connect.IntersectionConstraint;
import dcad.model.constraint.length.EqualRelLengthConstraint;
import dcad.model.constraint.pointOnSegment.pointOnCircularCurveConstraint;
import dcad.model.constraint.pointOnSegment.pointOnLineConstraint;
import dcad.model.constraint.pointOnSegment.pointOnPointConstraint;
import dcad.model.constraint.points.NoMergeConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.ImpPoint;
import dcad.model.geometry.PixelInfo;
import dcad.model.geometry.SegmentPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.Text;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.SegPoint;
import dcad.model.geometry.segment.Segment;
import dcad.model.marker.*;
import dcad.process.ProcessManager;
import dcad.process.beautification.ConstraintSolver;
import dcad.process.io.Command;
import dcad.process.preprocess.PreProcessingManager;
import dcad.process.preprocess.PreProcessor;
import dcad.process.recognition.RecognitionManager;
import dcad.process.recognition.constraint.pointOnSegmentRecognizer;
import dcad.process.recognition.marker.MarkerRecogManager;
import dcad.process.recognition.marker.MarkerToConstraintConverter;
import dcad.process.recognition.segment.SegmentRecognizer;
import dcad.process.recognition.stroke.StrokeRecognizer;
import dcad.ui.help.HelpDrawingView;
import dcad.ui.help.HelpView;
import dcad.ui.main.CircularArcParameterWindow;
import dcad.ui.main.EditView;
import dcad.ui.main.LineParameterWindow;
import dcad.ui.main.MainWindow;
import dcad.ui.main.StatusBar;
import dcad.ui.main.WindowActions;
import dcad.ui.recognize.RecognizedView;
import dcad.util.GConstants;
import dcad.util.GMethods;
import dcad.util.GVariables;
import dcad.util.Maths;
import dcad.ui.main.ToolBar;
import dcad.ui.drawing.*;

/**
 * Top-Level class. Sets off the Stroke->Segment->Constraint->Draw chain on mouse-press/release events.
 * 'Track' adds the points to the stroke and thus links the gui with the underlying plumbing.
 *
 */

public class DrawingView extends JPanel implements MouseListener, MouseMotionListener, KeyListener,
		KeyEventDispatcher, Serializable
		
	{
	
	private ProcessManager m_processManager;
	/**
	 * Strokes, constraints, markets, anchors, text etc added by recognizeSegmentsAndConstraints (primarily)
	 */
	private DrawingData m_drawData;

	private Stroke m_currStroke;

	private Point m_prevPt;

	private Point m_mousePos;

	public Point getM_mousePos() {
		return m_mousePos;
	}

	public void setM_mousePos(Point mMousePos) {
		m_mousePos = mMousePos;
	}

	private double m_length;

	private Rectangle m_bBox;

	private boolean m_trackFlag = false;

	private boolean m_mouseOverPanel = false;

	private Rectangle m_canvasUsed = new Rectangle(0, 0, 0, 0);

	private boolean m_saved = true;

	private boolean m_newFile = true;

	private Vector m_highlightedElements = null;

	private Vector m_selectedElements = null;

	private String typedText = "";

	private boolean m_elementDragged = false;

	private int m_keyEventCode = -1;

	private boolean m_logBetweenKeyPress = false;

	private Vector m_movedElementsOldPos = null;

	private boolean m_showLastStroke = true;

	private boolean m_keyPressedLogged = false;

	private boolean m_mousePressedLogged = false;
	
	private int m_button_type;
	
	private Vector highlightedSegWhileDragging = null;
	
	/**
	 * Mouse is over this segment.
	 */
	private Segment segUnderCursor = null ;
	
	//added on 02-06-10
	/** if the segment is converted */
	private static boolean isSegmentConverted = false;
	
	public boolean isSegmentConverted() {
		return isSegmentConverted;
	}

	public void setSegmentConverted(boolean isSegmentConverted) {
		DrawingView.isSegmentConverted = isSegmentConverted;
	}

	private static int segmentConvertedTo = -1;
	public int isSegmentConvertedTo() {
		return segmentConvertedTo;
	}

	public void setSegmentConvertedTo(int segmentConvertedTo) {
		DrawingView.segmentConvertedTo = segmentConvertedTo;
	}

	// added on 29-05-10
	/** if stroke is converted from normal to marker or vice-versa */
	private static boolean isStrokeConverted = false;
	
	public boolean isStrokeConverted() {
		return isStrokeConverted;
	}

	public void setStrokeConverted(boolean isStrokeConverted) {
		DrawingView.isStrokeConverted = isStrokeConverted;
	}
	
	/** stroke is converted to Line or circular arc*/
	private static int strokeConvertedTo = -1;
	
	public int getStrokeConvertedTo() {
		return strokeConvertedTo;
	}

	public void setStrokeConvertedTo(int strokeConvertedTo) {
		DrawingView.strokeConvertedTo = strokeConvertedTo;
	}
	
	/** to check whether a new window to set parameters is open or not */
	private static boolean parameterWinBitSet = false;
	
	public boolean isParameterWinBitSet() {
		return parameterWinBitSet;
	}

	public void setParameterWinBitSet(boolean parameterWinBitSet) {
		DrawingView.parameterWinBitSet = parameterWinBitSet;
	}

	// 22-02-10 
	private boolean isEnterKeyClicked = false;
	public boolean isEnterKeyClicked() {
		return isEnterKeyClicked;
	}

	public void setEnterKeyClicked(boolean isEnterKeyClicked) {
		this.isEnterKeyClicked = isEnterKeyClicked;
	}
	
	//19-04-10
	Point pt = null;
	Segment seg = null;
	Segment movedPointSegment = null;
	
	
	public Segment getMovedPointSegment() {
		return movedPointSegment;
	}

	public void setMovedPointSegment(Segment movedPointSegment) {
		this.movedPointSegment = movedPointSegment;
	}

	//26-09-09
	private static DrawGrid drGrid =null;
	private static HelpDrawingView helpDrawView = null;
	
	// to check whether grid is active  or not	
	private static boolean  m_gridActive = true; 
												 
	public boolean isM_gridActive() {
		return m_gridActive;
	}

	public void setM_gridActive(boolean active) {
		m_gridActive = active;
	}

	public DrawingData getDrawData(){
		return m_drawData;
	}
	
	WindowActions winAct = null;
	ToolBar tb = null;
	
	// threshold for distance between start and end points of a segment to be deleted
	final double THRESHOLD = (GConstants.cmScaleDrawingRatio/10)*2; // 2 mm
	
	// 10-05-10
	// to check while dragging line whether it becomes collinear with other line
	private static boolean  m_AreLinesCollinear = false; 
	
	public static boolean isM_AreLinesCollinear() {
		return m_AreLinesCollinear;
	}

	public static void setM_AreLinesCollinear(boolean mAreLinesCollinear) {
		m_AreLinesCollinear = mAreLinesCollinear;
	}
	
	Point pt1 = null;
	Point pt2 = null;
	
	// for line parameter window
	LineParameterWindow lineWindow = null;
	public LineParameterWindow getLineWindow() {
		return lineWindow;
	}

	public void setLineWindow(LineParameterWindow lineWindow) {
		this.lineWindow = lineWindow;
	}

	// for circular arc parameter window
	CircularArcParameterWindow circArcWindow = null;
	
	public CircularArcParameterWindow getCircArcWindow() {
		return circArcWindow;
	}

	public void setCircArcWindow(CircularArcParameterWindow circArcWindow) {
		this.circArcWindow = circArcWindow;
	}

	public DrawingView()
	{
		super();
		init();
		setM_saved(true);
		setM_newFile(true);
		if(helpDrawView == null){
			helpDrawView = MainWindow.getHelpDrawingView();
		}
		if(tb==null){
			tb = MainWindow.getM_toolBar();
		}
		parameterWinBitSet = false;
		//tb.setConvertActiveBit(false);
	}

	public void init()
	{
		removeAll();
		removeKeyListener(this);
		removeMouseListener(this);
		removeMouseMotionListener(this);

		reset();

		setLayout(new BorderLayout());
		setVisible(true);
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);

		setAutoscrolls(true);

		m_processManager = ProcessManager.getInstance();

		// reset the global id
		GeometryElement.globalID = 0;
		m_drawData = new DrawingData();
		m_mousePos = new Point(-1, -1);
		m_trackFlag = false;
		m_mouseOverPanel = false;
		m_showLastStroke = true;
		m_logBetweenKeyPress = false;
		m_saved = true;
		m_newFile = true;
		GVariables.DRAWING_MODE = GConstants.DRAW_MODE;
		setCursor(MainWindow.getM_defCursor());
		m_selectedElements = new Vector();
		m_highlightedElements = new Vector();
		m_movedElementsOldPos = new Vector();
		highlightedSegWhileDragging = new Vector();
		m_keyEventCode = -1;
		m_keyPressedLogged = false;
		m_mousePressedLogged = false;
		typedText = "";
		m_elementDragged = false;
		m_canvasUsed = new Rectangle(0, 0, 0, 0);
		//resizePanel();

		GConstants.drawingRatio = -1;
		//GMethods.getHelpView().initialize(HelpView.afterClear);

		setM_saved(true);
		this.setBackground(GVariables.BACKGROUND_COLOR);
		repaint();
		

		// add commdand to initialize the drawing view.
		logEvent("init();");
		logEvent(Command.PAUSE);
		
		
	}

	
	private void reset()
	{
		m_bBox = null;
		m_prevPt = null;
		m_length = 0.0;
	}

	/**
	 * This draws the component on-screen. AND repaints the entire screen - the grid,segments,text,markers etc. 
	 * Is there any other way??
	 */
	public void paintComponent(Graphics gc)
	{
		super.paintComponent(gc);
		//super.paint(gc);
		//this.setBackground(GVariables.BACKGROUND_COLOR);
		int drViewHeight = getHeight();
		int drViewWidth = getWidth();
		 // if lines are collinear draw a line 
		// 10 - 05 -10
		
		if(isM_AreLinesCollinear()){
			System.out.println("collinear bit set");
			drawCollinearLine(gc, GVariables.SELECTED_FIXED_COLOR);
		}
		else if(pt1 !=null && pt2!=null){
			drawCollinearLine(gc, GVariables.BACKGROUND_COLOR);
		}
			
		// to draw grid
		// is the grid repainted on every stroke?
		 if(drGrid == null){
		 drGrid = new DrawGrid();
		 }
		
		 if(isM_gridActive() == true){  
				drGrid.drawGrid(gc, drViewHeight, drViewWidth, GVariables.GRID_COLOR);
			}
		 else{
			 drGrid.drawGrid(gc, drViewHeight, drViewWidth, GVariables.BACKGROUND_COLOR);
		 }
		 	 
		// Draw last stroke as Raw segment First
		Stroke lastStroke = m_drawData.getLastStroke(true);
		if ((lastStroke != null) && (m_showLastStroke))
			lastStroke.drawRaw(gc);
		
		drawStrokes(gc, m_drawData.getStrokeList());
		/*Nooo! Is everything redrawn all the time!? */
	/*	if(winAct.getUndoBit() == 1){
			drawMarkers(gc, m_drawData.getM_markers(),GVariables.BACKGROUND_COLOR);
			winAct.setUndoBit(0);
		}
		else{*/
			drawMarkers(gc, m_drawData.getM_markers());
		
		//}
		drawTextElements(gc, m_drawData.getM_textElements());
		
	}

	// 10 - 05 -10
	//
	public void drawCollinearLine(Graphics g, Paint color)
	{
		Graphics2D g2 = (Graphics2D)g;
		g2.setPaint(color);
		
		g2.drawLine(pt1.x,pt1.y,pt2.x,pt2.y);
		
	}

	/** draw all the strokes, segments and segment ponints */
	private void drawStrokes(Graphics gc, Vector strokes)
	{
		Iterator iter = strokes.iterator();
		while (iter.hasNext())
		{
			Stroke aStroke = (Stroke) iter.next();
			aStroke.draw(gc);
		}
	}

	/** draw all the markers */
	public void drawMarkers(Graphics gc, Vector markers)
	{
		//Graphics2D g2 = (Graphics2D)gc;
		//g2.setPaint(color);
		// if last drawn stroke is a marker, change the colour
		Iterator iter;
		iter = markers.iterator();
		while (iter.hasNext())
		{
			Marker gEle = (Marker) iter.next();
			gEle.draw(gc);
		}
	}

	/** draw all the text elments */
	private void drawTextElements(Graphics gc, Vector textElements)
	{
		Iterator iter;
		iter = textElements.iterator();
		while (iter.hasNext())
		{
			Text txt = (Text) iter.next();
			if (!txt.isM_used())
				txt.draw(gc);
		}
	}

	/**
	 * Called on mouse released and pressed(left) events. 
	 * @param x
	 * @param y
	 * @param time
	 */
	public void track(int x, int y, long time)
	{
		Graphics gc = this.getGraphics();
		if (isM_trackFlag())
		{
			if (m_prevPt == null)
			{
				if (!GVariables.undoing)
					gc.drawLine(x, y, x, y);
				m_prevPt = new Point(x, y);
				m_bBox = new Rectangle(x, y, x, y);
			} else
			{
				if (!GVariables.undoing)
					gc.drawLine(m_prevPt.x, m_prevPt.y, x, y);
				m_length += Point.distance(m_prevPt.x, m_prevPt.y, x, y);
				m_prevPt.x = x;
				m_prevPt.y = y;
				if (x < m_bBox.x)
					m_bBox.x = x;
				if (x > m_bBox.width)
					m_bBox.width = x;
				if (y < m_bBox.y)
					m_bBox.y = y;
				if (y > m_bBox.height)
					m_bBox.height = y;
			}

			gc.dispose();
			addPointToStroke(x, y, time);
		}

		// Display the mouse position to the status bar
		String statusStr = "";
		if (m_mouseOverPanel)
			statusStr = x + ", " + y;

		updateStatusBar(x, y, " ( Drag ) " + statusStr, "");
	}

	public void addPointToStroke(int x, int y, long time)
	{
		// store current pixle position with timing information
		m_currStroke.addPoint(x, y, time);
/*		if (x > m_canvasUsed.width)
			m_canvasUsed.width = x;
		if (y > m_canvasUsed.height)
			m_canvasUsed.height = y;*/
	}

	/**
	 * Called by mouse-released. Detects Segment Points and does recognition and constraints
	 * @param theStroke
	 * @return Segments-Constraints Vector.
	 */
	public Vector addStroke(Stroke theStroke)
	{
		// Pre-process the stroke to obtain various information from it and
		// perform initial operations if required.
		PreProcessingManager preProcessMan = m_processManager.getPreProManager();
		PreProcessor preProcessor = preProcessMan.getPreProcessor();
		Vector segPts = preProcessor.preProcess(theStroke);

		// add the segment points to the stroke
		theStroke.setM_segPtList(segPts);

		return recognizeSegmentsAndConstraints(theStroke);
	}

	/**
	 * Calls a lot of methods in other parts of the program. This is the key method where everything is set in motion.
	 * @param theStroke
	 * @return
	 */
	public Vector recognizeSegmentsAndConstraints(Stroke theStroke)
	{
		m_currStroke = theStroke;
		recognizeSegments(theStroke);
		adjustStroke(theStroke);
		m_drawData.addStroke(theStroke);
		theStroke.recognizeConnectConstraints(m_drawData.getStrokeList());

		RecognitionManager recogMan = m_processManager.getRecogManager();
		StrokeRecognizer strokeRecog = recogMan.getM_strokeRecogManager().getStrokeRecognizer();
		// to set stroke's properties in case convert option is clicked
		theStroke.setStrokeConverted(isStrokeConverted);
		theStroke.setStrokeConvertedTo(strokeConvertedTo);
		int stkType = strokeRecog.findType(theStroke);

		// We have found whether the stroke is marker or not. Now remove the
		// intersection constraints
		// At this point, the circular arc will have circularArcConstraint.
		// Don't remove it.
		Iterator iter = theStroke.getM_segList().iterator();
		while (iter.hasNext())
		{
			Segment seg = (Segment) iter.next();
			Vector vecTemp = seg.getM_constraints();
			for (int i = 0; i < vecTemp.size(); i++)
			{
				Constraint c = (Constraint) vecTemp.elementAt(i);
				if (c instanceof IntersectionConstraint)
					c.remove();
				else if (c instanceof circularArcConstraint && stkType == Stroke.TYPE_MARKER)
					c.remove();
			}
		}
		if (stkType == Stroke.TYPE_MARKER)
		{
			theStroke.setM_type(Stroke.TYPE_MARKER);
			// as this is a marker.. for each of its segments clear the points
			// Vector for each of their constraints
			Marker marker = strokeRecog.getMarker();
			if (marker != null)
				addGeoElement(marker);
			UpdateUI(1,m_drawData.getM_constraints());
			return null;
		} else
		{
			// 08-02-10
			//snapIPs(m_drawData.getAllAnchorPoints());
			snapIPs();
			// find the constraints between the segments of this stroke and
			// the segments of ALL the previous strokes.
			Vector constraints = theStroke.recognizeAllConstraints(m_drawData.getStrokeList());
			if (constraints != null)
				m_drawData.addConstraints(constraints);
			/*
			 * RecognizedView rv = MainWindow.getRecognizedView();
			 * rv.reset(m_drawData.getM_constraints());
			 */
			return constraints;
		}
	}

	public void recognizeSegments(Stroke theStroke)
	{
		//m_currStroke = theStroke;
		RecognitionManager recogMan = m_processManager.getRecogManager();
		// identify segments
		try
		{
			SegmentRecognizer segmentRecog = recogMan.getSegmentRecogMan().getSegmentRecognizer();
			// added on 23-02-10
			// if this stroke is converted, then no need to call segment recognizer 
			
			theStroke.recognizeSegments(segmentRecog);
			// displaying of recognized segment can be done somewhere else as
			// well.
			if (!GVariables.undoing)
				theStroke.drawSegments(getGraphics());
		} catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "Error Occured in recognize segments : "
					+ e.getMessage());
			e.printStackTrace();
		}
	}

	public void addGeoElement(GeometryElement gEle)
	{
		if (gEle instanceof Stroke)
			m_drawData.addStroke((Stroke) gEle);
		else if (gEle instanceof Text)
			m_drawData.addTextElement((Text) gEle);
		else if (gEle instanceof Marker)
			m_drawData.addMarker((Marker) gEle);
	}

	
	public void removeGeoElement(GeometryElement gEle)
	{
		if (gEle instanceof Stroke)
			m_drawData.removeStroke((Stroke) gEle);
		else if (gEle instanceof Text)
			m_drawData.removeTextElement((Text) gEle);
		else if (gEle instanceof Marker)
			m_drawData.removeMarker((Marker) gEle);
	}

	
	public void mouseClicked(MouseEvent e)
	{
	}

	public Vector isPtOnAnyAnchorPoint(Point pt)
	{
		Vector selectedAps = new Vector();
		Vector apList = m_drawData.getAllAnchorPoints();
		for (int i = 0; i < apList.size(); i++)
		{
			AnchorPoint ap = (AnchorPoint) apList.get(i);
			if (ap.containsPt(pt))
				selectedAps.add(ap);
		}
		return selectedAps;
	}

	public Segment isPtOnAnySegment(Point2D pt)
	{
		// find the closest segment
		Vector segList = m_drawData.getAllSegments();
		Iterator itr = segList.iterator();
		while (itr.hasNext())
		{
			Segment seg = (Segment) itr.next();
			if (seg.containsPt(pt)) 
				return seg;
		}
		
		return null;
	}

	public GeometryElement isPtOnAnyText(Point2D pt)
	{
		Iterator itr = m_drawData.getM_textElements().iterator();
		while (itr.hasNext())
		{
			Text txt = (Text) itr.next();
			if (txt.containsPt(pt))
				return txt;
		}
		return null;
	}

	public Marker isPtOnAnyMarker(Point2D pt)
	{
		Vector markerList = m_drawData.getM_markers();
		Iterator itr = markerList.iterator();
		while (itr.hasNext())
		{
			Marker marker = (Marker) itr.next();
			if (marker.containsPt(pt))
				return marker;
		}
		return null;
	}

	public Stroke isPtOnAnyStroke(Point2D pt)
	{
		for (int i = m_drawData.getStrokeList().size() - 1; i >= 0; i--)
		{
			Stroke stroke = (Stroke) m_drawData.getStrokeList().get(i);
			if (stroke.containsPt(pt))
				return stroke;
		}
		return null;
	}

	public Vector isPtOnGeometryElement(Point2D pt)
	{
		Vector gEles = new Vector();
		Vector aps = isPtOnAnyAnchorPoint(m_mousePos);
		if ((aps != null) && (aps.size() > 0))
		{
			gEles.addAll(aps);
			return gEles;
		}

		// check if the mouse is close to any other geometry element
		GeometryElement gEle = isPtOnAnySegment(m_mousePos);
		if ((gEle != null) && (gEle.isEnabled()))
		{
			gEles.add(gEle);
			return gEles;
		}

		// check if point is on any Text element
		gEle = isPtOnAnyText(m_mousePos);
		if ((gEle != null) && (gEle.isEnabled()))
		{
			gEles.add(gEle);
			return gEles;
		}

		// check if point is on any marker
		gEle = isPtOnAnyMarker(m_mousePos);
		if ((gEle != null) && (gEle.isEnabled()))
		{
			gEles.add(gEle);
			return gEles;
		}

		return gEles;
	}

	public void snapAllImpPoints(Vector SegmentList)
	{
		Iterator iter = SegmentList.iterator();
		while (iter.hasNext())
		{
			Segment seg = (Segment) iter.next();
			snapSegment(seg);
		}
	}

	public void snapSegment(Segment seg)
	{
		// snap AnchorPoints
		boolean changed = false;
		Vector v = seg.getM_impPoints();
		for (int w = 0; w < v.size(); w++)
		{
			ImpPoint ip = (ImpPoint) v.elementAt(w);
			changed = snapIP(ip) || changed;
		}

		Vector closePt = seg.findClosestSeg(m_drawData.getAllSegments());
		if ((closePt != null) && (closePt.size() == 2))
		{
			Point2D fromPt = (Point2D) closePt.get(0);
			Point2D toPt = (Point2D) closePt.get(1);
			if (!fromPt.equals(toPt))
				seg.move(fromPt, toPt);
		}
	}

	/**
	 * Snap all the IPs of the Vector
	 * 
	 * @param ips
	 */
/*	public void snapIPs(Vector ips)
	{
		
		Iterator iter = ips.iterator();
		while (iter.hasNext())
		{
			ImpPoint ip = (ImpPoint) iter.next();
			snapIP(ip);
		}
	}*/
// 08-02-10
/**function snap the ips
 * if a new stroke is drawn, check for if end points of this 
 * stroke needs to be snapped.	
  * In drawing mode, if a circular arc is drawn, then do not merge 
	 * center of circle with any other point.
	 * Merge it only in Edit mode(when an element is being dragged)
 * @author Sunil Kumar
 */
	public void snapIPs()
	{
		// found on 22-02-10 when we type a text then also the tool is in Drawing mode.
		// so checked here whether the Enter key is clicked do not do this
		if(!isEnterKeyClicked()){
			if(GVariables.getDRAWING_MODE() == GConstants.DRAW_MODE && m_drawData.getStrokeList().size()>=1){
				Vector segPts = new Vector();
				segPts = null;
				if(m_currStroke != null){
					if(m_currStroke.getM_segPtList().size()!=0){
                  		segPts = m_currStroke.getM_segPtList();
            
                  			Iterator iter1 = segPts.iterator();
                  			while (iter1.hasNext()){
                  				SegmentPoint point = (SegmentPoint)iter1.next();
                  				Point2D segPoint = point.getM_point();	
			
                  				Vector anchorPoints = m_drawData.getAllAnchorPoints();
                  				Iterator iter2 = anchorPoints.iterator();
		
                  				while (iter2.hasNext()){
                  					ImpPoint ip = (ImpPoint) iter2.next();
                  					if(ip.getM_point()!=null){
                  						if((ip.getM_point()).equals(segPoint) 
                  								|| (ip.getM_point().distance(segPoint) < (((GConstants.cmScaleDrawingRatio)/10)*2))){
                  							snapIP(ip);					// distance < 2 mm
                  						}
                  					}
                  				}
                  			}
					}
				}
			}
			else{
				if (isM_elementDragged() && (m_highlightedElements.size() > 0)){
					System.out.println("highlighted elements count : " + m_highlightedElements.size());
					Iterator iter = m_highlightedElements.iterator();
					while (iter.hasNext()){
						GeometryElement seg = (GeometryElement)iter.next();
						if(seg instanceof SegCircleCurve){
							SegCircleCurve segCC = (SegCircleCurve)seg;
							Vector segCirCurvePts = segCC.getM_impPoints();
							iter = segCirCurvePts.iterator();
							while (iter.hasNext()){
								snapIP((ImpPoint)iter.next());
							}	
						 //snapIP((ImpPoint)iter.next());
						}
						else if(seg instanceof SegLine){
							System.out.println("Seg Line");
							SegLine segL = (SegLine) seg; 
							Vector segLinePts = segL.getM_impPoints();
							iter = segLinePts.iterator();
							while (iter.hasNext()){
								snapIP((ImpPoint)iter.next());
							}	
						}
						else if(seg instanceof ImpPoint){
							System.out.println("ImpPoint");
							snapIP((ImpPoint)seg);
						}
						else{
							System.out.println("this is else part");
						}
					}
				}
			}
		}
	}
	/**Function to check if this Imp point is the center of Circular arc 
	 * @author Sunil Kumar
	 */
	public boolean iterateParentVector(ImpPoint ip){
		Vector parents = ip.getAllParents();
		Iterator iter = parents.iterator();
		while (iter.hasNext())
		{
			Segment seg = (Segment) iter.next();
		// if this important point belongs to Circular arc and is center point
		// don't merge	
			if(seg instanceof SegCircleCurve){
			   SegCircleCurve segCC = (SegCircleCurve)seg;
			   if((segCC.getM_center().getM_point()).equals(ip.getM_point())){
				   return true;
			   }   
			}
			else if(seg instanceof SegLine){
				SegLine sLine = (SegLine)seg;
				if((sLine.getM_middle().getM_point()).equals(ip.getM_point())){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean snapIP(ImpPoint ip)
	{
		// It may happen that this point was replaced by some point in the outer
		// call of this function.
		// In that case, it is no longer required to check for a close point for
		// this point.
		if (ip.getM_point() == null)
			return false;

		// find all the imp points close to this ip
		ImpPoint closestIP = ip.findClosestIP(m_drawData.getAllAnchorPoints());
		Point2D closestPt;
		if (closestIP != null)
			closestPt = closestIP.getM_point();
		else
			closestPt = null;

		// snap ip to the closest PT;
		if (closestPt != null)
		{
			// added on 09-02-10
			// if this important point belongs to Circular arc and is center point
			// don't merge
			// ip.move(closestPt.getX(), closestPt.getY());
			if(GVariables.getDRAWING_MODE() == GConstants.DRAW_MODE){
				if(!iterateParentVector(ip)){
					if(!iterateParentVector(closestIP)){
						mergePoints(closestIP, ip);
						return true;
					}
				}
			}
			else{
				mergePoints(closestIP, ip);
				return true;
			}
		} 
		else{
			closestPt = ip.findClosestSeg(m_drawData.getAllSegments());
			if (closestPt != null)
			{
				ip.move(closestPt.getX(), closestPt.getY());
				return true;
			}
		}
		return false;
	}

	
	public void mergePoints(ImpPoint ip1, ImpPoint ip2)
	{	
		if(constraintsHelper.getNoMergeConstraintBetweenPoints((AnchorPoint)ip1,(AnchorPoint)ip2) != null)
			return;
		
		// Move the old point and all segments connected to it to the new point
		ip1.move(ip2.getX(), ip2.getY());

		Vector parents = ip1.getAllParents();
		// ISHWAR Parents are removed in the changepoint function.
		// So, eventually this parents.size will get to be 0 and the loop will
		// break
		// Found on 12/1/2008
		for (int l = 0; l < parents.size();)
			((Segment) parents.elementAt(l)).changePoint(ip1, ip2);
		if (ip1.isFixed() && !ip2.isFixed())
			ip2.setFixed(true);
		ip1.deleteSelf();
		int tempIndex;
		if (m_highlightedElements.contains(ip1))
		{
			tempIndex = m_highlightedElements.indexOf(ip1);
			m_highlightedElements.remove(ip1);
			addHighLightedElement(ip2);
			// m_highlightedElements.add(tempIndex,ip2);
		}
		if (m_selectedElements.contains(ip1))
		{
			tempIndex = m_selectedElements.indexOf(ip1);
			m_selectedElements.remove(ip1);
			m_selectedElements.add(tempIndex, ip2);
		}
		postMergeOperations(ip2);
	}

	private void removeConstraintsOfType(AnchorPoint ap, Class className)
	{
		Vector constraints = ap.getConstraintsByType(className);
		for (int i = 0; i < constraints.size(); i++)
		{
			Constraint c = (Constraint) constraints.get(i);
			if (!constraintsHelper.arePointsUnique(c))
			{
				c.remove();
				justAddedConstraints.remove(c);
			}
		}
	}

	public void postMergeOperations(ImpPoint ip)
	{
		AnchorPoint ap = (AnchorPoint) ip;

		// two of the three collinear points may get merged. In that case, the
		// constraint is no longer required so remove it
		removeConstraintsOfType(ap, CollinearPointsConstraint.class);
		removeConstraintsOfType(ap, pointOnLineConstraint.class);
		removeConstraintsOfType(ap, pointOnCircularCurveConstraint.class);
		removeConstraintsOfType(ap, pointOnPointConstraint.class);
		removeConstraintsOfType(ap, CollinearLinesConstraint.class);
	}

	public void mouseEntered(MouseEvent e)
	{
		requestFocusInWindow();
		m_mouseOverPanel = true;
		removeKeyListener(this);
		addKeyListener(this);
		DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
		m_mousePos.x = e.getX();
		m_mousePos.y = e.getY();
		//if(index == 0)
		//helpDrawView.selectRows(GConstants.LEFT_CLICK);
		//index++;
		highlightTableRowsSelectedElems();
	
	}
	/**Function to highlight table rows in Help table
	 * @author Sunil Kumar
	 */
	public void highlightTableRowsSelectedElems()
	{
		if(m_selectedElements != null){
		int size = m_selectedElements.size();
		if(size!=0){
			if(size == 1){
				GeometryElement g1 = (GeometryElement)m_selectedElements.get(0);
					if((g1 instanceof SegLine)){
						helpDrawView.selectRows(GConstants.SELECT_LINE);
					}
					else if(g1 instanceof SegCircleCurve){
						//System.out.println("Highlight Point");
						helpDrawView.selectRows(GConstants.SELECT_ARC);
					}
					else if(g1 instanceof AnchorPoint){
						helpDrawView.selectRows(GConstants.SELECT_POINT);
					}
			}
			else if(size == 2){
				GeometryElement g1 = (GeometryElement)m_selectedElements.get(0);
				GeometryElement g2 = (GeometryElement)m_selectedElements.get(1);
				
				if(g1 instanceof AnchorPoint){
					if(g2 instanceof AnchorPoint){
						helpDrawView.selectRows(GConstants.SELECT_POINTS);
					}
					if(g2 instanceof SegCircleCurve){
						helpDrawView.selectRows(GConstants.SELECT_POINT_ARC);
					}
					if(g2 instanceof SegLine){
						helpDrawView.selectRows(GConstants.SELECT_POINT_LINE);
					}
				}
				
				else if(g1 instanceof SegLine){
					if(g2 instanceof AnchorPoint){
						helpDrawView.selectRows(GConstants.SELECT_POINT_LINE);
					}
					if(g2 instanceof SegCircleCurve){
						helpDrawView.selectRows(GConstants.SELECT_LINE_ARC);
					}
					if(g2 instanceof SegLine){
						helpDrawView.selectRows(GConstants.MOVE_DELETE_ELEMENTS);
					}
				}
				
				else if(g1 instanceof SegCircleCurve){
					if(g2 instanceof AnchorPoint){
						helpDrawView.selectRows(GConstants.SELECT_POINT_ARC);
					}
					if(g2 instanceof SegCircleCurve){
						helpDrawView.selectRows(GConstants.MOVE_DELETE_ELEMENTS);
					}
					if(g2 instanceof SegLine){
						helpDrawView.selectRows(GConstants.SELECT_LINE_ARC);
					}
				}
				
				
			}
		}
		}
	}
	public void mouseExited(MouseEvent e)
	{
		m_mouseOverPanel = false;
		removeKeyListener(this);
		DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
		MainWindow.getM_statusBar().setCoordLabelText("");
		helpDrawView.unselectRows();
		m_mousePos.x = e.getX();
		m_mousePos.y = e.getY();
	}

	/**
	 * todo
	 */
	public void mousePressed(MouseEvent e)
	{
	//	if(!isParameterWinBitSet()){
			mousePressed(e.getButton(), e.getClickCount(), e.getX(), e.getY(), e.getWhen());
	//	}
	//	else{
	//		 JOptionPane.showMessageDialog(MainWindow.getDv(),"Please close the properties window first");
	//	}
			
	}

	public void mousePressed(int buttontype, int clickcount, int x, int y, long time)
	{
		repaint();
		setM_button_type(buttontype);
		if (buttontype == MouseEvent.BUTTON1)
		{
			// 04-10-09 to highlight rows in HELP table
			
			helpDrawView.selectRows(GConstants.LEFT_CLICK);
			if (clickcount == 1){
				mouseButton1Pressed(x, y, time);
			}
		} 
		else if (buttontype == MouseEvent.BUTTON2){
			// 04-10-09 to highlight rows in HELP table
			if(m_highlightedElements.size() != 0 ){
			helpDrawView.selectRows(GConstants.MIDDLE_CLICK);
			}
			mouseButton2Pressed(x, y);
		}
		else if (buttontype == MouseEvent.BUTTON3){
			// 04-10-09 to highlight rows in HELP table
			if(m_highlightedElements.size() != 0 ){
				if(m_keyEventCode == KeyEvent.VK_SHIFT){
					if(m_highlightedElements.size() == 1){
						GeometryElement ge = (GeometryElement) m_highlightedElements.get(0);
						if(ge instanceof ImpPoint){
							ImpPoint ip = (ImpPoint)ge;
							if(ip.getAllParents().size() > 1){
								helpDrawView.unselectRows();
								helpDrawView.selectRow(5);
							}
						}
					}
				}
				else{
					helpDrawView.selectRows(GConstants.RIGHT_CLICK);
				}
			}
			mouseButton3Pressed(x, y);
		}
		m_mousePos.x = x;
		m_mousePos.y = y;
	}

	private void partitionLineSegments(int x, int y)
	{
		if (m_highlightedElements.get(0) instanceof AnchorPoint)
		{
			System.out.println("Anchor point clicked");
			System.out.println(" \n\n\n Shift key clicked \n\n\n ");
			AnchorPoint ap = (AnchorPoint) m_highlightedElements.get(0);
			Vector v = constraintsHelper.getPointSegmentConstraintsOfPoints(ap);
			if (v.size() > 0)
			{
				pointOnLineConstraint c;
				for (int i = 0; i < v.size(); i++)
				{
					if (v.get(i) instanceof pointOnLineConstraint)
					{
						// This has one problem.
						// If I add one point by shift + right click and then if
						// I resegment the same segment, I'll loose that point.
						c = (pointOnLineConstraint) v.get(i);
						SegLine l = (SegLine) c.getM_seg();
						Stroke parentStroke = l.getM_parentStk();
						Point2D p = new Point2D.Double(x, y);

						int index1 = l.getM_rawStartIdx(), index2 = l.getM_rawEndIdx();
						double d1 = l.getM_start().distance(p);
						double d = l.getM_length();
						int index3 = index1 + (int) (d1 * (index2 - index1) / d);

						// parentStroke.addSegment(new
						// SegLine(l.getM_start(),ap,parentStroke,-1,-1));
						// parentStroke.addSegment(new
						// SegLine(l.getM_end(),ap,parentStroke,-1,-1));

						Vector v1 = new Vector();
						v1.add(l.getM_start().getM_point());
						v1.add(p);
						SegLine l1 = new SegLine(v1);
						l1.setM_rawStartIdx(index1);
						l1.setM_rawEndIdx(index3);
						l1.setM_parentStk(parentStroke);
						parentStroke.addSegment(l1);

						Vector v2 = new Vector();
						v2.add(l.getM_end().getM_point());
						v2.add(p);
						SegLine l2 = new SegLine(v2);
						l2.setM_rawStartIdx(index3);
						l2.setM_rawEndIdx(index2);
						l2.setM_parentStk(parentStroke);
						parentStroke.addSegment(l2);

						l.delete();
					}
				}
				snapIPsAndRecalculateConstraints();
			} else
			{

				System.out.println("\n\n\n\n\nSeparating the points\n\n\n\n\n");
				Vector oldParentsVector = ap.getAllParents();
				if (oldParentsVector.size() > 1)
				{
					int siz = oldParentsVector.size();
					for(int i=0;i<siz;i++)
					{
						Segment seg1 = (Segment)oldParentsVector.get(i);
						for(int j=i+1;j<siz;j++)
						{
							Segment seg2 = (Segment)oldParentsVector.get(j);
							Vector tempVector = constraintsHelper.getRelativeConstraintsBetween2Segments(seg1,seg2);
							constraintsHelper.removeConstraints(tempVector);
						}
					}
					
					Vector newAPs = new Vector();
					while (oldParentsVector.size() != 0)
					{
						Segment seg = (Segment) oldParentsVector.get(0);
						if (seg instanceof SegPoint)
						{
							oldParentsVector.remove(0);
							continue;
						}
						Vector newParentsVector = new Vector();
						newParentsVector.add(seg);
						AnchorPoint newAP = new AnchorPoint( new Point2D.Double(ap.getX(), ap.getY()), newParentsVector );
						newAPs.add(newAP);

						Vector resultingConstraints = new Vector();
						Vector otherConstraints = new Vector();

						if (seg instanceof SegLine)
						{
							SegLine l = (SegLine) seg;
							otherConstraints = (Vector) l.getM_start().getConstraints().clone();
							if (l.getM_start() == ap)
								otherConstraints = (Vector) l.getM_end().getConstraints().clone();
							resultingConstraints = constraintsHelper.minusInverse(ap
									.getConstraints(), otherConstraints);
						} else if (seg instanceof SegCircleCurve)
						{
							SegCircleCurve c = (SegCircleCurve) seg;
							AnchorPoint ap1 = c.getM_start(), ap2 = c.getM_end();
							if (ap == c.getM_start())
							{
								ap1 = c.getM_end();
								ap2 = c.getM_center();
							} else if (ap == c.getM_end())
							{
								ap1 = c.getM_start();
								ap2 = c.getM_center();
							}
							otherConstraints = constraintsHelper.minusInverse(ap1.getConstraints(),
									ap2.getConstraints());
							resultingConstraints = constraintsHelper.minusInverse(ap
									.getConstraints(), otherConstraints);
						} else
							System.out.println("Segment is an instance of unknown shape !!!");

						int size = resultingConstraints.size();
						for (int i = 0; i < size; i++)
						{
							Constraint c = (Constraint) resultingConstraints.get(i);
							c.changePoint(ap, newAP);
							((AnchorPoint) newAP).addConstraint(c);
						}
						ap.getConstraints().removeAll(resultingConstraints);
						seg.changeAnchorPoint(ap, newAP);
						seg.changePoint4Segment(ap, newAP);
						oldParentsVector.remove(0);
					}
					ap.deleteConstraints();
					justAddedConstraints = new Vector();
					siz = newAPs.size();
					for(int i=0;i<siz;i++)
					{
						AnchorPoint ap1 = (AnchorPoint)newAPs.get(i);
						Constraint c;
						for(int j=i+1;j<siz;j++)
						{
							AnchorPoint ap2 = (AnchorPoint)newAPs.get(j);
							c = new NoMergeConstraint(ap1,ap2,Constraint.HARD,false);
							m_drawData.addConstraint(c);
							justAddedConstraints.add(c);
						}
					}
					snapIPsAndRecalculateConstraints();
					UpdateUI(1,justAddedConstraints);
				}

			}
		}

	}

	public void mouseButton3Pressed(int x, int y)
	{

		logEvent("mouseMoved({int}" + x + ", {int}" + y + ");");
		logEvent("mouseButton3Pressed({int}" + x + ", {int}" + y + ");");
		setM_mousePressedLogged(true);

		// order log events is imp
		// check if any of the highlighted elemets are selected, in which case
		// the operation will be performed on all the selected elements.
		// Note that selected elements may or may not be connected.
		if (m_highlightedElements.size() > 0)
		{
			
			// added on 08-05-10
			// to put highlighted element if it is a line in a segment 
			if(m_highlightedElements.size() == 1){
				System.out.println("Element Selected : " + m_highlightedElements.get(0).getClass());
				String parsedCons[];
				
				parsedCons = m_highlightedElements.get(0).getClass().toString().split("[ ]+");
				
					if(parsedCons[1].compareToIgnoreCase("dcad.model.geometry.segment.SegLine") == 0){
							System.out.println("Equal");
							Segment segm = (Segment)m_highlightedElements.get(0);
							
							if(segm instanceof SegLine)
								highlightedSegWhileDragging.add(segm);
							System.out.println("Added highlighted element : " + (Segment)highlightedSegWhileDragging.get(0));
							System.out.println("size : "+ highlightedSegWhileDragging.size());
					}
			}
			
			//***************************************************
			if ((m_keyEventCode == KeyEvent.VK_SHIFT))
			{
				partitionLineSegments(x, y);
			} else
			{
				GVariables.setDRAWING_MODE(GConstants.EDIT_MODE);
				if (!smartMergeSelectedEleToHighLightedEle())
				{
					clearSelection();
				}
			}
		} 
	}

	private void addConstraintsForMarkers()
	{
		justAddedConstraints = new Vector();

		// check if new markers are added.
		if ((m_drawData.isUnusedMarker()) || (m_drawData.isUnusedText()))
		{
			setM_mousePressedLogged(true);
			// dont show the last stroke
			m_showLastStroke = false;

			// recognize markers
			RecognitionManager recogMan = ProcessManager.getInstance().getRecogManager();
			MarkerRecogManager markerMan = recogMan.getMarkerRecognitionMan();
			MarkerToConstraintConverter converter = markerMan.getM_markerConverter();

			// check if there are new markers related to the text objects
			Vector newMarkers = converter.recognizeTextAsMarkers(m_drawData.getM_markers(),
					m_drawData.getM_textElements(), m_drawData.getAllSegments(),
					m_selectedElements, m_highlightedElements);

			// add the newly obtained markers, if any
			m_drawData.getM_markers().addAll(newMarkers);

			// recognize the set of markers as constraints
			Vector constraints = converter.recognizeMarkersAsConstraints(m_drawData.getM_markers(),
					m_drawData.getM_textElements(), m_drawData.getAllSegments());

			if (constraints != null && constraints.size() > 0)
			{
				Cursor prevCursorType = this.getCursor();
				this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				
				
				if (ConstraintSolver.addConstraintsAppliedUsingMarker(constraints) != null)
				{
					m_drawData.addConstraints(constraints);
					justAddedConstraints.addAll(constraints);
					//GMethods.getHelpView().initialize(HelpView.afterDrawing);
					snapIPsAndRecalculateConstraints();
				}
				//*************************************************************
				else
				{
					JOptionPane.showMessageDialog(this,"The constraint could not be added");
					updateConstraints(constraintsHelper.getListOfConstraints(m_drawData.getAllAnchorPoints()),Constraint.HARD);
					//GMethods.getHelpView().initialize(HelpView.constraintAddingFailed);
				}
				resizePanel();
				repaint();
				this.setCursor(prevCursorType);

//				snapIPsAndRecalculateConstraints();
			}

		}

	}

	private void fixElements(int x, int y)
	{
		logEvent("mouseMoved({int}" + x + ", {int}" + y + ");");
		if (!smartMergeSelectedEleToHighLightedEle())
		{
			clearSelection();
		}

		logEvent("mouseButton2Pressed({int}" + x + ", {int}" + y + ");");
		setM_mousePressedLogged(true);

		// don't TOGGLE the fixed flag of all the elements, at least on
		// element is not fixed, first fix it
		// else if all elements were fixed then unfix all.
		boolean setFixed = false;
		Iterator iter = m_highlightedElements.iterator();
		while (iter.hasNext())
		{
			GeometryElement element = (GeometryElement) iter.next();
			if (!element.isFixed())
			{
				setFixed = true;
				break;
			}
		}

		// set the fixed flag of all the elements, depending on the setFixed
		// Flag
		iter = m_highlightedElements.iterator();
		while (iter.hasNext())
		{
			GeometryElement element = (GeometryElement) iter.next();
			element.setFixed(setFixed);
		}
	}

	public void mouseButton2Pressed(int x, int y)
	{
		// check if any of the highlighted elemets are selected, in which case
		// the operation will be performed on al the selected elements.
		// Note that selected elements may or may not be connected.
		if (m_highlightedElements.size() > 0)
		{
			fixElements(x, y);
		} else
		{
			logEvent("mouseMoved({int}" + x + ", {int}" + y + ");");
			logEvent("mouseButton2Pressed({int}" + x + ", {int}" + y + ");");
			addConstraintsForMarkers();
		}
	}

	private boolean smartMergeSelectedEleToHighLightedEle()
	{
		boolean intersect = false;

		// check if any of the highlighted element is a selected element as
		// well.
		Iterator iter = m_selectedElements.iterator();
		while (iter.hasNext())
		{
			GeometryElement ele = (GeometryElement) iter.next();
			if (m_highlightedElements.contains(ele))
			{
				intersect = true;
				break;
			}
		}
		if (intersect)
		{
			// some highlighted element were in present in the selected list, so
			// add all selected elements to the highlighted list.
			// first remove all selected elemnts for highlighted list (this is
			// to avoid duplicates objects in the list)
			m_highlightedElements.removeAll(m_selectedElements);

			// set the highlight flag for all the selected objects
			iter = m_selectedElements.iterator();
			while (iter.hasNext())
			{
				GeometryElement element = (GeometryElement) iter.next();
				element.setHighlighted(true);
			}

			// add all selected elements to the highlighted list.
			addHighLightedElements(m_selectedElements);
			// m_highlightedElements.addAll(m_selectedElements);
		}
		return intersect;
	}

	public void mouseButton1Pressed(int x, int y, long time)
	{
		justAddedConstraints = new Vector();

		logEvent("mouseMoved({int}" + x + ", {int}" + y + ");");
		logEvent("mouseButton1Pressed({int}" + x + ", {int}" + y + ", {long}" + time + ");");
		setM_mousePressedLogged(true);

		if (m_keyEventCode == -1)
		{
			setM_trackFlag(true);
			reset();
			m_currStroke = new Stroke();
			track(x, y, time);
			clearSelection();
		} else
		{
			if (m_keyEventCode == KeyEvent.VK_CONTROL)
			{
				performSelection(x, y);
			} 
			else if ((m_keyEventCode == KeyEvent.VK_SHIFT)){
				if (m_showLastStroke){
					Vector constraints = performSegRecycling(x, y);
					if ((constraints != null) && (constraints.size() > 0)){
						if (ConstraintSolver.addConstraintsAfterDrawing(constraints) != null)
							justAddedConstraints.addAll(constraints);
					}
					snapIPsAndRecalculateConstraints();

				}
			} 
			else{
				clearSelection();
			}
		}
	}

	private void clearSelection()
	{
		// some other element was clicked .. so clear all the selection
		Iterator iter = m_selectedElements.iterator();
		while (iter.hasNext())
		{
			GeometryElement element = (GeometryElement) iter.next();
			element.setSelected(false);
		}
		m_selectedElements.clear();
		GMethods.getRecognizedView().updateSelection(m_selectedElements);
	}
	// added on 19-04-10
	// checks whether the point is an child of how many elements 
	/**Function to return given point belongs to how many elements 
	 * @author Sunil Kumar
	 */
	
	int ptOnSegments(Point pt){
		int count = 0; 
		Vector segList = m_drawData.getAllSegments();
		Iterator itr = segList.iterator();
		while (itr.hasNext())
		{
			Segment seg = (Segment) itr.next();
			Vector impPoints = new Vector();
			impPoints = seg.getM_impPoints();
			Iterator iter = impPoints.iterator();
			while(iter.hasNext()){
				AnchorPoint pt1 = (AnchorPoint) iter.next();
				if ((Double.compare(pt.getX(),pt1.getX())== 0) && (Double.compare(pt1.getY(), pt.getY())==0)){ 
					count++;
				}
			}
		}
		return count;
	}
	
	public void mouseReleased(MouseEvent e)
	{
		
		mouseReleased(e.getX(), e.getY(),e.getButton());
	}

	// This contains all constraints added after drawing or movement.
	// They will be shown in the recognized view. And user can remove some of
	// them if required.
	Vector justAddedConstraints = new Vector();
//***************************************************************
	//19-04-10
	//functions to get mouse click's point location
	public void setMousePointerLocation(Point pt1){
		if(pt == null){
			pt = new Point(); 
		}
		System.out.println("setmousepointer");
		pt.setLocation(pt1);
	}
	
	public Point getMousePointerLocation(){
		return pt;
	}
	
	public void setGeoElementClicked(Segment segSelected){
		seg = segSelected;
	}
	
	public Segment getGeoElementClicked(){
		return seg;
	}
	
//*******************************************************************	
	/**Function to check whether Mouse_Button 3 is clicked on any element
	 * If yes then show appropriate parameter window
	 * @author Sunil Kumar
	 */
	public void showElementPropertiesWindow(int x, int y,int buttonType){
		if((m_currStroke==null || 
				(m_currStroke.getLength()==0 )) 
				&&  buttonType == MouseEvent.BUTTON1 
				&&  m_keyEventCode!= KeyEvent.VK_SHIFT
				&& this.hasFocus()){
			Point pt = new Point();
			pt.setLocation(x, y);
			
			setMousePointerLocation(pt) ;
			
			Segment seg = null;
			// check whether the point is on any segment
			seg = isPtOnAnySegment((Point2D)pt); 
			this.segUnderCursor = seg ;
			int count = ptOnSegments(pt);
			//System.out.println("Count =" +count);
			// count <2 
			// count = 0 means that the point is not an anchor point
			// count >=1 means that the anchor point is a child of # elements
			if((seg)!=null && count < 2 && m_keyEventCode!= KeyEvent.VK_CONTROL  ){
				//System.out.println("Control key not pressed");
				setGeoElementClicked (seg);
				UpdateUI(2, null) ;
			}
		}
	}
	
	
	public void mouseReleased(int x, int y,int buttonType)
	{
		boolean extraClick = false;
		
		//System.out.println("mouse released ");
		
		Point pt1 = new Point(x,y) ;
		setMousePointerLocation(pt1) ;

		//added on 19-04-10 for showing GUI to set properties of an element
		if(!isParameterWinBitSet()) //not already open
		{
			System.out.println("Enter show elements properties ");
			showElementPropertiesWindow(x, y, buttonType);
		}
		
	//*******************************************	
		//If the user is clicking in the blank area of the screen, do nothing. Don't show the point
		//If user clicks twice at the same place, 2nd time, m_currStroke is set to null. So, the condition checking it for null is required 
		if( m_keyEventCode == -1 && m_currStroke!=null && m_currStroke.getLength() < Prefs.getAnchorPtSize() )
			extraClick = true;

		if (isM_mousePressedLogged())
		{
			logEvent("mouseReleased({int}" + x + ", {int}" + y + ", {int}" + buttonType + ");");
			// log only if no key is pressed
			if (m_keyEventCode == -1)
				logEvent(Command.PAUSE);
			setM_mousePressedLogged(false);
		}

		justAddedConstraints = new Vector();
		//Moved this out of the block checked by the following condition.
		if(m_keyEventCode== KeyEvent.VK_SHIFT){
			
		}
		else if (GVariables.getDRAWING_MODE() == GConstants.DRAW_MODE)
		{
			setM_trackFlag(false);
			if(!extraClick)
			{
				//added on 25-02-10
				//if (m_currStroke != null
				Vector constraints = ProcessStroke(m_currStroke) ;
			}
		}
		else
		{
			if(handleMouseDragEditMode(x, y))
			{
				if (isM_elementDragged() && (m_highlightedElements.size() > 0)){
					snapIPsAndRecalculateConstraints();
					
					// 06-10-09
				/*	if(winAct == null){
						winAct = WindowActions.getInstance();
					}
					if(winAct.getUndoVector() != null){
					if(winAct.getUndoIndex() < (winAct.getUndoVector().size()-1)){
						winAct.removeUndoVectorElements();
						}
					}
					winAct.addElementToUndoVector();*/
				}
				
		
			}
			else{}
				//GMethods.getHelpView().initialize(HelpView.movementFailed);

			//added on 11-05-10
			// check to add whether dragged element was collinear
			if(isM_AreLinesCollinear()){
				System.out.println("mouse released unsetting bit ");
				setM_AreLinesCollinear(false);
				repaint();
			}
		
			
			this.setCursor(Cursor.getDefaultCursor());

		}
		
		//Show all constraints in the system
		if(extraClick) 
		{
			Vector c = m_drawData.getM_constraints();
			UpdateUI(1, c) ;
		}
		
		// do common housekeeping
		m_currStroke = null;
		// mouse was in the Edit Mode, change it to draw mode
		GVariables.setDRAWING_MODE(GConstants.DRAW_MODE);

		setM_elementDragged(false);
		// so that the current element is selected
		mouseMoved(x, y);
		removeElementsWithStartEndPtMerged();
	
		// if converted bit now set to false
		setStrokeConverted(false);
		setSegmentConverted(false);
		// check if the area of this panel is to be increased to accomodate the
		// drawing
		resizePanel();
	}
	
	
	public Vector ProcessStroke(Stroke strk)
	{
		Vector constraints = null ;
		if (strk != null )
		{
		//1-6-2008
		//m_currStroke.smoothen();
			
			constraints = addStroke(strk); //Does all the work
			
		//add the constraints to the Constraint solver
			if (strk.getM_type() == Stroke.TYPE_NORMAL)
			{
				if ((constraints != null) && (constraints.size() > 0))
				{
					if (ConstraintSolver.addConstraintsAfterDrawing(constraints) != null)
						justAddedConstraints = constraints;
				}
				snapIPsAndRecalculateConstraints();
				//GMethods.getHelpView().initialize(HelpView.afterDrawing);
			}
		//25-3-2008 Added this line.
			else {
				addConstraintsForMarkers(); 
			}
		//show the last stroke
			m_showLastStroke = true;
			//TODO: Semantics of this..
			addToUndoVector();
			repaint();
			
			
			
		}
		return constraints;
	}
	
	
	// added on 18-05-10
	// to remove segment lines and their respective constraints
	// if start point and end point of the segment are merged
	// or the distance between two points is negligible
	/**Function to remove segment lines and their respective constraints
	 * if start point and end point of the segment are merged
	 * or the distance between two points is negligible
	 * @author Sunil Kumar
	 */
	
	void removeElementsWithStartEndPtMerged(){
		
			Vector segList = m_drawData.getAllSegments();
			if(segList != null ){
				Iterator segIter = segList.iterator();
				while(segIter.hasNext()){
					Segment segment  = (Segment)segIter.next();
					if(segment instanceof SegLine){
						
						if(segment.getSegStart().distance(segment.getSegEnd()) < THRESHOLD){
							System.out.println("segment removed");
							segment.clearConstraints(Constraint.SOFT);
							segment.clearConstraints(Constraint.HARD);
							segment.delete();
						}
					}
				}
			}
		
		repaint();
	}
	

	private Vector findAnchorPoints(Vector elements)
	{
		Vector anchorPts = new Vector();
		Iterator iter;
		iter = elements.iterator();
		while (iter.hasNext())
		{
			GeometryElement ele = (GeometryElement) iter.next();
			if (ele instanceof AnchorPoint)
			{
				if (!anchorPts.contains(ele))
					anchorPts.add(ele);
			} else if (ele instanceof Segment)
			{
				Segment seg = (Segment) ele;

				// avoid duplicates
				anchorPts.removeAll(seg.getM_impPoints());
				// add all the imp points of this segment
				anchorPts.addAll(seg.getM_impPoints());
			}
		}

		return anchorPts;
	}
	
	//// added on 10-05-10
	// sort anchor points by x or y points
	/**function to sort anchor points by x or y points
	 * @author Sunil Kumar
	 */
	public void sortAnchorPoints(double segPoints[][], int sortBy){
			    int n = segPoints.length;
			    System.out.println("total points " + n );
		    for (int pass=1; pass < n; pass++) {  // count how many times
		        for (int i=0; i < n-pass; i++) {
		            if (segPoints[i][sortBy] > segPoints[i+1][sortBy]) {
		                // exchange elements
		                double tempX = segPoints[i][0];
		                double tempY = segPoints[i][1];
		                segPoints[i][0] = segPoints[i+1][0];
		                segPoints[i][1] = segPoints[i+1][1];
		                segPoints[i+1][0] = tempX;
		                segPoints[i+1][1] = tempY;
		            }
		        }
		    }
		    
		    System.out.println("Sorted Points");
		    for(int i = 0; i < n; i++){
		    	System.out.println("Points  X: " + segPoints[i][0] + "Y: " + segPoints[i][1]);
		    }
		    
		   // return segPoints;
	}

	/**function to find is the given line horizontal
	 * @author Sunil Kumar
	 */
	public boolean isLineHorizontal(double angle1)
	{
		// checked it by a margin of .5 degrees on either sides
		/*p: 0.5 degrees is the magic constant here*/
		//System.out.println("horizontal");
		if((((Double.compare(angle1, 0.0) == 0) || (Double.compare(angle1, 0.0) > 0))
				&& (Double.compare(angle1, 0.5) == 0) || (Double.compare(angle1, 0.5) < 0))
				|| ((Double.compare(angle1, 179.5) == 0) || (Double.compare(angle1, 179.5) > 0))){
			return true;
		}
		return false;
			
	}	
	
	/**function to find is the given line vertical
	 * @author Sunil Kumar
	 */
	public boolean isLineVertical(double angle1){
		// checked it by a margin of .5 degrees on either sides
		//System.out.println("vertical");
		if(((Double.compare(angle1, 89.5) == 0) || (Double.compare(angle1, 89.5) > 0))
				&& (Double.compare(angle1, 90.5) == 0) || (Double.compare(angle1, 90.5) < 0)){
			return true;
		}
		return false;
			
	}
	
	
	// added on 10-05-10
	// find the end point and starting point of other line according to orientation
	/**function to find the Middle points i.e, end point of first segment and 
	 * starting point of other, between whom we have to draw a line 
	 * @author Sunil Kumar
	 */
	public double[][] findMiddlePtsWhileMoving(Segment seg1, Segment seg2){
		int sortByX = 0;
		int sortByY = 1;
		
		// get seg points
	    double x1 = seg1.getSegStart().getX();
	    double y1 = seg1.getSegStart().getY();
	    double x2 = seg1.getSegEnd().getX();
	    double y2 = seg1.getSegEnd().getY();
	    
	    double x3 = seg2.getSegStart().getX();
	    double y3 = seg2.getSegStart().getY();
	    double x4 = seg2.getSegEnd().getX();
	    double y4 = seg2.getSegEnd().getY();
		
	    
		double [][] segPoints = new double[][] {{x1, y1},
											{x2, y2},
											{x3, y3},
											{x4, y4}
											};
		
		// get angle of these seg lines  with origin
		double angle1 = Math.abs(Maths.AngleInDegrees(x1, y1, x2, y2));
		double angle2 = Math.abs(Maths.AngleInDegrees(x3, y3, x4, y4));
		// if lines are horizontal
		if(isLineHorizontal(angle1) && isLineHorizontal(angle2)){
			sortAnchorPoints(segPoints, sortByX);
		}
		// if lines are vertical
		else if(isLineVertical(angle1) && isLineVertical(angle2)){
				sortAnchorPoints(segPoints, sortByY);
		}
		else{
			sortAnchorPoints(segPoints, sortByX);
		}
	int n= segPoints.length;
			if(n > 0){
	/*		 System.out.println("Sorted Points returned");
			    for(int i = 0; i < n; i++){
			    	System.out.println("Points  X: " + segPoints[i][0] + "Y: " + segPoints[i][1]);
			    }
		*/	    
			
			    if(pt1 == null){
			    	pt1 = new Point();
			    }
			    if(pt2 == null){
			    	pt2 = new Point();
			    }
			    pt1.x = (int)segPoints[1][0];
			    pt1.y = (int)segPoints[1][1];
			    pt2.x = (int)(segPoints[2][0]);
			    pt2.y = (int)(segPoints[2][1]);
			    
			}
		else{
			//System.out.println("size of array is" + segPoints.length);
		}
			return segPoints;
	}
	
	// added on 08-05-10
	// to show line while two line become collinear while dragging the element
	/**function to to show a line connecting two lines, when they become collinear while dragging
	 * one of them
	 * @author Sunil Kumar
	 */
	public void checkForCollinearLines(){
		//System.out.println("Entered  collinear lines ");
		
		// to put highlighted element if it is a line in a segment 
		if(m_highlightedElements.size() == 1 && m_button_type == MouseEvent.BUTTON3){
			//System.out.println("Element Selected : " + m_highlightedElements.get(0).getClass());
			String parsedCons[];
			
			parsedCons = m_highlightedElements.get(0).getClass().toString().split("[ ]+");
			
			if(parsedCons[1].compareToIgnoreCase("dcad.model.geometry.segment.SegLine") == 0){
				//System.out.println("Equal");
				//Segment segm = (Segment)m_highlightedElements.get(0);
				//System.out.println("Segment is a line");
				//System.out.println("M_button_type :" + m_button_type + "   Mouse Event : " + MouseEvent.BUTTON3);
				//System.out.println("size of highlighted elements : " + m_highlightedElements.size());
		
				Segment seg  = (Segment)m_highlightedElements.get(0);

				Vector parallelLinesConstraintList = getParallelLinesConsList("lines", "parellel", seg);
				if(parallelLinesConstraintList.size() != 0 ){
					//System.out.println("Size of elements :" + parallelLinesConstraintList.size());
					int consNumber = 0;
					// for each parallel line to this find whether they are collinear and display that line
					for(consNumber = 0; consNumber < parallelLinesConstraintList.size(); consNumber++){
						Constraint c=(Constraint)parallelLinesConstraintList.get(consNumber);
						if(c instanceof RelativeConstraint){
							//System.out.println("Enter relative constraints");
							RelativeConstraint rc=(RelativeConstraint)c;
							SegLine seg1 = (SegLine)rc.getM_seg1();
							SegLine seg2 = (SegLine)rc.getM_seg2();
							
							//System.out.println("X1 " + seg1.getM_start().getX()+ ", Y1 " +seg1.getM_start().getY());
							//System.out.println("X2 " + seg1.getM_end().getX()+ ", Y2 " +seg1.getM_end().getY());
							//System.out.println("X3 " + seg2.getM_start().getX()+ ", Y3 " +seg2.getM_start().getY());
							//System.out.println("X4 " + seg2.getM_end().getX()+ ", Y4 " +seg2.getM_end().getY());
							// check whether line formed by (x2,y2) and (x3,y3) is parallel to any
							// other segment
							double [][] segPoints = new double[4][2];
							segPoints = findMiddlePtsWhileMoving(seg1, seg2);
							
				/*			int n= segPoints.length;
							 System.out.println("Sorted Points");
							    for(int i = 0; i < n; i++){
							    	System.out.println("Points  X: " + segPoints[i][0] + "Y: " + segPoints[i][1]);
							    }
					*/		    
							if(constraintsHelper.areSlopesEqual(seg1.getM_start().getX(),seg1.getM_start().getY(),
																seg1.getM_end().getX(),seg1.getM_end().getY(),
																segPoints[1][0],segPoints[1][1],
																segPoints[2][0],segPoints[2][1],false)){
							//	drawDashedLineWhileMoving(seg1, seg2);
								//System.out.println("Lines are collinear");
								setM_AreLinesCollinear(true);
								
							}
							else{
								//System.out.println("not collinear");
								setM_AreLinesCollinear(false);
								
							}
							repaint();
						}
					}
				}
				else{
					System.out.println("No parallel constraint");
				}
			
				//System.out.println("Segment is a line");
		}
	}
}

	public void mouseDragged(MouseEvent e)
	{
		//System.out.println("Mouse dragged");
		mouseDragged(e.getX(), e.getY(), e.getWhen());
	}

	private boolean handleMouseDragEditMode(int x, int y)
	{
		boolean result = true;
		// check for collinearity while dragging the line
		checkForCollinearLines();
		
		if (m_highlightedElements.size() > 0)
		{
			System.out.println("Mouse drag highlighted elements : " + m_highlightedElements.size());
			Vector elementsToMove = new Vector();

			// find all elements to move
			Iterator iter = m_highlightedElements.iterator();
			while (iter.hasNext())
			{
				GeometryElement element = (GeometryElement) iter.next();
				if (!element.isFixed())
				{
					if (!(elementsToMove.contains(element)))
						elementsToMove.add(element);
				}
			}

			// In case there are element to move, go in
			if (elementsToMove.size() > 0)
			{
				if (!isM_elementDragged())
				{
					boolean remMarkers = false;
					// this is done only once
					iter = elementsToMove.iterator();
					while (iter.hasNext())
					{
						GeometryElement element = (GeometryElement) iter.next();
						if (!element.isFixed())
						{
							if (element instanceof Text){
								// do nothing
							} 
							else{
								// clear the soft constraints for the selected
								// element
								removeConstraints(element);
								remMarkers = true;
							}
						}
					}

					// clear the list of moved elements
					m_movedElementsOldPos.clear();

					// add all the highlighted elements to the movedElements
					// Vector
					copyMovedElements(elementsToMove);

					if (remMarkers)
					{
						// remove all the unused markers or Text elements
						Iterator itr = m_drawData.getUnusedMarkers().iterator();
						while (itr.hasNext())
						{
							Marker marker = (Marker) itr.next();
							marker.delete();
						}
				
						// remove all the unused text Elements
						itr = m_drawData.getUnusedText().iterator();
						while (itr.hasNext())
						{
							Text text = (Text) itr.next();
							text.delete();
						}
					}

					setM_elementDragged(true);
				}

				Vector movedPts = findAnchorPoints(elementsToMove);
				double movedPointsPositions[] = new double[movedPts.size() * 2];
				// System.out.println("!!!!!!!!! Initial Positions : ");
				for (int temp = 0; temp < movedPts.size(); temp++)
				{
					AnchorPoint ap = (AnchorPoint) movedPts.get(temp);
					movedPointsPositions[temp * 2] = ap.getX();
					movedPointsPositions[temp * 2 + 1] = ap.getY();
					// System.out.println(ap.getM_label() + " " + ap.getX() + "
					// " + ap.getY());
				}
				
				iter = elementsToMove.iterator();
				while (iter.hasNext())
				{
					GeometryElement element = (GeometryElement) iter.next();
					element.move(m_mousePos.x, m_mousePos.y, x, y);
				}
				
				//iter = elementsToMove.iterator();
				if (movedPts.size() > 0)
				{
					Vector affectedCons = ConstraintSolver.solveConstraintsAfterMovement(movedPts,
							movedPointsPositions);
					if (affectedCons == null)
					{
						result = false;
						// undo this move
						// 29-1-2008 WindowActions.getInstance().undo();
						// WindowActions.getInstance().undo();
					} else
					{
						updateConstraints(affectedCons, Constraint.HARD);
					}
				}
			}
			
		/*	if(getMovedPointSegment() != null){
				Segment movedSeg  = getMovedPointSegment();
				
				movedSeg.clearConstraints(Constraint.SOFT);
				movedSeg.clearConstraints(Constraint.HARD);
				//movedSeg.delete();
				//setMovedPointSegment(null);
			}*/
			
			repaint();
		}
		return result;
	}

	public void mouseDragged(int x, int y, long time)
	{
		if (GVariables.getDRAWING_MODE() == GConstants.EDIT_MODE)
		{
			logEvent("mouseDragged({int}" + x + ", {int}" + y + ", {long}" + time + ");");
			handleMouseDragEditMode(x, y);
		} else
		{
			if (isM_trackFlag())
				logEvent("mouseDragged({int}" + x + ", {int}" + y + ", {long}" + time + ");");
			track(x, y, time);
		}
		m_mousePos.x = x;
		m_mousePos.y = y;
	}

	private void copyMovedElements(Vector elements)
	{
		Iterator iterator = elements.iterator();
		while (iterator.hasNext())
		{
			GeometryElement ele = (GeometryElement) iterator.next();
			if (ele instanceof AnchorPoint)
			{
				AnchorPoint ap = (AnchorPoint) ele;
				// to avoid duplicates
				if (!m_movedElementsOldPos.contains(ap))
					m_movedElementsOldPos.add(ap.copy());
			} else if (ele instanceof Segment)
			{
				Segment seg = (Segment) ele;
				Iterator iter = seg.getM_impPoints().iterator();

				while (iter.hasNext())
				{
					AnchorPoint ap = (AnchorPoint) iter.next();
					// to avoid duplicates
					if (!m_movedElementsOldPos.contains(ap))
						m_movedElementsOldPos.add(ap.copy());
				}
			}
		}
	}

	public void mouseMoved(MouseEvent e)
	{
		mouseMoved(e.getX(), e.getY());
	}

	void addToUndoVector(){
		winAct = WindowActions.getInstance();
		if(winAct.getUndoVector() != null){
			
			if(winAct.getUndoIndex() < (winAct.getUndoVector().size()-1)){
				winAct.removeUndoVectorElements();
			}
		}
		winAct.addElementToUndoVector();
	}
	
	// 25-01-10
	public double areaOfTriangle(AnchorPoint ptA, AnchorPoint ptB, Point ptP){
		return(Math.abs(ptA.getX()*ptB.getY() + ptB.getX()*ptP.getY()
				+	ptP.getX()*ptA.getY() - ptA.getX()*ptP.getY()
				-	ptP.getX()*ptB.getY() - ptB.getX()*ptA.getY())/2);
	}
	
	public double areaOfTriangle(AnchorPoint ptA, AnchorPoint ptB, AnchorPoint ptP){
		return(Math.abs(ptA.getX()*ptB.getY() + ptB.getX()*ptP.getY()
				+	ptP.getX()*ptA.getY() - ptA.getX()*ptP.getY()
				-	ptP.getX()*ptB.getY() - ptB.getX()*ptA.getY())/2);
	}
	
	public double findAngleWithXAxis(AnchorPoint ptA, AnchorPoint ptB){
		double angle = 0;
		double slope;
		if((ptA.getX() - ptB.getX()) !=0){
			slope = (ptA.getY() - ptB.getY())/(ptA.getX() - ptB.getX());
			angle = Math.atan(slope);
		}
		return angle;
	}
	
	// 08 - 05- 10
	// gives all the parallel lines constraints to the given segment
	/**function to give all the parallel lines constraints to the given segment
	 * @author Sunil Kumar
	 */
	public static Vector getParallelLinesConsList(String element, String constraint, Segment seg){		
		Vector constraints = seg.getM_constraints();
		Vector parallelLinesConstraintList = new Vector();
		Iterator itr = constraints.iterator();
		while (itr.hasNext()){
			Constraint cons = (Constraint)itr.next();
			String constraintString = cons.toString();
			String parsedCons[];
			
			parsedCons = constraintString.split("[ ]+");
			
			
			if((parsedCons[4].compareToIgnoreCase(element) == 0) && (parsedCons[3].compareToIgnoreCase(constraint) == 0)){
				parallelLinesConstraintList.add(cons);
			}
			
		}
		
		if(parallelLinesConstraintList != null){
			return parallelLinesConstraintList;
		}
		else{
		return null;
		}
	}
	
	
	public void mouseMoved(int x, int y)
	{		
		
		if(m_highlightedElements.size() == 0 && m_selectedElements.size()==0){
			if(m_drawData.isUnusedMarker() ){
				Vector marker = m_drawData.getUnusedMarkers();
				
				Marker mark = (Marker)(marker.get(marker.size()-1));
				
				if(mark instanceof MarkerAngle){
					helpDrawView.selectRows(GConstants.MARKER_ANGLE);
				}
				else if(mark instanceof MarkerEquality){
					helpDrawView.selectRows(GConstants.MARKER_EQUALITY);
				}
				else if(mark instanceof MarkerParallel){
					MarkerParallel markParallel = (MarkerParallel) mark;
					Segment seg = markParallel.getM_seg();
					if(seg instanceof SegCircleCurve){
						helpDrawView.selectRows(GConstants.MARKER_PARALLEL_ON_ARC);
					}
					else if(seg instanceof SegLine){
						helpDrawView.selectRows(GConstants.MARKER_PARALLEL_ON_LINE);
					}
					
				}
				else if(mark instanceof MarkerPerpendicular){
					helpDrawView.selectRows(GConstants.MARKER_PERP);
				}
			}
			else if(m_keyEventCode == KeyEvent.VK_SHIFT){
				helpDrawView.selectRows(GConstants.REMOVE_ANCHOR_POINT);
			}
			else if(m_keyEventCode == KeyEvent.VK_CONTROL){
				helpDrawView.selectRows(GConstants.SELECT_ELEMENT);
			}
			else{
			helpDrawView.selectRows(GConstants.LEFT_CLICK);
			}
		}
		m_mousePos.x = x;
		m_mousePos.y = y;
		
		
//###############################################################################
		// 24-01-10
		
		//  vector to store segment points of all the strokes
	/*	Vector segPoints  = new Vector();
		segPoints = m_drawData.getAllAnchorPoints();
		
		//  Max distance within which the angle marker gets highlighted
		double maxDistance = GConstants.cmScaleDrawingRatio/2;
		AnchorPoint point = new AnchorPoint();
		
		//  To find the anchor point closest to the mouse pointer
		Iterator iter = segPoints.iterator();
		while(iter.hasNext()){
			AnchorPoint ap = (AnchorPoint)iter.next();
			if(ap.distance(m_mousePos) <= maxDistance){
				point = ap;
			//	System.out.println("close to point " + point.getX() + point.getY());
				break;
			}
		}
		
		// Vectors containing segment list and line segments having this point 
		Vector segList = new Vector();
		Vector lineSegsContainAP = new Vector();
		segList = m_drawData.getAllSegments();
		
		// get the list of segments containing this anchor point
		iter = segList.iterator();
		while(iter.hasNext()){
			Segment seg = (Segment)iter.next();
			if(seg instanceof SegLine){
				if((seg.getSegStart() == point) || (seg.getSegEnd() == point) ){
					lineSegsContainAP.add((SegLine)seg);
				}
			}
		}
		
		//System.out.println("size is" + lineSegsContainAP.size());
		// find the point is near to which lines
		int vectorSize = lineSegsContainAP.size();
		// if there are only two lines whether this point lies outside the
		// triangle formed by these points
		Vector pointsOfTriangle = new Vector(); 
		if( vectorSize == 2){
			iter = lineSegsContainAP.iterator();
			while(iter.hasNext()){
				Segment seg = (Segment)iter.next();
				if(seg.getSegStart() != point){
					pointsOfTriangle.add((AnchorPoint)seg.getSegStart());
				}
				else{
					pointsOfTriangle.add((AnchorPoint)seg.getSegEnd());
				}
			}
			AnchorPoint ptA = (AnchorPoint)pointsOfTriangle.get(0);
			AnchorPoint ptB = (AnchorPoint)pointsOfTriangle.get(1);
			AnchorPoint ptC = point;
			Point ptP = m_mousePos;
			
			double areaABP = areaOfTriangle(ptA, ptB, ptP);
			
			double areaBCP = areaOfTriangle(ptB, ptC, ptP);

			double areaACP = areaOfTriangle(ptA, ptC, ptP);

			double areaABC = areaOfTriangle(ptA, ptB, ptC);
			
			double errorMargin = 0.000000000000001;
			SegLine lineAB, lineAC;
			if(areaABC == (areaABP + areaBCP + areaACP)){
				
				// angle of line AB with X-axis
				lineAB = (SegLine)lineSegsContainAP.get(0);
				double angleAB = findAngleWithXAxis(lineAB.getSegStart(),lineAB.getSegEnd());
				
				
				//angle of line AC with X-axis
				lineAC = (SegLine)lineSegsContainAP.get(1);
				double angleAC = findAngleWithXAxis(lineAC.getSegStart(),lineAC.getSegEnd());
				
				
			//	System.out.println("Point is inside the triangle with slope " + angleAB);
			//	System.out.println("Point is inside the triangle with slope " + angleAC);
				
				// draw a circular arc between these two lines
				 Marker m_marker = null; 
				 m_marker = new MarkerAngle(null, lineAB, lineAC, null);
				 int markerType = m_marker.getM_type();
			}
			else{
			//	System.out.println("Point is outside the triangle");
			}
			
						 
			
		}
		
		
		
		
		
		
		lineSegsContainAP.clear();
	/*	if ((ele.isSelected()) && (m_selectedElements.contains(ele)))
		{
			ele.setSelected(false);
			m_selectedElements.remove(ele);
		}*/ 
		//################################################################
		
		
		// unhighlight the highlighted component in the drawing view, if
		// required
		if(!isParameterWinBitSet()){
	//	System.out.println("mouse moved");
		if (m_highlightedElements.size() > 0){
			//System.out.println("highlighted elements ");
			// first check if the mouse is on the same object,
			boolean repaintReq = false;
			Iterator iter = m_highlightedElements.iterator();
			while (iter.hasNext())
			{
				GeometryElement ele = (GeometryElement) iter.next();
				if (!ele.containsPt(m_mousePos))
				{
					repaintReq = true;
					ele.setHighlighted(false);
					iter.remove();
				}
			}
			if (!repaintReq)
				// mouse is still on the same object.. no need to do anything.
				return;
			else{
				//addToUndoVector();
				repaint();
			}
		}
		
		
		
		if ((m_keyEventCode != -1) && (m_keyEventCode == KeyEvent.VK_SHIFT)){
			if (m_showLastStroke)
			{
				Stroke lastStroke = m_drawData.getLastStroke(true);
				if ((lastStroke != null) && (lastStroke.containsPt(m_mousePos)))
					addHighLightedElement(lastStroke);
				else
				{
					Vector aps = isPtOnAnyAnchorPoint(m_mousePos);
					
					if ((aps != null) && (aps.size() > 0))
						addHighLightedElements(aps);
					else
					{
						Vector gEles = isPtOnGeometryElement(m_mousePos);
						if (gEles != null)
							addHighLightedElements(gEles);
					}
				}
			}
		} 
		else{
			// check if the mouse is close to any other geometry element
			Vector gEles = isPtOnGeometryElement(m_mousePos);
			
			if (m_keyEventCode == KeyEvent.VK_CONTROL)
			{
				for (int i = 0; i < gEles.size(); i++)
				{
					if (gEles.get(i) instanceof SegLine)
					{
						SegLine seg = (SegLine) gEles.get(i);
						Vector equalRelativeLengthConstraints = seg
								.getConstraintByType(EqualRelLengthConstraint.class);
						for (int j = 0; j < equalRelativeLengthConstraints.size(); j++)
						{
							EqualRelLengthConstraint eq = (EqualRelLengthConstraint) equalRelativeLengthConstraints
									.get(j);
							if (eq.getM_seg1() == seg)
								addHighLightedElement(eq.getM_seg2());
							// m_highlightedElements.add(eq.getM_seg2());
							else
								addHighLightedElement(eq.getM_seg1());
							// m_highlightedElements.add(eq.getM_seg1());

						}
					}
				}
			}

			if (gEles != null){
				addHighLightedElements(gEles);
			//	System.out.println("point on anchor point");
			}
			// m_highlightedElements.addAll(gEles);
		}

		// create the label to be shown in the status bar.
		// Also, set all the elements in the highlighted Vector to be true
		String label = "";
		if (m_highlightedElements.size() > 0)
		{
			// 04-10-09 to highlight rows in HELP table
			GeometryElement g1 = (GeometryElement)m_highlightedElements.get(0);
			if(m_selectedElements.size() == 0){
				if((g1 instanceof SegLine) || (g1 instanceof SegCircleCurve)){
					if(m_keyEventCode == KeyEvent.VK_CONTROL){
						helpDrawView.selectRows(GConstants.SELECT_ELEMENT);
					}
					else if(m_keyEventCode == KeyEvent.VK_SHIFT){
						helpDrawView.unselectRows();
					}
					else{
						helpDrawView.selectRows(GConstants.HIGHLIGHT_ELEMENTS);
					}
				}
				else if(g1 instanceof ImpPoint){
					//System.out.println("Highlight Point");
					ImpPoint ip = (ImpPoint)g1;
					if(m_keyEventCode == KeyEvent.VK_SHIFT){
						helpDrawView.selectRows(GConstants.REMOVE_ANCHOR_POINT); // unselect row 7
							if(ip.getAllParents().size() < 2){
								//helpDrawView.unselectRows();
								helpDrawView.unselectRow(5);
							}
							helpDrawView.unselectRow(4);
					}
					else if(m_keyEventCode == KeyEvent.VK_CONTROL){
						helpDrawView.unselectRow(3); // unselect row 4
					}
					else{
						helpDrawView.selectRows(GConstants.HIGHLIGHT_POINT);
						if(ip.getAllParents().size() < 2)
							helpDrawView.unselectRow(5);
					}	
				}
				else if(g1 instanceof Stroke){
					helpDrawView.selectRows(GConstants.ADD_AP);
				}
			}
			////////////////////////////////////////
			
			Iterator iter = m_highlightedElements.iterator();
			while (iter.hasNext()){
				GeometryElement element = (GeometryElement) iter.next();
				// select the element if its enabled
				element.setHighlighted(true);
				label += element.getM_label() + "  ";
			}
			repaint();
		}
		/*else{
			helpDrawView.unselectRows();
		}*/
		updateStatusBar(x, y, "( Move )", label);
		}
		else{
		//	System.out.println("mouse moved Parameter window bit set");
		}
			
	}

	
	private void addHighLightedElement(GeometryElement g)
	{
		if (!m_highlightedElements.contains(g))
			m_highlightedElements.add(g);
	}

	private void addHighLightedElements(Vector v)
	{
		for (int i = 0; i < v.size(); i++)
			addHighLightedElement((GeometryElement) v.get(i));
	}

	private Vector performSegRecycling(int x, int y)
	{
		Vector modConstraints = new Vector();
		Point pt = new Point(x, y);
		Iterator iter = m_highlightedElements.iterator();
		while (iter.hasNext())
		{
			GeometryElement element = (GeometryElement) iter.next();
			if (element instanceof Stroke)
			{
				Stroke stroke = (Stroke) element;
				if (stroke.containsPt(pt))
				{
					Vector vec = performReSegmentation(stroke, pt);
					if (vec != null)
						modConstraints.addAll(vec);
				}
				break;
			}
			if (element instanceof AnchorPoint)
			{
				AnchorPoint ap = (AnchorPoint) element;
				Vector vec = performSegFusion(ap);
				if (vec != null)
					modConstraints.addAll(vec);
				break;
			}
		}
		return modConstraints;
	}

	/**
	 * This methods removes the segment point (breakpoint) indicated by this
	 * Anchor Point. Note that the anchor point will be removed only if the its
	 * not one of the end points of the stroke
	 * 
	 * @param ap
	 *            The Anchor point to be removed from the stroke
	 */
	private Vector performSegFusion(AnchorPoint ap)
	{
		// TODO Add a check : If the segment was drawn in the last stroke, then
		// only do the resegmentation else don't.
		// Important : Found on 9-5-2008
		// When some point is shift clicked, choose the last segment added in
		// its parent vector.
		// The stroke of this segment should be revised.
		
		// 03-05-10
		// condition is added to avoid array index out of bounds exception in case parents size is null
		GeometryElement gEle = null;
		if(ap.getAllParents().size()!=0) {
		gEle = (GeometryElement) ap.getAllParents().elementAt(
				ap.getAllParents().size() - 1);
		}
		
		if (gEle instanceof Segment)
		{
			// parent is segment, so remove the segment point from which this
			// segment was formed
			Segment seg = (Segment) gEle;
			
			if(seg.getM_parentStk() == m_drawData.getLastStroke(true))
			{
				// get the location of the pixel at the
				int spLoc = seg.getSegmentPt(ap);
				// get the parent stroke for the segment
				Stroke theStroke = seg.getM_parentStk();

				// find the segment point at this index, if any and delete it
				SegmentPoint sp = theStroke.getSegPtAt(spLoc);
				if (sp != null)
				{
					// remove the segment point of this stroke.
					theStroke.getM_segPtList().remove(sp);

					// remove all segments of this stroke.
					theStroke.deleteSegments();

					return recognizeSegmentsAndConstraints(theStroke);

				}
			}

		}
		return null;
	}

	private Vector performReSegmentation(Stroke theStroke, Point pt)
	{
		// remove all the segments of this stroke
		theStroke.deleteSegments();

		// find the closest pixelinfo to pt
		PixelInfo ppi = theStroke.findPrevPI(pt.getX(), pt.getY());
		PixelInfo npi = theStroke.findNextPI(pt.getX(), pt.getY());

		if ((ppi != null) && (npi != null))
		{
			// create a new PI object by interpolation
			PixelInfo newPI = new PixelInfo(pt, (long) ((ppi.getTime() + npi.getTime()) / 2));

			// set the other information for this pixel
			newPI.setCurvature((ppi.getCurvature() + npi.getCurvature()) / 2);
			newPI.setSlope((ppi.getSlope() + npi.getSlope()) / 2);
			newPI.setSpeed((ppi.getCurvature() + npi.getCurvature()) / 2);

			Vector tempV = new Vector();
			tempV.add(theStroke);
			SegmentPoint sp = new SegmentPoint(newPI, tempV);

			// insert this pi in the list of points of this stroke.
			// find proper location in the current segment Point list where this
			// new segment point is to be inserted, if at all
			theStroke.getM_ptList().insertElementAt(newPI, theStroke.getM_ptList().indexOf(npi));

			SegmentPoint prevSP = null;
			SegmentPoint nextSP = null;
			Vector segPtList = theStroke.getM_segPtList();
			Iterator iterator = segPtList.iterator();
			if (iterator.hasNext())
			{
				prevSP = (SegmentPoint) iterator.next();
				PixelInfo prevPI = (PixelInfo) prevSP.getM_point();
				if (prevPI.getTime() > newPI.getTime())
				{
					// this pixel itself is less than the first SP itself
					prevSP = null;
				}
			}

			if (prevSP != null)
			{
				while (iterator.hasNext())
				{
					nextSP = (SegmentPoint) iterator.next();
					PixelInfo prevPI = (PixelInfo) prevSP.getM_point();
					PixelInfo nextPI = (PixelInfo) nextSP.getM_point();
					// check if we have a proper segment point location
					if ((prevPI.getTime() < newPI.getTime())
							&& (newPI.getTime() < nextPI.getTime()))
					{
						// we have a valid Segment Point location
						break;
					}
					prevSP = nextSP;
				}
				// insert a new segment point in this array
				segPtList.add(segPtList.indexOf(prevSP) + 1, sp);
			} else
			{
				// insert a new segment point at the start
				segPtList.add(0, sp);
			}

			return recognizeSegmentsAndConstraints(theStroke);
		} else
		{
			JOptionPane.showMessageDialog(null, "No Close Pixel found ", "Resegmentation Error",
					JOptionPane.ERROR_MESSAGE, null);
			return null;
		}
	}

	/**
	 * Merges the segments of the stroke by ensuring end-points coincide. 
	 * @param theStroke
	 */
	public void adjustStroke(Stroke theStroke)
	{
		// connect the end points of the stroke
		if (theStroke.getM_segList().size() >= 2)
		{
			Iterator iter = theStroke.getM_segList().iterator();
			Segment prevSeg = null;
			if (iter.hasNext())
				prevSeg = (Segment) iter.next();
			while (iter.hasNext())
			{
				Segment nextSeg = (Segment) iter.next();
				prevSeg.getSegEnd()
						.move(nextSeg.getSegStart().getX(), nextSeg.getSegStart().getY());
				// 18-2-2008
				mergePoints(prevSeg.getSegEnd(), nextSeg.getSegStart());
				prevSeg = nextSeg;
			}
		}
	}

	private void performSelection(int x, int y)
	{
		// check if the mouse is close to any other geometry element
		Vector gEles = isPtOnGeometryElement(new Point(x, y));
		Iterator iter = gEles.iterator();
		while (iter.hasNext())
		{
			GeometryElement ele = (GeometryElement) iter.next();
			if (ele instanceof ImpPoint)
			{
				ImpPoint ip = (ImpPoint) ele;
				// check if any of the segments for the AP is already selected.
				// In that case we do not need to add this ap
				if ((ip.isSelected()) && (m_selectedElements.contains(ele)))
				{
					ele.setSelected(false);
					m_selectedElements.remove(ele);
				} else if ((!ip.isSelected()) && (!m_selectedElements.contains(ele)))
				{
					ele.setSelected(true);
					m_selectedElements.add(ele);
				}
			} else
			{
				if ((ele.isSelected()) && (m_selectedElements.contains(ele)))
				{
					ele.setSelected(false);
					m_selectedElements.remove(ele);
				} else if ((!ele.isSelected()) && (!m_selectedElements.contains(ele)))
				{
					ele.setSelected(true);
					m_selectedElements.add(ele);
				}
			}
		}

		String label = "";
		if (m_selectedElements != null)
		{
			// 04-10-09 to show select rows in Help Table
			highlightTableRowsSelectedElems();
			iter = m_selectedElements.iterator();
			while (iter.hasNext())
			{
				GeometryElement element = (GeometryElement) iter.next();
				// select the element if its enabled
				element.setSelected(element.isEnabled());
				label += ", " + element.getM_label();
			}
			repaint();
		}
		updateStatusBar(x, y, "( Move )", label);
		GMethods.getRecognizedView().updateSelection(m_selectedElements);
	}

	public void keyPressed(KeyEvent e)
	{
		keyPressed(e.getKeyCode());
	}

	public void keyPressed(int keyCode)
	{
		m_keyEventCode = keyCode;
		switch (m_keyEventCode)
		{
		case KeyEvent.VK_CONTROL:
			// take any action only if no text has been typed so far
			if (typedText.length() > 0)
				m_keyEventCode = -1;
			else
			{
				if(m_drawData.getStrokeList() != null){
					helpDrawView.selectRows(GConstants.SELECT_ELEMENT);
				}
				logEvent("keyPressed({int}" + keyCode + ");");
				setM_logBetweenKeyPress(false);
				setM_keyPressedLogged(true);
			}
			break;

		case KeyEvent.VK_SHIFT:
			// take any action only if no text has been typed so far
			if (typedText.length() > 0)
				m_keyEventCode = -1;
			else
			{
				if(m_drawData.getStrokeList() != null){
					helpDrawView.selectRows(GConstants.REMOVE_ANCHOR_POINT);
				}
				logEvent("keyPressed({int}" + keyCode + ");");
				setM_logBetweenKeyPress(false);
				setM_keyPressedLogged(true);
			}
			break;

		default:
			break;
		}
	}

	public void keyReleased(KeyEvent e)
	{
		keyReleased(e.getKeyCode());
	}

	public void keyReleased(int keyCode)
	{
		boolean logPause = isM_logBetweenKeyPress();
		switch (keyCode)
		{
		case KeyEvent.VK_CONTROL:
			// take any action only if no text has been typed so far
			if (isM_keyPressedLogged())
			{
				logEvent("keyReleased({int}" + keyCode + ");");
				setM_keyPressedLogged(false);
			}
			if (logPause)
				logEvent(Command.PAUSE);
			break;

		case KeyEvent.VK_SHIFT:
			// take any action only if no text has been typed so far
			if (isM_keyPressedLogged())
			{
				logEvent("keyReleased({int}" + keyCode + ");");
				setM_keyPressedLogged(false);
			}
			if (logPause)
				logEvent(Command.PAUSE);
			break;

		default:
			break;
		}
		m_keyEventCode = -1;
	}

	public void keyTyped(KeyEvent e)
	{
		char c = e.getKeyChar();
		FontMetrics fm = getGraphics().getFontMetrics();
		int width = fm.stringWidth(typedText);

		// create a blank string to overwrite whatever was written so far.
		if ((((c >= '0') && (c <= '9')) || ((c >= 'a') && (c <= 'z')) ||((c >= 'A') && (c <= 'Z')) ||(c == '.')) && (hasFocus())){
			// 04-10-09
			helpDrawView.selectRows(GConstants.TYPE_LETTERS);
			// some other element was clicked .. so clear all the selection
			// 7-5-2008 Removed this so that the selected elements will not be
			// cleared and we can add distance constraints.
			// m_selectedElements.clear();

			// add the new char to the existing string
			typedText += c;
			// write the string in the required area
			getGraphics().drawString(typedText, m_mousePos.x, m_mousePos.y);
			//getGraphics().drawString(c + "", m_mousePos.x + width, m_mousePos.y);
		} else
		{
			switch (c)
			{
			case KeyEvent.VK_BACK_SPACE:
				backspacePressed();
				break;
			case KeyEvent.VK_DELETE:
				// Delete the selected elements
				deleteKeyPressed();
				break;
			case KeyEvent.VK_ENTER:
				System.out.println("Enter key pressed");
				setEnterKeyClicked(true);
				if (typedText.length() > 0)
				{
					// clear the required part of the canvas by a blank string
					String blankStr = "";
					for (int i = 0; i < typedText.length(); i++)
						blankStr += " ";
					getGraphics().drawString(blankStr, m_mousePos.x, m_mousePos.y);
					
					writeText(m_mousePos.x, m_mousePos.y, typedText);
					repaint();
				}
				setEnterKeyClicked(false);
				typedText = "";
				System.out.println("*************************"+"typed text "+ typedText);
				break;
			default:
				break;
			}
		}
	}

	private void backspacePressed()
	{
		FontMetrics fm = getGraphics().getFontMetrics();
		int height = fm.getHeight();
		// some other element was clicked .. so clear all the selection
		clearSelection();

		// char is back space
		if (typedText.length() > 0)
		{
			// get the last character
			String remain = typedText.substring(typedText.length() - 1);
			typedText = typedText.substring(0, typedText.length() - 1);

			// clear the required part of the canvas by a char with background
			// color
			Graphics2D g2D = (Graphics2D) this.getGraphics();
			Color oldColor = g2D.getColor();
			g2D.setColor(GVariables.BACKGROUND_COLOR);
			g2D.fillRect(m_mousePos.x + fm.stringWidth(typedText), m_mousePos.y - height, fm
					.stringWidth(remain), height);
			g2D.setColor(oldColor);
		}
	}

	public void deleteKeyPressed()
	{
		if (m_selectedElements.size() > 0)
		{
			logEvent("deleteKeyPressed()");
			logEvent(Command.PAUSE);
			Iterator iter = m_selectedElements.iterator();
			while (iter.hasNext())
			{
				GeometryElement gEle = (GeometryElement) iter.next();
				gEle.delete();
			}
			m_selectedElements.clear();
			repaint();
			//RecognizedView rv = MainWindow.getRecognizedView();
			//rv.reset(m_drawData.getM_constraints());
			UpdateUI(1,m_drawData.getM_constraints());
		}
	}

	public void removeConstraints(GeometryElement gEle)
	{
		if (gEle != null)
		{
			Object movedElement = gEle;
			if (gEle instanceof ImpPoint)
			{
				for (int l = 0; l < ((ImpPoint) gEle).getAllParents().size(); l++)
				{
					movedElement = (Segment) ((ImpPoint) gEle).getAllParents().elementAt(l);
					if (movedElement instanceof Segment)
					{
						Segment seg = (Segment) movedElement;
						seg.clearConstraints(Constraint.SOFT);
					}
				}
			} else if (movedElement instanceof Segment)
			{
				Segment seg = (Segment) movedElement;
				seg.clearConstraints(Constraint.SOFT);
			}
		}
	}

	public void snapIPsAndRecalculateConstraints()
	{
		snapIPs();

		// After snapping the points, add more constraints that can be added
		Vector newlyAddedConstraints = recalculateConstraints(m_drawData.getAllSegments());

		// These constraints could be solved
		Vector solvedConstraints = ConstraintSolver
				.solveConstraintsAfterSnapAndRecalculation(m_drawData.getAllAnchorPoints());

		// If the constraints could not be solved, remove the newly added
		// constraints
		if (solvedConstraints.size() == 0)
			constraintsHelper.removeConstraints(newlyAddedConstraints);
		else
		{
			// First of all, update the solved constraints
			updateConstraints(solvedConstraints, Constraint.HARD);

			// It may happen that some newly identified soft constraints were
			// removed while solving, delete them.
			Vector tempVector = (Vector) newlyAddedConstraints.clone();

			// Find out the constraints that could not get sovled
			tempVector.removeAll(solvedConstraints);

			// Remove the unsolved constraints from justAdded list
			newlyAddedConstraints.removeAll(tempVector);

			justAddedConstraints.addAll(newlyAddedConstraints);
		}

		updateConstraints(constraintsHelper.getListOfConstraints(m_drawData.getAllAnchorPoints()),
				Constraint.HARD);
		// commented on 15-10-09 
		// Because due to it, if user draws some small stroke it gets removed which th user does not want 
		/* 8-5-2008 */
		// Don't forget to clone it
		/*
		Vector allSegments = (Vector) m_drawData.getAllSegments().clone();
		Segment seg;
		for (int i = 0; i < allSegments.size(); i++)
		{
			seg = (Segment) allSegments.get(i);
			if (seg instanceof SegLine
					&& ((SegLine) seg).getM_length() < Constraint.MAX_ALLOWED_CONNECT_GAP)
				{}//seg.delete();
			else if (seg instanceof SegCircleCurve
					&& ((SegCircleCurve) seg).getM_radius() < Constraint.MAX_ALLOWED_CONNECT_GAP)
				{}//seg.delete();
		}
		*/
		
		UpdateUI(1,justAddedConstraints);
	}

	private Vector recalculateConstraints(Vector affectedSegs)
	{
		Vector v = new Vector();
		Iterator iter;
		iter = affectedSegs.iterator();
		while (iter.hasNext())
		{
			GeometryElement element = (GeometryElement) iter.next();
			v.addAll(recalculateConstraints(element));
		}
		return v;
	}

	public Vector recalculateConstraints(GeometryElement gElement)
	{
		Vector cons = new Vector();

		if (gElement != null)
		{
			Vector allMovedElements = new Vector();
			if (gElement instanceof ImpPoint)
				allMovedElements = ((ImpPoint) gElement).getAllParents();
			else
				allMovedElements.add(gElement);

			GeometryElement movedElement;

			for (int l = 0; l < allMovedElements.size(); l++)
			{
				movedElement = (GeometryElement) allMovedElements.elementAt(l);

				if (movedElement instanceof Segment)
				{
					Segment seg = (Segment) movedElement;
					seg.clearConstraints(Constraint.SOFT);

					cons = seg.findConstraints(m_drawData.getAllSegments());
					if (cons != null)
					{
						m_drawData.addConstraints(cons);
						// justAddedConstraints.addAll(cons);
					}
				}
			}
		}
		return cons;
	}

	public void writeText(int X, int Y, String c)
	{
		Text t = new Text(c + "", X, Y);
		if (t != null)
		{
			addGeoElement(t);
			logEvent("writeText({int}" + X + ", {int}" + Y + ", {" + String.class.getName() + "}"
					+ c + ");");
			logEvent(Command.PAUSE);
			setM_logBetweenKeyPress(false);
		}
		repaint();

		// Added on 6-5-2008
		// This was added in mousemoved function where writeText is called
		// But, because of that, this was not done while loading a file
		// Now it's working.
		addConstraintsForMarkers();
		clearSelection();
	}

	private void updateStatusBar(int x, int y, String statusStr, String label)
	{
		StatusBar sb = MainWindow.getM_statusBar();
		if (sb != null)
		{
			sb.setCoordLabelText(" " + statusStr + " " + x + ", " + y);
			if (label.length() != 0)
				sb.setIDLabelText(" LABEL : " + label);
			else
				sb.setIDLabelText("");
		}
	}
	
	private void resizePanel()
	{
		Vector v = m_drawData.getAllAnchorPoints();
		// 27-09-09
		Stroke stk = m_drawData.getLastStroke(true);
	/*	if (stk==null)
			 return;
		Vector vec = stk.getM_ptList();
		for(int i=0;i<vec.size();i++)
		{
			Point p = ((PixelInfo) vec.get((Integer)i));
			//AnchorPoint p = (AnchorPoint)v.get(i);
			if(p.getX() > m_canvasUsed.width)
				m_canvasUsed.width = (int)p.getX() + 100;
			if(p.getY() > m_canvasUsed.height)
				m_canvasUsed.height = (int)p.getY() + 100;
		}
		*/
		
		// added on 18-05-10
		// change canvas's width and height in case of draw and drag mode
		for(int i=0;i<v.size();i++)
		{
		
			AnchorPoint p = (AnchorPoint) v.get(i);
			//AnchorPoint p = (AnchorPoint)v.get(i);
			if(p.getX() > m_canvasUsed.width)
				m_canvasUsed.width = (int)p.getX() + ((int)GConstants.cmScaleDrawingRatio*3);
			if(p.getY() > m_canvasUsed.height)
				m_canvasUsed.height = (int)p.getY() + ((int)GConstants.cmScaleDrawingRatio*3);
		}
		
		
		boolean changed = false;
		Dimension drawArea = new Dimension();
		changed = true;
		if (m_canvasUsed.width > getSize().width)
		{
			drawArea.width = m_canvasUsed.width;
			
			
			changed = true;
		}
		else
			drawArea.width = getSize().width;
		

		if (m_canvasUsed.height > getSize().height)
		{
			drawArea.height = m_canvasUsed.height;
			changed = true;
		}
		else
			drawArea.height = getSize().height;

		if (changed)
		{
			// adjust the area to the new dimensions.
			this.setPreferredSize(drawArea);
			this.revalidate();

		}
	}

	/**
	 * Update the recognized view constraint list and the edit pane. 
	 * TODO: Fix point null problem. 
	 * @param type 1=only constraints 2=everything, including edit pane 
	 * @param cons
	 */
	public void UpdateUI (int type, Vector cons) 
	{
		Stroke stroke = getCurrStroke(); 
		Point pt2 = getMousePointerLocation() ; 
		
		EditView ev = MainWindow.getEv() ;
		
		if(cons!=null) {
			GMethods.getRecognizedView().reset(cons) ;
		}
		Segment segm = null ;

		if(m_highlightedElements.size() ==1 ){
			m_highlightedElements.add(seg);
		}
		Vector segL; 
		
		//Need mouse-pointer location to get the segment, if the segment/stroke is null. Mouse-press creates new stroke, over writing previous one, so this 
		// is needed. 
		
		if(stroke==null || stroke.getM_segList().isEmpty()) {
			segm = isPtOnAnySegment((Point2D)pt2); 

		}
		else {
			segL = stroke.getM_segList();

			segm = (Segment)segL.elementAt(0) ;
		}
			//lineWindow = new LineParameterWindow();
			if(segm!=null)
			{
				//Point pt = getMousePointerLocation() ;
				Point pt = getM_mousePos() ;
				
				Point pt3 = getLocationOnScreen() ;
				Point pt4 = getLocation() ;
				System.out.println("BEGIN POINT CO_ORDINATES......................") ;
				
				ev.displayOptions(segm,pt2) ;
			}
			//m_highlightedElements.clear();		
	//	}
	}
	
	
	public boolean isM_trackFlag()
	{
		return m_trackFlag;
	}

	public void setM_trackFlag(boolean flag)
	{
		m_trackFlag = flag;
	}

	
	public void clearView()
	{
		if (null != m_drawData)
		{
			m_drawData = null;
			init();
			//RecognizedView rv = MainWindow.getRecognizedView();
			//rv.reset(m_drawData.getM_constraints());
			UpdateUI(1,m_drawData.getM_constraints()) ;
		}
		repaint();
		this.revalidate();
	}

	public DrawingData getM_drawData()
	{
		return m_drawData;
	}

	public void setM_drawData(DrawingData data)
	{
		m_drawData = data;
		m_currStroke = null;
	}

	public boolean dispatchKeyEvent(KeyEvent e)
	{
		processKeyEvent(e);
		return true;
	}

	public void logEvent(String str)
	{
		Command comm = new Command(str);
		logEvent(comm);
	}

	public void logEvent(Command comm)
	{
		getM_drawData().getM_commands().add(comm);
		WindowActions.getInstance().setCq(null);
		setM_logBetweenKeyPress(true);
		setM_saved(false);
	}

	public boolean isM_saved()
	{
		return m_saved;
	}

	public void setM_saved(boolean m_saved)
	{
		this.m_saved = m_saved;
	}

	public boolean isM_elementDragged()
	{
		return m_elementDragged;
	}

	public void setM_elementDragged(boolean dragged)
	{
		m_elementDragged = dragged;
	}

	public void repaint()
	{
		if (!GVariables.undoing)
			super.repaint();
	}

	public boolean isM_newFile()
	{
		return m_newFile;
	}

	public void setM_newFile(boolean file)
	{
		m_newFile = file;
	}

	public Vector getM_highlightedElements()
	{
		return m_highlightedElements;
	}

	public boolean isM_logBetweenKeyPress()
	{
		return m_logBetweenKeyPress;
	}

	public void setM_logBetweenKeyPress(boolean betweenKEyPress)
	{
		m_logBetweenKeyPress = betweenKEyPress;
	}

	/**
	 * Updates the constraints related to the Segment based on the new values of
	 * the segments
	 */
	protected void updateConstraints(Vector constraints, int catagory)
	{
		if (constraints == null)
			return;
		Iterator iter = constraints.iterator();
		while (iter.hasNext())
		{
			Constraint cons = (Constraint) iter.next();
			if (cons.getM_category() == catagory)
			{
				cons.update();
			}
		}
	}

	public boolean isM_keyPressedLogged()
	{
		return m_keyPressedLogged;
	}

	public void setM_keyPressedLogged(boolean pressedLogged)
	{
		m_keyPressedLogged = pressedLogged;
	}

	public boolean isM_mousePressedLogged()
	{
		return m_mousePressedLogged;
	}

	public void setM_mousePressedLogged(boolean pressedLogged)
	{
		m_mousePressedLogged = pressedLogged;
	}

	public void setCursor(Cursor cursor)
	{
		if (!GVariables.undoing)
			super.setCursor(cursor);
	}
	
	//22-01-10
	public Vector getSelectedElements(){
		return m_selectedElements;
	}

	
	public void setCurrentStroke(Stroke stroke){
		m_currStroke = stroke;
	}
	
	public Stroke getCurrStroke(){
		return m_currStroke;
	}
	public Vector getJustAddedConstraints(){
		return justAddedConstraints;
	}
	
	public void setJustAddedConstraints(Vector constraints){
		justAddedConstraints = constraints;
	}
	public void setLastStrokeBit(boolean status){
		m_showLastStroke = true;
	}

	public void setM_button_type(int m_button_type) {
		this.m_button_type = m_button_type;
	}

	public int getM_button_type() {
		return m_button_type;
	}
}
