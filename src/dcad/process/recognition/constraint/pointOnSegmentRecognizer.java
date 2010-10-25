package dcad.process.recognition.constraint;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.length.LineMidPointConstraint;
import dcad.model.constraint.pointOnSegment.*;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.SegmentPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.SegPoint;
import dcad.model.geometry.segment.Segment;
import dcad.ui.drawing.DrawingView;
import dcad.ui.main.MainWindow;
import dcad.util.GConstants;
import dcad.util.GMethods;
import dcad.util.GVariables;

public class pointOnSegmentRecognizer extends RelConstraintRecognitionScheme
{
	public pointOnSegmentRecognizer(Segment seg1, Segment seg2)
	{
		super(seg1, seg2);
	}

	protected void init(Segment seg1, Segment seg2)
	{
		super.init(seg1, seg2);
	}

	public Vector recognize()
	{
		// when segment 1 is a Point Segment
		if((m_seg1 instanceof SegPoint) && (m_seg2 instanceof SegPoint))
		{
			//Do nothing. This is not possible because if two points are overlapping, they'll get merged
		}
		else if((m_seg1 instanceof SegPoint) && (m_seg2 instanceof SegLine))
		{
			getConstraints((SegLine)m_seg2, (SegPoint)m_seg1);
		}
		else if((m_seg1 instanceof SegPoint) && (m_seg2 instanceof SegCircleCurve))
		{
			getConstraints((SegCircleCurve)m_seg2, (SegPoint)m_seg1);
		}
		
		//Segment 1 is line
		else if((m_seg1 instanceof SegLine) && (m_seg2 instanceof SegPoint))
		{
			getConstraints((SegLine)m_seg1, (SegPoint)m_seg2);
		}
		else if((m_seg1 instanceof SegLine) && (m_seg2 instanceof SegLine))
		{
			getConstraints((SegLine)m_seg1, (SegLine)m_seg2);
		}
		else if((m_seg1 instanceof SegLine) && (m_seg2 instanceof SegCircleCurve))
		{
			getConstraints((SegCircleCurve)m_seg2, (SegLine)m_seg1);
		}

		//Segment 1 is curve
		else if((m_seg1 instanceof SegCircleCurve) && (m_seg2 instanceof SegPoint))
		{
			getConstraints((SegCircleCurve)m_seg1, (SegPoint)m_seg2);
		}
		else if((m_seg1 instanceof SegCircleCurve) && (m_seg2 instanceof SegLine))
		{
			getConstraints((SegCircleCurve)m_seg1, (SegLine)m_seg2);
		}
		else if((m_seg1 instanceof SegCircleCurve) && (m_seg2 instanceof SegCircleCurve))
		{
			getConstraints((SegCircleCurve)m_seg1, (SegCircleCurve)m_seg2);
		}

		return m_constraints;
	}
	
	private void getConstraints(SegLine seg1, SegPoint seg2)
	{
		if( seg1.getM_start() != seg2.getM_pt() && seg1.getM_end() != seg2.getM_pt() )
		//if( constraintsHelper.arePointsUnique(new AnchorPoint[]{seg1.getM_start(),seg1.getM_end(),seg2.getM_pt()}) )
			if(seg1.containsPt(seg2.getM_pt().getM_point()))
				addPointOnLineConstraint(seg1,seg2.getM_pt());
			
	}	
	
	private void getConstraints(SegLine seg1, SegLine seg2)
	{
		if(constraintsHelper.arePointsUnique(new AnchorPoint[]{seg1.getM_start(),seg1.getM_end(),seg2.getM_start(),seg2.getM_end()}))
		{
			if(seg1.containsPt(seg2.getM_start().getM_point()))
				addPointOnLineConstraint(seg1,seg2.getM_start());
			if(seg1.containsPt(seg2.getM_end().getM_point()))
				addPointOnLineConstraint(seg1,seg2.getM_end());
		}
	}
	
