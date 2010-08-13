package dcad.model.constraint.angle;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.IndependentConstraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegLine;
import dcad.util.GMethods;
import dcad.util.Maths;

public class IndAngleConstraint extends IndependentConstraint
{
	protected static final int MARKER_ANGLE_SIDE_LENGTH = 10;
	protected static final int MARKER_LINE_LENGTH = 33;
	protected double m_angle = 0.0;
	protected Rectangle marker_rect = new Rectangle(-1, -1, -1, -1);
	protected Line2D marker_line = new Line2D.Double(-1, -1, -1, -1);
	
	public IndAngleConstraint(SegLine seg, double diff, int category, boolean promoted)
	{
		super(seg,category,promoted);
		addPoint(seg.getM_start());
		addPoint(seg.getM_end());
		setM_angle(diff);
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
		return addPrefix()+"Line angle " + GMethods.formatNum(m_angle) + " : " + m_seg.getM_label();
	}

	public Vector getEquation(Vector fixedPoints)
	{
		Vector retVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		retVec.add(constraintEquations.getIndependentAngleEQ(pointStrings[0],pointStrings[1],m_angle));
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
		retVec.add(constraintEquations.getIndependentAnglePD(pt,xory,anchorPoints[0],anchorPoints[1],pointStrings[0],pointStrings[1],m_angle));
		return retVec;
	}

	public void draw(Graphics g)
	{
		if(m_seg.isHighlighted())
		{
			AnchorPoint midPoint = ((SegLine)m_seg).getM_middle();
			g.drawString( String.valueOf(m_angle) + "' A" , (int) midPoint.getX() - 10 , (int) midPoint.getY() - 10 );
		}
	}

	public boolean isConstraintSolved()
	{
		this.update();
		
		SegLine l=(SegLine)m_seg;
		AnchorPoint ap1 = l.getM_start();
		AnchorPoint ap2 = l.getM_end();
		Point2D p = new Point2D.Double(ap1.getX(),ap1.getY());
//		Point2D p = new Point2D.Double(ap1.getX(),-ap1.getY());
		
		double desiredAngle = m_angle;
		if(desiredAngle > 90)
			desiredAngle = 180 - desiredAngle;
		
		double lineAngle = Maths.newAngleInDegrees(p,ap2.getM_point());
		if(lineAngle < 0)
			lineAngle *= (-1);
		if (Math.abs(lineAngle-desiredAngle)<=constraintsHelper.independentAngleErrorThreshold)
			return true;
		constraintsHelper.printConstraintSolvingFailure("IndAngleConstraint.isConstraintSolved"," Line angle is : " + lineAngle + "  Desired algne is : "+desiredAngle,true);
		return false;
	}
	
	public void update()
	{
//		calcMarkers();
	}

}

/*
 
	private void calcMarkers()
	{
		if((m_type == IND_ANGLE_0) || (m_type == IND_ANGLE_180))
		{
			// draw the axis line
			marker_line.setLine((int)m_seg.getSegStart().getX(),(int) m_seg.getSegStart().getY()-MARKER_LINE_LENGTH,(int) m_seg.getSegStart().getX(), (int)m_seg.getSegStart().getY()+MARKER_LINE_LENGTH);
			
			// draw the 90 deg angle
			if(Math.abs(m_seg.getSegStart().getX() - m_seg.getSegEnd().getX()) > MARKER_ANGLE_SIDE_LENGTH)
			{
				if(m_seg.getSegStart().getX() < m_seg.getSegEnd().getX())
				{
					marker_rect.setBounds((int)(m_seg.getSegStart().getX()+0.5), (int)(m_seg.getSegStart().getY()+0.5)-MARKER_ANGLE_SIDE_LENGTH, MARKER_ANGLE_SIDE_LENGTH, MARKER_ANGLE_SIDE_LENGTH);
				}
				else
				{
					marker_rect.setBounds((int)(m_seg.getSegStart().getX()+0.5)-MARKER_ANGLE_SIDE_LENGTH, (int)(m_seg.getSegStart().getY()+0.5)-MARKER_ANGLE_SIDE_LENGTH, MARKER_ANGLE_SIDE_LENGTH, MARKER_ANGLE_SIDE_LENGTH);
				}
			}
		}
		else if((m_type == IND_ANGLE_90) || (m_type == IND_ANGLE_270))
		{
			// draw the axis line
			marker_line.setLine((int)m_seg.getSegStart().getX()-MARKER_LINE_LENGTH,(int) m_seg.getSegStart().getY(),(int) m_seg.getSegStart().getX()+MARKER_LINE_LENGTH, (int)m_seg.getSegStart().getY());
			
			// draw the 90 deg angle
			if(Math.abs(m_seg.getSegStart().getY() - m_seg.getSegEnd().getY()) > MARKER_ANGLE_SIDE_LENGTH)
			{
				if(m_seg.getSegStart().getY() < m_seg.getSegEnd().getY())
				{
					marker_rect.setBounds((int)(m_seg.getSegStart().getX()+0.5), (int)(m_seg.getSegStart().getY()+0.5), MARKER_ANGLE_SIDE_LENGTH, MARKER_ANGLE_SIDE_LENGTH);
				}
				else
				{
					marker_rect.setBounds((int)(m_seg.getSegStart().getX()+0.5), (int)(m_seg.getSegStart().getY()+0.5)-MARKER_ANGLE_SIDE_LENGTH, MARKER_ANGLE_SIDE_LENGTH, MARKER_ANGLE_SIDE_LENGTH);
				}
			}
		}
	}









if((m_type == Constraint.IND_ANGLE_0) || (m_type == Constraint.IND_ANGLE_180))
{
	// y coordinates of both end points of the line are same
	str = "("+point1[1]+" - "+point2[1]+")"; 
}
else if((m_type == Constraint.IND_ANGLE_90) || (m_type == Constraint.IND_ANGLE_270))
{
	// x coordinates of both end points of the line are same
	str = "("+point1[0]+" - "+point2[0]+")"; 
}
else






		if((m_type == Constraint.IND_ANGLE_0) || (m_type == Constraint.IND_ANGLE_180))
		{
			if(pt.equals(p1.getM_strId()) && xory.equalsIgnoreCase("y"))
			{
				str = "1";
			}
			else if(pt.equals(p2.getM_strId()) && xory.equalsIgnoreCase("y"))
			{
				str = "-1";
			}
		}
		else if((m_type == Constraint.IND_ANGLE_90) || (m_type == Constraint.IND_ANGLE_270))
		{
			if(pt.equals(p1.getM_strId()) && xory.equalsIgnoreCase("x"))
			{
				str = "1";
			}
			else if(pt.equals(p2.getM_strId()) && xory.equalsIgnoreCase("x"))
			{
				str = "-1";
			}
		}
		else

*/