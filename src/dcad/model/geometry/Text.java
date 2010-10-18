package dcad.model.geometry;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

import dcad.model.geometry.segment.Segment;
import dcad.model.marker.Marker;
import dcad.ui.drawing.DrawingView;
import dcad.util.GMethods;

public class Text extends GeometryElement
{
	public static final int DEF_FONT_TYPE = Font.PLAIN;
	public static final int DEF_FONT_SIZE = 12;
	private static final int CLOSENESS_DIST_SEGMENT = 5;
	private static final int CLOSENESS_DIST_MARKER = 20;
	private AnchorPoint m_ap;
	private String m_text = "";
	private int fontSize = DEF_FONT_SIZE;
	private FontMetrics fm = null;
	private Marker m_marker = null;
	private boolean m_used = false;

	public Text(String t, Point2D pt)
	{
		if(t!=null) setM_text(t);
		else setM_text(getM_strId());
		// create a new AP for this text
		Vector tempV = new Vector();
		tempV.add(this);
		m_ap = new AnchorPoint(pt, tempV);
		DrawingView dv = GMethods.getCurrentView();
		fm = dv.getGraphics().getFontMetrics();
		m_marker = null;
		m_used = false;
	}

	public Text(String t, double x1, double y1)
	{
		this(t, new Point2D.Double(x1, y1));
	}

	public Text(String t, ArithElement x1, ArithElement y1)
	{
		this(t, x1.dvalue(), y1.dvalue());
	}

	public Text(String t)
	{ 
		this(t, null);
	}

	public Text(Point2D p1)
	{ 
		this(null, p1);
	}

	public String toString()
	{
		return m_label + " = Text(" + getM_text() + ")";
	}

	public void setM_text(String s)
	{
		m_text = GMethods.convertStringToUnicode(s);
	}

	public void setFontSize(String s)
	{
		fontSize = Integer.parseInt(s);
	}

	public void setFontSize(int s)
	{
		fontSize = s;
	}

	public String getM_text()
	{
		return m_text;
	}

	public AnchorPoint getM_ap()
	{
		return m_ap;
	}

	public void draw(Graphics g)
	{
		if(!isEnabled()) return;
		super.draw(g);
		
		int x1 = (int) m_ap.getX();
		int y1 = (int) m_ap.getY();
		
		Color prevColor = g.getColor();
		g.setColor(getM_color());

		Font f = g.getFont();
		g.setFont(new Font(f.getName(), DEF_FONT_TYPE, fontSize));
		fm = g.getFontMetrics();
		if (m_text != null)	
		{
			g.drawString(getM_text(), x1, y1);
			if(isHighlighted())
			{
				((Graphics2D)g).draw(getBB());
			}
		}
		
		g.setFont(f);
		// reset the color back
		g.setColor(prevColor);
	}

	private Rectangle getBB()
	{
		int x1 = (int) m_ap.getX();
		int y1 = (int) m_ap.getY();
		if (m_text != null)
		{
			int l = fm.stringWidth(m_text);
			int h = fm.getHeight();
			return new Rectangle(x1-2, y1-h+2, l+2, h);
		}
		return null;
	}

	public void move(int x1, int y1, int x2, int y2)
	{
		AffineTransform tx = new AffineTransform();
		tx.setToTranslation(x2-x1, y2-y1);
		tx.transform(m_ap.getM_point(), m_ap.getM_point());
	}

	public void setM_parent(GeometryElement m_parent)
	{
		this.m_parent = m_parent;
	}
	
	public boolean isCLose(GeometryElement gEle, int dist)
	{
//		System.out.println("Text.isCLose()");
		return intersects(gEle, dist);
	}
	
	public boolean touches(GeometryElement gEle)
	{
//		System.out.println("Text.touches()");
		// no buffer for touching
		return intersects(gEle, 0);
	}
	
