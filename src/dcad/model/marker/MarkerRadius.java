package dcad.model.marker;

import dcad.model.geometry.Text;
import dcad.model.geometry.segment.SegCircleCurve;

public class MarkerRadius extends Marker
{

	private Text m_text = null; 
	private double m_radius = -1.0;
	private SegCircleCurve m_circleCurveSeg = null;
	
	public MarkerRadius(SegCircleCurve circularCurveSeg, Text text)
	{
		super(null);
		setM_circleCurveSeg(circularCurveSeg);
		setM_text(text);
		setM_type(Marker.TYPE_RADIUS);
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
			double radius = Double.parseDouble(m_text.getM_text());
			if(radius >= 0)
			{
				setM_radius(radius);
				this.m_text = m_text;
			}
		} catch (NumberFormatException e)
		{
			setM_radius(-1.0);
			e.printStackTrace();
		}
	}

	public double getM_radius()
	{
		return m_radius;
	}
	public void setM_radius(double m_radius)
	{
		this.m_radius = m_radius;
	}

	public SegCircleCurve getM_circleCurveSeg()
	{
		return m_circleCurveSeg;
	}

	public void setM_circleCurveSeg(SegCircleCurve curveSeg)
	{
		m_circleCurveSeg = curveSeg;
	}
	
}
