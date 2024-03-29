/**
 * 
 */
package utils;

import java.util.Random;

import com.vividsolutions.jts.algorithm.MinimumDiameter;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.buffer.BufferOp;

import processing.core.*;
import wblut.geom.*;
import wblut.math.WB_Epsilon;
import wblut.processing.WB_Render;
import Guo_Cam.*;
import controlP5.ControlP5;

/**
 * @author amo Sep 14, 2018
 * 
 */
public class Tools {

	/**
	 * @param testUtils
	 */
	public WB_Render render;
	public CameraController cam;
	public ControlP5 cp5;
	private PApplet app;

	/**
	 * Construct a useful tool for Architecture Design.
	 * 
	 * @param PApplet
	 *            app: the Parent PApplet
	 * 
	 * @param int
	 *            len: Camera Len;
	 */
	public Tools(PApplet app, int len) {
		render = new WB_Render(app);
		cam = new CameraController(app, len);
		cp5 = new ControlP5(app);
		cp5.setAutoDraw(false);
		this.app = app;
	}

	/**
	 * Change a JTS Polygon into WB_Polygon
	 * 
	 * @param: Polygon
	 *             ply
	 * @return: WB_Polygon
	 * @throws:
	 */
	public static WB_Polygon toWB_Polygon(Polygon ply) {
		return new WB_GeometryFactory().createPolygonFromJTSPolygon2D(ply);
	}

	/**
	 *
	 * @param: WB_Polygon
	 *             ply
	 * @return:Polygon
	 * @throws:
	 */
	public static Polygon toJTSPolygon(WB_Polygon ply) {
		WB_Coord[] polypt = ply.getPoints().toArray();
		Coordinate[] pts = new Coordinate[polypt.length + 1];

		for (int i = 0; i < polypt.length; ++i) {
			pts[i] = new Coordinate(polypt[i].xd(), polypt[i].yd());
		}
		pts[polypt.length] = new Coordinate(polypt[0].xd(), polypt[0].yd());
		return new GeometryFactory().createPolygon(pts);
	}

