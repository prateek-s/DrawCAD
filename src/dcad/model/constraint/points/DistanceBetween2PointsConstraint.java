package dcad.model.constraint.points;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.IndependentPointConstraints;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.Segment;
import dcad.util.GConstants;
import dcad.util.GMethods;


public class DistanceBetween2PointsConstraint extends IndependentPointConstraints
{
	private double m_distance=0.0;
	private double distanceToShow = 0;

	public DistanceBetween2PointsConstraint(AnchorPoint ap1, AnchorPoint ap2,double argDistance,int category,boolean promoted)
	{
		super(new AnchorPoint[]{ap1,ap2},category,promoted);
		m_distance = argDistance;
		distanceToShow = m_distance;
		if(GConstants.drawingRatio == -1)
			setDrawingRatio(ap1.distance(ap2),m_distance);
		m_distance=m_distance * GConstants.drawingRatio;
	}
	
	public String toString()
	{
		AnchorPoint m_ap1 = (AnchorPoint)points.elementAt(0);
		AnchorPoint m_ap2 = (AnchorPoint)points.elementAt(1);
		return addPrefix()+" Distance between "+m_ap1.getM_strId() + " and " + m_ap2.getM_strId() + " : " + distanceToShow;
	}
	
	public Vector getEquation(Vector fixedPoints)
	{
		Vector retVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		retVec.add(constraintEquations.getLineLengthEQ(pointStrings[0],pointStrings[1],m_distance));
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

	public boolean isConstraintSolved()
	{
		this.update();
		if(constraintsHelper.independentLengthSatisfied((AnchorPoint)points.elementAt(0),(AnchorPoint)points.elementAt(1),m_distance,true))
			return true;
		System.out.println("Distance between points constraint failed...");
		return false;
	}
	
	public void draw(Graphics g)
	{
		AnchorPoint ap1 = (AnchorPoint)points.get(0);
		AnchorPoint ap2 = (AnchorPoint)points.get(1);
		Graphics2D g2d = (Graphics2D)g;
		// 	set the color of the graphics to the color of the segment
		Color prevColor = g.getColor();
		g2d.setColor(getColor());
		if(ap1.isHighlighted() || ap2.isHighlighted())
		{
			BasicStroke prevStroke = (BasicStroke)g2d.getStroke();
			g2d.setStroke(new BasicStroke(prevStroke.getLineWidth(), prevStroke.getEndCap(), prevStroke.getLineJoin(), prevStroke.getMiterLimit(), new float[]{4, 4, 8, 4}, prevStroke.getDashPhase()));
			Line2D l = new Line2D.Double(ap1.getM_point(),ap2.getM_point());
			g2d.draw(l);
			g2d.setStroke(prevStroke);
			
			Point2D midPoint = new Point2D.Double( ( ap1.getX() + ap2.getX() ) / 2  ,  ( ap1.getY() + ap2.getY() ) / 2 );
			g2d.drawString( String.valueOf(distanceToShow) , (int) midPoint.getX() - 10 , (int) midPoint.getY() - 10 );

		}
		g2d.setColor(prevColor);
	}

}