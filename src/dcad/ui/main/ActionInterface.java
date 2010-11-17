package dcad.ui.main;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;

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
import dcad.model.geometry.Stroke;
import dcad.model.geometry.Text;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.SegPoint;
import dcad.model.geometry.segment.Segment;
import dcad.model.marker.Marker;
import dcad.model.marker.MarkerCircleArcAngle;
import dcad.model.marker.MarkerLength;
import dcad.model.marker.MarkerLineAngle;
import dcad.model.marker.MarkerRadius;
import dcad.process.ProcessManager;
import dcad.process.beautification.ConstraintSolver;
import dcad.process.io.Command;
import dcad.process.preprocess.PreProcessingManager;
import dcad.process.preprocess.PreProcessor;
import dcad.process.recognition.RecognitionManager;
import dcad.process.recognition.marker.MarkerRecogManager;
import dcad.process.recognition.marker.MarkerToConstraintConverter;
import dcad.process.recognition.segment.SegmentRecognizer;
import dcad.process.recognition.stroke.StrokeRecognizer;
import dcad.ui.drawing.DrawingData;
import dcad.util.GConstants;
import dcad.util.GMethods;
import dcad.util.GVariables;
import dcad.util.Maths;


/**
 * Not  an interface as the name suggests.
 * This is the class containing all the actions that are supported. For example, segment_delete, is an action.
 * Mouse clicked is not. However, segment_select is an action. 
 * Should these be handled in Segment and associated classes? No. These are 'global' methods, since in the program any action can potentially 
 * change the entire drawing completely. As a result, seg_property_change not only changes property of the segment, but also of other segments. 
 * @author prateek
 *
 */


/**
 * Design notes. 
 * This class essentially contains the list of all the actions that any UI must support. For example, UI can either be java-awt, log-driven, web-based, etc. 
 * Thus, the class serves as the interface for all the UI actions. The methods below have been carefully chosen and are hopefully an exhaustive and orthogonal 
 * collection of actions supported by the program. If any capability needs to be added, it should be done *HERE* first, tested, and then later modify the UI to call 
 * the added action.
 * Most methods do not need to inform the UI of anything. UI can assume that once the method is called and returns, the intended action has been either successfully 
 * performed or if there is an error, the program raises the appropriate exception. However, to facilitate a tighter integration with the UI, integer return codes
 * have been provided. These allow the UI to keep some track of the internal state of the program. Ideally, UI should have no business with the state of the program. 
 * 
 */

public class ActionInterface extends ActionHelper
{
    /*
     * The local variables of this class together form a subset of the state of the entire program. 
     * The state is divided into these categories:
     * UI State: handled exclusively by *View. Mainly drawing view.
     * ActionItem state: This class
     * Drawing State: Handled bydrawing data, and invariably, by the segments and strokes which the drawing is composed of. .
     */

    /*************************** STATE VARIABLES.  
     * ********************* BE CAREFUL WHAT GOES HERE.	
     */
    public Vector<Constraint> new_constraints ;

    public DrawingData m_drawData = new DrawingData();

    public Vector m_selectedElements = new Vector() ;
    
    public ProcessManager m_processManager = ProcessManager.getInstance(); 

    public Vector<GeometryElement> m_highlightedElements = new Vector()  ;

 //   public Vector<GeometryElement> m_selectedElements ;



    /********************************************************************/

    /**
     * Perform Segmentation, recognize segments, merges strokes,
     * recognizes constraints, 
     */
    public Vector A_draw_Stroke(Stroke strk) 
    {
	new_constraints = null ;
	Vector segPts = Perform_Segmentation(strk) ;
	Vector<Segment> Segments =  recognizeSegments(strk) ; 
	adjustStroke(strk) ; //merges segments.

	m_drawData.addStroke(strk) ;
	new_constraints = recognize_Constraints(strk);
		 
	return null ;
    }

