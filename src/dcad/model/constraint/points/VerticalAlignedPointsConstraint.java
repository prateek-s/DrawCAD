package dcad.model.constraint.points;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.IndependentPointConstraints;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.Segment;
import dcad.util.GMethods;

public class VerticalAlignedPointsConstraint extends IndependentPointConstraints
{
	public VerticalAlignedPointsConstraint(AnchorPoint ap1, AnchorPoint ap2,int category,boolean promoted)
	{
		super(new AnchorPoint[]{ap1,ap2},category,promoted);
	}
	
	public String toString()
	{
		AnchorPoint m_ap1 = (AnchorPoint)points.elementAt(0);
		AnchorPoint m_ap2 = (AnchorPoint)points.elementAt(1);
		return addPrefix()+"Vertically aligned Points : " + m_ap1.getM_strId() + " and " + m_ap2.getM_strId();
	}
	
	public Vector getEquation(Vector fixedPoints)
	{
		Vector retVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		//retVec.add(constraintEquations.getVerticalLineSegmentEQ(pointStrings[0],pointStrings[1]));
		retVec.add( constraintsHelper.verticalConstraintScaleFactor + "*"+constraintEquations.getXDifferenceEQ(pointStrings[0],pointStrings[1]) );
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
		//retVec.add(constraintEquations.getVerticalLineSegmentPD(pt, xory, anchorPoints[0],anchorPoints[1],pointStrings[0],pointStrings[1]));
		retVec.add( constraintsHelper.verticalConstraintScaleFactor + "*"+constraintEquations.getXDifferencePD(pt,xory,anchorPoints[0],anchorPoints[1],pointStrings[0],pointStrings[1]) );
		return retVec;
	}

	public boolean isConstraintSolved()
	{
		this.update();
		
		if(Math.abs(  ((AnchorPoint)points.get(0)).getX() -  ((AnchorPoint)points.get(1)).getX()  )<constraintsHelper.pointOverlapErrorThreshold)
			return true;
		constraintsHelper.printConstraintSolvingFailure("VerticalAlignedPointsConstraint.isConstraintSolved","Not printing the X values",true);

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
		}
		g2d.setColor(prevColor);
	}


	

}
