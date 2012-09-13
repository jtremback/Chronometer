package com.metamage.chronometer;


public class Orbit
{
	
	private double baseAngle;
	private double lastAngle;
	
	private int circuits = 0;
	
	public Orbit( double initialAngle )
	{
		baseAngle = initialAngle;
		lastAngle = initialAngle;
	}
	
	public double distance()
	{
		return Trig.unsignedAngularDistance( baseAngle, lastAngle ) + circuits * 360;
	}
	
	public double update( double angle )
	{
		final double a = Trig.signedAngularDistance( baseAngle, lastAngle );
		final double b = Trig.signedAngularDistance( baseAngle, angle     );
		
		final double c = Trig.signedAngularDistance( angle, lastAngle );
		
		if ( Math.abs( b ) < 90  &&  (a < 0) != (b < 0) )
		{
			circuits += a < b ? 1 : -1;
		}
		
		final double result = angle - lastAngle;
		
		lastAngle = angle;
		
		return result;
	}
	
}

