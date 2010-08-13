package dcad.process.recognition.constraint;

import java.util.Vector;

import dcad.model.constraint.angle.ParallelSegConstraint;
import dcad.model.constraint.collinearity.*;//CollinearPointConstraint;
import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.SegPoint;
import dcad.model.geometry.segment.Segment;
import dcad.util.GMethods;

public class CollinearPointsRecognizer extends RelConstraintRecognitionScheme
{
	
	public CollinearPointsRecognizer(Segment seg1, Segment seg2)
	{
		super(seg1, seg2);
	}
	
	public Vector recognize()
	{
		if((m_seg1 instanceof SegPoint) && (m_seg2 instanceof SegPoint))
		{
			// do nothing, two points are anyways collinear
		}
		
		else if((m_seg1 instanceof SegPoint) && (m_seg2 instanceof SegLine))
		{
//			getConstraints((SegLine)m_seg2, (SegPoint)m_seg1);
		}
		else if((m_seg1 instanceof SegLine) && (m_seg2 instanceof SegPoint))
		{
//			getConstraints((SegLine)m_seg1, (SegPoint)m_seg2);
		}

		else if((m_seg1 instanceof SegPoint) && (m_seg2 instanceof SegCircleCurve))
		{
//			getConstraints((SegCircleCurve)m_seg2, (SegPoint)m_seg1);
		}
		else if((m_seg1 instanceof SegCircleCurve) && (m_seg2 instanceof SegPoint))
		{
//			getConstraints((SegCircleCurve)m_seg1, (SegPoint)m_seg2);
		}

		else if((m_seg1 instanceof SegLine) && (m_seg2 instanceof SegCircleCurve))
		{
			getConstraints((SegCircleCurve)m_seg2, (SegLine)m_seg1);
		}
		else if((m_seg1 instanceof SegCircleCurve) && (m_seg2 instanceof SegLine))
		{
			getConstraints((SegCircleCurve)m_seg1, (SegLine)m_seg2);
		}

		else if((m_seg1 instanceof SegLine) && (m_seg2 instanceof SegLine))
		{
			getConstraints((SegLine)m_seg1, (SegLine)m_seg2);
		}
		
		else if((m_seg1 instanceof SegCircleCurve) && (m_seg2 instanceof SegCircleCurve))
		{
			getConstraints((SegCircleCurve)m_seg1, (SegCircleCurve)m_seg2);
		}

		return m_constraints;
	}
	
	private void getConstraints(SegLine seg1, SegLine seg2)
	{
		AnchorPoint start1=seg1.getM_start();
		AnchorPoint end1=seg1.getM_end();
		AnchorPoint start2=seg2.getM_start();
		AnchorPoint end2=seg2.getM_end();

		//Lines are collinear
		if(constraintsHelper.getConstraintBetween2Segments(seg1,seg2,CollinearLinesConstraint.class)!=null)
			return;
		//One line contains one of the end points of the other line. PointOnSegment constraint will take care of this.
		if(seg1.containsPt(start2.getM_point()) || seg1.containsPt(end2.getM_point()) || seg2.containsPt(start1.getM_point()) || seg2.containsPt(end1.getM_point()))
			return;
		
		if (findCollinearityAndAdd(start2,start1,end1))
			return ;
		if(findCollinearityAndAdd(end2,start1,end1))
			return ;
		if(findCollinearityAndAdd(start1,start2,end2))
			return ;
		if(findCollinearityAndAdd(end1,start2,end2))
			return ;
	}

	private void getConstraints(SegCircleCurve segCCurve, SegLine segLine)
	{
//		findCollinearityAndAdd(segCCurve.getM_start(), segCCurve.getM_end(), segLine.getM_start());
//		findCollinearityAndAdd(segCCurve.getM_start(), segCCurve.getM_end(), segLine.getM_end());
//		findCollinearityAndAdd(segLine.getM_start(), segLine.getM_end(), segCCurve.getM_start());
//		findCollinearityAndAdd(segLine.getM_start(), segLine.getM_end(), segCCurve.getM_end());
		if(segLine.containsPt(segCCurve.getM_center().getM_point()) || segLine.containsPt(segCCurve.getM_center().getM_point()))
			return;
		findCollinearityAndAdd(segLine.getM_start(), segLine.getM_end(), segCCurve.getM_center());
	}
	
	private void getConstraints(SegCircleCurve segCCurve1, SegCircleCurve segCCurve2)
	{
/*		findCollinearityAndAdd(segCCurve1.getM_start(), segCCurve1.getM_end(), segCCurve2.getM_start());
		findCollinearityAndAdd(segCCurve1.getM_start(), segCCurve1.getM_end(), segCCurve2.getM_end());
		findCollinearityAndAdd(segCCurve1.getM_start(), segCCurve1.getM_end(), segCCurve2.getM_center());
		findCollinearityAndAdd(segCCurve2.getM_start(), segCCurve2.getM_end(), segCCurve1.getM_start());
		findCollinearityAndAdd(segCCurve2.getM_start(), segCCurve2.getM_end(), segCCurve1.getM_end());
		findCollinearityAndAdd(segCCurve2.getM_start(), segCCurve2.getM_end(), segCCurve1.getM_center());*/
	}
	
	private void getConstraints(SegLine l, SegPoint p)
	{
		//Add the constraint only if the line doesn't contain the point
		if(!l.containsPt(p.getM_pt().getM_point()))
			findCollinearityAndAdd(p.getM_pt(),l.getM_start(), l.getM_end());
	}
	private void getConstraints(SegCircleCurve segCCurve, SegPoint segPoint)
	{
		if(!segCCurve.containsPt(segPoint.getM_pt().getM_point()))
			findCollinearityAndAdd( segPoint.getM_pt(),segCCurve.getM_start(), segCCurve.getM_end());
	}
	
	/**
	 * Returns the actual distance of the pt from infinitely extended line, if the pt is collinear to the line, -1 otherwise
	 * @param line
	 * @param pt
	 * @return  
	 */
	private boolean findCollinearityAndAdd(AnchorPoint ap1, AnchorPoint ap2, AnchorPoint ap3)
	{
		//If constraint already exists, return
		AnchorPoint v[];
		v=new AnchorPoint[]{ap1,ap2,ap3};
		if(constraintsHelper.doesConstraintAlreadyExist(ap1,CollinearPointsConstraint.class,v)!=null)
			return true;
		if(constraintsHelper.areSlopesEqual(ap1,ap2,ap3,ap2,false))
		{
			CollinearPointsConstraint cc = new CollinearPointsConstraint(new AnchorPoint[]{ap3, ap1, ap2},Constraint.HARD,true);
			addConstraint(cc,new Segment[]{});
			return true;
		}
		return false;
	}
}