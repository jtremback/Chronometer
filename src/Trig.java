package net.lookgoodonthe.chronometer;

public final class Trig
{
	
	static public double angleFromXY( float x, float y )
	{
		if ( x == 0 )
		{
			if ( y < 0 )
			{
				return 0;
			}
			else
			{
				return 180;
			}
		}
		
		double angle = Math.atan( y / x ) * 180 / Math.PI + 90;
		
		if ( x < 0 )
		{
			angle += 180;
		}
		
		return angle;
	}
	
	static public boolean matchingAngles( double a, double b, double range )
	{
		final double delta = Math.abs( a - b );
		
		return delta < range  ||  360 - delta < range;
	}
	
}

