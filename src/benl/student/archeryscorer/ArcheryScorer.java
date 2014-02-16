package benl.student.archeryscorer;

import java.io.File;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class ArcheryScorer extends Activity {

	private static final String TAG = "ArcheryScorer";
	private static final String THUMB0 = "DynamicArcheryScoreSheet.png";
	private static final String THUMB1 = "ArcheryScoreSheet.png";
	private static final String THUMB2 = "ListScores.png";
	private static final String THUMB3 = "TouchScreenInterface.png";
	private static final String THUMB4 = "ListSightMarks.png";
	private static final String THUMB5 = "Calculator.png";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_archery_scorer);
		PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);
		setThumbnails();
	}
	
	private void setThumbnails() {
		File file = new File(getFilesDir(), THUMB0);
		ImageView imageView = (ImageView) findViewById(R.id.dynamic_archery_score_sheet);
		if (file.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
			imageView.setImageBitmap(bitmap);
		} else {
			imageView.setImageResource(R.drawable.dynamic_score_sheet);
		}
		
		file = new File(getFilesDir(), THUMB1);
		imageView = (ImageView) findViewById(R.id.archery_score_sheet);
		if (file.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
			imageView.setImageBitmap(bitmap);
		} else {
			imageView.setImageResource(R.drawable.archery_score_sheet);
		}
		
		file = new File(getFilesDir(), THUMB2);
		imageView = (ImageView) findViewById(R.id.saved_pages);
		if (file.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
			imageView.setImageBitmap(bitmap);
		} else {
			imageView.setImageResource(R.drawable.saved_pages);
		}
		
		file = new File(getFilesDir(), THUMB3);
		imageView = (ImageView) findViewById(R.id.target);
		if (file.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
			imageView.setImageBitmap(bitmap);
		} else {
			imageView.setImageResource(R.drawable.target_page);
		}
		
		file = new File(getFilesDir(), THUMB4);
		imageView = (ImageView) findViewById(R.id.sight_marks);
		if (file.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
			imageView.setImageBitmap(bitmap);
		} else {
			imageView.setImageResource(R.drawable.sight_marks);
		}
		
		file = new File(getFilesDir(), THUMB5);
		imageView = (ImageView) findViewById(R.id.calculator);
		if (file.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
			imageView.setImageBitmap(bitmap);
		} else {
			imageView.setImageResource(R.drawable.calculator);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_archery_scorer, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			switchPage(SettingsActivity.class);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void changePage(View view) {
		try {
			int index = Integer.parseInt(view.getTag().toString());
			switch (index) {
			case 0:
				switchPage(DynamicArcheryScoreSheet.class);
				break;
				
			case 1:
				switchPage(ArcheryScoreSheet.class);
				break;
				
			case 2:
				switchPage(ListScores.class);
				break;
				
			case 3:
				switchPage(TouchScreenInterface.class);
				break;
				
			case 4:
				switchPage(ListSightMarks.class);
				break;
				
			case 5:
				switchPage(Calculator.class);
				break;
				
			default:
				break;
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void switchPage(Class<?> cls) {
  		startActivity(new Intent(this, cls));
  		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
  	  		this.finish();
		}
  	}
}
