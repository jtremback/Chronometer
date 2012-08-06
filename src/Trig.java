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
	
	static public double unsignedAngularDistance( double a, double b )
	{
		return (b - a + 360) % 360;
	}
	
	static public double signedAngularDistance( double a, double b )
	{
		return (b - a + 180 + 360) % 360 - 180;
	}
	
	static public boolean matchingAngles( double a, double b, double range )
	{
		return Math.abs( signedAngularDistance( a, b ) ) < range;
	}
	
}

