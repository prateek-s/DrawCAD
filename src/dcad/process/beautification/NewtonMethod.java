package dcad.process.beautification;

import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import dcad.Prefs;
import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.ImpPoint;
import dcad.process.beautification.MathEvaluator.Node;
import dcad.util.GConstants;
import dcad.util.GMethods;
import dcad.util.Maths;

public class NewtonMethod
{
	public static int solutionStatus=-1;
	public static final int trivial=2;
	public static final int solved=3;
	public static final int unsolvable=4;

	public static int solverCalledFrom = -1;
	public static final int fromUserAppliedConstraints = 1;
	public static final int afterMovement = 2;
	public static final int afterUserDrawingOrRecycling = 3;
	public static final int afterRecalculationAndSnapping = 4;
	
	static Vector noOfRowsForEachConstraint=new Vector();
	static Vector indexOfTheConstraintForEachRow=new Vector();
	
	public static Vector suspectedPromotedConstraints;

	public static Matrix finalPositionsMatrix;
	public static Matrix finalErrorMatrix;
	
	public static Matrix initialPositions;
	public static AnchorPoint[] initialVariables;
	
	public static AnchorPoint[] apArr;
	static int debugCount = 0;
	
	/**
	 * Solves for the final position of the all the points if possible
	 * @param conVector Vector containing all the constraints
	 * @param points
	 * @param fixedPoints
	 * @return
	 */
	public static Matrix solve(Vector conVector, Vector fixedPoints, int calledFrom)
	{
		if(isTrivial(fixedPoints,conVector))
		{
			solutionStatus = trivial;
			return null;
		}
		
		int i, j, k;
		AnchorPoint[] points = apArr;
		int no_of_columns = 2 * points.length; // no of variables
		
		Vector nodesErrTemp=getNodesErr(conVector,fixedPoints);
		System.out.println("SOLVE:"+ nodesErrTemp.toString()) ;
		int no_of_rows=nodesErrTemp.size();
		
		// JX = b ?????????
		// the matrix of all the variables.
		Matrix X = new Matrix(no_of_columns, 1);

		// set all the variable names.
		for (j = 0; j < no_of_columns; j++)
			X.set(j, 0, (j % 2) == 0 ? points[j / 2].getX() : points[j / 2].getY());
	
		// Jacobian Matrix
		Matrix J = new Matrix(no_of_rows, no_of_columns);
		
		// Error matrix
		Matrix b = new Matrix(no_of_rows, 1);
		

		// get the Jacobian matrix as the string of Partial derivaties of wrt all the variables.
		Vector nodesJTemp=getNodesJ(conVector, points, fixedPoints);

		///System.out.println("\n\n******************************************** Solve function called... ********************************************\n");

		for(int pq=0;pq<no_of_rows;pq++);
			///System.out.println("Equation no. " + (pq+1) + "  " +((Node)nodesErrTemp.elementAt(pq)).nString);
		///System.out.println("\nAnchor points : ");
		for(int pq=0;pq<points.length;pq++);
			///System.out.println(points[pq].getM_strId() + " (" + points[pq].getM_point().getX() + "," + points[pq].getM_point().getY() + ")");
		///System.out.println("\nFixed points : ");
		for(int pq=0;pq<fixedPoints.size();pq++)
			System.out.print( ((AnchorPoint)fixedPoints.elementAt(pq)).getM_strId() + "  (" + ((AnchorPoint)fixedPoints.elementAt(pq)).getM_point().getX() + "," + ((AnchorPoint)fixedPoints.elementAt(pq)).getM_point().getY() +")");
		///System.out.println("\nDebug count : " + debugCount + " ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		debugCount++;
				
		MathEvaluator tempME = new MathEvaluator();

		tempME.clearFixedVariables();
		for (j = 0; j < fixedPoints.size(); j++)
		{
			tempME.addFixedVariable(((ImpPoint) fixedPoints.elementAt(j)).getM_strId() + ".x",
					((ImpPoint) fixedPoints.elementAt(j)).getX());
			tempME.addFixedVariable(((ImpPoint) fixedPoints.elementAt(j)).getM_strId() + ".y",
					((ImpPoint) fixedPoints.elementAt(j)).getY());
		}

		Vector nodesErr = nodesErrTemp;
		Vector nodesJ = nodesJTemp;
		
		/***********************************************************************
		 * Jacobian iterations started
		 **********************************************************************/
		double prevNorm = java.lang.Double.MAX_VALUE;
		int sameNormCount=0;
		int decreasingNorm=0;
		int critical=0;
		int revisedCriticalIndex=-1;
		
		
		//Used for writing a string for debugging applet
		String textForFile="";
		
		int noOfPoints = X.getRowDimension() / 2;
		boolean [][] alreadyConvergedPoints = new boolean[noOfPoints][noOfPoints];
		for(j=0;j<noOfPoints;j++)
			for(k=0;k<noOfPoints;k++)
				alreadyConvergedPoints[j][k] = false;
		for(j=0;j<noOfPoints;j++)
		{
			Point2D p = new Point2D.Double(X.get( j*2 , 0),X.get( j*2+1 , 0));
			for(int z=j+1;z<noOfPoints;z++)
			{
				Point2D q = new Point2D.Double(X.get( z*2 , 0),X.get( z*2+1 , 0));
				if(p.distance(q) < Prefs.getAnchorPtSize() * 2)
				{
					alreadyConvergedPoints[j][z] = true;
					alreadyConvergedPoints[z][j] = true;
				}
			}
		}
		boolean TwoPointsConvergingToTheSameLocation = false;
		double[][] arrPoints = new double[noOfPoints ][2];
		
		
		
		//Newton method iterations
		for (k = 0; ; k++)
		{

			///System.out.println("\n\n!!!!!!!!!!!!!!!!!   Iteration no. : " + k + "   !!!!!!!!!!!!!!!!!");
			if(k > Prefs.getMinJacobianIterations() && revisedCriticalIndex==-1)
			{
				if(decreasingNorm>10)
					k-=5;
				else
				{
					solutionStatus=unsolvable;
					///System.out.println("-----------------------------Un solvable k > no. of iterations-----------------------------");
					break;
				}
			}
			
			
			
//			debug			textForFile = textForFile + "# Iteration no. : " + (k+1) + "\n";
			textForFile = textForFile + "# Iteration no. : " + (k+1) + "\n";
			
			MathEvaluator me = new MathEvaluator();
			

			for (j = 0; j < points.length; j++)
			{
				double x = X.get( j*2 , 0);
				double y = X.get( j*2+1 , 0);
				me.addVariable(points[j].getM_strId() + ".x",x);
				me.addVariable(points[j].getM_strId() + ".y",y);
				arrPoints[j][0] = x;
				arrPoints[j][1] = y;
//debug			textForFile = textForFile + points[j].getM_strId() + "=" + X.get(j*2,0) + "=" + X.get(j*2+1,0) + "=0\n"; 
				
			}

			
			TwoPointsConvergingToTheSameLocation = false;
			for(j=0;j<noOfPoints;j++)
			{
				Point2D p = new Point2D.Double(arrPoints[j][0],arrPoints[j][1]);
				for(int z=j+1;z<noOfPoints;z++)
				{
					if(alreadyConvergedPoints[j][z])
						continue;
					Point2D q = new Point2D.Double(arrPoints[z][0],arrPoints[z][1]);
					if(p.distance(q) < Prefs.getAnchorPtSize() * 2 && constraintsHelper.haveCommonParent(points[j],points[z]))
					{
						TwoPointsConvergingToTheSameLocation = true;
						break;
					}
				}
				if(TwoPointsConvergingToTheSameLocation)
					break;
			}
			

			//If 2 points are convergint to the same location, return failure.
			//This may be due to over-constrained system or due to some other reasons
			if(TwoPointsConvergingToTheSameLocation)
			{
				solutionStatus = unsolvable;
				break;
			}
			
			
//debug
			for(j=0;j<fixedPoints.size();j++)
			{
				AnchorPoint ap = (AnchorPoint) fixedPoints.get(j);
				textForFile = textForFile + ap.getM_strId() + "=" + ap.getX() + "=" +  ap.getY() + "=1\n";
			}
			
			
			
			
			GMethods.printTime("Updation of nodes",true,true);
			updateJFromNodesJ(me, nodesJ, J);
			updateBFromNodesErr(me, nodesErr, b);
			GMethods.printTime("Updation of nodes",false,true);
			
			///System.out.print("\nError values : ");
			///for(int zz=0;zz<no_of_rows;zz++)
			///	System.out.print((zz+1) +":" + (int)b.get(zz,0)+" ");
			///System.out.print("\nvalues of X : ");
			///for(int zz=0;zz<no_of_columns;zz++)
			///	System.out.print((int)X.get(zz,0)+" ");
			///System.out.println("");
			
				
			double d = b.normF();
			///System.out.println("Norm : " + d);
			if(d==java.lang.Double.NaN)
			{
				solutionStatus=unsolvable;
				break;
			}

			//if(d<100)
			if(d<10)
			{
				if(allConstraintsSolved(conVector,b,fixedPoints,X))
				{
					solutionStatus=solved;
					break;
				}
			}
			// check if the Norm is within range
			if (d < Prefs.getMinAcceptableNorm() )//  || d==prevNorm)
			{
				// set the flag to false (GOOD condition)
				solutionStatus=solved;
				///System.out.println("-----------------------------Solution found d < min norm " + Prefs.getMinAcceptableNorm() + "-----------------------------");
				break;
			}
			
/*			if(d<minimumNorm)
			{
				minimumNorm=d;
				minimumNormPositionsMatrix=(Matrix)X.clone();
				minimumNormErrorMatrix=(Matrix)b.clone();
			}*/
			
			//Count the no. of increases in the norm - It may happen that the norm is oscillating between some values or is constantly increasing
/*			if(d>prevNorm+1)
				increasingNorm++;*/
			
			//Count the no. of iterations from which, the norm has been decreasing constantly
			if(d<prevNorm)
				decreasingNorm++;
			else
				decreasingNorm=0;
			
			//If no. of increases in the norm exceed some value, see if the result found at minimum norm value seen so far was valid or not.
			//If it's valid, return it else assume that this can't be solved
/*			if(increasingNorm>7)
			{
				if(allConstraintsSolved(conVector,minimumNormErrorMatrix,fixedPoints,minimumNormPositionsMatrix))
				{
					b=minimumNormErrorMatrix;
					X=minimumNormPositionsMatrix;
					solutionStatus=solved;
					break;
				}
				else
				{
					solutionStatus=unsolvable;
					break;
				}
				
			}*/
			
			
			GMethods.printTime("SVD and inverse etc.",true,true);

			
			Matrix dx;
			Matrix ainv;

			SingularValueDecomposition svd ;
			
			if(no_of_rows < no_of_columns)
				svd = new SingularValueDecomposition(J.transpose());
			else
				svd = new SingularValueDecomposition(J);
			
			Matrix u = svd.getU();
			
			Matrix s = svd.getS();
			
			Matrix v = svd.getV();
			
			// create a new matrix s1 with dimension same as s, and initilize it to 0
			Matrix s1 = new Matrix(s.getRowDimension(),s.getColumnDimension());
			for (int l = 0; l < s.getRowDimension(); l++)
				for (int n = 0; n < s.getColumnDimension(); n++)
					s1.set(l, n, 0.0);

			System.out.print("Singular values : ");
			for(int lk=0;lk<s.getRowDimension();lk++)
					System.out.print((lk+1) + ":" + s.get(lk,lk) + "   ");
			///System.out.println("");
			
	
//			///System.out.println(" Prev norm. : " + prevNorm + "   Curr. norm : " + d);
			
			//Reinitialize the same norm count
			if(prevNorm - d > (prevNorm / 20))
				sameNormCount =1;
			
			//The norm is decreasing very slowly. Probably we require to revise the critical index.
			//if(d > prevNorm -3)
			if( prevNorm - d < (prevNorm / 100) )
			{
				sameNormCount++;
				///System.out.println("Same norm count : " + sameNormCount);
				if(sameNormCount==3)
				{
					//See if all constraints are solved
					if(allConstraintsSolved(conVector,b,fixedPoints,X))
					{
						solutionStatus=solved;
						///System.out.println("-----------------------------Solution found with same norm thing-----------------------------");
						break;
					}
					else
					{
						if(critical < s.getRowDimension())
						{
							int l=critical;
							if(s.get(critical,critical)>1)
								for( l=critical;l<s.getRowDimension();l++)
									if(isCritical(s.get(l,l),s.get(l-1,l-1),2))
										break;
/*							if(critical == l)
								for( l=critical;l<s.getRowDimension();l++)
									if(isCritical(s.get(l,l),s.get(l-1,l-1),3))
										break;*/
							
							if(critical!=l)
							{
								revisedCriticalIndex = l;
								///System.out.println("\n\nRevising the critical index : " + revisedCriticalIndex + "\n\n");
							}
							
//							if(s.get(critical,critical) > 1)
//							{
//								revisedCriticalIndex=critical+1;
//								///System.out.println("\n\n\n\n\n\n Revising the critical index : " + revisedCriticalIndex);
//							}
							
							else
							{
								solutionStatus = unsolvable;
								///System.out.println("Critical index couldn't be revised");
							}
						}
						else
							solutionStatus=unsolvable;
						if(solutionStatus == unsolvable)
						{
//							///System.out.println("-----------------------------Called for dropping constraints-----------------------------");
							break;
						}
					}
					sameNormCount = 1;
				}
				
			}
			else if( prevNorm - d < (prevNorm / 5) )
			{
				if(allConstraintsSolved(conVector,b,fixedPoints,X))
				{
					///System.out.println("\n\n!!!!!!!!!!              Decrease in norm was less than 20 %. So, I checked whether all constraints are solved. Ane yes... they are               !!!!!!!!!!\n\n");
					solutionStatus = solved;
					break;
				}
			}
				

			if(b.getRowDimension()>=2)//s.getRowDimension()>=3)
			{
				critical = s.getRowDimension();
				for(int l=1;l<s.getRowDimension();l++)
					if(isCritical(s.get(l,l),s.get(l-1,l-1),1))//s.get(l,l)==0 || (s.get(l,l)<(s.get(l-1,l-1)/2)))
					{
						critical=l;
						break;
					}
			}
			else
				critical = s.getRowDimension();
			
			//critical = s.getRowDimension();
			
			//if(debugCount == 3)
				//critical +=2;
			
			//The critical index has been revised after seeing that the solver got stuck at some point
			if(critical < revisedCriticalIndex && revisedCriticalIndex <= s.getRowDimension() )
				critical = revisedCriticalIndex;
				
			///System.out.println("\n\n\nCritical Index : " + critical);

			for(int l=0;l<critical;l++)
				if(s.get(l,l)!=0 && s.get(l,l)>0.001) // If value is greater than 0.001 then only take its reciprocal else don't take it.
					s1.set(l,l,1.0/s.get(l,l));
				else
					break;

			for(int l=critical;l<s.getRowDimension();l++)
				s1.set(l,l,0);
			
			if(no_of_rows < no_of_columns)
				ainv=(u.times(s1)).times(v.transpose());
			else
				ainv=(v.times(s1)).times(u.transpose());
			
			dx = ainv.times(b);


			GMethods.printTime("SVD and inverse etc.",false,true);

			
			//if(k==0)
			{
				GMethods.printMatrix(b," b ",false);
				GMethods.printMatrix(J," J ",false);
				GMethods.printMatrix(u,"U",false);
				GMethods.printMatrix(v,"V",false);
				GMethods.printMatrix(s,"S",false);
				GMethods.printMatrix(s1,"S1",false);
				GMethods.printMatrix(v.times(s1), "V times S1", false);
				GMethods.printMatrix(u.transpose(), "U transpose", false);
				GMethods.printMatrix(ainv, " A inverse ",false);
				GMethods.printMatrix(dx, " dx ",false);
			}
			
			// Update the new values for the variables in X
			///System.out.print("Change in values : ");
			for(int lk=0;lk<X.getRowDimension();lk++) ;
				///System.out.print(dx.get(lk,0) + "  ");
			///System.out.println("");
			
			double divideRatio = 1;
/*			if(debugCount==3 )//&& revisedCriticalIndex == -1)
				divideRatio = 0.9;*/
			
			for (i = 0; i < X.getRowDimension(); i++)
				X.set(i, 0, X.get(i, 0) + (dx.get(i, 0)/divideRatio));
			
			//X.set(i, 0, X.get(i, 0) + (dx.get(i, 0) /2 ));

			System.out.print("New values : ");
			for(int lk=0;lk<X.getRowDimension();lk++) ;
				///System.out.print(X.get(lk,0) + "  ");

			
			prevNorm = d;
				
		}

		//debug
//		///System.out.println("\n\n\n\n\n\n\n\nText to be written to the file : " + textForFile);
		try
		{
			FileWriter fstream = new FileWriter("debugOutput.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(String.valueOf(k+1)+"\n");
			out.write(String.valueOf(points.length + fixedPoints.size()) + "\n");
			out.write(textForFile);
			out.close();
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage());
		}

		
		

		//If solution was not found, find out all probable promoted soft constraints that should be removed
		if(solutionStatus!=solved)
		{
			updateSuspectedPromotedConstraints(conVector,b,calledFrom);
		}

		finalErrorMatrix=b;
		finalPositionsMatrix=X;
		
		return null;

	}
	
