package dcad.model.geometry;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.connect.lineCircularCurveTangencyConstraint;
import dcad.model.constraint.pointOnSegment.pointOnCircularCurveConstraint;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.model.marker.Marker;
import dcad.process.ProcessManager;
import dcad.process.recognition.RecognitionManager;
import dcad.process.recognition.constraint.ConstraintRecogManager;
import dcad.process.recognition.constraint.IndConstraintRecognizer;
import dcad.process.recognition.constraint.RelConstraintRecognizer;
import dcad.process.recognition.constraint.pointOnSegmentRecognizer;
import dcad.process.recognition.constraint.tangencyRecognizer;
import dcad.process.recognition.segment.CircularCurveRecognizer;
import dcad.process.recognition.segment.LineRecognizer;
import dcad.process.recognition.segment.SegmentRecognitionScheme;
import dcad.process.recognition.segment.SegmentRecognizer;
import dcad.ui.drawing.DrawingView;
import dcad.util.GMethods;
import dcad.util.GVariables;
import dcad.util.Maths;
import dcad.ui.main.MainWindow;
import dcad.ui.main.ToolBar;
import dcad.model.geometry.*;

public class Stroke extends GeometryElement
{
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_MARKER = 1;
	
	

	// added on 29-05-10
	
	private static boolean isStrokeConverted = false;
	
	public boolean isStrokeConverted() {
		return isStrokeConverted;
	}

	public void setStrokeConverted(boolean isStrokeConverted) {
		this.isStrokeConverted = isStrokeConverted;
	}

	private static int strokeConvertedTo = -1;
	
	public int getStrokeConvertedTo() {
		return strokeConvertedTo;
	}

	public void setStrokeConvertedTo(int strokeConvertedTo) {
		this.strokeConvertedTo = strokeConvertedTo;
	}

	/** this stores the raw data points */
	private Vector m_ptList;
	
	/** Stroke is a aggregation of segement. every segment is a part of some stroke. */
	private Vector<Segment> m_segList = new Vector();

	/** indexes of the segment points detected in the stroke after preprocessing */
	private Vector m_segPtList = new Vector();
	
	/**
	 * check whether the stroke is a normal one or a some other type like marker etc
	 */
	private int m_type = TYPE_NORMAL;
	
	/**
	 * point-number of the end-point of the previous stroke.
	 */
	private int m_prevEnd;
	
	public Stroke()
	{
		this.m_ptList = new Vector();
		setM_color(GVariables.RAW_STROKE_COLOR);
	}
	
	/**
	 * Called by Track. This feeds Stroke and stores (x,y,time) into a list.
	 * @param x
	 * @param y
	 * @param time
	 */
	public void addPoint(int x, int y, long time)
	{
		PixelInfo pInfo = new PixelInfo(x, y, time); 
		m_ptList.add(pInfo);
	}
	
	
	
	/*************************   Drawing Functions   *****************************/
	
	/**
	 * Draw the segment points and the segments associated with this stroke.
	 */
	public void draw(Graphics gc)
	{
		// only if the stroke is enabled show it along with its segment points
		//p: needs m_SegPtList
		if(isEnabled())
		{
			if(m_type == TYPE_NORMAL)
				drawSegPts(gc);
			drawSegments(gc);
		}
	}
	/**
	 * Draw graphics pixel-by-pixel.Uses raw data points (not part of Graphics)
	 * @param gc
	 */
	public void drawRaw(Graphics gc)
	{
		Color prevColor = gc.getColor();
		gc.setColor(getM_color());
		
		PixelInfo prevPt = null;
		Iterator iter = m_ptList.iterator(); /* m_ptList is filled by... ?*/
		if(iter.hasNext())
		{
			prevPt = ((PixelInfo)iter.next());
			gc.drawLine(prevPt.x, prevPt.y, prevPt.x, prevPt.y);
		}
		
		while (iter.hasNext())
		{
			PixelInfo nextPt = ((PixelInfo)iter.next());
			gc.drawLine(prevPt.x, prevPt.y, nextPt.x, nextPt.y);
			prevPt = nextPt;
		}
		
		gc.setColor(prevColor);
	}
	
