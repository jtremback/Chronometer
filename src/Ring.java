package com.metamage.chronometer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.WindowManager;


public final class Ring extends Activity
{
	
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		
		getWindow().addFlags( + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
		                      | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
		                      | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
		                      | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		
		setContentView( R.layout.snooze );
		
		vibrate( 500 );
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		
		overridePendingTransition( 0, 0 );
	}
	
	public void vibrate( long ms )
	{
		Vibrator vibrator = (Vibrator) getSystemService( VIBRATOR_SERVICE );
		
		vibrator.vibrate( ms );
	}
	
}

