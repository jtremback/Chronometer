package net.lookgoodonthe.chronometer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;


public class ClockView extends View
{
	private Drawable clockFace;
	
	private Drawable hourHand;
	private Drawable minuteHand;
	private Drawable secondHand;
	
	private Drawable alarmHandle;
	private Drawable timerHandle;
	
	private int centerX;
	private int centerY;
	
	private float scale;
	
	private float alarmAngle;
	private float timerAngle;
	
	private Handler updateHandler = new Handler();
	
	private Runnable update = new Runnable()
	{
		public void run()
		{
			ClockView.this.invalidate();
			
			final long ms = System.currentTimeMillis();
			
			final long remainder = ms % 1000;
			
			final long gap = 1000 - remainder;
			
			updateHandler.postDelayed( this, gap );
		}
	};
	
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
		
		clockFace = res.getDrawable( R.drawable.face );
		
		hourHand   = res.getDrawable( R.drawable.hour   );
		minuteHand = res.getDrawable( R.drawable.minute );
		secondHand = res.getDrawable( R.drawable.second );
		
		alarmHandle = res.getDrawable( R.drawable.alarm );
		timerHandle = res.getDrawable( R.drawable.timer );
		
		updateHandler.post( update );
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
		super.onMeasure( widthMeasureSpec, heightMeasureSpec );
		
		final int width  = MeasureSpec.getSize( widthMeasureSpec  );
		final int height = MeasureSpec.getSize( heightMeasureSpec );
		
		centerX = width  / 2;
		centerY = height / 2;
		
		final int min = Math.min( width, height );
		final int max = Math.max( width, height );
		
		final int margin = (max - min) / 2;
		
		int top  = 0;
		int left = 0;

		if ( width < height )
		{
			top += margin;  // portrait mode
		}
		else
		{
			left += margin;  // landscape mode
		}
		
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
		
		final float hourAngle   = hours   * 30;  // 360 / 12
		final float minuteAngle = minutes *  6;  // 360 / 60
		final float secondAngle = seconds *  6;  // 360 / 60
		
		alarmAngle = hourAngle;
		timerAngle = minuteAngle;
		
		drawHand( canvas, hourHand,    hourAngle   );
		drawHand( canvas, alarmHandle, alarmAngle  );
		drawHand( canvas, minuteHand,  minuteAngle );
		drawHand( canvas, timerHandle, timerAngle  );
		drawHand( canvas, secondHand,  secondAngle );
	}
}

