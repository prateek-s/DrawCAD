package dcad.model.marker;

import java.util.Iterator;

import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.Segment;
import dcad.ui.drawing.DrawingView;
import dcad.util.GConstants;
import dcad.util.GMethods;


public abstract class Marker extends GeometryElement
{
	public static final int TYPE_NONE = 0; 
	public static final int TYPE_EQUALITY = 1; 
	public static final int TYPE_ANGLE = 2; 
	public static final int TYPE_PARALLEL = 3;
	public static final int TYPE_RIGHT_ANGLE = 4; 
	public static final int TYPE_FIXED_LENGTH = 5; 
	public static final int TYPE_RADIUS = 6; 
	public static final int TYPE_CIRCULAR_ARC_ANGLE = 7; 
	public static final int TYPE_LINE_ANGLE = 8;
	public static final int TYPE_LINE_DISTANCE = 9;
	public static final int TYPE_LINE_CURVE_DISTANCE = 10 ;
	public static final int TYPE_2_POINTS_DISTANCE = 11 ;
	public static final int TYPE_HORIZONTAL_POINTS = 12;
	public static final int TYPE_VERTICAL_POINTS = 13;
	public static final int TYPE_POINT_SEGMENT_DISTANCE = 14;
	public static final int TYPE_CURVE_DISTANCE = 15;
	public static int MARKER_SIZE = GConstants.DEF_MARKER_SIZE;
	
	protected Stroke m_stroke = null;
	protected int m_type = TYPE_NONE;
	protected boolean m_used = false;
	
	protected Marker(Stroke stroke)
	{
		m_stroke = stroke;
	}
	public boolean containsPt(double x, double y)
	{
		if(m_stroke != null)
		{
			Iterator iter = m_stroke.getM_segList().iterator();
			while (iter.hasNext())
			{
				Segment seg = (Segment) iter.next();
				if(seg.containsPt(x, y))
				{
					return true;
				}
			}
		}
		return false;
	}

	public void move(int x1, int y1, int x2, int y2)
	{
		if(m_stroke != null) m_stroke.move(x1, y1, x2, y2);
	}

	public Stroke getM_stroke()
	{
		return m_stroke;
	}

	public void setM_stroke(Stroke m_stroke)
	{
		this.m_stroke = m_stroke;
	}

	public int getM_type()
	{
		return m_type;
	}

	public void setM_type(int m_type)
	{
		this.m_type = m_type;
	}
	
	public boolean isM_used()
	{
		return m_used;
	}
	
	public void setM_used(boolean m_used)
	{
		if(getM_stroke() != null) getM_stroke().setEnabled(!m_used);
		this.m_used = m_used;
	}

	public void delete()
	{
		// remove this Text element from the Drawing data.
		DrawingView dv = GMethods.getCurrentView();
		dv.removeGeoElement(this);
		// this is just a part of the stroke.. so remove the stroke
		if(m_stroke != null) m_stroke.delete();
		super.delete();
	}
	
	public void setSelected(boolean selected)
	{
		if(m_stroke != null) 
		{
			Iterator iter = m_stroke.getM_segList().iterator();
			while (iter.hasNext())
			{
				Segment seg = (Segment) iter.next();
				seg.setSelected(selected);
			}
			super.setSelected(selected);
		}
	}

	public void setHighlighted(boolean highlighted)
	{
		if(m_stroke != null) 
		{
			Iterator iter = m_stroke.getM_segList().iterator();
			while (iter.hasNext())
			{
				Segment seg = (Segment) iter.next();
				seg.setHighlighted(highlighted);
			}
			super.setHighlighted(highlighted);
		}
	}
	
	public GeometryElement copy()
	{
		return null;
	}

	
}
