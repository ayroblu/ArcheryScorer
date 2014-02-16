package benl.student.archeryscorer;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import benl.student.archeryscorer.layouts.TargetView;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TargetViewer extends FragmentActivity implements ActionBar.TabListener {
	
	private static final String delimiter = ";";
	private DynamicScoreCard dynamicScoreCard;
	private ScoreSheetSectionFragment scoreSheet;
	private TargetSectionFragment target;
	private static Point outSize = new Point();
	private Button mainButton;
//	private static TargetViewer parent;

	ArrayList<DistanceLine> distanceLines = new ArrayList<DistanceLine>();
	int numArrows = 6;
	

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_target_viewer);


		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		highAPIActionBar();
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
    		setSizeHighAPI(outSize);
    	} else {
    		setSizeLowAPI(outSize);
    	}
		
//		parent = this;
		dynamicScoreCard = new DynamicScoreCard();

		createTargetLayout();
		createScoreSheetLayout();
		
		try {
			Intent intent = getIntent();
			int position = intent.getIntExtra(ListScores.EXTRA_NUM, 0);
			String name = intent.getStringExtra(ListScores.EXTRA_NAME);

			readData(name,position);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void highAPIActionBar() {
		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		// Set up the action bar.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			final ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
				@Override
				public void onPageSelected(int position) {
					actionBar.setSelectedNavigationItem(position);
				}
			});

			// For each of the sections in the app, add a tab to the action bar.
			for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
				// Create a tab with text corresponding to the page title defined by
				// the adapter. Also specify this Activity object, which implements
				// the TabListener interface, as the callback (listener) for when
				// this tab is selected.
				actionBar.addTab(actionBar.newTab()
						.setText(mSectionsPagerAdapter.getPageTitle(i))
						.setTabListener(this));
			}
		}
	}


	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mViewPager.setCurrentItem(tab.getPosition());
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			if (position == 0) {
				return target;
			} else {
				return scoreSheet;
			}
		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(Locale.getDefault());
			case 1:
				return getString(R.string.title_section2).toUpperCase(Locale.getDefault());
				//			case 2:
				//				return getString(R.string.title_section3).toUpperCase(Locale.getDefault());
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class TargetSectionFragment extends Fragment {
		TargetView targetView;
		LinearLayout ll;

		public TargetSectionFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			return ll;
		}
	}
	
	public static class ScoreSheetSectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
		
		LinearLayout scrollViewll;
		ScrollView scrollView;
		ArrayList<TextView> textViews = new ArrayList<TextView>();

		public ScoreSheetSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			return scrollView;
		}
	}

	
	private void createTargetLayout() {
		target = new TargetSectionFragment();
		target.targetView = new TargetView(this);
		target.targetView.setNumArrows(numArrows);
		target.ll = new LinearLayout(this);
		target.ll.setOrientation(LinearLayout.VERTICAL);
		target.ll.addView(target.targetView, new LinearLayout.LayoutParams(outSize.x, outSize.x));
		

		LinearLayout.LayoutParams btParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,1);
		LinearLayout.LayoutParams mainParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
		LinearLayout ll = new LinearLayout(this);
		ll.setLayoutParams(lp);
		ll.setGravity(Gravity.BOTTOM);
		
		ll.addView(createButton(btParams, 0));
		ll.addView(createButton(btParams, 1));
		mainButton = createButton(mainParams, 2);
		ll.addView(mainButton);
		ll.addView(createButton(btParams, 3));
		ll.addView(createButton(btParams, 4));
		target.ll.addView(ll);
	}
	
	private Button createButton(LinearLayout.LayoutParams btParams, final int index) {
		final String name;
		switch (index) {
		case 0:
			name = "\u21E4";
			break;

		case 1:
			name = "\u2190";
			break;

		case 2:
			name = "All";
			break;

		case 3:
			name = "\u2192";
			break;
			
		case 4:
			name = "\u21E5";
			break;
			
		default:
			name = "";
			break;
		}
		
		//Button to alternate between ends and all
		final Button button = new Button(this);
		View.OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (index) {
				case 0:
					target.targetView.setEnd(index);
					break;

				case 1:
					target.targetView.setEnd(index);
					break;

				case 2:
					if (target.targetView.alternatePoints()) {
						button.setText("All");
					} else {
						button.setText("End "+ (target.targetView.getEnd()+1) + "/" + (target.targetView.getEndSize()+1));
					}
					target.targetView.setEnd(index);
					return;

				case 3:
					target.targetView.setEnd(index);
					break;
					
				case 4:
					target.targetView.setEnd(index);
					break;
					
				default:
					break;
				}
				if (!mainButton.getText().toString().equals("All")) {
					mainButton.setText("End "+ (target.targetView.getEnd()+1) + "/" + (target.targetView.getEndSize()+1));
				}
			}
		};
		button.setOnClickListener(clickListener);
		button.setGravity(Gravity.CENTER);
		button.setText(name);
		button.setLayoutParams(btParams);
		return button;
	}
	
	private void createScoreSheetLayout() {
		scoreSheet = new ScoreSheetSectionFragment();
		scoreSheet.scrollView = new ScrollView(this);
		scoreSheet.scrollViewll = new LinearLayout(this);
		scoreSheet.scrollViewll.setOrientation(LinearLayout.VERTICAL);
		scoreSheet.scrollView.addView(scoreSheet.scrollViewll);
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
	
	

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
  		case android.R.id.home:
  			this.finish();
			return true;
			
		default:
			return super.onMenuItemSelected(featureId, item);
		}
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

    		if (scoreSheet.textViews.size() == numArrows + 1) {
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

    		textView.setTextSize(20);



    		textView.setLayoutParams(tvParams);

    		scoreLL.addView(textView);
    		scoreSheet.textViews.add(textView);
    	}
    	return scoreLL;
    }

	// Reads temporary ScoreCard for this name from file
	private void readData(String name, int index) {
		/**
		 * With reading data, you need to read all the data that has been saved
		 * Draw the points on the TargetView
		 * Write the score to the ScoreSheet
		 */
		
		String tempFile = name + "_target_" + index;

		// Reads and writes all textView data from previous close
		byte[] buffer = new byte[1024];
		StringBuffer tempFileContent = new StringBuffer("");
		String tempFileData = "";
		String[] temp;

		FileInputStream fis;
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
			
			enterData(temp);
			
		}
	}
	
	private void enterData(String[] temp) {
		//Turns input data into information for ScoreCard
		if (temp.length > 10) {
			int distanceLength = Integer.parseInt(temp[0]);
			int counter = 0;
			
			DistanceLine distanceLine = new DistanceLine(this, true);
			distanceLine.disableLine();
			scoreSheet.scrollViewll.addView(distanceLine.getLine());
			distanceLines.add(distanceLine);
			
			for (int i = 0; i < distanceLength-1; i++) {
				distanceLine = new DistanceLine(this, false);
				distanceLine.disableLine();
				scoreSheet.scrollViewll.addView(distanceLine.getLine());
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
				
				for (int i = 0; i < data[0]/(numArrows+2); i++) {
					if (i >= lastDistanceChange) {
						if (increment < distanceChanges) {
							lastDistanceChange = dynamicScoreCard.getDistanceChangeId(increment++);
						} else {
							//Effectively at infinity
							lastDistanceChange = dynamicScoreCard.getPointer();
							++increment;
						}
						
					}
					scoreSheet.scrollViewll.addView(createScoreLine(), i + increment);
				}
				
				for (int i = 0; i < data[0]; i++) {
					scoreSheet.textViews.get(i).setText(temp[++counter]);
				}
				
				//For the target
				target.targetView.enterPoints(temp, counter);

			} catch (Exception e) {
				Log.e("TargetViewer", "enterData: " + e.toString());
				e.printStackTrace();
			}

		}
	}

	
//	private void cT(String s) {
//		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
//	}

}
