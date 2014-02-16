package benl.student.archeryscorer;

import java.io.FileOutputStream;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;
import benl.student.archeryscorer.ArcheryScoreSheet.DummySectionFragment;
import benl.student.archeryscorer.R.id;

public class ScoreCard {
	private int[] scoreCard; //Not done, to be actual scores
	private boolean[] misses; //Not done, to be true false on misses
	private boolean[] Xs; //Not done, to be true false on X's
	private int pointer; //Not done, references which arrow its up to
	private int block; //Represents which block its up to
	private int[] blockScores; //Total score up to that block
	private int[] oldPointer; //Total number of arrows shot for each block
	
	private static final int NUM_BLOCKS = 4;
	private static final int NUM_TEXTVIEWS = 192;
	private static final int NUM_TV_IN_BLOCK = 48; //TV is TextViews
	private static final int NUM_TV_IN_ROW = 8;
	private static final int END_FIRST_ROW = 7;
	private static final int END_SEC_ROW = 15;

    private static final String delimiter = ";";
    private Context context;
	
	public ScoreCard(Context context) {
		this.scoreCard = new int[NUM_TEXTVIEWS];
		this.misses = new boolean[NUM_TEXTVIEWS];
		this.Xs = new boolean[NUM_TEXTVIEWS];
		this.pointer = 0;
		this.block = 0;
		this.blockScores = new int[NUM_BLOCKS];
		this.oldPointer = new int[NUM_BLOCKS];
		this.context = context;
	}
	
