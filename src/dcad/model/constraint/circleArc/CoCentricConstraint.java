package dcad.model.constraint.circleArc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.Prefs;
import dcad.model.constraint.RelativeConstraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.util.GMethods;

public class CoCentricConstraint extends RelativeConstraint
{
	private static final int CENTER_RADIUS_DIFF = 2;

	public CoCentricConstraint(SegCircleCurve seg1, SegCircleCurve seg2, int category,boolean promoted)
	{
		super(seg1, seg2,category,promoted);
		addPoint(seg1.getM_center());
		addPoint(seg2.getM_center());
	}

	public String toString()
	{
		return addPrefix()+" Concentric arcs : "+m_seg1.getM_label()+" , "+m_seg2.getM_label();
	}

	public Vector getEquation(Vector fixedPoints)
	{
		Vector retVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		//retVec.add(constraintEquations.getOverlapEQ(pointStrings[0],pointStrings[1]));
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
		//retVec.add(constraintEquations.getOverlapPD(pt, xory, anchorPoints[0],anchorPoints[1], pointStrings[0],pointStrings[1]));
		return retVec;
	}

	public void draw(Graphics g)
	{
		if(m_seg1.isEnabled() && m_seg2.isEnabled())
		{
			if((m_seg1.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL)&&(m_seg2.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL))
			{
				SegCircleCurve seg1 = (SegCircleCurve)m_seg1;
				SegCircleCurve seg2 = (SegCircleCurve)m_seg2;
				Graphics2D g2d = (Graphics2D)g;
				// set the color of the graphics to the color of the segment
				Color prevColor = g.getColor();
				g2d.setColor(getColor());
				int size = Prefs.getAnchorPtSize();
				
				// draw the solid circles
				g2d.fillArc((int)(seg1.getM_center().getX()-size+0.5), (int)(seg1.getM_center().getY()-size+0.5), size*2, size*2, 0, 360);
				g2d.fillArc((int)(seg2.getM_center().getX()-size+0.5), (int)(seg2.getM_center().getY()-size+0.5), size*2, size*2, 0, 360);

				// draw the hollow circles
				size += CENTER_RADIUS_DIFF;
				g2d.drawArc((int)(seg1.getM_center().getX()-size+0.5), (int)(seg1.getM_center().getY()-size+0.5), size*2, size*2, 0, 360);
				g2d.drawArc((int)(seg2.getM_center().getX()-size+0.5), (int)(seg2.getM_center().getY()-size+0.5), size*2, size*2, 0, 360);
				
				// reset the graphics color back
				g2d.setColor(prevColor);
			}
		}
	}
	
	public boolean isConstraintSolved()
	{
		return true;
		/*
		AnchorPoint p1=(AnchorPoint)points.get(0);
		AnchorPoint p2=(AnchorPoint)points.get(1);
		if(constraintsHelper.pointsOverlap(p1,p2))
			return true;
		constraintsHelper.printConstraintSolvingFailure("CocentricConstraint.isConstraintSolved","Two centers are : ("+p1.getX()+","+p1.getY()+") , ("+p2.getX()+","+p2.getY()+")",true);
		return false;
		*/
	}

}