	/**
	 * Does nothing? Since sp.draw calls an empty method in GeometricElement
	 * @param gc
	 */
	public void drawSegPts(Graphics gc)
	{
		Iterator iter = m_segPtList.iterator();
		while (iter.hasNext())
		{
			SegmentPoint sp = (SegmentPoint) iter.next();
			sp.draw(gc);
		}
	}

	/**
	 * Display all the recognized segments
	 * Draw all the segments associated with this stroke.
	 */
	public void drawSegments(Graphics gc)
	{
		if(m_segList != null)
		{
			Iterator iter = m_segList.iterator();
			while (iter.hasNext())
			{
				Segment seg = (Segment) iter.next();
				seg.draw(gc); //same
			}
		}
	}
	
	public int user_given = 0 ; //user says which segment this should be. 
	
	/*************************   Segment Functions   *****************************/

	/**
	 * Recognizes all the segments of this stroke. 
	 */
	
		public Vector<Segment> recognizeSegments(SegmentRecognizer segRecog,Vector segptlist) throws Exception
		{

		deleteSegments();
		
		m_prevEnd = 0;
		
		// Stroke segmentation already performed.
	
		Iterator iter = segptlist.iterator();

		// get the first segment point. There will be atleast one point in every stroke (in the case of a point)
		SegmentPoint start = null;
		if(iter.hasNext())
		{
			start = (SegmentPoint)iter.next();
		}

 		if(start != null)
		{
			// check if there is only one segment point in the segment point list.
			if(!iter.hasNext())
			{
				// only one point detected, The stroke is a point
				addSegment(detectSeg(segRecog, start.getM_point(), start.getM_point()));
				/////System.out.println("******** "+m_prevEnd);
			}
			else
			{
				// find segments. NOTE that segment points are repeated for the adjacent segments
				while (iter.hasNext())
				{
					// get the index in the segmentPoint list.
					SegmentPoint end = (SegmentPoint)iter.next();
					
					addSegment(detectSeg(segRecog, start.getM_point(), end.getM_point()));
					/////System.out.println("******** "+m_prevEnd);

					// make pointB as the start of the new segment
					start = end;
				}
			}
		}

	//	}
 		
 		return m_segList ;
 		
	}

