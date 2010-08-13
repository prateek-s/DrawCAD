package dcad.model.marker;

import dcad.model.geometry.Text;
import dcad.model.geometry.segment.SegLine;

public class MarkerLength extends Marker
{
	private Text m_text = null; 
	private double m_length = -1.0;
	private SegLine m_lineSeg = null;
	
	public MarkerLength(SegLine lineSeg, Text text)
	{
		super(null);
		setM_lineSeg(lineSeg);
		setM_text(text);
		setM_type(Marker.TYPE_FIXED_LENGTH);
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
			double length = Double.parseDouble(m_text.getM_text());
			if(length >= 0)
			{
				setM_length(length);
				this.m_text = m_text;
			}
		} catch (NumberFormatException e)
		{
			setM_length(-1.0);
			e.printStackTrace();
		}
	}

	public double getM_length()
	{
		return m_length;
	}

	private void setM_length(double m_length)
	{
		this.m_length = m_length;
	}

	public SegLine getM_lineSeg()
	{
		return m_lineSeg;
	}

	public void setM_lineSeg(SegLine seg)
	{
		m_lineSeg = seg;
	}

}
