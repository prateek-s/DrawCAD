package dcad.model.marker;

import dcad.model.geometry.AnchorPoint;

public class MarkerPointsAlignment extends Marker
{
	private AnchorPoint[] points;
	
	public MarkerPointsAlignment(AnchorPoint point1,AnchorPoint point2, int markerType)
	{
		super(null);
		points = new AnchorPoint[]{point1,point2};
		m_type = markerType;
	}

	public AnchorPoint[] getPoints()
	{
		return points;
	}

}
