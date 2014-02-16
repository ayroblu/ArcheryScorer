package benl.student.archeryscorer;

import java.io.FileInputStream;

import benl.student.archeryscorer.R;
import benl.student.archeryscorer.R.id;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ScoreSheetViewer extends Activity {

	ScoreCard scoreCard = new ScoreCard(this);
	//	private static final int NUM_BLOCKS = 4;
	//	private static final int NUM_TEXTVIEWS = 192;
	private static final int NUM_TV_IN_BLOCK = 48; //TV is TextViews
	//	private static final int NUM_TV_IN_ROW = 8;
	private static final int END_FIRST_ROW = 7;
	//	private static final int END_SEC_ROW = 15;

    private static final String delimiter = ";";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_score_sheet_viewer);
		setupActionBar();
		try {
			Intent intent = getIntent();
			int position = intent.getIntExtra(ListScores.EXTRA_NUM, 0);
			String name = intent.getStringExtra(ListScores.EXTRA_NAME);

			tempRead(name,position);
		} catch (Exception e) {
			// TODO: handle exception
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

	// Reads temporary ScoreCard for this name from file
	private void tempRead(String name, int index) {
		String tempFile = name + "_" + index;

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
			e.printStackTrace();
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
//					String[] tempScoreCard = new String[202];
//					tempScoreCard = Arrays.copyOfRange(temp, counter, counter + 202);
//					
//					scoreCard.inputScoreCard(tempScoreCard);
					scoreCard.inputScoreCard(temp,counter);

					readDataFromScoreCard(index);

				} catch (Exception e) {
					cT("FAILED READ");
				}

			}
		}
	}

	// Turns the data in the ScoreCard into information on the page
	private void readDataFromScoreCard(int index) {
		if (scoreCard.getPointer() > 0) {
			LastEntry lastEntry;

			// First loop loops through each block
			for (int i = 0; i <= scoreCard.getBlockNumber(); i++) {

				//Finds the last entry for that block, if its not used, the method exits
				if (i < scoreCard.getBlockNumber()) {
					lastEntry = new LastEntry(scoreCard.getOldPointer(i));
				} else if (i == scoreCard.getBlockNumber()) {
					lastEntry = new LastEntry(scoreCard.getPointer());
				} else {
					return;
				}

				// Second loop loops through each entry
				for (int j = i*NUM_TV_IN_BLOCK; j <= lastEntry.getEndOfRow(); j++) {
					if (j <= lastEntry.getLastEntry()) {

						//Checks and inserts correct data into textViews
						if (scoreCard.isMiss(j)) {
							findTextView("textView" + j, index).setText("M");
						} else if (scoreCard.isX(j)) {
							findTextView("textView" + j, index).setText("X");
						} else if (j != END_FIRST_ROW) {
							findTextView("textView" + j, index).setText(scoreCard.getScoreCardValue(j) + "");
						}

						// Checks and enters totals for the final end totals
					} else if (j == lastEntry.getRowTotalIndex()) {
						findTextView("textView" + j, index).setText(scoreCard.getScoreCardValue(j) + "");
					} else if (j == lastEntry.getEndOfRow() && j > END_FIRST_ROW) {
						findTextView("textView" + j, index).setText(scoreCard.getScoreCardValue(j) + "");
					}
				}
			}
		}
	}


	//View finding simplifiers
	private TextView findTextView(String name, int index) {
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

	private EditText findEditText(String name, int index) {
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
	

	//create Toast
	public void cT(String s) { 
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}

}
