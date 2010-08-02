package dcad.process.recognition;

import dcad.process.recognition.constraint.ConstraintRecogManager;
import dcad.process.recognition.marker.MarkerRecogManager;
import dcad.process.recognition.segment.SegmentRecogManager;
import dcad.process.recognition.stroke.StrokeRecogManager;


public class RecognitionManager
{
	private static RecognitionManager recogMan;
	private SegmentRecogManager m_segRecogMan = null;
	private ConstraintRecogManager m_constraintRecogManager = null;
	private StrokeRecogManager m_strokeRecogManager = null;
	private MarkerRecogManager m_markerRecogManager = null;

	public static RecognitionManager getInstance()
	{
		if (recogMan == null) recogMan = new RecognitionManager();
		return recogMan;
	}

	private RecognitionManager()
	{
		init();
	}
	
	private void init()
	{
		m_segRecogMan = SegmentRecogManager.getInstance();
		m_constraintRecogManager = ConstraintRecogManager.getInstance();
		m_markerRecogManager = MarkerRecogManager.getInstance();
		m_strokeRecogManager = StrokeRecogManager.getInstance();
	}

	public ConstraintRecogManager getConstraintRecogManager()
	{
		return m_constraintRecogManager;
	}

	public SegmentRecogManager getSegmentRecogMan()
	{
		return m_segRecogMan;
	}
	
	public MarkerRecogManager getMarkerRecognitionMan()
	{
		return m_markerRecogManager;
	}

	public StrokeRecogManager getM_strokeRecogManager()
	{
		return m_strokeRecogManager;
	}

}
