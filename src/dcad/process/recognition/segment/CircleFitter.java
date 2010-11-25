// Copyright (c) 2005, Luc Maisonobe
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with
// or without modification, are permitted provided that
// the following conditions are met:
// 
//    Redistributions of source code must retain the
//    above copyright notice, this list of conditions and
//    the following disclaimer. 
//    Redistributions in binary form must reproduce the
//    above copyright notice, this list of conditions and
//    the following disclaimer in the documentation
//    and/or other materials provided with the
//    distribution. 
//    Neither the names of spaceroots.org, spaceroots.com
//    nor the names of their contributors may be used to
//    endorse or promote products derived from this
//    software without specific prior written permission. 
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
// CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
// PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
// THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
// USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
// IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
// USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package dcad.process.recognition.segment;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

/** Class fitting a circle to a set of points.
 * <p>This class implements the fitting algorithms described in the
 * paper <a
 * href="http://www.spaceroots.org/documents/circle/circle-fitting.pdf">
 * Finding the circle that best fits a set of points</a></p>
 * @author Luc Maisonobe
 */
public class CircleFitter
{
	public void doFit(double[][] stroke) throws Exception
	{
			// read the points, ignoring blank lines and comment lines
			ArrayList list = new ArrayList();
			int l = stroke.length;
			for (int i = 0; i < stroke.length; i++)
			{
				// parse the fields and add the point to the list
				list.add(new Point2D.Double(stroke[i][0], stroke[i][1]));
			}
			Point2D.Double[] points = (Point2D.Double[]) list
					.toArray(new Point2D.Double[list.size()]);

			DecimalFormat format = new DecimalFormat("00000.00000000",
					new DecimalFormatSymbols(Locale.US));

			initialize(points);
/*			///System.out.println("initial circle: "
					+ format.format(getCenter().x) + " "
					+ format.format(getCenter().y) + " "
					+ format.format(getRadius()));*/

			// minimize the residuals
			int iter = minimize(1000, 0.02, 1.0e-4);
/*			///System.out.println("converged after " + iter + " iterations");
			///System.out.println("final circle: "
					+ format.format(getCenter().x) + " "
					+ format.format(getCenter().y) + " "
					+ format.format(getRadius()));*/

	}

	/** Build a new instance with a default current circle.
	 */
	public CircleFitter()
	{
		center = new Point2D.Double(0.0, 0.0);
		rHat = 1.0;
		points = null;
	}

	/** Initialize an approximate circle based on all triplets.
	 * @param points circular ring sample points
	 * @exception LocalException if all points are aligned
	 */
	public void initialize(Point2D.Double[] points) throws LocalException
	{

		// store the points array
		this.points = points;

		// analyze all possible points triplets
		center.x = 0.0;
		center.y = 0.0;
		int n = 0;
		for (int i = 0; i < (points.length - 2); ++i)
		{
			Point2D.Double p1 = (Point2D.Double) points[i];
			for (int j = i + 1; j < (points.length - 1); ++j)
			{
				Point2D.Double p2 = (Point2D.Double) points[j];
				for (int k = j + 1; k < points.length; ++k)
				{
					Point2D.Double p3 = (Point2D.Double) points[k];

					// compute the triangle circumcenter
					Point2D.Double cc = circumcenter(p1, p2, p3);
					if (cc != null)
					{
						// the points are not aligned, we have a circumcenter
						++n;
						center.x += cc.x;
						center.y += cc.y;
					}
				}
			}
		}

		if (n == 0)
		{
			throw new LocalException("all points are aligned");
		}

		// initialize using the circumcenters average
		center.x /= n;
		center.y /= n;
		updateRadius();

	}

	/** Update the circle radius.
	 */
	private void updateRadius()
	{
		rHat = 0;
		for (int i = 0; i < points.length; ++i)
		{
			double dx = points[i].x - center.x;
			double dy = points[i].y - center.y;
			rHat += Math.sqrt(dx * dx + dy * dy);
		}
		rHat /= points.length;
	}

	/** Compute the circumcenter of three points.
	 * @param pI first point
	 * @param pJ second point
	 * @param pK third point
	 * @return circumcenter of pI, pJ and pK or null if the points are aligned
	 */
	private Point2D.Double circumcenter(Point2D.Double pI, Point2D.Double pJ,
			Point2D.Double pK)
	{

		// some temporary variables
		Point2D.Double dIJ = new Point2D.Double(pJ.x - pI.x, pJ.y - pI.y);
		Point2D.Double dJK = new Point2D.Double(pK.x - pJ.x, pK.y - pJ.y);
		Point2D.Double dKI = new Point2D.Double(pI.x - pK.x, pI.y - pK.y);
		double sqI = pI.x * pI.x + pI.y * pI.y;
		double sqJ = pJ.x * pJ.x + pJ.y * pJ.y;
		double sqK = pK.x * pK.x + pK.y * pK.y;

		// determinant of the linear system: 0 for aligned points
		double det = dJK.x * dIJ.y - dIJ.x * dJK.y;
		if (Math.abs(det) < 1.0e-10)
		{
			// points are almost aligned, we cannot compute the circumcenter
			return null;
		}

		// beware, there is a minus sign on Y coordinate!
		return new Point2D.Double((sqI * dJK.y + sqJ * dKI.y + sqK * dIJ.y)
				/ (2 * det), -(sqI * dJK.x + sqJ * dKI.x + sqK * dIJ.x)
				/ (2 * det));

	}

