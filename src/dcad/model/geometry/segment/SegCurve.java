package dcad.model.geometry.segment;

import java.awt.Graphics;
import java.util.Vector;


public abstract class SegCurve extends Segment
{

	public SegCurve()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	public void draw(Graphics g)
	{
		if(!isEnabled()) return;
		super.draw(g);
	}
}
