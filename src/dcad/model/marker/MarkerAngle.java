package dcad.model.marker;

import java.awt.geom.Point2D;

import dcad.model.geometry.Stroke;
import dcad.model.geometry.Text;
import dcad.model.geometry.segment.Segment;

public class MarkerAngle extends Marker
{
	private Segment m_seg1 = null;
	private Segment m_seg2 = null;
	
	// this will be filled by associating the text indicating the angle value
	private double m_angle = -1.0;
	private Text m_text = null;
	
	public MarkerAngle(Stroke stroke, Segment seg1, Segment seg2, Text text)
	{
		super(stroke);
		m_type = Marker.TYPE_ANGLE;
		m_seg1 = seg1;
		m_seg2 = seg2;
		setM_text(text);
	}

	public Segment getM_seg1()
	{
		return m_seg1;
	}

	public void setM_seg1(Segment m_seg1)
	{
		this.m_seg1 = m_seg1;
	}

	public Segment getM_seg2()
	{
		return m_seg2;
	}

	public void setM_seg2(Segment m_seg2)
	{
		this.m_seg2 = m_seg2;
	}

	public double getM_angle()
	{
		return m_angle;
	}

	private void setM_angle(double m_angle)
	{
		this.m_angle = m_angle;
	}

	public Text getM_text()
	{
		return m_text;
	}

	public void setM_text(Text m_text)
	{
		if(m_text != null)
		{
			try
			{
				double angle = Double.parseDouble(m_text.getM_text());
				if((angle >= -360) && (angle <= 360))
				{
					setM_angle(angle);
					this.m_text = m_text;
				}
			} catch (Exception e)
			{
				setM_angle(-1.0);
				e.printStackTrace();
			}
		}
	}

	
	
	
}
