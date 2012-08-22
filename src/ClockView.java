package com.metamage.chronometer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Calendar;


public class ClockView extends View
{
	final int NOTHING      = 0;
	final int ALARM_HANDLE = 1;
	final int TIMER_HANDLE = 2;
	
	private Drawable clockFace;
	private Drawable clockCenter;
	
	private Drawable hourHand;
	private Drawable minuteHand;
	private Drawable secondHand;
	
	private Drawable alarmHandle;
	private Drawable timerHandle;
	
	private int centerX;
	private int centerY;
	
	private float scale;
	
	private float hourAngle;
	private float minuteAngle;
	
	private float alarmAngle;
	private float timerAngle;
	
	private int dragging = NOTHING;
	
	private long timeOfDrag;
	
	private double baseDragAngle;
	private double lastDragAngle;
	
	private int crossings;
	
	private final Chronometer chronometer = (Chronometer) getContext();
	
	public ClockView( Context context, AttributeSet attrs, int defStyle )
	{
		super( context, attrs, defStyle );
		
		init( context );
	}
	
	public ClockView( Context context, AttributeSet attrs )
	{
		super( context, attrs );
		
		init( context );
	}
	
	private void init( Context context )
	{
		final Resources res = context.getResources();
		
		clockFace   = res.getDrawable( R.drawable.face   );
		clockCenter = res.getDrawable( R.drawable.center );
		
		hourHand   = res.getDrawable( R.drawable.hour   );
		minuteHand = res.getDrawable( R.drawable.minute );
		secondHand = res.getDrawable( R.drawable.second );
		
		alarmHandle = res.getDrawable( R.drawable.alarm );
		timerHandle = res.getDrawable( R.drawable.timer );
		
		setClickable( true );
	}
	
	static void setBoundsOfHand( Drawable hand, float scale, Rect face )
	{
		final int handWidth  = Math.round( hand.getIntrinsicWidth () * scale );
		final int handHeight = Math.round( hand.getIntrinsicHeight() * scale );
		
		final int hMargin = (face.width () - handWidth ) / 2;
		final int vMargin = (face.height() - handHeight) / 2;
		
		final Rect bounds = new Rect( face );
		
		bounds.inset( hMargin, vMargin );
		
		hand.setBounds( bounds );
	}
	
