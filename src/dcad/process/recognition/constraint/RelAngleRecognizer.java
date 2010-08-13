package dcad.process.recognition.constraint;

import java.util.Vector;

import javax.naming.ldap.HasControls;

import dcad.Prefs;
import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.angle.ParallelIndConstraint;
import dcad.model.constraint.angle.ParallelSegConstraint;
import dcad.model.constraint.angle.PerpendicularIndConstraint;
import dcad.model.constraint.angle.PerpendicularSegConstraint;
import dcad.model.constraint.angle.RelAngleConstraint;
import dcad.model.constraint.collinearity.CollinearLinesConstraint;
import dcad.model.constraint.length.EqualRelLengthConstraint;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.util.GMethods;

public class RelAngleRecognizer extends RelConstraintRecognitionScheme
{
	public static final double DEF_MAX_ANGLE_TOL = 5;
	public RelAngleRecognizer(Segment line1, Segment line2)
	{
		super(line1, line2);
	}

	protected void init(Segment line1, Segment line2)
	{
		super.init(line1, line2);
	}

	public Vector recognize()
	{
		SegLine line1 = (SegLine)m_seg1;
		SegLine line2 = (SegLine)m_seg2;
		if(constraintsHelper.getConstraintBetween2Segments(line1,line2,CollinearLinesConstraint.class)==null)
		{
			Constraint cons=null;
			if(constraintsHelper.areSlopesEqual(line1.getM_start(),line1.getM_end(),line2.getM_start(),line2.getM_end(),false))
				cons=getParallelSegmentsConstraint(line1, line2, Constraint.HARD,true);
			else if(constraintsHelper.areLinesPerpendicular(line1.getM_start(),line1.getM_end(),line2.getM_start(),line2.getM_end(),-1,false))
			{
				if(constraintsHelper.getUniquePointsForConnectedLines(line1,line2).size()!=0)
					cons=getPerpendicularSegmentsConstraint(line1, line2,Constraint.HARD,true);
			}
			if(cons!=null)
				addConstraint(cons,new Segment[]{m_seg1,m_seg2});
		}
		return m_constraints;
	}
	
	public static Constraint getParallelSegmentsConstraint(SegLine l1,SegLine l2,int category, boolean promoted)
	{
		Constraint c=constraintsHelper.getConstraintBetween2Segments(l1,l2,ParallelSegConstraint.class);
		if(c==null)
			return new ParallelSegConstraint(l1, l2, category,promoted);
		if(!promoted)
			c.setPromoted(promoted);
		return null;
	}
	
	public static Constraint getPerpendicularSegmentsConstraint(SegLine l1,SegLine l2,int category, boolean promoted)
	{
		Constraint c=constraintsHelper.getConstraintBetween2Segments(l1,l2,PerpendicularSegConstraint.class);
		if(c==null)
		return new PerpendicularSegConstraint(l1, l2, category,promoted);
		if(!promoted)
			c.setPromoted(promoted);
		return null;
	}
/*
	private Constraint addConstraint(Segment line1, Segment line2, int consType)
	{
		Constraint cons = null;
		switch (consType)
		{
		case Constraint.REL_ANGLE_PARALLEL:
			if(constraintsHelper.getConstraintBetween2Segments(line1,line2,CollinearLinesConstraint.class)!=null)
				return cons;
			if(Prefs.getPromotionPreference())
			{
			}
			break;

		case Constraint.REL_ANGLE_PERPENDICULAR:
			if(Prefs.getPromotionPreference())
			{
				if(!(constraintsHelper.getConstraintBetween2Segments(line1,line2,PerpendicularSegConstraint.class)!=null))// Not already added
				{
					if (!  // It is not the case that one is horizontal and the other vertical
							(  line1.getConstraintByType(ParallelIndConstraint.class).size()!=0   
									&&   line2.getConstraintByType(PerpendicularIndConstraint.class).size()!=0
									||    line2.getConstraintByType(ParallelIndConstraint.class).size()!=0  
									&&   line1.getConstraintByType(PerpendicularIndConstraint.class).size()!=0  ) 
						)
					{
						cons = new PerpendicularSegConstraint((SegLine)line1, (SegLine)line2, Constraint.HARD,true);
						if(cons.getPoints().size()==0)	//If constraint was added before snapping the points, it won't have points in interconnection, so discard this constraint
							cons=null;
					}
				}
			}
			break;

		default:
			break;
		}
	
		if (cons != null)
			addConstraint(cons,new Segment[]{line1,line2});
		return cons;
	}*/
}