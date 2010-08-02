package dcad.model.marker;

import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.Segment;

public class MarkerEquality extends Marker
{
	private Segment m_seg = null; 
	
	public MarkerEquality(Stroke stroke, Segment seg)
	{
		super(stroke);
		m_type = Marker.TYPE_EQUALITY;
		m_seg = seg;
	}

	public Segment getM_seg()
	{
		return m_seg;
	}

	public void setM_seg(Segment m_seg)
	{
		this.m_seg = m_seg;
	}


}
