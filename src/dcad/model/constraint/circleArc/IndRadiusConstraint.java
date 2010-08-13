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
import dcad.model.geometry.segment.SegLine;
import dcad.model.marker.MarkerRadius;
import dcad.util.GConstants;

public class IndRadiusConstraint extends IndependentConstraint
{
	private double m_radius = 0.0;
	private double radiusToShow = 0.0;
	
	public IndRadiusConstraint(SegCircleCurve seg, double rad, int category,boolean promoted)
	{
		super(seg,category,promoted);
		addPoint(((SegCircleCurve)seg).getM_start());
		addPoint(((SegCircleCurve)seg).getM_end());
		addPoint(((SegCircleCurve)seg).getM_center());
		radiusToShow = rad;
		if(GConstants.drawingRatio == -1)
			setDrawingRatio(seg.getM_radius(),rad);
		m_radius = rad * GConstants.drawingRatio;
	}

	public void draw(Graphics g)
	{
		if(m_seg.isHighlighted())
		{
			AnchorPoint center = ((SegCircleCurve)m_seg).getM_center();
			g.drawString( radiusToShow + " R"  , (int) center.getX() + 10 , (int) center.getY() + 10 );
		}
	}

	public Vector getEquation(Vector fixedPoints)
	{
		Vector returnVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		returnVec.add(constraintEquations.getLineLengthEQ(pointStrings[0], pointStrings[2],m_radius));
		returnVec.add(constraintEquations.getLineLengthEQ(pointStrings[1], pointStrings[2],m_radius));
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
		retVec.add(constraintEquations.getLineLengthPD(pt,xory,anchorPoints[0],anchorPoints[2],pointStrings[0], pointStrings[2]));
		retVec.add(constraintEquations.getLineLengthPD(pt,xory,anchorPoints[1],anchorPoints[2],pointStrings[1], pointStrings[2]));
		return retVec;
	}
	
	public String toString()
	{
		return addPrefix()+" Arc Radius " + radiusToShow + " : " + m_seg.getM_label();
	}

	public boolean isConstraintSolved()
	{
		update();
		if( 
				constraintsHelper.independentLengthSatisfied((AnchorPoint)points.get(0),(AnchorPoint)points.get(2),m_radius,true)
				&& 	constraintsHelper.independentLengthSatisfied((AnchorPoint)points.get(1),(AnchorPoint)points.get(2),m_radius,true)
				)
			return true;
		System.out.println("Indipendent radius constraint failed...");
		return false;
	}
}