package dcad.process.recognition.constraint;

import dcad.model.geometry.segment.Segment;

public abstract class IndConstraintRecognitionScheme extends ConstraintRecognitionScheme
{
	protected Segment m_seg = null;

	public IndConstraintRecognitionScheme(Segment seg)
	{
		super();
		init(seg);
	}
	
	protected void init(Segment seg)
	{
		super.reset();
		m_seg = null;
		m_seg=seg;
	}


}