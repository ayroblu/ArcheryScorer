package benl.student.archeryscorer.layouts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import benl.student.archeryscorer.ButtonLayoutType;
import benl.student.archeryscorer.DistanceLine;
import benl.student.archeryscorer.DynamicScoreCard;
import benl.student.archeryscorer.R;
import benl.student.archeryscorer.SettingsActivity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;

public class DynamicSectionFragment extends Fragment {
	private static final String TAG = "DynamicSectionFragment";
	private static final int NUM_ARROWS = 6;
	private static final String delimiter = ";";


	private ArrayList<TextView> textViews = new ArrayList<TextView>();
	private LinearLayout scrollViewll;
	private LinearLayout buttonll;
	private ArrayList<DistanceLine> distanceLines = new ArrayList<DistanceLine>();
	public int numArrows = NUM_ARROWS;
	public String archerName;

	private View view;
	private boolean justStarted = true;
	private Context context;
	private DynamicScoreCard scoreCard;

	//Construction / Layout-------------------------------------------------------------------------------
	public DynamicSectionFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: called, view: " + view);
		if (view == null) {
			createLayout(inflater, container);
		} else {
			if (view.getParent() != null) {
				((ViewGroup) view.getParent()).removeView(view);
			}
		}
		Log.d(TAG, "onCreateView: viewParent: " + view.getParent());
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		if (justStarted) {
			Log.i(TAG, "DummySectionFragment: onStart: calling readData");
			readData();
			justStarted = false;
		}
	}

	public void setContext(Context context) {
		this.context = context;
		scoreCard = new DynamicScoreCard(numArrows);
		//TODO: This doesn't seem right...
	}

	private void createLayout(LayoutInflater inflater, ViewGroup container) {
		view = inflater.inflate(R.layout.fragment_dynamic_score_sheet, container, false);

		scrollViewll = ((LinearLayout) view.findViewById(R.id.scrollViewll));
		Log.i(TAG, "scrollViewll receives focus: " + scrollViewll.requestFocus());
		DistanceLine distanceLine = new DistanceLine(context, true);
		scrollViewll.addView(distanceLine.getLine());
		distanceLines.add(distanceLine);
		try {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			scrollViewll.addView(createScoreLine(numArrows));
			buttonll = ((LinearLayout) view.findViewById(R.id.buttonll));

			int buttonLayout = Integer.parseInt(sharedPref.getString(SettingsActivity.INPUT_TYPE, ButtonLayoutType.FULL+""));
			createButtonLayout(buttonLayout);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createButtonLayout(int layoutType) {
		buttonll.removeAllViews();

		String[] buttonNames;
		String[] buttonTags;

//		int pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics());
		LinearLayout.LayoutParams lp;
		lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		buttonll.setLayoutParams(lp);

		switch (layoutType) {
		case ButtonLayoutType.FULL:
			buttonNames = new String[] {"X", "10", "9", "8"};
			buttonTags = new String[] {"11", "10", "9", "8"};

			buttonll.addView(buttonLine(buttonNames, buttonTags));

			buttonNames = new String[] {"7", "6", "5", "4"};
			buttonTags = new String[] {"7", "6", "5", "4"};

			buttonll.addView(buttonLine(buttonNames, buttonTags));

			buttonNames = new String[] {"3", "2", "1", "M"};
			buttonTags = new String[] {"3", "2", "1", "0"};

			buttonll.addView(buttonLine(buttonNames, buttonTags));

			lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			buttonll.setLayoutParams(lp);

			return;

		case ButtonLayoutType.SPOT:
			buttonNames = new String[] {"X", "10", "9", "8"};
			buttonTags = new String[] {"11", "10", "9", "8"};

			buttonll.addView(buttonLine(buttonNames, buttonTags));
			buttonNames = new String[] {"7", "6", "5", "M"};
			buttonTags = new String[] {"7", "6", "5", "0"};

			buttonll.addView(buttonLine(buttonNames, buttonTags));

			return;

		case ButtonLayoutType.INDOOR_SPOT:
			buttonNames = new String[] {"X", "10", "9"};
			buttonTags = new String[] {"11", "10", "9"};

			buttonll.addView(buttonLine(buttonNames, buttonTags));
			buttonNames = new String[] {"8", "7", "6", "M"};
			buttonTags = new String[] {"8", "7", "6", "0"};

			buttonll.addView(buttonLine(buttonNames, buttonTags));

			return;

		case ButtonLayoutType.SMALL_SPOT:
			buttonNames = new String[] {"X", "10", "9"};
			buttonTags = new String[] {"11", "10", "9"};

			buttonll.addView(buttonLine(buttonNames, buttonTags));
			buttonNames = new String[] {"8", "7", "M"};
			buttonTags = new String[] {"8", "7", "0"};

			buttonll.addView(buttonLine(buttonNames, buttonTags));

			return;

		case ButtonLayoutType.FIVE_ZONE:
			buttonNames = new String[] {"9", "7", "5"};
			buttonTags = new String[] {"9", "7", "5"};

			buttonll.addView(buttonLine(buttonNames, buttonTags));
			buttonNames = new String[] {"3", "1", "0"};
			buttonTags = new String[] {"3", "1", "0"};

			buttonll.addView(buttonLine(buttonNames, buttonTags));

			return;

		case ButtonLayoutType.FIELD:
			buttonNames = new String[] {"6", "5", "4"};
			buttonTags = new String[] {"6", "5", "4"};

			buttonll.addView(buttonLine(buttonNames, buttonTags));
			buttonNames = new String[] {"3", "2", "1", "M"};
			buttonTags = new String[] {"3", "2", "1", "0"};

			buttonll.addView(buttonLine(buttonNames, buttonTags));

			return;

		case ButtonLayoutType.COMPOUND_INDOOR_SPOT:
			buttonNames = new String[] {"10", "9", "8"};
			buttonTags = new String[] {"10", "9", "8"};

			buttonll.addView(buttonLine(buttonNames, buttonTags));
			buttonNames = new String[] {"7", "6", "M"};
			buttonTags = new String[] {"7", "6", "0"};

			buttonll.addView(buttonLine(buttonNames, buttonTags));

			return;

		default:
			return;
		}
	}

	private LinearLayout buttonLine(String[] buttonNames, String[] buttonTags) {
		if (buttonNames.length != buttonTags.length) {
			Log.i(TAG,"buttonLine: arrays not equal size");
			return null;
		}
		LinearLayout ll = new LinearLayout(context);

		for (int i = 0; i < buttonNames.length; i++) {
			final Button button = new Button(context);
			View.OnClickListener go = new OnClickListener() {
				@Override
				public void onClick(View v) {
					addPoints(v);
				}
			};
			button.setOnClickListener(go);
			LinearLayout.LayoutParams btParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,1);
			button.setGravity(Gravity.CENTER);
			button.setText(buttonNames[i]);
			button.setTag(buttonTags[i]);
			button.setLayoutParams(btParams);
			ll.addView(button);
		}

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
		ll.setLayoutParams(lp);

		return ll;
	}

	//Actions------------------------------------------------------------------------------------------
	public void addPoints(View view) {
		//Read the tag associated with each button, each one should be a number
		int buttonTag;
		try {
			buttonTag = Integer.parseInt(view.getTag().toString());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		//Adds data to scoreCard and textViews
		scoreCard.addEntry(buttonTag, textViews);

		//Adds a new score line dynamically when necessary
		if (scoreCard.getPointer() % (numArrows+2) == 0) {
			Log.i(TAG, "Create new ScoreLine");
			scrollViewll.addView(createScoreLine(numArrows));
		}

		scrollToPoints();
	}

	
	public void backSpace(View view) {
		try {
			if (scoreCard.getPointer() > 0) {
				if (scoreCard.getPointer() % (numArrows+2) == 0) {
					if (scoreCard.isLastDistanceChange()) {
						scrollViewll.removeViewAt(scoreCard.getPointer()/(numArrows+2)+scoreCard.getNumDistanceChanges());
						scoreCard.removeLastDistanceChange();
						distanceLines.remove(distanceLines.size()-1);
						return;
					}
					scrollViewll.removeViewAt(scoreCard.getPointer()/(numArrows+2)+1 + scoreCard.getNumDistanceChanges());
					scoreCard.removeLastRow();
					removeRowTextViews(textViews, numArrows);
				}
				scoreCard.undo(textViews);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "back(): " + e.getMessage());
		}
		scrollToPoints();
	}


	public void changeDistance() {
		if (!scoreCard.changeDistance()) {
			cT("Must be at the end of a scoring end to change distances");
		} else {
			DistanceLine distanceLine = new DistanceLine(context, false);
			int pointer = scoreCard.getPointer();
			int numDistChg = scoreCard.getNumDistanceChanges();

			scrollViewll.addView(distanceLine.getLine(),pointer/(numArrows+2)+numDistChg);
			distanceLines.add(distanceLine);
		}
	}

	public void clearAll() {
		scrollViewll.removeAllViews();
		textViews = new ArrayList<TextView> ();

		distanceLines = new ArrayList<DistanceLine> ();
		DistanceLine distanceLine = new DistanceLine(context, true);
		scrollViewll.addView(distanceLine.getLine());
		distanceLines.add(distanceLine);
		scrollViewll.addView(createScoreLine(numArrows));
		scoreCard = new DynamicScoreCard(numArrows);

		scrollToPoints();
	}

	
	//Dynamic Layouts------------------------------------------------------------------------
	private void removeRowTextViews(ArrayList<TextView> textViews, int numArrows) {
		for (int i = 0; i < (numArrows+2); i++) {
			textViews.remove(textViews.size()-1);
		}
	}

	private LinearLayout createScoreLine(int numArrows) {
		final LinearLayout scoreLL = new LinearLayout(context);

		for (int i = 0; i < numArrows + 2; i++) {
			final TextView textView = new TextView(context);
			textView.setTextColor(0xFF000000);
			textView.setGravity(Gravity.CENTER);

			LinearLayout.LayoutParams tvParams;
			if (i == numArrows) {
				tvParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 40);
			} else if (i == numArrows + 1) {
				tvParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 57);
			} else {
				tvParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 33);
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
//			if (i==1) {
//				int pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,10, getResources().getDisplayMetrics());
//				cT(pixels+"");
//			}
			textView.setTextSize(getResources().getDimension(R.dimen.text_size));

			textView.setLayoutParams(tvParams);

			scoreLL.addView(textView);
			textViews.add(textView);
		}
		return scoreLL;
	}

	private void scrollToPoints() {
		ScrollView scrollview1 = (ScrollView) view.findViewById(R.id.scrollView1);
		scrollview1.fullScroll(View.FOCUS_DOWN);
	}


	//Saving actions----------------------------------------------------------------------
	public boolean save() {
		// Saves totals to score file for score sheet
		String[] fileNames = new String[6];
		fileNames[0] = archerName + "_round_file";
		fileNames[1] = archerName + "_distance_file";
		fileNames[2] = archerName + "_face_file";
		fileNames[3] = archerName + "_score_file";
		fileNames[4] = archerName + "_date_file";
		fileNames[5] = archerName + "_time_file";

		int dataCheck = distanceLines.get(0).roundEditText.getText().length();
		dataCheck *= distanceLines.get(0).distanceEditText.getText().length();
		dataCheck *= distanceLines.get(0).faceEditText.getText().length();
		if (dataCheck == 0) {
			return false;
		}
		
		int itemNumber = readListNumber();
		try {
			Log.d(TAG, "save: distanceLines size = " + distanceLines.size());

			String round = distanceLines.get(0).roundEditText.getText()+"";
			String[] save = new String[5];
			saveStringCreation(save);

			FileOutputStream fos;
			for (int i = 0; i < fileNames.length; i++) {
				fos = context.openFileOutput(fileNames[i], Context.MODE_APPEND);
				if (i == 0) {
					fos.write((round + delimiter).getBytes());
				} else {
					fos.write((save[i-1] + delimiter).getBytes());
				}
				fos.close();
			}

			scoresSave(archerName + "_dynamic_" + itemNumber);

			saveListNumber((itemNumber+1) + "");
			cT("Saved");
		} catch (FileNotFoundException e) {
			Log.e(TAG, "save: FileNotFoundException" + e.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
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
				String distance = distanceLines.get(i).distanceEditText.getText().toString();
				if (!distance.equals("")) {
					distances.append(distance + "m");
				}
				
				if (i != increment-1)
					distances.append("\n");
				
				String face = distanceLines.get(i).faceEditText.getText().toString();
				if (!face.equals("")) {
					faces.append(face + "cm");
				}
				if (i != increment-1) {
					faces.append("\n");
				}

				scores.append((scoreCard.getBlockScore(i+1)-scoreCard.getBlockScore(i)) + "/" + 
						(scoreCard.getBlockShots(i+1)-scoreCard.getBlockShots(i)));

				if (increment != 1) {
					scores.append("\n");
				}

			}
			//Appends total score and shots
			if (increment > 1) {
				int totalNumberOfShots = scoreCard.getBlockShots(increment);
				Log.i(TAG, "saveStringCreation: dynamicScoreCard.getBlockShots(increment): " + scoreCard.getBlockShots(increment));
				//Assumes this is set in the code above as it is not set in general operation
				scores.append(scoreCard.getBlockScore(increment) + "/" + totalNumberOfShots);
				Log.i(TAG, "saveStringCreation: dynamicScoreCard.getBlockScore(increment): " + scoreCard.getBlockScore(increment));
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
	public void scoresSave(String fileName) {
		// Saves all data on page such as all data in each textView
		if (fileName == null) {
			fileName = archerName + "_dynamic_temp_file";
		}
		try {
			FileOutputStream fos;
			fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);

			Log.i(TAG, "distanceLines.size() = " + distanceLines.size());
			fos.write((distanceLines.size()+delimiter).getBytes());
			fos.write((distanceLines.get(0).roundEditText.getText()+delimiter).getBytes());
			for (int i = 0; i < distanceLines.size(); i++) {
				fos.write((distanceLines.get(i).distanceEditText.getText()+delimiter).getBytes());
				fos.write((distanceLines.get(i).faceEditText.getText()+delimiter).getBytes());
			}

			//Save main ScoreCard
			fos.write((numArrows+delimiter).getBytes());

			scoreCard.saveDistanceChanges(fos);

			int size = textViews.size();
			if (scoreCard.getPointer() % (numArrows+2) == 0) {
				size -= numArrows+2;
//				if (size == (numArrows+2)) {
//					--size;
//				}
			}
			fos.write((size + delimiter).getBytes());

			for (int i = 0; i < size; i++) {
				fos.write((textViews.get(i).getText() + delimiter).getBytes());
			}

			if (fileName.equals(archerName + "_dynamic_temp_file")) {
				scoreCard.saveBlockScores(fos);
			}

			fos.close();

		} catch (FileNotFoundException e) {
			Log.e(TAG, "scoresSave: FileNotFoundException" + e.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Reads temporary ScoreCard for this name from file
	private void readData() {
		/**
		 * With reading data, you need to read all the data that has been saved
		 * Draw the points on the TargetView
		 * Write the score to the ScoreSheet
		 */

		String tempFile = archerName + "_dynamic_temp_file";

		// Reads and writes all textView data from previous close
		byte[] buffer = new byte[1024];
		StringBuffer tempFileContent = new StringBuffer("");
		String tempFileData = "";
		String[] temp;

		FileInputStream fis;
		File file = new File(context.getFilesDir(), tempFile);
		if (file.exists()) {
			try {
				fis = context.openFileInput(tempFile);
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
		Log.i(TAG, "enterData: temp.length = " + temp.length);
		if (temp.length > 5) {
			//Removal in case the numArrows is different
			scrollViewll.removeViewAt(1);
			removeRowTextViews(textViews, numArrows);
			
			int counter = 0;
			int distanceLength = Integer.parseInt(temp[counter]);
			Log.i(TAG, "enterData: distanceLength = " + temp[counter]);

			for (int i = 0; i < distanceLength-1; i++) {
				DistanceLine distanceLine = new DistanceLine(context, false);
				scrollViewll.addView(distanceLine.getLine());
				distanceLines.add(distanceLine);
			}

			distanceLines.get(0).roundEditText.setText(temp[++counter]);
			Log.i(TAG, "enterData: roundEditText = " + temp[counter]);
			//Account for the round name and the distance and face fields
			for (int i = 0; i < distanceLength; i++) {
				distanceLines.get(i).distanceEditText.setText(temp[++counter]);
				Log.i(TAG, "enterData: distanceEditText = " + temp[counter]);
				distanceLines.get(i).faceEditText.setText(temp[++counter]);
				Log.i(TAG, "enterData: faceEditText = " + temp[counter]);
			}


			try {
				numArrows = Integer.parseInt(temp[++counter]);
				Log.i(TAG, "enterData: numArrows = " + temp[counter]);
				scoreCard = new DynamicScoreCard(numArrows);
				scrollViewll.addView(createScoreLine(numArrows), 1);

				int[] data = new int[2];
				scoreCard.inputScoreCard(temp, counter, data);
				counter = data[1]; // 0 is scoreCardSize
				Log.i(TAG, "enterData: data[0] = " + data[0]);
				Log.i(TAG, "enterData: data[1] = " + data[1]);

				// each DistanceChange is not including all the current distance changes
				// make sure to offset by the first one, then each subsequent recorded one.

				int distanceChanges = scoreCard.getNumDistanceChanges();
				int increment = 0;
				int lastDistanceChange = 0;
				if (distanceChanges > 0) {
					lastDistanceChange = scoreCard.getDistanceChangeId(increment++);
				}

				//TODO: FIND OUT WHY!!!
				final int lines;
				if (data[0] == numArrows + 1) {
					lines = (data[0]+1)/(numArrows+2);
				} else {
					lines = data[0]/(numArrows+2);
				}

				//Adds lines at each necessary line, after the initial one. Offset by the 1. After each of the appropriate distance lines.
				for (int i = 0; i < lines; i++) {
					if (i >= lastDistanceChange-1) {
						if (increment < distanceChanges) {
							lastDistanceChange = scoreCard.getDistanceChangeId(increment++);
						} else {
							//Effectively at infinity
							lastDistanceChange = scoreCard.getPointer();
							++increment;
						}
					}
					//Extra 1 for initial call of createScoreLine() in createLayout
					scrollViewll.addView(createScoreLine(numArrows), i + increment+1); 
				}
				//So that there is no extra line when the current one has not been completed
				while (textViews.size() - numArrows - 2 > scoreCard.getPointer()) {
					scrollViewll.removeViewAt(scrollViewll.getChildCount()-1);
					removeRowTextViews(textViews, numArrows);
				}

				for (int i = 0; i < data[0]; i++) {
					textViews.get(i).setText(temp[++counter]);
				}

				scoreCard.inputBlockScores(temp, counter, data);

			} catch (Exception e) {
				Log.e(TAG, "enterData: " + e.toString());
				e.printStackTrace();
			}

		}
	}


	//Saves the new number of items in the list
	private void saveListNumber(String listLength) {
		String listNumberFile = archerName + "_list_number_file";

		FileOutputStream fos;
		try {
			fos = context.openFileOutput(listNumberFile, Context.MODE_PRIVATE);
			fos.write((listLength + delimiter).getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "saveListNumber: FileNotFoundException" + e.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Finds how many items in the list
	private int readListNumber() {
		String listNumberFile = archerName + "_list_number_file";

		byte[] buffer = new byte[1024];
		StringBuffer listNumberFileContent = new StringBuffer("");

		FileInputStream fis;
		try {
			fis = context.openFileInput(listNumberFile);
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

	
	//Interface Actions ----------------------------------------------------------------------
	//create Toast
	private void cT(String s) { 
		Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
	}
	
	public int getPointer() {
		return scoreCard.getPointer();
	}

}
