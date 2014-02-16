package benl.student.archeryscorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import benl.student.archeryscorer.R.id;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ArcheryScoreSheet extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
	 * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
	 * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
	 * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	static ViewPager mViewPager;


	/**
	 * Basic code
	 */
	private static ArrayList<ScoreCard> scoreCards = new ArrayList<ScoreCard>();
	private static ArrayList<DummySectionFragment> fragments = new ArrayList<DummySectionFragment>();
	private static ArrayList<String> pageNames = new ArrayList<String>();

	private static final int NUM_BLOCKS = 4;
	private static final int NUM_TEXTVIEWS = 192;
	private static final int NUM_TV_IN_BLOCK = 48; //TV is TextViews
	private static final int END_FIRST_ROW = 7;

	private static final String delimiter = ";";
	private static final String TEMP_OPENED_NAMES_FILE = "temp_opened_names_file";
	private static final String DEFAULT_USERNAME = "User";
	private static final String TAG = "ArcheryScoreSheet";

	private static ArcheryScoreSheet parent;
	private boolean leaving = false;


	//Construction-------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_archery_score_sheet);
		setupActionBar();

		try {
			// Create the adapter that will return a fragment for each of the three primary sections
			// of the app.
			mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


			// Set up the ViewPager with the sections adapter.
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mSectionsPagerAdapter);


			scoreCards = new ArrayList<ScoreCard>();
			fragments = new ArrayList<DummySectionFragment>();
			pageNames = new ArrayList<String>();

			//Runs upon activity start
			boolean result = openOldNames();
			if (!result || pageNames.size() < 1) {
				Log.i(TAG, "onCreate: New ArrayLists");
				scoreCards = new ArrayList<ScoreCard>();
				fragments = new ArrayList<DummySectionFragment>();
				pageNames = new ArrayList<String>();

				//Runs upon activity start
				scoreCards.add(new ScoreCard(this));

				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
				String defaultUsername = sharedPref.getString(SettingsActivity.DEFAULT_USERNAME, DEFAULT_USERNAME);
				pageNames.add(defaultUsername);
			} else {
				for (int i = 0; i < pageNames.size(); i++) {
					scoreCards.add(new ScoreCard(this));
					fragments.add(new DummySectionFragment());
					fragments.get(i).setTheIndex(i);
				}
			}

			parent = this;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}


	//Saves the new number of items in the list
	private void saveOpenedNames() {
		FileOutputStream fos;

		try {
			fos = openFileOutput(TEMP_OPENED_NAMES_FILE, Context.MODE_PRIVATE);
			for (int i = 0; i < pageNames.size(); i++) {
				fos.write((pageNames.get(i) + delimiter).getBytes());
			}
			fos.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "saveOpenedNames: FileNotFoundException" + e.toString());

		} catch (Exception e) {
			e.printStackTrace();
			cT("Temp Name Save Error");
		}
	}

	private boolean openOldNames() {
		File file = new File(getFilesDir(), TEMP_OPENED_NAMES_FILE);

		byte[] buffer = new byte[1024];
		StringBuffer namesFileContent = new StringBuffer("");
		String namesFileData;


		FileInputStream fis;
		if (file.exists()) {
			try {
				fis = openFileInput(TEMP_OPENED_NAMES_FILE);
				while ((fis.read(buffer)) != -1) {
					namesFileContent.append(new String(buffer));
				}
				fis.close();

				namesFileData = namesFileContent.substring(0,namesFileContent.lastIndexOf(delimiter));

				pageNames = new ArrayList<String>(Arrays.asList(namesFileData.split(delimiter))); 

				for (int i = 0; i < pageNames.size(); i++) {
					Log.i(TAG, "openOldNames: pageNames " + i + pageNames.get(i));
				}
			} catch (FileNotFoundException e) {
				Log.e(TAG, "openOldNames: FileNotFoundException: " + e.toString());
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}


	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
	 * sections of the app.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			if (fragments.size() < i+1) {
				fragments.add(new DummySectionFragment());
				fragments.get(i).setTheIndex(i);
			}

			return fragments.get(i);
		}

		@Override
		public int getCount() {
			return pageNames.size(); // Changing this changes the number of pages
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return pageNames.get(position);
		}

		@Override
		public int getItemPosition(Object object) {
			return PagerAdapter.POSITION_NONE;
		}

	}

	/**
	 * A dummy fragment representing a section of the app, but that simply displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {

		private int index;
		private View view = null;
		private boolean justStarted = true;

		public DummySectionFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			if (view == null) {
				view = inflater.inflate(R.layout.fragment_score_sheet, container, false);
			} else {
				if (view.getParent() != null) {
					((ViewGroup) view.getParent()).removeView(view);
				}
			}
			return view;
		}

		@Override
		public void onStart() {
			super.onStart();
			if (justStarted) {
				parent.tempRead(index);
				justStarted = false;
			}
		}

		@Override
		public void onPause() {
			super.onPause();
			if (index < pageNames.size() && !parent.leaving) parent.scoresSave(null, index);
		}


		public void setTheIndex(int index) {
			this.index = index;
		}
	}



	//Mathematical actions-----------------------------------------------------------------
	public void addPoints(View view) {
		// Condition checks that we have not exceeded the maximum number of scores
		if (scoreCards.get(mViewPager.getCurrentItem()).getPointer() < NUM_TEXTVIEWS) {
			//Read the tag associated with each button, each one should be a number
			int buttonTag;
			try {
				buttonTag = Integer.parseInt(view.getTag().toString());
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			// Adds the data to the scoreCard, checking for Xs and misses
			scoreCards.get(mViewPager.getCurrentItem()).addEntry(buttonTag, fragments.get(mViewPager.getCurrentItem()));

			scrollToPoints(mViewPager.getCurrentItem());
		} else {
			//Finished ScoreCard
			cT("End of ScoreCard Reached");
		}
	}


	//Removal actions
	public void backSpace(View view) {
		scoreCards.get(mViewPager.getCurrentItem()).undo(fragments.get(mViewPager.getCurrentItem()));
		scrollToPoints(mViewPager.getCurrentItem());
	}

	public void clearAll(View view) {
		if (scoreCards.get(mViewPager.getCurrentItem()).getPointer() > 0) {
			//Ask the user if they want to clear all
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.clearAll)
			.setMessage(R.string.really_clear)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					//Clears everything
					clearAll();
				}

			})
			.setNegativeButton(R.string.no, null)
			.show();
		}
	}

	private void clearAll() {
		// Clears all blocks and resets values
		for (int i = 0; i < NUM_TEXTVIEWS; i++) {
			findTextView("textView" + i).setText("");
		}
		scoreCards.set(mViewPager.getCurrentItem(), new ScoreCard(this));

		findEditText("editText1").setText("");
		for (int i = 1; i <= NUM_BLOCKS; i++) {
			findEditText("editTextd" + i).setText("");
			findEditText("editTextf" + i).setText("");
		}

		scrollToPoints(mViewPager.getCurrentItem());
	}


	//Transition actions
	private void scrollToPoints(int index) {
		// Scrolls to where the points are
		int position = 0;
		int[] location = new int[2];
		int[] locationScrollView = new int[2];

		int pointer = scoreCards.get(index).getPointer();
		int number;

		if (pointer % NUM_TV_IN_BLOCK == 0 && pointer > 0 && (pointer / NUM_TV_IN_BLOCK) < 3) {
			number = scoreCards.get(index).getBlockNumber();
		} else {
			number = scoreCards.get(index).getBlockNumber()+1;
		}

		findEditText("editTextd" + (number)).getLocationInWindow(location);

		ScrollView scrollview1 = (ScrollView) fragments.get(index).getView().findViewById(R.id.scrollView1);
		position = scrollview1.getScrollY();
		scrollview1.getLocationInWindow(locationScrollView);
		scrollview1.smoothScrollTo(0, location[1] + position - locationScrollView[1]);
	}

	public void next(View view) {
		// Moves all pointers to next block
		if (scoreCards.get(mViewPager.getCurrentItem()).getBlockNumber() < 3 && 
				scoreCards.get(mViewPager.getCurrentItem()).getPointer() % NUM_TV_IN_BLOCK != 0) {
			scoreCards.get(mViewPager.getCurrentItem()).nextBlock();
			scrollToPoints(mViewPager.getCurrentItem());
		} else {
			cT("End of Score Card");
		}
	}


	//Saving actions-------------------------------------------------------------------------
	private void alertSave() {
		//Alert popup when leaving
		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Save")
		.setMessage("Do you want to save the session and quit, or have " +
				"you finished the shoot and want to save the Score Card?")
				.setPositiveButton("Save", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int dataCheck = findEditText("editText1").getText().toString().length();
						dataCheck *= findEditText("editTextd1").getText().toString().length();
						dataCheck *= findEditText("editTextf1").getText().toString().length();

						if (dataCheck == 0) {
							alertNotify("Save Failed", "Please make sure each of the top fields " +
									"for the round, distance and face size of the target are filled");
						} else {
							save();
						}
					}
				})
				.setNeutralButton("Save & Quit", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						saveOpenedNames();
						leaving = true;
						NavUtils.navigateUpFromSameTask(parent);
					}
				})
				.setNegativeButton("Cancel", null)
				.show();
	}

	public void save() {
		// Saves totals to score file for score sheet
		String archerName = pageNames.get(mViewPager.getCurrentItem());
		String roundFile = archerName + "_round_file";
		String distanceFile = archerName + "_distance_file";
		String faceFile = archerName + "_face_file";
		String scoreFile = archerName + "_score_file";
		String dateFile = archerName + "_date_file";
		String timeFile = archerName + "_time_file";

		int itemNumber = readListNumber();

		String round = findEditText("editText1").getText().toString();
		String[] save = new String[5];
		saveStringCreation(save);

		FileOutputStream fos;
		try {
			fos = openFileOutput(roundFile, Context.MODE_APPEND);
			fos.write((round + delimiter).getBytes());
			fos.close();
			fos = openFileOutput(distanceFile, Context.MODE_APPEND);
			fos.write((save[0] + delimiter).getBytes());
			fos.close();
			fos = openFileOutput(faceFile, Context.MODE_APPEND);
			fos.write((save[1] + delimiter).getBytes());
			fos.close();
			fos = openFileOutput(scoreFile, Context.MODE_APPEND);
			fos.write((save[2] + delimiter).getBytes());
			fos.close();
			fos = openFileOutput(dateFile, Context.MODE_APPEND);
			fos.write((save[3] + delimiter).getBytes());
			fos.close();
			fos = openFileOutput(timeFile, Context.MODE_APPEND);
			fos.write((save[4] +delimiter).getBytes());
			fos.close();

			scoresSave(pageNames.get(mViewPager.getCurrentItem()) + "_" + itemNumber, mViewPager.getCurrentItem());

			saveListNumber((itemNumber+1) + "");
			cT("Saved");
		} catch (FileNotFoundException e) {
			Log.e(TAG, "save: FileNotFoundException" + e.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void save(View view) {
		alertSave();
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
			increment = scoreCards.get(mViewPager.getCurrentItem()).getBlockNumber() + 1;
			int pointer = scoreCards.get(mViewPager.getCurrentItem()).getPointer();
			if (pointer % NUM_TV_IN_BLOCK == 0 && increment > 1 && increment < 4) {
				--increment;
			}

			//Adds strings for each section
			for (int i = 1; i <= increment; i++) {
				if (findEditText("editTextd" + i).getText().length() != 0) {
					distances.append(findEditText("editTextd" + i).getText() + "m");
					if (i != increment)
						distances.append("\n");
				} else {
					distances.append("\n");
				}
				if (findEditText("editTextf" + i).getText().length() != 0) {
					faces.append(findEditText("editTextf" + i).getText() + "cm");
					if (i != increment) {
						faces.append("\n");
					}
				} else {
					faces.append("\n");
				}


				if (i > 1) {
					scores.append("\n");
				}
				scores.append(scoreCards.get(mViewPager.getCurrentItem()).getScoreForBlock(i-1) + "/" + 
						scoreCards.get(mViewPager.getCurrentItem()).getShotsForBlock(i-1));

			}
			//Appends total score and shots
			if (increment > 1) {
				int totalNumberOfShots = 0;
				for (int i = 0; i <= increment-1; i++) {
					totalNumberOfShots += scoreCards.get(mViewPager.getCurrentItem()).getShotsForBlock(i);
				}
				//Assumes this is set in the code above as it is not set in general operation
				scores.append("\n" + scoreCards.get(mViewPager.getCurrentItem()).getBlockScore(increment-1) + "/" + totalNumberOfShots);
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
	private void scoresSave(String fileName, int index) {
		// Saves all data on page such as all data in each textView
		if (fileName == null) {
			fileName = pageNames.get(index) + "_temp_file";
		}
		try {
			FileOutputStream fos;
			fos = openFileOutput(fileName, Context.MODE_PRIVATE);

			fos.write((findEditText("editText1", index).getText().toString() + delimiter).getBytes());
			for (int i = 1; i <= NUM_BLOCKS; i++) {
				fos.write((findEditText("editTextd" + i, index).getText().toString() + delimiter).getBytes());
				fos.write((findEditText("editTextf" + i, index).getText().toString() + delimiter).getBytes());
			}
			scoreCards.get(index).saveScoreCard(fos);
			fos.close();

		} catch (FileNotFoundException e) {
			Log.e("ArcheryScoreScheet", "scoresSave: FileNotFoundException" + e.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Reads temporary ScoreCard for this name from file
	private void tempRead(int index) {
		String tempFile = pageNames.get(index) + "_temp_file";

		// Reads and writes all textView data from previous close
		byte[] buffer = new byte[1024];
		StringBuffer tempFileContent = new StringBuffer("");
		String tempFileData = null;
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
			return;
		}
		if (!tempFileData.equals("")) {
			temp = tempFileData.split(delimiter);

			//Turns input data into information for ScoreCard
			if (temp.length > 200) {
				findEditText("editText1", index).setText(temp[0]);
				int counter = 1;
				for (int i = 1; i <= 4; i++) {
					findEditText("editTextd" + i, index).setText(temp[counter]);
					findEditText("editTextf" + i, index).setText(temp[counter+1]);
					counter += 2;
				}

				try {
					scoreCards.get(index).inputScoreCard(temp, counter);

					readDataFromScoreCard(index);

					scrollToPoints(index);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}	
	}

	// Turns the data in the ScoreCard into information on the page
	private void readDataFromScoreCard(int index) {
		if (scoreCards.get(index).getPointer() > 0) {
			LastEntry lastEntry;

			// First loop loops through each block
			for (int i = 0; i <= scoreCards.get(index).getBlockNumber(); i++) {

				//Finds the last entry for that block, if its not used, the method exits
				if (i < scoreCards.get(index).getBlockNumber()) {
					lastEntry = new LastEntry(scoreCards.get(index).getOldPointer(i));
				} else if (i == scoreCards.get(index).getBlockNumber()) {
					lastEntry = new LastEntry(scoreCards.get(index).getPointer());
				} else {
					return;
				}

				// Second loop loops through each entry
				for (int j = i*NUM_TV_IN_BLOCK; j <= lastEntry.getEndOfRow(); j++) {
					if (j <= lastEntry.getLastEntry()) {

						//Checks and inserts correct data into textViews
						if (scoreCards.get(index).isMiss(j)) {
							findTextView("textView" + j, index).setText("M");
						} else if (scoreCards.get(index).isX(j)) {
							findTextView("textView" + j, index).setText("X");
						} else if (j != END_FIRST_ROW) {
							findTextView("textView" + j, index).setText(scoreCards.get(index).getScoreCardValue(j) + "");
						}

						// Checks and enters totals for the final end totals
					} else if (j == lastEntry.getRowTotalIndex()) {
						findTextView("textView" + j, index).setText(scoreCards.get(index).getScoreCardValue(j) + "");
					} else if (j == lastEntry.getEndOfRow() && j > END_FIRST_ROW) {
						findTextView("textView" + j, index).setText(scoreCards.get(index).getScoreCardValue(j) + "");
					}
				}
			}
		}
	}

	//Saves the new number of items in the list
	private void saveListNumber(String listLength) {
		String listNumberFile = pageNames.get(mViewPager.getCurrentItem()) + "_list_number_file";

		FileOutputStream fos;
		try {
			fos = openFileOutput(listNumberFile, Context.MODE_PRIVATE);
			fos.write((listLength + delimiter).getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			Log.e("ArcheryScoreScheet", "saveListNumber: FileNotFoundException" + e.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Finds how many items in the list
	private int readListNumber() {
		String listNumberFile = pageNames.get(mViewPager.getCurrentItem()) + "_list_number_file";

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
			Log.e("ArcheryScoreScheet", "readListNumber: FileNotFoundException" + e.toString());
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}


	/**
	 * New stuff:
	 * To change the layout completely such that it is dynamically generating each line and distance change
	 * Dynamically also change the button layout depending on the user choice.
	 */




	//Menu and ActionBar actions------------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Creates an option menu from menu button
		getMenuInflater().inflate(R.menu.activity_archery_score_sheet, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection

		switch (item.getItemId()) {
		case R.id.menu_add_page:
			addPage();
			return true;
		case R.id.menu_edit_name:
			editName();
			return true;
		case R.id.menu_delete:
			deletePage();
			return true;
		case R.id.menu_delete_all:
			scoreCards = new ArrayList<ScoreCard>();
			scoreCards.add(new ScoreCard(this));
			fragments = new ArrayList<DummySectionFragment>();
			fragments.add(new DummySectionFragment());
			fragments.get(0).setTheIndex(0);
			pageNames = new ArrayList<String>();
			pageNames.add("User");
			mViewPager.setCurrentItem(0);
			mViewPager.getAdapter().notifyDataSetChanged();

			return true;

		case android.R.id.home:
			alertLeave();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void addPage() {
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
		.setTitle("New Page")
		.setMessage("Name of New Archer")
		.setCancelable(false)
		.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Create New Page
				if (!input.getText().toString().equals("")) {
					//Exit if the current name already exists
					//  					for (int i = 0; i < pageNames.size(); i++) {
					//						if (input.getText().toString().equals(pageNames.get(i))) {
					//							cT("Name Already Exists");
					//							return;
					//						}
					//					}

					if (pageNames.contains(input.getText().toString())) {
						cT("Name Already Exists");
						return;
					}

					pageNames.add(input.getText().toString());
					scoreCards.add(new ScoreCard(ArcheryScoreSheet.this));
					mViewPager.getAdapter().notifyDataSetChanged();
					mViewPager.setCurrentItem(fragments.size()-1);
					dialog.dismiss();
				}
			}

		})
		.setNegativeButton("Cancel", null)
		.show().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	private void editName() {
		final FrameLayout fl = new FrameLayout(this);
		final EditText input = new EditText(this);
		input.setGravity(Gravity.CENTER);
		input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		input.setHint("Name");
		input.setHintTextColor(getResources().getColor(R.drawable.green));
		fl.addView(input, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

		new AlertDialog.Builder(this)
		.setView(fl)
		.setTitle("Name")
		.setMessage("Enter the new Name")
		.setCancelable(false)
		.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Create New Page
				if (!input.getText().toString().equals("")) {
					// Check that the name doesn't already exist
					for (int i = 0; i < pageNames.size(); i++) {
						if (input.getText().toString().equals(pageNames.get(i))) {
							cT("Name already exists");
							return;
						}
					}

					(new File(pageNames.get(mViewPager.getCurrentItem()) + "_temp_file")).delete();
					pageNames.set(mViewPager.getCurrentItem(), input.getText().toString());
					mViewPager.getAdapter().notifyDataSetChanged();
					dialog.dismiss();
					tempRead(mViewPager.getCurrentItem());
				}
			}

		})
		.setNegativeButton("Cancel", null)
		.show().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	private void deletePage() {
		if (fragments.size()>1) {
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle("Delete Page")
			.setMessage("Are you sure?")
			.setCancelable(false)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int index = mViewPager.getCurrentItem();

					//Avoids changing to a nonexistant page
					if (index >= pageNames.size() - 1) {
						mViewPager.setCurrentItem(pageNames.size() - 2);
					}

					//Removes all from arraylists
					pageNames.remove(index);
					scoreCards.remove(index);
					fragments.remove(index);

					for (int i = index; i < fragments.size(); i++) {
						fragments.get(i).setTheIndex(i);
					}
					mViewPager.getAdapter().notifyDataSetChanged();

					dialog.dismiss();
				}
			})
			.setNegativeButton(R.string.no, null)
			.show();
		}
	}


	//View finding simplifiers--------------------------------------------------------------------------
	private TextView findTextView(String name) {
		// Finds textView address
		try {
			Class<id> aClass = R.id.class;
			int aid = aClass.getField(name).getInt(aClass);
			return (TextView) fragments.get(mViewPager.getCurrentItem()).getView().findViewById(aid);
		} catch(Exception e) {		
			e.printStackTrace();
			return null;
		}
	}

	private TextView findTextView(String name, int index) {
		// Finds textView address
		try {
			Class<id> aClass = R.id.class;
			int aid = aClass.getField(name).getInt(aClass);
			return (TextView) fragments.get(index).getView().findViewById(aid);
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
			return (EditText) fragments.get(mViewPager.getCurrentItem()).getView().findViewById(aid);
		}catch(Exception e) {		
			e.printStackTrace();
			return null;
		}
	}

	private EditText findEditText(String name, int index) {
		// Finds editText Address
		try {
			Class<id> aClass = R.id.class;
			int aid = aClass.getField(name).getInt(aClass);
			return (EditText) fragments.get(index).getView().findViewById(aid);
		} catch(Exception e) {		
			e.printStackTrace();
			return null;
		}
	}


	//create Toast--------------------------------------------------------------------------------
	public void cT(String s) { 
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
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

		//Alert popup when leaving
		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.quit)
		.setMessage(R.string.really_quit)
		.setPositiveButton("Quit", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				for (int i = 0; i < pageNames.size(); i++) {
					File file = new File(getFilesDir(), pageNames.get(i) + "_temp_file");
					if (file.exists() && file.delete()) {
						Log.i(TAG, "alertLeave: File deleted: " + file.getName());
					} else {
						Log.i(TAG, "alertLeave: File not found: " + file.getName());
					}
				}

				File file = new File(getFilesDir(), TEMP_OPENED_NAMES_FILE);
				if (file.exists() && file.delete()) {
					Log.i(TAG, "alertLeave: File deleted: " + file.getName());
				} else {
					Log.i(TAG, "alertLeave: File not found: " + file.getName());
				}
				leaving = true;
				NavUtils.navigateUpFromSameTask(parent);
			}
		})
		.setNeutralButton("Save & Quit", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveOpenedNames();
				NavUtils.navigateUpFromSameTask(parent);
			}
		})
		.setNegativeButton("Cancel", null)
		.show();
	}

	private void generateThumbnail() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean update = sharedPref.getBoolean(SettingsActivity.UPDATE_THUMBNAILS, true);
		if (update) {
			File file = new File(getFilesDir(), TAG+".png");

			try {
				OutputStream os = new FileOutputStream(file);

				mViewPager.getRootView().setDrawingCacheEnabled(true);
				getResizedBitmap(mViewPager.getRootView().getDrawingCache()).compress(CompressFormat.PNG, 100, os);
				mViewPager.getRootView().setDrawingCacheEnabled(false);

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

	//Program Exit-----------------------------------------------------------------------------
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//Handle the back button to exit
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			//Ask the user if they want to quit
			alertLeave();

			return true;
		}
		else {
			return super.onKeyDown(keyCode, event);
		}

	}
}
