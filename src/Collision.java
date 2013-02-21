
public class Collision {
	int idBall1;//points to 2d array
	int idBall2;// other pointer to 2d array
	int heapPointer; // points at heap for specifc collision
	double collisionTime;
    Collision(int idBall1, int idBall2, double collisionTime, int heapPointer) { 
    	this.heapPointer = heapPointer;
		this.idBall1 =idBall1;
		this.idBall2 =idBall2;
		this.collisionTime = collisionTime;
	}
    
}
