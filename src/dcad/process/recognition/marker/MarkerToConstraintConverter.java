package dcad.process.recognition.marker;

import java.util.Iterator;
import java.util.Vector;
import java.awt.geom.Point2D;
import java.lang.Double;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.angle.EqualAngleConstraint;
import dcad.model.constraint.angle.IndAngleConstraint;
import dcad.model.constraint.angle.ParallelSegConstraint;
import dcad.model.constraint.angle.PerpendicularSegConstraint;
import dcad.model.constraint.angle.RelAngleConstraint;
import dcad.model.constraint.circleArc.EqualRadiusConstraint;
import dcad.model.constraint.circleArc.IndRadiusConstraint;
import dcad.model.constraint.circleArc.circleArcAngleConstraint;
import dcad.model.constraint.connect.lineCircularCurveTangencyConstraint;
import dcad.model.constraint.distance.DistanceBetweenPointAndCircularCurveConstraint;
import dcad.model.constraint.distance.DistanceBetweenPointAndLineConstraint;
import dcad.model.constraint.length.EqualRelLengthConstraint;
import dcad.model.constraint.length.IndLengthConstraint;
import dcad.model.constraint.points.DistanceBetween2PointsConstraint;
import dcad.model.constraint.points.HorizontalAlignedPointsConstraint;
import dcad.model.constraint.points.VerticalAlignedPointsConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.Text;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.model.marker.*;
import dcad.util.GConstants;
import dcad.util.GMethods;
import dcad.process.recognition.constraint.*;

public class MarkerToConstraintConverter
{
	private static MarkerToConstraintConverter m_converter = null;
	private Vector constraints = new Vector() ;
	
	public static MarkerToConstraintConverter getInstance()
	{
		if(m_converter == null) m_converter = new MarkerToConstraintConverter();
		return m_converter;
	}

	private void addConstraint(Constraint c,Segment[] segments)
	{
		if(c!=null)
		{
			constraints.add(c);
			constraintsHelper.addCons2SegsAndRecogview(c,segments);
		}
	}
	
	public Constraint simple_marker_recog(int marker_type, Vector Segments) 
	{
	Constraint c =null;
	if(marker_type == Marker.TYPE_EQUALITY) 
	{
		SegLine s1 =( SegLine)Segments.elementAt(0) ;
		SegLine s2 = ( SegLine)Segments.elementAt(1);
		c = (EqualRelLengthConstraint)RelLengthRecognizer.getEqualLengthConstraint(s1, s2 ,Constraint.HARD, false);
		addConstraint(c ,new Segment[]{ s1,s2 });
		return c;
		
	}
	else if (marker_type == Marker.TYPE_RIGHT_ANGLE)
	{
		SegLine s1 =( SegLine)Segments.elementAt(0) ;
		SegLine s2 = ( SegLine)Segments.elementAt(1);
		
		 c = (PerpendicularSegConstraint)RelAngleRecognizer.getPerpendicularSegmentsConstraint(s1, s2,Constraint.HARD, false);
		
		addConstraint(c ,new Segment[]{ s1,s2 });
		return c;
		
	}
	
	else if (marker_type == Marker.TYPE_PARALLEL)
	{
		Segment seg1 = (Segment)Segments.elementAt(0) ;
		Segment seg2 = (Segment)Segments.elementAt(1);
		
		
		if(seg1 instanceof SegLine && seg2 instanceof SegLine)
		{
			ParallelSegConstraint cons = (ParallelSegConstraint)RelAngleRecognizer.getParallelSegmentsConstraint((SegLine)seg1, (SegLine)seg2,Constraint.HARD, false);
	
			addConstraint(cons,new Segment[]{seg1, seg2});
			return cons; 

		}
		else if(seg1 instanceof SegLine && seg2 instanceof SegCircleCurve || seg2 instanceof SegLine && seg1 instanceof SegCircleCurve)
		{
			SegLine l;
			SegCircleCurve cu;
			if(seg1 instanceof SegLine)
			{
				l=(SegLine)seg1;
				cu=(SegCircleCurve)seg2;
			}
			else
			{
				l=(SegLine)seg2;
				cu=(SegCircleCurve)seg1;
			}
			lineCircularCurveTangencyConstraint cons = null;

			AnchorPoint ap = constraintsHelper.getCommonPointBetweenLineAndCircularCurve(l,cu);
			if(ap!=null && ap!=cu.getM_center())
					cons = (lineCircularCurveTangencyConstraint) tangencyRecognizer.addLineCircleTangency(l,cu,ap.getM_point(),ap.getM_point());
			else if(ap!=cu.getM_center())
			{
				Point2D p1 = l.getNearestPointOnSeg(cu.getM_center().getM_point());
				Point2D p2 = cu.getNearestPointOnSeg(p1);
				cons = (lineCircularCurveTangencyConstraint) tangencyRecognizer.addLineCircleTangency(l,cu,p1,p2);
			}
	
			addConstraint(cons,new Segment[]{l,cu});	
			return cons ;
		}
		return c;
	}
	
	else return c ;

	}
	