	private boolean intersects(GeometryElement gEle, int closeness)
	{
//		System.out.println("Text.intersects()");
		Rectangle rect = getBB();
		if(rect != null)
		{
			// increase the rect by a few
//			System.out.println(rect);
			rect.setBounds(rect.x-closeness, rect.y-closeness, rect.width+(closeness*2), rect.height+(closeness*2));
//			System.out.println(rect);
			if(gEle instanceof Segment)
			{
				Segment seg = (Segment)gEle;
				Shape shape = seg.getM_shape();
				if(shape.intersects(rect))
				{
//					System.out.println("INTERSECTION*************");
					return true;
				}
			}
			else if(gEle instanceof Stroke)
			{
				Stroke stk = (Stroke)gEle;
				Vector segments = stk.getM_segList();
				Iterator iter = segments.iterator();
				while (iter.hasNext())
				{
					Segment seg = (Segment)iter.next();
					Shape shape = seg.getM_shape();
					if(shape.intersects(rect))
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean containsPt(double x, double y)
	{
		Rectangle rect = getBB();
		if ((rect != null) && (rect.contains(x, y))) return true;
		return false;
	}
	
	public AnchorPoint addAnchorPoint(Point2D pt)
	{
		Vector tempV = new Vector();
		tempV.add(this);
		AnchorPoint ap = new AnchorPoint(pt, tempV);
		return ap;
	}
	
	public void delete()
	{
//		System.out.println("Text.delete()");
		
		// remove this Text element from the Drawing data.
		DrawingView dv = GMethods.getCurrentView();
		dv.A.removeGeoElement(this);
		
//		// remove the Text Element
//		removeFromMarker();
	}
	
/*	*//**
	 * This text could be a part of some constraint, through a marker, 
	 * in which case that constaints is also required to be removed.
	 *
	 *//*
	public void removeFromMarker()
	{
		if(m_marker != null)
		{
			m_marker.delete();
		}
	}
*/
	public Marker getM_marker()
	{
		return m_marker;
	}

	public void setM_marker(Marker m_marker)
	{
		this.m_marker = m_marker;
	}

	public boolean isM_used()
	{
		return m_used;
	}

	public void setM_used(boolean m_used)
	{
		this.m_used = m_used;
	}
	
	public Marker getClosestMarker(Vector markers)
	{
		// first check for touch
		Iterator iter = markers.iterator();
		while (iter.hasNext())
		{
			Marker marker = (Marker) iter.next();
			if(this.touches(marker.getM_stroke())) return marker;
		}
		
		// no touching found .. find closeness to any of the markers
		iter = markers.iterator();
		while (iter.hasNext())
		{
			Marker marker = (Marker) iter.next();
			if(this.isCLose(marker.getM_stroke(), CLOSENESS_DIST_MARKER)) return marker;
		}
		return null;
	}

	public Marker getClosestMarker(Vector markers, Class classname)
	{
		// first check for touch
		Iterator iter = markers.iterator();
		while (iter.hasNext())
		{
			Marker marker = (Marker) iter.next();
			if((!marker.isM_used()) &&(classname.equals(marker.getClass()))&&(this.touches(marker.getM_stroke()))) return marker;
		}
		
		// no touching found .. find closeness to any of the markers
		iter = markers.iterator();
		while (iter.hasNext())
		{
			Marker marker = (Marker) iter.next();
			if(((!marker.isM_used()) && classname.equals(marker.getClass()))&&(this.isCLose(marker.getM_stroke(), CLOSENESS_DIST_MARKER))) return marker;
		}
		return null;
	}

	public Segment getClosestSegment(Vector segments)
	{
		// first find is this text element is touching with any of the segments
		Iterator iter = segments.iterator();
		while (iter.hasNext())
		{
			Segment seg = (Segment) iter.next();
			if(this.touches(seg)) return seg;
		}

		//no touching found with any of the segments, find closeness to any of the segments
		iter = segments.iterator();
		while (iter.hasNext())
		{
			Segment seg = (Segment) iter.next();
			if(this.isCLose(seg, CLOSENESS_DIST_SEGMENT)) return seg;
		}
		return null;
	}
	
	public Segment getClosestSegment(Vector segments, Class classname)
	{
		// first find is this text element is touching with any of the segments
		Iterator iter = segments.iterator();
		while (iter.hasNext())
		{
			Segment seg = (Segment) iter.next();
			if((classname.equals(seg.getClass()))&&(this.touches(seg))) return seg;
		}

		//no touching found with any of the segments, find closeness to any of the segments
		iter = segments.iterator();
		while (iter.hasNext())
		{
			Segment seg = (Segment) iter.next();
			if((classname.equals(seg.getClass()))&&(this.isCLose(seg, CLOSENESS_DIST_SEGMENT))) return seg;
		}
		return null;
	}
	
	public GeometryElement getM_parent()
	{
		return m_marker;
	}

	public GeometryElement copy()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
