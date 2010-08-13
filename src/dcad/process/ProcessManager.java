package dcad.process;

import dcad.process.beautification.ConstraintSolver;
import dcad.process.preprocess.PreProcessingManager;
import dcad.process.recognition.RecognitionManager;

/**
 * This class is the main class for processing the raw data obtained from user or through
 * a lesson file. This class gives pointers to other manager for various stanges of processing.
 * This is a singleton class, but the other managers it calls/uses many not be singleton. 
 * @author vishalk
 *
 */
public class ProcessManager
{
	private PreProcessingManager m_preProManager;
	private RecognitionManager m_recogManager;
	private ConstraintSolver m_constraintSolver;
	private static ProcessManager processMan;

	public static ProcessManager getInstance()
	{
		if (processMan == null) processMan = new ProcessManager();
		return processMan;
	}

	private ProcessManager()
	{
		init();
	}
	
	private void init()
	{
		m_preProManager = PreProcessingManager.getInstance();
		m_recogManager = RecognitionManager.getInstance();
//		m_constraintSolver = ConstraintSolver.getInstance();
	}

	public PreProcessingManager getPreProManager()
	{
		return m_preProManager;
	}

	public RecognitionManager getRecogManager()
	{
		return m_recogManager;
	}

	public ConstraintSolver getM_constraintSolver()
	{
		return m_constraintSolver;
	}
}
