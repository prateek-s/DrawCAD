package dcad.process.recognition.constraint;

import java.util.Vector;

import dcad.model.geometry.segment.Segment;

public class IndConstraintRecognizer
{
	private IndAngleRecognizer m_indAngleRecog = null;
	private Vector m_constraints = null;
	private Segment m_seg = null;

	public IndConstraintRecognizer()
	{
		reset();
	}

	private void reset()
	{
		m_constraints = new Vector();
	}
	
	private void init(Segment seg)
	{
		reset();
		m_seg=seg;
	}
	
	/**
	 * recognize *angle* constraints for the segment 
	 * There will be another method for recognizing constraints for a particular segment 
	 * @param seg the first segment
	 * @return Vector of all the constraints recognized.
	 */
	public Vector recognizeConstraints(Segment seg)
	{
//		System.out.println("IndConstraintRecognizer.recognizeConstraints()");
		init(seg);
		if(seg != null)
		{
			// find all the constraints as a vector
			Vector angleCons = recogAngleConstraints();
			
			// add all the constraints found above, some processing can be done at this stage
			if(angleCons != null) m_constraints.addAll(angleCons);
		}
		
		return m_constraints;
	}
	
	/** 
	 *  Horizontal/Vertical soft constraints.
	 * @return
	 */
	private Vector recogAngleConstraints()
	{
//		System.out.println("IndConstraintRecognizer.recogAngleConstraints()");
		// find all the constraint related to angle
		if(m_indAngleRecog == null) m_indAngleRecog = new IndAngleRecognizer(m_seg);
		else m_indAngleRecog.init(m_seg);
		return m_indAngleRecog.recognize();
	}
}
