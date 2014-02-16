package benl.student.archeryscorer;

import android.content.Context;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

//This class handles and makes the views for each Distance
public class DistanceLine {
	public LinearLayout roundll;
	public EditText roundEditText;
	public EditText distanceEditText;
	public EditText faceEditText;
	
	private Context context;
	private static final String ROUND_HINT = "Round";
	private static final String DISTANCE_HINT = "m";
	private static final String FACE_HINT = "cm";

	public DistanceLine(Context context, boolean roundLine) {
		this.context = context;
		roundll = new LinearLayout(context);
		roundEditText = new EditText(context);
		distanceEditText = new EditText(context);
		faceEditText = new EditText(context);

		setEditTextStyle(roundLine);

		roundll.addView(roundEditText);
		roundll.addView(distanceEditText);
		roundll.addView(faceEditText);
		
	}

	private void setEditTextStyle(boolean roundLine) {
		LinearLayout.LayoutParams edParams;

		edParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
		roundEditText.setLayoutParams(edParams);

		edParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
		distanceEditText.setLayoutParams(edParams);
		faceEditText.setLayoutParams(edParams);

		if (roundLine) {
			roundEditText.setHintTextColor(context.getResources().getColor(R.drawable.green));
			roundEditText.setHint(ROUND_HINT);
			roundEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
			roundEditText.setTextColor(0xFFFFFFFF);
		} else {
			roundEditText.setEnabled(false);
		}

		distanceEditText.setHintTextColor(context.getResources().getColor(R.drawable.green));
		faceEditText.setHintTextColor(context.getResources().getColor(R.drawable.green));

		distanceEditText.setHint(DISTANCE_HINT);
		faceEditText.setHint(FACE_HINT);

		distanceEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
		faceEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
		
		distanceEditText.setTextColor(0xFFFFFFFF);
		faceEditText.setTextColor(0xFFFFFFFF);
		
		roundEditText.setId(1);
		distanceEditText.setId(2);
		faceEditText.setId(3);
		
		roundEditText.setNextFocusDownId(distanceEditText.getId());
		distanceEditText.setNextFocusUpId(roundEditText.getId()); //What happens if roundEditText is disabled...
		
		distanceEditText.setNextFocusDownId(faceEditText.getId());
		faceEditText.setNextFocusUpId(distanceEditText.getId());
		faceEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
	}

	public LinearLayout getLine() {
		return roundll;
	}
	
	public void disableLine() {
		roundEditText.setEnabled(false);
		distanceEditText.setEnabled(false);
		faceEditText.setEnabled(false);
		distanceEditText.setHint("");
		faceEditText.setHint("");
	}
}