    /**
     * Segmentation using the appropriate segmentation scheme which is set by other means.
     * @param strk
     * @return
     */
    public Vector Perform_Segmentation(Stroke strk)
    {
	PreProcessingManager preProcessMan = m_processManager.getPreProManager();
	PreProcessor preProcessor = preProcessMan.getPreProcessor();
	Vector segPts = preProcessor.preProcess(strk);
	strk.setM_segPtList(segPts); 
		
	return segPts ;
		
    }
	
	
    /**
     * Calculate new constraints which occur as a result of adding a stroke,
     * snapips, add markers if any.
     * @param strk
     * @param constraints
     * @return
     */
    public Vector Refresh_Drawing (Stroke strk, Vector constraints)
    {
	Vector new_constraints = null ;
	if (strk.getM_type() == Stroke.TYPE_NORMAL)
	    {
		if ((constraints != null) && (constraints.size() > 0))
		{
			if (ConstraintSolver.addConstraintsAfterDrawing(constraints) != null)
			    ;
		 }
		new_constraints = A_snapIPsAndRecalculateConstraints(constraints);
		//GMethods.getHelpView().initialize(HelpView.afterDrawing);
	    }
	//25-3-2008 Added this line.
	else {
	    A_add_all_markers(); 
	}
	return new_constraints ;
    }
	
	
    /**
     *Deletes a given element (segment/marker). Simple call, no recalculation of constraints.!
     */
    public int A_delete_Element(GeometryElement element) 
    {
	element.delete() ;
	return 1 ;
    }

    boolean m_elementDragged ;
    
	public boolean isM_elementDragged()
	{
		return m_elementDragged;
	}

	public void setM_elementDragged(boolean dragged)
	{
		m_elementDragged = dragged;
	}
    
    /**
     *Move the elements to new location 'to'
     */
    public int A_move_Elements(Vector elements, Point from, Point to,int ongoing) 
    {
		if (something_highlighted())
		{
			///System.out.println("Mouse drag highlighted elements : " + m_highlightedElements.size());
			Vector elementsToMove = new Vector();

			// find all elements to move
			for (Object ele : m_highlightedElements) {
				GeometryElement element = (GeometryElement) ele ;
				if( element==null) break ;
				if (!element.isFixed())
				{
					if (!(elementsToMove.contains(element)))
						elementsToMove.add(element);
				}
			}
    	
	elementsToMove = elements ;
	if (!isM_elementDragged())
	    {
		boolean remMarkers = false;
		// this is done only once
		Iterator iter = elementsToMove.iterator();
		while (iter.hasNext())
		    {
			GeometryElement element = (GeometryElement) iter.next();
			if(element==null) break ;
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
		//NExt 2 statement dance not really needed. 
		// clear the list of moved elements
	//	m_movedElementsOldPos.clear();
		// add all the highlighted elements to the movedElements
		// Vector
	//	copyMovedElements(elementsToMove);
		//
		
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
	// ///System.out.println("!!!!!!!!! Initial Positions : ");
	for (int temp = 0; temp < movedPts.size(); temp++)
	    {
		AnchorPoint ap = (AnchorPoint) movedPts.get(temp);
		movedPointsPositions[temp * 2] = ap.getX();
		movedPointsPositions[temp * 2 + 1] = ap.getY();
		// ///System.out.println(ap.getM_label() + " " + ap.getX() + "
		// " + ap.getY());
	    }
				
	Iterator iter = elementsToMove.iterator();
	while (iter.hasNext())
	    {
		GeometryElement element = (GeometryElement) iter.next();
		element.move(from.x, from.y, to.x, to.y);
	    }
				
	//iter = elementsToMove.iterator();
	if (movedPts.size() > 0)
	    {
		Vector affectedCons = ConstraintSolver.solveConstraintsAfterMovement(movedPts,movedPointsPositions);
		if (affectedCons == null)
		    {
			boolean result = false;
			// undo this move
			// 29-1-2008 WindowActions.getInstance().undo();
			// WindowActions.getInstance().undo();
		    } else
		    {
			updateConstraints(affectedCons, Constraint.HARD);
		    }
	    }
    }
		
		return 1;
}





public int A_undo(int count) 
{
return 1;
}

public int A_redo(int count) 
{
return 1;
}

public int A_change_to_marker(Stroke strk) 
{
return 1;
}

public int A_change_marker_to_segment(Stroke strk) 
{
return 1;
}


public int A_add_all_markers()
{
	// check if new markers are added.
	if ((m_drawData.isUnusedMarker()) || (m_drawData.isUnusedText()))
	{
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
		
		Vector new_markers = m_drawData.getM_markers() ;
		
		A_add_markers(new_markers) ;
	}
return 1;
}


public int A_add_markers(Vector markers)
{
	Vector new_constraints; 
	RecognitionManager recogMan = ProcessManager.getInstance().getRecogManager();
	MarkerRecogManager markerMan = recogMan.getMarkerRecognitionMan();
	MarkerToConstraintConverter converter = markerMan.getM_markerConverter();
	// recognize the set of markers as constraints.text elements not read at all. relax
	Vector constraints = converter.recognizeMarkersAsConstraints(markers,
			m_drawData.getM_textElements(), m_drawData.getAllSegments());

	if (constraints != null && constraints.size() > 0)
	{
		if (ConstraintSolver.addConstraintsAppliedUsingMarker(constraints) != null)
		{
			m_drawData.addConstraints(constraints);
			//GMethods.getHelpView().initialize(HelpView.afterDrawing);
			A_snapIPsAndRecalculateConstraints(constraints );
		}
		//*************************************************************
	}
	
	return 1; 
}
	
/**
 * partition line segment earlier.
 * @param pt
 * @return
 */
public int A_add_anchor_point(Vector<GeometryElement> elements, Point pt) 
{
	Vector new_constraints = new Vector() ;
	Iterator iter = m_highlightedElements.iterator(); 
//	Iterator iter = elements.iterator() ;
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
					new_constraints.addAll(vec);
			}
			break;
		}
	}
	
