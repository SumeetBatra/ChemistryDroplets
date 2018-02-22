package com.urop.chemistrydroplets;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
		
		
		
		Thread timerThread = new Thread() {
			public void run() {
				try{
					sleep(2000);
				}catch(InterruptedException e){
					e.printStackTrace();
				}finally{
					Intent intent = new Intent(SplashActivity.this, SelectionActivity.class);
					startActivity(intent);
				}
			}
		};
		timerThread.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}


}
