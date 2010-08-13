package dcad.model.marker;

import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.Segment;

public class MarkerPerpendicular extends MarkerAngle
{
	public MarkerPerpendicular(Stroke stroke, Segment seg1, Segment seg2)
	{
		// set the angle as 90 degrees
		super(stroke, seg1, seg2, null);
		m_type = TYPE_RIGHT_ANGLE;
	}
}
