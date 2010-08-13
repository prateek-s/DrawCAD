package dcad.model;

import java.awt.Point;
import java.awt.Rectangle;

public class BoundingBox
{
	protected Point topLeft = new Point();
	protected Point bottomRight = new Point();
	//protected Rectangle m_bBox;
	
	public BoundingBox(Point topLeft, Point bottomRight)
	{
		this.topLeft = (Point)topLeft.clone();
		this.bottomRight = (Point)bottomRight.clone();
		//m_bBox = new Rectangle(topLeft.x, topLeft.y, bottomRight.x-topLeft.x, bottomRight.y-topLeft.y);
	}

	public BoundingBox(int x1, int y1, int x2, int y2)
	{
		this.topLeft.x = x1;
		this.topLeft.y = y1;
		this.bottomRight.x = x2;
		this.bottomRight.y = y2;
		//m_bBox = new Rectangle(x1, y1, x2-x1, y2-y1);
	}
	
	public BoundingBox(Rectangle bb)
	{
		setBBRect(bb);
	}
	
	public Point getBottomRight()
	{
		return bottomRight;
	}
	public void setBottomRight(Point bottomRight)
	{
		this.bottomRight = bottomRight;
	}
	public Point getTopLeft()
	{
		return topLeft;
	}
	public void setTopLeft(Point topLeft)
	{
		this.topLeft = topLeft;
	}
	
	public BoundingBox copy()
	{
		return new BoundingBox((Point)topLeft.clone(), (Point)bottomRight.clone());
	}	
	
	public Rectangle getBBRect()
	{
		return new Rectangle(topLeft.x, topLeft.y, bottomRight.x-topLeft.x, bottomRight.y-topLeft.y);
	}

	public void setBBRect(Rectangle bb)
	{
		this.topLeft.x = bb.x;
		this.topLeft.y = bb.y;
		this.bottomRight.x = bb.x+bb.width;
		this.bottomRight.y = bb.y+bb.width;
	}
}