	/** Minimize the distance residuals between the points and the circle.
	 * <p>We use a non-linear conjugate gradient method with the Polak and
	 * Ribiere coefficient for the computation of the search direction. The
	 * inner minimization along the search direction is performed using a
	 * few Newton steps. It is worthless to spend too much time on this inner
	 * minimization, so the convergence threshold can be rather large.</p>
	 * @param maxIter maximal iterations number on the inner loop (cumulated
	 * across outer loop iterations)
	 * @param innerThreshold inner loop threshold, as a relative difference on
	 * the cost function value between the two last iterations
	 * @param outerThreshold outer loop threshold, as a relative difference on
	 * the cost function value between the two last iterations
	 * @return number of inner loop iterations performed (cumulated
	 * across outer loop iterations)
	 * @exception LocalException if we come accross a singularity or if
	 * we exceed the maximal number of iterations
	 */
	public int minimize(int iterMax, double innerThreshold,
			double outerThreshold) throws LocalException
	{

		computeCost();
		if ((J < 1.0e-10) || (Math.sqrt(dJdx * dJdx + dJdy * dJdy) < 1.0e-10))
		{
			// we consider we are already at a local minimum
			return 0;
		}
		int iterations = 0;
		double previousJ = J;
		double previousU = 0.0, previousV = 0.0;
		double previousDJdx = 0.0, previousDJdy = 0.0;
		for (; iterations < iterMax;)
		{

			// search direction
			double u = -dJdx;
			double v = -dJdy;
			if (iterations != 0)
			{
				// Polak-Ribiere coefficient
				double beta = (dJdx * (dJdx - previousDJdx) + dJdy
						* (dJdy - previousDJdy))
						/ (previousDJdx * previousDJdx + previousDJdy
								* previousDJdy);
				u += beta * previousU;
				v += beta * previousV;
			}
			previousDJdx = dJdx;
			previousDJdy = dJdy;
			previousU = u;
			previousV = v;

			// rough minimization along the search direction
			double innerJ;
			do
			{
				innerJ = J;
				double lambda = newtonStep(u, v);
				center.x += lambda * u;
				center.y += lambda * v;
				updateRadius();
				computeCost();
			} while ((++iterations < iterMax)
					&& ((Math.abs(J - innerJ) / J) > innerThreshold));

			// global convergence test
			if ((Math.abs(J - previousJ) / J) < outerThreshold)
			{
				error = (Math.abs(J - previousJ) / J);
				return iterations;
			}
			previousJ = J;

		}
		error = (Math.abs(J - previousJ) / J);
		return iterations;
// no need for error to be thrown even if it cannto converafter 
/*		throw new LocalException("unable to converge after " + iterMax
				+ " iterations");
*/
	}

	/** Compute the cost function and its gradient.
	 * <p>The results are stored as instance attributes.</p>
	 */
	private void computeCost() throws LocalException
	{
		J = 0;
		dJdx = 0;
		dJdy = 0;
		for (int i = 0; i < points.length; ++i)
		{
			double dx = points[i].x - center.x;
			double dy = points[i].y - center.y;
			double di = Math.sqrt(dx * dx + dy * dy);
			if (di < 1.0e-10)
			{
				throw new LocalException("cost singularity:"
						+ " point at the circle center");
			}
			double dr = di - rHat;
			double ratio = dr / di;
			J += dr * (di + rHat);
			dJdx += dx * ratio;
			dJdy += dy * ratio;
		}
		dJdx *= 2.0;
		dJdy *= 2.0;
	}

	/** Compute the length of the Newton step in the search direction.
	 * @param u abscissa of the search direction
	 * @param v ordinate of the search direction
	 * @return value of the step along the search direction
	 */
	private double newtonStep(double u, double v)
	{

		// compute the first and second derivatives of the cost
		// along the specified search direction
		double sum1 = 0, sum2 = 0, sumFac = 0, sumFac2R = 0;
		for (int i = 0; i < points.length; ++i)
		{
			double dx = center.x - points[i].x;
			double dy = center.y - points[i].y;
			double di = Math.sqrt(dx * dx + dy * dy);
			double coeff1 = (dx * u + dy * v) / di;
			double coeff2 = (di - rHat) / di;
			sum1 += coeff1 * coeff2;
			sum2 += coeff2;
			sumFac += coeff1;
			sumFac2R += coeff1 * coeff1 / di;
		}

		// step length attempting to nullify the first derivative
		return -sum1
				/ ((u * u + v * v) * sum2 - sumFac * sumFac / points.length + rHat
						* sumFac2R);

	}

	/** Get the circle center.
	 * @return circle center
	 */
	public Point2D.Double getCenter()
	{
//		///System.out.println("Center : " + center.getX() + " " + center.getY());
		return center;
	}

	/** Get the circle radius.
	 * @return circle radius
	 */
	public double getRadius()
	{
//		///System.out.println("Radius : " + rHat);
		return rHat;
	}

	/** Local exception class for algorithm errors. */
	public static class LocalException extends Exception
	{
		/** Build a new instance with the supplied message.
		 * @param message error message
		 */
		public LocalException(String message)
		{
			super(message);
		}
	}

	/** Current circle center. */
	private Point2D.Double center;

	/** Current circle radius. */
	private double rHat;

	/** Circular ring sample points. */
	private Point2D.Double[] points;

	/** Current cost function value. */
	private double J;

	/** Current cost function gradient. */
	private double dJdx;

	private double dJdy;
	
	private double error = Double.MAX_VALUE;

	public double getError()
	{
		return error;
	}
}
