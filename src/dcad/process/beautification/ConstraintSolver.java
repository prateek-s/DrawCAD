package dcad.process.beautification;

import java.util.Vector;

import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;

public class ConstraintSolver
{

	private static Vector userMovedPoints=new Vector();
	private static double[] userMovedPointsInitialPositions;

	/**
	 * Get the list of all the Variables
	 * @param allFixedPoints
	 * @param conVector
	 * @return
	 */
	private static AnchorPoint[] getVariableList(Vector allFixedPoints, Vector allPoints)
	{
		Vector hs = (Vector)allPoints.clone();
		hs.removeAll(allFixedPoints);

		AnchorPoint[] apArray ;
		// convert Vector to array
		if(hs.size()==0)
		{
//If no other points are affected, just check whether this movement is valid or not.
//For that, just add the moved point to the existing points and try to solve the constraints using them.
			apArray = new AnchorPoint[allFixedPoints.size()];
			for(int i=0; i<allFixedPoints.size(); i++)
				apArray[i] = (AnchorPoint) allFixedPoints.get(i);
		}
		else
		{
			apArray = new AnchorPoint[hs.size()];
			for(int i=0; i<hs.size(); i++)
				apArray[i] = (AnchorPoint) hs.get(i);
		}
		return apArray;
	}

	private static Vector getUserFixedAnchorPoints(Vector aps)
	{
		Vector result = new Vector();
		for(int i=0;i<aps.size();i++)
		{
			AnchorPoint a = (AnchorPoint)aps.get(i);
			if ( a.isFixed() )
				result.add(a);
		}
		return result;
	}
	
	//Adds the constraints applied using markers.
	public static Vector addConstraintsAppliedUsingMarker(Vector constraints)
	{
		Vector ptsvec = constraintsHelper.getAllAnchorPointsOfConstraints(constraints);
		Vector allAffectedConstraints = constraintsHelper.getListOfConstraints( ptsvec ) ;
		Vector allAffectedPoints = constraintsHelper.getAllAnchorPointsOfConstraints(allAffectedConstraints);
		Vector fixedPoints = new Vector();
		fixedPoints = getUserFixedAnchorPoints(allAffectedPoints);

		NewtonMethod.initializeVariables(getVariableList(fixedPoints,allAffectedPoints));
		NewtonMethod.saveInitialValues();
		
		if(solveConstraints(allAffectedConstraints,fixedPoints,NewtonMethod.fromUserAppliedConstraints,new Vector()))
		{
			///System.out.println(" :) :) :) :) :) Constraint added (: (: (: (: (: ");
			return constraints;
		}


		// Try to see if it can be solved by removing some promoted constraints
		NewtonMethod.movePointsBackToInitialPositions();		
		Vector savedSuspectedPromotedConstraints = new Vector();
		NewtonMethod.removeSuspectedPromotedConstraints(allAffectedConstraints,savedSuspectedPromotedConstraints);
		
		if (savedSuspectedPromotedConstraints.size() != 0)
		{
			///System.out.println("----------------------------------   Dropping some constraints   ----------------------------------");
			if(solveConstraints(allAffectedConstraints,fixedPoints, NewtonMethod.fromUserAppliedConstraints,savedSuspectedPromotedConstraints))
			{
				///System.out.println(" :) :) :) :) :) Constraint added after dropping some promoted constraints (: (: (: (: (: ");
				return constraints;
			}
		}
		constraintsHelper.addAllIgnoreDuplicates(allAffectedConstraints,savedSuspectedPromotedConstraints);
		
		
		//It could not be solved by removing SOME promoted constraints. Remove all of them
		NewtonMethod.movePointsBackToInitialPositions();		
		Vector savedAllPromotedConstraints = new Vector();
		NewtonMethod.removeAllPromotedConstraints(allAffectedConstraints,savedAllPromotedConstraints);

		if (savedAllPromotedConstraints.size() != 0)
		{
			///System.out.println("----------------------------------   Dropping all promoted constraints   ----------------------------------");
			if(solveConstraints(allAffectedConstraints,fixedPoints, NewtonMethod.fromUserAppliedConstraints,savedAllPromotedConstraints))
			{
				///System.out.println(" :) :) :) :) :) Constraint added after dropping all promoted constraints (: (: (: (: (: ");
				return constraints;
			}
		}
		
		NewtonMethod.movePointsBackToInitialPositions();		
		constraintsHelper.removeConstraints(constraints);
		///System.out.println(" :( :( :( :( :( Can not add this constraint ): ): ): ): ): ");
		return null;		
	}
	