	if ((new_constraints != null) && (new_constraints.size() > 0)){
		ConstraintSolver.addConstraintsAfterDrawing(new_constraints) ;
			//Vector justAddedConstraints =addAll(new_constraints);
	}
	A_snapIPsAndRecalculateConstraints (new_constraints) ;
	return 1;
}

/**
 * Delete the point.
 * @param pt
 * @return
 */
public int A_delete_anchor_point(Point pt) 
{
	Vector new_constraints = new Vector() ;
	Iterator iter = m_highlightedElements.iterator(); 
//	Iterator iter = elements.iterator() ;
	while (iter.hasNext())
	{
		GeometryElement element = (GeometryElement) iter.next();
		if (element instanceof AnchorPoint)
		{
			AnchorPoint ap = (AnchorPoint) element;
			Vector vec = performSegFusion(ap);
			if (vec != null)
				new_constraints.addAll(vec);
			break;
		}
	}
	if ((new_constraints != null) && (new_constraints.size() > 0)){
		ConstraintSolver.addConstraintsAfterDrawing(new_constraints) ;
			//Vector justAddedConstraints =addAll(new_constraints);
	}
	A_snapIPsAndRecalculateConstraints (new_constraints) ;
	return 1 ;
}

	
	
public int A_add_text(String text, Point pt) 
{
	Text t = new Text(text+"" , pt.x , pt.y) ;
	
	addGeoElement(t);

	RecognitionManager recogMan = ProcessManager.getInstance().getRecogManager();
	MarkerRecogManager markerMan = recogMan.getMarkerRecognitionMan();
	MarkerToConstraintConverter converter = markerMan.getM_markerConverter();

	// check if there are new markers related to the text objects
	Vector newMarkers = converter.recognizeTextAsMarkers(m_drawData.getM_markers(),
			m_drawData.getM_textElements(), m_drawData.getAllSegments(),
			m_selectedElements, m_highlightedElements);
	
	m_drawData.getM_markers().addAll(newMarkers) ;
	
	A_add_markers(newMarkers) ;
	return 1;
}

/**
 * Clears all the selected elements. 
 * @return
 */
public int A_clear_selection() 
{
	int selected = m_selectedElements.size() ;
	// some other element was clicked .. so clear all the selection
	Iterator iter = m_selectedElements.iterator();
	while (iter.hasNext())
	{
		GeometryElement element = (GeometryElement) iter.next();
		element.setSelected(false);
	}
	m_selectedElements.clear();
	return (selected-m_selectedElements.size()) ;
	
}

public int A_delete_text(Point location)
{
return 1;
}

public int A_change_Segment_to(Segment seg, int seg_type) 
{
return 1;
}

