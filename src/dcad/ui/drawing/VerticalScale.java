package dcad.ui.drawing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;
import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import dcad.Prefs;
import dcad.model.BoundingBox;
import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.circleArc.circularArcConstraint;
import dcad.model.constraint.collinearity.CollinearLinesConstraint;
import dcad.model.constraint.collinearity.CollinearPointsConstraint;
import dcad.model.constraint.connect.IntersectionConstraint;
import dcad.model.constraint.length.EqualRelLengthConstraint;
import dcad.model.constraint.pointOnSegment.pointOnCircularCurveConstraint;
import dcad.model.constraint.pointOnSegment.pointOnLineConstraint;
import dcad.model.constraint.pointOnSegment.pointOnPointConstraint;
import dcad.model.constraint.points.NoMergeConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.ImpPoint;
import dcad.model.geometry.PixelInfo;
import dcad.model.geometry.SegmentPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.Text;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.SegPoint;
import dcad.model.geometry.segment.Segment;
import dcad.model.marker.Marker;
import dcad.process.ProcessManager;
import dcad.process.beautification.ConstraintSolver;
import dcad.process.io.Command;
import dcad.process.preprocess.PreProcessingManager;
import dcad.process.preprocess.PreProcessor;
import dcad.process.recognition.RecognitionManager;
import dcad.process.recognition.constraint.pointOnSegmentRecognizer;
import dcad.process.recognition.marker.MarkerRecogManager;
import dcad.process.recognition.marker.MarkerToConstraintConverter;
import dcad.process.recognition.segment.SegmentRecognizer;
import dcad.process.recognition.stroke.StrokeRecognizer;
import dcad.ui.help.HelpView;
import dcad.ui.main.MainWindow;
import dcad.ui.main.StatusBar;
import dcad.ui.main.WindowActions;
import dcad.ui.recognize.RecognizedView;
import dcad.util.GConstants;
import dcad.util.GMethods;
import dcad.util.GVariables;

import dcad.ui.drawing.*;

/**Class to draw Vertical scale
 * @author Sunil Kumar
 */
public class VerticalScale extends JPanel{
	
    private final double DEPTH_PER_CM = GConstants.cmScaleDrawingRatio;
    private final double CELL_UNIT = DEPTH_PER_CM/10;


	private static int countCm = 0;
	private static int index = 0;
	
	public VerticalScale(){
		setBackground(Color.WHITE);
	}
	
	
	public void setYMoved(int pixelsMoved){
		double cms = (pixelsMoved)/DEPTH_PER_CM;
		index = (int)(cms*10);
		countCm=(int)Math.ceil(index/10.0);
		repaint();
	}
	
	public void paint(Graphics gc)
	{
		 super.paint(gc);
		 int drViewHeight = getHeight();
		 int drViewWidth= getWidth();
	
		 
		 int vertScaleX = drViewWidth; 
		 int scaleTextX = vertScaleX - 30;
		 int scaleCmX = vertScaleX -10;
		 int scaleHalfCmX = vertScaleX - 7;
		 int scaleOtherX = vertScaleX - 4;
		 
		 int row;
 
		 Graphics2D g2 = (Graphics2D)gc;
		 
		 g2.setPaint(Color.WHITE);
		 g2.drawRect(0, 0, drViewWidth, drViewHeight);
		 
		 String str;
		 g2.setPaint(Color.black);
		 
		 int index = VerticalScale.index;
		 int countCm = VerticalScale.countCm;
			
		 g2.drawLine(vertScaleX, 0, vertScaleX, drViewHeight);
		
		 int tempi;
		 for(row = 0 , tempi=1; row <= drViewHeight ; row=(int)(tempi*CELL_UNIT),tempi++){
						g2.drawLine(vertScaleX, row, scaleOtherX, row);
						if((index%10) == 0){
							g2.drawLine(vertScaleX, row, scaleCmX, row);
							
								str = Integer.toString(countCm);
							g2.drawString(str,scaleTextX , row);
							countCm++;
						}
						else if((index%5) == 0){
							g2.drawLine(vertScaleX, row, scaleHalfCmX, row);
						}
						
						
						index++;
		}
	}
}