	private static void updateSuspectedPromotedConstraints(Vector conVector,Matrix b,int calledFrom)
	{
		suspectedPromotedConstraints=new Vector();
		
		int size = conVector.size();
		for(int i=0;i<size;i++)
		{
			Constraint c = (Constraint)conVector.get(i);
			///System.out.println(c.toString());
			c.isConstraintSolved();
			if( c.isPromoted() && !c.isConstraintSolved() )
			{
				///System.out.println("Failed...");
				suspectedPromotedConstraints.add(new Integer(i));
			}
		}
		
/*
		if(calledFrom == fromUserAppliedConstraints)
		{
			
		}
		else
		{
			int intArray[]=new int[b.getRowDimension()];
			int intArraySize=0;
			
			//Find the indices of the constraints for each row that MAY have problem.
			for(int z=0;z<b.getRowDimension();z++)
				if(   (  (Constraint) conVector.get(((Integer)indexOfTheConstraintForEachRow.elementAt(z)).intValue())  ).isPromoted()   )
					if(b0.get(z,0)==b1.get(z,0))
						intArray[intArraySize++]=((Integer)indexOfTheConstraintForEachRow.elementAt(z)).intValue();
			
			//The intArray may have duplicate entries. Remove the duplicates. intArray will be sorted so just traverse it once and remove duplicates
			if(intArraySize>0)
				suspectedPromotedConstraints.add(new Integer(intArray[0]));
			for(int z=1;z<intArraySize;z++)
			{
				if(intArray[z]==intArray[z-1])
					continue;
//				if(((Constraint)conVector.get(intArray[z])).isPromoted())
				suspectedPromotedConstraints.add(new Integer(intArray[z]));
			}
		}
*/
	}
	
