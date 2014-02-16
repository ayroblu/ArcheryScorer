package benl.student.archeryscorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

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
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import benl.student.archeryscorer.layouts.DynamicSectionFragment;

public class DynamicArcheryScoreSheet extends FragmentActivity {
	//Defaults
	private static final String DEFAULT_USERNAME = "User";
	private static final String delimiter = ";";
	private static final String TAG = "DynamicArcheryScoreSheet";
	private static final String TEMP_OPENED_NAMES_FILE = "temp_dynamic_opened_names_file";

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

	private ArrayList<DynamicSectionFragment> fragments = new ArrayList<DynamicSectionFragment>();

	private SharedPreferences sharedPref;


	//Starting activity----------------------------------------------------------------------------
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

			sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

			//Runs upon activity start
			Log.i(TAG, "onCreate: New ArrayLists");
			fragments = new ArrayList<DynamicSectionFragment>();
			boolean result = openOldNames();
			if (!result || fragments.size() < 1) {
				//Runs upon activity start
				//TODO: Settings Integration
				String defaultUsername = sharedPref.getString(SettingsActivity.DEFAULT_USERNAME, DEFAULT_USERNAME);
				addNewFragment(defaultUsername);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		//Already done in manifest
		//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
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
		final String tempOpenedNamesFile = TEMP_OPENED_NAMES_FILE;

		FileOutputStream fos;

		try {
			fos = openFileOutput(tempOpenedNamesFile, Context.MODE_PRIVATE);
			for (int i = 0; i < fragments.size(); i++) {
				fos.write((fragments.get(i).archerName + delimiter).getBytes());
			}
			fos.close();
		} catch (FileNotFoundException e) {
			Log.e("ArcheryScoreScheet", "saveOpenedNames: FileNotFoundException" + e.toString());

		} catch (Exception e) {
			e.printStackTrace();
			cT("Temp Name Save Error");
		}
	}

	private boolean openOldNames() {
		final String tempOpenedNamesFile = TEMP_OPENED_NAMES_FILE;

		byte[] buffer = new byte[1024];
		StringBuffer namesFileContent = new StringBuffer("");
		String namesFileData;


		FileInputStream fis;
		try {
			fis = openFileInput(tempOpenedNamesFile);
			while ((fis.read(buffer)) != -1) {
				namesFileContent.append(new String(buffer));
			}
			fis.close();

			namesFileData = namesFileContent.substring(0,namesFileContent.lastIndexOf(delimiter));


			String[] temp = namesFileData.split(delimiter);
			for (int i = 0; i < temp.length; i++) {
				addNewFragment(temp[i]);
			}
			mViewPager.setOffscreenPageLimit(temp.length);

		} catch (FileNotFoundException e) {
			Log.e("DynamicArcheryScoreSheet", "openOldNames: FileNotFoundException: " + e.toString());
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
	 * sections of the app.
	 */
	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			return fragments.get(i);
		}

		@Override
		public int getCount() {
			return fragments.size(); // Changing this changes the number of pages
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return fragments.get(position).archerName;
		}

		@Override
		public int getItemPosition(Object object) {
			return PagerAdapter.POSITION_NONE;
		}
		
	}


	//Actions-----------------------------------------------------------------------------------
	public void backSpace(View view) {
		fragments.get(mViewPager.getCurrentItem()).backSpace(view);
	}