public int A_change_Seg_property(Segment seg,String type, String val)
{
	
	RecognitionManager recogMan = ProcessManager.getInstance().getRecogManager();
	MarkerRecogManager markerMan = recogMan.getMarkerRecognitionMan();
	MarkerToConstraintConverter converter = markerMan.getM_markerConverter();
	
	Text t = new Text(val) ; 
	Marker  marker = null;
	if(type=="angle") {
		if(seg instanceof SegLine) {
			 marker = new MarkerLineAngle((SegLine)seg, t);
		}
		else if (seg instanceof SegCircleCurve) {
			 marker = new MarkerCircleArcAngle((SegCircleCurve)seg, t);
		}
	}

	if (type=="length") {
		if(seg instanceof SegLine) {
			 marker = new MarkerLength((SegLine)seg, t);
		}
	}
	if (type=="radius") {
		 marker = new MarkerRadius((SegCircleCurve)seg, t);
	}
	
	if(marker==null) return -1 ;
	
	Vector newMarkers = new Vector() ; newMarkers.add(marker) ;
	
	m_drawData.getM_markers().add(marker) ;

	// recognize the set of markers as constraints
	Vector constraints = converter.recognizeMarkersAsConstraints(m_drawData.getM_markers(),
			m_drawData.getM_textElements(), m_drawData.getAllSegments());

	if (constraints != null && constraints.size() > 0)
	{
		
	
		if (ConstraintSolver.addConstraintsAppliedUsingMarker(constraints) != null)
		{
			m_drawData.addConstraints(constraints);
			//GMethods.getHelpView().initialize(HelpView.afterDrawing);
			Vector c = new Vector() ;
			A_snapIPsAndRecalculateConstraints(c);
		}
		//*************************************************************
		else
		{
			updateConstraints(constraintsHelper.getListOfConstraints(m_drawData.getAllAnchorPoints()),Constraint.HARD);
			//GMethods.getHelpView().initialize(HelpView.constraintAddingFailed);
		}
	}

	
	return 1;
}



public int A_clear() 
{
return 1;
}

public int A_load()
{
return 1;
}

public int A_save() 
{
return 1;
}

/**
 * There are many ways in which a constraint can be added
 * as a marker ; after moving ; after snapping ; after drawing. 
 * Here we assume that this is as a marker/user action. 
 * The other cases call the correct functions in ConstraintSolver directly,
 * (SnapIP, move, draw, etc)
 * @param constraint
 * @return
 */
public int A_add_constraints(Vector constraints) 
{
	ConstraintSolver.addConstraintsAppliedUsingMarker(constraints) ;
	return 1;
	
}


public int A_delete_constraint(Constraint constraint) 
{
return 1;
}

/**
 * Overriden by Elements selected. 
 * @param pt
 * @return
 */
public Segment A_seg_selected(Point pt) 
{
return null ;
}

/***************************** ELEMENT SELECTION **********************/
/**
 * returns the geometric elements under this point. Can be several, so return a vector. Already selected elements are passed, so duplicates are removed
 * @param pt
 * @return
 */
public Vector<GeometryElement>  A_elements_selected (Point pt,Vector m_selectedElements1) 
{
    int x = pt.x ;
    int y = pt.y ;
    Vector<GeometryElement> m_selectedElements = m_selectedElements1;
		
    // check if the mouse is close to any  geometry element
    Vector gEles = isPtOnGeometryElement(pt);
    Iterator iter = gEles.iterator();
    while (iter.hasNext()) 
	{
	    GeometryElement ele = (GeometryElement) iter.next();
	    if (ele instanceof ImpPoint) 
		{
		    ImpPoint ip = (ImpPoint) ele;
		    // check if any of the segments for the AP is already selected.
		    // In that case we do not need to add this ap
		    if ((ip.isSelected()) && (m_selectedElements.contains(ele))) {
			ele.setSelected(false);
			m_selectedElements.remove(ele);
		    } else if ((!ip.isSelected())
			       && (!m_selectedElements.contains(ele))) {
			ele.setSelected(true);
			m_selectedElements.add(ele);
		    }
		} 
	    else 
	    {
		if ((ele.isSelected()) && (m_selectedElements.contains(ele))) {
		    ele.setSelected(false);
		    m_selectedElements.remove(ele);
		} else if ((!ele.isSelected())  && (!m_selectedElements.contains(ele))) {
		    ele.setSelected(true);
		    m_selectedElements.add(ele);
		}
	    }
	}
		
    iter = m_selectedElements.iterator(); 
    while (iter.hasNext())
	{
	    GeometryElement element = (GeometryElement) iter.next();
	    // select the element if its enabled
	    element.setSelected(element.isEnabled());
			
	}
		
    return m_selectedElements ;
}		

/****************************************************************************************/

// added on 19-04-10
// checks whether the point is an child of how many elements 
/**Function to return given point belongs to how many elements 
 */