	private static boolean isCritical(double v1,double v2,int powerForRatio)
	{
		if(v1==0)
			return true;
		double ratio=Math.log(v2);
//		///System.out.println("Values : "+ v1 + "  " + v2 + "  Ratio : " + ratio);
		
		//6-5-2008
/*		if(ratio>3)
			ratio-=2;
		else if(ratio>2)
			ratio-=1;*/
		ratio = Math.pow(ratio,powerForRatio);
		if(v1<v2/ratio)
		{
//			///System.out.println("Returning true");
			return true;
		}
//		///System.out.println("Returning false");
		return false;
	}
		
	public static boolean allConstraintsSolved(Vector conVector,Matrix errorMatrix,Vector fixedPoints,Matrix positionsMatrix)
	{
		finalPositionsMatrix=positionsMatrix;
		ConstraintSolver.movePointsAfterSolvingConstraints(fixedPoints);
		int length=conVector.size();
		Constraint c;
//		int currentCount=0;
		for(int i=0;i<length;i++)
		{
			c=(Constraint)conVector.elementAt(i);
/*			double[] a=new double[((Integer)noOfRowsForEachConstraint.elementAt(i)).intValue()];
			for(int j=0;j<((Integer)noOfRowsForEachConstraint.elementAt(i)).intValue();j++,currentCount++)
				a[j]=Math.abs(errorMatrix.get(currentCount,0));*/
			if( !c.isConstraintSolved() )
			{
				///System.out.println("Constraint failed : " + i);
				return false;
			}
		}
		return true;
	}