	public void clearAll(View view) {
		//Ask the user if they want to clear all
		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.clearAll)
		.setMessage(R.string.really_clear)
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Clears everything
				fragments.get(mViewPager.getCurrentItem()).clearAll();
			}

		})
		.setNegativeButton(R.string.no, null)
		.show();
	}


	public void next(View view) {
		fragments.get(mViewPager.getCurrentItem()).changeDistance();
	}

	public void save(View view) {
		alertSave();
	}

	/**
	 * New stuff:
	 * To change the layout completely such that it is dynamically generating each line and distance change
	 * Dynamically also change the button layout depending on the user choice.
	 */

	private void alertSave() {
		//Alert popup when leaving
		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Save")
		.setMessage("Do you want to save the session and quit, or have you finished the shoot and want to save the Score Card?")
		.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (!fragments.get(mViewPager.getCurrentItem()).save()) {
					alertNotify("Save Failed", "Please make sure each of the top fields for the round, distance and face size of the target are filled");
				}
			}
		})
		.setNeutralButton("Save & Quit", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveOpenedNames();
				for (int i = 0; i < fragments.size(); i++) {
					fragments.get(i).scoresSave(null);
				}
				NavUtils.navigateUpFromSameTask(DynamicArcheryScoreSheet.this);
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


	//Menu and ActionBar actions-----------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Creates an option menu from menu button
		getMenuInflater().inflate(R.menu.activity_dynamic_archery_score_sheet, menu);
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
			deleteFiles();
			mViewPager.setCurrentItem(0);
			for (int i=fragments.size()-1; i>0;--i) {
//				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//				transaction.remove(fragments.get(i));
//				fragments.remove(i);
//				transaction.commit();
				mViewPager.removeViewAt(i);
				fragments.remove(i);
			}
			fragments.get(0).clearAll();
			String defaultUsername = sharedPref.getString(SettingsActivity.DEFAULT_USERNAME, DEFAULT_USERNAME);
			fragments.get(0).archerName = defaultUsername;
//			addNewFragment(defaultUsername);
//			mViewPager.setCurrentItem(0);
			mViewPager.getAdapter().notifyDataSetChanged();
			return true;
			
		case R.id.menu_change_button_layout:
			alertButtonLayout();
			return true;

		case R.id.menu_change_end_size:
			alertEndSize();
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
				String name = input.getText().toString();
				//Create New Page
				if (!name.equals("")) {
					//Exit if the current name already exists
					for (int i = 0; i < fragments.size(); i++) {
						if (fragments.get(i).archerName.equals(name)) {
							cT("Name Already Exists");
							return;
						}
					}
					addNewFragment(name);

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
				String name = input.getText().toString();
				//Edit Name
				if (!name.equals("")) {
					// Check that the name doesn't already exist
					for (int i = 0; i < fragments.size(); i++) {
						if (fragments.get(i).archerName.equals(name)) {
							cT("Name Already Exists");
							return;
						}
					}

					(new File(name + "_dynamic_temp_file")).delete();
					fragments.get(mViewPager.getCurrentItem()).archerName = name;
					mViewPager.getAdapter().notifyDataSetChanged();
					dialog.dismiss();
					//TODO: readData: (originally), make sure to delete old file... done?
				}
			}

		})
		.setNegativeButton("Cancel", null)
		.show().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	private void deletePage() {
		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Delete Page")
		.setMessage("Are you sure?")
		.setCancelable(false)
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int index = mViewPager.getCurrentItem();
				
				if (fragments.size()>1) {
					//Avoids changing to a nonexistant page
					if (index >= fragments.size() - 1) {
						mViewPager.setCurrentItem(fragments.size() - 2);
					}

					fragments.get(index).clearAll();
					fragments.remove(index);
//					mViewPager.removeViewAt(index);
					
//					FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//					transaction.remove(fragments.get(index));
//					fragments.remove(index);
//					transaction.commit();
					
					mViewPager.getAdapter().notifyDataSetChanged();
				} else {
					fragments.get(mViewPager.getCurrentItem()).clearAll();
				}

				dialog.dismiss();
			}
		})
		.setNegativeButton(R.string.no, null)
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
					int numArrows = Integer.parseInt(alertFindSelectedRadioButton(R.id.radioGroup, dialog).getText()+ "");

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
				if (checkBox.isChecked() == true) {
					sharedPref.edit().putBoolean(SettingsActivity.CHANGE_END_SIZE_CONFIRM, false).commit();
				}
				changeEndSize(numArrows);
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			//Seems like a bad idea...
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (checkBox.isChecked() == true) {
					sharedPref.edit().putBoolean(SettingsActivity.CHANGE_END_SIZE_CONFIRM, false).commit();
				}
			}
		})
		.show();
	}

	private void changeEndSize(int numArrows) {
		try {
			boolean sameScoreCard = sharedPref.getBoolean(SettingsActivity.SAME_SCORECARD, true);
			if (sameScoreCard) {
				for (int i = 0; i < fragments.size(); i++) {
					fragments.get(i).numArrows = numArrows;
					fragments.get(i).clearAll();
				}
			} else {
				int index = mViewPager.getCurrentItem();
				fragments.get(index).numArrows = numArrows;
				fragments.get(index).clearAll();

			}
		} catch (Exception e) {
			Log.e(TAG, "alertEndSize, onClick");
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

	private void alertButtonLayout() {
		//Alert popup to select spot size
		new AlertDialog.Builder(this)
		.setView(getLayoutInflater().inflate(R.layout.alert_radio_button_layout,null))
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Scoring Layout")
		.setMessage("Select the type of scoring layout")
		.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					int index = mViewPager.getCurrentItem();
					int position = Integer.parseInt(alertFindSelectedRadioButton(R.id.radioGroup, dialog).getTag().toString());

					fragments.get(index).createButtonLayout(position);

				} catch (Exception e) {
					Log.e(TAG, "alertButtonLayout, onClick");
				}
			}

		})
		.setNegativeButton("Cancel", null)
		.show();
	}


	private void addNewFragment(String name) {
		int index = fragments.size();
		fragments.add(new DynamicSectionFragment());
		//So that everyone has the same num of arrows TODO
		boolean sameScoreCard = sharedPref.getBoolean(SettingsActivity.SAME_SCORECARD, true);
		if (sameScoreCard && mViewPager.getCurrentItem() < fragments.size()) {
			fragments.get(index).numArrows = fragments.get(mViewPager.getCurrentItem()).numArrows;
		}

		fragments.get(index).setContext(this);
		fragments.get(index).archerName = name;
		Log.i(TAG, "addNewFragment: new fragment created");
		Log.i(TAG, "addNewFragment: Pointer:"+ fragments.get(index).getPointer());
	}



	//Create Toast and Extras-------------------------------------------------------------------------
	public void cT(String s) { 
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}

	private void deleteFiles() {
		for (int i = 0; i < fragments.size(); i++) {
			File file = new File(getFilesDir(), fragments.get(i).archerName + "_dynamic_temp_file");
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
	}
	
	private void alertLeave() {
		generateThumbnail();
		if (!(fragments.size() == 1 && fragments.get(0).getPointer() == 0)) {
			//Alert popup when leaving
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.quit)
			.setMessage(R.string.really_quit)
			.setPositiveButton("Quit", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					deleteFiles();
					NavUtils.navigateUpFromSameTask(DynamicArcheryScoreSheet.this);
				}
			})
			.setNeutralButton("Save & Quit", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					saveOpenedNames();
					for (int i = 0; i < fragments.size(); i++) {
						fragments.get(i).scoresSave(null);
					}
					NavUtils.navigateUpFromSameTask(DynamicArcheryScoreSheet.this);
				}
			})
			.setNegativeButton("Cancel", null)
			.show();
		} else {
			File file = new File(getFilesDir(), fragments.get(0).archerName + "_dynamic_temp_file");
			if (file.exists() && file.delete()) {
				Log.i(TAG, "alertLeave: File deleted: " + file.getName());
			} else {
				Log.i(TAG, "alertLeave: File not found: " + file.getName());
			}

			file = new File(getFilesDir(), TEMP_OPENED_NAMES_FILE);
			if (file.exists() && file.delete()) {
				Log.i(TAG, "alertLeave: File deleted: " + file.getName());
			} else {
				Log.i(TAG, "alertLeave: File not found: " + file.getName());
			}
			NavUtils.navigateUpFromSameTask(DynamicArcheryScoreSheet.this);
		}
	}

	private void generateThumbnail() {
		boolean update = sharedPref.getBoolean(SettingsActivity.UPDATE_THUMBNAILS, true);
		if (update) {
			File file = new File(getFilesDir(), TAG+".png");
			
			try {
				OutputStream os = new FileOutputStream(file);
				
				mViewPager.getRootView().setDrawingCacheEnabled(true);
				getResizedBitmap(mViewPager.getRootView().getDrawingCache()).compress(CompressFormat.PNG, 100, os);
//				mViewPager.getRootView().getDrawingCache().compress(CompressFormat.PNG, 100, os);
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

	//Program Exit
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
