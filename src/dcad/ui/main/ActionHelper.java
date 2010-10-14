package dcad.ui.main;

import java.awt.Cursor;
import java.awt.Point;
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
import dcad.model.constraint.pointOnSegment.pointOnCircularCurveConstraint;
import dcad.model.constraint.pointOnSegment.pointOnLineConstraint;
import dcad.model.constraint.pointOnSegment.pointOnPointConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.ImpPoint;
import dcad.model.geometry.PixelInfo;
import dcad.model.geometry.SegmentPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.model.marker.Marker;
import dcad.process.ProcessManager;
import dcad.process.beautification.ConstraintSolver;
import dcad.process.recognition.RecognitionManager;
import dcad.process.recognition.marker.MarkerRecogManager;
import dcad.process.recognition.marker.MarkerToConstraintConverter;
import dcad.process.recognition.segment.SegmentRecognizer;
import dcad.process.recognition.stroke.StrokeRecognizer;
import dcad.ui.drawing.DrawingData;
import dcad.util.GVariables;
import dcad.util.Maths;

public class ActionHelper 
{
	public DrawingData m_drawData = new DrawingData();

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
	
	/**
	 * Calls a lot of methods in other parts of the program. This is the key method where everything is set in motion.
	 * @param theStroke
	 * @return
	 */
	public Vector recognize_Constraints(Stroke theStroke)
	{
		Stroke m_currStroke = theStroke;
		
//		recognizeSegments(theStroke);
//		adjustStroke(theStroke);
//		m_drawData.addStroke(theStroke);
		
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
				addGeoElement(marker) ;
			
			
		//	UpdateUI(1,m_drawData.getM_constraints());
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

			return constraints;
		}
	}
	

	public Vector<Segment> recognizeSegments(Stroke theStroke)
	{
		//m_currStroke = theStroke;
		RecognitionManager recogMan = m_processManager.getRecogManager();
		Vector<Segment> Segments = null ;
		// identify segments
		try
		{
			SegmentRecognizer segmentRecog = recogMan.getSegmentRecogMan().getSegmentRecognizer();
			// added on 23-02-10
			// if this stroke is converted, then no need to call segment recognizer 
			
			Segments = theStroke.recognizeSegments(segmentRecog);
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
		
		return Segments ;
		
	}
	
	
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

					return recognizeSegmentsAndConstraints(theStroke);

				}
			}

		}
		return null;
	}

	
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

			return recognizeSegmentsAndConstraints(theStroke);
		} else
		{
			JOptionPane.showMessageDialog(null, "No Close Pixel found ", "Resegmentation Error",
					JOptionPane.ERROR_MESSAGE, null);
			return null;
		}
	}
	
	
	public Vector performSegRecycling(Vector m_highlightedElements, int x, int y)
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
					Vector vec = A.performReSegmentation(stroke, pt);
					if (vec != null)
						modConstraints.addAll(vec);
				}
				break;
			}
			if (element instanceof AnchorPoint)
			{
				AnchorPoint ap = (AnchorPoint) element;
				Vector vec = A.performSegFusion(ap);
				if (vec != null)
					modConstraints.addAll(vec);
				break;
			}
		}
		return modConstraints;
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


	public boolean Check_for_Collinearity(GeometryElement e)
	{
		String parsedCons[];
		
		
		
		parsedCons = e.getClass().toString().split("[ ]+");
		
		if(parsedCons[1].compareToIgnoreCase("dcad.model.geometry.segment.SegLine") == 0)
		{

			Segment seg  = (Segment)e;

			Vector parallelLinesConstraintList = A.getParallelLinesConsList("lines", "parellel", seg);
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
	
	
	
	
	
} // END CLASS