public int ptOnSegments(Point pt){
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

/**
 * returns all the anchor points,segments,markers that the point 
 * belongs to.
 * @param pt : Point
 * @return List of geometric elements
 */
public Vector isPtOnGeometryElement(Point pt)
{
	Vector gEles = new Vector();

	Vector aps = isPtOnAnyAnchorPoint(pt);
	if ((aps != null) && (aps.size() > 0))
	{
		gEles.addAll(aps);
		return gEles;
	}

	// check if the mouse is close to any other geometry element
	GeometryElement gEle = isPtOnAnySegment(pt);
	if ((gEle != null) && (gEle.isEnabled()))
	{
		gEles.add(gEle);
		return gEles;
	}

	// check if point is on any Text element
	gEle = isPtOnAnyText(pt);
	if ((gEle != null) && (gEle.isEnabled()))
	{
		gEles.add(gEle);
		return gEles;
	}

	// check if point is on any marker
	gEle = isPtOnAnyMarker(pt);
	if ((gEle != null) && (gEle.isEnabled()))
	{
		gEles.add(gEle);
		return gEles;
	}
	return gEles;
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

/****************************** END SELECT***********************************************/
	

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


public int A_fix_elements() 
{
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
    return 1;
}


/**********************************************************************************************************************/

public ActionInterface() {
    // TODO Auto-generated constructor stub
}



/****************************************************************************/

public void addHighLightedElement(GeometryElement g)
{
	if (!m_highlightedElements.contains(g))
		m_highlightedElements.add(g);
}


public void addHighlightedElements(Vector v) 
{
	Iterator i = v.iterator() ;
	while(i.hasNext())
		addHighLightedElement((GeometryElement) i.next()) ;
}

/**
 * Add or remove anchor point.
 */
public Vector performSegRecycling(int x, int y)
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


public int post_anchor_ops(Vector constraints) 
{
	
	if ((constraints != null) && (constraints.size() > 0)){
		ConstraintSolver.addConstraintsAfterDrawing(constraints) ;
		//	newConstraints.addAll(constraints);
	}
	A_snapIPsAndRecalculateConstraints(constraints);
	return 1;
}

/************************** MERGE OPERATIONS ******************************/
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
			A_merge_points(prevSeg.getSegEnd(), nextSeg.getSegStart());
			prevSeg = nextSeg;
		}
	}
}

/**
 * 
 * @param ip1
 * @param ip2
 */
