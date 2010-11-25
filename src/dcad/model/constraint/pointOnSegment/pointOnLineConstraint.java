package dcad.model.constraint.pointOnSegment;

import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.PointSegmentConstraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;

public class pointOnLineConstraint extends PointSegmentConstraint
{
	public pointOnLineConstraint(Segment seg, AnchorPoint ap,int category,boolean promoted)
	{
		super(seg,ap,category,promoted);
		addPoint(((SegLine)seg).getM_start());
		addPoint(((SegLine)seg).getM_end());
		addPoint(ap);
	}

	public boolean isConstraintSolved()
	{
		if(constraintsHelper.onLineOrNot((AnchorPoint)points.get(2),(AnchorPoint)points.get(0),(AnchorPoint)points.get(1),true))
			return true;
		///System.out.println("Point on line constraint failed...");
		return false;
	}
	
	public String toString()
	{
		return addPrefix()+" Point " + m_ap.getM_label() + " on line " + m_seg.getM_label();
	}

	public Vector getEquation(Vector fixedPoints)
	{
		String[][] pointStrings=constraintsHelper.getpointStrings(constraintsHelper.getAnchorPoints(points));
		return constraintEquations.getPointOnLineEQ(pointStrings[2],pointStrings[0],pointStrings[1]);
	}
		
	public Vector getPartialDifferentialString(String var, Vector fixedPoints)
	{
		StringTokenizer st = new StringTokenizer(var, ".");
		String pt = st.nextToken();
		String xory = st.nextToken();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(constraintsHelper.getAnchorPoints(points));
		return constraintEquations.getPointOnLinePD(pt,xory,anchorPoints[2],anchorPoints[0],anchorPoints[1],pointStrings[2],pointStrings[0],pointStrings[1]);
	}

}