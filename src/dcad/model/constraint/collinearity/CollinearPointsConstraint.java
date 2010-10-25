package dcad.model.constraint.collinearity;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.geometry.AnchorPoint;
import dcad.util.GMethods;
import dcad.util.GVariables;
import dcad.model.constraint.*;

public class CollinearPointsConstraint extends IndependentPointConstraints
{
	
	public CollinearPointsConstraint(AnchorPoint[] apArray, int category,boolean promoted)
	{
		super(apArray,category,promoted);
	}

	public String toString()
	{
		String str=addPrefix() + "Collinear points : ";
		int i=0;
		for(;i<points.size()-1;i++)
		{
			AnchorPoint m_ap = (AnchorPoint)points.elementAt(i);
			str = str + m_ap.getM_strId() + " , ";
		}
		str = str + ((AnchorPoint)points.get(i)).getM_strId();
		return str;
	}

	public Vector getEquation(Vector fixedPoints)
	{
		Vector retVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		for(int i=0; i<points.size() - 2; i++)
			retVec.addAll(constraintEquations.getCollinearPointsEQ(pointStrings[i+0],pointStrings[i+1],pointStrings[i+2]));
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
		for(int i=0; i<points.size() - 2; i++)
			retVec.addAll(constraintEquations.getCollinearPointsPD(pt, xory, anchorPoints[i+0],anchorPoints[i+1],anchorPoints[i+2],pointStrings[i+0],pointStrings[i+1],pointStrings[i+2]));
		return retVec;
	}

	public void draw(Graphics g)
	{
//		///System.out.println("CollinearPointConstraint.draw()");
		Graphics2D g2d = (Graphics2D)g;
		// set the color of the graphics to the color of the segment
		Color prevColor = g.getColor();
		g2d.setColor(getColor());

		boolean anythingHighlighted = false;
		for(int i=0;i<points.size();i++)
		{
			if( ((AnchorPoint)points.elementAt(i)).isHighlighted() )
			{
				anythingHighlighted = true;
				break;
			}
		}
		
		if(anythingHighlighted)
		{
			BasicStroke prevStroke = (BasicStroke)g2d.getStroke();
			g2d.setColor(getColor());
			g2d.setStroke(new BasicStroke(prevStroke.getLineWidth(), prevStroke.getEndCap(), prevStroke.getLineJoin(), prevStroke.getMiterLimit(), new float[]{4, 4, 8, 4}, prevStroke.getDashPhase()));

			for(int i=0; i<points.size() - 2; i++)
			{
				AnchorPoint m_ap1 = (AnchorPoint)points.elementAt(i+0);
				AnchorPoint m_ap2 = (AnchorPoint)points.elementAt(i+1);
				AnchorPoint m_ap3 = (AnchorPoint)points.elementAt(i+2);
				
				g2d.drawLine((int)m_ap3.getX(),(int) m_ap3.getY(),(int) m_ap1.getX(), (int)m_ap1.getY());
				g2d.drawLine((int)m_ap3.getX(), (int)m_ap3.getY(), (int)m_ap2.getX(), (int)m_ap2.getY());
			}
			
			g2d.setStroke(prevStroke);
		}
		// reset the graphics color back
		g2d.setColor(prevColor);
	}

	public boolean isConstraintSolved()
	{
		this.update();
		
		
		
		
		for(int i=0; i<points.size() - 2; i++)
			if(!(constraintsHelper.areSlopesEqual((AnchorPoint)points.elementAt(i+0),(AnchorPoint)points.elementAt(i+1),(AnchorPoint)points.elementAt(i+0),(AnchorPoint)points.elementAt(i+2),true)))
			{
				///System.out.println("Collinear points constraint failed...");
				return false;
			}
		return true;
	}
	
/*	public void delete()
	{
		for(int i=0;i<arrSegments.length;i++)
			arrSegments[i].removeConstraint(this);
	}*/
}