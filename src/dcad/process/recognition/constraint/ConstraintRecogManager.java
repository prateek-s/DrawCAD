package dcad.process.recognition.constraint;


public class ConstraintRecogManager
{
	private static ConstraintRecogManager consRecogMan = null;
	private IndConstraintRecognizer m_indConsRecog = null;
	private RelConstraintRecognizer m_relConsRecog = null;
	
	public static ConstraintRecogManager getInstance()
	{
		if (consRecogMan == null) consRecogMan = new ConstraintRecogManager();
		return consRecogMan;
	}
	
	private ConstraintRecogManager()
	{
		m_indConsRecog = new IndConstraintRecognizer();
		m_relConsRecog = new RelConstraintRecognizer();
	}

	public RelConstraintRecognizer getRelConsRecog()
	{
		return m_relConsRecog;
	}

	public IndConstraintRecognizer getIndConsRecog()
	{
		return m_indConsRecog;
	}

}
