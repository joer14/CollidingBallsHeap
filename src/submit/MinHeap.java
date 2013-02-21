/*
 * Joe Rowley
 * Jrowley@ucsc.edu
 * *This MinHeap implementation works perfectly fine I believe. 
 * Having issues 
 */

public class MinHeap {
 
    private CollisionData[ ] heapArray;
    private int numEle = 0;  			// Total number of nodes in heap

    public MinHeap(int heapSize) {
        heapArray = new CollisionData[heapSize];
    }
 
    public CollisionData min() {
        emptyCheck(); 
        return heapArray[0];
    }
 
    public CollisionData removeMin() {
    	emptyCheck(); 
        final CollisionData min = heapArray[0];
        heapArray[0] = heapArray[numEle-1];
        if(--numEle > 0) pushDown(0);
        return min;
    }
 
    public void emptyCheck( ) {
        if (numEle == 0) throw new RuntimeException( "HEAP IS FULLL!!!" );
    }
    
    public void fixPointers() {
    	System.out.println("Fixing Pointers!!!");
    	for (int i=0; i<heapArray.length; i++){
    		if(heapArray[i] != null) {
    			heapArray[i].heapPointer=i;
    			//System.out.println(i);
    		}
    	}
    }
    
    public int add( final CollisionData newest ) {
    	//System.out.println("Adding");
    	//emptyCheck(); 
        heapArray[numEle] = newest;
        int index = pushUp(numEle);
        numEle++;
        return index;
    }
 
    public void changeTime( int index, double time ) {
    	System.out.println("Changing Time");
    	heapArray[index].collisionTime = time;
    }
    
    private int pushUp( final int index ) {
        if( index > 0 ) {
            final int parent = ( index - 1 ) / 2;
            if( heapArray[ parent ].collisionTime > heapArray[ index ].collisionTime ) {
                swapCollisions( parent, index );
                pushUp(parent);
            }
        }
        return index;
    }
 
    private void pushDown( int index ) {
        //System.out.println("pushDown");
        final int lChild = 2 * index + 1;
        final int rChild = 2 * index + 2;
 
        // Check if the children are outside the heapArray bounds.
        if( rChild >= numEle && lChild >= numEle ) return;
 
        // Determine the smallest child out of the left and right children.
        final int smallestChild = 
            heapArray[ rChild ].collisionTime > heapArray[ lChild ].collisionTime ? lChild : rChild;
 
        if( heapArray[ index ].collisionTime > heapArray[ smallestChild ].collisionTime ) {
            swapCollisions( smallestChild, index );
            pushDown( smallestChild );
        }
    }
 

    private void swapCollisions(int x, int y ) {
    	CollisionData temp = heapArray[x];
    	heapArray[x] = heapArray[y];
    	heapArray[y] = temp;
        fixPointers();        
    }
    
    private void swap( CollisionData x, CollisionData y ) {
        CollisionData temp = x;
        x = y;
        y = temp;
    }
    public void printheapArray(){
    	System.out.println("heapArray:::::::::");
    	//System.out.println("Heap Length:  " + heapArray.length);
    	//System.out.println("Heap Min Collision Time:  " + heapArray[0].collisionTime + 
    	//	" first : "+ heapArray[0].first + " , Second:  " + heapArray[0].second );
    
    	for (int i = 0; i< 5; i++){ //heapArray.length
    		if (heapArray[i] != null){
    			//if(heapArray[i].collisionTime != Integer.MAX_VALUE) { // " heap pointer: " + heapArray[i].heapPointer
        	System.out.println( heapArray[i].first + " , " + heapArray[i].second + ", Collision Time "
        			+ heapArray[i].collisionTime );
        	}
//        	else  if (i< heapArray.length-2){
//        		if (heapArray[i+1] != null) System.out.println (" NUll HEAP ELEMENT? ");
//        	}
        }//}
    }
    
    
      public void minHeapify(int index){
    	//not doing anything with index
      	//System.out.println("Min Heap Before");
      	//printheapArray();   	  
    	  for (int i=0; i<heapArray.length; i++){
    	  	pushDown(i);
    	  	}
    	  
      	//System.out.println("Min Heap After");
  		printheapArray();
		}
    
    
//    public void minHeapify(int index){
//    	System.out.println("Min Heap Before");
//    	printheapArray();
//    	int left = 2*index;
//    	int right = 2*index + 1;
//    	int smallest = index;
//    	if (left < heapArray.length){
//    		smallest = left;
//    	}else smallest = index;
//    	if (right < heapArray.length) {
//    		if (heapArray[right].collisionTime < heapArray[smallest].collisionTime){
//    			smallest = right;
//    		}
//    	}
//    	if (smallest != index) {
//    		swap(heapArray[index],heapArray[smallest]);
//    		minHeapify( smallest);
//    	}
//    	System.out.println("After Minheapify");
//    	printheapArray();
//
//    }
//
}