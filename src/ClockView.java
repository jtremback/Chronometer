package com.metamage.chronometer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
	
	private Orbit dragOrbit;
	
	private int dragging = NOTHING;
	
	private long timeOfDrag;
	
	private double existingDragDistance;
	
	private final Chronometer chronometer = (Chronometer) getContext();
	
	public ClockView( Context context, AttributeSet attrs, int defStyle )
	{
		super( context, attrs, defStyle );
	}
	
	public ClockView( Context context, AttributeSet attrs )
	{
		super( context, attrs );
	}
	
	{
		final Resources res = getContext().getResources();
		
		clockFace   = res.getDrawable( R.drawable.face   );
		clockCenter = res.getDrawable( R.drawable.center );
		
		hourHand   = res.getDrawable( R.drawable.hour   );
		minuteHand = res.getDrawable( R.drawable.minute );
		secondHand = res.getDrawable( R.drawable.second );
		
		alarmHandle = res.getDrawable( R.drawable.alarm );
		timerHandle = res.getDrawable( R.drawable.timer );
		
		setClickable( true );
	}
	
	public boolean isDragging()
	{
		return dragging != NOTHING;
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
		
		scale *= getResources().getDisplayMetrics().density;
	}
	
	private void drawHand( Canvas canvas, Drawable hand, float degrees )
	{
		canvas.save();
		
		canvas.rotate( degrees, centerX, centerY );
		
		hand.draw( canvas );
		
		canvas.restore();
	}
	
	public void updateAlarmHandle( long ms )
	{
		final Calendar cal = Calendar.getInstance();
		
		cal.setTimeInMillis( ms );
		
		final int hour   = cal.get( Calendar.HOUR   );
		final int minute = cal.get( Calendar.MINUTE );
		final int second = cal.get( Calendar.SECOND );
		
		final float seconds = second;
		final float minutes = minute + seconds / 60;
		final float hours   = hour   + minutes / 60;
		
		alarmAngle = hours * 30;  // 360 / 12
		
	}
	
	public void updateTimerHandle( long ms )
	{
		final Calendar cal = Calendar.getInstance();
		
		cal.setTimeInMillis( ms );
		
		final int hour   = cal.get( Calendar.HOUR   );
		final int minute = cal.get( Calendar.MINUTE );
		final int second = cal.get( Calendar.SECOND );
		
		final float seconds = second;
		final float minutes = minute + seconds / 60;
		
		timerAngle = minutes * 6;  // 360 / 60
		
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
		
		if ( chronometer.getAlarmTime() == 0 )
		{
			alarmAngle = hourAngle;
		}
		
		if ( chronometer.getTimerTime() == 0 )
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
		chronometer.vibrate( 8 );
		
		return true;
	}
	
	private int msPerCircle()
	{
		final int msPerHour = 1000 * 60 * 60;
		
		final int msPerAlarmCircle = msPerHour * 12;
		final int msPerTimerCircle = msPerHour;
		
		return dragging == ALARM_HANDLE ? msPerAlarmCircle : msPerTimerCircle;
	}
	
	private double dragDistance()
	{
		return existingDragDistance + dragOrbit.distance();
	}
	
	private long eventTimeFromDrag()
	{
		final double angleDragged = dragDistance();
		
		final double msDragged = angleDragged / 360 * msPerCircle();
		
		final long eventTime = timeOfDrag + (long) msDragged;
		
		if ( eventTime <= System.currentTimeMillis() )
		{
			return 0;
		}
		
		return eventTime;
	}
	
	private void beginDrag()
	{
		final double initialDragAngle = dragging == ALARM_HANDLE ? alarmAngle : timerAngle;
		
		timeOfDrag = System.currentTimeMillis();
		
		final long eventTime = dragging == ALARM_HANDLE ? chronometer.getAlarmTime()
		                                                : chronometer.getTimerTime();
		
		existingDragDistance = 0;
		
		if ( eventTime != 0 )
		{
			final double timeOffset = eventTime - timeOfDrag;
			
			existingDragDistance = timeOffset / msPerCircle() * 360;
		}
		
		dragOrbit = new Orbit( initialDragAngle );
	}
	
	private void updateDrag( double angle )
	{
		int sections;
		
		if ( dragging == ALARM_HANDLE )
		{
			// round angle to nearest quarter hour
			
			sections = 12 * 4;  // 7.5 degrees each
		}
		else
		{
			// round angle to nearest half minute
			
			sections = 60 * 2;  // 3 degrees each
		}
		
		final double quantum = 360.0 / sections;
		
		angle += quantum / 2;
		
		angle -= angle % quantum;
		
		final double delta = dragOrbit.update( angle );
		
		final boolean incremented = (int) delta != 0;
		
		final long eventTime = eventTimeFromDrag();
		
		final boolean validDrag = dragDistance() > 0;
		
		if ( dragging == ALARM_HANDLE )
		{
			alarmAngle = validDrag ? (float) angle : hourAngle;
			
			chronometer.setAlarmTime( eventTime );
		}
		else
		{
			timerAngle = validDrag ? (float) angle : minuteAngle;
			
			chronometer.setTimerTime( eventTime );
		}
		
		invalidate();
		
		if ( validDrag  &&  incremented )
		{
			chronometer.vibrate( 2 );
		}
	}
	
	private void endDrag()
	{
		chronometer.commit();
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

