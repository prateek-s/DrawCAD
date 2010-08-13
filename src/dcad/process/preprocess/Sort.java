package dcad.process.preprocess;

import java.util.Vector;



class Sort{
	public void bubbleSort(double array[][],int n){
		 double Temp1, Temp2;
		 int i, j;
		    /* Make n passes through the array */
		    for(i=0;i<n;i++)
		        {
		        /* From the first element to the end
		           of the unsorted section */
		        for(j=1;j<(n-i);j++)
		           {
		           /* If adjacent items are out of order, swap them */
		        	 if (array[j-1][1] > array[j][1]) {
				    	  	Temp1 = array[j-1][1];
					        Temp2 = array[j-1][0];
					        array[j-1][1] = array[j][1];
					        array[j-1][0] = array[j][0];
					        array[j][1] = Temp1;
					        array[j][0] = Temp2;
					     
				      }
		           }
		        }  
	  }
	
	public int linearSearch( Vector a, int element )
    {
		int index = 0;
         for(index=0;index < a.size(); index++){
            if( (Integer)a.elementAt(index) == element ){
                return index;
            }
        }

        return -1;     // NOT_FOUND = -1
    }
	}
	

