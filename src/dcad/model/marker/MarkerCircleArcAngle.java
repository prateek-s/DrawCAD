package dcad.model.marker;

import dcad.model.geometry.Text;
import dcad.model.geometry.segment.SegCircleCurve;

public class MarkerCircleArcAngle extends Marker
{


	private Text m_text = null; 
	private double m_angle = -1.0;
	private SegCircleCurve m_circleCurveSeg = null;
	
	public MarkerCircleArcAngle(SegCircleCurve circularCurveSeg, Text text)
	{
		super(null);
		setM_circleCurveSeg(circularCurveSeg);
		setM_text(text);
		setM_type(Marker.TYPE_CIRCULAR_ARC_ANGLE);
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
			setM_angle(-1.0);
			e.printStackTrace();
		}
	}


	public SegCircleCurve getM_circleCurveSeg()
	{
		return m_circleCurveSeg;
	}

	public void setM_circleCurveSeg(SegCircleCurve curveSeg)
	{
		m_circleCurveSeg = curveSeg;
	}

	public double getM_angle()
	{
		return m_angle;
	}

	public void setM_angle(double m_angle)
	{
		this.m_angle = m_angle;
	}

	
}
