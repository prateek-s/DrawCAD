package dcad.model.constraint.collinearity;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.RelativeConstraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.SegLine;

public class CollinearLinesConstraint extends RelativeConstraint
{
	public CollinearLinesConstraint(SegLine seg1, SegLine seg2, int category,boolean promoted)
	{
		super(seg1, seg2,category,promoted);
		addPoint(seg1.getM_start());
		addPoint(seg1.getM_end());
		addPoint(seg2.getM_start());
		addPoint(seg2.getM_end());
	}

	public String toString()
	{
		return addPrefix()+" Collinear lines : "+m_seg1.getM_label()+" , "+m_seg2.getM_label();
	}

	public Vector getEquation(Vector fixedPoints)
	{
		Vector retVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		retVec.add(constraintEquations.getParallelSegmentConstraintEQ(pointStrings[0],pointStrings[2],pointStrings[1],pointStrings[3]));
		retVec.add(constraintEquations.getParallelSegmentConstraintEQ(pointStrings[1],pointStrings[2],pointStrings[0],pointStrings[3]));
//		retVec.add(constraintEquations.getCollinearPointsEQ(pointStrings[2],pointStrings[0],pointStrings[1]));
//		retVec.add(constraintEquations.getCollinearPointsEQ(pointStrings[3],pointStrings[0],pointStrings[1]));
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
		retVec.add(constraintEquations.getParallelSegmentConstraintPD(pt,xory,anchorPoints[0],anchorPoints[2],anchorPoints[1],anchorPoints[3],pointStrings[0],pointStrings[2],pointStrings[1],pointStrings[3]));
		retVec.add(constraintEquations.getParallelSegmentConstraintPD(pt,xory,anchorPoints[1],anchorPoints[2],anchorPoints[0],anchorPoints[3],pointStrings[1],pointStrings[2],pointStrings[0],pointStrings[3]));
//		retVec.add(constraintEquations.getCollinearPointsPD(pt, xory, anchorPoints[2],anchorPoints[0],anchorPoints[1],pointStrings[2],pointStrings[0],pointStrings[1]));
//		retVec.add(constraintEquations.getCollinearPointsPD(pt, xory, anchorPoints[3],anchorPoints[0],anchorPoints[1],pointStrings[3],pointStrings[0],pointStrings[1]));
		return retVec;
	}

	public void draw(Graphics g)
	{
		if(m_seg1.isHighlighted() || m_seg2.isHighlighted())
		{
			AnchorPoint ap1 = (AnchorPoint)points.get(0);
			AnchorPoint ap2 = (AnchorPoint)points.get(3);
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
			}
			g2d.setColor(prevColor);
		}
	}

	public boolean isConstraintSolved()
	{
		this.update();
		if (
				constraintsHelper.areSlopesEqual((AnchorPoint)points.elementAt(0),(AnchorPoint)points.elementAt(2),(AnchorPoint)points.elementAt(1),(AnchorPoint)points.elementAt(3),true) 
				&& constraintsHelper.areSlopesEqual((AnchorPoint)points.elementAt(1),(AnchorPoint)points.elementAt(2),(AnchorPoint)points.elementAt(0),(AnchorPoint)points.elementAt(3),true)
				)
			return true;
		///System.out.println("COllinear lines constraint failed...");
		return false;
	}
}