		public Vector<Segment> temp_recognizeSegments(SegmentRecognizer segRecog,Vector segptlist) throws Exception
		{
		Vector<Segment> segments = new Vector() ;
		
		m_prevEnd = 0;
		
		// Stroke segmentation already performed.
	
		Iterator iter = segptlist.iterator();

		// get the first segment point. There will be atleast one point in every stroke (in the case of a point)
		SegmentPoint start = null;
		if(iter.hasNext())
		{
			start = (SegmentPoint)iter.next();
		}

 		if(start != null)
		{
			// check if there is only one segment point in the segment point list.
			if(!iter.hasNext())
			{
				// only one point detected, The stroke is a point
				segments.add(detectSeg(segRecog, start.getM_point(), start.getM_point()));
				/////System.out.println("******** "+m_prevEnd);
			}
			else
			{
				// find segments. NOTE that segment points are repeated for the adjacent segments
				while (iter.hasNext())
				{
					// get the index in the segmentPoint list.
					SegmentPoint end = (SegmentPoint)iter.next();
					
					Segment seg = detectSeg(segRecog , start.getM_point(), end.getM_point());
					segments.add(seg);
					/////System.out.println("******** "+m_prevEnd);

					// make pointB as the start of the new segment
					start = end;
				}
			}
		}

	//	}
 		
 		return segments ;
 		
	}
		
	
	private Segment detectSeg(SegmentRecognizer segRecog, Point2D startPt, Point2D endPt) throws Exception
	{

		List ptList = getM_ptList().subList(m_prevEnd , getM_ptList().size());
		int start = ptList.indexOf(startPt) + m_prevEnd;
		int end = ptList.indexOf(endPt) + m_prevEnd;
		
		m_prevEnd = end;
		
		// get the pixels with the segment
		double[][] seg = getWindowElemAs2DMatrix_Double(start, end);
		
		if(user_given > 0) 
		{
			Segment to_return = null ;
			switch (user_given)
			{
			
			case Segment.POINT:
				//return ;

			case Segment.LINE:
			//basically, just call isLine() here.. 
				LineRecognizer m_lineRecog = new LineRecognizer(seg);
				m_lineRecog.approximate() ; //important side-effect. Sigh
				to_return = m_lineRecog.getSegment() ;
				to_return.setM_rawStartIdx(start);
				to_return.setM_rawEndIdx(end);
				to_return.setM_parentStk(this); 
				
				if(to_return instanceof SegCircleCurve)
					((SegCircleCurve)to_return).updateDetails();
				break ;
			
			case Segment.CIRCLE:
				//call isCircle
				segRecog.isCircle() ;
				to_return = segRecog.m_circleCurveRecog.getSegment() ;
			//	CircularCurveRecognizer m_circleCurveRecog = new CircularCurveRecognizer(seg);
			//	to_return = m_circleCurveRecog.getSegment() ;
				to_return.setM_rawStartIdx(start);
				to_return.setM_rawEndIdx(end);
				to_return.setM_parentStk(this); 
				
				if(to_return instanceof SegCircleCurve)
					((SegCircleCurve)to_return).updateDetails();
				break ;
			case Segment.ELLIPSE:
				//return m_ellipCurveRecog;
				break ;
			case Segment.GENERAL:
			//	return null;
				break ;
			}
			
			return to_return ;
		}
		
		if(seg != null)
		{
			SegmentRecognitionScheme brs = segRecog.recognizeSegment(seg);
			if(brs == null) throw new Exception("Unable to identify the segment ("+start+", "+end+")");
			else
			{
				// retrieve the segment  
				Segment recogSeg = brs.getSegment(); 	//basicRecog.getSegmentApprox();
				
				// set the basic properties of the segment the segment would contain its type.
				recogSeg.setM_rawStartIdx(start);
				recogSeg.setM_rawEndIdx(end);
				recogSeg.setM_parentStk(this);
				
				//Update details of circle. This was being done in the draw function when the circle was drawn the first time. This created several problems.
				//So, included it here on 8-3-2008
				if(recogSeg instanceof SegCircleCurve)
					((SegCircleCurve)recogSeg).updateDetails();
				return recogSeg;
				
			}
		}
		else
		{
			return null;// This should not happen TODO
		}
	}

	
	public void addSegment(Segment seg)
	{
//		// set the ID for the segment.
//		seg.setM_strId(this.getM_strId()+"_"+Integer.toString(m_segCounter++));
		
		// store the segment in the segment list. TODO: what if null is returned
		m_segList.add(seg);
	}

	public void removeSegment(Segment seg)
	{
		if(m_segList.contains(seg))
		{
			//seg.delete();
			m_segList.remove(seg);
		}
		if(m_segList.size() == 0)
		{
			// remove the stroke all togeather
			DrawingView dv = GMethods.getCurrentView();
			dv.getM_drawData().removeStroke(this);
		}
	}
	
	public void deleteSegments()
	{
		Iterator iter = m_segList.iterator();
		while (iter.hasNext())
		{
			Segment seg = (Segment) iter.next();
			iter.remove();
			seg.delete();
		}
	}


	
	/*************************   Constraint Functions   *****************************/
	//WHy is this even here? Segment class a better place ?

	/**
	 * Recognizes all the Independent constraints of the segments of this stroke.
	 */
	public Vector recognizeConstraints(IndConstraintRecognizer indConsRecog)
	{
		// to store all the constraints
		Vector constraints = new Vector();
		
		// do this for all the segments of this stroke
		Iterator iter = m_segList.iterator();
		while (iter.hasNext())
		{
			Segment seg = (Segment) iter.next();
			constraints.addAll(indConsRecog.recognizeConstraints(seg));
		}
	
		return constraints;
	}
	