	//Adds Entry to ScoreCard, resolves all pointer issues.
	public void addEntry(int buttonTag, DummySectionFragment fragment) {
		try {
			if (buttonTag == 11) {
				scoreCard[pointer] = 10;
				Xs[pointer] = true;
				findTextView("textView" + pointer, fragment).setText("X");
			} else {
				scoreCard[pointer] = buttonTag;
				if (buttonTag == 0) {
					misses[pointer] = true;
					findTextView("textView" + pointer, fragment).setText("M");
				} else {
					findTextView("textView" + pointer, fragment).setText(buttonTag + "");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "Error: report to developer: \nPointer: "+pointer+"\nError message: "+e.getMessage(), Toast.LENGTH_LONG).show();
			return;
		}
		incrementPointer();
		
		
		sumPoints(fragment);
		
		if (pointer % NUM_TV_IN_BLOCK == 0) {
			nextBlock();
		}
	}
	
	//Finds the row totals
	private void sumPoints(DummySectionFragment fragment) {
		if (pointer > 0) {
  			//Defining variables to represent the last entry and the total
  			LastEntry lastEntry = new LastEntry(pointer);
  			int total = 0;
  			
  			//Finds the total for the row
  			for (int i = 0; i <= lastEntry.getShotIndex(); i++) {
  				total += scoreCard[lastEntry.getLastEntry()-i];
  			}
  			
  			//Adds to the end total
  			scoreCard[lastEntry.getRowTotalIndex()] = total;
  			findTextView("textView" + lastEntry.getRowTotalIndex(), fragment).setText(total + "");
  			
  			//Finds the running total
  			if (lastEntry.getEndOfRow() > END_SEC_ROW) {
  				if (block > 0 && lastEntry.getLastEntry() % NUM_TV_IN_BLOCK < END_FIRST_ROW) {
  					scoreCard[lastEntry.getEndOfRow()] = total + blockScores[block-1];
  				} else {
  					scoreCard[lastEntry.getEndOfRow()] = total + scoreCard[lastEntry.getPreviousEndOfRow()];
  				}
  				findTextView("textView" + lastEntry.getEndOfRow(), fragment).setText(scoreCard[lastEntry.getEndOfRow()] + "");
  			} else if (lastEntry.getEndOfRow() > 7) {
  				scoreCard[lastEntry.getEndOfRow()] = total + scoreCard[lastEntry.getPreviousRowTotalIndex()];
  				findTextView("textView" + lastEntry.getEndOfRow(), fragment).setText(scoreCard[lastEntry.getEndOfRow()] + "");
  			}
  		}
	}
	
	private void incrementPointer() {
		// Adjusts the pointer to the next view
		int remainder = pointer % NUM_TV_IN_ROW;
		if (remainder > 4) {
			pointer += NUM_TV_IN_ROW-remainder;
		} else {
			++pointer;
		}
	}
	
	public void undo(DummySectionFragment fragment) {
		// Erases 1 entry and recalculates values
  		if (pointer > 0) {
  			LastEntry lastEntry;
  			if (block > 0) {
  				lastEntry = new LastEntry(pointer, oldPointer[block-1]);
  			} else {
  				lastEntry = new LastEntry(pointer);
  			}
  			
  			
  			//Clears row summation on deleting all row entries
  			if (lastEntry.getShotIndex() == 0) {
  				findTextView("textView" + lastEntry.getRowTotalIndex(), fragment).setText("");
  				
  				if (!lastEntry.isFirstRow()) findTextView("textView" + lastEntry.getEndOfRow(), fragment).setText("");
  			}
  			
  			// Adjusts the pointer to the correct location
  			if (pointer % NUM_TV_IN_BLOCK == 0 && block > 0) {
  				--block;
  			}
  			
  			pointer = lastEntry.getLastEntry();
  			findTextView("textView" + pointer, fragment).setText("");
  			Xs[pointer] = false;
  			misses[pointer] = false; // negates for X or miss;
  			
  			// Recalculate all totals, avoids backblock bug
  			if (pointer % NUM_TV_IN_ROW != 0) sumPoints(fragment);
  		}
	}
	
	
	
	public int getPointer() {
		return pointer;
	}
	
	public void setPointer(int pointer) {
		this.pointer = pointer;
	}
	
	public int getScoreCardValue(int pointer) {
		return scoreCard[pointer];
	}
	
	public void setScoreCardValue(int pointer, int value) {
		scoreCard[pointer] = value;
	}
	
	
	public boolean isX(int pointer) {
		return Xs[pointer];
	}
	
	public boolean isMiss(int pointer) {
		return misses[pointer];
	}
	
	//TODO: need to make this all in one
	public void nextBlock() {
		if (block < 3) {
			LastEntry lastEntry = new LastEntry(pointer);
			//Find score for leaving block
			if (lastEntry.getLastEntry() < END_FIRST_ROW) {
				blockScores[block] = scoreCard[lastEntry.getRowTotalIndex()];
			} else {
				blockScores[block] = scoreCard[lastEntry.getEndOfRow()];
			}
			
			oldPointer[block] = pointer;
			
			++block;
			pointer = block*NUM_TV_IN_BLOCK;
		}
	}
	
	
	public int getBlockScore(int block) {
		return blockScores[block];
	}
	
	public int getBlockNumber() {
		return block;
	}
	
	
	public int getOldPointer(int block) {
		return oldPointer[block];
	}

	public int getScoreForBlock(int block) {
		if (block == this.block) {
			LastEntry lastEntry = new LastEntry(pointer);
			//Find score for final block
			if (lastEntry.getLastEntry() < 6) {
				blockScores[block] = scoreCard[lastEntry.getRowTotalIndex()];
			} else {
				blockScores[block] = scoreCard[lastEntry.getEndOfRow()];
			}
		}
		//Finds a block total for a difference between subsequent blocks
		if (block > 0) {
			return blockScores[block]-blockScores[block-1];
		}
		//Finds a block total for the first block
		return blockScores[block];
	}
	
	public int getShotsForBlock(int block) {
		LastEntry lastEntry;
		if (block == this.block) {
			lastEntry = new LastEntry(pointer);
		} else {
			lastEntry = new LastEntry(oldPointer[block]);
		}
		
		int offsetForBlocks = (int)(lastEntry.getLastEntry() / NUM_TV_IN_BLOCK) * NUM_TV_IN_BLOCK;
		int blockLastEntry = lastEntry.getLastEntry() - offsetForBlocks;
		int offsetForRunningTotal = (blockLastEntry / NUM_TV_IN_ROW) * 2;
		
		return blockLastEntry - offsetForRunningTotal + 1;
	}


	public void saveScoreCard(FileOutputStream fos) {
		try {
			//Save heading details
			fos.write((pointer + delimiter).getBytes());
			fos.write((block + delimiter).getBytes());
			for (int i = 0; i < blockScores.length; i++) {
				fos.write((blockScores[i] + delimiter).getBytes());
			}
			for (int i = 0; i < oldPointer.length; i++) {
				fos.write((oldPointer[i] + delimiter).getBytes());
			}
			
			//Save main ScoreCard
			for (int i = 0; i < scoreCard.length; i++) {
				if (Xs[i]) {
					fos.write(("X" + delimiter).getBytes());
				} else if (misses[i]) {
					fos.write(("M" + delimiter).getBytes());
				} else {
					fos.write((scoreCard[i] + delimiter).getBytes());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void inputScoreCard(String[] temp, int counter) {
		if (temp.length >= 202) {
//			int counter = 0;
			pointer = Integer.parseInt(temp[counter++]);
			block = Integer.parseInt(temp[counter++]);
			for (int i = 0; i < blockScores.length; i++) {
				blockScores[i] = Integer.parseInt(temp[counter++]);
			}
			for (int i = 0; i < oldPointer.length; i++) {
				oldPointer[i] = Integer.parseInt(temp[counter++]);
			}

			LastEntry lastEntry;
			
			// First loop loops through each block
			for (int i = 0; i <= block; i++) {
			
				//Finds the last entry for that block, if its not used, the method exits
				if (i < block) {
					lastEntry = new LastEntry(oldPointer[i]);
				} else if (i == block) {
					lastEntry = new LastEntry(pointer);
				} else {
					return;
				}
				
				// Second loop loops through each entry
				for (int j = i*NUM_TV_IN_BLOCK; j <= lastEntry.getEndOfRow(); j++) {
					if (j <= lastEntry.getLastEntry()) {
						
						//Checks and inserts correct data into textViews
						if (temp[j+counter].equals("M")) {
							misses[j] = true;
						} else if (temp[j+counter].equals("X")) {
							Xs[j] = true;
							scoreCard[j] = 10;
						} else {
							try {
								scoreCard[j] = Integer.parseInt(temp[j+counter]);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					// Checks and enters totals for the final end totals
					} else if (j == lastEntry.getRowTotalIndex()) {
						scoreCard[j] = Integer.parseInt(temp[j+counter]);
					} else if (j == lastEntry.getEndOfRow() && j > END_FIRST_ROW) {
						scoreCard[j] = Integer.parseInt(temp[j+counter]);
					}
				}
			}
		}
	}
	

	//View finding simplifiers
  	private TextView findTextView(String name, DummySectionFragment fragment) {
  		// Finds textView address
  		try {
  			Class<id> aClass = R.id.class;
  			int aid = aClass.getField(name).getInt(aClass);
  			return (TextView) fragment.getView().findViewById(aid);
  		} catch(Exception e) {		
  			e.printStackTrace();
  			return null;
  		}
  	}
}
