package benl.student.archeryscorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;
import android.widget.Toast;

import benl.student.archeryscorer.layouts.DrawView;

public class TouchScreenInterface extends FragmentActivity {

	/**
	 * Okay, so first thing is to get an image displayed on the screen, this is apparently pretty simple using the WebView object
	 * webview.loadUrl("file://...")
	 * 
	 * After you've done that, you'll need to handle touching the screen
	 * A tap should create a filled circle on screen, as well as a popup showing what the score of it is (not a toast)
	 * Idea: also have a smaller navigation on the bottom left, thus through multitouch, one hand on navigation and one holding 
	 * 	down the screen, gets a popup showing the score and is moveable. Also, this navigation should be able to lock
	 * 	(such as near the centre) such as through a swipe outwards to allow holding moving popup allocation.
	 * 
	 * Holding moving popup allocation: hold the screen down and a popup shows the score
	 * Should happen by default when zoomed fully out.
	 * 
	 * Necessary saving:
	 * Coordinates of each point
	 * Maintain a ScoreCard too?
	 * 
	 * Terminology:
	 * Point: a shot, the filled circle on screen.
	 */

	/**
	 *  Stuff to do:
	 *  Create 3 arrow end layout
	 *  Allow for multi-spot targets (Vegas and indoor targets) (animation translation)
	 *  Save
	 *  Temporary Save
	 *  View
	 *  Settings page to allow the user to change stuff? (color of arrows, how many arrows to display at once (every end or all)
	 *  Changing distance/face in the middle of a shoot? (go button)
	 *  Export to excel
	 *  Facebook integration
	 */

	/**
	 * 3 arrow end layout:
	 * Will need to alter createScoreLine as well as when it is called
	 * Also textViews, dynamicScoreCard, other stuff that is made a row at a time.
	 */

	/**
	 * Save Data:
	 * Create an alert to cover all data that hasn't been collected
	 * Round, Distance, Face Size (Stick inside scrollView)
	 * Save exactly same as main, except with custom distances for listView
	 * Viewer will need all the point locations, and scoreSheet data (also end by end viewing...)
	 */

	private DrawView drawView;
	private Point outSize = new Point();
	private ArrayList<TextView> textViews = new ArrayList<TextView>();
	private ArrayList<DistanceLine> distanceLines = new ArrayList<DistanceLine>();
	private LinearLayout scrollViewLL;
	private DynamicScoreCard dynamicScoreCard;
	private ScrollView scrollView;
	private SectionsPagerAdapter spa;
	private TargetSectionFragment target;
	private ScoreSheetSectionFragment scoreSheet;
	private ViewPager vp;
	
	private static final String TAG = "TouchScreenInterface";
	private static final String delimiter = ";";
	
	private int numArrows = 6;

	
	//Setup--------------------------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			setSizeHighAPI(outSize);
		} else {
			setSizeLowAPI(outSize);
		}
		
		dynamicScoreCard = new DynamicScoreCard(numArrows);

//		setContentView(createLayout());
		setContentView(R.layout.activity_tabbed_target);
		createLayout();
		
		readData();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	private View createLayout() {
		final String buttonName = "All";
		final String newButtonName = "End";
		final String distanceButtonName = "New Dist";
		final String backButtonName = "Undo";
		
		final LinearLayout ll = new LinearLayout(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			enableSplitMotionEvents(ll);
		}
		scrollView = new ScrollView(this);

		
		//Go Button
		final Button button = new Button(this);
		View.OnClickListener go = new OnClickListener() {
			@Override
			public void onClick(View v) {
				go();
				if (button.getText().toString().equals(buttonName)) {
					button.setText(newButtonName);
				} else {
					button.setText(buttonName);
				}
			}
		};
		button.setOnClickListener(go);
		LinearLayout.LayoutParams btParams = new LinearLayout.LayoutParams(0, 
				LinearLayout.LayoutParams.WRAP_CONTENT,1);
		button.setGravity(Gravity.CENTER);
		button.setText(buttonName);
		button.setLayoutParams(btParams);
		
		
		//Button to Change Distance
		final Button distanceButton = new Button(this);
		View.OnClickListener chgDist = new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeDistance();
			}
		};
		distanceButton.setOnClickListener(chgDist);
		distanceButton.setGravity(Gravity.CENTER);
		distanceButton.setText(distanceButtonName);
		distanceButton.setLayoutParams(btParams);

		//Back Button
		final Button backButton = new Button(this);
		View.OnClickListener undo = new OnClickListener() {
			@Override
			public void onClick(View v) {
				back();
			}
		};
		backButton.setOnClickListener(undo);
		backButton.setGravity(Gravity.CENTER);
		backButton.setText(backButtonName);
		backButton.setLayoutParams(btParams);

		
		//Container Layout for the buttons
		final LinearLayout buttonll = new LinearLayout(this);
		buttonll.addView(backButton);
		buttonll.addView(button);
		buttonll.addView(distanceButton);

		
		//Layout for the scoring
		scrollViewLL = new LinearLayout(this);
		scrollViewLL.setOrientation(LinearLayout.VERTICAL);

		DistanceLine roundLine = new DistanceLine(this, true);
		scrollViewLL.addView(roundLine.getLine());
		distanceLines.add(roundLine);

		scrollViewLL.addView(createScoreLine());
		scrollView.addView(scrollViewLL);

		//Full container overview Layout
		ll.setOrientation(LinearLayout.VERTICAL);

		drawView = new DrawView(this);