	/**
	 * Recognizes all the Relative constraints of the segments of this stroke.
	 */
	public Vector recognizeConstraints(RelConstraintRecognizer relConsRecog)
	{
		// to store all the constraints
		Vector constraints = new Vector();
		
		// do this for all the segments of this stroke
		int segCount = m_segList.size();
		for (int i = 0; i < segCount; i++)
		{
			Segment seg = (Segment) m_segList.get(i);
			for (int j = i+1; j < segCount; j++)
			{
				Segment newSeg = (Segment)m_segList.get(j);
				constraints.addAll(relConsRecog.recognizeConstraints(seg, newSeg));
			}
		}
		
		return constraints;
	}
	
	
	/**
	 * Recognizes all the relative constraints of the segments of this stroke with the another stroke.
	 */
	public Vector recognizeConstraints(Stroke stroke, RelConstraintRecognizer consRecog)
	{
		// to store all the constraints
		Vector constraints = new Vector();
		
		if(!this.equals(stroke))
		{
			// do this for all the segments of this stroke
			Iterator iter = m_segList.iterator();
			while (iter.hasNext())
			{
				Segment seg = (Segment) iter.next();
				Iterator iterator = stroke.getM_segList().iterator();
				while (iterator.hasNext())
				{
					Segment newSeg = (Segment) iterator.next();
					constraints.addAll(consRecog.recognizeConstraints(seg, newSeg));
				}
			}
		}

		return constraints;
	}

	public Vector recognizeConnectConstraints(Vector strokeList)
	{
		RecognitionManager recogMan = ProcessManager.getInstance().getRecogManager();
		ConstraintRecogManager consRecogMan = recogMan.getConstraintRecogManager();
		RelConstraintRecognizer relConsRecog = consRecogMan.getRelConsRecog();
		
		Vector constraints = new Vector();
		int strkCount = strokeList.size();
		strkCount--; // last stroke is this stroke itself.
		
		for (int i = 0; i < strkCount; i++)
		{
			Stroke oldStroke = (Stroke) strokeList.get(i);
			if(oldStroke.isEnabled())
			{
				try
				{
					if(!this.equals(oldStroke))
					{
						// do this for all the segments of this stroke
						Iterator iter = m_segList.iterator();
						while (iter.hasNext())
						{
							Segment seg = (Segment) iter.next();
							Iterator iterator = oldStroke.getM_segList().iterator();
							while (iterator.hasNext())
							{
								Segment newSeg = (Segment) iterator.next();
								constraints.addAll(relConsRecog.recognizeConnectConstraints(seg, newSeg));
							}
						}
					}
				} 
				catch (Exception e)
				{
					e.printStackTrace();
					// TODO: handle exception , may be re-segment
					JOptionPane.showMessageDialog(null, "Error Occured in recognize constraints: "+e.getMessage());
				}
			}
		}

		return constraints;
	}
	
	public Vector recognizeStrokeConstraints(Vector strokeList)
	{
		
		Vector constraints = new Vector();
		int strkCount = strokeList.size();
//		strkCount--;
		SegLine l;
		SegCircleCurve c;

		for(int i=0;i<m_segList.size();i++)
		{
			Segment newSeg = (Segment)m_segList.get(i);
			if(newSeg instanceof SegCircleCurve)
			{
				c = (SegCircleCurve)newSeg;
				Vector rowPoints = newSeg.getRawPoints(); 
					
				for(int j=0;j<strkCount;j++)
				{
					Stroke oldStroke = (Stroke) strokeList.get(j);
					if(oldStroke.isEnabled())
					{
						Vector oldSegments = oldStroke.getM_segList();
						for(int k=0;k<oldSegments.size();k++)
						{
							Segment oldSeg = (Segment)oldSegments.get(k);
							if(oldSeg instanceof SegLine)
							{
								l = (SegLine)oldSeg;
								if
								(
									constraintsHelper.getConstraintBetween2Segments(c,l,lineCircularCurveTangencyConstraint.class) == null
//									&& constraintsHelper.arePointsUnique(new AnchorPoint[]{l.getM_start(),l.getM_end(),c.getM_start(),c.getM_end(),c.getM_center()})
									
								)
								{
									Constraint cons = findAndAddLineCircularCurveTangency((SegLine)oldSeg,(SegCircleCurve)newSeg,rowPoints);
									if(cons!=null)
										constraints.add(cons);
									else //See if this is point on segment constraint
									{
										AnchorPoint ap = null;
										//Is the start point on the arc ?
										if(this.isAnchorPointOnStroke(l.getM_start(),rowPoints))
											ap = l.getM_start();
										if(ap!=null && ap!=c.getM_start() && ap!=c.getM_end() && ap!=c.getM_center())
										{
											cons = pointOnSegmentRecognizer.getPointOnCircularCurveConstraint(c,ap,Constraint.HARD,true);
											if(cons!=null)
											{
												constraintsHelper.addCons2SegsAndRecogview(cons,new Segment[]{c});
												constraints.add(cons);
											}
										}
										ap = null;
										//Is the end point on the arc ?
										if(this.isAnchorPointOnStroke(l.getM_end(),rowPoints))
											ap = l.getM_end();
										if(ap!=null && ap!=c.getM_start() && ap!=c.getM_end() && ap!=c.getM_center())
										{
											cons = pointOnSegmentRecognizer.getPointOnCircularCurveConstraint(c,ap,Constraint.HARD,true);
											if(cons!=null)
											{
												constraintsHelper.addCons2SegsAndRecogview(cons,new Segment[]{c});
												constraints.add(cons);
											}
										}
									}
								}
							
							}
							else if(oldSeg instanceof SegCircleCurve)
							{
								
							}
						}
					}
				}
			}
		}

		
		return constraints;
	}
	