	//Adds the constraints identified after drawing / recycling
	public static Vector addConstraintsAfterDrawing(Vector constraints)
	{
		Vector allAffectedConstraints = constraintsHelper.getListOfConstraints( constraintsHelper.getAllAnchorPointsOfConstraints(constraints) ) ;
		Vector allAffectedPoints = constraintsHelper.getAllAnchorPointsOfConstraints(allAffectedConstraints);
		Vector fixedPoints = new Vector();
		fixedPoints = getUserFixedAnchorPoints(allAffectedPoints);

		NewtonMethod.initializeVariables(getVariableList(fixedPoints,allAffectedPoints));
		NewtonMethod.saveInitialValues();
		
		if(solveConstraints(allAffectedConstraints,fixedPoints,NewtonMethod.afterUserDrawingOrRecycling,new Vector()))
		{
			///System.out.println(" :) :) :) :) :) All promoted constraints added (: (: (: (: (: ");
			return constraints;
		}

		// Try to see if it can be solved by removing some promoted constraints
		NewtonMethod.movePointsBackToInitialPositions();		
		Vector savedSuspectedPromotedConstraints = new Vector();
		NewtonMethod.removeSuspectedPromotedConstraints(allAffectedConstraints,savedSuspectedPromotedConstraints);
		
		if (savedSuspectedPromotedConstraints.size() != 0)
		{
			///System.out.println("----------------------------------   Dropping some constraints   ----------------------------------");
			if(solveConstraints(allAffectedConstraints,fixedPoints,NewtonMethod.afterUserDrawingOrRecycling,savedSuspectedPromotedConstraints))
			{
				//Some of the removed promoted constraints might have been added JUST NOW. Remove them. 
				constraints.removeAll(savedSuspectedPromotedConstraints);
				///System.out.println(" :) :) :) :) :) Some constraints dropped and others added (: (: (: (: (: ");
				return constraints;
			}
		}
		
		NewtonMethod.movePointsBackToInitialPositions();		
		constraintsHelper.removeConstraints(constraints);
		///System.out.println(" :( :( :( :( :( Could not add the constraints. System may be inconsistent  ): ): ): ): ): ");
		return new Vector();		
	}
	
	public static Vector solveConstraintsAfterSnapAndRecalculation(Vector anchorPoints)
	{
		Vector allAffectedConstraints = constraintsHelper.getListOfConstraints( anchorPoints ) ;
		Vector allAffectedPoints = constraintsHelper.getAllAnchorPointsOfConstraints(allAffectedConstraints);
		Vector fixedPoints = new Vector();
		fixedPoints = getUserFixedAnchorPoints(allAffectedPoints);

		NewtonMethod.initializeVariables(getVariableList(fixedPoints,allAffectedPoints));
		NewtonMethod.saveInitialValues();
		
		if(solveConstraints(allAffectedConstraints,fixedPoints,NewtonMethod.afterRecalculationAndSnapping,new Vector()))
		{
			///System.out.println(" :) :) :) :) :) All promoted constraints added (: (: (: (: (: ");
			return allAffectedConstraints;
		}

		// Try to see if it can be solved by removing some promoted constraints
		NewtonMethod.movePointsBackToInitialPositions();		
		Vector savedSuspectedPromotedConstraints = new Vector();
		NewtonMethod.removeSuspectedPromotedConstraints(allAffectedConstraints,savedSuspectedPromotedConstraints);
		
		if (savedSuspectedPromotedConstraints.size() != 0)
		{
			///System.out.println("----------------------------------   Dropping some constraints   ----------------------------------");
			if(solveConstraints(allAffectedConstraints,fixedPoints,NewtonMethod.afterRecalculationAndSnapping,savedSuspectedPromotedConstraints))
			{
				//Some promoted constraints were removed. Remove them from the system 
				allAffectedConstraints.removeAll(savedSuspectedPromotedConstraints);
				///System.out.println(" :) :) :) :) :) Some constraints dropped and others added (: (: (: (: (: ");
				return allAffectedConstraints;
			}
		}
		NewtonMethod.movePointsBackToInitialPositions();		
		///System.out.println(" :( :( :( :( :( Could not add the constraints. System may be inconsistent  ): ): ): ): ): ");
		return new Vector();		
	}
	
