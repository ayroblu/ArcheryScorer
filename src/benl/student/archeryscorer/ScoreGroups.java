package benl.student.archeryscorer;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ScoreGroups {
	private Context context;
	ScoreGroups(Context context) {
		this.context = context;
	}
	
	public class Score {
		private int score;
		private boolean X;
		private boolean miss;
		private boolean displayed;
		
		public Score() {
			this.score = 0;
			this.X = false;
			this.miss = false;
			this.displayed = false;
		}
		
		public Score(int score, boolean X, boolean miss) {
			this.score = score;
			this.X = X;
			this.miss = miss;
			this.displayed = true;
		}
		
		public boolean isDisplayed() {
			return displayed;
		}
		
		public String getScore() {
			if (displayed) {
				if (X) {
					return "X";
				} else if (miss) {
					return "miss";
				} else {
					return score+"";
				}
			} else {
				return "";
			}
		}
		
		public int getScoreN() {
			if (displayed) {
				return score;
			} else {
				return 0;
			}
		}
		
		public void setScore(int score, boolean X, boolean miss) {
			this.score = score;
			this.X = X;
			this.miss = miss;
			this.displayed = true;
		}
		
		public void hide() {
			this.score = 0;
			this.X = false;
			this.miss = false;
			this.displayed = false;
		}
	}
	
	public class TVScore {
		private TextView tv;
		private Score score;
		
		public TVScore(TextView tv) {
			this.tv = tv;
			this.score = new Score();
		}
		
		public int getScore() {
			return score.getScoreN();
		}
		
		public void setScore(int score, boolean X, boolean miss) {
			this.score.setScore(score, X, miss);
			if (X) {
				this.tv.setText("X");
			} else if (miss) {
				this.tv.setText("M");
			} else {
				this.tv.setText(score+"");
			}
		}
	}
	
	
	public LinearLayout createScoreLine(int numArrows, boolean firstLine) {
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

			if (firstLine && i == numArrows + 1) {
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
			textView.setTextSize(context.getResources().getDimension(R.dimen.text_size));

			textView.setLayoutParams(tvParams);

			scoreLL.addView(textView);
		}
		return scoreLL;
	}
	
}
