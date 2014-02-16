package benl.student.archeryscorer;

public class LastEntry {
	private int lastEntry;
	private int remainder;
	private int endOfRow;
	
	public LastEntry(int pointer) {
		this.remainder = pointer % 8;
		if (this.remainder == 0) {
			this.lastEntry = pointer - 3;
		} else {
			this.lastEntry = pointer - 1;
		}
		this.remainder = this.lastEntry % 8;
		this.endOfRow = this.lastEntry + 7 - this.remainder;
	}
	
	public LastEntry(int pointer, int oldPointer) {
		if (pointer % 48 == 0) {
			pointer = oldPointer;
		} 
		this.remainder = pointer % 8;
		if (this.remainder == 0) {
			this.lastEntry = pointer - 3;
		} else {
			this.lastEntry = pointer - 1;
		}
		this.remainder = this.lastEntry % 8;
		this.endOfRow = this.lastEntry + 7 - this.remainder;
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
		return endOfRow-8;
	}
	
	public int getPreviousRowTotalIndex() {
		return endOfRow-9;
	}
	
	public boolean isFirstRow() {
		if (this.endOfRow < 8) {
			return true;
		} else {
			return false;
		}
	}
}