	public static void saveInitialValues()
	{
		initialVariables=(AnchorPoint[])apArr.clone();
		int no_of_columns = 2 * apArr.length; // no of variables
		initialPositions = new Matrix(no_of_columns, 1);
		for (int j = 0; j < no_of_columns; j++)
			initialPositions.set(j, 0, (j % 2) == 0 ? apArr[j / 2].getX() : apArr[j / 2].getY());
	}
	
	public static void movePointsBackToInitialPositions()
	{
		//Move the points to their initial positions
		for (int j = 0; j < initialVariables.length; j++)
		{
			initialVariables[j].move4Constraints(initialPositions.get(2 * j, 0), initialPositions.get(2 * j + 1, 0));
//			///System.out.println(initialPositions.get(2*j,0) + "  " + initialPositions.get(2*j+1,0));
		}


	}

	public static boolean isTrivial(Vector fixedPoints, Vector constraints)
	{
		//Check if none of the points is affected. If so, just move them
		if(constraints.size()==0 || apArr.length==0)
		{
			for(int i=0; i<fixedPoints.size(); i++)
			{
				ImpPoint ip=(ImpPoint)fixedPoints.elementAt(i);
				ip.move(ip.getX(),ip.getY());
			}
			return true;
		}
		return false;
	}
	
