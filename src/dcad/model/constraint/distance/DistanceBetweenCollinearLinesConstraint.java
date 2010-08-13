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

public class DistanceBetweenCollinearLinesConstraint  extends RelLengthConstraint
{
	private double m_distance=0;
	
	public DistanceBetweenCollinearLinesConstraint(SegLine seg1, SegLine seg2,double distance, int catagory,boolean promoted)
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
		AnchorPoint nearLine1;
		AnchorPoint nearLine2;
		
		if(start1.distance(start2) < end1.distance(start2))
			nearLine1 = start1;
		else
			nearLine1 = end1;
		
		if(nearLine1.distance(start2) < nearLine1.distance(end2))
			nearLine2 = start2;
		else
			nearLine2 = end2;

		addPoint(nearLine1);
		addPoint(nearLine2);
	}
	
	public String toString()
	{
		return addPrefix()+" Distance Between "+m_seg1.getM_label()+" , "+m_seg2.getM_label()+" : " + m_distance;
	}

	public Vector getEquation(Vector fixedPoints)
	{
		Vector returnVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		returnVec.add(constraintEquations.getLineLengthEQ(pointStrings[0],pointStrings[1],m_distance));
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
		retVec.add(constraintEquations.getLineLengthPD(pt,xory,anchorPoints[0],anchorPoints[1],pointStrings[0],pointStrings[1]));
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
		if(constraintsHelper.independentLengthSatisfied(anchorPoints[0],anchorPoints[1],m_distance,true))
			return true;
		System.out.println("Distance between collinear lines constraint failed... ");
		return false;
	}

	public void setM_distance(double m_distance)
	{
		this.m_distance = m_distance;
	}
}