package dcad.model.constraint.connect;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.PointSegmentConstraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.ImpPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.Segment;
import dcad.util.GMethods;

public abstract class TangentConstraint extends ConnectConstraint
{
	protected static final int MARKER_ANGLE_SIDE_LENGTH = 10;
	protected static final int MARKER_LINE_LENGTH = 66;

	protected Polygon anglemarker1 = new Polygon();
	protected Polygon anglemarker2 = new Polygon();

	protected Line2D marker_line_center1 = new Line2D.Double(-1, -1, -1, -1);
	protected Line2D marker_line_center2 = new Line2D.Double(-1, -1, -1, -1);
	
	protected Line2D marker_line_contact = new Line2D.Double(-1, -1, -1, -1);
	
	public TangentConstraint(Segment seg1, Segment seg2, AnchorPoint ap,int category,boolean promoted)
	{
		super(seg1, seg2,category,promoted);
//		Point2D contactPt1=new Point2D.Double(ap.getX(),ap.getY());
//		setM_contactPt1(contactPt1);
//		setM_contactPt2(contactPt1);
		setM_contactPt1(ap.getM_point());
		setM_contactPt2(ap.getM_point());
		//new AnchorPoint(contactPt1, null);
		addPoint(ap);//		points.add(ap);
	}
	
	public String toString()
	{
		return addPrefix()+" Tangent : "+m_seg1.getM_label()+" , "+m_seg2.getM_label();
	}

	public void draw(Graphics g)
	{
		if((m_seg1.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL) && (m_seg2.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL))
		{
			Graphics2D g2d = (Graphics2D)g;
			// set the color of the graphics to the color of the segment
			Color prevColor = g.getColor();
			// create a dashed line for radii lines
			BasicStroke prevStroke = (BasicStroke)g2d.getStroke();
			
			g2d.setColor(getColor());
			g2d.setColor(Color.BLACK);
			g2d.setStroke(new BasicStroke(prevStroke.getLineWidth(), prevStroke.getEndCap(), prevStroke.getLineJoin(), prevStroke.getMiterLimit(), new float[]{4, 4, 8, 4}, prevStroke.getDashPhase()));
			g2d.draw(marker_line_center1);
			g2d.draw(marker_line_center2);
			g2d.draw(marker_line_contact);
			
			g2d.setStroke(prevStroke);
			g2d.draw(anglemarker1);
			g2d.draw(anglemarker2);
			
			// reset the graphics color back
			g2d.setColor(prevColor);
		}
		super.draw(g);
	}
	
	//Added on 22-5-2008
	public void changePoint(ImpPoint ip1,ImpPoint ip2)
	{
		super.changePoint(ip1,ip2);
		if(m_contactPt1==ip1.getM_point())
			m_contactPt1 = ip2.getM_point();
		if(m_contactPt2==ip1.getM_point())
			m_contactPt2 = ip2.getM_point();
	}


	
	
	protected void setAngleMarkers(Point2D A, Point2D C, Point2D E, Polygon angleMarker)
	{
		// find point on the LARGER part of line1
		Point2D p1 = new Point2D.Double();
		double ratio = MARKER_ANGLE_SIDE_LENGTH/A.distance(E);
		p1.setLocation(ratio*A.getX()+(1-ratio)*E.getX(), ratio*A.getY()+(1-ratio)*E.getY());

			// find point on the LARGER part of line2
		Point2D p2 = new Point2D.Double();
		ratio = MARKER_ANGLE_SIDE_LENGTH/C.distance(E);
		p2.setLocation(ratio*C.getX()+(1-ratio)*E.getX(), ratio*C.getY()+(1-ratio)*E.getY());

		// at this point we have two points on the LARGER points
		Vector centers = GMethods.CircleCircleIntersections(p1.getX(), p1.getY(), MARKER_ANGLE_SIDE_LENGTH, p2.getX(), p2.getY(), MARKER_ANGLE_SIDE_LENGTH);
		angleMarker.reset();
		if(centers != null)
		{
			Point2D p3 = null;
			if(E.distance((Point2D)centers.get(0)) >= E.distance((Point2D)centers.get(1)))
			{
				p3 = (Point2D)centers.get(0);
			}
			else
			{
				p3 = (Point2D)centers.get(1);
			}
			angleMarker.addPoint((int)E.getX(), (int)E.getY());
			angleMarker.addPoint((int)p1.getX(), (int)p1.getY());
			angleMarker.addPoint((int)p3.getX(), (int)p3.getY());
			angleMarker.addPoint((int)p2.getX(), (int)p2.getY());
		}
	}
}