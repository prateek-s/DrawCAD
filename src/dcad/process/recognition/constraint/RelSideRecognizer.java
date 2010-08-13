package dcad.process.recognition.constraint;

import java.util.Vector;

import dcad.model.geometry.segment.Segment;

public class RelSideRecognizer extends RelConstraintRecognitionScheme
{

	public RelSideRecognizer(Segment seg1, Segment seg2)
	{
		super(seg1, seg2);
	}

	protected void init(Segment seg1, Segment seg2)
	{
		super.init(seg1, seg2);
	}

	public Vector recognize()
	{
		return null;
	}

}
