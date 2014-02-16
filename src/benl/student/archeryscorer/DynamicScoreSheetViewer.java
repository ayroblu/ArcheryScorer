package benl.student.archeryscorer;

import java.io.FileInputStream;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class DynamicScoreSheetViewer extends FragmentActivity {
	
	private static final String delimiter = ";";
	private static final String TAG = "DynamicScoreSheetViewer";
	
	private DynamicScoreCard dynamicScoreCard;
	private ScoreSheetSectionFragment scoreSheet;

	ArrayList<DistanceLine> distanceLines = new ArrayList<DistanceLine>();
	int numArrows = 6;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		
		scoreSheet = new ScoreSheetSectionFragment();
		getSupportFragmentManager().beginTransaction()
        .replace(android.R.id.content, scoreSheet)
        .commit();

		dynamicScoreCard = new DynamicScoreCard();

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
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
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
	

	public static class ScoreSheetSectionFragment extends Fragment {
		LinearLayout scrollViewll;
		ScrollView scrollView;
		ArrayList<TextView> textViews = new ArrayList<TextView>();

		public ScoreSheetSectionFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			
			return scrollView;
		}
	}

	
	private void createScoreSheetLayout() {
		scoreSheet.scrollView = new ScrollView(this);
		scoreSheet.scrollViewll = new LinearLayout(this);
		scoreSheet.scrollViewll.setOrientation(LinearLayout.VERTICAL);
		scoreSheet.scrollView.addView(scoreSheet.scrollViewll);
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
		
		String tempFile = name + "_dynamic_" + index;

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
			Log.i(TAG, "readData: entering data");
			enterData(temp);
			
		}
	}
	
	private void enterData(String[] temp) {
		//Turns input data into information for ScoreCard
		Log.i(TAG, "enterData: temp.length = " + temp.length);
		if (temp.length > 5) {
			int distanceLength = Integer.parseInt(temp[0]);
			Log.i(TAG, "enterData: distanceLength = " + temp[0]);
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
				dynamicScoreCard.setNumArrows(numArrows);
				
				int[] data = new int[2];
				Log.i(TAG, "enterData: inputScoreCard called");
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
				
				final int lines;
				if (data[0] == numArrows + 1) {
					lines = (data[0]+1)/(numArrows+2);
				} else {
					lines = data[0]/(numArrows+2);
				}
				
				for (int i = 0; i < lines; i++) {
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
				
			} catch (Exception e) {
				Log.e(TAG, "enterData: " + e.toString());
				e.printStackTrace();
			}

		}
	}

}
