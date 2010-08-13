package dcad.process.recognition.constraint;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Vector;

import dcad.Prefs;
import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.connect.IntersectionConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.SegPoint;
import dcad.model.geometry.segment.Segment;
import dcad.util.GConstants;
import dcad.util.GMethods;
import dcad.util.GVariables;

public class ConnectionRecognizer extends RelConstraintRecognitionScheme
{
	public ConnectionRecognizer(Segment seg1, Segment seg2)
	{
		super(seg1, seg2);
	}

	public Vector recognize()
	{
		// when segment 1 is a Point Segment
		if((m_seg1 instanceof SegPoint) && (m_seg2 instanceof SegPoint))
		{
			getConstraints((SegPoint)m_seg1, (SegPoint)m_seg2);
		}
		else if((m_seg1 instanceof SegPoint) && (m_seg2 instanceof SegLine))
		{
			getConstraints((SegLine)m_seg2, (SegPoint)m_seg1);
		}
		else if((m_seg1 instanceof SegPoint) && (m_seg2 instanceof SegCircleCurve))
		{
			getConstraints((SegCircleCurve)m_seg2, (SegPoint)m_seg1);
		}
		
		//Segment 1 is line
		else if((m_seg1 instanceof SegLine) && (m_seg2 instanceof SegPoint))
		{
			getConstraints((SegLine)m_seg1, (SegPoint)m_seg2);
		}
		else if((m_seg1 instanceof SegLine) && (m_seg2 instanceof SegLine))
		{
			getConstraints((SegLine)m_seg1, (SegLine)m_seg2);
		}
		else if((m_seg1 instanceof SegLine) && (m_seg2 instanceof SegCircleCurve))
		{
			getConstraints((SegCircleCurve)m_seg2, (SegLine)m_seg1);
		}

		//Segment 1 is curve
		else if((m_seg1 instanceof SegCircleCurve) && (m_seg2 instanceof SegPoint))
		{
			getConstraints((SegCircleCurve)m_seg1, (SegPoint)m_seg2);
		}
		else if((m_seg1 instanceof SegCircleCurve) && (m_seg2 instanceof SegLine))
		{
			getConstraints((SegCircleCurve)m_seg1, (SegLine)m_seg2);
		}
		else if((m_seg1 instanceof SegCircleCurve) && (m_seg2 instanceof SegCircleCurve))
		{
			getConstraints((SegCircleCurve)m_seg1, (SegCircleCurve)m_seg2);
		}

		return m_constraints;
	}
	
	private void getConstraints(SegPoint seg1, SegPoint seg2)
	{
		// find intersection points
		Vector intersections = seg1.intersects(seg2);
		if(intersections.size() == 1)
		{
			// there can be only one point of intersection between two Points
			addIntersectionConstraint(seg1, seg2, (Point2D)intersections.get(0), (Point2D)intersections.get(0));
		}
		else if(intersections.size() == 0)
		{
			// no intersections found, find if the points are close togeather
			Vector touches = seg1.touches(seg2);
			if(touches.size() == 2)
			{
				// there can be only one point of intersection between two points
				addIntersectionConstraint(seg1, seg2, (Point2D)touches.get(0), (Point2D)touches.get(1));
			}
		}
	}
	
	private void getConstraints(SegLine seg1, SegPoint seg2)
	{
		// find intersection points
		Vector intersections = seg1.intersects(seg2);
		if(intersections.size() == 1)
		{
			// there can be only one point of intersection between a line and a line
			addIntersectionConstraint(seg1, seg2, (Point2D)intersections.get(0), (Point2D)intersections.get(0));
		}
		else if(intersections.size() == 0)
		{
			// no intersections found, find if the lines are close togeather
			Vector touches = seg1.touches(seg2);
			if(touches.size() == 2)
			{
				// there can be only one point of intersection between two lines
				addIntersectionConstraint(seg1, seg2, (Point2D)touches.get(0), (Point2D)touches.get(1));
			}
		}
	}	
	