	public boolean isAnchorPointOnStroke(AnchorPoint ap,Vector rowPoints)
	{
		int size = rowPoints.size();
		for(int i=0;i<size;i++)
		{
			///System.out.println(ap.distance((Point2D)rowPoints.get(i)));
			///System.out.println((Point2D)rowPoints.get(i));
			if(ap.distance((Point2D)rowPoints.get(i)) < Constraint.MAX_ALLOWED_CONNECT_GAP)
				return true;
		}
		return false;
	}
	
	
	public Vector recognizeAllConstraints(Vector strokeList)
	{
//		///System.out.println("Stroke.recognizeAllConstraints()");
		RecognitionManager recogMan = ProcessManager.getInstance().getRecogManager();
		ConstraintRecogManager consRecogMan = recogMan.getConstraintRecogManager();
		RelConstraintRecognizer relConsRecog = consRecogMan.getRelConsRecog();
		IndConstraintRecognizer indConsRecog = consRecogMan.getIndConsRecog();
		
		Vector constraints = new Vector();
		
		// Find all the constraints within this stroke
		// All Independent constraints for the segments of THIS STROKE.
		constraints.addAll(this.recognizeConstraints(indConsRecog));
		// All relative constraints between the segments of THIS STROKE.
		constraints.addAll(this.recognizeConstraints(relConsRecog));
		
		
		//Find all constraints between this and old strokes
		int strkCount = strokeList.size();
		strkCount--; 	// last stroke is this stroke itself.
		for (int i = 0; i < strkCount; i++)
		{
			Stroke oldStroke = (Stroke) strokeList.get(i);
			if(oldStroke.isEnabled())
				constraints.addAll(this.recognizeConstraints(oldStroke, relConsRecog));
		}

		constraints.addAll(recognizeStrokeConstraints(strokeList));

		return constraints;
	}

