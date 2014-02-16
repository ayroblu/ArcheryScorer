package benl.student.archeryscorer;

import java.io.FileOutputStream;
import java.util.ArrayList;

import android.util.Log;
import android.widget.TextView;

public class DynamicScoreCard {
	private int pointer; //References which arrow its up to
	private int numArrows = 6;
	
	private ArrayList<Score> scoreCard = new ArrayList<Score> ();
	private ArrayList<DistanceChange> distanceChanges = new ArrayList<DistanceChange> ();
	private ArrayList<BlockScore> blockScores = new ArrayList<BlockScore> ();
	private ArrayList<BlockScore> blockShots = new ArrayList<BlockScore> ();
	
//	private TouchScreenInterface parent;
    private static final String delimiter = ";";
	
    

	public DynamicScoreCard(int numArrows) {
		this.pointer = 0;
//		this.parent = parent;
		this.numArrows = numArrows;
		
		blockScores.add(new BlockScore(0));
		blockScores.add(new BlockScore(0));

		blockShots.add(new BlockScore(0));
		blockShots.add(new BlockScore(0));
		
		addNewRow();
	}
	
	public DynamicScoreCard() {
		this.pointer = 0;
//		this.parent = null;
		
		blockScores.add(new BlockScore(0));
		blockScores.add(new BlockScore(0));

		blockShots.add(new BlockScore(0));
		blockShots.add(new BlockScore(0));
	}
	
	
	private class Score {
		private int value; //Actual scores
		private boolean miss; //True false on misses
		private boolean X; //True false on X's
		
		Score(int value, boolean X, boolean miss) {
			this.value = value;
			this.miss = miss;
			this.X = X;
		}
		
		public void setScore(int value, boolean X, boolean miss) {
			this.value = value;
			this.miss = miss;
			this.X = X;
		}
	}
	
	private class DistanceChange {
		private int row;
		
		DistanceChange(int row) {
			this.row = row;
		}
	}
	
	private class BlockScore {
		private int score;
		
		BlockScore(int score) {
			this.score = score;
		}
	}
	
	
	
	//Adds Entry to ScoreCard, resolves all pointer issues.
	public void addEntry(int buttonTag, ArrayList<TextView> textViews) {
		if (buttonTag == 11) {
			scoreCard.get(pointer).setScore(10,true,false);
			textViews.get(pointer).setText("X");
		} else {
			if (buttonTag == 0) {
				scoreCard.get(pointer).setScore(buttonTag,false,true);
				textViews.get(pointer).setText("M");
			} else {
				scoreCard.get(pointer).setScore(buttonTag,false,false);
				textViews.get(pointer).setText(buttonTag + "");
			}
		}
		++blockShots.get(blockShots.size()-1).score;
		
		incrementPointer();
		
		sumPoints(textViews);
		
	}
	
	//Finds the row totals
	private void sumPoints(ArrayList<TextView> textViews) {
		if (pointer > 0) {
  			//Defining variables to represent the last entry and the total
  			TouchLastEntry lastEntry = new TouchLastEntry(pointer, numArrows);
  			int total = 0;
  			
  			//Finds the total for the row
  			for (int i = 0; i <= lastEntry.getShotIndex(); i++) {
  				total += scoreCard.get(lastEntry.getLastEntry()-i).value;
  			}
  			
  			//Adds to the end total
  			scoreCard.get(lastEntry.getRowTotalIndex()).value = total;
//  			findTextView("textView" + lastEntry.getRowTotalIndex(), fragment).setText(total + "");
			textViews.get(lastEntry.getRowTotalIndex()).setText(total + "");
  			
			
			Log.d("DynamicScoreCard", "lastEntry.getEndOfRow() = " + lastEntry.getEndOfRow());
			Log.d("DynamicScoreCard", "pointer = " + pointer);
			Log.d("DynamicScoreCard", "numArrows = " + numArrows);
			Log.d("DynamicScoreCard", "scoreCard.size() = " + scoreCard.size());
			
			try {
	  			//Finds the running total
	  			if (lastEntry.getEndOfRow() > (numArrows+2)*2-1) {
	  				scoreCard.get(lastEntry.getEndOfRow()).value = total + scoreCard.get(lastEntry.getPreviousEndOfRow()).value;
	  				textViews.get(lastEntry.getEndOfRow()).setText(scoreCard.get(lastEntry.getEndOfRow()).value + "");
	  			} else if (lastEntry.getEndOfRow() > numArrows+1) {
	  				scoreCard.get(lastEntry.getEndOfRow()).value = total + scoreCard.get(lastEntry.getPreviousRowTotalIndex()).value;
	  				textViews.get(lastEntry.getEndOfRow()).setText(scoreCard.get(lastEntry.getEndOfRow()).value + "");
	  			} else {
	  				scoreCard.get(lastEntry.getEndOfRow()).value = total;
	  			}
	  			//Adds the score to blockScores to make for quick finding for saving
	  			blockScores.get(blockScores.size()-1).score = scoreCard.get(lastEntry.getEndOfRow()).value;
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("DynamicScoreCard", e.getMessage());
			}
  		}
	}
	
