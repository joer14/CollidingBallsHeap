/* 
 /*
 * Modified by Joe Rowley
 * Jrowley@ucsc.edu
 * 
 * 
 
 Version 1.1
 Fixes the stickyness of the balls.
 Fewer missed collisions. 
 Numerical inaccuracies cause slight drift in the balls. 
 */

/*
 * The program draws a random number of colliding balls on a square grid and computes their paths
 * based on simple rules of physics. When a collision occurs, balls will act as per rules of physics
 * the direction and speed of their paths follows those rules. 
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;

//class CollisionData implements Comparable<CollisionData>{ 
class CollisionData implements Comparable{ 
	int first; // which ball
	int second; // either another ball or wall with which ball is colliding. 
				// -ve(-1 top, -2 right, -3 bot, -4 left) indicates wall, 
				// positive indicates another ball.
	double collisionTime;
	int collisionId;
	int heapPointer;
	Vector targetPos1;
	Vector targetPos2;
	boolean valid;
	
	CollisionData(int f, int s, double t, int p) {
		first = f;
		second = s;
		collisionTime = t;
		collisionId = 0;
		valid = true;
		heapPointer = p;
	}
	
	public String toString() {
		return " cid = " + collisionId + " " + first + " vs " + second + " @" + collisionTime +
		" valid = " + valid + " ";
	}
	
	public int compareTo(CollisionData c) { 
		return (new Double(collisionTime)).compareTo(new Double(c.collisionTime));
	}
	public int compareTo(Object cd) { 
		CollisionData c = (CollisionData) cd;
		return (new Double(collisionTime)).compareTo(new Double(c.collisionTime));
	}
}

public class CollidingBalls extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int nballs;
	int nwalls;
	Ball[] balls;
	Wall[] walls;
	double globalTime;                     // ticking time, increments once per each update..
	static double increment;
	double timeStep;
	int tempvar;
    CollisionData[][] collisionTable;                // table that keeps track of time of collision between all objects.
	int[] rowMinimum;                      // keep track which entry is min for each row in collisionTable.
	CollisionData[] collisions;            // hold the collisions that need to be handled at some time T in future.
	int collisionCount;
	boolean verbose;
	MinHeap heap;
	public static final int X_RANGE = 600;
	public static final int Y_RANGE = 600;
	public static final int NUM_WALLS = 4;
	int numCollisions;
	volatile int stepP, animateP, animDelay;
	
	CollidingBalls(int n) {
		nballs = n;
		nwalls = NUM_WALLS;
		balls = new Ball[n+1];
		globalTime = 0;
		increment = 0.5;
		timeStep = 1;
		tempvar = 1;
		numCollisions = 0;
		verbose = false;
		
		/* gui button stuff */
		stepP = 0;
		animateP = 0;
		animDelay = 10;

		for(int i=1;i<=n;i++) { 
			balls[i]  = new Ball(i);
		}

		walls = new Wall[NUM_WALLS+1];
		// the walls are planes represented by a point and a normal vector, hard coded for now..
		walls[1] = new Wall(1, new Vector(10,10), new Vector(0, 1));
		walls[2] = new Wall(2, new Vector(X_RANGE-10,10), new Vector(-1, 0));
		walls[3] = new Wall(3, new Vector(10,Y_RANGE-10), new Vector(0, -1));
		walls[4] = new Wall(4, new Vector(10,10), new Vector(1, 0));
		int nCollisionObjects = nballs + nwalls;
		heap = new MinHeap((nCollisionObjects)*(nCollisionObjects)*(nCollisionObjects));
		
		collisionTable = new CollisionData[1+nCollisionObjects] [1 + nCollisionObjects];
		
		for(int i=0;i<=nCollisionObjects;i++) { 
			for(int j=0;j<=nCollisionObjects;j++) {
				CollisionData a = new CollisionData(i,j,Integer.MAX_VALUE, 0);
				a.first = i;
				a.second = j;
				heap.add(a);
				
				collisionTable[i][j] = a;
				//heap.printheapArray();
				// 
			}
		}
		rowMinimum = new int[1+nCollisionObjects];
		collisions = new CollisionData[nCollisionObjects];
		collisionCount = 0;
	}
	
	public void modifyBall(int id, Vector pos, Vector speed, double radius) {
		balls[id].setPosition(pos);
		balls[id].setSpeed(speed);
		balls[id].setRadius(radius);
	}

	public void writeConfig(String fileName) throws Exception { 
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		bw.write(" " + nballs + "\n");
		for(int i=1;i<=nballs;i++) {
			Vector p = balls[i].getPosition();
			Vector s  = balls[i].getSpeed();
			String str = " " + balls[i].getRadius() + " "+ p.getComponent(0)  + " " + p.getComponent(1)+ " " + s.getComponent(0) + " " + s.getComponent(1) + " \n";
			bw.write(str);
		}
		bw.close();
	}
	/* Check if there are any collisions to be handled at this current time 
	 * Note that multiple collisions may occur at same time, hence use of an array.
	 */
	public void DetectCollisions() { 
		if ( collisionCount == 0 ) { 
			return;
		}
		
		CollisionData cd = collisions[0];
		while (cd.collisionTime <= globalTime +increment) { 
			// time to handle these collisions
			// do graphics stuff
			for(int i=0;i<collisionCount;i++) { 
				 cd = collisions[i];
				 if(verbose) System.out.println("Handling " + cd);
				 handleCollision(cd);
				 
			}
			
			//figure out what collisions will occur
			
			for(int i=0;i<collisionCount;i++) { 
				 cd = collisions[i];
				 if (cd.first > nwalls) {
				 computeCollisions(cd.first - nwalls);
				 }
				 if (cd.second > nwalls) {
					 computeCollisions(cd.second - nwalls);
				 }
			}
			
			//figure out collision times
			
			computeCollisionTimes();
			
			if (verbose) { 
				System.out.println("Location of Balls");
				for(int i=1;i<=nballs;i++) {
					System.out.println(balls[i]);
				}
				System.out.println("Collision Table");
				printTable();
			}
			
			if ( collisionCount > 0 ) { 
				cd = collisions[0];
			} 
			else { 
				break;
			}
			
		} 
	}
	
	/*
	 * Wall = 1 is top, Wall = 3 is bottom
	 * Wall = 2 is right, Wall = 4 is left
	 */
	public void doCollisionWithWall(CollisionData cd) { 
		
		//int wallId = cd.second;
		//int ballId = cd.first;
		int wallId = (cd.second > cd.first )?cd.first:cd.second; 
		int ballId = (cd.second > cd.first )?cd.second:cd.first; 
		ballId = ballId - nwalls;
		Ball b = balls[ballId];
		b.setPosition(cd.targetPos1);
		
		if (wallId %2 == 1) { 
			// top or bottom walls, just flip the y component of the velocity
			Vector sp = b.getSpeed(); 
			sp.componentMult(new Vector(1, -1));
			b.setSpeed(sp);
		}
		else { 
			// left or right, flip the x component..
			Vector sp = b.getSpeed(); 
			sp.componentMult(new Vector(-1, 1));
			b.setSpeed(sp);
		}
	}
	
	
	/* Handle collisions of different balls 
	 *  with m = mass , v = velocity and r = center of ball.
	 *  in a elastic collision between two balls (m1, v1, r1) and (m2, v2, r2), the new velocities
	 *  in the one dimensional case are
	 *  v1' = (m1-m2)/(m1+m2) * v1 + 2*m2/(m1+m2) * v2
	 *  v2' = (m2-m1)/(m1+m2) * v2 + 2*m1(m1+m2) * v1
	 *  
	 *  Even in a two dimensional case, as is the case here, once we compute the vector components in the direction
	 *  of contact (vector going from r1 (center of ball 1) to r2 (center of ball 2), same will apply. 
	 *  Tangential components are not affected. 
	 * 
	 */
	public void doCollisionWithBall(CollisionData cd){
		int ballId1 = cd.first - nwalls;
		int ballId2 = cd.second - nwalls;


		if (true) System.out.println("Handling Collisions between Ball " + balls[ballId1] + " Ball " + balls[ballId2]);
		Ball ball1 = balls[ballId1];
		Ball ball2 = balls[ballId2];
		ball1.setPosition(cd.targetPos1);
		ball2.setPosition(cd.targetPos2);
		
		Vector v1 = ball1.getSpeed();
		double m1 = ball1.getMass();
		Vector r1 = ball1.getPosition();
		
		Vector v2 = ball2.getSpeed();
		double m2 = ball2.getMass();
		Vector r2 = ball2.getPosition();
		Vector n  = Vector.sub(r1,  r2); // vector in direction of impact.
													 // along the center of both balls
		n.normalize(); 
		Vector nrev = Vector.mult(n,  -1);


		//now find the vector component in direction of n. 
		double nv1 = Vector.dot(nrev, v1);
		Vector v1n = Vector.mult(nrev, nv1);
		Vector v1t = Vector.sub(v1, v1n);

		
		double nv2 = Vector.dot(n, v2);
		Vector v2n = Vector.mult(n, nv2);
		Vector v2t = Vector.sub(v2, v2n);
		
		/* now compute the result velocities due to elastic collision.
		 */
		/* v1_normal_final = (m1-m2)/(m1+m2)* v1n + 2m2 * v2n; */
		Vector v1n_temp1 = Vector.mult(v1n, (m1-m2)/(m1+m2));  
		Vector v1n_temp2 = Vector.mult(v2n, (2*m2)/(m1+m2));
		Vector v1n_final = Vector.add(v1n_temp1, v1n_temp2);
		  
		/* v2_normal_final = (m2-m1)/(m1+m2)* v2n + 2m1 * v1n; */
		Vector v2n_temp1 = Vector.mult(v2n, (m2-m1)/(m1+m2));  
		Vector v2n_temp2 = Vector.mult(v1n, (2*m1)/(m1+m2));
		Vector v2n_final = Vector.add(v2n_temp1, v2n_temp2);
		
		Vector vf1 = Vector.add(v1n_final, v1t);
		Vector vf2 = Vector.add(v2n_final, v2t);
		
		ball1.setSpeed(vf1);
		ball2.setSpeed(vf2);		
	}
	
	// look into the table and figure out which collisions will occur first, may be more than one...
	public void computeCollisionTimes() { 
		int nCollisionObjects = nballs + nwalls;
		int minj;
		double minTime = Integer.MAX_VALUE, time;
		collisionCount = 0;
	   //printTable();

		// ignore the last row..
		for(int i=1;i<nCollisionObjects;i++) { 
			minj = rowMinimum[i];
			time = collisionTable[i][minj].collisionTime;
			if (time < minTime) { 
			    System.out.println("minTime =" + time + " " + i + ", " + minj);
				minTime = time;
			}
		}
		// Now go back and look for any entries which have same collision time as the min.
		// This is to handle situation where multiple pairs could have same collision times. 
		for( int i=1;i<nCollisionObjects;i++) { 
			for( int j=i+1;j<=nCollisionObjects;j++) { 
			time = collisionTable[i][j].collisionTime; //changing Time
			CollisionData cd = null;
			if (time <= minTime) { 
				// make sure ball id is the first in collision entry
				if ( i <= nwalls) { 
					cd = heap.removeMin();
					//cd = new CollisionData(j,i, time, 0);
					int bId = j - nwalls;
					cd.targetPos1 = Vector.addMult(balls[bId].pos, balls[bId].velocity, (time - globalTime));
				} else {
					cd = heap.removeMin();
					//cd = new CollisionData(i, j, time, 0);
					int bId1 = i-nwalls, bId2 = j - nwalls;
					cd.targetPos1 = Vector.addMult(balls[bId1].pos, balls[bId1].velocity, (time - globalTime));
					cd.targetPos2 = Vector.addMult(balls[bId2].pos, balls[bId2].velocity, (time - globalTime));
				}
				if(true) System.out.println("Adding cd =" + cd + "@ t=" + globalTime + " minTime ="+minTime + " ccount="+collisionCount);
				System.out.println("Collision added was : " + cd.first + ", " + cd.second);
				//CollisionData cdd = heap.min();
				collisions[collisionCount++] = cd;
				
				
			}
			}
		}	
			
	}
	
	public double roundDown(double val, int p) { 
		int temp = (int)(val * Math.pow(10,  p));
		double ret = temp/Math.pow(10, p);
		return ret;
	}
	
	// we only compute the upper triangle of the 2d array..
	public void computeCollisions(int ballId) { 
		// compute the collisions and only record the earliest one.
        System.out.println("Computing Collisions for BallId:" + ballId);
        System.out.println("balls[].length = " + balls.length);
		heap.printheapArray();
		Ball b = balls[ballId];
		
		// start with the walls. 
		// time of collision for ball with vector eq ro + vt with wall (p-po).n = 0 is
		// t = (ro-po).n/(v.n)
		double minTime = Integer.MAX_VALUE;
		int minObj = 0;
		double cTime;
		double collideTime;
		int bIndex = ballId + nwalls; // index of this ball in the array..
		
		// handle the walls first, we only update the upper triangle in the array as it is symmetric..
		// This means that for all i > bIndex, the update is to entries in row indexed by bIndex.
		// and for i < bIndex, the update is to the column bIndex in all such rows. 
		int minCol = 0;

		for (int i=1;i<=nballs+nwalls;i++) { 
			if (i <= nwalls) { 
				System.out.println("Checking Wall: " + i);
				Wall w = walls[i];
				cTime = w.computeCollision(b);
			} else { 
				if (i == bIndex) continue;    // a ball does not collide with itself
				System.out.println("Checking Ball: " + i);
				Ball b2 = balls[i - nwalls];
				cTime = b2.computeCollision(b);
			}
			if (cTime <= 0) {
				// A negative value of cTime implies the collision will never happen. 
				collideTime = Integer.MAX_VALUE;
			} else {
				collideTime = roundDown(cTime + globalTime, 2);
			}
			if ( i > bIndex) { 
				
				collisionTable[bIndex][i].collisionTime = collideTime;
				System.out.println("collideTime = " + collideTime);
				heap.minHeapify(0);
				
				//should be able to get rid of this
				if (collideTime < minTime){
					System.out.println("CollideTime < minTime");
					minTime = collideTime;
					minObj = i;
				}
			}


			// Now update the column entries for which i < bIndex..
			if (i<bIndex) { 
				double prevTime = collisionTable[i][bIndex].collisionTime;
				//shouldn't need to call minHeapify as we aren't editing the heap
				heap.minHeapify(0);
				
				minCol = rowMinimum[i];
				double rowminTime = collisionTable[i][minCol].collisionTime;
				collisionTable[i][bIndex].collisionTime = collideTime;
				
				heap.minHeapify(0);
				
				
				if (rowminTime > collideTime) { 
					rowMinimum[i] = bIndex;
				}
				if (minCol == bIndex && prevTime < collideTime) { 
					// min is invalid, have to recompute the min for the ithm row
					double iTimeMin = Integer.MAX_VALUE;
					int iminCol = 0;
					double itime;
					int j=0;
					for (j=1;j<=nwalls+nballs;j++) { 
						if (j==i) 
							continue;
						itime = collisionTable[i][j].collisionTime;
						heap.minHeapify(0);
						if (itime < iTimeMin) { 
							iTimeMin = itime;
							iminCol = j;
						}
					}
					rowMinimum[i] = iminCol;
				}
			}
		}
		
		rowMinimum[bIndex] = minObj;
	}
	
	public void printTable() { 
		int nObj = nwalls + nballs;
		String str;
		for(int i=1;i<=nObj;i++) { 
			System.out.print("c[" +i + "]=");
			for(int j=i+1;j<=nObj;j++) { 
				if ( j== rowMinimum[i]) str ="R=";
				else str = " ";
				
				if (collisionTable[i][j].collisionTime == Integer.MAX_VALUE) {
					continue;
				} else {
					System.out.print( str + "["+j+"]"+collisionTable[i][j] + ", ");
				}
			}
			System.out.println(" | rowMin = " + rowMinimum[i]);			
		}
	}
	public void handleCollision(CollisionData cd) {
		System.out.println("Handling Collision, First: " + cd.first + " Second: " + cd.second + " time: " + cd.collisionTime);
		// now compute the new trajectory for both the balls.
		if ((cd.second> nwalls) && (cd.first > nwalls)) {
			
			System.out.println(" Doing Collision With Ball!!!");
			doCollisionWithBall(cd);
		} else { 
			System.out.println(" Doing Collision With Wall!!!");
			doCollisionWithWall(cd);
		}
		// compute the next collision for the two balls involved

	}
	
	public void paintComponent(Graphics g) { 
		g.drawString(" time = " + globalTime, X_RANGE-150, 10);
		g.setColor(Color.BLACK);
		globalTime+=increment;

	    for(int i=1;i<=nballs;i++) { 
			  balls[i].paint(g);
		}

		DetectCollisions();
		
		g.drawLine(10, 10, X_RANGE-10, 10);
		g.drawLine(X_RANGE-10, 10, X_RANGE-10, Y_RANGE-10);
		g.drawLine(X_RANGE-10, Y_RANGE-10, 10, Y_RANGE-10);
		g.drawLine( 10, Y_RANGE-10, 10, 10);

	}
	
	public void addButtons(JToolBar tb) { 
		JButton b = new JButton("step");
        b.setActionCommand("step");
        b.setToolTipText("move anim by one");
		b.addActionListener(this);
		tb.add(b);
		
		JButton bs = new JButton("stop");
        bs.setActionCommand("stop");
        bs.setToolTipText("stop anim");
		bs.addActionListener(this);
		tb.add(bs);	
		
		JButton ba = new JButton("animate");
        ba.setActionCommand("animate");
        ba.setToolTipText("move anim");
		ba.addActionListener(this);
		tb.add(ba);
		
		JButton bw = new JButton("slower");
        bw.setActionCommand("slower");
        bw.setToolTipText("slow move anim");
		bw.addActionListener(this);
		tb.add(bw);
		
		JButton bf = new JButton("faster");
        bf.setActionCommand("faster");
        bf.setToolTipText("fast move anim");
		bf.addActionListener(this);
		tb.add(bf);
	}
	public void addMenu(JMenuBar mb) { 
		JMenu menu;
		JMenuItem menuItem;
		
		menu = new JMenu("options");
		menuItem = new JCheckBoxMenuItem("set arrows");
		
		menuItem.setActionCommand("arrows");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		menuItem = new JCheckBoxMenuItem("show Numbers");
		menuItem.setActionCommand("numbers");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		menuItem = new JCheckBoxMenuItem("show Verbose info");
		menuItem.setActionCommand("info");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		
		menuItem = new JMenuItem("store config");
		menuItem.setActionCommand("config");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		mb.add(menu);
	}
	/* Format of the data file is as follows
	 * n   # no of balls
	 * radius, pos1_x, pos1_y (as ints), vec1_x, vec1_y (as double)    # position coords for ball1 and velocity vector for ball 2..
	 * .....                             # n such lines
	 */
	public static CollidingBalls readData(String dataFile) throws Exception { 
		Scanner sc = new Scanner(new File(dataFile));
		int nBalls = sc.nextInt();
		CollidingBalls cb = new CollidingBalls(nBalls);
		double radius;
		double px, py;
		double vx, vy;
		for(int i=1;i<=nBalls;i++) { 
			radius = sc.nextDouble();
			px = sc.nextDouble();
			py = sc.nextDouble();
			vx = sc.nextDouble();
			vy = sc.nextDouble();
			Vector pos = new Vector(px, py);
			Vector sp  = new Vector(vx, vy);
			cb.modifyBall(i, pos, sp, radius);
		}
		return cb;
	}

	/* 
	 * Usage : 
	 * java CollidingBalls no_of_balls
	 *   OR
	 * java CollidingBalls <input file>
	 *   
	 */
	public static void main(String[] args) throws Exception {
		
		CollidingBalls cb = null;
		JFrame f = new JFrame("Colliding Balls");
		JToolBar toolBar = new JToolBar("Still draggable");
		JMenuBar menuBar = new JMenuBar();



		int nballs = 2;
		if ( args.length != 0) { 
			try { 
				nballs = Integer.parseInt(args[0]);
				cb = new CollidingBalls(nballs);
			} catch (NumberFormatException e) { 
				// the arg is filename then and not a number
				cb = readData(args[0]);
			}
		} else { 
			 cb = new CollidingBalls(nballs);
		} 

		cb.addButtons(toolBar);
		cb.addMenu(menuBar);

		cb.writeConfig("data.config");
		for(int i=1;i<=cb.nballs;i++) {
			cb.computeCollisions(i);
		}
		cb.computeCollisionTimes();

		// Java Graphics related code..
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		f.add(cb, BorderLayout.CENTER);
		f.add(toolBar, BorderLayout.SOUTH);
		f.setJMenuBar(menuBar);
		f.setSize(X_RANGE+30, Y_RANGE+70);
		f.setVisible(true);
		while (true) { 
			if( cb.stepP == 1) { 
				cb.stepP = 0;
				f.repaint();
			}
			if ( cb.animateP == 1) { 
				f.repaint();
			}
			Thread.sleep(cb.animDelay);	
		}
	}
	
	// @Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String cmd = e.getActionCommand();

		if ( cmd.equals("step")) { 
			stepP = 1;
		}		
		if ( cmd.equals("numbers")) { 
			JCheckBoxMenuItem jm = (JCheckBoxMenuItem)e.getSource();
			if ( jm.isSelected() == true) { 
				Ball.showInfo(true);
			}else {
				Ball.showInfo(false);
			}
		}
		if ( cmd.equals("info")) { 
			JCheckBoxMenuItem jm = (JCheckBoxMenuItem)e.getSource();
			if ( jm.isSelected() == true) { 
				printTable();
				Ball.showVerbose(true);
				verbose = true;
			}else {
				Ball.showVerbose(false);
				verbose = false;
			}
		}
		if ( cmd.equals("arrows")) { 
			JCheckBoxMenuItem jm = (JCheckBoxMenuItem)e.getSource();
			if ( jm.isSelected() == true) { 
				Ball.showLines(true);
			}else {
				Ball.showLines(false);
			}
		}
		if ( cmd.equals("config")) {
			try { 
				writeConfig("data.config");
			} catch (Exception ev) {
				// do nothing;
			}
		}
		
		if ( cmd.equals("animate")) { 
			animateP = 1;
		}
		if ( cmd.equals("slower")) { 
			animDelay += 10;
			System.out.println("delay = " + animDelay);
		}
		if ( cmd.equals("faster")) { 
			animDelay -= 10;
			if (animDelay <= 0) { 
				animDelay = 1;
			}
			System.out.println("delay = " + animDelay);
		}
		if ( cmd.equals("stop")) { 
			animateP = 0;
		}
	}
}

