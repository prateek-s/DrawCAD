package dcad.model.constraint.circleArc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.length.RelLengthConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.util.GMethods;

public class EqualRadiusConstraint extends RelLengthConstraint
{
	
	public EqualRadiusConstraint(SegCircleCurve seg1, SegCircleCurve seg2, int catagory,boolean promoted)
	{
		super(seg1, seg2,Constraint.HARD,promoted);
		addPoint(seg1.getM_start());
		addPoint(seg1.getM_end());
		addPoint(seg1.getM_center());
		addPoint(seg2.getM_start());
		addPoint(seg2.getM_end());
		addPoint(seg2.getM_center());
	}

	public String toString()
	{
		return addPrefix()+" Equal Radius : "+m_seg1.getM_label()+", "+m_seg2.getM_label();
	}

	public Vector getEquation(Vector fixedPoints)
	{
		Vector returnVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		returnVec.add(constraintEquations.getEqualRelativeLengthEQ(pointStrings[0],pointStrings[2],pointStrings[3],pointStrings[5]));
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
		retVec.add(constraintEquations.getEqualRelativeLengthPD(pt,xory,anchorPoints[0],anchorPoints[2],anchorPoints[3],anchorPoints[5],pointStrings[0],pointStrings[2],pointStrings[3],pointStrings[5]));
		return retVec;
	}
	

	public void draw(Graphics g)
	{
	}

	public void update()
	{
		super.update();
	}
	public boolean isConstraintSolved()
	{
		this.update();
		SegCircleCurve c1=(SegCircleCurve)m_seg1;
		SegCircleCurve c2=(SegCircleCurve)m_seg2;
		if(constraintsHelper.areLengthsEqual(c1.getM_end(),c1.getM_center(),c2.getM_end(),c2.getM_center(),true))
			return true;
		System.out.println("Equal radius constraint failed...");
		return false;
	}

}