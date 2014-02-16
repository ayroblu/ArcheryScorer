package benl.student.archeryscorer;

public class TouchLastEntry {
	private int lastEntry;
	private int remainder;
	private int endOfRow;
	private int numArrows;
	
	public TouchLastEntry(int pointer, int numArrows) {
		this.numArrows = numArrows;
		
		this.remainder = pointer % (numArrows+ 2);
		if (this.remainder == 0) {
			this.lastEntry = pointer - 3;
		} else {
			this.lastEntry = pointer - 1;
		}
		this.remainder = this.lastEntry % (numArrows + 2);
		this.endOfRow = this.lastEntry + (numArrows + 1) - this.remainder;
		
	}
	
	/** Pointer location of the last entry on the score card */
	public int getLastEntry() {
		return lastEntry;
	}
	
	public int getShotIndex() {
		return remainder;
	}
	
	public int getEndOfRow() {
		return endOfRow;
	}
	
	public int getRowTotalIndex() {
		return endOfRow-1;
	}
	
	public int getPreviousEndOfRow() {
		return endOfRow-numArrows-2;
	}
	
	public int getPreviousRowTotalIndex() {
		return endOfRow-numArrows-3;
	}
	
	public boolean isFirstRow() {
		if (this.endOfRow < numArrows + 2) {
			return true;
		} else {
			return false;
		}
	}
}
