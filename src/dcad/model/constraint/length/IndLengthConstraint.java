package dcad.model.constraint.length;

import java.awt.Graphics;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.IndependentConstraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.SegLine;
import dcad.model.marker.MarkerLength;
import dcad.util.GConstants;
import dcad.util.GMethods;

public class IndLengthConstraint extends IndependentConstraint
{
	private double m_length = 0.0;
	private double lengthToShow = 0.0;
	private MarkerLength m_marker = null;

	public IndLengthConstraint(SegLine seg, double len, int category,boolean promoted)
	{
		super(seg,category,promoted);
		addPoint(((SegLine)m_seg).getM_start());
		addPoint(((SegLine)m_seg).getM_end());
		lengthToShow = len;
		if(GConstants.drawingRatio == -1)
			setDrawingRatio(seg.getM_length(),len);
		m_length=len * GConstants.drawingRatio;
	}

	public Vector getEquation(Vector fixedPoints)
	{
		Vector retVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		retVec.add(constraintEquations.getLineLengthEQ(pointStrings[0], pointStrings[1],m_length));
		return retVec;
	}

	public Vector getPartialDifferentialString(String var, Vector fixedPoints)
	{
		Vector retVec = new Vector();
		StringTokenizer st = new StringTokenizer(var, ".");
		String pt = st.nextToken();
		String xory = st.nextToken();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		retVec.add(constraintEquations.getLineLengthPD(pt, xory, anchorPoints[0],anchorPoints[1],pointStrings[0],pointStrings[1]));
		return retVec;
	}

	public void draw(Graphics g)
	{
		if(m_seg.isHighlighted())
		{
			AnchorPoint midPoint = ((SegLine)m_seg).getM_middle();
			g.drawString( String.valueOf(lengthToShow) + " L" , (int) midPoint.getX() + 10 , (int) midPoint.getY() + 10 );
		}
	}

	public MarkerLength getM_marker()
	{
		return m_marker;
	}

	public void setM_marker(MarkerLength marker)
	{
		this.m_marker = marker;
	}
	
	public String toString()
	{
		return addPrefix()+" Line length " + GMethods.formatNum(lengthToShow) + " : " + m_seg.getM_label();
	}
	
	public boolean isConstraintSolved()
	{
		this.update();
		SegLine l1=(SegLine)m_seg;
		if(constraintsHelper.independentLengthSatisfied(l1.getM_start(),l1.getM_end(),m_length,true))
			return true;
		System.out.println("Independent length constraint failed..");
		return false;
	}
	
}