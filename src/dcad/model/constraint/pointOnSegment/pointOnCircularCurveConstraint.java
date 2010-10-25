package dcad.model.constraint.pointOnSegment;

import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.PointSegmentConstraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.Segment;

public class pointOnCircularCurveConstraint extends PointSegmentConstraint
{
	public pointOnCircularCurveConstraint(Segment seg, AnchorPoint ap,int category,boolean promoted)
	{
		super(seg,ap,category,promoted);
		addPoint(((SegCircleCurve)seg).getM_center());
		addPoint(((SegCircleCurve)seg).getM_start());
		addPoint(((SegCircleCurve)seg).getM_end());
		addPoint(ap);
	}
	
	public boolean isConstraintSolved()
	{
		this.update();
		if(constraintsHelper.onCircularCurveOrNot((AnchorPoint)points.get(1),(AnchorPoint)points.get(2),(AnchorPoint)points.get(0),(AnchorPoint)points.get(3),true))
			return true;
		///System.out.println("point on curve constraint failed..");
		return false;
	}
	
	public Vector getEquation(Vector fixedPoints)
	{
		String[][] pointStrings=constraintsHelper.getpointStrings(constraintsHelper.getAnchorPoints(points));
		return constraintEquations.getPointOnCircularCurveEQ(pointStrings[0],pointStrings[1],pointStrings[2],pointStrings[3]);
	}
		
	public Vector getPartialDifferentialString(String var, Vector fixedPoints)
	{
		StringTokenizer st = new StringTokenizer(var, ".");
		String pt = st.nextToken();
		String xory = st.nextToken();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(constraintsHelper.getAnchorPoints(points));
		return constraintEquations.getPointOnCircularCurvePD(pt,xory,anchorPoints[0],anchorPoints[1],anchorPoints[2],anchorPoints[3],pointStrings[0],pointStrings[1],pointStrings[2],pointStrings[3]);
	}
	
	public String toString()
	{
		return addPrefix()+" Point " + m_ap.getM_label() + " on arc " + m_seg.getM_label();
	}

}