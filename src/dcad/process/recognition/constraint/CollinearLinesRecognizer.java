package dcad.process.recognition.constraint;

import java.util.Vector;

import dcad.model.constraint.angle.ParallelSegConstraint;
import dcad.model.constraint.collinearity.*;
import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;

public class CollinearLinesRecognizer extends RelConstraintRecognitionScheme
{
	
	public CollinearLinesRecognizer(Segment seg1, Segment seg2)
	{
		super(seg1, seg2);
	}
	
	public Vector recognize()
	{
		if((m_seg1 instanceof SegLine) && (m_seg2 instanceof SegLine))
		{
			SegLine seg1=(SegLine)m_seg1;
			SegLine seg2=(SegLine)m_seg2;
			if(constraintsHelper.getUniquePointsForConnectedLines(seg1,seg2).size()==0)
			{
				//If lines are collinear and the constraint has not been added yet, remove old parallel segment and collinear points constraints and add a collinear lines constraint 
				AnchorPoint start1=seg1.getM_start();
				AnchorPoint end1=seg1.getM_end();
				AnchorPoint start2=seg2.getM_start();
				AnchorPoint end2=seg2.getM_end();
				if(constraintsHelper.getConstraintBetween2Segments(seg1,seg2,CollinearLinesConstraint.class)==null)
				{
					if(constraintsHelper.areSlopesEqual(start1,end1,start2,end1,false) && constraintsHelper.areSlopesEqual(start1,end1,end2,end1,false))
					{
						//Remove all other constraints that are not required now.
						AnchorPoint v[];
						//If there is no collinear line constraint added yet, remove all other constraints that will be no more required
						ParallelSegConstraint pc=(ParallelSegConstraint)constraintsHelper.getConstraintBetween2Segments(seg1,seg2,ParallelSegConstraint.class);
						if(pc!=null)
							pc.remove();

/*						CollinearPointsConstraint c;
						v=new AnchorPoint[]{start1,end1,start2};
						c=(CollinearPointsConstraint)constraintsHelper.doesConstraintAlreadyExist(start1,CollinearPointsConstraint.class,v);
						if(c!=null)
							c.remove();
						v=new AnchorPoint[]{start1,end1,end2};
						c=(CollinearPointsConstraint)constraintsHelper.doesConstraintAlreadyExist(start1,CollinearPointsConstraint.class,v);
						if(c!=null)
							c.remove();
						v=new AnchorPoint[]{start2,end2,start1};
						c=(CollinearPointsConstraint)constraintsHelper.doesConstraintAlreadyExist(start2,CollinearPointsConstraint.class,v);
						if(c!=null)
							c.remove();
						v=new AnchorPoint[]{start2,end2,end1};
						c=(CollinearPointsConstraint)constraintsHelper.doesConstraintAlreadyExist(start2,CollinearPointsConstraint.class,v);
						if(c!=null)
							c.remove();*/

						//Add collinear lines constraint
						CollinearLinesConstraint cc = new CollinearLinesConstraint(seg1,seg2,Constraint.HARD,false);
						addConstraint(cc,new Segment[]{seg1,seg2});
					}
				}
			}
		}
		return m_constraints;
	}
}