	private void getConstraints(SegLine seg1, SegLine seg2)
	{
		AnchorPoint s1 = seg1.getM_start();
		AnchorPoint e1 = seg1.getM_end();
		AnchorPoint s2 = seg2.getM_start();
		AnchorPoint e2 = seg2.getM_end();
		
		
		if( seg1.getNearestPointOnSeg(s2.getM_point()).distance(s2.getM_point()) < Constraint.MAX_ALLOWED_CONNECT_GAP 
				|| seg1.getNearestPointOnSeg(e2.getM_point()).distance(e2.getM_point()) < Constraint.MAX_ALLOWED_CONNECT_GAP 
				|| seg2.getNearestPointOnSeg(s1.getM_point()).distance(s1.getM_point()) < Constraint.MAX_ALLOWED_CONNECT_GAP 
				|| seg2.getNearestPointOnSeg(e1.getM_point()).distance(e1.getM_point()) < Constraint.MAX_ALLOWED_CONNECT_GAP
				)
		{
			addIntersectionConstraint(seg1, seg2, new Point2D.Double(0,0),new Point2D.Double(0,0));
			return;
		}
			
		//It should not come here.
		
		
		// find intersection points
		Vector intersections = seg1.intersects(seg2);
		if(intersections.size() == 1)
		{
//			if(! (endPointsIntersect(seg1,seg2,(Point2D)intersections.elementAt(0))))
			{
			// there can be only one point of intersection between two lines
				addIntersectionConstraint(seg1, seg2, (Point2D)intersections.get(0), (Point2D)intersections.get(0));
			}
		}
		else if(intersections.size() == 0)
		{
			// no intersections found, find if the lines are close together
			Vector touches = seg1.touches(seg2);
			if(touches.size() == 4)
			{
				// there can be only one point of intersection between two lines
				// if two near points were found and no intersection points were found then the line are parallel and close to each other.
				// but this would be detected by the constraint recognizer and not connection recognizer.
				// therefor use the first 2 intersection points
				addIntersectionConstraint(seg1, seg2, (Point2D)touches.get(0), (Point2D)touches.get(1));
			}
			else if(touches.size() == 2)
			{
				// there can be only one point of intersection between two lines
				addIntersectionConstraint(seg1, seg2, (Point2D)touches.get(0), (Point2D)touches.get(1));
			}
		}
	}
	
	private void getConstraints(SegCircleCurve seg1, SegPoint seg2)
	{
		// find intersection points
		Vector intersections = seg1.intersects(seg2);
		if(intersections.size() == 1)
		{
			// there can be only one point of intersection between a circular curve and a point
			addIntersectionConstraint(seg1, seg2, (Point2D)intersections.get(0), (Point2D)intersections.get(0));
		}
		else if(intersections.size() == 0)
		{
			// no intersections found, find if the lines are close togeather
			Vector touches = seg1.touches(seg2);
			if(touches.size() == 2)
			{
				// there can be only one point of intersection between two lines
				addIntersectionConstraint(seg1, seg2, (Point2D)touches.get(0), (Point2D)touches.get(1));
			}
		}
	}
	
