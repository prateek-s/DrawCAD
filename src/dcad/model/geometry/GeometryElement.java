package dcad.model.geometry;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;

import dcad.util.GVariables;

public abstract class GeometryElement extends Element
{
	protected boolean fixed = false;
	protected GeometryElement m_parent = null;

	public abstract boolean containsPt(double x, double y);
	public abstract void move(int x1, int y1, int x2, int y2);
	/**
	 * returns copy of the element
	 */
	public abstract GeometryElement copy();
	
	public void draw(Graphics g)
	{
		if(!isEnabled()) return;
	}

	public String toString()
	{
		return getM_label();
	}
	
	public boolean containsPt(Point2D pt)
	{
		return containsPt(pt.getX(), pt.getY());
	}

	public void delete() {}

	public void move(Point2D p1, Point2D p2)
	{
		move(p1.getX(), p1.getY(), p2.getX(), p2.getY());
	}

	public void move(double x1, double y1, double x2, double y2)
	{
		move((int)x1, (int)y1, (int)x2, (int)y2);
	}

	public boolean isFixed()
	{
		return fixed;
	}

	public void setFixed(boolean fixed)
	{
		this.fixed = fixed;
	}

	public Color getM_color()
	{
		if(isSelected() && isHighlighted())
		{
			return GVariables.HIGHLIGHTED_SELECTED_COLOR;
		}
		else if(isHighlighted())
		{
			return GVariables.HIGHLIGHTED_COLOR;
		}
		else if(isFixed() && isSelected())
		{
			return GVariables.SELECTED_FIXED_COLOR;
		}
		else if(isFixed())
		{
			return GVariables.FIXED_COLOR;
		}
		else if(isSelected())
		{
			return GVariables.SELECTED_COLOR;
		}
		else
		{
			return m_color;
		}
	}

	protected void setM_parent(GeometryElement m_parent)
	{
		this.m_parent = m_parent;
	}

}