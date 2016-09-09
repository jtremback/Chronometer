package com.metamage.chronometer;

import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.WindowManager;


public final class Ring extends Activity
{
	
	private Ringtone ringtone;
	
	private Ringtone startRinging()
	{
		vibrate( 500 );
		
		final Uri defaultAlarm = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_ALARM );
		
		final Ringtone ringtone = RingtoneManager.getRingtone( this, defaultAlarm );
		
		ringtone.play();
		
		return ringtone;
	}
	
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		
		getWindow().addFlags( + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
		                      | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
		                      | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
		                      | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		
		setContentView( R.layout.snooze );
		
		ringtone = startRinging();
	}
	
	@Override
	protected void onDestroy()
	{
		ringtone.stop();
		
		super.onDestroy();
		
		overridePendingTransition( 0, 0 );
	}
	
	public void vibrate( long ms )
	{
		Vibrator vibrator = (Vibrator) getSystemService( VIBRATOR_SERVICE );
		
		vibrator.vibrate( ms );
	}
	
}

