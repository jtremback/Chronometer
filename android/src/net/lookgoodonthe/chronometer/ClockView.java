package net.lookgoodonthe.chronometer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;


public class ClockView extends View
{
	private Drawable clockFace;
	
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
	}
	
	@Override
	protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec )
	{
		super.onMeasure( widthMeasureSpec, heightMeasureSpec );
		
		final int width  = MeasureSpec.getSize( widthMeasureSpec  );
		final int height = MeasureSpec.getSize( heightMeasureSpec );
		
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
		
		final Rect faceBounds = new Rect( left, top, right, bottom );
		
		clockFace.setBounds( faceBounds );
	}
	
	@Override
	protected void onDraw( Canvas canvas )
	{
		super.onDraw( canvas );
		
		clockFace.draw( canvas );
	}
}