	/**
	 *
	 * @param:
	 * @return:WB_Polygon
	 * @throws:
	 */
	@SuppressWarnings("deprecation")
	public static WB_Polygon JTSOffset(WB_Polygon ply, double r, double distance) {
		try {
			BufferOp b1 = new BufferOp(toJTSPolygon(ply));
			b1.setEndCapStyle(BufferOp.CAP_ROUND);
			Geometry g1 = b1.getResultGeometry(-r);

			BufferOp b2 = new BufferOp(g1);
			b2.setEndCapStyle(BufferOp.CAP_ROUND);
			Geometry g2 = b2.getResultGeometry(r - distance);
			return toWB_Polygon((Polygon) g2);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 *
	 * @param: WB_Polygon
	 *             ply
	 * @return:double
	 * @throws:
	 */
	public static double calcPlyPerimeter(WB_Polygon ply) {
		double ret = 0;
		int n = ply.getNumberOfPoints();
		for (int i = 0; i < n; ++i) {
			ret += ply.getd(i, (i+1)%n);
		}
		return ret;
	}

	/**
	 * @param: WB_Polygon
	 *             a
	 * @param: WB_Polygon
	 *             b
	 * @param: double
	 *             distance
	 * @return: a is within b or not;
	 */
	public boolean JTSwithin(WB_Polygon a, WB_Polygon b, double distance) {
		Polygon g1 = toJTSPolygon(a);
		Polygon g2 = toJTSPolygon(b);
		return g1.isWithinDistance(g2, distance);
	}

	/**
	 *
	 * @param:
	 * @return:WB_Polygon[]
	 * @throws:
	 */
	public WB_Polygon[] getRectangles(WB_Polygon ply, double distance, int N, int seed) {
		WB_Point[] centers = getRandomPoint(ply, distance, N, seed);

		return null;
	}

	public WB_Point[] optimizedPoint(WB_Polygon ply, double distance, int N, int seed) {
		WB_Point[] initPoint = getRandomPoint(ply, distance, N, seed);
		WB_Point[] resultPoint = new WB_Point[N];

		return resultPoint;

	}

	public WB_Point[] getRandomPoint(WB_Polygon ply, double distance, int N, int seed) {
		WB_Point[] pts = new WB_Point[N];
		Random random = new Random(seed);
		if (Math.abs(ply.getSignedArea()) < 1)
			return null;
		Polygon g1 = toJTSPolygon(JTSOffset(ply, 0, distance));

		for (int i = 0; i < N;) {
			// Point g2 = new GeometryFactory().createPoint(new Coordinate(100, 100));
			Point g2 = new GeometryFactory().createPoint(new Coordinate(
					ply.getAABB().getMinX() + random.nextDouble() * (ply.getAABB().getMaxX() - ply.getAABB().getMinX()),
					ply.getAABB().getMinY()
							+ random.nextDouble() * (ply.getAABB().getMaxY() - ply.getAABB().getMinY())));

			// System.out.println(g2);
			if (g2.within(g1)) {
				pts[i] = new WB_Point(g2.getX(), g2.getY(), 0);
				i++;
			}
		}
		return pts;
	}

	public WB_Polygon getMinimumRectangle(WB_Polygon ply) {
		Polygon ret = (Polygon) (new MinimumDiameter(toJTSPolygon(ply))).getMinimumRectangle();
		return toWB_Polygon(ret);
	}

	public double getRectangularRatio(WB_Polygon ply) {
		WB_Polygon rect = getMinimumRectangle(ply);
		return Math.abs(ply.getSignedArea() / rect.getSignedArea());
	}

	public double getHWRatio(WB_Polygon ply) {
		WB_Polygon rect = getMinimumRectangle(ply);
		return rect.getSegment(0).getLength() / rect.getSegment(1).getLength();
	}

	public double[] getRectangleEdge(WB_Polygon ply) {
		double[] ret = new double[2];
		WB_Polygon rect = getMinimumRectangle(ply);
		ret[0] = Math.min(rect.getSegment(0).getLength(), rect.getSegment(1).getLength());
		ret[1] = Math.max(rect.getSegment(0).getLength(), rect.getSegment(1).getLength());
		return ret;
	}

	/**
	 * Draw a box with two Vectors and a base Point
	 * 
	 * @param: WB_Point
	 *             pt left bottom vertex of Box
	 * @param: WB_Vector
	 *             vec from pt to right up of Box
	 * @param: WB_Vector
	 *             Xaxis axis of Z
	 * @return: void
	 * @throws:
	 */
	public void drawBox(WB_Point pt, WB_Vector vec, WB_Vector Xaxis) {
	}

	public void drawBox(WB_Point pt, WB_Vector vec) {
		WB_Vector Xaxis = new WB_Vector(1, 0, 0);
		drawBox(pt, vec, Xaxis);
	}

	/**
	 * Draw ControlP5 Panel in a 2D canvas;
	 */
	public void drawCP5() {
		this.cam.begin2d();
		this.cp5.draw();
		this.cam.begin3d();
	}

	/**
	 * print string on (x, y) with size
	 * 
	 * @param: str
	 *             strings need to print
	 * @param: font
	 *             size
	 * @param: (x,
	 *             y)
	 * @return: void
	 * @throws:
	 */
	public void printOnScreen(String str, int size, float x, float y) {
		cam.begin2d();
		app.textSize(size);
		app.text(str, x, y);
		cam.begin3d();
	}

	public void printOnScreen3D(String str, int size, double x, double y, double z) {
		Vec_Guo vg = cam.getCoordinateOnScreen(x, y, z);
		printOnScreen(str, size, (float) vg.x, (float) vg.y);
	}

	/**
	 *
	 * @param:
	 * @return:WB_Segment []
	 * @throws:
	 */
	public static WB_Segment[] getSegmentsFromPolygon(WB_Polygon ply) {
		int n = ply.getNumberOfPoints();
		WB_Segment[] segs = new WB_Segment[n];
		for (int i = 0; i < n; ++i) {
			segs[i] = new WB_Segment(ply.getPoint(i), ply.getPoint((i + 1) % n));
		}
		return segs;
	}

	public static WB_Polygon[] extrudePolygon(WB_Polygon ply, WB_Vector vec) {
		WB_Coord[] pts = ply.getPoints().toArray();
		WB_Polygon[] ret = new WB_Polygon[pts.length + 2];
		ret[0] = ply;
		ret[1] = movePolygon(ply, vec);
		WB_Point[] newPts = new WB_Point[4];
		for (int i = 0; i < pts.length; ++i) {
			newPts[0] = new WB_Point(pts[i]);
			newPts[1] = new WB_Point(pts[(i + 1) % pts.length]);
			newPts[2] = new WB_Point(vec.add(pts[(i + 1) % pts.length]));
			newPts[3] = new WB_Point(vec.add(pts[i]));
			ret[i + 2] = new WB_Polygon(newPts);
		}
		return ret;
	}

	public static WB_Polygon movePolygon(WB_Polygon ply, WB_Vector vec) {
		WB_Coord[] pts = ply.getPoints().toArray();
		WB_Point[] newPts = new WB_Point[pts.length];
		for (int i = 0; i < pts.length; ++i) {
			newPts[i] = new WB_Point(vec.add(pts[i]));
		}
		return new WB_Polygon(newPts);
	}

	public static boolean isIntersect(WB_AABB aabb, WB_Segment seg) {
		WB_Coord aabb_max = aabb.getMax();
		WB_Coord aabb_min = aabb.getMin();
		WB_Coord o = seg.getOrigin();
		WB_Coord d = seg.getDirection();

		double tmin = 0;
		double tmax = 1;

		for (int i = 0; i < 3; ++i) {
			if (Math.abs(d.getd(i)) < WB_Epsilon.EPSILON) {
				if (o.getd(i) < aabb_min.getd(i) || o.getd(i) > aabb_max.getd(i)) {
					return false;
				}
			} else {
				double od = 1.0 / d.getd(i);
				double tmp = 0;
				double t1 = (aabb_min.getd(i) - o.getd(i)) * od;
				double t2 = (aabb_max.getd(i) - o.getd(i)) * od;
				if (t1 > t2) {
					tmp = t1;
					t1 = t2;
					t2 = tmp;
				}
				tmin = Math.max(tmin, t1);
				tmax = Math.min(tmax, t2);

				if (tmin > tmax)
					return false;
			}
		}
		return true;
	}
}