public void mergePoints(ImpPoint ip1, ImpPoint ip2)
{	
	A_merge_points(ip1, ip2) ;
	
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


/**
 * constraints handled. Delete the merged point too.
 * @param ip1
 * @param ip2
 * @return
 */
public int A_merge_points(ImpPoint ip1, ImpPoint ip2)
{	
	if(constraintsHelper.getNoMergeConstraintBetweenPoints((AnchorPoint)ip1,(AnchorPoint)ip2) != null)
		return 0;
	
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
	
	return 1 ;
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



public void removeConstraintsOfType(AnchorPoint ap, Class className)
{
	Vector constraints = ap.getConstraintsByType(className);
	for (int i = 0; i < constraints.size(); i++)
	{
		Constraint c = (Constraint) constraints.get(i);
		if (!constraintsHelper.arePointsUnique(c))
		{
			c.remove();
			//justAddedConstraints.remove(c); //FIXME
		}
	}
}

/**************************************************************************/

/**
 * Try recognizing all constraints of the stroke. Called after segmentation, snapping has been 
 * performed already.
 * @param theStroke
 * @return
 */
public Vector recognize_Constraints(Stroke theStroke)
{
	Stroke m_currStroke = theStroke;
	
//	recognizeSegments(theStroke);
//	adjustStroke(theStroke);
//	m_drawData.addStroke(theStroke);
	
	Vector connectConstraints = theStroke.recognizeConnectConstraints(m_drawData.getStrokeList());

	RecognitionManager recogMan = m_processManager.getRecogManager();
	StrokeRecognizer strokeRecog = recogMan.getM_strokeRecogManager().getStrokeRecognizer();
	
	
	//
	// to set stroke's properties in case convert option is clicked
	//move out next two lines
	//theStroke.setStrokeConverted(isStrokeConverted);
	//theStroke.setStrokeConvertedTo(strokeConvertedTo);
	//
	
	int stkType = strokeRecog.findType(theStroke);	//marker or stroke?
	if (stkType == Stroke.TYPE_MARKER)
	{
		theStroke.setM_type(Stroke.TYPE_MARKER);
		// as this is a marker.. for each of its segments clear the points
		// Vector for each of their constraints
		Marker marker = strokeRecog.getMarker();
		if (marker != null)
			addGeoElement(marker) ;
				
	//	UpdateUI(1,m_drawData.getM_constraints());
		return null;
	}
	
	/** *** 	CAN MARKERS HAVE SEGMENTS?? :-OOOOOOOO *******/
	
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

	//snapIPs();	//this should be removed

	// find the constraints between the segments of this stroke and
	// the segments of ALL the previous strokes.
	Vector constraints = theStroke.recognizeAllConstraints(m_drawData.getStrokeList());

	if (constraints != null)
		m_drawData.addConstraints(constraints);

	return constraints;
	
}


/************************************************************************/

/**
 * Recognize segments of the stroke.. Assumes segmentation has already 
 * been performed.  
 */
public Vector<Segment> recognizeSegments(Stroke theStroke)
{
	//m_currStroke = theStroke;
	RecognitionManager recogMan = m_processManager.getRecogManager();
	Vector<Segment> Segments = null ;
	// identify segments
	try
	{
		SegmentRecognizer segmentRecog = recogMan.getSegmentRecogMan().getSegmentRecognizer();
		
		Segments = theStroke.recognizeSegments(segmentRecog);

	} catch (Exception e)
	{
		JOptionPane.showMessageDialog(null, "Error Occured in recognize segments : "
				+ e.getMessage());
		e.printStackTrace();
	}
	
	return Segments ;
	
}

/**************************************************************************/

/************************ CONSTRAINTS *********************************/


public Vector recalculateConstraints(Vector affectedSegs)
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


/**
 * Updates the constraints related to the Segment based on the new values of
 * the segments
 */
public void updateConstraints(Vector constraints, int catagory)
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

/**************************** SNAP *********************************/



public Vector A_snapIPsAndRecalculateConstraints(Vector justAddedConstraints)
{
	// After snapping the points, add more constraints that can be added
	Vector newlyAddedConstraints =recalculateConstraints(m_drawData.getAllSegments());

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
	
	return justAddedConstraints ;
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


public Vector  A_addConstraintsForMarkers() 
{
	Vector newConstraints = new Vector();

	// check if new markers are added.
	if ((m_drawData.isUnusedMarker()) || (m_drawData.isUnusedText()))
	{
		//setM_mousePressedLogged(true);
		// dont show the last stroke
		
		///m_showLastStroke = false;

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
			//Cursor prevCursorType = this.getCursor();
		//	this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			
			if (ConstraintSolver.addConstraintsAppliedUsingMarker(constraints) != null)
			{
				m_drawData.addConstraints(constraints);
				newConstraints.addAll(constraints);
				//GMethods.getHelpView().initialize(HelpView.afterDrawing);
				return newConstraints ;
			//	snapIPsAndRecalculateConstraints(newConstraints);
			}
			//*************************************************************
			else
			{
			//	JOptionPane.showMessageDialog(this,"The constraint could not be added");
				updateConstraints(constraintsHelper.getListOfConstraints(m_drawData.getAllAnchorPoints()),Constraint.HARD);
				//GMethods.getHelpView().initialize(HelpView.constraintAddingFailed);
				return newConstraints ;
			}

		///	resizePanel();
		///	repaint();
		///	this.setCursor(prevCursorType);

//			snapIPsAndRecalculateConstraints();
		}

	}
return null;

}




/************************* END CONSTRAINTS ********************/

/**
 * This methods removes the segment point (breakpoint) indicated by this
 * Anchor Point. Note that the anchor point will be removed only if the its
 * not one of the end points of the stroke
 * 
 * @param ap
 *            The Anchor point to be removed from the stroke
 */
public Vector performSegFusion(AnchorPoint ap)
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

				
				recognizeSegments(theStroke);
				adjustStroke(theStroke);
				m_drawData.addStroke(theStroke);

				return recognize_Constraints(theStroke) ;
				//return recognizeSegmentsAndConstraints(theStroke);

			}
		}

	}
	return null;
}

/************************/

public Vector performReSegmentation(Stroke theStroke, Point pt)
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
		
		recognizeSegments(theStroke);
		adjustStroke(theStroke);
		m_drawData.addStroke(theStroke);
		return recognize_Constraints(theStroke) ;
		//return recognizeSegmentsAndConstraints(theStroke);
	} else
	{
		JOptionPane.showMessageDialog(null, "No Close Pixel found ", "Resegmentation Error",
				JOptionPane.ERROR_MESSAGE, null);
		return null;
	}
}