	Constraint findAndAddLineCircularCurveTangency(SegLine l,SegCircleCurve c, Vector rowPoints)
	{
		double[] distances = new double[rowPoints.size()];
		Point2D[] points = new Point2D[rowPoints.size()]; 

		AnchorPoint start = l.getM_start();
		AnchorPoint end = l.getM_end();
		double sy = start.getY(), ey = end.getY(), sx = start.getX(), ex = end.getX();
		double slope = (sy-ey) / (sx-ex);
		double intercept = ( (sy+ey)*(ex-sx) - (ey-sy)*(sx+ex) ) / 2 / (ex-sx);
		double dist;
		PixelInfo pi;
		
		double maxPositiveDist=0, maxNegativeDist=0, minDist=Double.MAX_VALUE;
		int minDistIndex=-1;
		for(int i=0;i<rowPoints.size();i++)
		{
			pi = (PixelInfo)rowPoints.get(i);
			points[i] = new Point2D.Double(pi.x,pi.y);
			dist = points[i].distance(l.getNearestPointOnSeg(points[i]));
			distances[i] = dist;
			if(minDist > dist)
			{
				minDist = dist;
				minDistIndex = i;
			}
			if( (pi.y - slope*pi.x - intercept) < 0)
			{
				if(maxNegativeDist < dist)
					maxNegativeDist = dist;
				distances[i]=-1*dist;
			}
			else
			{
				if(maxPositiveDist < dist)
					maxPositiveDist = dist;
			}
//			///System.out.println("Index : " + i + " Distance : " + distances[i]);
		}
		
		//The arc is mostly on one side of the line
		if(maxPositiveDist < Constraint.MAX_ALLOWED_CONNECT_GAP || maxNegativeDist < Constraint.MAX_ALLOWED_CONNECT_GAP)
		{
			//The arc is actually near the line
			if(minDist < Constraint.MAX_ALLOWED_CONNECT_GAP)
			{
				
				//The following comments are no more valid. Look for TODO 12-5-2008
				//ap1 is the nearest point from line.
				//ap2 and ap3 are the points on both sides of ap1
				//We'll look for the slope of ap1,ap2 and ap1,ap3
				//And compare this slope with that of line
				//If slopes seem to be equal, we'll assume tangency
				AnchorPoint ap1=null,ap2=null,ap3=null,ap4=null,ap5=null;
				
/*				double curvature = ((PixelInfo)m_ptList.get(minDistIndex)).getCurvature();
				///System.out.println("Curvature is : " + curvature + " Curve angle is : " + Math.toDegrees(curvature) + " Line angle is : " + l.getM_angle() + "\n\n\n\n\n" );*/
				
				ap1 = new AnchorPoint(points[minDistIndex].getX(),points[minDistIndex].getY());
				if(minDistIndex < rowPoints .size()-1)
					ap2 = new AnchorPoint(points[minDistIndex+1].getX(),points[minDistIndex+1].getY());
				if(minDistIndex > 0)
					ap3 = new AnchorPoint(points[minDistIndex-1].getX(),points[minDistIndex-1].getY());
				if(minDistIndex < rowPoints.size()-2)
					ap4 = new AnchorPoint(points[minDistIndex+2].getX(),points[minDistIndex+2].getY());
				if(minDistIndex > 1)
					ap5 = new AnchorPoint(points[minDistIndex-2].getX(),points[minDistIndex-2].getY());
				
				if(ap2 == null || ap3 == null)
				{
/*					///System.out.println("\n\n!!!!! One of the anchor points is null !!!!!\n\n");
					AnchorPoint ap = l.getM_start();
					if( ap1.distance(l.getM_start().getM_point()) > ap1.distance(l.getM_end().getM_point()) )
							ap = l.getM_end();

					
					//If the nearest point is one of the end points of the stroke,
					//It is most likely that a tangency constraint would have been added at that end-point of the stroke
					//Still, if it's not added, we should add it. Will do this later.
					if(constraintsHelper.doesConstraintAlreadyExist(ap,lineCircularCurveTangencyConstraint.class,new AnchorPoint[]{l.getM_start(),c.getM_start(),c.getM_end(),c.getM_center()}) == null)
					{
						///System.out.println("Constraint does not already exit \n\n\n\n\n");
						Constraint cons = new lineCircularCurveTangencyConstraint(l,c,ap,Constraint.HARD,false);
						constraintsHelper.addCons2SegsAndRecogview(cons,new Segment[]{l,c});
						return cons;
					}*/
					;
				}
				else
				{
					if(constraintsHelper.areLinesPerpendicular(l.getM_start(),l.getM_end(),ap1,c.getM_center(),35,false))
					{
						//Constraint cons = new lineCircularCurveTangencyConstraint(l,c,ap1,Constraint.HARD,false);
						Constraint cons = tangencyRecognizer.addLineCircleTangency(l,c,ap1.getM_point(),ap1.getM_point());
						constraintsHelper.addCons2SegsAndRecogview(cons,new Segment[]{l,c});
						return cons;
					}
				}
			}
/*			else
				///System.out.println("\n\n !!!The minimum distance is not small enough\n\n");*/
		}
/*		else
			///System.out.println("\n\n !!!The maximum distance is not small enough\n\n");*/
		
		return null;	
	}
	
