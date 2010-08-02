package dcad.model.marker;

import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.Segment;

public class MarkerParallel extends Marker
{
	private Segment m_seg = null;
	
	public MarkerParallel(Stroke stroke, Segment seg)
	{
		super(stroke);
		m_type = Marker.TYPE_PARALLEL;
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
