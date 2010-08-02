package dcad.model.geometry;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.Vector;


public class SegmentPoint extends ImpPoint
{
	public SegmentPoint(Point2D point, Vector parent)
	{
		super(point, parent);
		setM_color(Color.BLACK);
	}

	public void draw(Graphics g)
	{
		if(!isEnabled()) return;
		super.draw(g);
	}

	public GeometryElement copy()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