	public void removeConstraints()
	{
//		///System.out.println("Stroke.removeConstraints()");
		Iterator iterator = m_segList.iterator();
		while (iterator.hasNext())
		{
			Segment seg = (Segment) iterator.next();
			seg.clearConstraints(Constraint.SOFT);
		}
	}
	
	
	
	/*************************   Helper functions   *****************************/
	
	public SegmentPoint getSegPtAt(int index)
	{
		if(index >= 0)
		{
			Iterator iter = m_segPtList.iterator();
			while (iter.hasNext())
			{
				SegmentPoint sp = (SegmentPoint) iter.next();
				if(sp.getM_point().equals(m_ptList.elementAt(index))) return sp;
			}
		}

		return null;
	}
	
	
	public double[][] getWindowElemAs2DMatrix_Double(int start, int end)
	{
		// check if the values are legitimate
		if((end < start)||(end > (m_ptList.size()-1))||(start < 0)) return null;
		
		double [][] segMat = new double[end-start+1][2];
		for(int i=start; i<=end; i++)
		{
			PixelInfo pt = (PixelInfo)m_ptList.get(i);
			segMat[i-start][0] = pt.x;
			segMat[i-start][1] = pt.y;
		}

		return segMat;
	}

	public double getLength(int start, int end)
	{
		double length = 0.0;

		// check if the values are legitimate
		if((end <= start)||(end > (m_ptList.size()-1))||(start < 0)) return length;
		
		PixelInfo prevPt = (PixelInfo)m_ptList.get(start);
		for(int i=start+1; i<=end; i++)
		{
			PixelInfo pt = (PixelInfo)m_ptList.get(i);
			length += prevPt.distance(pt);
			prevPt = pt;
		}
		return length;
	}

	
	public double getLength()
	{
		return getLength(0, m_ptList.size()-1);
	}
	
	/**
	 * Why not just return the previous element in the list instead of line intersections?
	 * Note that the Point X,Y coordinates are integers, which is why the x+1,y+1 trick works.
	 * @param x
	 * @param y
	 * @return
	 */
	public PixelInfo findPrevPI(double xc, double yc)
	{
		PixelInfo prevPt = null;
		Iterator iter = m_ptList.iterator();
		if(iter.hasNext())
		{
			prevPt = ((PixelInfo)iter.next());
			if(Line2D.linesIntersect(prevPt.x, prevPt.y, prevPt.x+1, prevPt.y+1, xc, yc, xc+1, yc+1)) return prevPt;
		}
		
		while (iter.hasNext())
		{
			PixelInfo nextPt = ((PixelInfo)iter.next());
			if(Line2D.linesIntersect(prevPt.x, prevPt.y, nextPt.x, nextPt.y, xc, yc, xc+1, yc+1)) return prevPt;
			prevPt = nextPt;
		}
		return null;
	}

	/**
	 * See findPrevPI
	 * @param x
	 * @param y
	 * @return
	 */
	public PixelInfo findNextPI(double xc, double yc)
	{
		PixelInfo prevPt = null;
		Iterator iter = m_ptList.iterator();
		if(iter.hasNext())
		{
			prevPt = ((PixelInfo)iter.next());
			if(Line2D.linesIntersect(prevPt.x, prevPt.y, prevPt.x+1, prevPt.y+1, xc, yc, xc+1, yc+1)) return prevPt;
		}
		
		while (iter.hasNext())
		{
			PixelInfo nextPt = ((PixelInfo)iter.next());
			if(Line2D.linesIntersect(prevPt.x, prevPt.y, nextPt.x, nextPt.y, xc, yc, xc+1, yc+1)) return nextPt;
			prevPt = nextPt;
		}
		return null;
	}

	
	public boolean containsPt(double x, double y)
	{
//		///System.out.println("Stroke.containsPt()");
		PixelInfo prevPt = null;
		Iterator iter = m_ptList.iterator();
		if(iter.hasNext())
		{
			prevPt = ((PixelInfo)iter.next());
			if(Line2D.linesIntersect(prevPt.x, prevPt.y, prevPt.x, prevPt.y, x, y, x+1, y+1)) return true;
		}
		
		while (iter.hasNext())
		{
			PixelInfo nextPt = ((PixelInfo)iter.next());
			if(Line2D.linesIntersect(prevPt.x, prevPt.y, nextPt.x, nextPt.y, x, y, x+1, y+1)) return true;
			prevPt = nextPt;
		}
		return false;
	}
	
