package dcad.model.marker;

import dcad.model.geometry.Text;
import dcad.model.geometry.segment.SegLine;

public class MarkerLineAngle extends Marker
{

	private Text m_text = null; 
	private double m_angle = 45;
	private SegLine m_segLine = null;
	
	public MarkerLineAngle(SegLine seg, Text text)
	{
		super(null);
		setM_segLine(seg);
		setM_text(text);
		setM_type(Marker.TYPE_LINE_ANGLE);
	}
	
	public Text getM_text()
	{
		return m_text;
	}
	public void setM_text(Text m_text)
	{
		// convert the text to length, if possible
		try
		{
			double angle = Double.parseDouble(m_text.getM_text());
			if(angle >= 0)
			{
				setM_angle(angle);
				this.m_text = m_text;
			}
		} catch (NumberFormatException e)
		{
			setM_angle(45);
			e.printStackTrace();
		}
	}

	public double getM_angle()
	{
		return m_angle;
	}
	public void setM_angle(double m_angle)
	{
		this.m_angle = m_angle;
	}

	public SegLine getM_segLine()
	{
		return m_segLine;
	}

	public void setM_segLine(SegLine seg)
	{
		m_segLine = seg;
	}
	
}
