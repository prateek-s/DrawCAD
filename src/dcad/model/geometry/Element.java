package dcad.model.geometry;

import java.awt.Color;
import java.io.Serializable;

import dcad.util.MyColor;

/**
 * Element is the parent class of all the elements displayed on the drawing view. It contains
 * common fields like id of each element and color definitions etc.
 *
 */
abstract class Element implements Serializable
{
	protected Color def_hiColor = new Color(255, 0, 100, 50);
	protected Color def_selColor = Color.RED;

	// for storing a global unique ID for all the elements.
	public static int globalID = 0; 
	
	protected String m_strId = "";
	protected String m_label = "";
	protected Color m_color = Color.BLACK;
	protected Color prevColor = m_color;
	protected boolean hideLabel = false;
	protected boolean highlighted = false;
	protected boolean enabled = true;
	protected boolean hidden = false;
	protected boolean selected = false;
	protected boolean update = false;

	public Element()
	{
		// whenever a new element is created, give it a unique
		globalID++;
		m_strId = Integer.toString(globalID);
		setM_color(Color.BLACK);
	}
	
	public void update() {}

	public String getM_strId()
	{
		return m_strId;
	}

	public void setM_strId(String id)
	{
		m_strId = id;
	}

	public String getM_label()
	{
		if((m_label==null)||(m_label.trim()==""))
		return getM_strId();
		
		return m_label;
	}

	public void setM_label(String m_label)
	{
		this.m_label = m_label;
	}

	public Color getM_color()
	{
		return m_color;
	}

	public void setM_color(Color color)
	{
		this.prevColor = this.m_color;
		this.m_color = color;
	}
	
	public boolean equals(Object obj)
	{
		if(obj instanceof Element)
		{
			Element ele = (Element)obj;
			return (ele.m_strId == m_strId);
		}
		return false;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isHidden()
	{
		return hidden;
	}

	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
	}

	public boolean isHideLabel()
	{
		return hideLabel;
	}

	public void setHideLabel(boolean hideLabel)
	{
		this.hideLabel = hideLabel;
	}

	public boolean isHighlighted()
	{
		return highlighted;
	}

	public void setHighlighted(boolean highlighted)
	{
		this.highlighted = highlighted;
	}
	
	public String toString()
	{
		return getM_strId();
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	public Color getDef_hicolor()
	{
		return def_hiColor;
	}

	public void setDef_hicolor(Color hicolor)
	{
		this.def_hiColor = hicolor;
	}

	public boolean isUpdate()
	{
		return update;
	}

	public void setUpdate(boolean update)
	{
		this.update = update;
	}

	public void H(Color c)
	{
		if(!isSelected())
		{
			prevColor = getM_color();
		}
		setM_color(c);
		highlighted = true;
	}
	
	public void H()
	{
		H(getDef_hicolor());
	}

	public void H(String s)
	{
		H(MyColor.getColor(s));
	}

	public void U()
	{
		if (highlighted)
		{
			if(selected)
			{
				setM_color(def_selColor);
			}
			else
			{
				setM_color(prevColor);
			}
			highlighted = false;
		}
	}

	public void select()
	{
		prevColor = getM_color();
		setM_color(def_selColor);
		selected = true;
	}

	public void unSelect()
	{
		if(selected)
		{
			//ISHWAR removed this statement because the color became Black after deleting some segment
//			setM_color(prevColor);
			selected = false;
		}
	}
	
	public boolean isGreater(Element ele)
	{
		if(Integer.parseInt(getM_strId()) > Integer.parseInt(ele.getM_strId()))
			return true;
		return false;
	}

	public Color getPrevColor()
	{
		return prevColor;
	}

	public void setPrevColor(Color prevColor)
	{
		this.prevColor = prevColor;
	}
}
