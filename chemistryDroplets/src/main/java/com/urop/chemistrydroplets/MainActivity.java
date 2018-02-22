

package com.urop.chemistrydroplets;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends Activity implements CvCameraViewListener2{
	
	private Mat mRgba;
	private Mat mRgb;
	private Mat grayFrame;
	private Mat grayBinary;
	private Mat processedFrame;
	private Mat yuv;
	private Mat rgbFinal;
	private boolean locked = false;

	private int counter;
	
	private MainView mOpenCvCameraView;

	ImageButton testButton;
	
	//instantiate opencvcameraview and load native libraries
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch(status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				mOpenCvCameraView.setMaxFrameSize(640, 480);
				mOpenCvCameraView.enableView();
				System.loadLibrary("opencvnative");
				break;
			}
			default:
			{
				super.onManagerConnected(status);
			}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mOpenCvCameraView = (MainView) findViewById(R.id.MainActivityCameraView);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		//testButton = (ImageButton)findViewById(R.id.camerabutton);

		counter = 0;
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int idx = 0;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		double x = event.getX();
		double y = event.getY();
		
		switch(event.getAction()) {
		//if user taps the screen, get its x/y position 
			case MotionEvent.ACTION_UP:
			{
				Display display = getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				double displayWidth = size.x;
				double displayHeight = size.y;
				
				double scaledX = (x / displayWidth) * 640;
				double scaledY = (y / displayHeight) * 480;
				//pass the x/y scaled position to native code 
				GetTouchedPoint(scaledX, scaledY);
			}
			case MotionEvent.ACTION_MOVE:
			{
				
			}
		}
		
		return false;
	}
	
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
	}
	
	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.disableView();
		}
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		//initialize frame matrices
		mRgba = new Mat();
		mRgb = new Mat();
		grayFrame = new Mat();
		grayBinary = new Mat();
		processedFrame = new Mat();
		yuv = new Mat();
		rgbFinal = new Mat();

		//mOpenCvCameraView.lockAutoExposure();
		mOpenCvCameraView.setFPS();
		mOpenCvCameraView.setRecordingHint();
//		testButton.setOnClickListener(new View.OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				Bundle extras = getIntent().getExtras();
//				if(extras != null) {
//					String value = extras.getString("isConnected");
//					if(value.equals("true")) {
//						((Bluetooth)getApplicationContext()).sendData("cmd move_steps 0 200 \r\n");
//					}else{
//						Toast msg = Toast.makeText(getBaseContext(), "Please Connect to the JY-MCU module", Toast.LENGTH_SHORT);
//						msg.show();
//					}
//				}
//			}
//		});
	}

	@Override
	public void onCameraViewStopped() {
		mRgba.release();
		mRgb.release();
		grayFrame.release();
		grayBinary.release();
		processedFrame.release();
		yuv.release();
		rgbFinal.release();
	}
	


	

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

		if(!locked) {
			//first time setup that requires the first frame to be processed in order to lock auto exposure and display FPS.
			mOpenCvCameraView.setFPS();
			mOpenCvCameraView.setRecordingHint();
			//mOpenCvCameraView.lockAutoExposure();
			locked = true;
		}


		//convert input frame into format that native code can process. Darken the frame to make the Droplets easier to detect
		mRgba = inputFrame.rgba();
		Imgproc.cvtColor(mRgba, mRgb, Imgproc.COLOR_RGBA2RGB);

		mRgb.convertTo(processedFrame, -1, 1, -150);
		Imgproc.cvtColor(processedFrame, grayFrame, Imgproc.COLOR_RGB2GRAY);
		//isolate the Droplets' illumination value
		Core.inRange(grayFrame, new Scalar(102), new Scalar(106), grayBinary);
		TrackDroplets(processedFrame.nativeObj, grayBinary.nativeObj, mRgb.nativeObj);

		counter++;
		if(counter % 2 == 0) {
			mOpenCvCameraView.lockAutoExposure();
		}

		return mRgb;
	}
	
	public native void TrackDroplets(long matAddrDarkRgba, long matAddrGrayBin, long matAddrRgba);
	public native void GetTouchedPoint(double x, double y);
}