/*********************************************************************/
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
public  Vector getParallelLinesConsList(String element, String constraint, Segment seg){		
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


/**function to find is the given line horizontal
 * @author Sunil Kumar
 */
public boolean isLineHorizontal(double angle1)
{
	// checked it by a margin of .5 degrees on either sides
	/*p: 0.5 degrees is the magic constant here*/
	/////System.out.println("horizontal");
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
	/////System.out.println("vertical");
	if(((Double.compare(angle1, 89.5) == 0) || (Double.compare(angle1, 89.5) > 0))
			&& (Double.compare(angle1, 90.5) == 0) || (Double.compare(angle1, 90.5) < 0)){
		return true;
	}
	return false;
		
}




//// added on 10-05-10
// sort anchor points by x or y points
/**function to sort anchor points by x or y points
 * @author Sunil Kumar
 */
public void sortAnchorPoints(double segPoints[][], int sortBy){
		    int n = segPoints.length;
		    ///System.out.println("total points " + n );
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
	    
	    ///System.out.println("Sorted Points");
	    for(int i = 0; i < n; i++){
	    	///System.out.println("Points  X: " + segPoints[i][0] + "Y: " + segPoints[i][1]);
	    }
	    
	   // return segPoints;
}


/************************************************************************/	

/**
 * Get all anchor points present in the given list of elements
 */
public Vector findAnchorPoints(Vector elements)
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

/**
*UI separated
* @param e
* @return
*/
public boolean Check_for_Collinearity(GeometryElement e)
{
	String parsedCons[];
	parsedCons = e.getClass().toString().split("[ ]+");
	
	if(parsedCons[1].compareToIgnoreCase("dcad.model.geometry.segment.SegLine") == 0)
	{
		Segment seg  = (Segment)e;
		Vector parallelLinesConstraintList = getParallelLinesConsList("lines", "parellel", seg);
		if(parallelLinesConstraintList.size() != 0 ){
			int consNumber = 0;
			for(consNumber = 0 ; consNumber < parallelLinesConstraintList.size(); consNumber++){
				Constraint c=(Constraint)parallelLinesConstraintList.get(consNumber);
				if(c instanceof RelativeConstraint){
					RelativeConstraint rc=(RelativeConstraint)c;
					SegLine seg1 = (SegLine)rc.getM_seg1();
					SegLine seg2 = (SegLine)rc.getM_seg2();
						double [][] segPoints = new double[4][2];
					segPoints = findMiddlePtsWhileMoving(seg1, seg2);
					
					if(constraintsHelper.areSlopesEqual(seg1.getM_start().getX(),seg1.getM_start().getY(),
														seg1.getM_end().getX(),seg1.getM_end().getY(),
														segPoints[1][0],segPoints[1][1],
														segPoints[2][0],segPoints[2][1],false)){
				
						return true ;
					}
					else{
						return false; 
					}
				}
			}
		}
	}
	
	return false ;
}


Point pt1,pt2 ;
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
/*			 ///System.out.println("Sorted Points returned");
		    for(int i = 0; i < n; i++){
		    	///System.out.println("Points  X: " + segPoints[i][0] + "Y: " + segPoints[i][1]);
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
		/////System.out.println("size of array is" + segPoints.length);
	}
		return segPoints;
}

/**************************** SNAPPING *****************************/



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
	for (int w = 0 ; w < v.size() ; w++)
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
		if(GVariables.getDRAWING_MODE() == GConstants.DRAW_MODE)
		{
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
	else {
		closestPt = ip.findClosestSeg(m_drawData.getAllSegments());
		if (closestPt != null)
		{
			ip.move(closestPt.getX(), closestPt.getY());
			return true;
		}
	}
	return false;
}

/**Function to check if this Imp point is the center of Circular arc 
 * @author Sunil Kumar
 */
public boolean iterateParentVector(ImpPoint ip)
{
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

/**
 * Snap IPs of newly drawn stroke.
 */
public void Snap_IPs_new(Stroke m_currStroke) 
{
	Vector segPts = new Vector();
	segPts = null;
	
	if(m_currStroke==null)
		return ;

	segPts = m_currStroke.getM_segPtList();
	Vector anchorPoints = m_drawData.getAllAnchorPoints();
	
	if(segPts.size()!=0)
	{
		for (Object pt:segPts) 
		{
			SegmentPoint point = (SegmentPoint)pt;
			Point2D segPoint = point.getM_point();	

			for(Object ap : anchorPoints)
			{
				ImpPoint ip = (ImpPoint) ap;
				if(ip.getM_point()!=null){
					if((ip.getM_point()).equals(segPoint) 
							|| (ip.getM_point().distance(segPoint) < (((GConstants.cmScaleDrawingRatio)/10)*2)))
					{
						snapIP(ip);					// distance < 2 mm
					}
				}
			}	
		}
	}
}


public void Snap_IP_drag(Vector<GeometryElement> m_highlightedElements) 
{
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
			///System.out.println("Seg Line");
			SegLine segL = (SegLine) seg; 
			Vector segLinePts = segL.getM_impPoints();
			iter = segLinePts.iterator();
			while (iter.hasNext()){
				snapIP((ImpPoint)iter.next());
			}	
		}
		else if(seg instanceof ImpPoint){
			///System.out.println("ImpPoint");
			snapIP((ImpPoint)seg);
		}
		else{
			///System.out.println("this is else part");
		}
	}
	
}