	public static void initializeVariables(AnchorPoint[] a)
	{
		apArr = a;
	}
	
	public static void removeSuspectedPromotedConstraints(Vector allConstraints,Vector saveSuspetedConstraintsHere)
	{
		int size = suspectedPromotedConstraints.size();
		if (size != 0)
		{
			for (int i = 0; i < size; i++)
				suspectedPromotedConstraints.set(i, new Integer(((Integer) suspectedPromotedConstraints.elementAt(i)).intValue() - i));
			for (int i = 0; i < suspectedPromotedConstraints.size(); i++)
			{
				Constraint con = (Constraint) allConstraints.get(((Integer) suspectedPromotedConstraints.elementAt(i)).intValue());
				saveSuspetedConstraintsHere.add(con);
				allConstraints.remove(((Integer) suspectedPromotedConstraints.elementAt(i)).intValue());
			}
		}
	}
	
	public static void removeAllPromotedConstraints(Vector allConstraints,Vector saveAllPromotedConstraintsHere)
	{
		int size = allConstraints.size();
		for(int i=0;i<size;i++)
		{
			Constraint c = (Constraint)allConstraints.get(i);
			if(c.isPromoted())
				saveAllPromotedConstraintsHere.add(c);
		}
		allConstraints.removeAll(saveAllPromotedConstraintsHere);
	}
	
