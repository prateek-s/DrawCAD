package dcad.model.constraint.points;

import java.awt.Graphics;
import java.util.Vector;

import dcad.model.constraint.IndependentPointConstraints;
import dcad.model.geometry.AnchorPoint;

public class NoMergeConstraint extends IndependentPointConstraints
{
		public NoMergeConstraint(AnchorPoint ap1, AnchorPoint ap2,int category,boolean promoted)
		{
			super(new AnchorPoint[]{ap1,ap2},category,promoted);
			setDeleted(true);
		}
		
		public String toString()
		{
			AnchorPoint m_ap1 = (AnchorPoint)points.elementAt(0);
			AnchorPoint m_ap2 = (AnchorPoint)points.elementAt(1);
			return addPrefix()+" * No Merge Constraint on "+ m_ap1.getM_strId() + " and " + m_ap2.getM_strId();
		}
		
		public Vector getEquation(Vector fixedPoints)
		{
			return new Vector();
		}

		public Vector getPartialDifferentialString(String var, Vector fixedPoints)
		{
			return new Vector();
		}

		public boolean isConstraintSolved()
		{
			return true;
		}
		
		public void draw(Graphics g)
		{
		}
}