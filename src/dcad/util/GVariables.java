package dcad.util;

import java.awt.Color;
import java.awt.Cursor;

import dcad.ui.drawing.DrawingView;
import dcad.ui.main.MainWindow;

public class GVariables
{
	// color codes for drawing
	public static Color BACKGROUND_COLOR = Color.WHITE;
	public static Color FIXED_COLOR = Color.LIGHT_GRAY;
	public static Color RAW_STROKE_COLOR = Color.LIGHT_GRAY;
	
//	public static Color DRAWING_ASSIST_COLOR = new Color(255, 210, 180);//Color.YELLOW;
	public static Color DRAWING_ASSIST_COLOR = new Color(255, 130, 180);//Color.YELLOW;
	public static Color RECOGNIZED_COLOR = Color.BLACK;
	public static Color SOFT_CONSTRAINT_COLOR = new Color(255,165,00);
	public static Color HARD_CONSTRAINT_COLOR = new Color(110, 110, 110);

	public static Color DRAWING_COLOR = Color.BLACK;
	public static Color EDIT_COLOR = Color.RED;
	public static Color SELECTED_COLOR = new Color(0,255,127);
	public static Color HIGHLIGHTED_COLOR = Color.MAGENTA;
	public static Color SELECTED_FIXED_COLOR = Color.RED;
	public static Color HIGHLIGHTED_SELECTED_COLOR = Color.PINK;
	public static Color GRID_COLOR = new Color(224, 238, 224);
	public static int DRAWING_MODE = GConstants.DRAW_MODE;
	public static Color DEF_IP_COLOR = new Color(135,205,250);
	public static Color MARKER_COLOR = Color.blue;
	public static boolean undoing=false;
	
	public static int getDRAWING_MODE()
	{
		return DRAWING_MODE;
	}

	public static void setDRAWING_MODE(int drawing_mode)
	{
		DRAWING_MODE = drawing_mode;
		DrawingView dv = GMethods.getCurrentView();;
		if(dv != null)
		{
			switch (DRAWING_MODE)
			{
			case GConstants.DRAW_MODE:

		        dv.setCursor(MainWindow.getM_defCursor());
				break;

			case GConstants.EDIT_MODE:
				dv.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				break;

			default:
				break;
			}
		}
	}
}
