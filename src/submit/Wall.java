class Wall { 
	Vector pos;
	Vector normal;
	int Id;
	Wall(int Id, Vector p0, Vector n0) {
		this.Id = Id;
		pos = p0;
		normal = n0;
	}
	public Vector getPoint() { 
		return pos;
	}
	public Vector getNormal() { 
		return normal;
	}
	public double computeCollision(Ball b) { 
		
		Vector n = getNormal();
		Vector p0 = getPoint();
		
		Vector v = b.getSpeed();
		Vector r0 = b.getPosition();
		double bRadius = b.getRadius();
		// Sanity check, make sure ball is at a margin from the wall. Distance is given by n.(x - x0).
		
		double dist = Vector.sub(r0, p0).dot(n);
		if ( dist < bRadius ) { 
			// adjust the ball back..
			double adjDist = Math.abs(bRadius - dist);
			Vector diff = Vector.mult(n, adjDist+1);
			r0.add(diff);
		}
		
		double vdotn = Vector.dot(v,  n);
		if ( vdotn == 0 ) {
			// the time of collision will be at infinity as the path of ball is parallel to the wall
			return -1;
		}
		
		Vector nMult = Vector.mult(n, bRadius);
		Vector num2 = Vector.add(p0, nMult);
		Vector num = Vector.sub(num2, r0);
		double t = num.dot(n)/vdotn;
		//System.out.println(" Wall " + Id + "Ball " + b.getID() + "Collide at " + t);
		return t;
	}
}

