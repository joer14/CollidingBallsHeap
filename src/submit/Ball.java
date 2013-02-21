 /*
 * Modified by Joe Rowley
 * Jrowley@ucsc.edu
 * 
 * */
import java.awt.Graphics;

class Ball { 
	int id;
	double genId; //changes with each successful collision
	Vector pos;
	double radius;
	double mass;  // proportional to the area of the ball
	public static final double BALL_RADIUS = 10;
	Vector velocity;
	CollisionData cd;
	static boolean putLines;
	static boolean putInfo;
	static boolean verbose;
	
	Ball(int id, Vector pos, Vector v) { 
		verbose = false;
		this.id = id;
		genId = 0;
		this.pos = pos;
		this.radius = BALL_RADIUS;
		velocity = v;
		this.mass = radius * radius;
	}
	
	Ball(int id) {
		verbose = false;
		this.id = id;
		genId = 0;
		this.radius = BALL_RADIUS;
		pos = new Vector(radius + Math.random() * CollidingBalls.X_RANGE-2*radius, radius + Math.random() * CollidingBalls.Y_RANGE-2*radius);
		velocity = new Vector(Math.random(), Math.random());
		this.mass = radius * radius;
	}
	
	public Vector positionAt(double t) {
		Vector v = Vector.mult(velocity, t);
		v.add(pos);
		return v;
	}
	
	public static void showInfo(boolean val) { putInfo = val;}
	public static void showLines(boolean val) { putLines = val;}
	public static void showVerbose(boolean val) { verbose = val;}

	public void setRadius(double r) { radius = r; mass = r * r;}

	public double getRadius() { return radius;}
	
	public double getMass() { return mass; } 
	
	public int getID() { return id; }
	
	public void setSpeed(Vector vel) { 
		velocity = vel;
	}
	
	public Vector getSpeed() { 
		return velocity;
	}
	
	public void setPosition(Vector p) { 
		pos.v[0] = p.v[0];
		pos.v[1] = p.v[1];
	}
	public Vector getPosition() { 
		return pos;
	}
	
	public String toString() { 
		if ( verbose) { 
			return " " + id + "(pos=" + pos + "):" + velocity;
		} else { 
			return " " + id;
		}
	}
	
	public void paint(Graphics g) { 
		double pos0_x =  pos.getComponent(0);
		double pos0_y =  pos.getComponent(1);
		double pos_x = pos0_x - radius ; 
		double pos_y = pos0_y - radius ; 

		g.drawOval((int)pos_x,  (int)pos_y, (int)(2*radius), (int)(2*radius));
		Vector v = velocity.copy();
		//v.normalize();
		Vector endP = Vector.add(pos, Vector.mult(v, 20));

		if(putLines) g.drawLine((int)pos0_x, (int)pos0_y, (int)endP.getComponent(0), (int)endP.getComponent(1));

		if(putInfo) g.drawString(this.toString(), (int)pos0_x,  (int)pos0_y);
		Vector incr = Vector.mult(velocity, CollidingBalls.increment);
		pos.add(incr);
	}
	
	/* Compute when the balls collide 
	 * Say ball 1 is at r1 (r1 is center of the ball) with velocity v1 and ball 2 is at r2 with velocity v2.
	 * After time t, ball 1 is at r1' and ball 2 is at r2'. 
	 * So ||r1' - r2'|| = R where R is sum of radiuses of the two balls. 
	 * This will result in a quadratic equation  of from ax^2 +bx +c 
	 * where a = ||(v1-v2)||^2
	 * b = (v1 - v2) . (r1-r2)
	 * c = || r1 - r2 ||^2  - 4 R^2
	 */
	double computeCollision(Ball b2) {
		Vector r2 = b2.getPosition();
		Vector v2 = b2.getSpeed();
		Vector r1 = pos;
		Vector v1 = velocity;

		Vector dv = Vector.sub(v1,  v2);
		Vector dr = Vector.sub(r1,  r2);
		Vector drNorm = dr.copy();
		drNorm.normalize();
		double a = dv.magnitude();
		a = a * a;
		double b = 2 * Vector.dot(dv,  dr);
		double c = dr.magnitude();
		double R = radius + b2.getRadius();

		/* hack */
		if ( c < R ) { 
			// simply return -1 as there cant be collision here..
			return -1;
		} 
		
		c = c * c - R*R;
		double discriminant = b *b - 4 * a * c;
		if ( discriminant  < 0 ) { 
			return -1; // no solution
		}
		// else solutions are (-b +- sqrt(discriminant))/(2a).
		double s1 = (-b + Math.sqrt(discriminant)) / (2 * a);
		double s2 = (-b - Math.sqrt(discriminant)) / (2 * a);
		double ret = -1;
    
		// if both are positive choose the one which is smaller..
		if ( s1 > 0 && s2 > 0  ) { 
			ret = (s1 <= s2)?s1:s2;
		}
		else if ( s1 > 0 ) { 
			ret = s1;
		}
		else if ( s2 > 0 ) {
			ret = s2;
		}
		// System.out.println(" Ball " + this + "Ball " + b2 + "Collide at " + ret);
		return ret;
	}
}
