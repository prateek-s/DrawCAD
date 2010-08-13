package dcad.model.constraint.circleArc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.IndependentConstraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.marker.MarkerCircleArcAngle;
import dcad.util.GConstants;
import dcad.util.GMethods;

public class circleArcAngleConstraint extends IndependentConstraint
{
	protected double m_angle = 0.0;
	private Arc2D m_arc = null; 
	public static final double SIZE = 14;
	public static final double ANGLUAR_GAP = SIZE/2;

	public circleArcAngleConstraint(SegCircleCurve seg, double diff, int category,boolean promoted)
	{
		super(seg,category,promoted);
		addPoint(seg.getM_start());
		addPoint(seg.getM_end());
		addPoint(seg.getM_center());
		setM_angle(diff);
		calcMarker();
	}
	
	public void setM_angle(double angle)
	{
		m_angle = angle;
		// set the angle within the range.
		if(m_angle > 360) m_angle = angle - (((int)(angle/360))*360);
		else if(m_angle < -360) m_angle = angle - (((int)(angle/360)+1)*360);
	}
	
	public String toString()
	{
		return addPrefix()+" Arc Angle " + GMethods.formatNum(m_angle) + " : " + m_seg.getM_label();
	}

	public Vector getEquation(Vector fixedPoints)
	{
		Vector retVec = new Vector();
		
		if(points.size()>0)
		{
			AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
			String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
			retVec.add(constraintEquations.getConstantAngleEQ(pointStrings[0],pointStrings[1],pointStrings[2],m_angle));
		}
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
		retVec.add(constraintEquations.getConstantAnglePD(pt, xory, anchorPoints[0],anchorPoints[1],anchorPoints[2],pointStrings[0],pointStrings[1],pointStrings[2]));
		return retVec;
	}

	public void draw(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g;
		// set the color of the graphics to the color of the segment
		Color prevColor = g.getColor();
		g2d.setColor(getColor());
		
		// for hard constraint the indicator should be drawn where marker was origially drawn by the user AFAP 
		if(m_arc != null) g2d.draw(m_arc);

		if(m_seg.isHighlighted())
		{
			Point2D center = ((SegCircleCurve)m_seg).getM_center().getM_point();
			g.drawString( String.valueOf(m_angle) + "' A" , (int) center.getX() -15 , (int) center.getY() - 15);
		}
		
		// reset the graphics color back
		g2d.setColor(prevColor);

	}
	
	protected void calcMarker()
	{
		if(points.size()>0)
		{
			AnchorPoint A=(AnchorPoint)points.get(0);
			AnchorPoint B=(AnchorPoint)points.get(1);
			AnchorPoint C=(AnchorPoint)points.get(2);
			m_arc = new Arc2D.Double((C.getX()-SIZE), (C.getY()-SIZE), (2*SIZE), (2*SIZE), 0, 0, Arc2D.OPEN);
			//m_arc.setAngles(A.getM_point(), B.getM_point());
			m_arc.setAngles(B.getM_point(),A.getM_point());
			if(m_arc.getAngleExtent() > 180) 
				m_arc.setAngleExtent(m_arc.getAngleExtent()-360);
			//if(m_arc.getAngleExtent() > 360) m_arc.setAngleExtent(m_arc.getAngleExtent()-360);
		}
	}
	
	public void update()
	{
		calcMarker();
		super.update();
	}
	
	public boolean isConstraintSolved()
	{
		update();
		double arcAngle=Math.abs( ((SegCircleCurve)m_seg).getM_arcAngle() );
		double diff1 = Math.abs( arcAngle - m_angle );
		double diff2 = 360 - arcAngle - m_angle;
		double diff = (diff1<diff2) ? diff1 : diff2;
		if(diff <= constraintsHelper.relativeAngleErrorThreshold)
			return true;
		constraintsHelper.printConstraintSolvingFailure("circleArcAngleConstraint.isConstraintSolved","Desired angle : " + this.m_angle + " Actual angle : " + arcAngle,true);
		return false;
	}

}