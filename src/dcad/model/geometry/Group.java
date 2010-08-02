package dcad.model.geometry;

import java.awt.Graphics;
import java.util.Iterator;
import java.util.Vector;

/**
 * Class to store information about segments as groups (for editing purposes)
 * @author vishalk
 *
 */
public class Group extends GeometryElement
{
	// vector for storing all the elements belonging to the grp
	private Vector m_elements;
	
	public Group()
	{
		m_elements = new Vector();
	}

	public void draw(Graphics g)
	{
		if(!isEnabled()) return;
		super.draw(g);

		// code to call draw methods of individual elements, thus in effect printing the 
		// whole group
		Iterator iter = m_elements.iterator();
		while (iter.hasNext())
		{
			GeometryElement element = (GeometryElement) iter.next();
			element.draw(g);
		}
	}
	
	public void addElement(GeometryElement theElem)
	{
		m_elements.add(theElem);
	}

	public void removeElement(GeometryElement theElem)
	{
		m_elements.remove(theElem);
	}

	public boolean containsPt(double x, double y)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void move(int x1, int y1, int x2, int y2)
	{
		// TODO Auto-generated method stub
		
	}

	public GeometryElement copy()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
