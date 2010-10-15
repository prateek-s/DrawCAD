package dcad.ui.main;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

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
import dcad.process.beautification.ConstraintSolver;
import dcad.process.io.Command;
import dcad.process.preprocess.PreProcessingManager;
import dcad.process.preprocess.PreProcessor;
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
	 * Refresh drawing after drawing a new stroke.
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
			addConstraintsForMarkers(); 
		}
		return new_constraints ;
	}
	
	
	public int A_delete_Segment(Segment seg) 
	{

	}

	public int A_move_Segment(Segment seg, Point to) 
	{

	}

	public int A_change_seg_property(Segment seg, String property_type, String value) 
	{

	}

	public int A_undo(int count) 
	{

	}

	public int A_redo(int count) 
	{

	}

	public int A_change_to_marker(Stroke strk) 
	{

	}

	public int A_change_marker_to_segment(Stroke strk) 
	{

	}

	public int A_add_marker( )
	{
		
	}
	
	/**
	 * partition line segment earlier.
	 * @param pt
	 * @return
	 */
	public int A_add_anchor_point(GeometryElement e, Point pt) 
	{
		int x = pt.x ;
		int y = pt.y ;
		if (e instanceof AnchorPoint)
		{
			System.out.println("Anchor point clicked");
			System.out.println(" \n\n\n Shift key clicked \n\n\n ");
			AnchorPoint ap = (AnchorPoint) e;
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
				snapIPsAndRecalculateConstraints(newConstraints);
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
						}
						else if (seg instanceof SegCircleCurve)
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
						}
						else
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
					newConstraints = new Vector();
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
							newConstraints.add(c);
						}
					}
					snapIPsAndRecalculateConstraints(newConstraints);
					UpdateUI(1,newConstraints);
				}

			}
		}

	}

	public int A_delete_anchor_point(Point pt) 
	{

	}

	public int A_add_text(String text, Point location) 
	{
		addConstraintsForMarkers();

	}

	/**
	 * To be moved out. 
	 * @param X
	 * @param Y
	 * @param c
	 */
	public void writeText(int X, int Y, String c) 
	{
		Text t = new Text(c + "", X, Y);
		if (t != null) {
			addGeoElement(t);
			logEvent("writeText({int}" + X + ", {int}" + Y + ", {"
					+ String.class.getName() + "}" + c + ");");
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


	public int A_delete_text(Point location)
	{

	}

	public int A_change_Segment_to(Segment seg, int seg_type) 
	{

	}

	public int A_clear() 
	{

	}

	public int A_load()
	{

	}

	public int A_save() 
	{

	}

	public int A_add_constraint(Constraint constraint) 
	{

	}

	public int A_delete_constraint(Constraint constraint) 
	{


	}


	public Segment A_seg_selected(Point pt) 
	{


	}

	/**
	 * returns the geometric elements under this point. Can be several, so return a vector.
	 * @param pt
	 * @return
	 */
	public Vector<GeometryElement>  A_elements_selected(Point pt,Vector m_selectedElements1) 
	{

		int x = pt.x ;
		int y = pt.y ;
		Vector<GeometryElement> m_selectedElements = m_selectedElements1;
		
		// check if the mouse is close to any other geometry element
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
			} else {
				if ((ele.isSelected()) && (m_selectedElements.contains(ele))) {
					ele.setSelected(false);
					m_selectedElements.remove(ele);
				} else if ((!ele.isSelected())
						&& (!m_selectedElements.contains(ele))) {
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


	/*********************************************************************/
	

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