	private void incrementPointer() {
		// Adjusts the pointer to the next view
		int remainder = pointer % (numArrows+2);
		if (remainder > numArrows-2) {
			pointer += numArrows+2-remainder;
			addNewRow();
		} else {
			++pointer;
		}
	}
	
	public void undo(ArrayList<TextView> textViews) {
		// Erases 1 entry and recalculates values
  		if (pointer > 0) {
  			TouchLastEntry lastEntry = new TouchLastEntry(pointer, numArrows);
  			
  			
  			//Clears row summation on deleting all row entries
  			if (lastEntry.getShotIndex() == 0) {
  				textViews.get(lastEntry.getRowTotalIndex()).setText("");
  				
  				if (!lastEntry.isFirstRow()) {
  	  				textViews.get(lastEntry.getEndOfRow()).setText("");
  				}
  			}
  			
  			pointer = lastEntry.getLastEntry();
			textViews.get(pointer).setText("");
			scoreCard.get(pointer).value = 0;
  			
  			// Recalculate all totals, avoids backblock bug
			sumPoints(textViews);
  			

  			//Counts the number of arrows to allow saving to be easier
  			--blockShots.get(blockShots.size()-1).score;
  			
  		}
	}
	
	public boolean changeDistance() {
		if (pointer % (numArrows+2) == 0 && pointer > 0 && !isLastDistanceChange()) {
			distanceChanges.add(new DistanceChange(pointer / (numArrows+2)));
			
			blockScores.add(new BlockScore(blockScores.get(blockScores.size()-1).score));
			blockShots.add(new BlockScore(blockShots.get(blockShots.size()-1).score));
			return true;
		} else {
			return false;
		}
	}
	
	public int getNumDistanceChanges() {
		return distanceChanges.size();
	}
	
	public int getDistanceChangeId(int id) {
		return distanceChanges.get(id).row;
	}
	
