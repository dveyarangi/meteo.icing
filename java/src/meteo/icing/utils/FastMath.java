package meteo.icing.utils;

public class FastMath
{
	private static final int BIG_ENOUGH_INT = 16 * 1024;
	private static final float BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
	private static final float BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5f;

	public static final double SQRT2 = Math.sqrt(2);
	public static final double SQRT3 = Math.sqrt(3);

	public static final double YOTTA = 1.0e24d;
	public static final double ZETTA = 1.0e21d;
	public static final double EXA = 1.0e18d;
	public static final double PETA = 1.0e15d;
	public static final double TERA = 1.0e12d;
	public static final double GIGA = 1.0e9d;
	public static final double MEGA = 1.0e6d;
	public static final double KILO = 1.0e3d;
	public static final double HECTO = 1.0e2d;
	public static final double DECA = 1.0e1d;
	public static final double DECI = 1.0e-1d;
	public static final double CENTI = 1.0e-2d;
	public static final double MILLI = 1.0e-3d;
	public static final double MICRO = 1.0e-6d;
	public static final double NANO = 1.0e-9d;
	public static final double PICO = 1.0e-12d;
	public static final double FEMTO = 1.0e-15d;
	public static final double ATTO = 1.0e-18d;
	public static final double ZEPTO = 1.0e-21d;
	public static final double YOCTO = 1.0e-24d;

	public static int floor( double x )
	{
		assert ! Double.isNaN( x );
		return (int) ( x + BIG_ENOUGH_FLOOR ) - BIG_ENOUGH_INT;
	}

	public static int round( double x )
	{
		assert ! Double.isNaN( x );
		return (int) ( x + BIG_ENOUGH_ROUND ) - BIG_ENOUGH_INT;
	}

	public static int ceil( double x )
	{
		assert ! Double.isNaN( x );
		return BIG_ENOUGH_INT - (int) ( BIG_ENOUGH_FLOOR - x ); // credit:
																// roquen
	}

	public static int floor( float x )
	{
		assert ! Float.isNaN( x );
		return (int) ( x + BIG_ENOUGH_FLOOR ) - BIG_ENOUGH_INT;
	}

	public static int round( float x )
	{
		assert ! Float.isNaN( x );
		return (int) ( x + BIG_ENOUGH_ROUND ) - BIG_ENOUGH_INT;
	}

	public static int ceil( float x )
	{
		assert ! Float.isNaN( x );
		return BIG_ENOUGH_INT - (int) ( BIG_ENOUGH_FLOOR - x ); // credit:
																// roquen
	}

	public static int toGrid( int val, int cell )
	{
		return val - val % cell;
		// return val/cell * cell; // embrace eternity!
	}

	public static double toGrid( double val, double cell )
	{
		return round(val / cell) * cell;
	}

	public static float toGrid( float val, float cell )
	{
		return round(val / cell) * cell;
	}

	public static int toGrid( double val, int cell )
	{
		return round(val / cell) * cell;
	}

	public static int toGrid( float val, int cell )
	{
		return round(val / cell) * cell;
	}

	public static double powOf2( double d )
	{
		return d * d;
	}

	public static float powOf2( float d )
	{
		return d * d;
	}

	public static int log2( int n )
	{
		if( n <= 0 )
			throw new IllegalArgumentException("Argument cannot be <= 0");
		return 31 - Integer.numberOfLeadingZeros(n);
	}

	public static float ema_smooth( float oldVal, float newVal, float newWeight )
	{
		return oldVal * ( 1 - newWeight ) + newVal * newWeight;
	}

	public static double ema_smooth( double oldVal, double newVal, double newWeight )
	{
		return oldVal * ( 1 - newWeight ) + newVal * newWeight;
	}

}