	/**
	 * 
	 * @param pMoved Vector containing all the moved points, these are actually the fixed points
	 * @return Vector the constraints affected if the move was successful, null otherwise
	 */
	public static Vector solveConstraintsAfterMovement(Vector movedPts,double[] movedPointsPositions)
	{
		try 
		{
			userMovedPoints=(Vector)movedPts.clone();
			userMovedPointsInitialPositions=movedPointsPositions;
			
			// points that is being moved is considered to be a fixed point
			// list of constraints is constraints associated with the fixed points
			Vector allAffectedConstraints = constraintsHelper.getListOfConstraints(movedPts);
			Vector allAffectedPoints = constraintsHelper.getAllAnchorPointsOfConstraints(allAffectedConstraints);
			Vector fixedPoints = new Vector();
			fixedPoints = getUserFixedAnchorPoints(allAffectedPoints);
			
			constraintsHelper.addAllIgnoreDuplicates(fixedPoints,movedPts);
			NewtonMethod.initializeVariables(getVariableList(fixedPoints,allAffectedPoints));
			NewtonMethod.saveInitialValues();
			
			if(solveConstraints(allAffectedConstraints,fixedPoints,NewtonMethod.afterMovement,new Vector()))
			{
				///System.out.println(" :) :) :) :) :) All constraints solved after movement (: (: (: (: (: ");
				return allAffectedConstraints;
			}

			
			
			
			// Try to see if it can be solved by removing some promoted constraints
			NewtonMethod.movePointsBackToInitialPositions();		
			Vector savedSuspectedPromotedConstraints = new Vector();
			NewtonMethod.removeSuspectedPromotedConstraints(allAffectedConstraints,savedSuspectedPromotedConstraints);
			
			if (savedSuspectedPromotedConstraints.size() != 0)
			{
				///System.out.println("----------------------------------   Dropping some constraints   ----------------------------------");
				if(solveConstraints(allAffectedConstraints,fixedPoints,NewtonMethod.afterMovement,savedSuspectedPromotedConstraints))
				{
					///System.out.println(" :) :) :) :) :) All except the dropped promoted promoted constraints solved (: (: (: (: (: ");
					return allAffectedConstraints;
				}
			}
			
			
			
			
			//It could not be solved by removing SOME promoted constraints. Remove all of them
			constraintsHelper.addAllIgnoreDuplicates(allAffectedConstraints,savedSuspectedPromotedConstraints);
			NewtonMethod.movePointsBackToInitialPositions();
			Vector savedAllPromotedConstraints = new Vector();
			NewtonMethod.removeAllPromotedConstraints(allAffectedConstraints,savedAllPromotedConstraints);

			if (savedAllPromotedConstraints.size() != 0)
			{
				///System.out.println("----------------------------------   Dropping all promoted constraints   ----------------------------------");
				if(solveConstraints(allAffectedConstraints,fixedPoints, NewtonMethod.fromUserAppliedConstraints,savedAllPromotedConstraints))
				{
					///System.out.println(" :) :) :) :) :) Constraint added after dropping all promoted constraints (: (: (: (: (: ");
					return allAffectedConstraints;
				}
			}
			
			
			
			
			
			//Constraints could not be solved even after removing some soft constraints
			//So, add back those soft constraints
			//And allow the movement of the user moved points
			//addAllIgnoreDuplicates(allAffectedConstraints,savedSuspectedPromotedConstraints);
			constraintsHelper.addAllIgnoreDuplicates(allAffectedConstraints,savedAllPromotedConstraints);
			NewtonMethod.movePointsBackToInitialPositions();
			fixedPoints.removeAll(userMovedPoints);
			constraintsHelper.addAllIgnoreDuplicates(allAffectedPoints,userMovedPoints);
			
			NewtonMethod.initializeVariables(getVariableList(fixedPoints,allAffectedPoints));
			
			if(solveConstraints(allAffectedConstraints,fixedPoints,NewtonMethod.afterMovement,new Vector()))
			{
				///System.out.println(" :) :) :) :) :) Allowing movement - All constraints solved after movement (: (: (: (: (: ");
				return allAffectedConstraints;
			}
			
			// Try to see if it can be solved by removing some promoted constraints
			NewtonMethod.movePointsBackToInitialPositions();
			moveUserMovedPointsBackToInitialPositions();
			savedSuspectedPromotedConstraints = new Vector();
			NewtonMethod.removeSuspectedPromotedConstraints(allAffectedConstraints,savedSuspectedPromotedConstraints);
			
			if (savedSuspectedPromotedConstraints.size() != 0)
			{
				///System.out.println("----------------------------------   Dropping some constraints   ----------------------------------");
				if(solveConstraints(allAffectedConstraints,fixedPoints, NewtonMethod.afterMovement,savedSuspectedPromotedConstraints))
				{
					///System.out.println(" :) :) :) :) :) Allowing movement - All except the dropped promoted promoted constraints solved (: (: (: (: (: ");
					return allAffectedConstraints;
				}
			}
			
			//This also did not work. Add back the removed promoted constraints
			constraintsHelper.addAllIgnoreDuplicates(allAffectedConstraints,savedSuspectedPromotedConstraints);
			
			
			//It could not be solved by removing SOME promoted constraints. Remove all of them
			NewtonMethod.movePointsBackToInitialPositions();
			moveUserMovedPointsBackToInitialPositions();
			savedAllPromotedConstraints = new Vector();
			NewtonMethod.removeAllPromotedConstraints(allAffectedConstraints,savedAllPromotedConstraints);

			if (savedAllPromotedConstraints.size() != 0)
			{
				///System.out.println("----------------------------------   Dropping all promoted constraints   ----------------------------------");
				if(solveConstraints(allAffectedConstraints,fixedPoints, NewtonMethod.fromUserAppliedConstraints,savedAllPromotedConstraints))
				{
					///System.out.println(" :) :) :) :) :) Constraint added after dropping all promoted constraints (: (: (: (: (: ");
					return allAffectedConstraints;
				}
			}
			
			//This also did not work. Add back the removed promoted constraints
			constraintsHelper.addAllIgnoreDuplicates(allAffectedConstraints,savedAllPromotedConstraints);
			moveUserMovedPointsBackToInitialPositions();
			NewtonMethod.movePointsBackToInitialPositions();
			
			//Nothing worked...
			///System.out.println("\n\n\n :( :( :( :( :( Sorry :) :) :) :) :) \n\n\n");
			
			return new Vector();
		}
		catch(Exception e)
		{
			///System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$   Exception!!!   $$$$$$$$$$$$$$$$$$$$$$$$$");
			///System.out.println(e.toString() + "\n" + e.getMessage());
			e.printStackTrace();
			return new Vector();
		}
		finally
		{
			userMovedPoints=new Vector();
			userMovedPointsInitialPositions=null;
		}
	}
	