/***************************************************************************/

public boolean smartMergeSelectedEleToHighLightedEle()
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
		addHighlightedElements(m_selectedElements);
		// m_highlightedElements.addAll(m_selectedElements);
	}
	return intersect;
}

/************************ HIGH-LIGHT, SELECT **********************/

public boolean something_highlighted()
{
	if (m_highlightedElements.size()>0) {
		if (m_highlightedElements.get(0)!=null)
			return true;
	}
	return false ;
}

/**
 * Simply clearing list wont work, have to unflag the elements in the
 * drawing data also
 * @return
 */
public int clear_highlighted()
{
	int size = m_highlightedElements.size() ;
	if(size ==0 || m_highlightedElements.elementAt(0)==null) return 0;
	for (Object ele : m_highlightedElements) {
		GeometryElement element = (GeometryElement) ele ;
		if(element!=null)
			element.setHighlighted(false) ;
	}
	m_highlightedElements.clear() ;
	return size ;
}

/**
 * Given the mouse position and the aux input type, add appropriate
 * elements to the highlighted list. This is the top level highlighting
 * method which handles all cases.
 * @param m_mousePos
 * @param type: what modifier key etc has been pressed, etc
 * @return number of elements highlighted.
 */
public int Highlight(Point m_mousePos, int type) 
{
	int highlighted = 0 ;
	Vector<GeometryElement> to_highlight = new Vector() ;
	clear_highlighted() ;
	switch(type) {
	//0==plain mouse movement.
	case 0 :
	{	
		//Should the highlighted elements list be cleared every time?	
		Vector elements_under = isPtOnGeometryElement(m_mousePos);
		to_highlight = merge_highlighted(to_highlight,elements_under) ;
		

	}
	
	case KeyEvent.VK_SHIFT:
	{
		Stroke lastStroke = m_drawData.getLastStroke(true);
		if ((lastStroke != null) && (lastStroke.containsPt(m_mousePos))) {
			to_highlight.add(lastStroke) ;
		}
			//addHighLightedElement(lastStroke);

		else
		{
			Vector aps = isPtOnAnyAnchorPoint(m_mousePos);

			if ((aps != null) && (aps.size() > 0))
				to_highlight.addAll(aps) ;
			else
			{
				Vector gEles = isPtOnGeometryElement(m_mousePos);
				if (gEles != null)
					to_highlight.addAll(gEles);
			}
		}	
	}
	
	case  KeyEvent.VK_CONTROL :
	{
		Vector gEles = isPtOnGeometryElement(m_mousePos);
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
						to_highlight.add(eq.getM_seg2());
					else
						to_highlight.add(eq.getM_seg1());	
				}
			}
		}
	}
		
	} // SWITCH
	
	for(GeometryElement e: to_highlight) {
		e.setHighlighted(true) ;
	}
	m_highlightedElements = to_highlight ;
	
	if(m_highlightedElements.size() > 0)
		System.out.println("Highligted" + m_highlightedElements.toString()) ;
	return to_highlight.size() ;
	
	
}


public int merge_selected_highlighted()
{
	m_highlightedElements.addAll(m_selectedElements) ;
	return m_selectedElements.size() ;
}

/**
 * Merges vector a into vector b. Simple intersection, but could add
 * complicated merging rules later on.
 * Where are all the java libraries ??? 
 * 
 * @param a
 * @param b
 * @return
 */
public Vector merge_highlighted(Vector a, Vector b)
{
	for (Object ao:a) {
		if(!b.contains(ao)) {
			b.add(ao) ;
		}
	}
	return b ;
}

/********************* END OF CLASS ***********************************/
}