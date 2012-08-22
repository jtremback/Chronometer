package com.metamage.chronometer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;


public final class Chronometer extends Activity
{
	
	private long alarmTime = 0;
	private long timerTime = 0;
	
	private View clockView;
	
	private Handler updateHandler = new Handler();
	
	private Runnable update = new Runnable()
	{
		public void run()
		{
			clockView.invalidate();
			
			final long ms = System.currentTimeMillis();
			
			final long remainder = ms % 1000;
			
			final long gap = 1000 - remainder;
			
			updateHandler.postDelayed( this, gap );
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
		
		updateHandler.post( update );
	}
	
}