	private static void moveUserMovedPointsBackToInitialPositions()
	{
//		///System.out.println("!!!!!!!!!         Final Positions : ");
		for (int j = 0; j < userMovedPoints.size(); j++)
		{
			AnchorPoint old= (AnchorPoint)userMovedPoints.get(j);
			old.move4Constraints(userMovedPointsInitialPositions[j*2],userMovedPointsInitialPositions[j*2+1]);
//			///System.out.println(old.getM_label() + "  " + userMovedPointsInitialPositions[j*2] + "  " + userMovedPointsInitialPositions[j*2+1]);
		}
	}

	/**
	 * checks whether a solution obtained is valid or not and move the points to the final position
	 * @param conVector Vector containing all the constraints
	 * @param arr variables array
	 * @param X contains the values of the variable as calculated by the Solver 
	 * @return boolean
	 */
	public static boolean movePointsAfterSolvingConstraints(Vector fixedPoints)
	{
		for (int j = 0; j < fixedPoints.size(); j++)
		{
			AnchorPoint temp = (AnchorPoint)fixedPoints.get(j);
			temp.move(temp.getM_point().getX(),temp.getM_point().getY());
		}
		// move the points to the specified positions
		for (int j = 0; j < NewtonMethod.apArr.length; j++)
			NewtonMethod.apArr[j].move4Constraints(NewtonMethod.finalPositionsMatrix.get(2 * j, 0), NewtonMethod.finalPositionsMatrix.get(2 * j + 1, 0));
		return true;
	}
	
	private static boolean solveConstraints(Vector constraints, Vector fixedPoints, int calledFrom,Vector savedSuspectedPromotedConstraints)
	{
		NewtonMethod.solve(constraints,fixedPoints,calledFrom);
		int status = NewtonMethod.solutionStatus;
		if (status == NewtonMethod.trivial || status==NewtonMethod.solved)
		{
			constraintsHelper.removeConstraints(savedSuspectedPromotedConstraints);
			if(NewtonMethod.solutionStatus == NewtonMethod.solved)
				movePointsAfterSolvingConstraints(fixedPoints);
			return true;
		}
		return false;
	}

}