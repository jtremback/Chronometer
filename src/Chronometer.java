package com.metamage.chronometer;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;


public final class Chronometer extends Activity
{
	
	static final String CHRONOMETER_PREFS = "chronometer";
	
	static final String PREFS_ALARM = "alarm";
	static final String PREFS_TIMER = "timer";
	
	private boolean updating;
	
	private long alarmTime = 0;
	private long timerTime = 0;
	
	private TextView alarmText;
	private TextView timerText;
	
	private ClockView clockView;
	
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
			
			updateTimerText();
			
			final long ms = System.currentTimeMillis();
			
			final long remainder = ms % 1000;
			
			final long gap = 1000 - remainder;
			
			updateHandler.postDelayed( this, gap );
			
			boolean shouldRing = false;
			
			if ( alarmTime != 0  &&  ms >= alarmTime )
			{
				setAlarmTime( 0 );
				
				shouldRing = true;
			}
			
			if ( timerTime != 0  &&  ms >= timerTime )
			{
				setTimerTime( 0 );
				
				shouldRing = true;
			}
			
			if ( shouldRing )
			{
				savePrefs();
				
				vibrate( 500 );
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
		ms -= ms % 1000;
		
		alarmTime = ms;
		
		String text = "";
		
		if ( ms > 0 )
		{
			text = DateFormat.getTimeInstance( DateFormat.SHORT ).format( ms );
			
			if ( text.charAt( 1 ) == ':' )
			{
				text = "\u2007" + text;  // Prepend a figure space
			}
		}
		
		alarmText.setText( text );
	}
	
	public void setTimerTime( long ms )
	{
		ms -= ms % 1000;
		
		timerTime = ms;
		
		updateTimerText();
	}
	
	public void commit()
	{
		savePrefs();
	}
	
	private void updateTimerText()
	{
		String text = "";
		
		if ( timerTime > 0 )
		{
			final long delta = timerTime - System.currentTimeMillis();
			
			final int seconds = (int) (delta / 1000);
			
			if ( seconds >= 60 )
			{
				final int minutes = seconds / 60;
				
				text = Integer.toString( minutes ) + " min";
			}
			else
			{
				text = Integer.toString( seconds ) + " sec";
			}
		}
		
		timerText.setText( text );
	}
	
	static private void commitEdits( SharedPreferences.Editor editor )
	{
		if ( VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD )
		{
			editor.apply();
		}
		else
		{
			editor.commit();
		}
	}
	
	private SharedPreferences getPrefs()
	{
		return getSharedPreferences( CHRONOMETER_PREFS, MODE_WORLD_READABLE );
	}
	
	private void loadPrefs()
	{
		final SharedPreferences prefs = getPrefs();
		
		setAlarmTime( prefs.getLong( PREFS_ALARM, 0 ) );
		setTimerTime( prefs.getLong( PREFS_TIMER, 0 ) );
	}
	
	private void savePrefs()
	{
		final SharedPreferences prefs = getPrefs();
		
		final SharedPreferences.Editor editor = prefs.edit();
		
		editor.putLong( PREFS_ALARM, alarmTime );
		editor.putLong( PREFS_TIMER, timerTime );
		
		commitEdits( editor );
	}
	
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		
		setContentView( R.layout.main );
		
		clockView = (ClockView) findViewById( R.id.clock );
		
		alarmText = (TextView) findViewById( R.id.alarm );
		timerText = (TextView) findViewById( R.id.timer );
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		updating = true;
		
		updateHandler.post( update );
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		loadPrefs();
		
		if ( alarmTime != 0 )
		{
			clockView.updateAlarmHandle( alarmTime );
		}
		
		if ( timerTime != 0 )
		{
			clockView.updateTimerHandle( timerTime );
		}
		
		// Cancel the alarm, since we're already running
		setSystemAlarm( 0 );
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		long systemAlarmTime = alarmTime;
		
		if ( systemAlarmTime == 0  ||  timerTime != 0  &&  timerTime < systemAlarmTime )
		{
			systemAlarmTime = timerTime;
		}
		
		if ( systemAlarmTime != 0 )
		{
			setSystemAlarm( systemAlarmTime );
		}
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		
		updating = false;
	}
	
	public void vibrate( long ms )
	{
		Vibrator vibrator = (Vibrator) getSystemService( VIBRATOR_SERVICE );
		
		vibrator.vibrate( ms );
	}
	
	void setSystemAlarm( long ms )
	{
		AlarmManager alarmManager = (AlarmManager) getSystemService( ALARM_SERVICE );
		
		Intent intent = new Intent( this, Chronometer.class );
		
		PendingIntent pendingIntent = PendingIntent.getActivity( this, 0, intent, 0 );
		
		if ( ms != 0 )
		{
			alarmManager.set( AlarmManager.RTC_WAKEUP, ms, pendingIntent );
		}
		else
		{
			alarmManager.cancel( pendingIntent );
		}
	}
	
}