	private void getConstraints(SegCircleCurve seg1, SegLine seg2)
	{
		// find intersection points
		Vector intersections = seg1.intersects(seg2);
		Line2D line = (Line2D)seg2.getM_shape();

		// add all the intersection points found
		if(intersections.size() == 2)
		{
			//check if they are close togeather.. in which case its a possible tangent
			Point2D p1 = (Point2D)intersections.get(0);
			Point2D p2 = (Point2D)intersections.get(1);
			Point2D mp = new Point.Double((p1.getX()+p2.getX())/2, (p1.getY()+p2.getY())/2);
			Point2D nearMP = seg1.getNearestPointOnSeg(mp);
			if(nearMP != null)
			{
				if(mp.distance(nearMP) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
				{
				}
				else
				{
					addIntersectionConstraint(seg1, seg2, p1, p1);
					addIntersectionConstraint(seg1, seg2, p2, p2);
				}
			}
			else
			{
				addIntersectionConstraint(seg1, seg2, p1, p1);
				addIntersectionConstraint(seg1, seg2, p2, p2);
			}
		}
		else if(intersections.size() == 1)
		{
			Point2D interPt = (Point2D)intersections.get(0);
			// check for tangency, For this check if both the end points of the circular curve are on the same side of the line,
			// as if they are then the line is tanget to the circular curve at the intersection point.
			// check if there distance of the end points of the line are a distance >= the radius of the circular arc
//			if(((seg1.getM_center().distance(line.getP1()) >= seg1.getM_radius()) && ((seg1.getM_center().distance(line.getP2()) >= seg1.getM_radius()))) && (line.relativeCCW(seg1.getM_start().getM_point()) == line.relativeCCW(seg1.getM_end().getM_point())))
			double dist = line.ptLineDist(seg1.getM_center().getM_point());
			if((dist >= (seg1.getM_radius()-Constraint.MAX_ALLOWED_CONNECT_GAP))&&(dist <= (seg1.getM_radius()+Constraint.MAX_ALLOWED_CONNECT_GAP)))
			{
			}
			else
			{
				// add the intersection point
				addIntersectionConstraint(seg1, seg2, interPt, interPt);
				// the line is not tangent to the curve. Find the touch points. 

				Vector touches = seg1.touches(seg2);
				// As we already have one interseciton point, Only end points of the curve and/or the line can be other possible intersection points.
				if(touches.size() == 2)
				{
					Point2D pt1 = (Point2D)touches.get(0);
					Point2D pt2 = (Point2D)touches.get(1);
					if((pt1.distance(interPt) > 1)&&(pt2.distance(interPt) > 1))
					{
						addIntersectionConstraint(seg1, seg2, (Point2D)touches.get(0), (Point2D)touches.get(1));
					}
				}
			}
		}
		else if(intersections.size() == 0)
		{
			Vector touches = seg1.touches(seg2);
			if(touches.size() == 2)
			{
				// only to touch points. This can be a tangent point.
				if(Math.abs(line.ptLineDist(seg1.getM_center().getM_point()) - seg1.getM_radius()) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
				{
				}
				else
				{
					addIntersectionConstraint(seg1, seg2, (Point2D)touches.get(0), (Point2D)touches.get(1));
				}
			}
			if(touches.size() == 4)
			{
				addIntersectionConstraint(seg1, seg2, (Point2D)touches.get(0), (Point2D)touches.get(1));
				addIntersectionConstraint(seg1, seg2, (Point2D)touches.get(2), (Point2D)touches.get(3));
			}
		}
	}
	
	private void getConstraints(SegCircleCurve seg1, SegCircleCurve seg2)
	{
		// find intersection points
		Vector intersections = seg1.intersects(seg2);
		double distr1_r2 = seg1.getM_center().distance(seg2.getM_center()); 

		if(intersections.size() == 2)
		{
			//check if they are close togeather.. in which case its a possible tangent
			Point2D p1 = (Point2D)intersections.get(0);
			Point2D p2 = (Point2D)intersections.get(1);

			Point2D mp = new Point.Double((p1.getX()+p2.getX())/2, (p1.getY()+p2.getY())/2);
			Point2D nearMP_1 = seg1.getNearestPointOnSeg(mp);
			Point2D nearMP_2 = seg2.getNearestPointOnSeg(mp);
			
			if((nearMP_1 != null) && (nearMP_1 != null))
			{
				if((mp.distance(nearMP_1)+mp.distance(nearMP_2)) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
				{
				}
				else
				{
					addIntersectionConstraint(seg1, seg2, p1, p1);
					addIntersectionConstraint(seg1, seg2, p2, p2);
				}
			}
/*			if(p1.distance(p2) < Constraint.MIN_ALLOWED_CONNECT_GAP)
			{
				// the two points are close togeather, the line is tangent to the circular curve
				Point mp = new Point((int)((p1.getX()+p2.getX())/2), (int)((p1.getY()+p2.getY())/2));
				addTangentConstraint(seg1, seg2, mp);
				addTangentConstraint(seg2, seg1, mp);
			}
*/			else
			{
				addIntersectionConstraint(seg1, seg2, p1, p1);
				addIntersectionConstraint(seg1, seg2, p2, p2);
			}
		}
		else if(intersections.size() == 1)
		{
			// check if the distance of the centers of the two curves is alomst equal the sum 
			// of the distances of the two centers with the point of intersection
			Point2D interPt = (Point2D)intersections.get(0);
			
			// check if this is a Tangent point
			if((Math.abs((seg1.getM_radius()+seg2.getM_radius()) - distr1_r2) < Constraint.MAX_ALLOWED_CONNECT_GAP)
				|| (Math.abs(Math.abs(seg1.getM_radius()-seg2.getM_radius()) - distr1_r2) < Constraint.MAX_ALLOWED_CONNECT_GAP))
			{
			}
			else
			{
				// the curves are NOT tangent to each other, add the intersection point
				addIntersectionConstraint(seg1, seg2, interPt, interPt);
				
				// Find the touch points. 
				Vector touches = seg1.touches(seg2);
				// As we already have one interseciton point, Only end points of the curve and/or the line can be other possible intersection points.
				if(touches.size() == 2)
				{
					addIntersectionConstraint(seg1, seg2, (Point2D)touches.get(0), (Point2D)touches.get(1));
				}
			}
		}
		else if(intersections.size() == 0)
		{
				// no intersections, find the closes points on both the segments
			Vector touches = m_seg1.touches(m_seg2);
			if(touches.size() == 2)
			{
				// only 2 touch points. This can be a tangent point.
				if(((distr1_r2 <= (seg1.getM_radius() + seg2.getM_radius()) + Constraint.MAX_ALLOWED_CONNECT_GAP) && (distr1_r2 > (seg1.getM_radius() + seg2.getM_radius()))) 
						|| ((distr1_r2 < Math.abs(seg1.getM_radius() - seg2.getM_radius())) && (distr1_r2 >= (Math.abs(seg1.getM_radius() - seg2.getM_radius()) - Constraint.MAX_ALLOWED_CONNECT_GAP))))
//				if((Math.abs(distr1_r2 - (seg1.getM_radius() + seg2.getM_radius())) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
//					|| ((seg1.getM_radius() - distr1_r2 + Constraint.MAX_ALLOWED_CONNECT_GAP) >= seg2.getM_radius())
//					|| ((seg2.getM_radius() - distr1_r2 + Constraint.MAX_ALLOWED_CONNECT_GAP) >= seg1.getM_radius()))
				{
				}
				else
				{
					addIntersectionConstraint(seg1, seg2, (Point2D)touches.get(0), (Point2D)touches.get(1));
				}
			}
			
			if(touches.size() == 4)
			{
				addIntersectionConstraint(m_seg1, m_seg2, (Point2D)touches.get(0), (Point2D)touches.get(1));
				addIntersectionConstraint(m_seg1, m_seg2, (Point2D)touches.get(2), (Point2D)touches.get(3));
			}
		}
	}
	
	private Constraint addIntersectionConstraint(Segment seg1, Segment seg2, Point2D pt1, Point2D pt2)
	{
 		Constraint ic = new IntersectionConstraint(seg1, seg2, pt1, pt2,Constraint.SOFT,true);
 		addConstraint(ic,new Segment[]{seg1,seg2});
		return ic;
	}
	
}