//		ll.addView(drawView, new LinearLayout.LayoutParams(outSize.x, outSize.x));
//		ll.addView(scrollView, new LinearLayout.LayoutParams(outSize.x, 0, 1));
//		ll.addView(buttonll);


//		LinearLayout tabll = (LinearLayout) findViewById(R.id.tabll);
		vp = (ViewPager) findViewById(R.id.viewpager);
//		vp = new NonSwipeableViewPager(this);
		target = new TargetSectionFragment();
		target.ll = new LinearLayout(this);
		target.ll.setOrientation(LinearLayout.VERTICAL);
		target.ll.addView(drawView, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				drawView.screenSize.x));
		scoreSheet = new ScoreSheetSectionFragment();
		scoreSheet.sv = scrollView; 
		scoreSheet.ll = new LinearLayout(this);
		scoreSheet.ll.setOrientation(LinearLayout.VERTICAL);
		scoreSheet.ll.addView(scoreSheet.sv, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
		cT("difference: " + (drawView.screenSize.x + 200 - drawView.screenSize.y));
		if (drawView.screenSize.x + 200 > drawView.screenSize.y) {
			scoreSheet.ll.addView(buttonll);
		} else {
			target.ll.addView(new View(this), new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,0,1));
			target.ll.addView(buttonll);
		}
		
		spa = new SectionsPagerAdapter(getSupportFragmentManager());
		vp.setAdapter(spa);
//		tabll.addView(vp, new LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
		initialiseTabHost();
		
		
