import java.text.DecimalFormat;

class Vector { 
	double[] v;
	int len;
	Vector(double x, double y) { 
		v= new double[2];
		len = 2;
		v[0] = x;
		v[1] = y;
	}
	Vector(int len) { 
		v = new double[len];
		this.len = len;
		for (int i=0;i<len;i++) {
			v[0] = 1;
		}
	}
	public static double dot(Vector a, Vector b) { 
		double res = 0;
		for(int i=0;i<a.len;i++) { 
			res += a.v[i] * b.v[i];
		}
		return res;
	}
	public static Vector add(Vector a, Vector b) { 
		Vector res = new Vector(a.len);
		for(int i=0;i<a.len;i++) { 
			res.v[i] = a.v[i] + b.v[i];
		}
		return res;
	}
	public static Vector mult(Vector a, double c) { 
		Vector res = new Vector(a.len);
		for(int i=0;i<a.len;i++) { 
			res.v[i] = a.v[i] * c;
		}
		return res;
	}
	public static Vector addMult(Vector a, Vector b, double c) { 
		Vector res = new Vector(a.len);
		for(int i=0;i<a.len;i++) { 
			res.v[i] = a.v[i] + b.v[i] * c;
		}
		return res;
	}
	
	public static Vector sub(Vector a, Vector b) { 
		Vector res = new Vector(a.len);
		for(int i=0;i<a.len;i++) { 
			res.v[i] = a.v[i] - b.v[i];
		}
		return res;
	}
	public void add(Vector a) { 
		for(int i=0;i<len;i++) { 
			v[i] += a.v[i];
		}
	}
	
	public double dot(Vector a) { 
		double res = 0;
		for(int i=0;i<len;i++) { 
			res += v[i] * a.v[i];
		}
		return res;
	}
	public void sub(Vector a) { 
		for(int i=0;i<len;i++) { 
			v[i] -= a.v[i];
		}
	}
	
	// this is scalar multiply
	public void mult(double c) { 
		for(int i=0;i<len;i++) { 
			v[i] *= c;
		}
	}
	public void componentMult(Vector a) { 
		for(int i=0;i<len;i++) { 
			v[i] *= a.v[i];
		}
	}
	
	public void normalize() { 
		double mag = magnitude();
		for(int i=0;i<len;i++) { 
			v[i] /= mag;
		}
	}
	
	public double magnitude() { 
		double res = 0.0;
		for(int i=0;i<len;i++) { 
			res += (v[i]*v[i]);
		}
		return Math.sqrt(res);
	}
	
	public Vector copy() { 
		Vector vec = new Vector(v[0], v[1]);
		return vec;
	}
	
	public double getComponent(int i) { 
		return v[i];
	}
	
	public String toString() {
		DecimalFormat df = new DecimalFormat("0.00");
		String fv1 = df.format(v[0]);
		String fv2 = df.format(v[1]);
		return "( " + fv1 + ", " + fv2 + " )";
	}
}
