package dcad.model.marker;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.Text;
import dcad.model.geometry.segment.Segment;

public class MarkerDistance extends Marker
{
	private Segment[] m_segments = null;
	private AnchorPoint[] m_anchorPoints = null;
	private double m_distance = 0;
	
	public MarkerDistance(Segment[] segArray, AnchorPoint[] apArray, Text text, int markerType)
	{
		super(null);
		m_segments = segArray;
		m_anchorPoints = apArray;
		m_distance = Double.parseDouble(text.getM_text());
		m_type = markerType;
	}

	public double getM_distance()
	{
		return m_distance;
	}

	public Segment[] getM_segments()
	{
		return m_segments;
	}

	public AnchorPoint[] getM_anchorPoints()
	{
		return m_anchorPoints;
	}
}