package dcad.model.constraint.connect;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import dcad.model.constraint.RelativeConstraint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.Segment;

public abstract class ConnectConstraint extends RelativeConstraint
{
	public static final int SIZE = 3;
	protected Point2D m_contactPt1 = null;
	protected Point2D m_contactPt2 = null;
	
	public ConnectConstraint(Segment seg1, Segment seg2,int category,boolean promoted)
	{
		super(seg1, seg2,category,promoted);
	}

	public Point2D getM_contactPt2()
	{
		return m_contactPt2;
	}

	public void setM_contactPt2(Point2D pt)
	{
		m_contactPt2 = pt;
	}

	public Point2D getM_contactPt1()
	{
		return m_contactPt1;
	}

	public void setM_contactPt1(Point2D pt1)
	{
		m_contactPt1 = pt1;
	}
	
	public void draw(Graphics g)
	{
		
/*		if((m_seg1.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL) && (m_seg2.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL))
		{
			// set the color of the graphics.
			Graphics2D g2d = (Graphics2D)g;
			Color prevColor = g.getColor();
			g2d.setColor(getColor());

			// draw first contact point
			drawPoint(g2d, (int)(m_contactPt1.getX()+0.5), (int)(m_contactPt1.getY()+0.5));
			
			g2d.setColor(getColor());
			// draw second contact point
			drawPoint(g2d, (int)(m_contactPt2.getX()+0.5), (int)(m_contactPt2.getY()+0.5));

			// reset the color back
			g2d.setColor(prevColor);
		}
		*/
	}
	
	private void drawPoint(Graphics2D g2d, int x, int y)
	{
		g2d.fillArc(x-SIZE+1, y-SIZE+1, SIZE*2, SIZE*2, 0, 360);
//		g2d.setColor(Color.YELLOW);
//		// mark the actual point
// 		g2d.drawRect(x, y, 1, 1);
	}
}
