package dcad.model.constraint.angle;

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
import dcad.util.GMethods;

public class ParallelSegConstraint extends RelAngleConstraint
{
	private Line2D line1Mark1 = new Line2D.Double(-1,-1,-1,-1);
	private Line2D line1Mark2 = new Line2D.Double(-1,-1,-1,-1);
	private Line2D line2Mark1 = new Line2D.Double(-1,-1,-1,-1);
	private Line2D line2Mark2 = new Line2D.Double(-1,-1,-1,-1);
	
	public ParallelSegConstraint(SegLine seg1, SegLine seg2,int category,boolean promoted)
	{
		super(seg1, seg2,category,promoted);
		addPoint(seg1.getM_start());
		addPoint(seg1.getM_end());
		addPoint(seg2.getM_start());
		addPoint(seg2.getM_end());
		calcMarker();
		setM_angleDiff(0);
	}

	public String toString()
	{
		return addPrefix()+" Parellel lines : "+m_seg1.getM_label()+" , "+m_seg2.getM_label();
	}

	public Vector getEquation(Vector fixedPoints)
	{
		Vector retVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		retVec.add(constraintEquations.getParallelSegmentConstraintEQ(pointStrings[0],pointStrings[1],pointStrings[2],pointStrings[3]));
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
		retVec.add(constraintEquations.getParallelSegmentConstraintPD(pt,xory,anchorPoints[0],anchorPoints[1],anchorPoints[2],anchorPoints[3],pointStrings[0],pointStrings[1],pointStrings[2],pointStrings[3]));
		return retVec;
	}

	public void draw(Graphics g)
	{
		// draw only if both the segments are enabled and both are instances of line.
		if(m_seg1.isEnabled() && m_seg2.isEnabled())
		{
			if((m_seg1.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL)&&(m_seg2.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL))
			{
				Graphics2D g2d = (Graphics2D)g;
				// 	set the color of the graphics to the color of the segment
				Color prevColor = g.getColor();
				g2d.setColor(getColor());

				if((line1Mark1 != null) && (line1Mark2 != null) && (line2Mark1 != null) && (line2Mark2 != null))
				{
					g2d.draw(line1Mark1);
					g2d.draw(line1Mark2);
					g2d.draw(line2Mark1);
					g2d.draw(line2Mark2);
					
					// write the marker number
//					Font f = g.getFont();
//					g.setFont(new Font(f.getName(), DEF_FONT_TYPE, DEF_FONT_SIZE));
//					g.drawString(Integer.toString(getM_markerCount()), (int)line1Mark1.getX2()+2, (int)line1Mark2.getY2());
//					g.drawString(Integer.toString(getM_markerCount()), (int)line2Mark1.getX2()+2, (int)line2Mark2.getY2());
//					g.setFont(f);
				}
				// 	reset the graphics color back
				g2d.setColor(prevColor);
			}
		}
	}

	protected void calcMarker()
	{
		SegLine seg1=(SegLine)m_seg1;
		SegLine seg2=(SegLine)m_seg2;
		// calculate the display markers
		Line2D shapeLine = (Line2D)seg1.getM_shape();
		// interpolate the line to find the new start point
		Point2D newStartPt = new Point2D.Double(RATIO*shapeLine.getX2()+(1-RATIO)*shapeLine.getX1(), RATIO*shapeLine.getY2()+(1-RATIO)*shapeLine.getY1());

		// this is now the new smaller line
		Line2D line = new Line2D.Double(newStartPt, shapeLine.getP2());
		double halfDistance = line.getP1().distance(line.getP2())/2;
		double rad = Math.sqrt(halfDistance*halfDistance + ANGLUAR_GAP*ANGLUAR_GAP);
		//System.out.println(halfDistance*2+" asdasdas: "+rad);
		Vector centers = GMethods.CircleCircleIntersections(line.getX1(), line.getY1(), rad, line.getX2(), line.getY2(), rad);
		Point2D lineMP = new Point2D.Double((line.getX1()+line.getX2())/2, (line.getY1()+line.getY2())/2);
		// interpolate the line to find the newMP
		Point2D newMP = new Point2D.Double((lineMP.getX()*(halfDistance-ANGLUAR_GAP)+line.getX2()*ANGLUAR_GAP)/halfDistance, (lineMP.getY()*(halfDistance-ANGLUAR_GAP)+line.getY2()*ANGLUAR_GAP)/halfDistance);
		if((line1Mark1 != null) && (line1Mark2 != null))
		{
			line1Mark1.setLine((Point2D)centers.get(0), newMP); 
			line1Mark2.setLine(newMP, (Point2D)centers.get(1)); 
		}
		
		// calculate the display markers
		shapeLine = (Line2D)seg2.getM_shape();
		// interpolate the line to find the new start point
		newStartPt = new Point2D.Double(RATIO*shapeLine.getX2()+(1-RATIO)*shapeLine.getX1(), RATIO*shapeLine.getY2()+(1-RATIO)*shapeLine.getY1());

		// this is now the new smaller line
		line = new Line2D.Double(newStartPt, shapeLine.getP2());
		halfDistance = line.getP1().distance(line.getP2())/2;
		rad = Math.sqrt(halfDistance*halfDistance + ANGLUAR_GAP*ANGLUAR_GAP);
		//System.out.println(halfDistance*2+" asdasdas: "+rad);
		centers = GMethods.CircleCircleIntersections(line.getX1(), line.getY1(), rad, line.getX2(), line.getY2(), rad);
		lineMP = new Point2D.Double((line.getX1()+line.getX2())/2, (line.getY1()+line.getY2())/2);
		// interpolate the line to find the newMP
		newMP = new Point2D.Double((lineMP.getX()*(halfDistance-ANGLUAR_GAP)+line.getX2()*ANGLUAR_GAP)/halfDistance, (lineMP.getY()*(halfDistance-ANGLUAR_GAP)+line.getY2()*ANGLUAR_GAP)/halfDistance);
		if((line2Mark1 != null) && (line2Mark2 != null))
		{
			line2Mark1.setLine((Point2D)centers.get(0), newMP); 
			line2Mark2.setLine(newMP, (Point2D)centers.get(1));
		}
	}

	public void update()
	{
		calcMarker();
		super.update();
	}

	public boolean isConstraintSolved()
	{
		this.update();
		SegLine l1=(SegLine)m_seg1;
		SegLine l2=(SegLine)m_seg2;
		if(constraintsHelper.areSlopesEqual(l1.getM_start(),l1.getM_end(),l2.getM_start(),l2.getM_end(),true))
			return true;
		System.out.println("Parallel Segment Constraint failed...");
		return false;
	}
}