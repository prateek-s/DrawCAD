package dcad.model.geometry;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Vector;

import dcad.Prefs;


/**
 * Points to move the geometry object it is bounded to 
 * @author vishalk
 *
 */
public class MovePoint extends ImpPoint
{
 	public MovePoint(Point2D point, Vector parent)
	{
		super(point, parent);
		m_size = Prefs.getMovePtSize();
		m_type = Prefs.getMovePtType();
		setM_color(Color.GREEN);
	}

	public void draw(Graphics g)
	{
		if(!isEnabled()) return;
		super.draw(g);

		// set the color of the graphics.
		Graphics2D g2d = (Graphics2D)g;
		Color prevColor = g.getColor();
		g2d.setColor(getM_color());
		
		drawPoint(g2d, m_size, m_type);
		
		g2d.setColor(Color.YELLOW);
		// mark the actual point
 		g2d.drawRect((int)getM_point().getX(), (int)getM_point().getY(), 1, 1);
		// reset the color back
		g2d.setColor(prevColor);
	}

	public GeometryElement copy()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