	/**
	 * Updates the Jacobian matix
	 * @param me MathEvaluator
	 * @param strJ Jacobian Matrix where each component is a string
	 * @param J Jacobian Matrix for updation
	 */
	private static void updateJFromNodesJ(MathEvaluator me, Vector nodesJ, Matrix J)
	{
		int no_of_rows = J.getRowDimension();
		int no_of_columns = J.getColumnDimension();
		for (int i = 0; i < no_of_rows; i++)
		{
			for (int j = 0; j < no_of_columns; j++)
			{
				me.setNode(nodesJ.elementAt(i*no_of_columns+j));
				J.set(i, j, me.getValue().doubleValue());
			}
		}
	}
	
	/**
	 * Updates B matrix
	 * @param me MathEvaluator object
	 * @param strErr Error String for constraints
	 * @param b B matrx for updation
	 */
	private static void updateBFromNodesErr(MathEvaluator me, Vector nodesErr, Matrix b)
	{
		int no_of_rows = b.getRowDimension();
		for (int i = 0; i < no_of_rows; i++)
		{
			me.setNode(nodesErr.elementAt(i));
			b.set(i, 0, -1 * me.getValue().doubleValue());
		}
	}
	
	private static Vector getNodesErr(Vector conVector, Vector fixedPoints)
	{
		Vector result = new Vector();
		Iterator iter = conVector.iterator();
		noOfRowsForEachConstraint = new Vector();
		indexOfTheConstraintForEachRow=new Vector();
		int i=0;
		while (iter.hasNext())
		{
			Constraint c = (Constraint) iter.next();
			Vector v = c.getNodesErr();
			result.addAll(v);
			noOfRowsForEachConstraint.add(new Integer(v.size()));
			for(int j=0;j<v.size();j++)
				indexOfTheConstraintForEachRow.add(new Integer(i));
			i++;
		}
		return result;
	}
	