	private void getConstraints(SegCircleCurve seg1, SegPoint seg2)
	{
		if( seg1.getM_start() != seg2.getM_pt() && seg1.getM_end() != seg2.getM_pt()  && seg1.getM_center() != seg2.getM_pt() )
//		if(constraintsHelper.arePointsUnique(new AnchorPoint[]{seg1.getM_start(),seg1.getM_end(),seg1.getM_center(),seg2.getM_pt()}))
			if( seg1.containsPt(seg2.getM_pt().getM_point()) )
				addPointOnCircularCurve(seg1,seg2.getM_pt());
	}
	
	private void getConstraints(SegCircleCurve seg1, SegLine seg2)
	{
		if( seg1.getM_start() != seg2.getM_start() && seg1.getM_end() != seg2.getM_start()  && seg1.getM_center() != seg2.getM_start() )
//		if(constraintsHelper.arePointsUnique(new AnchorPoint[]{seg1.getM_start(),seg1.getM_end(),seg1.getM_center(),seg2.getM_start()}))
			if( seg1.containsPt(seg2.getM_start().getM_point()) )
				addPointOnCircularCurve(seg1,seg2.getM_start());
		if( seg1.getM_start() != seg2.getM_end() && seg1.getM_end() != seg2.getM_end()  && seg1.getM_center() != seg2.getM_end() )
//		if(constraintsHelper.arePointsUnique(new AnchorPoint[]{seg1.getM_start(),seg1.getM_end(),seg1.getM_center(),seg2.getM_end()}))
			if( seg1.containsPt(seg2.getM_end().getM_point()) )
				addPointOnCircularCurve(seg1,seg2.getM_end());
	}
	
	private void getConstraints(SegCircleCurve seg1, SegCircleCurve seg2)
	{
		if( seg1.getM_start() != seg2.getM_start() && seg1.getM_end() != seg2.getM_start()  && seg1.getM_center() != seg2.getM_start() )
//		if(constraintsHelper.arePointsUnique(new AnchorPoint[]{seg1.getM_start(),seg1.getM_end(),seg1.getM_center(),seg2.getM_start()}))
			if( seg1.containsPt(seg2.getM_start().getM_point()) )
				addPointOnCircularCurve(seg1,seg2.getM_start());
		if( seg1.getM_start() != seg2.getM_end() && seg1.getM_end() != seg2.getM_end()  && seg1.getM_center() != seg2.getM_end() )
//		if(constraintsHelper.arePointsUnique(new AnchorPoint[]{seg1.getM_start(),seg1.getM_end(),seg1.getM_center(),seg2.getM_end()}))
			if( seg1.containsPt(seg2.getM_end().getM_point()) )
				addPointOnCircularCurve(seg1,seg2.getM_end());
		if( seg1.getM_start() != seg2.getM_center() && seg1.getM_end() != seg2.getM_center()  && seg1.getM_center() != seg2.getM_center() )
//		if(constraintsHelper.arePointsUnique(new AnchorPoint[]{seg1.getM_start(),seg1.getM_end(),seg1.getM_center(),seg2.getM_center()}))
			if( seg1.containsPt(seg2.getM_center().getM_point()) )
				addPointOnCircularCurve(seg1,seg2.getM_center());
	}
	
	public void addPointOnCircularCurve(SegCircleCurve seg,AnchorPoint p)
	{
		Constraint tc = getPointOnCircularCurveConstraint(seg,p,Constraint.HARD,true);
		if(tc!=null)
			addConstraint(tc,new Segment[]{seg});

	}
	
	public static Constraint getPointOnCircularCurveConstraint(SegCircleCurve seg,AnchorPoint p,int category,boolean promoted)
	{
		AnchorPoint[] a = new AnchorPoint[]{seg.getM_start(),seg.getM_end(),seg.getM_center(),p};
		if(constraintsHelper.doesConstraintAlreadyExist(p,pointOnCircularCurveConstraint.class,a)==null) //There is no such constraint added previously
			return new pointOnCircularCurveConstraint(seg,p,Constraint.HARD,true);
		return null;
	}
	
