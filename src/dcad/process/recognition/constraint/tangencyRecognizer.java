package dcad.process.recognition.constraint;

import java.awt.geom.Point2D;
import java.util.Vector;
import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.connect.lineCircularCurveTangencyConstraint;
import dcad.model.constraint.connect.twoCircularCurveTangencyConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;

public class tangencyRecognizer extends RelConstraintRecognitionScheme
{
	public tangencyRecognizer(Segment seg1, Segment seg2)
	{
		super(seg1, seg2);
	}

	protected void init(Segment seg1, Segment seg2)
	{
		super.init(seg1, seg2);
	}

	public Vector recognize()
	{
		if((m_seg1 instanceof SegLine) && (m_seg2 instanceof SegCircleCurve))
		{
			getConstraints((SegCircleCurve)m_seg2, (SegLine)m_seg1);
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
	
	private void getConstraints(SegCircleCurve seg1, SegLine seg2)
	{
		//1-6-2008 Changed the logic to recognize tangency
		Point2D p1 = seg2.getNearestPointOnSeg(seg1.getM_center().getM_point());
		Point2D p2 = seg1.getNearestPointOnSeg(p1);
		if(
				constraintsHelper.withinRange(p1.distance(p2),0,Constraint.MAX_ALLOWED_CONNECT_GAP)
				&& constraintsHelper.areLinesPerpendicular(p1,seg1.getM_center().getM_point(),seg2.getM_start().getM_point(),seg2.getM_end().getM_point(),20,false)
		)
			addConstraint(addLineCircleTangency(seg2, seg1, p1,p2),new Segment[]{seg2,seg1});
	}
	
	private void getConstraints(SegCircleCurve seg1, SegCircleCurve seg2)
	{
		//1-6-2008 Changed the logic to recognize tangency
		double distanceBetweenCenters = seg1.getM_center().distance(seg2.getM_center());
		double sumOfRadius = seg1.getM_radius() + seg2.getM_radius();
		Point2D p1 = seg1.getNearestPointOnSeg(seg2.getM_center().getM_point());
		Point2D p2 = seg2.getNearestPointOnSeg(seg1.getM_center().getM_point());
		
		if(
				constraintsHelper.withinRange(distanceBetweenCenters,sumOfRadius,Constraint.MAX_ALLOWED_CONNECT_GAP)
				&& constraintsHelper.withinRange(p1.distance(p2),0,Constraint.MAX_ALLOWED_CONNECT_GAP)
				)
		{
			addCircleCircleTangency(seg1, seg2,p1,p2);
		}
	}
		
	public static Constraint addLineCircleTangency(SegLine segL,SegCircleCurve segC,Point2D pt1,Point2D pt2)
	{
			if(!(constraintsHelper.getConstraintBetween2Segments(segL,segC,lineCircularCurveTangencyConstraint.class)!=null)) //There is no such constraint added previously
			{
				AnchorPoint ap=null;
				
				if(segL.getM_start()==segC.getM_start())
					ap=segL.getM_start();
				else if(segL.getM_end()==segC.getM_start())
					ap=segL.getM_end();
				else if(segL.getM_start()==segC.getM_end())
					ap=segL.getM_start();
				else if(segL.getM_end()==segC.getM_end())
					ap=segL.getM_end();

				//Previously I was checking if two points are same.
				//If so, add tangency at that place. (See the code above)
				//This did'nt take into account the fact that if connection is near one of the end points, that endpoint should become
				//the point of tangency. So, added the following code.
				
				if(ap==null)
				{
					Point2D p = new Point2D.Double( (pt1.getX() + pt2.getX())/2 , (pt1.getY() + pt2.getY())/2 );
					if(segL.getM_start().distance(p) < Constraint.MAX_ALLOWED_CONNECT_GAP)
						ap = segL.getM_start();
					else if(segL.getM_end().distance(p) < Constraint.MAX_ALLOWED_CONNECT_GAP)
						ap = segL.getM_end();
					else if(segC.getM_start().distance(p) < Constraint.MAX_ALLOWED_CONNECT_GAP)
						ap = segC.getM_start();
					else if(segC.getM_end().distance(p) < Constraint.MAX_ALLOWED_CONNECT_GAP)
						ap = segC.getM_end();
					else
						ap=new AnchorPoint(p.getX(),p.getY());
				}
				
				Constraint tc=new lineCircularCurveTangencyConstraint(segL,segC,ap,Constraint.HARD,false);
				return tc;
			}
			return null;
	}
	
	private void addCircleCircleTangency(SegCircleCurve segC1,SegCircleCurve segC2,Point2D pt1,Point2D pt2)
	{
		if(!(constraintsHelper.getConstraintBetween2Segments(segC1,segC2,twoCircularCurveTangencyConstraint.class)!=null)) //There is no such constraint added previously
		{
			AnchorPoint ap=null;
			if(ap==null)
				ap=new AnchorPoint(pt1.getX(),pt1.getY());
			Constraint tc=new twoCircularCurveTangencyConstraint(segC1,segC2,ap,Constraint.HARD,false);
			addConstraint(tc,new Segment[]{segC1,segC2});
		}
	}
}
