package benl.student.archeryscorer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import benl.student.archeryscorer.R;
import benl.student.archeryscorer.R.id;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;


public class Calculator extends Activity {
	private static final String message = "Hi, this calculates the amount you need to move " +
			"your sight for field based on the elevation of the target. " +
			"\nAll you need to do is enter the marked/direct distance to the target and " +
			"approximately how high the target is from level, where positive is up." +
			"\nBeware: this is not guaranteed to be be correct! " +
			"\nHave you calibrated this for you bow yet? \nCurrent arrow speed = ";
	private static final String speedUnit = "m/s";
	private static final String resultPositiveMessage = "\n\nMove your sight up: ";
	private static final String resultNegativeMessage = "\n\nMove your sight down: ";
	
	private static final double DEFAULT_NOCK_TO_EYE = 0.15;
	private static final double DEFAULT_EYE_TO_SIGHT = 1;
	private static final double DEFAULT_ARROW_SPEED = 50;
	private static final double g = -9.81; // gravity
	private static final double CONVERGENCE_TEST = 1e-7;
	private static final String TAG = "Calculator";

	
	//Overridden and startup methods---------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_calculator);
		setupActionBar();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String arrowSpeed = sharedPref.getString(SettingsActivity.ARROW_SPEED, DEFAULT_ARROW_SPEED+""); 
		findTextView("textViewDisplay").setText(message + arrowSpeed + speedUnit);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		try {
			OnEditorActionListener editListener = new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_GO) {
						calculate();
						return true;
					}
					return false;
				}
			};
			findEditText("editTextDistanceMetres").setOnEditorActionListener(editListener);
			findEditText("editTextDistanceYards").setOnEditorActionListener(editListener);
			findEditText("editTextElevationMetres").setOnEditorActionListener(editListener);
			findEditText("editTextElevationYards").setOnEditorActionListener(editListener);
			findEditText("editTextElevationDegrees").setOnEditorActionListener(editListener);
			findEditText("editTextElevationRadians").setOnEditorActionListener(editListener);
			
		} catch (Exception e) {
			e.printStackTrace();
			cT("Failed to set editTextListeners");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Creates an option menu from menu button
		getMenuInflater().inflate(R.menu.activity_archery_scorer, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			alertCalibrate();
			return true;

		case android.R.id.home:
			generateThumbnail();
			NavUtils.navigateUpFromSameTask(this);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	
	//Calculations----------------------------------------------------------------------------------
	public void calculate(View view) {
		calculate();
	}

	private void calculate() {
		//Defines initial conditions and all variables.
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String arrowSpeed = sharedPref.getString(SettingsActivity.ARROW_SPEED, DEFAULT_ARROW_SPEED+""); 

		final double mToYards = 0.9144;
		final int mToMm = 1000;

		try {
			final double v = Double.parseDouble(arrowSpeed); // Velocity

			double d = Double.parseDouble(findSelectedEditText(findSelectedRadioButton(
					R.id.radioGroupDistance), "Distance").getText().toString()); // Distance
			double e = Double.parseDouble(findSelectedEditText(findSelectedRadioButton(
					R.id.radioGroupElevation), "Elevation").getText().toString()); //Elevation

			String distanceType = findSelectedRadioButton(R.id.radioGroupDistance).getText().toString();
			String elevationType = findSelectedRadioButton(R.id.radioGroupElevation).getText().toString();

			if (distanceType == "Yards") {
				d *= mToYards;
			}

			if (elevationType.equals("Yards")) {
				e *= mToYards;
			} else if (elevationType.equals("Degrees")) {
				e = Math.toRadians(e);
				e = d * Math.sin(e);
			} else if (elevationType.equals("Radians")) {
				e = d * Math.sin(e);
			}


			double da = Math.sqrt(Math.pow(d, 2) - Math.pow(e, 2)); // Calculates horizontal distance

			double radians = Math.atan((-Math.pow(v, 2) + Math.sqrt(Math.pow(v, 4) - Math.pow(g*d, 2)))/(g*d));
			double radians2 = Math.atan((-Math.pow(v, 2) + Math.sqrt(Math.pow(v, 4) + 2*e*g*Math.pow(v, 2) - Math.pow(g*da, 2)))/(g*da));

			double answer = -Math.tan(radians2-Math.asin(e/d)-radians);
			DecimalFormat df = new DecimalFormat("#.##");

			String result = df.format(answer * mToMm) + "mm";

			if (answer > 0) {
				findTextView("textViewDisplay").setText(message + arrowSpeed + speedUnit + resultPositiveMessage + result);
			} else {
				findTextView("textViewDisplay").setText(message + arrowSpeed + speedUnit + resultNegativeMessage + result);
			}

			
			InputMethodManager inputManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE); 
			inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//			imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

		} catch (Exception e) {
			cT("Error Detected");
		}
	}

	
	//Calibration----------------------------------------------------------------------------------
	private void alertCalibrate() {
		final FrameLayout fl = new FrameLayout(this);
		final EditText input = new EditText(this);
		input.setGravity(Gravity.CENTER);
		input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		input.setHint("mm");
		input.setHintTextColor(getResources().getColor(R.drawable.green));
		fl.addView(input, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

		new AlertDialog.Builder(this)
		.setView(fl)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Calibrate")
		.setMessage("Enter the difference between your 30m and 50m sight mark in mm")
		.setCancelable(false)
		.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				calculateCalibrate(input.getText().toString());
				dialog.dismiss();
			}

		})
		.setNegativeButton("Cancel", null)
		.show().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	private void calculateCalibrate(String input) {
		if (!input.equals("")) {
			try {
				double length = Double.parseDouble(input);
				double speed = span(DEFAULT_ARROW_SPEED, length/1000);

				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Calculator.this);
				DecimalFormat df = new DecimalFormat("#.##");
				String result = df.format(speed);

				sharedPref.edit().putString(SettingsActivity.ARROW_SPEED, result).commit();
				findTextView("textViewDisplay").setText(message + result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//Math for calibration------------------------------------------------------------------------
	private double equation(double v) {
		//Returns the sight mark given a certain arrow speed
		//Now accommadating angle between nock, target and eye
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		double nockToEye = Double.parseDouble(sharedPref.getString(SettingsActivity.NOCK_TO_EYE, DEFAULT_NOCK_TO_EYE+"")); 
		double eyeToSight = Double.parseDouble(sharedPref.getString(SettingsActivity.EYE_TO_SIGHT, DEFAULT_EYE_TO_SIGHT+"")); 
		int d1 = 30; //shorter distance
		int d2 = 50; //longer distance
		return Math.tan(0.5*Math.asin(g*d1/Math.pow(v, 2)) - Math.atan(nockToEye/d1))*eyeToSight 
				- Math.tan(0.5*Math.asin(g*d2/Math.pow(v, 2)) - Math.atan(nockToEye/d2))*eyeToSight;
	}

	private double f(double v, double s) {
		// s = sight mark difference
		return equation(v) - s;
	}

	private double span(double a, double s) {
		//a is arrow speed, s = sight mark difference
		//b is the new arrow speed
		double ay = f(a,s);
		Log.d(TAG, "span: ay = " + ay);
		double by;
		double k;
		if (ay > 0) {
			by = f(a+1,s);
			k = (by-ay);
			Log.d(TAG, "span: ay > 0");
		} else {
			by = f(a-1,s);
			k = (ay-by);
			Log.d(TAG, "span: ay < 0");
		}
		Log.d(TAG, "span: by = " + by);
		Log.d(TAG, "span: k = " + k);
		double b = a - 1.2/k*ay;
		if (b < Math.sqrt(-g*50)+1) {
			b = Math.ceil(Math.sqrt(-g*50)+1);
		}
		Log.d(TAG, "span: b = " + b);
		by = f(b,s);
		Log.d(TAG, "span: by = " + by);
		if (ay*by>0) {
			Log.d(TAG, "span: ay*by > 0");
			if (ay>0) {
				Log.d(TAG, "span: ay > 0");
				return span(a+2,s);
			} else {
				Log.d(TAG, "span: ay < 0");
				return span(a-2,s);
			}
		} else {
			Log.d(TAG, "span: ay*by < 0");
			return iterator(a,b,ay,by, s);
		}
	}

	private double iterator(double a, double b, double ay, double by, double s) {
		double m = ((b-a)*-ay)/(by-ay) + a;
		double my = f(m,s);
		if (ay*my > 0) {
			a = m;
			ay = my;
		} else {
			b = m;
			by = my;
		}
		Log.d(TAG, "iterator: m = " + m);
		Log.d(TAG, "iterator: my = " + my);
		if (Math.abs(my) > CONVERGENCE_TEST) {
			return iterator(a,b,ay,by,s);
		} else {
			return m;
		}
	}

	
	//View finding simplifiers----------------------------------------------------------------------
	private TextView findTextView(String name) {
		// Finds textView address
		try {
			Class<id> aClass = R.id.class;
			int aid = aClass.getField(name).getInt(aClass);
			return (TextView) findViewById(aid);
		} catch(Exception e) {		
			e.printStackTrace();
			return null;
		}
	}

	private EditText findEditText(String name) {
		// Finds editText Address
		try {
			Class<id> aClass = R.id.class;
			int aid = aClass.getField(name).getInt(aClass);
			return (EditText) findViewById(aid);
		}catch(Exception e) {		
			e.printStackTrace();
			return null;
		}
	}

	private RadioButton findSelectedRadioButton(int id) {
		RadioGroup radioGroup = (RadioGroup) findViewById(id);

		// Get selected radio button from radioGroup
		int selectedId = radioGroup.getCheckedRadioButtonId();

		// Find the RadioButton by returned id
		return (RadioButton) findViewById(selectedId);
	}

	private EditText findSelectedEditText(RadioButton radioButton, String elevationDistance) {
		return findEditText("editText" + elevationDistance + radioButton.getText().toString());
	}

	//create Toast and finishing------------------------------------------------------------------
	public void cT(String s) { 
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}
	
	private void generateThumbnail() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean update = sharedPref.getBoolean(SettingsActivity.UPDATE_THUMBNAILS, true);
		if (update) {
			File file = new File(getFilesDir(), TAG+".png");
			
			try {
				OutputStream os = new FileOutputStream(file);
				
				View v = findViewById(android.R.id.content).getRootView();
				v.setDrawingCacheEnabled(true);
				getResizedBitmap(v.getDrawingCache()).compress(CompressFormat.PNG, 100, os);
				v.setDrawingCacheEnabled(false);
				
				os.close();
			} catch (FileNotFoundException e) {
				Log.e(TAG, "save: FileNotFoundException" + e.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Bitmap getResizedBitmap(Bitmap bm) {
	    int width = bm.getWidth();
	    int height = bm.getHeight();

	    return Bitmap.createScaledBitmap(bm, width/2, height/2, false);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			generateThumbnail();
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
