package dcad.model.constraint;
import dcad.model.geometry.segment.Segment;

public abstract class RelativeConstraint extends Constraint
{
	protected Segment m_seg1 = null;
	protected Segment m_seg2 = null;
	
	public RelativeConstraint(Segment seg1, Segment seg2,int category,boolean promoted )
	{
		this.m_seg1 = seg1;
		this.m_seg2 = seg2;
		setM_category(category);
		setPromoted(promoted);
		clearPoints();
	}

	public Segment getM_seg1()
	{
		return m_seg1;
	}

	public Segment getM_seg2()
	{
		return m_seg2;
	}
	
	public void delete()
	{
		m_seg1.removeConstraint(this);
		m_seg2.removeConstraint(this);
	}
}