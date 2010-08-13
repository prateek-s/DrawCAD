
package dcad.model.constraint;

import dcad.model.constraint.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.ImpPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.Segment;

public abstract class PointSegmentConstraint extends Constraint
{
	public static final int SIZE = 3;
	protected Segment m_seg = null;
	protected AnchorPoint m_ap = null;
	
	public PointSegmentConstraint(Segment seg,AnchorPoint ap,int category,boolean promoted)
	{
		m_seg=seg;
		m_ap = ap;
		setM_category(category);
		setPromoted(promoted);
		clearPoints();
	}

	public AnchorPoint getM_ap()
	{
		return m_ap;
	}
	
	public Segment getM_seg()
	{
		return m_seg;
	}
	
	public void changePoint(ImpPoint ip1,ImpPoint ip2)
	{
		super.changePoint(ip1,ip2);
		if(m_ap == ip1)
			m_ap=(AnchorPoint)ip2;
	}
	
	
	public void delete()
	{
		m_seg.removeConstraint(this);
	}

	public void draw(Graphics g)
	{
		
		//Commented this on 8-5-2008 
		//This was showing the black points on the screen even after removal of the constraint 
/*		if(m_seg.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL)
		{
			// set the color of the graphics.
			Graphics2D g2d = (Graphics2D)g;
			Color prevColor = g.getColor();
			g2d.setColor(getColor());

			// draw first contact point
			AnchorPoint a=(AnchorPoint)points.get(points.size()-1);
			drawPoint(g2d, (int)(a.getX()+0.5), (int)(a.getY()+0.5));
			
			g2d.setColor(getColor());

			// reset the color back
			g2d.setColor(prevColor);
		}*/
	}
	
	private void drawPoint(Graphics2D g2d, int x, int y)
	{
		g2d.fillArc(x-SIZE+1, y-SIZE+1, SIZE*2, SIZE*2, 0, 360);
//		g2d.setColor(Color.YELLOW);
//		// mark the actual point
// 		g2d.drawRect(x, y, 1, 1);
	}

		
}