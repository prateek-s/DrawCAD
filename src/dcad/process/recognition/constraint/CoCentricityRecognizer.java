package dcad.process.recognition.constraint;

import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.circleArc.CoCentricConstraint;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.Segment;
import dcad.util.GMethods;

public class CoCentricityRecognizer extends RelConstraintRecognitionScheme
{
	public CoCentricityRecognizer(Segment seg1, Segment seg2)
	{
		super(seg1, seg2);
	}
	
	public Vector recognize()
	{
		SegCircleCurve curve1 = (SegCircleCurve)m_seg1;
		SegCircleCurve curve2 = (SegCircleCurve)m_seg2;
		if(curve1.getM_center().distance(curve2.getM_center()) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
			if(constraintsHelper.getConstraintBetween2Segments(curve1,curve2,CoCentricConstraint.class)==null)
			{
				Constraint cc = new CoCentricConstraint(curve1, curve2,Constraint.HARD,false);
				addConstraint(cc,new Segment[]{curve1,curve2});
			}
		return m_constraints;
	}
}