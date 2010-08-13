package dcad.process.recognition.segment;

public class SegmentRecogManager
{
	private static SegmentRecogManager segRecogMan;
	private SegmentRecognizer m_segmentRecognizer;
	public static SegmentRecogManager getInstance()
	{
		if (segRecogMan == null) segRecogMan = new SegmentRecogManager();
		return segRecogMan;
	}

	private SegmentRecogManager()
	{
		init();
	}
	
	private void init()
	{
		m_segmentRecognizer = new SegmentRecognizer();
	}

	public SegmentRecognizer getSegmentRecognizer()
	{
		return m_segmentRecognizer;
	}
}
