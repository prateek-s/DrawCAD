package dcad.process.preprocess;



/**
 * Class has methods to preprocess the drawing data. Currently this class is singleton class, 
 * as currently it is assumed that their will be only one drawing window as present at any 
 * point of time. In future this may not be the case.
 * @author vishalk
 *
 */
public class PreProcessingManager
{
	private static PreProcessingManager m_ppm;
	private PreProcessor m_preProcessor;

	public static PreProcessingManager getInstance()
	{
		if (m_ppm == null)
			m_ppm = new PreProcessingManager();
		return m_ppm;
	}

	private PreProcessingManager()
	{
		init();
	}

	/**
	 * Method to initialize various member variables
	 *
	 */
	private void init()
	{
		m_preProcessor = new PreProcessor();
	}
	
	public PreProcessor getPreProcessor()
	{
		return m_preProcessor;
	}
}