	public boolean isLastDistanceChange() {
		if (distanceChanges.size() > 0 && distanceChanges.get(distanceChanges.size() - 1).row == pointer / (numArrows+2)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void removeLastDistanceChange() {
		if (distanceChanges.size() > 0) {
			distanceChanges.remove(distanceChanges.size() - 1);
			
			blockScores.remove(blockScores.size() -2);
			blockShots.remove(blockShots.size() -2);
		}
	}
	
 	public int getPointer() {
		return pointer;
	}
	
	private void addNewRow() {
		for (int i = 0; i < numArrows+2; i++) {
			scoreCard.add(new Score(0, false, false));
		}
	}
	
	public void removeLastRow() {
		for (int i = 0; i < numArrows+2; i++) {
			scoreCard.remove(scoreCard.size()-1);
		}
	}
	
	
	//Never used, Xs and Misses should be used in stats.
	public boolean isX(int pointer) {
		return scoreCard.get(pointer).X;
	}
	
	public boolean isMiss(int pointer) {
		return scoreCard.get(pointer).miss;
	}
	
	public int distanceChange(int pointer) {
		if (pointer < distanceChanges.size()) {
			return distanceChanges.get(pointer).row;
		}
		return 0;
	}
	
	public void setNumArrows(int numArrows) {
		this.numArrows = numArrows;
	}
	
	
	public int getBlockScore(int index) {
		return blockScores.get(index).score;
	}
	
	public int getBlockShots(int index) {
		return blockShots.get(index).score;
	}
	


	public void saveDistanceChanges(FileOutputStream fos) {
		try {
			//Save pointer
			fos.write((pointer + delimiter).getBytes());
			
			//Save Distance Changes
			fos.write((distanceChanges.size()+ delimiter).getBytes());
			for (DistanceChange distanceChange : distanceChanges) {
				fos.write((distanceChange.row+ delimiter).getBytes());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void saveBlockScores(FileOutputStream fos) {
		try {
			fos.write((blockScores.size()+delimiter).getBytes());
			for (int i = 0; i < blockScores.size(); i++) {
				fos.write((blockScores.get(i).score + delimiter).getBytes());
			}
			
			fos.write((blockShots.size()+delimiter).getBytes());
			for (int i = 0; i < blockShots.size(); i++) {
				fos.write((blockShots.get(i).score + delimiter).getBytes());
			}
			
		} catch (Exception e) {
			Log.i("DynamicScoreCard", "saveBlockScores: " + e.toString());
			e.printStackTrace();
		}
	}
	
	public void inputScoreCard(String[] temp, int counter, int[] data) {
		try {
			pointer = Integer.parseInt(temp[++counter]);
			Log.i("DynamicScoreCard", "inputScoreCard: pointer = " + pointer);

			int distChangesSize = Integer.parseInt(temp[++counter]);
			distanceChanges = new ArrayList<DistanceChange>();
			for (int i = 0; i < distChangesSize; i++) {
				distanceChanges.add(new DistanceChange(Integer.parseInt(temp[++counter])));
			}

			Log.i("DynamicScoreCard", "inputScoreCard: scoreCard current size:" + scoreCard.size());
			int scoreCardSize = Integer.parseInt(temp[++counter]);
			if (scoreCardSize == numArrows+2 && temp.length == counter+scoreCardSize) {
				--scoreCardSize;
			}
			Log.i("DynamicScoreCard", "inputScoreCard: scoreCard saved size:" + scoreCardSize);
			data[0] = scoreCardSize;
			data[1] = counter;
			Log.i("DynamicScoreCard", "inputScoreCard: counter: " + counter);
			
			while (scoreCard.size() % (numArrows+2) != 0) {
				scoreCard.add(new Score(0, false, false));
			}
			
			
			for (int i = 0; i < scoreCardSize; i++) {
				String score = temp[++counter];
				
				if (score.equals("X")) {
					scoreCard.add(i,new Score(10, true, false));
				} else if (score.equals("M")) {
					scoreCard.add(i,new Score(0, false, true));
				} else {
					if (!score.equals("")) {
						scoreCard.add(i,new Score(Integer.parseInt(score), false, false)); // Error
					} else {
						scoreCard.add(i, new Score(0, false, false));
					}
				}
			}
			Log.i("DynamicScoreCard", "inputScoreCard: scoreCard finish input size:" + scoreCard.size());
		} catch (Exception e) {
			Log.e("DynamicScoreCard", "inputScoreCard: " + e.toString());
			e.printStackTrace();
		}
	}

	public void inputBlockScores(String[] temp, int counter, int[] data) {
		blockScores = new ArrayList<BlockScore> ();
		blockShots = new ArrayList<BlockScore> ();
		int blockScoresSize = Integer.parseInt(temp[++counter]);
		for (int i = 0; i < blockScoresSize; i++) {
			blockScores.add(new BlockScore(Integer.parseInt(temp[++counter])));
		}
		int blockShotsSize = Integer.parseInt(temp[++counter]);
		for (int i = 0; i < blockShotsSize; i++) {
			blockShots.add(new BlockScore(Integer.parseInt(temp[++counter])));
		}
		
		data[1] = counter;
	}

}
