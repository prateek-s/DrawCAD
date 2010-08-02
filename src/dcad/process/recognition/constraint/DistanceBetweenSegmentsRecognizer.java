package dcad.process.recognition.constraint;
import java.util.Vector;

import javax.swing.JOptionPane;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.angle.ParallelSegConstraint;
import dcad.model.constraint.angle.PerpendicularSegConstraint;
import dcad.model.constraint.collinearity.CollinearLinesConstraint;
import dcad.model.constraint.connect.lineCircularCurveTangencyConstraint;
import dcad.model.constraint.distance.DistanceBetweenCircularCurveAndLineConstraint;
import dcad.model.constraint.distance.DistanceBetweenCircularCurvesConstraint;
import dcad.model.constraint.distance.DistanceBetweenCollinearLinesConstraint;
import dcad.model.constraint.distance.DistanceBetweenParallelLinesConstraint;
import dcad.model.constraint.distance.DistanceBetweenPerpendicularLinesConstraint;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.ui.main.MainWindow;
import dcad.util.GConstants;

public class DistanceBetweenSegmentsRecognizer extends RelConstraintRecognitionScheme
{
	public DistanceBetweenSegmentsRecognizer(Segment seg1, Segment seg2)
	{
		super(seg1, seg2);
	}

	public Vector recognize()
	{
		return m_constraints;
	}
	
	/**Function to add distance between two lines 
	 * first make them parallel
	 * and then set distance between them 
	 * @author Sunil Kumar
	 */

	public static Constraint getDistanceBetweenLinesConstraint(SegLine l1,SegLine l2,double distance,int category, boolean promoted){
		
	/*	if(constraintsHelper.getConstraintBetween2Segments(l1,l2,CollinearLinesConstraint.class)!=null)
		{
			DistanceBetweenCollinearLinesConstraint c=(DistanceBetweenCollinearLinesConstraint)constraintsHelper.getConstraintBetween2Segments(l1,l2,DistanceBetweenCollinearLinesConstraint.class);
			if(c!=null)
				c.remove();
			return new DistanceBetweenCollinearLinesConstraint(l1, l2,distance, category,promoted);
		}
		else if (constraintsHelper.getConstraintBetween2Segments(l1,l2,ParallelSegConstraint.class)!=null)
		{
			DistanceBetweenParallelLinesConstraint c=(DistanceBetweenParallelLinesConstraint)constraintsHelper.getConstraintBetween2Segments(l1,l2,DistanceBetweenParallelLinesConstraint.class);
			if(c!=null)
				c.remove();
			return new DistanceBetweenParallelLinesConstraint(l1, l2,distance, category,promoted);
		}
	 else if(constraintsHelper.getConstraintBetween2Segments(l1,l2,PerpendicularSegConstraint.class)==null)
		{
			if(constraintsHelper.areLinesPerpendicular(l1.getM_start(),l1.getM_end(),l2.getM_start(), l2.getM_end(),-1,false))
			{
				DistanceBetweenPerpendicularLinesConstraint c=(DistanceBetweenPerpendicularLinesConstraint)constraintsHelper.getConstraintBetween2Segments(l1,l2,DistanceBetweenPerpendicularLinesConstraint.class);
				if(c!=null)
					c.remove();
				return new DistanceBetweenPerpendicularLinesConstraint(l1, l2,distance, category,promoted);
			}
		}*/
	
		//added on 27-05-10
	
		
		
		if(constraintsHelper.getConstraintBetween2Segments(l1,l2,ParallelSegConstraint.class)!=null){
				DistanceBetweenParallelLinesConstraint c=(DistanceBetweenParallelLinesConstraint)constraintsHelper.getConstraintBetween2Segments(l1,l2,DistanceBetweenParallelLinesConstraint.class);
				if(c!=null)
					c.remove();
				return new DistanceBetweenParallelLinesConstraint(l1, l2,distance, category,promoted);
		}
		else{
			JOptionPane.showMessageDialog(MainWindow.getDv(),"Please first make two lines parallel to each other");
		}
		return null;
	}
	
	public static Constraint getDistanceBetweenCircularCurveAndLineConstraint(SegLine  l1,SegCircleCurve l2,double distance,int category, boolean promoted)
	{
		if(constraintsHelper.getConstraintBetween2Segments(l1,l2,lineCircularCurveTangencyConstraint.class)==null)
		{
			DistanceBetweenCircularCurveAndLineConstraint c = (DistanceBetweenCircularCurveAndLineConstraint) constraintsHelper.getConstraintBetween2Segments(l1,l2,DistanceBetweenCircularCurveAndLineConstraint.class);
			if(c!=null)
				c.remove();
			return new DistanceBetweenCircularCurveAndLineConstraint(l1,l2,distance,category,promoted);
			
		}
		return null;
	}
	
/*	public static Constraint getDistanceBetween2PointsConstraint()
	{
		
	}*/
	
	public static Constraint getDistanceBetweenCurvesConstraint(SegCircleCurve c1,SegCircleCurve c2,double distance,int category, boolean promoted){
		
		DistanceBetweenCircularCurvesConstraint c=(DistanceBetweenCircularCurvesConstraint)constraintsHelper.getConstraintBetween2Segments(c1,c2,DistanceBetweenCircularCurvesConstraint.class);
		if(c!=null)
			c.remove();
		return new DistanceBetweenCircularCurvesConstraint(c1, c2,distance, category,promoted);
	
	}
}