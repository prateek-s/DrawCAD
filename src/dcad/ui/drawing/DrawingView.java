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

import dcad.ui.main.ActionInterface ;


/**
 * Top-Level class. Sets off the Stroke->Segment->Constraint->Draw chain on mouse-press/release events.
 * 'Track' adds the points to the stroke and thus links the gui with the underlying plumbing.
 *
 */

public class DrawingView extends JPanel implements MouseListener, MouseMotionListener, KeyListener,
		KeyEventDispatcher, Serializable
		
{	
	public ActionInterface A ;
	
	public ProcessManager m_processManager;
	/**
	 * Strokes, constraints, markets, anchors, text etc added by recognizeSegmentsAndConstraints (primarily).
	 * Action interface's data is used.
	 */
	private DrawingData m_drawData;

	/**
	 * Current stroke being drawn, recently drawn stroke.
	 */
	private Stroke m_currStroke;

	private Point m_prevPt;

	/**
	 * Current mouse position, updated by mouse methods
	 */
	private Point m_mousePos;

	public Point getM_mousePos() {
		return m_mousePos;
	}
	
	public void setM_mousePos(Point mMousePos) {
		m_mousePos = mMousePos;
	}

	// Skip to the next section which deals with the mouse listeners
	/********************* UI STATE *********************/

	/**
	 * Length of the stroke being drawn
	 */
	private double m_length;

	private Rectangle m_bBox;

	private boolean m_trackFlag = false;

	private boolean m_mouseOverPanel = false;

	private Rectangle m_canvasUsed = new Rectangle(0, 0, 0, 0);

	private boolean m_saved = true;

	private boolean m_newFile = true;

	//private Vector A.m_highlightedElements = null;

//	private Vector A.m_selectedElements = null;

	private String typedText = "";

	private boolean m_elementDragged = false;

	private int m_keyEventCode = -1;

	private boolean m_logBetweenKeyPress = false;

	private Vector m_movedElementsOldPos = null;

	/**
	 * Only the marker stroke should not be shown. 
	 * This can be eliminated?
	 */
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
	
	Point pt1 = null;  //needed for collinearity
	Point pt2 = null;  //needed for collinearity

	/**
	 * Initializes everything.
	 */
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
	}

	/**
	 * Set up UI listeners, layout UI components etc.
	 */
	public void init()
	{
		A = new ActionInterface () ;
		
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
		
		m_drawData = A.m_drawData ;
		
		m_mousePos = new Point(-1, -1);
		m_trackFlag = false;
		m_mouseOverPanel = false;
		m_showLastStroke = true;
		m_logBetweenKeyPress = false;
		m_saved = true;
		m_newFile = true;
		GVariables.DRAWING_MODE = GConstants.DRAW_MODE;
		setCursor(MainWindow.getM_defCursor());
		
		A.m_selectedElements = new Vector();
		A.m_highlightedElements = new Vector();
		
		
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
	
/***************************************************************************/
/************************ MOUSE EVENTS *************************************/
	
	/**
	 * Disable all event listening when mouse exits area. Unselect all help rows
	 */
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

	public void mouseReleased(MouseEvent e)
	{
		mouseReleased(e.getX(), e.getY(),e.getButton());
	}
	
	public void mousePressed(MouseEvent e)
	{
		mousePressed(e.getButton(), e.getClickCount(), e.getX(), e.getY(), e.getWhen());
	}
	
	public void mousePressed(int buttontype, int clickcount, int x, int y, long time)
	{
		repaint();
		
		m_mousePos.x = x;
		m_mousePos.y = y;
		
		setM_button_type(buttontype);
		if (buttontype == MouseEvent.BUTTON1)
		{			
			helpDrawView.selectRows(GConstants.LEFT_CLICK);	
			if (clickcount == 1)
			{
				mouseButton1Pressed(x, y, time);
			}
		}
		else if (buttontype == MouseEvent.BUTTON2) 
		{
			if(A.something_highlighted() ) {
				helpDrawView.selectRows(GConstants.MIDDLE_CLICK);
				}
			
			mouseButton2Pressed(x, y);
		}
		else if (buttontype == MouseEvent.BUTTON3) 
		{
			//highlight rows in HELP table
			if(A.something_highlighted()) 
			{
				if(m_keyEventCode == KeyEvent.VK_SHIFT)
				{
				// SHIFT+RIGHT-CLICK => Separate anchor points. 
					// Need something highlighted for that first.
					//if(A.m_highlightedElements.size() == 1)
					if(A.something_highlighted()) {
						GeometryElement ge = (GeometryElement) A.m_highlightedElements.get(0);
						if(ge instanceof ImpPoint) {
							ImpPoint ip = (ImpPoint)ge;
							if(ip.getAllParents().size() > 1) {
								helpDrawView.unselectRows();
								helpDrawView.selectRow(5);
							}
						}
					}
				}
				else {	
					helpDrawView.selectRows(GConstants.RIGHT_CLICK);
				}

			}
			
			mouseButton3Pressed(x, y);
		}
		// Update the mouse position AFTER the complete event has been processed. 
		//Moved to the top of the function.
	}

	/**
	 * Middle click
	 * @param x
	 * @param y
	 */
	public void mouseButton2Pressed(int x, int y)
	{
		// check if any of the highlighted elemets are selected, in which case
		// the operation will be performed on al the selected elements.
		// Note that selected elements may or may not be connected.
		if (A.m_highlightedElements.size() > 0)
		{
			/**
			 * Fix the position of the element to it's current position.
			 */
			fixElements(x, y);
		} else
		{
			logEvent("mouseMoved({int}" + x + ", {int}" + y + ");");
			logEvent("mouseButton2Pressed({int}" + x + ", {int}" + y + ");");
			/**
			 * Look for markers and apply the constraints that they can enforce.
			 * This is not needed here, really.
			 */
			addConstraintsForMarkers();
		}
	}

	/**
	 * Left-click
	 * @param x
	 * @param y
	 * @param time
	 */
	public void mouseButton1Pressed(int x, int y, long time)
	{
		newConstraints = new Vector();

		logEvent("mouseMoved({int}" + x + ", {int}" + y + ");");
		logEvent("mouseButton1Pressed({int}" + x + ", {int}" + y + ", {long}" + time + ");");
		setM_mousePressedLogged(true);

		if (m_keyEventCode == -1)
		{
			UI_log(A.getMethod()+"NEW STROKE BEGIN") ;
			/**
			 * @UI: Left-button press
			 * @Action: Beginning of a stroke. Paint stroke on screen
			 * DrawingState: Segmentation and constraints added/modified
			 */
			setM_trackFlag(true);
			reset();
			m_currStroke = new Stroke();
			Point pt = new Point(x,y) ;
			
			paint_point(pt) ;
			addPointToStroke(m_currStroke,pt, time) ;
			
			clearSelection() ;
		}
		else 
		{
			if (m_keyEventCode == KeyEvent.VK_CONTROL)
			{
				/**
				 * Element under cursor selected. 
				 * Operations can be performed on these selected elements. 
				 */
				performSelection(x, y);
			} 
			else if ((m_keyEventCode == KeyEvent.VK_SHIFT))
			{
				if (m_showLastStroke)
				{
					UI_log("Perform Seg Recycling" + "SHIFT" ) ;
					/**
					 * Adds or removes anchor-points. Anchor-points are start/end points
					 * of a segment. Hence this changes the segmentation of the stroke. 
					 * DrawingState: Stroke is resegmented. Constraints adjusted. Other segments
					 * also can be affected.
					 */
					Vector constraints = A.performSegRecycling(x, y);
					repaint() ;
					A.post_anchor_ops(constraints) ;
					repaint() ;
				}
			} 
			else{
				clearSelection();
			}
		}
	}

	/**
	 * Right-click
	 * @param x
	 * @param y
	 */
	public void mouseButton3Pressed(int x, int y)
	{
		Point pt = new Point(x,y) ;
		
		logEvent("mouseMoved({int}" + x + ", {int}" + y + ");");
		logEvent("mouseButton3Pressed({int}" + x + ", {int}" + y + ");");
		setM_mousePressedLogged(true);

		// order log events is imp
		// check if any of the highlighted elemets are selected, in which case
		// the operation will be performed on all the selected elements.
		// Note that selected elements may or may not be connected.
		if (A.m_highlightedElements.size() > 0)
		{
			if ((m_keyEventCode == KeyEvent.VK_SHIFT))
			{
				/**
				 * @UI : Shift+Right-click.
				 * @Action: Separate Anchor Points.
				 * @Description: Anchor point is added on  the position of the click.
				 * @Side-effect: 
				 * @Internal: Calls A_add_anchor_point
				 */
				GeometryElement e = (GeometryElement) A.m_highlightedElements.get(0) ;
				Vector es = (Vector) A.m_highlightedElements.subList(0, 1) ;
				A.A_add_anchor_point(es , pt) ;
			
				//partitionLineSegments(e, x, y);
				//WAS HERE
			}
			else
			{
				GVariables.setDRAWING_MODE(GConstants.EDIT_MODE);
				if (!A.smartMergeSelectedEleToHighLightedEle())
				{
					clearSelection();
				}
			}
		} 
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param buttonType
	 */
	public void mouseReleased(int x, int y,int buttonType)
	{
		boolean extraClick = false;
		
		/////System.out.println("mouse released ");
		
		Point pt1 = new Point(x,y) ;
		setMousePointerLocation(pt1) ;

		//added on 19-04-10 for showing GUI to set properties of an element

		///System.out.println("Enter show elements properties ");
		showElementPropertiesWindow(x, y, buttonType);

		
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

		newConstraints = new Vector();
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
				Vector constraints = Process_Stroke(m_currStroke) ;
			}
		}
		else //edit mode
		{
			/**
			 * Move an element
			 * Dragging is a transient operation.
			 * 
			 */
			boolean dragged = handleMouseDragEditMode(x, y); 
			if (dragged)
			{
				if (isM_elementDragged() && (A.m_highlightedElements.size() > 0)){
					snapIPsAndRecalculateConstraints(newConstraints);		
				}
			}
			//added on 11-05-10
			// check to add whether dragged element was collinear
			if(isM_AreLinesCollinear()){
				///System.out.println("mouse released unsetting bit ");
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
		//removeElementsWithStartEndPtMerged();
		repaint(); 
	
		// if converted bit now set to false
		setStrokeConverted(false);
		setSegmentConverted(false);
		// check if the area of this panel is to be increased to accomodate the
		// drawing
		resizePanel();
	}

	
	public void mouseDragged(MouseEvent e)
	{
		/////System.out.println("Mouse dragged");
		mouseDragged(e.getX(), e.getY(), e.getWhen());
	}
	
	public void mouseDragged(int x, int y, long time)
	{
		if (GVariables.getDRAWING_MODE() == GConstants.EDIT_MODE)
		{
			logEvent("mouseDragged({int}" + x + ", {int}" + y + ", {long}" + time + ");");
			handleMouseDragEditMode(x, y);
		} 
		else
		{
			if (isM_trackFlag())
				logEvent("mouseDragged({int}" + x + ", {int}" + y + ", {long}" + time + ");");
			Point pt = new Point(x,y) ;
			paint_point(pt) ;
			if(m_currStroke==null) {
				return ;
			}
			addPointToStroke(m_currStroke, pt, time) ;
			//track(x, y, time); WAS HERE
		}
		m_mousePos.x = x;
		m_mousePos.y = y;
	}

	public void mouseMoved(MouseEvent e)
	{
		mouseMoved(e.getX(), e.getY());
	}
	
	/**
	 * Highlight segments and help rows.
	 * @param x
	 * @param y
	 */
	public void mouseMoved(int x, int y)
	{	
		m_mousePos.x = x;
		m_mousePos.y = y;
		
		A.Highlight(m_mousePos, 0) ;
		repaint() ;
		
		HELP_UPDATE("default");

		if (A.something_highlighted())
		{
			int repaintReq = A.Highlight(m_mousePos,0);
			if (repaintReq!=0) {
				repaint();
				// mouse is still on the same object.. no need to do anything.
				return;
			}
		}

		if ((m_keyEventCode != -1) && (m_keyEventCode == KeyEvent.VK_SHIFT))
			if (m_showLastStroke)
				A.Highlight(m_mousePos, m_keyEventCode) ;
			
		else
		{
			// check if the mouse is close to any other geometry element
			Vector gEles = A.isPtOnGeometryElement(m_mousePos);

			//Ctrl+click is select, so when close to something, the appropriate
			//things, equal segments etc should get highlighted.
			if (m_keyEventCode == KeyEvent.VK_CONTROL)
			{
				A.Highlight(m_mousePos, KeyEvent.VK_CONTROL) ;
			}
		}
		
		//mostly help stuff
		String label = "";
		if (A.something_highlighted())
		{
			// 04-10-09 to highlight rows in HELP table
			HELP_UPDATE("moved") ;
	
			for (Object ele: A.m_highlightedElements) {
				if(ele==null) break; 
				GeometryElement element = (GeometryElement) ele ;
			//	element.setHighlighted(true); //need this really?
				label += element.getM_label() + "  ";
			}
			//repaint if something higlighted.
			repaint();
		}
		updateStatusBar(x, y, "( Move )", label);
	}
	
/***********************************************************************/
/**
 * Update all the help rows etc 
 */
public void HELP_UPDATE(String type) {
if(type=="default"){
	if(!A.something_highlighted())
	{
		//highlight help for different markers.
		if(m_drawData.isUnusedMarker())
		{
			Vector markers = m_drawData.getUnusedMarkers();
			
			Marker mark = (Marker)(markers.get(markers.size()-1));
			
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
	} //end outer-if
}

if(type=="moved") 
{
	GeometryElement g1 = (GeometryElement)A.m_highlightedElements.get(0);
	if(A.m_selectedElements.size() == 0)
	{
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
			/////System.out.println("Highlight Point");
			ImpPoint ip = (ImpPoint)g1;
			
			if(m_keyEventCode == KeyEvent.VK_SHIFT)
			{
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
}

}
	
	
	public void setM_button_type(int m_button_type) {
		this.m_button_type = m_button_type;
	}

	public int getM_button_type() {
		return m_button_type;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
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

		highlightTableRowsSelectedElems();
	}
	
	
	//19-04-10
	//functions to get mouse click's point location
	public void setMousePointerLocation(Point pt1)
	{
		if(pt == null){
			pt = new Point(); 
		}
		///System.out.println("setmousepointer");
		pt.setLocation(pt1);
	}
	
	public Point getMousePointerLocation(){
		return pt;
	}
	

/*************************************************************************/
	
/******************************** KEY EVENTS ****************************/
	
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
			// A.m_selectedElements.clear();

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
				///System.out.println("Enter key pressed");
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
				///System.out.println("*************************"+"typed text "+ typedText);
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
		if (A.m_selectedElements.size() > 0)
		{
			logEvent("deleteKeyPressed()");
			logEvent(Command.PAUSE);

			Iterator iter = A.m_selectedElements.iterator();
			while (iter.hasNext())
			{
			    A.A_delete_Element((GeometryElement )iter.next()) ;

			}
			A.m_selectedElements.clear();
			repaint();

			UpdateUI(1,m_drawData.getM_constraints());
		}
	}


/********************* DRAWING ON SCREEN ***************************/

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
		
		/* handle collinear line cue 'dashed-line' here */
		if(isM_AreLinesCollinear())
		{
			drawCollinearLine(gc, GVariables.SELECTED_FIXED_COLOR);
		}
		/*When does this happen? get rid? */
		else if(pt1 !=null && pt2!=null)
		{
			drawCollinearLine(gc, GVariables.BACKGROUND_COLOR);
		}
		
		// to draw grid
		// is the grid repainted on every stroke?
		 if(drGrid == null)
		 {
			 drGrid = new DrawGrid();
		 }
		
		 if(isM_gridActive() == true)
		 {  
			 drGrid.drawGrid(gc, drViewHeight, drViewWidth, GVariables.GRID_COLOR);
		 }
		 else
		 {
			 drGrid.drawGrid(gc, drViewHeight, drViewWidth, GVariables.BACKGROUND_COLOR);
		 }
		 	 
		// Draw last stroke as Raw segment First
		Stroke lastStroke = m_drawData.getLastStroke(true);
		if ((lastStroke != null) && (m_showLastStroke))
			lastStroke.drawRaw(gc);
		
		
		drawStrokes(gc, m_drawData.getStrokeList());

		drawMarkers(gc, m_drawData.getM_markers());

		drawTextElements(gc, m_drawData.getM_textElements());		
	}


	/**
	 * Draws a collinear line between point pt1 and pt2, which are global, and set 
	 * somewhere else
	 */
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


/************************ STROKE ADD ******************************************/
	/**
	 * REPLACES public void track(int x, int y, long time)
	 * Called on mouse released and pressed(left) events. 
	 * @param pt
	 * @return
	 */
	public int paint_point(Point pt) 
	{
		int x = pt.x ;
		int y = pt.y ;
		Graphics gc = this.getGraphics();
		if (isM_trackFlag())
		{
			if (m_prevPt == null)
			{
				if (!GVariables.undoing)
					gc.drawLine(x, y, x, y);
				m_prevPt = new Point(x, y);
				m_bBox = new Rectangle(x, y, x, y);
			}
			else
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
		}
		
		// Display the mouse position to the status bar
		String statusStr = "";
		if (m_mouseOverPanel)
			statusStr = x + ", " + y;

		updateStatusBar(x, y, " ( Drag ) " + statusStr, "");
		
		return 1 ;
	}

	/**
	 * Adds point to given stroke. Usually preceeded by paintpoint
	 * @param pt
	 * @param time
	 */
	public void addPointToStroke(Stroke m_currStroke, Point pt, long time)
	{
		int x = pt.x ;
		int y = pt.y ;
		// store current pixle position with timing information
		m_currStroke.addPoint(x, y, time);
	}

	/**
	 * Called by ProcessStroke. Detects Segment Points and does recognition.
	 * @param theStroke
	 * @return Constraints Vector.
	 */
	public Vector Add_Stroke(Stroke theStroke)
	{
		A.A_draw_Stroke(theStroke) ;
		
		Vector constraints = A.new_constraints ;
		
		if (!GVariables.undoing)
			theStroke.drawSegments(getGraphics());
	
		repaint() ;	
		UpdateUI(1,m_drawData.getM_constraints());
		
		return constraints ;
	}

	/**
	 *  The UI part of the code when adding a stroke to a drawing. 
	 * @param strk
	 * @return
	 */
	public Vector Process_Stroke(Stroke strk)
	{
		UI_log(A.getMethod()) ;
		Vector constraints = null ;
		if (strk != null )
		{			
			constraints = Add_Stroke(strk); //Does all the work

			constraints = A.Refresh_Drawing(strk,constraints) ;
			
			//show the last stroke
			m_showLastStroke = true;
			//TODO: Semantics of undo
			addToUndoVector();
			repaint();	
		}
		return constraints ;
	}
	


/***************************** SNAPPING **************************************/
	
	

/**function snap the ips
 * if a new stroke is drawn, check for if end points of this 
 * stroke needs to be snapped.	
 * In drawing mode, if a circular arc is drawn, then do not merge 
 * center of circle with any other point.
 * Merge it only in Edit mode(when an element is being dragged)
 */
	public void snapIPs()
	{
		// found on 22-02-10 when we type a text then also the tool is in Drawing mode.
		// so checked here whether the Enter key is clicked do not do this
		if(!isEnterKeyClicked())
		{
			if(GVariables.getDRAWING_MODE() == GConstants.DRAW_MODE && m_drawData.getStrokeList().size()>=1)
			{
				A.Snap_IPs_new(m_currStroke) ;
			}
			//else, not draw mode, or no strokes.
			else	
			{
				if (A.m_highlightedElements.size() > 0)
//FIXME				//if (isM_elementDragged() && (A.m_highlightedElements.size() > 0))
				{
					A.Snap_IP_drag(A.m_highlightedElements) ; 
				}
			}
		}
	}

/***********************************************************************/
	
	/**
	 * 
	 */
	public void addConstraintsForMarkers()
	{
		if ((m_drawData.isUnusedMarker()) || (m_drawData.isUnusedText()))
		{			
			//Do not want to show marker strokes at all
			m_showLastStroke = false;
		}
		
		Vector cons = A.A_addConstraintsForMarkers() ;
		if (cons!=null && cons.size() >0 ) {
			snapIPsAndRecalculateConstraints(cons);
		}
		
		resizePanel();
		repaint();
		
	}

	/**
	 * Fix the position of an element, so that it is not affected by
	 * any other process (beutification etc)
	 * @param x
	 * @param y
	 */
	private void fixElements(int x, int y)
	{
		logEvent("mouseMoved({int}" + x + ", {int}" + y + ");");
		if (!A.smartMergeSelectedEleToHighLightedEle())
		{
			clearSelection();
		}

		logEvent("mouseButton2Pressed({int}" + x + ", {int}" + y + ");");
		setM_mousePressedLogged(true);

		A.A_fix_elements() ;
	}


	/**
	 * Resets the selected elements.
	 * DrawingState: element selected flag unset
	 * RecognizedView : cleared
	 */
	private void clearSelection()
	{
		A.A_clear_selection() ;
		GMethods.getRecognizedView().updateSelection(A.m_selectedElements);
	}
	
	
	
	// This contains all constraints added after drawing or movement.
	// They will be shown in the recognized view. And user can remove some of
	// them if required.
	Vector newConstraints = new Vector();
//***************************************************************

	
	public void setGeoElementClicked(Segment segSelected){
		seg = segSelected;
	}
	
	public Segment getGeoElementClicked(){
		return seg;
	}
	
//*******************************************************************	

	

	
	// added on 18-05-10
	// to remove segment lines and their respective constraints
	// if start point and end point of the segment are merged
	// or the distance between two points is negligible
	/**Function to remove segment lines and their respective constraints
	 * if start point and end point of the segment are merged
	 * or the distance between two points is negligible
	 * @author Sunil Kumar
	 */

	public void removeElementsWithStartEndPtMerged()
	{
		Vector<Segment> segList = m_drawData.getAllSegments();
		if(segList != null )
		{
			for (Segment segment : segList) 
			{
				if(segment instanceof SegLine)
				{
					if(segment.getSegStart().distance(segment.getSegEnd()) < THRESHOLD){
						///System.out.println("segment removed");
						segment.clearConstraints(Constraint.SOFT);
						segment.clearConstraints(Constraint.HARD);
						segment.delete();
					}
				}
			} //for
		}
	}

	
	// added on 08-05-10
	// to show line while two line become collinear while dragging the element
	/**function to to show a line connecting two lines, when they become collinear while dragging
	 * one of them
	 * @author Sunil Kumar
	 */
	public boolean checkForCollinearLines()
	{
		boolean are_collinear = false ;
		/////System.out.println("Entered  collinear lines ");
		
		// to put highlighted element if it is a line in a segment 
		if(A.m_highlightedElements.size() == 1 && m_button_type == MouseEvent.BUTTON3) 
		{
			are_collinear = A.Check_for_Collinearity((GeometryElement)A.m_highlightedElements.get(0)) ;
		}
		return are_collinear ;
	}
	

	private boolean handleMouseDragEditMode(int x, int y)
	{
		UI_log(A.getMethod()) ;
		boolean result = true ;
		// check for collinearity while dragging the line
	//	boolean are_collinear = checkForCollinearLines() ;
	//	setM_AreLinesCollinear(are_collinear) ;  
		repaint() ;
		
		if (A.something_highlighted())
		{
			///System.out.println("Mouse drag highlighted elements : " + A.m_highlightedElements.size());
			Vector elementsToMove = new Vector();

			// find all elements to move
			for (Object ele : A.m_highlightedElements) {
				if (ele == null) break ;
				GeometryElement element = (GeometryElement) ele ;
			
				if (!element.isFixed())
				{
					if (!(elementsToMove.contains(element))) {
						elementsToMove.add(element);
						System.out.println("DRAG EDIT: "+element.toString());
					}
					
				}
			}

			// In case there are element to move, go in
			if (elementsToMove.size() > 0)
			{
			A.A_move_Elements( elementsToMove,m_mousePos,new Point(x,y),0) ;

			repaint();
			}
		}
			
		 //return result;
			return true ;
	}


	void addToUndoVector()
	{
		winAct = WindowActions.getInstance();
		if(winAct.getUndoVector() != null){
			
			if(winAct.getUndoIndex() < (winAct.getUndoVector().size()-1)){
				winAct.removeUndoVectorElements();
			}
		}
		winAct.addElementToUndoVector();
	}
	

public void UI_log(String s) 
{
	System.out.println(s) ;
}


	/**
	 * check if the mouse is close to any other geometry element
	 * @param x
	 * @param y
	 */
	public void performSelection(int x, int y)
	{
		UI_log(A.getMethod()+ "CTRL " + " SELECT") ;
		Point pt = new Point (x,y) ;
		A.m_selectedElements = A.A_elements_selected(pt,A.m_selectedElements) ;
		
		Iterator iter = A.m_selectedElements.iterator() ;

		String label = "";
		if (A.m_selectedElements != null)
		{
			// 04-10-09 to show select rows in Help Table
			highlightTableRowsSelectedElems();
			iter = A.m_selectedElements.iterator();
			while (iter.hasNext())
			{
				GeometryElement element = (GeometryElement) iter.next();
				label += ", " + element.getM_label();
			}
			
			repaint();
		}
		updateStatusBar(x, y, "( Move )", label);
		GMethods.getRecognizedView().updateSelection(A.m_selectedElements);
	}



	public void snapIPsAndRecalculateConstraints(Vector NewConstraints)
	{
		snapIPs(); 
		
		newConstraints = A.A_snapIPsAndRecalculateConstraints(NewConstraints) ;
		
		UpdateUI(1,newConstraints);
	}

/**
 * Write some text-string on the drawing. This can be inferred as a marker or just 
 * simply plain text. 
 * @param X
 * @param Y
 * @param c
 */
	public void writeText(int X, int Y, String c)
	{
		Text t = new Text(c + "", X, Y);
		if (t != null)
		{
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
		A.A_add_text(c,new Point (X,Y)) ;
		
		clearSelection();
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
		return A.m_drawData;
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

	
/******************* PURE UI HANDLING ************************************/
	
	
	/**Function to highlight table rows in Help table
	 * @author Sunil Kumar
	 */
	public void highlightTableRowsSelectedElems()
	{
		if(A.m_selectedElements != null){
			int size = A.m_selectedElements.size();
			if(size!=0){
				if(size == 1){
					GeometryElement g1 = (GeometryElement)A.m_selectedElements.get(0);
					if((g1 instanceof SegLine)){
						helpDrawView.selectRows(GConstants.SELECT_LINE);
					}
					else if(g1 instanceof SegCircleCurve){
						/////System.out.println("Highlight Point");
						helpDrawView.selectRows(GConstants.SELECT_ARC);
					}
					else if(g1 instanceof AnchorPoint){
						helpDrawView.selectRows(GConstants.SELECT_POINT);
					}
				}
				else if(size == 2){
					GeometryElement g1 = (GeometryElement)A.m_selectedElements.get(0);
					GeometryElement g2 = (GeometryElement)A.m_selectedElements.get(1);

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


	/**
	 * 
	 * @param x
	 * @param y
	 * @param statusStr
	 * @param label
	 */
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
	
	/**Function to check whether Mouse_Button 3 is clicked on any element
	 * If yes then show appropriate parameter window
	 * @author Sunil Kumar
	 */
	public void showElementPropertiesWindow(int x, int y,int buttonType)
	{
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
			seg = A.isPtOnAnySegment((Point2D)pt); 
			this.segUnderCursor = seg ;
			int count = A.ptOnSegments(pt);
			/////System.out.println("Count =" +count);
			// count <2 
			// count = 0 means that the point is not an anchor point
			// count >=1 means that the anchor point is a child of # elements
			if((seg)!=null && count < 2 && m_keyEventCode!= KeyEvent.VK_CONTROL  ){
				/////System.out.println("Control key not pressed");
				setGeoElementClicked (seg);
				UpdateUI(2, null) ;
			}
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

		if(A.m_highlightedElements.size() ==1 ){
			A.m_highlightedElements.add(seg);
		}
		Vector segL; 
		
		//Need mouse-pointer location to get the segment, if the segment/stroke is null. Mouse-press creates new stroke, over writing previous one, so this 
		// is needed. 
		
		if(stroke==null || stroke.getM_segList().isEmpty()) {
			segm = A.isPtOnAnySegment((Point2D)pt2); 

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
				///System.out.println("BEGIN POINT CO_ORDINATES......................") ;
				
				ev.displayOptions(segm,pt2) ;
			}
			//A.m_highlightedElements.clear();		
	//	}
	}
	
	

	private void resizePanel()
		{
			Vector v = m_drawData.getAllAnchorPoints();
			Stroke stk = m_drawData.getLastStroke(true);
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
	
	
/************************* EVENT LOGGING FOR REPLAY **********************/
	
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
	
/************************************************************************/
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
		return A.m_highlightedElements;
	}

	public boolean isM_logBetweenKeyPress()
	{
		return m_logBetweenKeyPress;
	}

	public void setM_logBetweenKeyPress(boolean betweenKEyPress)
	{
		m_logBetweenKeyPress = betweenKEyPress;
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
		return A.m_selectedElements;
	}

	
	public void setCurrentStroke(Stroke stroke){
		m_currStroke = stroke;
	}
	
	public Stroke getCurrStroke(){
		return m_currStroke;
	}
	public Vector getnewConstraints(){
		return newConstraints;
	}
	
	public void setnewConstraints(Vector constraints){
		newConstraints = constraints;
	}
	public void setLastStrokeBit(boolean status){
		m_showLastStroke = true;
	}

//	
//	/** Get the method name from which this function is called .
//	 * Might have some good use in logging/debugging.
//	 * @return
//	 */
//	public String getMethod()
//	{
//	     StackTraceElement stackTraceElements[] =
//	             (new Throwable()).getStackTrace();
//	     return stackTraceElements[1].toString();
//	}
//	
}