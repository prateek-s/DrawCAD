package dcad.process.recognition.constraint;

import dcad.model.geometry.segment.Segment;

public abstract class RelConstraintRecognitionScheme extends ConstraintRecognitionScheme
{
	protected Segment m_seg1 = null;
	protected Segment m_seg2 = null;

	public RelConstraintRecognitionScheme(Segment seg1, Segment seg2)
	{
		super();
		init(seg1, seg2);
	}

	protected void init(Segment seg1, Segment seg2)
	{
		super.reset();
		m_seg1=seg1;
		m_seg2=seg2;
	}
}