	public void move(int x1, int y1, int x2, int y2)
	{
	}
	
	public void setEnabled(boolean enabled)
	{
		Iterator iter = getM_segList().iterator();
		while (iter.hasNext())
		{
			Segment seg = (Segment) iter.next();
			seg.setEnabled(enabled);
		}
		super.setEnabled(enabled);
	}
	
	public boolean isSmallSize()
	{
		return (getLength() <= (Marker.MARKER_SIZE*getM_segList().size()));
	}
	
	public GeometryElement copy()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void delete()
	{
		Iterator iter = m_segList.iterator();
		while (iter.hasNext())
		{
			Segment seg = (Segment) iter.next();
			iter.remove();
			seg.delete();
		}

		// remove the stroke all togeather
		DrawingView dv = GMethods.getCurrentView();
		dv.getM_drawData().removeStroke(this);

		super.delete();
		// this should do more.. like remove this stroke from the drawing data list.
	}
	
	public String toString()
	{
		return toXMLString();
	}

	public String toXMLString()
	{
		String str = "";
		str += this.getClass().getName()+":"+getM_strId()+":(";
		Iterator iter = getM_ptList().iterator();
		while (iter.hasNext())
		{
			PixelInfo pi = (PixelInfo) iter.next();
			str += pi.toXMLString()+",";
		}
		
		// remove the last comma
		str = str.substring(0, str.length()-1);
		str += ")";
		return str;
	}
	
	public boolean equals(Object obj)
	{
		if(obj instanceof Stroke)
		return  getM_strId().equals(((Stroke)obj).getM_strId());
		return false;
	}

	
	
	/*************************   Different types of data returning Functions   *****************************/
	
	public double[] getSpeedData()
	{
		int len = m_ptList.size();
		double [] speedData = new double[len];
		for(int i=0; i<len; i++)
			speedData[i] = ((PixelInfo) m_ptList.get(i)).getSpeed();
		return speedData;
	}

	public double[] getCurvatureData()
	{
		int len = m_ptList.size();
		double [] curveData = new double[len];
		for(int i=0; i<len; i++)
			curveData[i] = Math.abs(((PixelInfo) m_ptList.get(i)).getCurvature());
		return curveData;
	}

	public double[] getSlopeData()
	{
		int len = m_ptList.size();
		double [] slopeData = new double[len];
		for(int i=0; i<len; i++)
			slopeData[i] = ((PixelInfo) m_ptList.get(i)).getSlope();
		return slopeData;
	}

	public double[] getTimeData()
	{
		int len = m_ptList.size();
		double [] timeData = new double[len];
		
		// take the time of the first pixel as the base time.
		PixelInfo prevPixel = (PixelInfo)m_ptList.get(0);
		
		// store the time information from each pixel in the array
		for(int i=0; i<len; i++)
		{
			PixelInfo currPixel = (PixelInfo) m_ptList.get(i);
			timeData[i] = currPixel.getTime() - prevPixel.getTime();
			prevPixel = currPixel;
		}
		return timeData;
	}

	
	
	/*************************   Getters and Setters   *****************************/

	public int getM_type()
	{
		return m_type;
	}

	public void setM_type(int m_type)
	{
		this.m_type = m_type;
	}

	public Vector getM_ptList()
	{
		return m_ptList;
	}
	
	public void setM_ptList(Vector rawPtList){
		m_ptList = rawPtList;
	}

	public Vector getM_segPtList()
	{
		return m_segPtList;
	}

	public void setM_segPtList(Vector ptList)
	{
		m_segPtList = ptList;
	}
	
	public Vector<Segment> getM_segList()
	{
		return m_segList;
	}
	
	
}