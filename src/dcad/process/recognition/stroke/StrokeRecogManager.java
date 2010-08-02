package dcad.process.recognition.stroke;


public class StrokeRecogManager
{
	private static StrokeRecogManager strokeRecogMan;
	private StrokeRecognizer m_strokeRecognizer;
	public static StrokeRecogManager getInstance()
	{
		if (strokeRecogMan == null) strokeRecogMan = new StrokeRecogManager();
		return strokeRecogMan;
	}

	private StrokeRecogManager()
	{
		m_strokeRecognizer = new StrokeRecognizer();
	}

	public StrokeRecognizer getStrokeRecognizer()
	{
		return m_strokeRecognizer;
	}
}
