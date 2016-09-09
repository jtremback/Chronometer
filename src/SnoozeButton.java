package com.metamage.chronometer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class SnoozeButton extends View
{
	private final Ring ring = (Ring) getContext();
	
	public SnoozeButton( Context context, AttributeSet attrs, int defStyle )
	{
		super( context, attrs, defStyle );
	}
	
	public SnoozeButton( Context context, AttributeSet attrs )
	{
		super( context, attrs );
	}
	
	@Override
	public boolean onTouchEvent( MotionEvent event )
	{
		ring.finish();
		
		return false;  // don't track the touch any further
	}
}

