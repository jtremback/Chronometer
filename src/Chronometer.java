package com.metamage.chronometer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;


public final class Chronometer extends Activity
{
	
	private boolean updating;
	
	private long alarmTime = 0;
	private long timerTime = 0;
	
	private View clockView;
	
	private Handler updateHandler = new Handler();
	
	private Runnable update = new Runnable()
	{
		public void run()
		{
			if ( !updating )
			{
				return;
			}
			
			clockView.invalidate();
			
			final long ms = System.currentTimeMillis();
			
			final long remainder = ms % 1000;
			
			final long gap = 1000 - remainder;
			
			updateHandler.postDelayed( this, gap );
			
			if ( alarmTime != 0  &&  ms >= alarmTime )
			{
				setAlarmTime( 0 );
			}
			
			if ( timerTime != 0  &&  ms >= timerTime )
			{
				setTimerTime( 0 );
			}
		}
	};

	long getAlarmTime()
	{
		return alarmTime;
	}
	
	long getTimerTime()
	{
		return timerTime;
	}
	
	public void setAlarmTime( long ms )
	{
		alarmTime = ms;
	}
	
	public void setTimerTime( long ms )
	{
		timerTime = ms;
	}
	
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		
		setContentView( R.layout.main );
		
		clockView = findViewById( R.id.clock );
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		updating = true;
		
		updateHandler.post( update );
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		
		updating = false;
	}
	
}

