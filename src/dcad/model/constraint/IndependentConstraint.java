package dcad.model.constraint;

import dcad.model.geometry.segment.Segment;

public abstract class IndependentConstraint extends Constraint
{
	protected Segment m_seg = null;
	
	protected IndependentConstraint(Segment seg,int category,boolean promoted)
	{
		this.m_seg=seg;
		setM_category(category);
		setPromoted(promoted);
		clearPoints();
	}

	public void delete()
	{
		m_seg.removeConstraint(this);
	}

	public Segment getM_seg()
	{
		return m_seg;
	}
	
}