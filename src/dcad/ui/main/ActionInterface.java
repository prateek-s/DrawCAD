package dcad.ui.main;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.pointOnSegment.pointOnLineConstraint;
import dcad.model.constraint.points.NoMergeConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.ImpPoint;
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
import dcad.ui.drawing.DrawingData;
import dcad.util.GMethods;


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

    public DrawingData mDrawingData ;

    public Vector<GeometryElement> m_highlighedElements ;

    public Vector<GeometryElement> m_selectedElements ;



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

    /**
     *Move the elements to new location 'to'
     */
    public int A_move_Element(Vector<GeometryElement> elements, Point from, Point to,int ongoing) 
    {
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
    	
	elementsToMove = elements ;
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
Text t = new Text(val) ;
if(type=="angle") {
if(seg instanceof SegLine) {
	MarkerLineAngle marker = new MarkerLineAngle((SegLine)seg, t);
}
else if (seg instanceof SegCircleCurve) {
	MarkerCircleArcAngle marker = new MarkerCircleArcAngle((SegCircleCurve)seg, t);

}
}

if (type=="length") {
	if(seg instanceof SegLine) {
	MarkerLength marker = new MarkerLength((SegLine)seg, t);
	}
}
if (type=="radius") {
	MarkerRadius marker = new MarkerRadius((SegCircleCurve)seg, t);

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



/********************* END OF CLASS ***********************************/
}