//		TabHost th = new TabHost(this);
//		LinearLayout tabll = new LinearLayout(this);
//		TabWidget tw = new TabWidget(this);
//		ViewPager vp = new ViewPager(this);
//		target = new TargetSectionFragment();
//		target.ll = new LinearLayout(this);
//		target.ll.addView(drawView);
//		scoreSheet = new ScoreSheetSectionFragment();
//		scoreSheet.sv = scrollView; 
//		
//		spa = new SectionsPagerAdapter(getSupportFragmentManager());
//		vp.setAdapter(spa);
//		tabll.setOrientation(LinearLayout.VERTICAL);
//		tabll.addView(tw);
//		tabll.addView(vp, new LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.MATCH_PARENT,0,1));
//		th.addView(tabll);
//		ll.addView(th);
//		ll.addView(buttonll);

		drawView.requestFocus();
		
		return ll;
	}
	
	public class NonSwipeableViewPager extends ViewPager {
	    public NonSwipeableViewPager(Context context) {
	        super(context);
	    }

	    public NonSwipeableViewPager(Context context, AttributeSet attrs) {
	        super(context, attrs);
	    }

	    @Override
	    public boolean onInterceptTouchEvent(MotionEvent arg0) {
	        // Never allow swiping to switch between pages
	        return false;
	    }

	    @Override
	    public boolean onTouchEvent(MotionEvent event) {
	        // Never allow swiping to switch between pages
	        return false;
	    }
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void enableSplitMotionEvents(LinearLayout ll) {
		ll.setMotionEventSplittingEnabled(true);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void setSizeHighAPI(Point outSize) {
		getWindowManager().getDefaultDisplay().getSize(outSize);
	}

	@SuppressWarnings("deprecation")
	private void setSizeLowAPI(Point outSize) {
		outSize.y = getWindowManager().getDefaultDisplay().getHeight();
		outSize.x = getWindowManager().getDefaultDisplay().getWidth();
	}


	public LinearLayout createScoreLine() {
		final LinearLayout scoreLL = new LinearLayout(this);

		for (int i = 0; i < numArrows + 2; i++) {
			final TextView textView = new TextView(this);
			textView.setTextColor(0xFF000000);
			textView.setGravity(Gravity.CENTER);

			LinearLayout.LayoutParams tvParams;
			if (i == numArrows) {
				tvParams = new LinearLayout.LayoutParams(40, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			} else if (i == numArrows + 1) {
				tvParams = new LinearLayout.LayoutParams(57, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			} else {
				tvParams = new LinearLayout.LayoutParams(33, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			}

			if (textViews.size() == numArrows + 1) {
				textView.setBackgroundColor(0x00000000);
				tvParams.setMargins(2, 2, 0, 0);
			} else if (i == numArrows) {
				textView.setBackgroundColor(0xFFBBBBBB);
				tvParams.setMargins(3, 2, 0, 0);
			} else if (i == numArrows + 1) {
				textView.setBackgroundColor(0xFFCCCCCC);
				tvParams.setMargins(2, 2, 0, 0);
			} else {
				textView.setBackgroundColor(0xFFAAAAAA);
				tvParams.setMargins(1, 2, 0, 0);
			}

			textView.setTextSize(getResources().getDimension(R.dimen.text_size));



			textView.setLayoutParams(tvParams);

			scoreLL.addView(textView);
			textViews.add(textView);
		}
		return scoreLL;
	}
	
	//------------------------------Create tabbed Layout
	private void initialiseTabHost() {
		final TabHost mTabHost = (TabHost)findViewById(R.id.tabhost);
        mTabHost.setup();

        Log.d(TAG, "I'm not cool");
        
        mTabHost.addTab(mTabHost.newTabSpec("Tag 1")
        		.setIndicator("Target").setContent(new MyTabFactory(this)));
        mTabHost.addTab(mTabHost.newTabSpec("Tag 2")
        		.setIndicator("Scores").setContent(new MyTabFactory(this)));
        Log.d(TAG, "I'm cool");
        OnTabChangeListener tabListener = new OnTabChangeListener() {
        	@Override
        	public void onTabChanged(String tabId) {
        		int pos = mTabHost.getCurrentTab();
        		vp.setCurrentItem(pos);
        	}
        };
        mTabHost.setOnTabChangedListener(tabListener);
//        TabInfo tabInfo = null;
//        this.AddTab(this, 
//        		mTabHost, 
//        		mTabHost.newTabSpec("Tab1").setIndicator("Tab 1"), 
//        		( tabInfo = new TabInfo("Tab1", Tab1Fragment.class, args)));
//        this.mapTabInfo.put(tabInfo.tag, tabInfo);
//        TabsViewPagerFragmentActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab2").setIndicator("Tab 2"), ( tabInfo = new TabInfo("Tab2", Tab2Fragment.class, args)));
//        this.mapTabInfo.put(tabInfo.tag, tabInfo);
//        TabsViewPagerFragmentActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab3").setIndicator("Tab 3"), ( tabInfo = new TabInfo("Tab3", Tab3Fragment.class, args)));
//        this.mapTabInfo.put(tabInfo.tag, tabInfo);
//        // Default to first tab
//        //this.onTabChanged("Tab1");
//        //
//        mTabHost.setOnTabChangedListener(this);
        Log.d(TAG, "I'm pretty cool");
	}
	public class MyTabFactory implements TabContentFactory {

	    private final Context mContext;

	    public MyTabFactory(Context context) {
	        mContext = context;
	    }

	    public View createTabContent(String tag) {
	        View v = new View(mContext);
	        v.setMinimumWidth(0);
	        v.setMinimumHeight(0);
	        return v;
	    }
	}
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		@Override
		public Fragment getItem(int position) {
			if (position == 0) {
				return target;
			} else {
				return scoreSheet;
			}
		}
		@Override
		public int getCount() {
			return 2;
		}
		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(Locale.getDefault());
			case 1:
				return getString(R.string.title_section2).toUpperCase(Locale.getDefault());
			}
			return null;
		}
	}
	
	public static class TargetSectionFragment extends Fragment {
		LinearLayout ll;

		public TargetSectionFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			return ll;
		}
	}
	
	public static class ScoreSheetSectionFragment extends Fragment {
		LinearLayout ll;
		ScrollView sv;

		public ScoreSheetSectionFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			return ll;
		}
	}
	
	
	//Menu Options------------------------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_touch_screen_interface, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection

		switch (item.getItemId()) {
		case R.id.menu_change_face:
			changeFace();
			return true;

		case R.id.menu_change_spot_size:
			alertSpotSize();
			return true;

		case R.id.menu_change_end_size:
			alertEndSize();
			return true;

		case R.id.menu_save:
			alertSave();
			return true;

  		case android.R.id.home:
  			alertLeave();
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void go() {
		swap();
	}

	private void back() {
		try {
			if (dynamicScoreCard.getPointer() > 0) {
				if (dynamicScoreCard.getPointer() % (numArrows+2) == 0) {
					if (dynamicScoreCard.isLastDistanceChange()) {
						scrollViewLL.removeViewAt(dynamicScoreCard.getPointer()/(numArrows+2)+dynamicScoreCard.getNumDistanceChanges());
						dynamicScoreCard.removeLastDistanceChange();
						distanceLines.remove(distanceLines.size()-1);
						return;
					}
					scrollViewLL.removeViewAt(dynamicScoreCard.getPointer()/(numArrows+2)+1 + dynamicScoreCard.getNumDistanceChanges());
					dynamicScoreCard.removeLastRow();
					removeRowTextViews();
				}
				dynamicScoreCard.undo(textViews);
				drawView.removeLastPoint();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "back(): " + e.getMessage());
			Log.e(TAG, "removalAt: " + (dynamicScoreCard.getPointer()/(numArrows+2)+1 + dynamicScoreCard.getNumDistanceChanges()));
		}
	}

	private void swap() {
		drawView.swap();
	}
	
//	private void animate() {
//		drawView.drawAnimation();
//	}
	
	private void changeDistance() {
		if (!dynamicScoreCard.changeDistance()) {
			cT("Must be at the end of a scoring end to change distances");
		} else {
			DistanceLine distanceLine = new DistanceLine(this,false);
			scrollViewLL.addView(distanceLine.getLine(),dynamicScoreCard.getPointer()/(numArrows+2)+dynamicScoreCard.getNumDistanceChanges());
			distanceLines.add(distanceLine);
		}
	}

	private void changeFace() {
		while (dynamicScoreCard.getPointer() > 0) {
			back();
		}
		drawView.changeFace(readSpotSize());
	}

	
	private void removeRowTextViews() {
		for (int i = 0; i < (numArrows+2); i++) {
			textViews.remove(textViews.size()-1);
		}
	}

	//Alerts ----------------------------------------------------------------------------------
	private void alertSpotSize() {
		//Alert popup to select spot size
		new AlertDialog.Builder(this)
		.setView(getLayoutInflater().inflate(R.layout.alert_radio_spot,null))
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Spot Size")
		.setMessage("Select outer ring")
		.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					saveSpotSize(alertFindSelectedRadioButton(R.id.radioGroup, dialog).getText()+"");
				} catch (Exception e) {
					Log.e(TAG, "alertSpotSize, onClick");
				}
			}

		})
		.setNegativeButton("Cancel", null)
		.show();
	}

	private void alertEndSize() {
		//Alert popup to select spot size
		new AlertDialog.Builder(this)
		.setView(getLayoutInflater().inflate(R.layout.alert_radio_end,null))
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Number of Arrows Per End")
		.setMessage("Select the Number of Arrows Per End")
		.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					numArrows = Integer.parseInt(alertFindSelectedRadioButton(R.id.radioGroup, dialog).getText()+ "");

					SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(TouchScreenInterface.this);
					boolean changeEndSizeConfirm = sharedPref.getBoolean(SettingsActivity.CHANGE_END_SIZE_CONFIRM, true);
					if (changeEndSizeConfirm) {
						alertConfirmEndSize(numArrows);
					} else {
						changeEndSize(numArrows);
					}
				} catch (Exception e) {
					Log.e(TAG, "alertEndSize, onClick");
				}
			}

		})
		.setNegativeButton("Cancel", null)
		.show();
	}
	
	private void alertConfirmEndSize(final int numArrows) {
		final CheckBox checkBox = new CheckBox(this);
		checkBox.setText("Don't show again");
		checkBox.setChecked(false);
		
		//Alert popup to select spot size
		new AlertDialog.Builder(this)
		.setView(checkBox)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Confirm")
		.setMessage("Changing the end size will delete all the data on the scoresheet. Are you sure you want to continue?")
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(TouchScreenInterface.this);
				if (checkBox.isChecked() == true) {
					sharedPref.edit().putBoolean(SettingsActivity.CHANGE_END_SIZE_CONFIRM, false).commit();
				}
				changeEndSize(numArrows);
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(TouchScreenInterface.this);
				if (checkBox.isChecked() == true) {
					sharedPref.edit().putBoolean(SettingsActivity.CHANGE_END_SIZE_CONFIRM, false).commit();
				}
			}
		})
		.show();
	}

	private void changeEndSize(int numArrows) {
			dynamicScoreCard.setNumArrows(numArrows);

			textViews = new ArrayList<TextView>();
			dynamicScoreCard = new DynamicScoreCard(numArrows);
			setContentView(createLayout());
	}
	

	private void alertSaveName() {
		int dataCheck = distanceLines.get(0).roundEditText.getText().length();
		for (int i = 0; i < distanceLines.size(); i++) {
			dataCheck *= distanceLines.get(i).distanceEditText.getText().length();
			dataCheck *= distanceLines.get(i).faceEditText.getText().length();
		}

		if (dataCheck == 0) {
			cT("Insufficient data");
		} else {
			final FrameLayout fl = new FrameLayout(this);
			final EditText input = new EditText(this);
			input.setGravity(Gravity.CENTER);
			input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
			input.setHint("Name");
			input.setHintTextColor(getResources().getColor(R.drawable.green));
			fl.addView(input, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

			new AlertDialog.Builder(this)
			.setView(fl)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle("Save Score")
			.setMessage("Enter your Name: ")
			.setCancelable(false)
			.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					//Create New Page
					if (!input.getText().toString().equals("")) {
						save(input.getText().toString());
					} else {
						alertNotify("Save Failed", "No name was entered");
					}
					dialog.dismiss();
				}

			})
			.setNegativeButton("Cancel", null)
			.show().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}
	}

	private void alertSave() {
		//Alert popup when leaving
		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Save")
		.setMessage("Do you want to save the session and quit, or have you finished the shoot and want to save the Score Card?")
		.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int dataCheck = distanceLines.get(0).roundEditText.getText().length();
				for (int i = 0; i < distanceLines.size(); i++) {
					dataCheck *= distanceLines.get(i).distanceEditText.getText().length();
					dataCheck *= distanceLines.get(i).faceEditText.getText().length();
				}

				if (dataCheck == 0) {
					alertNotify("Save Failed", "Please make sure each of the top fields for " +
							"the round, distance and face size of the target are filled");
				} else {
					alertSaveName();
				}
			}
		})
		.setNeutralButton("Save & Quit", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				scoresSave(null);
				NavUtils.navigateUpFromSameTask(TouchScreenInterface.this);
			}
		})
		.setNegativeButton("Cancel", null)
		.show();
	}
	
	private void alertNotify(String title, String message) {
  		//Alert popup when leaving
  		new AlertDialog.Builder(this)
  		.setIcon(android.R.drawable.ic_dialog_alert)
  		.setTitle(title)
  		.setMessage(message)
  		.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
  			@Override
  			public void onClick(DialogInterface dialog, int which) {
  				dialog.dismiss();
  			}
  		})
  		.show();
  	}
  	
	
	private void alertLeave() {
		generateThumbnail();
		if (!(dynamicScoreCard.getPointer() == 0)) {
			//Alert popup when leaving
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.quit)
			.setMessage(R.string.really_quit)
			.setPositiveButton("Quit", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					File file = new File(getFilesDir(), "target_temp_file");
					if (file.exists() && file.delete()) {
						Log.i(TAG, "alertLeave: File deleted");
					} else {
						Log.i(TAG, "alertLeave: File not found");
					}
					NavUtils.navigateUpFromSameTask(TouchScreenInterface.this);
				}
			})
			.setNeutralButton("Save & Quit", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					scoresSave(null);
					NavUtils.navigateUpFromSameTask(TouchScreenInterface.this);
				}
			})
			.setNegativeButton("Cancel", null)
			.show();
		} else {
			File file = new File(getFilesDir(), "target_temp_file");
			if (file.exists() && file.delete()) {
				Log.i(TAG, "alertLeave: File deleted");
			} else {
				Log.i(TAG, "alertLeave: File not found");
			}
			NavUtils.navigateUpFromSameTask(TouchScreenInterface.this);
		}
	}

	
	//Saves the size of spot target
	private void saveSpotSize(String spotOuterRing) {
		String fileName = "spot_size_file";

		FileOutputStream fos;
		try {
			fos = openFileOutput(fileName, Context.MODE_PRIVATE);
			fos.write((spotOuterRing + delimiter).getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "saveSpotSize: FileNotFoundException" + e.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Finds the spot size of the target
	private int readSpotSize() {
		String fileName = "spot_size_file";

		byte[] buffer = new byte[1024];
		StringBuffer listNumberFileContent = new StringBuffer("");

		FileInputStream fis;
		try {
			fis = openFileInput(fileName);
			while ((fis.read(buffer)) != -1) {
				listNumberFileContent.append(new String(buffer));
			}
			fis.close();

			return Integer.parseInt(listNumberFileContent.substring(0,listNumberFileContent.lastIndexOf(delimiter)));
		} catch (FileNotFoundException e) {
			Log.e(TAG, "readSpotSize: FileNotFoundException" + e.toString());
			return 5;
		} catch (Exception e) {
			e.printStackTrace();
			return 5;
		}
	}


	private RadioButton alertFindSelectedRadioButton(int id, DialogInterface dialog) {
		AlertDialog alert = (AlertDialog) dialog;
		RadioGroup radioGroup = (RadioGroup) alert.findViewById(R.id.radioGroup);
		int selectedId;
		try {
			// Get selected radio button from radioGroup
			selectedId = radioGroup.getCheckedRadioButtonId();
		} catch (Exception e) {
			Log.e(TAG, "Failed to find checked radio button");
			return null;
		}

		// Find the RadioButton by returned id
		return (RadioButton) alert.findViewById(selectedId);
	}

	//Interface methods---------------------------------------------------------------------
	public int getNumArrows() {
		return numArrows;
	}

	public void addEntry(int buttonTag) {
		dynamicScoreCard.addEntry(buttonTag, textViews);
        Log.i("DrawView", "onTouch: row num: " + (dynamicScoreCard.getPointer() % (getNumArrows()+2)));
    	if (dynamicScoreCard.getPointer() % (getNumArrows()+2) == 0) {
    		Log.i("DrawView", "Create new ScoreLine");
			scrollViewLL.addView(createScoreLine());
		}
    	scrollView.fullScroll(View.FOCUS_DOWN);
	}

	//Saving actions---------------------------------------------------------------------------
	private void save(String name) {
		// Saves totals to score file for score sheet
		String[] fileNames = new String[6];
		fileNames[0] = name + "_round_file";
		fileNames[1] = name + "_distance_file";
		fileNames[2] = name + "_face_file";
		fileNames[3] = name + "_score_file";
		fileNames[4] = name + "_date_file";
		fileNames[5] = name + "_time_file";

		int itemNumber = readListNumber(name);

		String round = distanceLines.get(0).roundEditText.getText()+"";
		String[] save = new String[5];
		saveStringCreation(save);

		FileOutputStream fos;
		try {
			for (int i = 0; i < fileNames.length; i++) {
				fos = openFileOutput(fileNames[i], Context.MODE_APPEND);
				if (i == 0) {
					fos.write((round + delimiter).getBytes());
				} else {
					fos.write((save[i-1] + delimiter).getBytes());
				}
				fos.close();
			}

			scoresSave(name + "_target_" + itemNumber);

			saveListNumber(name, (itemNumber+1) + "");
			cT("Saved");
		} catch (FileNotFoundException e) {
			Log.e(TAG, "save: FileNotFoundException" + e.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Distance, face, score, time, date
	private void saveStringCreation(String save[]) {
		if (save.length == 5) { 

			Calendar c = Calendar.getInstance(); 

			//Appending scores
			StringBuffer distances = new StringBuffer("");
			StringBuffer faces = new StringBuffer("");
			StringBuffer scores = new StringBuffer("");

			int increment; //How many sections used
			increment = distanceLines.size();


			//Adds strings for each section
			for (int i = 0; i < increment; i++) {

				distances.append(distanceLines.get(i).distanceEditText.getText() + "m");
				if (i != increment-1)
					distances.append("\n");

				faces.append(distanceLines.get(i).faceEditText.getText() + "cm");
				if (i != increment-1) {
					faces.append("\n");
				}


				scores.append((dynamicScoreCard.getBlockScore(i+1)-dynamicScoreCard.getBlockScore(i)) + "/" + 
						(dynamicScoreCard.getBlockShots(i+1)-dynamicScoreCard.getBlockShots(i)));
				Log.i(TAG, "saveStringCreation: dynamicScoreCard.getBlockScore(i+1): " + dynamicScoreCard.getBlockScore(i+1));
				Log.i(TAG, "saveStringCreation: dynamicScoreCard.getBlockScore(i): " + dynamicScoreCard.getBlockScore(i));
				Log.i(TAG, "saveStringCreation: dynamicScoreCard.getBlockShots(i+1): " + dynamicScoreCard.getBlockShots(i+1));
				Log.i(TAG, "saveStringCreation: dynamicScoreCard.getBlockShots(i): " + dynamicScoreCard.getBlockShots(i));

				if (increment != 1) {
					scores.append("\n");
				}

			}
			//Appends total score and shots
			if (increment > 1) {
				int totalNumberOfShots = dynamicScoreCard.getBlockShots(increment);
				Log.i(TAG, "saveStringCreation: dynamicScoreCard.getBlockShots(increment): " + dynamicScoreCard.getBlockShots(increment));
				//Assumes this is set in the code above as it is not set in general operation
				scores.append(dynamicScoreCard.getBlockScore(increment) + "/" + totalNumberOfShots);
				Log.i(TAG, "saveStringCreation: dynamicScoreCard.getBlockScore(increment): " + dynamicScoreCard.getBlockScore(increment));
			}	

			save[0] = distances.toString();
			save[1] = faces.toString();
			save[2] = scores.toString();
			save[3] = tString(c.get(Calendar.DATE)) + "/" + tString(c.get(Calendar.MONTH)+1) + "/" + c.get(Calendar.YEAR);
			save[4] = tString(c.get(Calendar.HOUR_OF_DAY)) + ":" + tString(c.get(Calendar.MINUTE)) + ":" + tString(c.get(Calendar.SECOND));
		}
	}

	// Converts time integers to 2 significant figure strings
	private static String tString(int t) {
		String s = t + "";
		if (s.length() == 1) {
			return "0" + s;
		}
		return s;
	}

	// Saves current ScoreCard to file
	private void scoresSave(String fileName) {
		// Saves all data on page such as all data in each textView
		if (fileName == null) {
			fileName = "target_temp_file";
		}
		try {
			FileOutputStream fos;
			fos = openFileOutput(fileName, Context.MODE_PRIVATE);

			fos.write((distanceLines.size()+delimiter).getBytes());
			fos.write((distanceLines.get(0).roundEditText.getText()+delimiter).getBytes());
			for (int i = 0; i < distanceLines.size(); i++) {
				fos.write((distanceLines.get(i).distanceEditText.getText()+delimiter).getBytes());
				fos.write((distanceLines.get(i).faceEditText.getText()+delimiter).getBytes());
			}

			//Save main ScoreCard
			fos.write((numArrows+delimiter).getBytes());

			dynamicScoreCard.saveDistanceChanges(fos);

			int size = textViews.size();
			if (dynamicScoreCard.getPointer() % (numArrows+2) == 0) {
				size -= numArrows+2;
			}
			fos.write((size + delimiter).getBytes());

			for (int i = 0; i < size; i++) {
				fos.write((textViews.get(i).getText() + delimiter).getBytes());
			}

			if (fileName.equals("target_temp_file")) {
				dynamicScoreCard.saveBlockScores(fos);
			}
			
			//Save point locations, spot size/type
			//ScreenSize
			drawView.save(fos);

			fos.close();

		} catch (FileNotFoundException e) {
			Log.e(TAG, "scoresSave: FileNotFoundException" + e.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Saves the new number of items in the list
	private void saveListNumber(String name, String listLength) {
		String listNumberFile = name + "_list_number_file";

		FileOutputStream fos;
		try {
			fos = openFileOutput(listNumberFile, Context.MODE_PRIVATE);
			fos.write((listLength + delimiter).getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "saveListNumber: FileNotFoundException" + e.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Finds how many items in the list
	private int readListNumber(String name) {
		String listNumberFile = name + "_list_number_file";

		byte[] buffer = new byte[1024];
		StringBuffer listNumberFileContent = new StringBuffer("");

		FileInputStream fis;
		try {
			fis = openFileInput(listNumberFile);
			while ((fis.read(buffer)) != -1) {
				listNumberFileContent.append(new String(buffer));
			}
			fis.close();

			return Integer.parseInt(listNumberFileContent.substring(0,listNumberFileContent.lastIndexOf(delimiter)));
		} catch (FileNotFoundException e) {
			Log.e(TAG, "readListNumber: FileNotFoundException" + e.toString());
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}


	// Reads temporary ScoreCard for this name from file
	private void readData() {
		/**
		 * With reading data, you need to read all the data that has been saved
		 * Draw the points on the TargetView
		 * Write the score to the ScoreSheet
		 */

		String tempFile = "target_temp_file";

		// Reads and writes all textView data from previous close
		byte[] buffer = new byte[1024];
		StringBuffer tempFileContent = new StringBuffer("");
		String tempFileData = "";
		String[] temp;

		FileInputStream fis;
		File file = new File(getFilesDir(), tempFile);
		if (file.exists()) {
			try {
				fis = openFileInput(tempFile);
				while ((fis.read(buffer)) != -1) {
					tempFileContent.append(new String(buffer));
				}
				fis.close();
				tempFileData = tempFileContent.substring(0,tempFileContent.lastIndexOf(delimiter));
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			if (!tempFileData.equals("")) {
				temp = tempFileData.split(delimiter);
				
				Log.i(TAG, "readData: entering data");
				enterData(temp);

			}
		} else {
			Log.i(TAG, "readData: temp file not found");
		}
	}

	private void enterData(String[] temp) {
		//Turns input data into information for ScoreCard
		if (temp.length > 10) {
			//Removal in case the numArrows is different
			scrollViewLL.removeViewAt(1);
			removeRowTextViews(textViews, numArrows);

			int counter = 0;
			int distanceLength = Integer.parseInt(temp[counter]);

			for (int i = 0; i < distanceLength-1; i++) {
				DistanceLine distanceLine = new DistanceLine(this, false);
				scrollViewLL.addView(distanceLine.getLine());
				distanceLines.add(distanceLine);
			}

			distanceLines.get(0).roundEditText.setText(temp[++counter]);
			//Account for the round name and the distance and face fields
			for (int i = 0; i < distanceLength; i++) {
				distanceLines.get(i).distanceEditText.setText(temp[++counter]);
				distanceLines.get(i).faceEditText.setText(temp[++counter]);
			}


			try {
				numArrows = Integer.parseInt(temp[++counter]);
				dynamicScoreCard = new DynamicScoreCard(numArrows);
				scrollViewLL.addView(createScoreLine(), 1);

				int[] data = new int[2];
				dynamicScoreCard.inputScoreCard(temp, counter, data);
				counter = data[1]; // 0 is scoreCardSize

				// each DistanceChange is not including all the current distance changes
				// make sure to offset by the first one, then each subsequent recorded one.

				int distanceChanges = dynamicScoreCard.getNumDistanceChanges();
				int increment = 0;
				int lastDistanceChange = 0;
				if (distanceChanges > 0) {
					lastDistanceChange = dynamicScoreCard.getDistanceChangeId(increment++);
				}

				//TODO: FIND OUT WHY!!!
				final int lines;
				if (data[0] == numArrows + 1) {
					lines = (data[0]+1)/(numArrows+2);
				} else {
					lines = data[0]/(numArrows+2);
				}
				
				for (int i = 0; i < lines; i++) {
					if (i >= lastDistanceChange-1) {
						if (increment < distanceChanges) {
							lastDistanceChange = dynamicScoreCard.getDistanceChangeId(increment++);
						} else {
							//Effectively at infinity
							lastDistanceChange = dynamicScoreCard.getPointer();
							++increment;
						}

					}
					//Extra 1 for initial call of createScoreLine() in createLayout
					scrollViewLL.addView(createScoreLine(), i + increment+1); 
				}
				//So that there is no extra line when the current one has not been completed
				while (textViews.size() - numArrows - 2 > dynamicScoreCard.getPointer()) {
					scrollViewLL.removeViewAt(scrollViewLL.getChildCount()-1);
					removeRowTextViews(textViews, numArrows);
				}

				for (int i = 0; i < data[0]; i++) {
					textViews.get(i).setText(temp[++counter]);
				}

				dynamicScoreCard.inputBlockScores(temp, counter, data);
				
				counter = data[1];
				//For the target
//				target.targetView.enterPoints(temp, counter);
				drawView.enterPoints(temp, counter);
				
			} catch (Exception e) {
				Log.e(TAG, "enterData: " + e.toString());
				e.printStackTrace();
			}

		}
	}

	private void removeRowTextViews(ArrayList<TextView> textViews, int numArrows) {
		for (int i = 0; i < (numArrows+2); i++) {
			textViews.remove(textViews.size()-1);
		}
	}

	//Create Toast---------------------------------------------------------------------------
	private void cT(String s) {
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}

	private void generateThumbnail() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean update = sharedPref.getBoolean(SettingsActivity.UPDATE_THUMBNAILS, true);
		if (update) {
			File file = new File(getFilesDir(), TAG+".png");
			
			try {
				OutputStream os = new FileOutputStream(file);
				
				drawView.getRootView().setDrawingCacheEnabled(true);
				getResizedBitmap(drawView.getRootView().getDrawingCache()).compress(CompressFormat.PNG, 100, os);
				drawView.getRootView().setDrawingCacheEnabled(false);
				
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
		//Handle the back button to exit
  		if(keyCode == KeyEvent.KEYCODE_BACK) {
  			alertLeave();
  			return true;
  		}
  		else {
  			return super.onKeyDown(keyCode, event);
  		}
	}

}
