package dcad.model.constraint.circleArc;

import java.awt.Graphics;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.IndependentConstraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.util.GConstants;

public class circularArcConstraint extends IndependentConstraint
{
	public circularArcConstraint(SegCircleCurve seg,int category,boolean promoted)
	{
		super(seg,category,promoted);
		addPoint(((SegCircleCurve)seg).getM_start());
		addPoint(((SegCircleCurve)seg).getM_end());
		addPoint(((SegCircleCurve)seg).getM_center());
	}
	
	public void draw(Graphics g)
	{
		;
	}

	public Vector getEquation(Vector fixedPoints)
	{
		Vector returnVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		String str1 = constraintEquations.getEqualRelativeLengthEQ(pointStrings[0],pointStrings[2],pointStrings[1],pointStrings[2]); 
//		String str2 = constraintEquations.getEqualRelativeLengthEQ(pointStrings[1],pointStrings[2],pointStrings[0],pointStrings[2]);;
		returnVec.add(str1);
		//returnVec.add("("+str1+")-("+str2+")");
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
		String str1=constraintEquations.getEqualRelativeLengthPD(pt,xory,anchorPoints[0],anchorPoints[2],anchorPoints[1],anchorPoints[2],pointStrings[0],pointStrings[2],pointStrings[1],pointStrings[2]);
//		String str2=constraintEquations.getEqualRelativeLengthPD(pt,xory,anchorPoints[1],anchorPoints[2],anchorPoints[0],anchorPoints[2],pointStrings[1],pointStrings[2],pointStrings[0],pointStrings[2]);
		retVec.add(str1);
		//retVec.add("("+str1+")-("+str2+")");
		return retVec;
	}
	
	public String toString()
	{
		return addPrefix()+" Circular Arc Constraint on "+m_seg.getM_label();
	}
	
	public boolean isConstraintSolved()
	{
		update();
		if(constraintsHelper.areLengthsEqual((AnchorPoint)points.get(0),(AnchorPoint)points.get(2),(AnchorPoint)points.get(1),(AnchorPoint)points.get(2),true))
			return true;
		System.out.println("Circular arc constraint failed...");
		return false;
	}

}