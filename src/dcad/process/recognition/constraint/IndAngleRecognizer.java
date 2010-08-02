package dcad.process.recognition.constraint;

import java.util.Vector;

import dcad.Prefs;
import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.angle.ParallelIndConstraint;
import dcad.model.constraint.angle.PerpendicularIndConstraint;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.util.GMethods;

public class IndAngleRecognizer extends IndConstraintRecognitionScheme
{
	public static final double DEF_MAX_ANGLE_TOL = 5;
	public IndAngleRecognizer(Segment seg)
	{
		super(seg);
	}

	public Vector recognize()
	{
		if(m_seg instanceof SegLine)
		{
			// this segment is line
			SegLine line = (SegLine)m_seg;
			double seg_angle = line.getM_angle();
			
			Constraint cons = null;
			
			// compute angles such that angles are positive.
			if(seg_angle < 0) seg_angle += 360;

			if((seg_angle <= Prefs.getIndAngleLimit()) || (seg_angle >=360-Prefs.getIndAngleLimit())) 
				cons = addParallelConstraint(line, seg_angle,Constraint.HARD,true);
			else if((seg_angle <= (90+Prefs.getIndAngleLimit())) && (seg_angle >= (90-Prefs.getIndAngleLimit())))
				cons = addPerpendicularConstraint(line, seg_angle,Constraint.HARD,true);
			else if ((seg_angle <= (180+Prefs.getIndAngleLimit())) && (seg_angle >= (180-Prefs.getIndAngleLimit())))
				cons = addParallelConstraint(line, seg_angle,Constraint.HARD,true);
			else if((seg_angle <= (270+Prefs.getIndAngleLimit())) && (seg_angle >= (270-Prefs.getIndAngleLimit())))
				cons = addPerpendicularConstraint(line, seg_angle,Constraint.HARD,true);
			else
				;
			addConstraint(cons,new Segment[]{m_seg});
		}
		return m_constraints;
	}			

	public static Constraint addParallelConstraint(SegLine seg, double angle,int category,boolean promoted)
	{
		Vector v=constraintsHelper.getConstraintsByType(seg.getM_constraints(),ParallelIndConstraint.class);
		if(v.size()==0)
			return new ParallelIndConstraint(seg, angle,category,promoted);
		if(!promoted)
			((Constraint)v.get(0)).setPromoted(false);
		return null;
	}
	
	public static Constraint addPerpendicularConstraint(SegLine seg, double angle,int category,boolean promoted)
	{	
		Vector v=constraintsHelper.getConstraintsByType(seg.getM_constraints(),PerpendicularIndConstraint.class);
		if(v.size()==0)
			return new PerpendicularIndConstraint(seg, angle,category,promoted);
		if(!promoted)
			((Constraint)v.get(0)).setPromoted(false);
		return null;
	}
}
