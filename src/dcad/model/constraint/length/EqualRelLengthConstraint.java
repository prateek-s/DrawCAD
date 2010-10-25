package dcad.model.constraint.length;

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
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.SegLine;
import dcad.model.marker.MarkerEquality;
import dcad.util.GConstants;
import dcad.util.GMethods;

public class EqualRelLengthConstraint extends RelLengthConstraint
{
	public static final double SIZE = 20;
	public static final double GAP = 5;
	private Line2D lineMark1 = new Line2D.Double(-1,-1,-1,-1);
	private Line2D lineMark2 = new Line2D.Double(-1,-1,-1,-1);
	
	public EqualRelLengthConstraint(SegLine seg1, SegLine seg2, int catagory,boolean promoted)
	{
		super(seg1, seg2,Constraint.HARD,promoted);
		calcMarkers(seg1, seg2);
		addPoint(seg1.getM_start());
		addPoint(seg1.getM_end());
		addPoint(seg2.getM_start());
		addPoint(seg2.getM_end());
	}

	public String toString()
	{
		return addPrefix()+" Equal line lengths : "+m_seg1.getM_label()+" , "+m_seg2.getM_label();
	}


	public Vector getEquation(Vector fixedPoints)
	{
		Vector returnVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		returnVec.add(constraintEquations.getEqualRelativeLengthEQ(pointStrings[0],pointStrings[1],pointStrings[2],pointStrings[3]));
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
		retVec.add(constraintEquations.getEqualRelativeLengthPD(pt,xory,anchorPoints[0],anchorPoints[1],anchorPoints[2],anchorPoints[3],pointStrings[0],pointStrings[1],pointStrings[2],pointStrings[3]));
		return retVec;
	}
	

	public void draw(Graphics g)
	{
		if((m_seg1.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL) && (m_seg2.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL))
		{
			Graphics2D g2d = (Graphics2D)g;
			// set the color of the graphics to the color of the segment
			Color prevColor = g.getColor();
			g2d.setColor(getColor());
			
			g2d.draw(lineMark1);
			g2d.draw(lineMark2);

			// write the marker number
//			Font f = g.getFont();
//			g.setFont(new Font(f.getName(), DEF_FONT_TYPE, DEF_FONT_SIZE));
//			g.drawString(Integer.toString(getM_markerCount()), (int)lineMark1.getX2()+2, (int)lineMark1.getY2());
//			g.drawString(Integer.toString(getM_markerCount()), (int)lineMark2.getX2()+2, (int)lineMark2.getY2());
//			g.setFont(f);
			
			// reset the graphics color back
			g2d.setColor(prevColor);
		}
	}


	protected void calcMarkers(SegLine seg1, SegLine seg2)
	{
		// calculate the display markers
		Line2D line = (Line2D)seg1.getM_shape();
		double distance = line.getP1().distance(line.getP2());
		double rad = Math.sqrt((distance/2)*(distance/2) + (SIZE/2)*(SIZE/2));
		Vector centers = GMethods.CircleCircleIntersections(line.getX1(), line.getY1(), rad, line.getX2(), line.getY2(), rad);
		if(lineMark1 != null) lineMark1.setLine((Point2D)centers.get(0), (Point2D)centers.get(1)); 

		// calculate the display markers
		line = (Line2D)seg2.getM_shape();
		distance = line.getP1().distance(line.getP2());
		rad = Math.sqrt((distance/2)*(distance/2) + (SIZE/2)*(SIZE/2));
		centers = GMethods.CircleCircleIntersections(line.getX1(), line.getY1(), rad, line.getX2(), line.getY2(), rad);
		if(lineMark1 != null) lineMark2.setLine((Point2D)centers.get(0), (Point2D)centers.get(1)); 
	}

	public void update()
	{
		calcMarkers((SegLine)m_seg1, (SegLine)m_seg2);
		super.update();
	}
	public boolean isConstraintSolved()
	{
		this.update();
		SegLine l1=(SegLine)m_seg1;
		SegLine l2=(SegLine)m_seg2;
		if(constraintsHelper.areLengthsEqual(l1.getM_start(),l1.getM_end(),l2.getM_start(),l2.getM_end(),true))
			return true;
		///System.out.println("Equal relative length constraint failed...");
		return false;
	}
	
}

