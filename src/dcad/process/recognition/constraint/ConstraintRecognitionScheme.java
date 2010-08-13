package dcad.process.recognition.constraint;

import java.util.Vector;

import dcad.model.geometry.segment.Segment;
import dcad.util.GMethods;
import dcad.model.constraint.Constraint;import dcad.model.constraint.constraintsHelper;
;

public abstract class ConstraintRecognitionScheme
{
	protected double confidance;
	protected double error;
	protected Vector m_constraints;

	protected ConstraintRecognitionScheme()
	{
	}

	protected void reset()
	{
		confidance = -Double.MAX_VALUE;
		error = Double.MAX_VALUE;
		m_constraints = new Vector();
	}

	public abstract Vector recognize();
	
	protected void addConstraint(Constraint c,Segment[] segments)
	{
		if(c!=null)
		{
			m_constraints.add(c);
			constraintsHelper.addCons2SegsAndRecogview(c,segments);
		}
	}
}