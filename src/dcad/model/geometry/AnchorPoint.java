package dcad.model.geometry;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

import dcad.Prefs;
import dcad.model.constraint.Constraint;
import dcad.model.constraint.IndependentPointConstraints;
import dcad.model.constraint.constraintsHelper;
import dcad.util.GConstants;
import dcad.util.GVariables;
import dcad.util.Maths;

/**
 * End-points of segments are called anchor-points.
 *
 */
public class AnchorPoint extends ImpPoint
{
	protected static int MAX_WEIGHT = 1000;
	
	protected int lx = -5, ly = -5;
	protected int weight = 0;
	protected Vector constraints = new Vector();
	
	public AnchorPoint(Point2D point, Vector parent)
	{
		super(point, parent);
		constraints = new Vector();
		m_size = Prefs.getAnchorPtSize();
		m_type = Prefs.getAnchorPtType();
	}
	
	public AnchorPoint()
	{
		this(null, null);
	}

	public AnchorPoint(double x1, double y1)
	{
		this(new Point2D.Double(x1, y1), null);
	}

	public AnchorPoint(String label, double x1, double y1)
	{
		this(x1, y1);
		setM_label(label);
	}
		
	public void draw(Graphics g)
	{
		if(!isEnabled()) return;
		super.draw(g);
		// set the color of the graphics.
		Graphics2D g2d = (Graphics2D)g;
		Color prevColor = g.getColor();
		g2d.setColor(getM_color());

		// in case the element is higlighted or selected etc.. then increase the size of the Anchor Point
		switch (Prefs.getShowAnchorPoints())
		{
		case GConstants.SHOW_ANCHORPOINT_ALWAYS:
			drawPoint(g2d, m_size, m_type);
			break;

		case GConstants.SHOW_ANCHORPOINT_ON_HL:
			// in case AP is selected/highlighted show the AP with Actual size
			if(getM_color() != m_color) drawPoint(g2d, m_size, m_type);
			else drawPoint(g2d, 0, m_type);
			break;

		default:
			drawPoint(g2d, m_size, m_type);
			break;
		}

		g2d.setColor(GVariables.DRAWING_ASSIST_COLOR);
		// mark the actual point
 		g2d.drawRect((int)getM_point().getX(), (int)getM_point().getY(), 1, 1);
		// reset the color back
		g2d.setColor(prevColor);
		
		Vector constraints = constraintsHelper.getAllIndependentPointConstraints(this);
		for(int i=0;i<constraints.size();i++)
		{
			Constraint c = (Constraint)constraints.get(i);
			if(!c.isDeleted())
				((Constraint)constraints.get(i)).draw(g);
		}
	}

	/*	public DecoratedAnchorPoint getM_decorator()
	{
		return m_decorator;
	}

	public void setM_decorator(DecoratedAnchorPoint m_decorator)
	{
		this.m_decorator = m_decorator;
	}
*/
	/**
	 * returns the index in the parent geometric element
	 * @return index if parent is a stroke, else -1 (calling method should check)
	 */
	public int getIndex()
	{
		int l;
		for(l=0 ; l<vecParent.size();l++)
			if(vecParent.elementAt(l) instanceof Stroke)
				return ((Stroke)vecParent.elementAt(l)).getM_ptList().indexOf(getM_point());
		if(l==vecParent.size())
			return -1;
		
/*		GeometryElement parent = getM_parent();
			
		if(parent instanceof Stroke)
			return ((Stroke)parent).getM_ptList().indexOf(getM_point());
		else return -1;*/
		return 0;

	}

	public int getM_size()
	{
		return m_size;
	}

	public void setM_size(int m_size)
	{
		this.m_size = m_size;
	}

	public int getM_type()
	{
		return m_type;
	}

	public void setM_type(int m_type)
	{
		this.m_type = m_type;
	}

	public void removeConstraint(Constraint c)
	{
		constraints.remove(c);
	}

	public void addConstraint(Constraint c)
	{
		if(!(constraints.contains(c)))
			constraints.add(c);
	}

	public Vector getConstraints()
	{
		return constraints;
	}
	
	/**
	 * returns copy of the element
	 */
	public GeometryElement copy()
	{
		ImpPoint newAP = new AnchorPoint(getM_label(), getX(), getY());
//		newAP.setM_parent(this.getM_parent());
		newAP.setAllParents(this.getAllParents());
		return newAP;
	}

	// pravin : weight given to each point .. increased when the point
	// participate in some orther line or arc or mark.
	public void increaseWeight()
	{
		if (weight != MAX_WEIGHT)
		{
			weight++;
		}
	}

	public int getWeight()
	{
		return weight;
	}

	public void moveLabel(double lxnew, double lynew)
	{
		lx = (int) lxnew;
		ly = (int) lynew;
	}

	public void moveLabel(ArithElement ae1, ArithElement ae2)
	{
		lx = ae1.value();
		ly = ae2.value();
	}
	
	// Returns the angle between this Point and the new Point w.r.t. horizontal
	public double angle(ImpPoint q)
	{
		double x1 = getX();
		double y1 = getY();
		double x2 = q.getX();
		double y2 = q.getY();

		double angle = Math.atan((double) (y1 - y2) / (double) (x1 - x2));
		return angle;
	}

	public void free()
	{
		setFixed(false);
		U();
	}

	public void fix()
	{
		setFixed(true);
		H();
	}

	// TODO:CHECK  what this function does.
	public boolean inline(ImpPoint prev, ImpPoint pprev)
	{
		if (Math.abs(((pprev.getY() - getY()) * (prev.getX() - getX())) - ((prev.getY() - getY()) * (pprev.getX() - getX()))) < 1E-6)
			return true;
		else
			return false;
	}

	public void print()
	{
		System.out.println("\t(" + getX() + "," + getY() + ")");
	}
	
	public Vector getConstraintsByType(Class classname)
	{
		return constraintsHelper.getConstraintsByType(getConstraints(),classname);
	}
	
	public void deleteConstraints()
	{
		for(;constraints.size()!=0;)
		{
			((Constraint)constraints.get(0)).remove();
			//When a constraint is deleted from the system, it removes it self from all its anchor points. 
			//So, we do not need to explicitly remove it from this anchor point. 
			//constraints.remove(0);
		}
	}
	
	public void removeConstraints()
	{
		for(;constraints.size()!=0;)
			constraints.remove(0);
	}
	
	public void delete()
	{
		
		for(;vecParent.size()!=0;)
			((GeometryElement)vecParent.elementAt(0)).delete();
		
		//Last parent will delete all point constraints of this point. So, this line is not required.
//		deleteConstraints();
	}

}
