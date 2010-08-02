package dcad.process.recognition.marker;

public class MarkerRecogManager
{
	private static MarkerRecogManager markerRecogMan;
	private MarkerRecognizer m_markerRecognizer;
	private MarkerToConstraintConverter m_markerConverter;
	
	public static MarkerRecogManager getInstance()
	{
		if (markerRecogMan == null) markerRecogMan = new MarkerRecogManager();
		return markerRecogMan;
	}

	private MarkerRecogManager()
	{
		init();
	}
	
	private void init()
	{
		m_markerRecognizer = MarkerRecognizer.getInstance();
		m_markerConverter = MarkerToConstraintConverter.getInstance();
	}

	public MarkerRecognizer getMarkerRecognizer()
	{
		return m_markerRecognizer;
	}

	public MarkerToConstraintConverter getM_markerConverter()
	{
		return m_markerConverter;
	}
}

