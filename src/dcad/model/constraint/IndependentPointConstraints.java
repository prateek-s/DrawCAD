package dcad.model.constraint;

import dcad.model.geometry.AnchorPoint;

public abstract class IndependentPointConstraints extends Constraint
{
	
	public IndependentPointConstraints(AnchorPoint[] argPoints,int category,boolean promoted)
	{
		super();
		setM_category(category);
		setPromoted(promoted);
		for(int i=0;i<argPoints.length;i++)
			addPoint(argPoints[i]);
	}

	public void delete()
	{
	}
	
}