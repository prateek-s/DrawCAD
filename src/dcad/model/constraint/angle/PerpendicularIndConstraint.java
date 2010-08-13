package dcad.model.constraint.angle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.SegLine;
import dcad.util.GMethods;
import dcad.util.GVariables;

public class PerpendicularIndConstraint extends IndAngleConstraint
{
	
	public PerpendicularIndConstraint(SegLine seg, double diff, int category,boolean promoted)
	{
		super(seg,diff,category,promoted);
		calcMarkers();
	}

	public String toString()
	{
		return addPrefix()+" Vertical line : "+m_seg.getM_label();
	}

	public Vector getEquation(Vector fixedPoints)
	{
		Vector retVec = new Vector();
		String str = "";
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		//str=constraintEquations.getVerticalLineSegmentEQ(pointStrings[0],pointStrings[1]);
		str = constraintsHelper.verticalConstraintScaleFactor + "*"+constraintEquations.getXDifferenceEQ(pointStrings[0],pointStrings[1]);
		retVec.add(str);
		return retVec;
	}

	public Vector getPartialDifferentialString(String var, Vector fixedPoints)
	{
		Vector retVec = new Vector();
		String str = "0.0";
		StringTokenizer st = new StringTokenizer(var, ".");
		String pt = st.nextToken();
		String xory = st.nextToken();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		//str=constraintEquations.getVerticalLineSegmentPD(pt,xory,anchorPoints[0],anchorPoints[1],pointStrings[0],pointStrings[1]);
		str= constraintsHelper.verticalConstraintScaleFactor + "*"+constraintEquations.getXDifferencePD(pt,xory,anchorPoints[0],anchorPoints[1],pointStrings[0],pointStrings[1]);
		retVec.add(str);
		return retVec;
	}

	public void draw(Graphics g)
	{
		//19-2-2008 Removed showing the horizontal and vertical segment constraints
		if(true)
			return;
		if((m_seg.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL) && (m_seg.isEnabled()))
		{
			Graphics2D g2d = (Graphics2D)g;
			// set the color of the graphics to the color of the segment
			Color prevColor = g.getColor();
			g2d.setColor(getColor());

			// create a dashed line for radii lines
			BasicStroke prevStroke = (BasicStroke)g2d.getStroke();
			g2d.setColor(GVariables.DRAWING_ASSIST_COLOR);
			g2d.setStroke(new BasicStroke(prevStroke.getLineWidth(), prevStroke.getEndCap(), prevStroke.getLineJoin(), prevStroke.getMiterLimit(), new float[]{4, 4, 8, 4}, prevStroke.getDashPhase()));
			g2d.draw(marker_line);
			g2d.setStroke(prevStroke);
			g2d.draw(marker_rect);
			
			// reset the graphics color back
			g2d.setColor(prevColor);
		}
	}

	private void calcMarkers()
	{
		SegLine seg=(SegLine)m_seg;
		// draw the axis line
		marker_line.setLine((int)seg.getM_start().getX()-MARKER_LINE_LENGTH,(int) seg.getM_start().getY(),(int) seg.getM_start().getX()+MARKER_LINE_LENGTH, (int)seg.getM_start().getY());
		
		// draw the 90 deg angle
		if(Math.abs(seg.getM_start().getY() - seg.getM_end().getY()) > MARKER_ANGLE_SIDE_LENGTH)
		{
			if(seg.getM_start().getY() < seg.getM_end().getY())
			{
				marker_rect.setBounds((int)(seg.getM_start().getX()+0.5), (int)(seg.getM_start().getY()+0.5), MARKER_ANGLE_SIDE_LENGTH, MARKER_ANGLE_SIDE_LENGTH);
			}
			else
			{
				marker_rect.setBounds((int)(seg.getM_start().getX()+0.5), (int)(seg.getM_start().getY()+0.5)-MARKER_ANGLE_SIDE_LENGTH, MARKER_ANGLE_SIDE_LENGTH, MARKER_ANGLE_SIDE_LENGTH);
			}
		}
	}
	
	public void update()
	{
		calcMarkers();
		super.update();
	}
	
	public boolean isConstraintSolved()
	{
		this.update();
		SegLine l=(SegLine)m_seg;
		if(Math.abs(l.getM_start().getX() - l.getM_end().getX())<constraintsHelper.pointOverlapErrorThreshold)
			return true;
		constraintsHelper.printConstraintSolvingFailure("PerpendicularIndConstraint.isConstraintSolved","Two Xs are : "+l.getM_start().getX()+ " , " + l.getM_end().getX(),true);
		return false;
	}
}
