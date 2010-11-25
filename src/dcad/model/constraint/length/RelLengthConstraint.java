package dcad.model.constraint.length;

import java.awt.Graphics;
import java.util.Vector;

import dcad.model.constraint.RelativeConstraint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.Segment;
import dcad.util.GMethods;

public class RelLengthConstraint extends RelativeConstraint
{
	private double m_lenDiff = 0.0;

	public RelLengthConstraint(Segment seg1, Segment seg2,int category,boolean promoted)
	{
		super(seg1, seg2,category,promoted);
	}

	public double getM_lenDiff()
	{
		return m_lenDiff;
	}

	public void setM_lenDiff(double diff)
	{
		m_lenDiff = diff;
	}
	
/*	public String toString()
	{
		return super.toString()+"Relative Length Constraint: Segments: "+m_seg1.getM_label()+", "+m_seg2.getM_label()+", Length Diff: ("+GMethods.formatNum(m_lenDiff)+")";
	}
*/

	public Vector getEquation(Vector fixedPoints)
	{
		Vector returnVec = new Vector();
		String str = "";
		returnVec.add(str);

		return returnVec;
	}

	public Vector getPartialDifferentialString(String var, Vector fixedPoints)
	{
		Vector retVec = new Vector();
		String str = "0.0";
		retVec.add(str);

		return retVec;
	}

	public void draw(Graphics g)
	{
		if((m_seg1.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL) && (m_seg2.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL))
		{
		}
	}

/*	private void drawSegment(Graphics2D g, Segment seg)
	{
		if(m_seg1 instanceof SegLine)
		{
			Line2D line = (Line2D)seg.getM_shape();
			double distance = line.getP1().distance(line.getP2());
			double rad = Math.sqrt((distance/2)*(distance/2) + (SIZE/2)*(SIZE/2));
			/////System.out.println(distance+" asdasdas: "+rad);
			Vector centers = GMethods.CircleCircleIntersections(line.getX1(), line.getY1(), rad, line.getX2(), line.getY2(), rad);
			Line2D cons = new Line2D.Double((Point2D)centers.get(0), (Point2D)centers.get(1)); 
			g.draw(cons);

			// write the marker number
			Font f = g.getFont();
			g.setFont(new Font(f.getName(), DEF_FONT_TYPE, DEF_FONT_SIZE));
			g.drawString(Integer.toString(getM_markerCount()), (int)cons.getX2()+2, (int)cons.getY2());
			g.setFont(f);
		}
	}
*/
	
}
