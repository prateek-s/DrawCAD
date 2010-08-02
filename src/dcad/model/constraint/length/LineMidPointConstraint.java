package dcad.model.constraint.length;

import java.awt.Graphics;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.PointSegmentConstraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.pointOnSegment.pointOnLineConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegLine;

public class LineMidPointConstraint extends pointOnLineConstraint//PointSegmentConstraint
{
	public LineMidPointConstraint(SegLine seg,AnchorPoint p,int category,boolean promoted)
	{
		super(seg,p,category,promoted);
/*		addPoint(((SegLine)seg).getM_start());
		addPoint(((SegLine)seg).getM_end());
		addPoint(p);*/
	}
	
	public void draw(Graphics g) {	;	}
	
	public Vector getEquation(Vector fixedPoints)
	{
		Vector returnVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		returnVec.add(constraintEquations.getMidPointXEQ(pointStrings[0],pointStrings[1],pointStrings[2]));
		returnVec.add(constraintEquations.getMidPointYEQ(pointStrings[0],pointStrings[1],pointStrings[2]));
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
 		retVec.add(constraintEquations.getMidPointXPD(pt,xory,anchorPoints[0],anchorPoints[1],anchorPoints[2],pointStrings[0],pointStrings[1],pointStrings[2]));
		retVec.add(constraintEquations.getMidPointYPD(pt,xory,anchorPoints[0],anchorPoints[1],anchorPoints[2],pointStrings[0],pointStrings[1],pointStrings[2]));
		return retVec;
	}
	
	public String toString()
	{
		return "Line mid point : Line - " + m_seg.getM_label() + " & Point - " + m_ap.getM_label();
	}
	
	public boolean isConstraintSolved()
	{
		update();
		AnchorPoint p1=(AnchorPoint)points.get(0);
		AnchorPoint p2=(AnchorPoint)points.get(1);
		AnchorPoint p3=(AnchorPoint)points.get(2);
		double diff1 = Math.abs( (p1.getX() + p2.getX())/2 - p3.getX());
		double diff2 = Math.abs( (p1.getY() + p2.getY())/2 - p3.getY());
		
		if(diff1 < constraintsHelper.pointOverlapErrorThreshold && diff2 <constraintsHelper.pointOverlapErrorThreshold)
			return true;
		constraintsHelper.printConstraintSolvingFailure("LineMidPointConstraint.isConstraintSolved" , "Differences : " + diff1 + "  " + diff2 + "\nStart, End and Center are as follows : ",true);
		p1.print();
		p2.print();
		p3.print();
		return false;
	}
}