	public Vector recognizeMarkersAsConstraints(Vector markers, Vector textElements, Vector SEG)
	{
		constraints = new Vector();
		
//		Vector usedMarkers = new Vector();
		Iterator iter = markers.iterator();
		while (iter.hasNext())
		{
			// check if its a marker
			GeometryElement gEle = (GeometryElement) iter.next();
			//if((gEle instanceof Marker) && (!usedMarkers.contains(gEle)))
			if(gEle instanceof Marker)
			{
				Marker mark = (Marker) gEle;
				if(!mark.isM_used())
				{
					switch (mark.getM_type())
					{
					case Marker.TYPE_EQUALITY:
					{
						MarkerEquality eMarker1 = (MarkerEquality)mark;
						// get the constraints with other equality markers
						Iterator iter2 = markers.iterator();
						while (iter2.hasNext())
						{
							GeometryElement gEle2 = (GeometryElement) iter2.next();
							if((gEle2 instanceof MarkerEquality) && (!gEle2.equals(gEle)))
							{
								MarkerEquality eMarker2 = (MarkerEquality) gEle2;
								if(!eMarker2.isM_used())
								{
									Constraint cons = null;
									
									if(eMarker1.getM_seg() instanceof SegLine && eMarker2.getM_seg() instanceof SegLine)
									{
										cons = (EqualRelLengthConstraint)RelLengthRecognizer.getEqualLengthConstraint((SegLine)eMarker1.getM_seg(), (SegLine)eMarker2.getM_seg(),Constraint.HARD, false);
									}
									else if(eMarker1.getM_seg() instanceof SegCircleCurve && eMarker2.getM_seg() instanceof SegCircleCurve)
										cons = (EqualRadiusConstraint)RelLengthRecognizer.getEqualRadiusConstraint((SegCircleCurve)eMarker1.getM_seg(), (SegCircleCurve)eMarker2.getM_seg(),Constraint.HARD, false);
									if(cons!=null)
										addConstraint(cons,new Segment[]{eMarker1.getM_seg(),eMarker2.getM_seg()});
									mark.setM_used(true);
									((Marker)gEle2).setM_used(true);
								}
							}
						}
					}
					break;

					case Marker.TYPE_ANGLE: 
					{
						MarkerAngle eMarker1 = (MarkerAngle)mark;
						///System.out.println("enter angle ");
						// check if this marker has Text value set, if yes add a Relative angle constraint
						if(eMarker1.getM_text() != null)
						{
							double angle = eMarker1.getM_angle();
							/*if(angle > 180)
								angle%=180;*/
							// text is set, add a RelAngleConstraint
							Segment seg1 = eMarker1.getM_seg1();
							Segment seg2 = eMarker1.getM_seg2();
							if(constraintsHelper.getConstraintBetween2Segments(seg1,seg2,RelAngleConstraint.class)!=null)
								constraintsHelper.getConstraintBetween2Segments(seg1,seg2,RelAngleConstraint.class).remove();
							RelAngleConstraint rac = new RelAngleConstraint(seg1, seg2, angle, Constraint.HARD,false);
							addConstraint(rac,new Segment[]{seg1,seg2});
							mark.setM_used(true);
						}
						
						// check for Equal angle constraint
						Iterator iter2 = markers.iterator();
						while (iter2.hasNext())
						{
							GeometryElement gEle2 = (GeometryElement) iter2.next();
							if((gEle2 instanceof MarkerAngle) && (!gEle2.equals(gEle)))
							{
								MarkerAngle eMarker2 = (MarkerAngle) gEle2;
								if(!eMarker2.isM_used())
								{
									EqualAngleConstraint cons = new EqualAngleConstraint(eMarker1, eMarker2,Constraint.HARD,false);
									addConstraint(cons,new Segment[]{eMarker1.getM_seg1(),eMarker1.getM_seg2(),eMarker2.getM_seg1(),eMarker2.getM_seg2()});
									mark.setM_used(true);
									((Marker)gEle2).setM_used(true);
								}
								
							}
						}
					}
					break;
					case Marker.TYPE_RIGHT_ANGLE:
					{
						MarkerPerpendicular raMarker = (MarkerPerpendicular)mark;
						PerpendicularSegConstraint cons = (PerpendicularSegConstraint)RelAngleRecognizer.getPerpendicularSegmentsConstraint((SegLine)raMarker.getM_seg1(), (SegLine)raMarker.getM_seg2(),Constraint.HARD, false);
						if(cons!=null)
							addConstraint(cons,new Segment[]{raMarker.getM_seg1(),raMarker.getM_seg2()});
						mark.setM_used(true);
					}
					break;
					
					case Marker.TYPE_PARALLEL:
					{
						MarkerParallel eMarker1 = (MarkerParallel)mark;
						Iterator iter2 = markers.iterator();
						while (iter2.hasNext())
						{
							GeometryElement gEle2 = (GeometryElement) iter2.next();
							if((gEle2 instanceof MarkerParallel) && (!gEle2.equals(gEle)))
							{
								MarkerParallel eMarker2 = (MarkerParallel) gEle2;
								if(!eMarker2.isM_used())
								{
									Segment seg1 = eMarker1.getM_seg();
									Segment seg2 = eMarker2.getM_seg();
									
									if(seg1 instanceof SegLine && seg2 instanceof SegLine)
									{
										ParallelSegConstraint cons = (ParallelSegConstraint)RelAngleRecognizer.getParallelSegmentsConstraint((SegLine)eMarker1.getM_seg(), (SegLine)eMarker2.getM_seg(),Constraint.HARD, false);
										if(cons!=null)
											addConstraint(cons,new Segment[]{eMarker1.getM_seg(),eMarker2.getM_seg()});
										mark.setM_used(true);
										((Marker)gEle2).setM_used(true);
									}
									else if(seg1 instanceof SegLine && seg2 instanceof SegCircleCurve || seg2 instanceof SegLine && seg1 instanceof SegCircleCurve)
									{
										SegLine l;
										SegCircleCurve c;
										if(seg1 instanceof SegLine)
										{
											l=(SegLine)seg1;
											c=(SegCircleCurve)seg2;
										}
										else
										{
											l=(SegLine)seg2;
											c=(SegCircleCurve)seg1;
										}
										lineCircularCurveTangencyConstraint cons = null;

										AnchorPoint ap = constraintsHelper.getCommonPointBetweenLineAndCircularCurve(l,c);
										if(ap!=null && ap!=c.getM_center())
												cons = (lineCircularCurveTangencyConstraint) tangencyRecognizer.addLineCircleTangency(l,c,ap.getM_point(),ap.getM_point());
										else if(ap!=c.getM_center())
										{
											Point2D p1 = l.getNearestPointOnSeg(c.getM_center().getM_point());
											Point2D p2 = c.getNearestPointOnSeg(p1);
											cons = (lineCircularCurveTangencyConstraint) tangencyRecognizer.addLineCircleTangency(l,c,p1,p2);
										}
										if(cons!=null)
											addConstraint(cons,new Segment[]{l,c});
										mark.setM_used(true);
										((Marker)gEle2).setM_used(true);
									}
								}
							}
						}
					}
					break;
					
					case Marker.TYPE_FIXED_LENGTH:
					{
						MarkerLength lMarker = (MarkerLength)mark;
						SegLine seg=lMarker.getM_lineSeg();
						if(seg.getConstraintByType(IndLengthConstraint.class).size()!=0)
							((Constraint)seg.getConstraintByType(IndLengthConstraint.class).elementAt(0)).remove();
						IndLengthConstraint cons = new IndLengthConstraint(lMarker.getM_lineSeg(), lMarker.getM_length(), Constraint.HARD,false);
						cons.setM_marker(lMarker);
						addConstraint(cons,new Segment[]{lMarker.getM_lineSeg()});
						mark.setM_used(true);
					}
					break;
					
					case Marker.TYPE_RADIUS:
					{
						MarkerRadius rMarker = (MarkerRadius)mark;
						SegCircleCurve seg=rMarker.getM_circleCurveSeg();
						if(seg.getConstraintByType(IndRadiusConstraint.class).size()!=0)
							((Constraint)seg.getConstraintByType(IndRadiusConstraint.class).elementAt(0)).remove();
						IndRadiusConstraint cons = new IndRadiusConstraint(seg, rMarker.getM_radius(), Constraint.HARD,false);
						addConstraint(cons,new Segment[]{seg});
						mark.setM_used(true);
					}
					break;
						
					case Marker.TYPE_CIRCULAR_ARC_ANGLE:
					{
						MarkerCircleArcAngle aMarker = (MarkerCircleArcAngle)mark;
						double arcAngle = aMarker.getM_angle();
						if(arcAngle > 360)
							arcAngle %= 360 ;
						SegCircleCurve seg=aMarker.getM_circleCurveSeg();
						if(seg.getConstraintByType(circleArcAngleConstraint.class).size()!=0)
							((Constraint)seg.getConstraintByType(circleArcAngleConstraint.class).elementAt(0)).remove();
						circleArcAngleConstraint cons = new circleArcAngleConstraint(aMarker.getM_circleCurveSeg(),arcAngle,Constraint.HARD,false);
						addConstraint(cons,new Segment[]{aMarker.getM_circleCurveSeg()});
						mark.setM_used(true);
					}
					break;
						
					case Marker.TYPE_LINE_ANGLE:
					{
						MarkerLineAngle laMarker = (MarkerLineAngle)mark;
						double angleValue = laMarker.getM_angle();
						if(angleValue > 180)
							angleValue = angleValue % 180;
						SegLine seg=laMarker.getM_segLine();
						if(seg.getConstraintByType(IndAngleConstraint.class).size()!=0)
							((Constraint)seg.getConstraintByType(IndAngleConstraint.class).elementAt(0)).remove();
						Constraint cons = null;
						if(angleValue == 0 || angleValue == 180)
							cons = IndAngleRecognizer.addParallelConstraint(seg,0,Constraint.HARD,false);
						else if(angleValue == 90)
							cons = IndAngleRecognizer.addPerpendicularConstraint(seg,90,Constraint.HARD,false);
						else
							cons = new IndAngleConstraint(seg,angleValue, Constraint.HARD,false);
						addConstraint(cons,new Segment[]{seg});
						mark.setM_used(true);
					}
					break;
					
					case Marker.TYPE_LINE_DISTANCE:
					{
						MarkerDistance m=(MarkerDistance)mark;
						Segment[] s=m.getM_segments(); 
						Constraint cons = DistanceBetweenSegmentsRecognizer.getDistanceBetweenLinesConstraint((SegLine)s[0],(SegLine)s[1],m.getM_distance(),Constraint.HARD,false);
						addConstraint(cons,new Segment[]{s[0],s[1]});
						mark.setM_used(true);
					}
					break;
					
					case Marker.TYPE_CURVE_DISTANCE:
					{
						MarkerDistance m=(MarkerDistance)mark;
						Segment[] s=m.getM_segments(); 
						Constraint cons = DistanceBetweenSegmentsRecognizer.getDistanceBetweenCurvesConstraint((SegCircleCurve)s[0],(SegCircleCurve)s[1],m.getM_distance(),Constraint.HARD,false);
						addConstraint(cons,new Segment[]{s[0],s[1]});
						mark.setM_used(true);
					}
					break;
						
					case Marker.TYPE_LINE_CURVE_DISTANCE:
					{
						MarkerDistance m = (MarkerDistance)mark;
						Segment[] s=m.getM_segments(); 
						Constraint cons = DistanceBetweenSegmentsRecognizer.getDistanceBetweenCircularCurveAndLineConstraint((SegLine)s[0],(SegCircleCurve)s[1],m.getM_distance(),Constraint.HARD,false);
						addConstraint(cons,new Segment[]{s[0],s[1]});
						mark.setM_used(true);
					}
					break;
					
					case Marker.TYPE_2_POINTS_DISTANCE:
					{
						MarkerDistance m = (MarkerDistance) mark;
						AnchorPoint[] a = m.getM_anchorPoints();
						Constraint cons = new DistanceBetween2PointsConstraint(a[0],a[1],m.getM_distance(),Constraint.HARD,false);
						addConstraint(cons,new Segment[]{});
						mark.setM_used(true);
					}
					break;
					
					case Marker.TYPE_POINT_SEGMENT_DISTANCE:
					{
						MarkerDistance m = (MarkerDistance) mark;
						AnchorPoint[] a = m.getM_anchorPoints();
						Segment[] s = m.getM_segments();
						Constraint cons = null;
						if(s[0] instanceof SegLine)
							cons = new DistanceBetweenPointAndLineConstraint((SegLine)s[0],a[0],m.getM_distance(),Constraint.HARD,false);
						else
							cons = new DistanceBetweenPointAndCircularCurveConstraint((SegCircleCurve)s[0],a[0],m.getM_distance(),Constraint.HARD,false);
						addConstraint(cons,new Segment[]{s[0]});
						mark.setM_used(true);
					}
					break;
					
					case Marker.TYPE_HORIZONTAL_POINTS:
					{
						MarkerPointsAlignment m = (MarkerPointsAlignment) mark;
						Constraint cons = new HorizontalAlignedPointsConstraint(m.getPoints()[0],m.getPoints()[1],Constraint.HARD,false);
						addConstraint(cons,new Segment[]{});
						mark.setM_used(true);
					}
					break;
					
					case Marker.TYPE_VERTICAL_POINTS:
					{
						MarkerPointsAlignment m = (MarkerPointsAlignment) mark;
						Constraint cons = new VerticalAlignedPointsConstraint(m.getPoints()[0],m.getPoints()[1],Constraint.HARD,false);
						addConstraint(cons,new Segment[]{});
						mark.setM_used(true);
					}
					break;
					
					default: // do nothing
					break;
					}
				}
				
				//Commented on 25-3-2008 to allow recognizing constraint as soon as the markers are drawn.
				//Instead of this, now we set marker used to true in all of the cases above.
				//If I add two equal length markers, one of them will add constraint and set itself used.
				//So, when it's the turn of the second, it'll not be able to do anything and will not be able to set itself used.
				//So, force it to be used.
//				mark.setM_used(true);
			}
			
			// remove this element as it has already been considered
			//usedMarkers.add(gEle);
			
		}
		//m_drawData.getM_geoElements().removeAll(usedMarkers);
		// m_drawData.getM_geoElements().clear();
		return constraints; 
	}

	
	
	
	
	
	//8-5-2008
//	public Vector recognizeTextAsMarkers(Vector markers, Vector textElements, Vector segments,Vector selectedElements)
	public Vector recognizeTextAsMarkers(Vector markers, Vector textElements, Vector segments,Vector selectedElements,Vector highlightedElements)
	{
		Vector newMarkers = new Vector();
		Marker marker=null;
		
		// first assign the text to the markers OR generate Constraints with segments
		Iterator iter = textElements.iterator();
		while (iter.hasNext())
		{
			Text text = (Text) iter.next();
			marker=null;
			if(!text.isM_used())
			{
				// text element is not used so use it
				try
				{
					
					if(selectedElements.size()==0)
					{
						// check if this text is close to any of the constraints or geometric elements
						// first check if its close to any constraint. if it is then dont check closeness to any segment
						marker = text.getClosestMarker(markers, MarkerAngle.class);
						if(marker  != null)
						{
							String markerText = text.getM_text();
							/*if(doesTextContainAChar(markerText,'a',-1,true))
							{
								String angle=markerText.substring(0,markerText.length()-1);
								Double.parseDouble(angle);
								text.setM_text(angle);
								((MarkerAngle)marker ).setM_text(text);
							}*/
							String angle=markerText.substring(0,markerText.length());
							Double.parseDouble(angle);
							text.setM_text(angle);
							((MarkerAngle)marker ).setM_text(text);
							
						}
						else
						{
							// as this text is not close to any marker, check if its close to any segment
							Segment closeSeg = null;
							if( highlightedElements.size()!=0 && (highlightedElements.get(0) instanceof Segment) )
								closeSeg = (Segment) highlightedElements.get(0);
							if(closeSeg==null)
								closeSeg = text.getClosestSegment(segments,SegLine.class);
							if(closeSeg == null)
								closeSeg = text.getClosestSegment(segments,SegCircleCurve.class);
							
							/* 8-5-2008
							 Segment closeSeg = text.getClosestSegment(segments, SegLine.class);
							 if(closeSeg == null)
							 closeSeg = text.getClosestSegment(segments,SegCircleCurve.class);*/
							if(closeSeg != null)
							{
								if(closeSeg instanceof SegLine)
								{
									String markerText=text.getM_text();
									if(doesTextContainAChar(markerText,'a',-1,true))
									{
										String lineAngle=markerText.substring(0,markerText.length()-1);
										Double.parseDouble(lineAngle);
										text.setM_text(lineAngle);
										marker = new MarkerLineAngle((SegLine)closeSeg, text);
									}
									else
									{
										Double.parseDouble(markerText);
										marker = new MarkerLength((SegLine)closeSeg, text);
									}
								}
								else if(closeSeg instanceof SegCircleCurve)
								{
									String markerText=text.getM_text();
									if(doesTextContainAChar(markerText,'a',-1,true))
									{
										String angle=markerText.substring(0,markerText.length()-1);
										Double.parseDouble(angle);
										text.setM_text(angle);
										marker = new MarkerCircleArcAngle((SegCircleCurve)closeSeg, text);
									}
									else
									{
										Double.parseDouble(markerText);
										marker = new MarkerRadius((SegCircleCurve)closeSeg, text);
									}
								}
							}
						}
					}
					else //Some segments are selected, meaning this is a distance constraint 
					{
						Vector selectedLines=new Vector();
						Vector selectedCircularCurves = new Vector();
						Vector selectedAnchorPoints = new Vector();
						
						for(int i=0;i<selectedElements.size();i++)
						{
							GeometryElement g = (GeometryElement)selectedElements.get(i);
							if(g instanceof SegLine)
								selectedLines.add(g);
							else if(g instanceof SegCircleCurve)
								selectedCircularCurves.add(g);
							else if(g instanceof AnchorPoint)
								selectedAnchorPoints.add(g);
						}
						if(selectedLines.size()==2)
						{
							Double.parseDouble(text.getM_text());
							marker=new MarkerDistance(new Segment[] { (Segment)selectedLines.get(0), (Segment)selectedLines.get(1)}, new AnchorPoint[]{}, text,Marker.TYPE_LINE_DISTANCE);
						}
						else if(selectedCircularCurves.size() == 2){
							///System.out.println("Curve Distance");
							//Double.parseDouble(text.getM_text());
							//marker=new MarkerDistance(new Segment[] { (Segment)selectedCircularCurves.get(0), (Segment)selectedCircularCurves.get(1)}, new AnchorPoint[]{}, text,Marker.TYPE_CURVE_DISTANCE);
						}
						else if(selectedCircularCurves.size()==1 && selectedLines.size()>0)
						{
							Double.parseDouble(text.getM_text());
							marker = new MarkerDistance(new Segment[] { (Segment)selectedLines.get(0),(Segment)selectedCircularCurves.get(0) }, new AnchorPoint[]{} ,text,Marker.TYPE_LINE_CURVE_DISTANCE);
						}
						else if(selectedAnchorPoints.size() == 2)
						{
							String markerText = text.getM_text();
							if(doesTextContainAChar(markerText,'h',0,true))
							{
								marker = new MarkerPointsAlignment((AnchorPoint)selectedAnchorPoints.get(0),(AnchorPoint)selectedAnchorPoints.get(1),Marker.TYPE_HORIZONTAL_POINTS);
							}
							else if(doesTextContainAChar(markerText,'v',0,true))
							{
								marker = new MarkerPointsAlignment((AnchorPoint)selectedAnchorPoints.get(0),(AnchorPoint)selectedAnchorPoints.get(1),Marker.TYPE_VERTICAL_POINTS);
							}
							else
							{
								Double.parseDouble(text.getM_text());
								marker = new MarkerDistance(new Segment[]{}, new AnchorPoint[]{ (AnchorPoint)selectedAnchorPoints.get(0),(AnchorPoint)selectedAnchorPoints.get(1) },text,Marker.TYPE_2_POINTS_DISTANCE);
							}
						}
						else if(selectedAnchorPoints.size() > 2)
						{
							//Add collinearity constraint
						}
						else if(selectedAnchorPoints.size() == 1 && selectedLines.size() == 1)
						{
							Double.parseDouble(text.getM_text());
							marker = new MarkerDistance(new Segment[]{(Segment)selectedLines.get(0)}, new AnchorPoint[]{(AnchorPoint)selectedAnchorPoints.get(0)},text,Marker.TYPE_POINT_SEGMENT_DISTANCE);
						}
						else if(selectedAnchorPoints.size() == 1 && selectedCircularCurves.size() == 1)
						{
							Double.parseDouble(text.getM_text());
							marker = new MarkerDistance(new Segment[]{(Segment)selectedCircularCurves.get(0)}, new AnchorPoint[]{(AnchorPoint)selectedAnchorPoints.get(0)},text,Marker.TYPE_POINT_SEGMENT_DISTANCE);
						}
					}
					
					if(marker!=null)
					{
						newMarkers.add(marker);
						text.setM_used(true);
					}
				}
				catch (NumberFormatException e) 
				{
					// do nothing
				}
			}
		}
		return newMarkers; 
	}
	
	
	
	private boolean doesTextContainAChar(String text,char c, int position, boolean ignoreCase)
	{
		String str = new String();
		if(position==-1)
			str = String.valueOf(text.charAt(text.length()-1));
		else
			str = String.valueOf(text.charAt(position));
		if(ignoreCase && str.equalsIgnoreCase(String.valueOf(c)))
			return true;
		if(!ignoreCase && str.equals(String.valueOf(c)))
			return true;
		return false;
	}
	
	
	
	
	
	

}