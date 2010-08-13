package dcad.model.constraint.pointOnSegment;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.PointSegmentConstraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegPoint;
import dcad.model.geometry.segment.Segment;
import dcad.util.GConstants;

public class pointOnPointConstraint extends PointSegmentConstraint
{
	public pointOnPointConstraint(Segment seg, AnchorPoint ap,int category,boolean promoted)
	{
		super(seg,ap,category,promoted);
		addPoint(((SegPoint)seg).getM_pt());
		addPoint(ap);
	}

	public boolean isConstraintSolved()
	{
		return constraintsHelper.pointsOverlap((AnchorPoint)points.get(0),(AnchorPoint)points.get(1));
	}
	
	public Vector getEquation(Vector fixedPoints)
	{
		String[][] pointStrings=constraintsHelper.getpointStrings(constraintsHelper.getAnchorPoints(points));
		Vector v=new Vector();
		v.add(constraintEquations.getOverlapEQ(pointStrings[0],pointStrings[1]));
		return v;
	}
		
	public Vector getPartialDifferentialString(String var, Vector fixedPoints)
	{
		StringTokenizer st = new StringTokenizer(var, ".");
		String pt = st.nextToken();
		String xory = st.nextToken();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(constraintsHelper.getAnchorPoints(points));
		Vector v = new Vector();
		v.add(constraintEquations.getOverlapPD(pt,xory,anchorPoints[0],anchorPoints[1],pointStrings[0],pointStrings[1]));
		return v; 
	}

}