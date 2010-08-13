package dcad.model.constraint.distance;

import java.awt.Graphics;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.length.RelLengthConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegLine;
import dcad.util.GConstants;

public class DistanceBetweenParallelLinesConstraint extends RelLengthConstraint
{
	private double m_distance=0;
	private double distanceToShow = 0;
	public DistanceBetweenParallelLinesConstraint(SegLine seg1, SegLine seg2,double distance, int catagory,boolean promoted)
	{
		super(seg1, seg2,Constraint.HARD,promoted);
		calcMarkers(seg1, seg2);
		distanceToShow = distance;
		addPoint(seg1.getM_start());
		addPoint(seg1.getM_end());
		addPoint(seg2.getM_start());
		addPoint(seg2.getM_end());
		
		//Add two auxiliary points
		AnchorPoint p1=new AnchorPoint(seg1.getM_start().getX(),seg1.getM_start().getY());
		AnchorPoint p2=new AnchorPoint(seg2.getM_start().getX(),seg2.getM_start().getY());
		addPoint(p1);
		addPoint(p2);
		m_distance = distance * GConstants.cmScaleDrawingRatio;
	}
	
	public String toString()
	{
		return addPrefix()+"Distance Between Line Segments Constraint: Segments: "+m_seg1.getM_label()+", "+m_seg2.getM_label()+", Distance:" + distanceToShow;
	}

	public Vector getEquation(Vector fixedPoints)
	{
		Vector returnVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		returnVec.addAll(constraintEquations.getPointOnLineEQ(pointStrings[4],pointStrings[0],pointStrings[1]));
		returnVec.addAll(constraintEquations.getPointOnLineEQ(pointStrings[5],pointStrings[2],pointStrings[3]));
		returnVec.add(constraintEquations.getLineLengthEQ(pointStrings[4],pointStrings[5],m_distance));
		returnVec.add(constraintEquations.getPerpendicularSegmentsEQ(pointStrings[0],pointStrings[1],pointStrings[4],pointStrings[5]));
		returnVec.add(constraintEquations.getPerpendicularSegmentsEQ(pointStrings[2],pointStrings[3],pointStrings[4],pointStrings[5]));
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
		retVec.addAll(constraintEquations.getPointOnLinePD(pt,xory,anchorPoints[4],anchorPoints[0],anchorPoints[1],pointStrings[4],pointStrings[0],pointStrings[1]));
		retVec.addAll(constraintEquations.getPointOnLinePD(pt,xory,anchorPoints[5],anchorPoints[2],anchorPoints[3],pointStrings[5],pointStrings[2],pointStrings[3]));
		retVec.add(constraintEquations.getLineLengthPD(pt,xory,anchorPoints[4],anchorPoints[5],pointStrings[4],pointStrings[5]));
		retVec.add(constraintEquations.getPerpendicularSegmentsPD(pt,xory,anchorPoints[0],anchorPoints[1],anchorPoints[4],anchorPoints[5],pointStrings[0],pointStrings[1],pointStrings[4],pointStrings[5]));
		retVec.add(constraintEquations.getPerpendicularSegmentsPD(pt,xory,anchorPoints[2],anchorPoints[3],anchorPoints[4],anchorPoints[5],pointStrings[2],pointStrings[3],pointStrings[4],pointStrings[5]));
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
				constraintsHelper.onLineOrNot(anchorPoints[4],anchorPoints[0],anchorPoints[1],true)
				&& constraintsHelper.onLineOrNot(anchorPoints[5],anchorPoints[2],anchorPoints[3],true)
				&& constraintsHelper.independentLengthSatisfied(anchorPoints[4],anchorPoints[5],m_distance,true)
				&& constraintsHelper.areLinesPerpendicular(anchorPoints[0],anchorPoints[1],anchorPoints[4],anchorPoints[5],-1,true)
				&& constraintsHelper.areLinesPerpendicular(anchorPoints[2],anchorPoints[3],anchorPoints[4],anchorPoints[5],-1,true)
			)
			return true;
		System.out.println("Distance between parallel lines constraint failed... ");
		return false;
	}

	public void setM_distance(double m_distance)
	{
		this.m_distance = m_distance;
	}
}