	private static Vector getNodesJ(Vector conVector, AnchorPoint[] points, Vector fixedPoints)
	{
		
			int i, j;
			int no_of_rows = conVector.size(); //no of equations
			int no_of_columns = 2 * points.length; // no of variables

			Vector allNodes = new Vector();
			for (i = 0; i < no_of_rows; i++)
			{
				Vector rows = new Vector();
				Constraint c = (Constraint) conVector.elementAt(i);
				// for each constrait, get the Partial dirrerential string wrt each variable 
				for (j = 0; j < no_of_columns; j++)
				{
					String xory = (j % 2) == 0 ? "x" : "y";
					rows.addAll(c.getPDNodes(points[j / 2].getM_strId() + "." + xory));
				}
				
				// at this point we have all the rows PDs for this Constraint. Now split the rows.
				double num_rows = ((double)rows.size())/no_of_columns;
				if((num_rows-(int)num_rows) != 0)
				{
					System.err.println("!!!!!!!!!!!! ERROR IN getNodesJ function !!!!!!!!!!!!");
				}
				else
				{
					int colcounter=0;
					Node[][] nodeJ_rows = new Node[(int)num_rows][no_of_columns];
					Iterator iter = rows.iterator();
					while (iter.hasNext())
					{
						// fill one col at a time
						for(int row=0; row<(int)num_rows; row++)
						{
							Node objNode = (Node) iter.next();
							nodeJ_rows[row][colcounter] = objNode;
						}
						colcounter++;
					}
					
					for(int row=0; row<(int)num_rows; row++)
						for (j = 0; j < no_of_columns; j++)
							allNodes.add(nodeJ_rows[row][j]);
				}
			}
			return allNodes;
		
	}
	
}