	@Override
	protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec )
	{
		final int width  = MeasureSpec.getSize( widthMeasureSpec  );
		final int height = MeasureSpec.getSize( heightMeasureSpec );
		
		final int min = Math.min( width, height );
		
		centerX = min / 2;
		centerY = min / 2;
		
		setMeasuredDimension( min, min );
		
		int top  = 0;
		int left = 0;

		final int right  = left + min;
		final int bottom = top  + min;
		
		scale = (float) min / clockFace.getIntrinsicHeight();
		
		final Rect faceBounds = new Rect( left, top, right, bottom );
		
		clockFace.setBounds( faceBounds );
		
		setBoundsOfHand( hourHand,   scale, faceBounds );
		setBoundsOfHand( minuteHand, scale, faceBounds );
		setBoundsOfHand( secondHand, scale, faceBounds );
		
		setBoundsOfHand( alarmHandle, scale, faceBounds );
		setBoundsOfHand( timerHandle, scale, faceBounds );
		
		setBoundsOfHand( clockCenter, scale, faceBounds );
	}
	
	private void drawHand( Canvas canvas, Drawable hand, float degrees )
	{
		canvas.save();
		
		canvas.rotate( degrees, centerX, centerY );
		
		hand.draw( canvas );
		
		canvas.restore();
	}
	
	@Override
	protected void onDraw( Canvas canvas )
	{
		super.onDraw( canvas );
		
		clockFace.draw( canvas );
		
		final Calendar cal = Calendar.getInstance();
		
		final int hour   = cal.get( Calendar.HOUR   );
		final int minute = cal.get( Calendar.MINUTE );
		final int second = cal.get( Calendar.SECOND );
		
		final float seconds = second;
		final float minutes = minute + seconds / 60;
		final float hours   = hour   + minutes / 60;
		
		hourAngle   = hours   * 30;  // 360 / 12
		minuteAngle = minutes *  6;  // 360 / 60
		
		final float secondAngle = seconds *  6;  // 360 / 60
		
		if ( dragging != ALARM_HANDLE  &&  chronometer.getAlarmTime() == 0 )
		{
			alarmAngle = hourAngle;
		}
		
		if ( dragging != TIMER_HANDLE  &&  chronometer.getTimerTime() == 0 )
		{
			timerAngle = minuteAngle;
		}
		
		drawHand( canvas, alarmHandle, alarmAngle  );
		drawHand( canvas, timerHandle, timerAngle  );
		drawHand( canvas, hourHand,    hourAngle   );
		drawHand( canvas, minuteHand,  minuteAngle );
		drawHand( canvas, secondHand,  secondAngle );
		
		clockCenter.draw( canvas );
	}
	
	private int hitTest( double r, double angle )
	{
		r /= scale;
		
		if ( r > 150  &&  r < 250  &&  Trig.matchingAngles( angle, timerAngle, 10 ) )
		{
			return TIMER_HANDLE;
		}
		
		if ( r > 90  &&  r < 170  &&  Trig.matchingAngles( angle, alarmAngle, 20 ) )
		{
			return ALARM_HANDLE;
		}
		
		return NOTHING;
	}
	
	private boolean hitFeedback()
	{
		//final String vibrator_service = VIBRATOR_SERVICE;
		final String vibrator_service = "vibrator";

		Vibrator vibrator = (Vibrator) getContext().getSystemService( vibrator_service );
		
		vibrator.vibrate( 8 );
		
		return true;
	}
	
	private int msPerCircle()
	{
		final int msPerHour = 1000 * 60 * 60;
		
		final int msPerAlarmCircle = msPerHour * 12;
		final int msPerTimerCircle = msPerHour;
		
		return dragging == ALARM_HANDLE ? msPerAlarmCircle : msPerTimerCircle;
	}
	
	private void beginDrag()
	{
		final double initialDragAngle = dragging == ALARM_HANDLE ? alarmAngle : timerAngle;
		
		baseDragAngle = initialDragAngle;
		
		crossings = 0;
		
		timeOfDrag = System.currentTimeMillis();
		
		final long eventTime = dragging == ALARM_HANDLE ? chronometer.getAlarmTime()
		                                                : chronometer.getTimerTime();
		
		if ( eventTime != 0 )
		{
			final double timeOffset = eventTime - timeOfDrag;
			
			final double angleOffset = timeOffset / msPerCircle() * 360;
			
			crossings = (int) angleOffset / 360;
			
			baseDragAngle = (initialDragAngle - angleOffset + 360) % 360;
		}
		
		lastDragAngle = initialDragAngle;
	}
	
	private void updateDrag( double angle )
	{
		final double a = Trig.signedAngularDistance( baseDragAngle, lastDragAngle );
		final double b = Trig.signedAngularDistance( baseDragAngle, angle         );
		
		final double c = Trig.signedAngularDistance( angle, lastDragAngle );
		
		if ( Math.abs( b ) < 90  &&  (a < 0) != (b < 0) )
		{
			crossings += a < b ? 1 : -1;
		}
		
		final boolean validDrag = crossings >= 0;
		
		if ( dragging == ALARM_HANDLE )
		{
			alarmAngle = validDrag ? (float) angle : hourAngle;
		}
		else
		{
			timerAngle = validDrag ? (float) angle : minuteAngle;
		}
		
		lastDragAngle = angle;
		
		invalidate();
	}
	
	private void endDrag()
	{
		final double angleDragged = Trig.unsignedAngularDistance( baseDragAngle, lastDragAngle ) + crossings * 360;
		
		final double msDragged = angleDragged / 360 * msPerCircle();
		
		long eventTime = timeOfDrag + (long) msDragged;
		
		if ( eventTime <= System.currentTimeMillis() )
		{
			eventTime = 0;
		}
		
		if ( dragging == ALARM_HANDLE )
		{
			chronometer.setAlarmTime( eventTime );
		}
		else
		{
			chronometer.setTimerTime( eventTime );
		}
	}
	
	@Override
	public boolean onTouchEvent( MotionEvent event )
	{
		final int action = event.getActionMasked();
		
		switch ( action )
		{
			case MotionEvent.ACTION_DOWN:
				break;
			
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_UP:
				if ( dragging != NOTHING )
				{
					break;
				}
				
				// fall through
			
			default:
				return super.onTouchEvent( event );
		}
		
		final float x = event.getX() - centerX;
		final float y = event.getY() - centerY;
		
		final double r = Math.sqrt( x * x + y * y );
		
		final double angle = Trig.angleFromXY( x, y );
		
		if ( action == MotionEvent.ACTION_DOWN )
		{
			final int hit = hitTest( r, angle );
			
			if ( hit == NOTHING )
			{
				return false;
			}
			
			dragging = hit;
			
			beginDrag();
			
			return hitFeedback();
		}
		else  // MOVE or UP
		{
			updateDrag( angle );
			
			if ( action == MotionEvent.ACTION_UP )
			{
				endDrag();
				
				dragging = NOTHING;
			}
		}
		
		return true;
	}
}

