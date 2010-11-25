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
import javax.swing.JSplitPane;

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

/**Class to draw Horizontal scale
 * @author Sunil Kumar
 */
public class HorizontalScale extends JPanel{

    private final double DEPTH_PER_CM = GConstants.cmScaleDrawingRatio;
	private final double CELL_UNIT = DEPTH_PER_CM/10;
		
	private static int countCm = 0; // keep the count of Cms
	private static int index = 0;  // keep the value of starting point of scale

	
	public HorizontalScale(){
		setBackground(Color.WHITE);
	}
	public void setXMoved(int pixelsMoved){
		
		double cms = pixelsMoved/DEPTH_PER_CM;
		index = (int)(cms*10);
		countCm=(int)Math.ceil(index/10.0);
        /////System.out.println("Debug-- (pixelsMoved,cms,index,countCm)= ("+pixelsMoved+","+cms+","+index+","+countCm+")");
		repaint();
	}
	public void paint(Graphics gc)
	{
		super.paint(gc);
		 int drViewHeight = getHeight();
		
		 int drViewWidth = getWidth();
		
		 int horzScaleY = drViewHeight;
		 int scaleTextY = horzScaleY -14;
		 int scaleCmY = horzScaleY -10;
		 int scaleHalfCmY = horzScaleY - 7;
		 int scaleOtherY = horzScaleY - 4;		 
		 int col;

		 Graphics2D g2 = (Graphics2D)gc;
		 g2.setPaint(Color.WHITE);
		 g2.drawRect(0, 0, drViewWidth, drViewHeight);
		 
		 String str;
		 
		 g2.setPaint(Color.black);
		 
		    int index = HorizontalScale.index;
		    int countCm = HorizontalScale.countCm;
			
		 	g2.drawLine(0, horzScaleY, drViewWidth, horzScaleY);
		
		 	int tempi;
			for(col = 0,tempi=1; col <= drViewWidth; col=(int)(tempi*CELL_UNIT),tempi++){
				g2.drawLine(col, horzScaleY, col, scaleOtherY);
				if((index%10) == 0){
					g2.drawLine(col, scaleOtherY, col, scaleCmY);
					if(index != 0)
						str = Integer.toString(countCm);
					else{
					str = Integer.toString(countCm) + " Cm";
					}
					g2.drawString(str,col-2, scaleTextY);
					countCm++;
				}
				else if((index%5) == 0){
					g2.drawLine(col, scaleOtherY, col, scaleHalfCmY);
				}
				
				index++;
			}		
	} 
}