	public void addPointOnLineConstraint(SegLine seg,AnchorPoint p)
	{
		AnchorPoint[] a = new AnchorPoint[]{seg.getM_start(),seg.getM_end(),p};
		Constraint POS=null,LMP=null;
		boolean LMPExists = false;
		boolean isEditMode = false; 
		DrawingView dv = MainWindow.getDv();
		
		if(GVariables.getDRAWING_MODE() == GConstants.DRAW_MODE){
			Stroke stk = dv.getCurrStroke();
			boolean isSegInCurrentStroke = false;
			boolean isPtInCurrentStroke = false;
			Vector segList = stk.getM_segList();
			Iterator iter = segList.iterator();
			while (iter.hasNext()){
				GeometryElement segm = (GeometryElement)iter.next();
				if(segm instanceof SegLine){
					SegLine segL = (SegLine) segm; 
					if(segL.equals(seg)){
						isSegInCurrentStroke = true;
					}
					
					if(segL.getSegEnd().equals(p) || segL.getSegStart().equals(p)){
						isPtInCurrentStroke = true;
					}
				}
				else if(segm instanceof SegCircleCurve){
					SegCircleCurve segCC = (SegCircleCurve) segm;
					if(segCC.getSegEnd().equals(p) || segCC.getSegStart().equals(p)){
						isPtInCurrentStroke = true;
					}
				}
			}
			
			if(isPtInCurrentStroke && (!isSegInCurrentStroke)){
				isEditMode = true;
			}
		}
		else if(GVariables.getDRAWING_MODE() == GConstants.EDIT_MODE){
			
			Vector m_highlightedElements = dv.getM_highlightedElements();
			if(dv.isM_elementDragged() && (m_highlightedElements.size() > 0)){
				Iterator iter = m_highlightedElements.iterator();
				while (iter.hasNext()){
					GeometryElement segm = (GeometryElement)iter.next();
				
					if(segm instanceof SegLine){
						/////System.out.println("Seg Line");
						/////System.out.println("************************** line");
						SegLine segL = (SegLine) segm; 
				
							if(segL.equals(seg)){
								isEditMode = true;
								break;
							}
							else{
								Vector parents = p.getAllParents();
		          				Iterator iter1 = parents.iterator();
		          				// if any of the point's parent is dragged
		          				while(iter1.hasNext()){
		          					GeometryElement segm1 = (GeometryElement)iter1.next();
		          					if(segm1 instanceof SegLine){
		          						SegLine segLi = (SegLine) segm1; 
		          						
		    							if(segLi.equals(segL)){
		    								isEditMode = true;
		    								break;
		    							}
		          					}
		          				}
							}
						
					}
					else if(segm instanceof SegCircleCurve){
						SegCircleCurve segCC = (SegCircleCurve) segm;
						if(segCC.getSegEnd().equals(p) || segCC.getSegStart().equals(p)){
							isEditMode = true;
							break;
						}
					}
					else{
						/////System.out.println("************************** point");
						AnchorPoint segPt = (AnchorPoint) segm;
          				if(p.equals(segPt)){
          					isEditMode = true;
							break;
          				}
					}
				}
				
			}
		}
		
		if(constraintsHelper.doesConstraintAlreadyExist(p,LineMidPointConstraint.class,a)==null) //There is no such constraint added previously
		{
			if((p.distance(seg.getM_middle()) < Constraint.MAX_ALLOWED_CONNECT_GAP * 2) && isEditMode)
			{
				LMP=new LineMidPointConstraint(seg,p,Constraint.HARD,true);
				addConstraint(LMP,new Segment[]{seg});
				
				//Remove the point on segment constraint added earlier
				Constraint c=constraintsHelper.doesConstraintAlreadyExist(p,pointOnLineConstraint.class,a);
				if(c!=null)
					c.remove();
			}
		}
		else
			LMPExists= true;
		
		if(!LMPExists && LMP==null)
		{
			if(constraintsHelper.doesConstraintAlreadyExist(p,pointOnLineConstraint.class,a)==null) //There is no such constraint added previously
			{
				POS=new pointOnLineConstraint(seg,p,Constraint.HARD,true);
				addConstraint(POS,new Segment[]{seg});
			}
		}
	}
}