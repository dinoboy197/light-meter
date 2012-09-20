package com.rax.lightmeter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActivityMain extends Activity implements OnClickListener, OnFocusChangeListener {
	
	private static final String TAG = "RaxLog";
	private static final boolean DEBUG = true;
	
	private static final int DIALOG_QUIT_CONFIRM = 1;
	
	private TextView mTextLux;
	private TextView mTextEv;
	private TextView mTextIso;
	private TextView mTextAperture;
	private TextView mTextShutter;
	
	private LinearLayout mLcdLayout;
	private LinearLayout mBtnLayout;
	
	private Button mBtnMeasure;
	private Button mBtnUp;
	private Button mBtnDown;

	private SensorManager mSensorManager;
	private Sensor mLightSensor;
	private LightMeter mMeter;
	
	private boolean mIsEnableVolumeKey = true;
	private int mEvStep = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "ActivityMain::onCreate");
		super.onCreate(savedInstanceState);
		
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("CONF_ENABLE_KEEP_SCREEN_ON", false)) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		setContentView(R.layout.activity_main);
		
		mTextLux = (TextView) findViewById(R.id.main_lux_value);
		mTextEv = (TextView) findViewById(R.id.main_ev_value);
		mTextIso = (TextView) findViewById(R.id.main_iso_value);
		mTextIso.setOnClickListener(this);
		mTextIso.setOnFocusChangeListener(this);
		mTextAperture = (TextView) findViewById(R.id.main_aperture_value);
		mTextAperture.setOnClickListener(this);
		mTextAperture.setOnFocusChangeListener(this);
		mTextShutter = (TextView) findViewById(R.id.main_shutter_value);
		mTextShutter.setOnClickListener(this);
		mTextShutter.setOnFocusChangeListener(this);
		
		mLcdLayout = (LinearLayout) findViewById(R.id.main_lcd_layout);
		mLcdLayout.setOnClickListener(this);
		
		mBtnLayout = (LinearLayout) findViewById(R.id.main_button_layout);
		mBtnMeasure = (Button) findViewById(R.id.main_button);
		mBtnMeasure.setOnClickListener(this);
		mBtnMeasure.setOnTouchListener(mTouchListener);
		mBtnUp = (Button) findViewById(R.id.main_button_up);
		mBtnUp.setOnClickListener(this);
		mBtnDown = (Button) findViewById(R.id.main_button_down);
		mBtnDown.setOnClickListener(this);
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		mMeter = new LightMeter();
		mMeter.setISO(200);
		
		mTextIso.setText(String.valueOf(mMeter.getISO()));
	}
	
	private String printShutterValue(double shutter) {
		String str;
		if (shutter == 0) {
			str = "N/A";
		} else if (shutter < 0) {
			str = String.format("1/%.0f s", -shutter);
		} else {
			str = String.format("%.0f s", shutter);
		}
		return str;
	}

	@Override
	protected void onPause() {
		if (DEBUG) Log.v(TAG, "ActivityMain::onPause");
		//mSensorManager.unregisterListener(mSensorListener, mLightSensor);
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (DEBUG) Log.v(TAG, "ActivityMain::onResume");
		//mSensorManager.registerListener(mSensorListener, mLightSensor, SensorManager.SENSOR_DELAY_GAME);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mIsEnableVolumeKey = prefs.getBoolean("CONF_ENABLE_VOLUME_KEY", true);
		mEvStep = prefs.getInt("CONF_EV_STEP", 2);
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (DEBUG) Log.v(TAG, "ActivityMain::onOptionsItemSelected itemId:" + item.getItemId());
		switch (item.getItemId()) {
		case R.id.menu_setting:
			startActivity(new Intent(this, ActivitySettings.class));
			break;
		case R.id.menu_about:
			startActivity(new Intent(this, ActivityAbout.class));
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onClick(View v) {
		if (DEBUG) Log.v(TAG, "ActivityMain::onClick id:" + v.getId());
		switch (v.getId()) {
		case R.id.main_button_up:
			break;
		case R.id.main_button_down:
			break;
		case R.id.main_iso_value:
			break;
		case R.id.main_aperture_value:
			break;
		case R.id.main_shutter_value:
			break;
		case R.id.main_lcd_layout:
			clearFocus();
			break;
		}
	}
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (DEBUG) Log.v(TAG, "ActivityMain::onFocusChange id:" + v.getId() + " hasFocus:" + hasFocus);
		if (mTextIso.isFocused() || mTextAperture.isFocused() || mTextShutter.isFocused()) {
			if (mBtnLayout.isShown() == false) {
				mBtnLayout.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
				mBtnLayout.setVisibility(View.VISIBLE);
			}
		} else {
			if (mBtnLayout.isShown()) {
				mBtnLayout.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
				mBtnLayout.setVisibility(View.GONE);
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (DEBUG) Log.v(TAG, "ActivityMain::onKeyDown keyCode:" + keyCode);
		boolean handled = false;
		if (mIsEnableVolumeKey) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				mBtnDown.performClick();
				handled = true;
				break;
			case KeyEvent.KEYCODE_VOLUME_UP:
				mBtnUp.performClick();
				handled = true;
				break;
			}
		}
		return handled || super.onKeyDown(keyCode, event);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (DEBUG) Log.v(TAG, "ActivityMain::onConfigurationChanged");
		
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		if (DEBUG) Log.v(TAG, "ActivityMain::onCreateDialog id:" + id);
		switch (id) {
		case DIALOG_QUIT_CONFIRM:
			return new AlertDialog.Builder(this)
					.setTitle(R.string.dlg_quit_confirm_title)
					.setMessage(R.string.dlg_quit_confirm_message)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									finish();
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
								}
							}).create();
		}
		return null;
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onBackPressed() {
		if (DEBUG) Log.v(TAG, "ActivityMain::onBackPressed");
		showDialog(DIALOG_QUIT_CONFIRM);
	}
	
	private OnTouchListener mTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (DEBUG) Log.v(TAG, "ActivityMain::OnTouchListener::onTouch id:" + v.getId());
			
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mSensorManager.registerListener(mSensorListener, mLightSensor, SensorManager.SENSOR_DELAY_GAME);
				clearFocus();
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mSensorManager.unregisterListener(mSensorListener, mLightSensor);
				break;
			}
			return false;
		}
	};
	
	private SensorEventListener mSensorListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			//if (DEBUG) Log.v(TAG, "ActivityMain::SensorEventListener::onAccuracyChanged");
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			//if (DEBUG) Log.v(TAG, "ActivityMain::SensorEventListener::onSensorChanged");
			float lux = event.values[0];
			double ev = mMeter.setLux(lux);
			
			mTextLux.setText(String.format("%.2f", lux));
			mTextEv.setText(String.format("%.2f", ev));
			
			double shutter = mMeter.getTByFv(2.8d);
			mTextAperture.setText(String.valueOf(2.8d));
			if (shutter == 0) {
				shutter = mMeter.getTByFv(5.6d);
				mTextAperture.setText(String.valueOf(5.6d));
				if (shutter == 0) {
					shutter = mMeter.getTByFv(11d);
					mTextAperture.setText(String.valueOf(11d));
					if (shutter == 0) {
						shutter = mMeter.getTByFv(22d);
						mTextAperture.setText(String.valueOf(22d));
					}
				}
			}
			mTextShutter.setText(printShutterValue(shutter));
		}
	};
	
	private void clearFocus() {
		if (mTextIso.isFocused()) mTextIso.clearFocus();
		if (mTextAperture.isFocused()) mTextAperture.clearFocus();
		if (mTextShutter.isFocused()) mTextShutter.clearFocus();
	}
}
