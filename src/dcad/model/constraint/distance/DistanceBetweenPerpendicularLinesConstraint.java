package dcad.model.constraint.distance;

import java.awt.Graphics;
import java.util.StringTokenizer;
import java.util.Vector;
import java.awt.geom.Point2D;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.RelativeConstraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.length.RelLengthConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegLine;

public class DistanceBetweenPerpendicularLinesConstraint  extends RelLengthConstraint
{
	private double m_distance=0;
	
	public DistanceBetweenPerpendicularLinesConstraint(SegLine seg1, SegLine seg2,double distance, int catagory,boolean promoted)
	{
		super(seg1, seg2,Constraint.HARD,promoted);
		calcMarkers(seg1, seg2);
		
		calculateAndAddPoints(seg1,seg2);
		
		m_distance = distance;
	}
	
	private void calculateAndAddPoints(SegLine seg1,SegLine seg2)
	{
		AnchorPoint start1=seg1.getM_start();
		AnchorPoint end1=seg1.getM_end();
		AnchorPoint start2=seg2.getM_start();
		AnchorPoint end2=seg2.getM_end();
		AnchorPoint resultPoint=null;
		SegLine resultLine=null;
		AnchorPoint resultAuxiliaryPoint = null;
		
		Point2D ptS1L2 = seg2.getNearestPointOnSeg(start1.getM_point());
		Point2D ptE1L2 = seg2.getNearestPointOnSeg(end1.getM_point());
		Point2D ptS2L1 = seg1.getNearestPointOnSeg(start2.getM_point());
		Point2D ptE2L1 = seg1.getNearestPointOnSeg(end2.getM_point());
		
		
		double distS1L2 = start1.distance(ptS1L2);
		double distE1L2 = end1.distance(ptE1L2);
		double distS2L1 = start2.distance(ptS2L1);
		double distE2L1 = end2.distance(ptE2L1);
		
		if(distS1L2 > distE1L2)
			if(Math.abs(distS1L2 - seg1.getM_length() - distE1L2) < constraintsHelper.pointOverlapErrorThreshold )
			{
				resultPoint = end1;
				resultLine = seg2;
				resultAuxiliaryPoint = new AnchorPoint(ptS1L2.getX(),ptS1L2.getY());
			}

		if(resultPoint==null)
			if(distS1L2 <= distE1L2)
				if(Math.abs(distE1L2 - seg1.getM_length() - distS1L2) < constraintsHelper.pointOverlapErrorThreshold )
				{
					resultPoint = start1;
					resultLine = seg2;
					resultAuxiliaryPoint = new AnchorPoint(ptS1L2.getX(),ptS1L2.getY());
				}

		if(resultPoint==null)
			if(distS2L1 > distE2L1)
				if(Math.abs(distS2L1 - seg2.getM_length() - distE2L1) < constraintsHelper.pointOverlapErrorThreshold )
				{
					resultPoint = end2;
					resultLine = seg1;
					resultAuxiliaryPoint = new AnchorPoint(ptS2L1.getX(),ptS2L1.getY());
				}

		if(resultPoint==null)
			if(distS2L1 <= distE2L1)
				if(Math.abs(distE2L1 - seg2.getM_length() - distS2L1) < constraintsHelper.pointOverlapErrorThreshold )
				{
					resultPoint = start2;
					resultLine = seg1;
					resultAuxiliaryPoint = new AnchorPoint(ptS2L1.getX(),ptS2L1.getY());
				}
		
		addPoint(resultLine.getM_start());
		addPoint(resultLine.getM_end());
		addPoint(resultPoint);
		addPoint(resultAuxiliaryPoint);
	
	}
	
	public String toString()
	{
		return addPrefix()+"Distance Between Line Segments Constraint: Segments: "+m_seg1.getM_label()+", "+m_seg2.getM_label()+", Distance:" + m_distance;
	}

	public Vector getEquation(Vector fixedPoints)
	{
		Vector returnVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		returnVec.addAll(constraintEquations.getPointOnLineEQ(pointStrings[3],pointStrings[0],pointStrings[1]));
		returnVec.add(constraintEquations.getLineLengthEQ(pointStrings[2],pointStrings[3],m_distance));
		returnVec.add(constraintEquations.getPerpendicularSegmentsEQ(pointStrings[0],pointStrings[1],pointStrings[2],pointStrings[3]));
		return returnVec;
	}

	public Vector getPartialDifferentialString(String var, Vector fixedPoints)
	{
		Vector retVec = new Vector();
		StringTokenizer st = new StringTokenizer(var, ".");
		String pt = st.nextToken();
		String xory = st.nextToken();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		retVec.addAll(constraintEquations.getPointOnLinePD(pt,xory,anchorPoints[3],anchorPoints[0],anchorPoints[1],pointStrings[3],pointStrings[0],pointStrings[1]));
		retVec.add(constraintEquations.getLineLengthPD(pt,xory,anchorPoints[2],anchorPoints[3],pointStrings[2],pointStrings[3]));
		retVec.add(constraintEquations.getPerpendicularSegmentsPD(pt,xory,anchorPoints[0],anchorPoints[1],anchorPoints[2],anchorPoints[3],pointStrings[0],pointStrings[1],pointStrings[2],pointStrings[3]));
		return retVec;
	}

	public void draw(Graphics g)
	{
	}

	protected void calcMarkers(SegLine seg1, SegLine seg2)
	{
	}

	public void update()
	{
		calcMarkers((SegLine)m_seg1, (SegLine)m_seg2);
		super.update();
	}
	public boolean isConstraintSolved()
	{
		this.update();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		if(
				constraintsHelper.onLineOrNot(anchorPoints[3],anchorPoints[0],anchorPoints[1],true)
				&& constraintsHelper.independentLengthSatisfied(anchorPoints[2],anchorPoints[3],m_distance,true)
				&& constraintsHelper.areLinesPerpendicular(anchorPoints[0],anchorPoints[1],anchorPoints[2],anchorPoints[3],-1,true)
			)
			return true;
		///System.out.println("Distance between perpendicular lines constraint failed...");
		return false;
	}

	public void setM_distance(double m_distance)
	{
		this.m_distance = m_distance;
	}

}
