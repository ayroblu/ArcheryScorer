package benl.student.archeryscorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

public class ListScores extends ListActivity {
	/** 
	 * First thing is to display all the people with Saved Data, ignoring case...? (This means aggregating multiple files...)
	 * No... This should be done in the saving process... Alert Dialog with same name query
	 * 
	 * Once everyone has been listed setOnClickListers to detect once on is clicked to change to a list of their scores
	 * This should be a rehash of old code at this point
	 */

	//Set for whether we are using the simple or scores adapter. 0 == Simple, 1 == Scores.
	private int adapterType;
	private static final String delimiter = ";";
	private static final String TAG = "ListScores";

	public final static String EXTRA_NUM = "ArcheryScorer.ListScores.Position";
	public final static String EXTRA_NAME = "ArcheryScorer.ListScores.Name";
	public final static String EXTRA_MESSAGE = "ArcheryScorer.ListScores.Message";

	private ArrayList<String> listOfNames = new ArrayList<String> ();
	private ArrayList<Score> scores = new ArrayList<Score> ();

	private String lastOpenedName = "";
	private int lastOpenedNameIndex;

	private SimpleAdapter simpleAdapter;
	private ScoreAdapter scoreAdapter;
	
	private SearchView searchView;
	private EditText editText;
	
	private MenuItem searchMenuItem;

	//Declarations-------------------------------------------------------------
	private class Score {
		private String round;
		private String distance;
		private String face;
		private String score;
		private String time;
		private String date;

		private Score(String round, String distance, String face, String score, String date, String time) {
			this.round = round;
			this.distance = distance;
			this.face = face;
			this.score = score;
			this.time = time;
			this.date = date;
		}

		public String getRound() {
			return round;
		}

		public String getDistance() {
			return distance;
		}

		public String getFace() {
			return face;
		}

		public String getScore() {
			return score;
		}

		public String getTime() {
			return time;
		}

		public String getDate() {
			return date;
		}

		public String getData(int i) {
			switch (i) {
			case 0:
				return round;
			case 1:
				return distance;
			case 2:
				return face;
			case 3:
				return score;
			case 4:
				return time;
			case 5:
				return date;
			default:
				return "";
			}
		}

		@Override
		public String toString() {
			return round + ", " + distance + ", " + face + ", " + score + ", " + time + ", " + date;
		}
	}

	//The Adapters for the ListView
	public class SimpleAdapter extends ArrayAdapter<String> implements Filterable  {
	    public ArrayList<String> allItems;
	    private Filter filter;
	    public ArrayList<String> filtered;
	    
		public SimpleAdapter(Context context, int textViewResourceId, ArrayList<String> listOfNames) {
			super(context, textViewResourceId, listOfNames);
			this.allItems = new ArrayList<String>(listOfNames);
			if (listOfNames.size() == 0) {
				cT("No Saved Scores!");
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.simple_listview_layout, parent, false);
			}

			((TextView) convertView.findViewById(R.id.textView1)).setText(listOfNames.get(position));

			return convertView;
		}
		
	    
		@Override
	    public Filter getFilter() {
	        if(filter == null)
	            filter = new SimpleTypeFilter();
	        return filter;
	    }

	    private class SimpleTypeFilter extends Filter {

			@Override
	        protected FilterResults performFiltering(CharSequence constraint) {
	            // NOTE: this function is *always* called from a background thread, and
	            // not the UI thread.
	            constraint = constraint.toString().toLowerCase(Locale.ENGLISH);
	            FilterResults result = new FilterResults();
	            if(constraint != null && constraint.toString().length() > 0) {
	                ArrayList<String> filt = new ArrayList<String>();
	                for(String item : allItems) {
	                    if(item.toLowerCase(Locale.ENGLISH).contains(constraint))
	                        filt.add(item);
	                }
	                result.count = filt.size();
	                result.values = filt;
	            } else {
	                synchronized(this) {
	                    result.values = allItems;
	                    result.count = allItems.size();
	                }
	            }
	            Log.i(TAG, "namesList size, should stay the same: "+listOfNames.size());
	            return result;
	        }

	        @SuppressWarnings("unchecked")
	        @Override
	        protected void publishResults(CharSequence constraint, FilterResults results) {
	            // NOTE: this function is *always* called from the UI thread.
	            filtered = (ArrayList<String>)results.values;
	            notifyDataSetChanged();
	            clear();
	            Log.i(TAG, results.values + "");
	            int l = filtered.size();
	            for(int i = 0; i < l; i++)
	                add(filtered.get(i));
	            notifyDataSetInvalidated();
	        }

	    }
	}

	public class ScoreAdapter extends ArrayAdapter<Score> implements Filterable {
		public ArrayList<Score> allItems;
	    private Filter filter;
	    public ArrayList<Score> filtered;
	    
		public ScoreAdapter(Context context, int textViewResourceId, ArrayList<Score> scores) {
			super(context, textViewResourceId, scores);
			this.allItems = new ArrayList<Score>(scores);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.listview_score_layout, parent, false);
			}
			position = scores.size()-position-1;
			((TextView) convertView.findViewById(R.id.textView1)).setText(scores.get(position).getRound());
			((TextView) convertView.findViewById(R.id.textView2)).setText(scores.get(position).getDistance());
			((TextView) convertView.findViewById(R.id.textView3)).setText(scores.get(position).getFace());
			((TextView) convertView.findViewById(R.id.textView4)).setText(scores.get(position).getScore());
			((TextView) convertView.findViewById(R.id.textView5)).setText(scores.get(position).getDate() + "       "
					+ scores.get(position).getTime());
			return convertView;
		}

		@Override
	    public Filter getFilter() {
	        if(filter == null)
	            filter = new ScoreTypeFilter();
	        return filter;
	    }

	    private class ScoreTypeFilter extends Filter {
			@Override
	        protected FilterResults performFiltering(CharSequence constraint) {
	            // NOTE: this function is *always* called from a background thread, and
	            // not the UI thread.
	            constraint = constraint.toString().toLowerCase(Locale.ENGLISH);
	            FilterResults result = new FilterResults();
	            if(constraint != null && constraint.toString().length() > 0) {
	                ArrayList<Score> filt = new ArrayList<Score>();
	                for(Score item : allItems) {
	                    if(item.toString().toLowerCase(Locale.ENGLISH).contains(constraint))
	                        filt.add(item);
	                }
	                result.count = filt.size();
	                result.values = filt;
	            } else {
	                synchronized(this) {
	                    result.values = allItems;
	                    result.count = allItems.size();
	                }
	            }
	            Log.i(TAG, "namesList size, should stay the same: "+listOfNames.size());
	            return result;
	        }

	        @SuppressWarnings("unchecked")
			@Override
	        protected void publishResults(CharSequence constraint, FilterResults results) {
	            // NOTE: this function is *always* called from the UI thread.
	            filtered = (ArrayList<Score>) results.values;
	            notifyDataSetChanged();
	            clear();
	            int l = filtered.size();
	            for(int i = 0; i < l; i++)
	                add(filtered.get(i));
	            notifyDataSetInvalidated();
	        }

	    }
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			setContentView(R.layout.listview_old_score_layout);

			TextWatcher filterTextWatcher = new TextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
		        	Log.d(TAG, "afterTextChanged: " + s);
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
		        	Log.d(TAG, "beforeTextChanged: " + s);
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
		        	Log.d(TAG, "textChanged: " + s);
		        	if (adapterType == 0) {
		        		simpleAdapter.getFilter().filter(s);
		        	} else {
		        		scoreAdapter.getFilter().filter(s);
		        	}					
				}
		    };
		    EditText filterText = (EditText) findViewById(R.id.editTextSearch);
		    filterText.addTextChangedListener(filterTextWatcher);
        	Log.d(TAG, "textChangedListenerAdded");
		}
		
		setupActionBar();

		findAllNames();

		registerForContextMenu(getListView());
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private OnQueryTextListener getTextListener() {
		OnQueryTextListener listener = new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String newText) {
				return false;
			}
			@Override
			public boolean onQueryTextChange(String newText) {
		        if (TextUtils.isEmpty(newText)) {
		            getListView().clearTextFilter();
		        } else {
		        	getListView().setFilterText(newText.toString());
		        }
		        return true;
			}
		};
		return listener;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}


	//Setup the ListView--------------------------------------------------------
	//Runs the simple adapter for names
	private void findAllNames() {
		try {
			listOfNames = new ArrayList<String> ();

			//Lists all the files in the directory
			String[] listOfFiles = getFilesDir().list();
			String sameFileName = "_round_file";

			//Removes all the unnecessary files
			for (int i = listOfFiles.length-1; i >= 0; --i) {
				int length = listOfFiles[i].length();

				if (listOfFiles[i].length() > sameFileName.length() 
						&& listOfFiles[i].substring(length-sameFileName.length(), length).equals(sameFileName)) {
					listOfNames.add(listOfFiles[i].substring(0, length - sameFileName.length()));
				} 
			} //for
			Collections.sort(listOfNames);

			setSimpleList();
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("ListScores", "findAllNames: Error");
		}

	}//method

	private void setSimpleList() {
		try {
			simpleAdapter = new SimpleAdapter(this, R.layout.simple_listview_layout, listOfNames);
			setListAdapter(simpleAdapter);
//			setListAdapter(new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1,
//                listOfNames));
			
			//Waits for item click
			OnItemClickListener itemClicked = new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,	int arg2, long arg3) {
					if (adapterType == 0) {
						findScores(listOfNames.get(arg2));
						lastOpenedNameIndex = arg2;

						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
							clearSearch();
						} else {
							clearOldSearch();
						}
					} else if (adapterType == 1) {
						viewScore(arg2);
					}
				}
			};
			getListView().setOnItemClickListener(itemClicked);

			adapterType = 0;
			setTitle("Names");

		} catch (Exception e) {
			e.printStackTrace();
			cT("List Failed");
		}
	}


	//Runs the score adapter for scores for a certain name
	private void findScores(String name) {
		try {
			scores = new ArrayList<Score> ();

			String[] fileNames = new String[6];
			fileNames[0] = name+"_round_file";
			fileNames[1] = name+"_distance_file";
			fileNames[2] = name+"_face_file";
			fileNames[3] = name+"_score_file";
			fileNames[4] = name+"_date_file";
			fileNames[5] = name+"_time_file";

			String[] data = new String[6];

			for (int i = 0; i < fileNames.length; i++) {
				data[i] = readFromFile(fileNames[i]);
			}


			String data0[] = data[0].split(delimiter);
			String data1[] = data[1].split(delimiter);
			String data2[] = data[2].split(delimiter);
			String data3[] = data[3].split(delimiter);
			String data4[] = data[4].split(delimiter);
			String data5[] = data[5].split(delimiter);

			for (int i = 0; i < data0.length; i++) {
				if (data4[i].substring(2, 3).equals(":")) {
					String temp = data5[i];
					data5[i] = data4[i];
					data4[i] = temp;
					Log.e(TAG, "Time, Date swap needed");
				}
				scores.add(new Score(data0[i], data1[i], data2[i], data3[i], data4[i], data5[i]));
			}

			setListOfScores();

			lastOpenedName = name;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Rerun to return the string from a certain file.
	private String readFromFile(String fileName) {
		byte[] buffer = new byte[1024];
		StringBuffer fileContent = new StringBuffer("");
		try {
			FileInputStream fis = openFileInput(fileName);
			while ((fis.read(buffer)) != -1) {
				fileContent.append(new String(buffer));
			}
			fis.close();
			return fileContent.substring(0,fileContent.lastIndexOf(delimiter));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	//Takes list of score objects as input filling each textView with a certain list.
	private void setListOfScores() {
		try {
			scoreAdapter = new ScoreAdapter(this, R.layout.listview_score_layout, scores);
			setListAdapter(scoreAdapter);

			adapterType = 1;
			setTitle("Scores");
		} catch (Exception e) {
			e.printStackTrace();
			cT("List Failed");
		}
	}


	//Back button---------------------------------------------------------------
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			navigateUp();
			return true;

		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	private void navigateUp() {
		if (adapterType == 1) {
			generateThumbnail();
			findAllNames();
			

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				clearSearch();
			} else {
				clearOldSearch();
			}
		} else {
			NavUtils.navigateUpFromSameTask(this);
		}
	}

	//Context Menu------------------------------------------------------------
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (adapterType == 0) {
			getMenuInflater().inflate(R.menu.context_scores, menu);
		} else {
			getMenuInflater().inflate(R.menu.context_scores2, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId()) {
		case R.id.menu_view:
			if (adapterType == 0) {
				findScores(listOfNames.get(info.position));
			} else if (adapterType == 1) {
				//Here I need to add in a intent to start a new activity to display the score

				//Direct to new editing page, pass information through intent
				viewScore(scores.size() - 1 - info.position);
			}
			return true;

		case R.id.menu_export_to_excel:
			if (adapterType == 1) {
				exportToExcel(scores.size() - 1 - info.position);
			}
			return true;

		case R.id.menu_fb_share:
			openFacebook(scores.size() - 1 - info.position);
			return true;

		case R.id.menu_delete:
			//Deletes the current name or score
			if (adapterType == 0) {
				deleteName(info.position);
			} else if (adapterType == 1) {
				deleteScore(scores.size() - 1 - info.position);
			}
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	private void viewScore(int position) {
		File file = new File(getFilesDir(), lastOpenedName+"_"+(scores.size() - position-1));
		if (file.exists()) {
			//Direct to new editing page, pass information through intent
			Intent intent = new Intent(this, ScoreSheetViewer.class);
			intent.putExtra(EXTRA_NUM, scores.size() - position-1);
			intent.putExtra(EXTRA_NAME, lastOpenedName);
			startActivity(intent);
		} else {
			file = new File(getFilesDir(), lastOpenedName+"_target_"+(scores.size() - position-1));
			if (file.exists()) {
				//Direct to new editing page, pass information through intent
				Intent intent = new Intent(this, TargetViewer.class);
				intent.putExtra(EXTRA_NUM, scores.size() - position-1);
				intent.putExtra(EXTRA_NAME, lastOpenedName);
				startActivity(intent);
			} else {
				file = new File(getFilesDir(), lastOpenedName+"_dynamic_"+(scores.size() - position-1));
				if (file.exists()) {
					//Direct to new editing page, pass information through intent
					Intent intent = new Intent(this, DynamicScoreSheetViewer.class);
					intent.putExtra(EXTRA_NUM, scores.size() - position-1);
					intent.putExtra(EXTRA_NAME, lastOpenedName);
					startActivity(intent);
				}
			}
		}
		Log.i("ListScores", "viewScore: " + file.toString());

	}

	private void deleteName(int position) {
		//Looks for all the files and deletes the ones with the name, and in the arraylist

		try {
			//Lists all the files in the directory
			String[] listOfFiles = getFilesDir().list();
			String sameFileName = listOfNames.get(position)+ "_";

			//Removes all the unnecessary files
			for (int i = listOfFiles.length-1; i >= 0; --i) {
				int length = listOfFiles[i].length();

				if (length > sameFileName.length() 
						&& listOfFiles[i].substring(0, sameFileName.length()).equals(sameFileName)) {
					File file = new File(getFilesDir(), listOfFiles[i]);
					if (!file.delete()) {
						Log.i("ListScores", "deleteName: Failed to delete file: " + listOfFiles[i]);
					}
				} 
			} //for
			listOfNames.remove(position);
			simpleAdapter.notifyDataSetChanged();

		} catch (Exception e) {
			e.printStackTrace();
			Log.i("ListScores", "deleteName: Error Detected");
		}
	}

	private void deleteScore(int position) {
		if (scores.size() == 1) {
			deleteName(lastOpenedNameIndex);
			navigateUp();
			return;
		}

		//Looks for all the files and deletes the one in that position, and in the arraylist
		File scoreCardFile = new File(getFilesDir(), lastOpenedName + "_" + position);
		if (scoreCardFile.exists()) {
			scoreCardFile.delete();
		} else {
			scoreCardFile = new File(getFilesDir(), lastOpenedName + "_target_" + position);
			if (scoreCardFile.exists()) {
				scoreCardFile.delete();
			} else {
				scoreCardFile = new File(getFilesDir(), lastOpenedName + "_dynamic_" + position);
				if (scoreCardFile.exists()) {
					scoreCardFile.delete();
				} else {
					Log.i("ListScores","deleteScore: File not Found");
					cT("Error deleting file");
				}
			}
		}

		int length = scores.size()-1;
		Log.i(TAG, "Length = " + length);
		File dir = getFilesDir();
		Log.i(TAG, "Running renameScores from: " + (position+1) + " - to - " + length);
		for (int i = position + 1; i <= length; ++i) {
			renameAllScores(dir, i);
		}


		Log.i("ListScores", "Removing and saving scores");
		//Delete from arraylist and redo file, plus redo file
		scores.remove(position);
		saveScores();

		saveListNumber();
		//notifyDataSetChanged
		scoreAdapter.notifyDataSetChanged();

	}

	//Rename all saved scores upon deletion------------------------------------------
	public void renameAllScores(File dir, int index) {
		//Renames the file at an index to the file one less than that index (index -1)
		// File (or directory) with old name
		File file = new File(dir, lastOpenedName + "_" + index);

		// File (or directory) with new name
		File file2 = new File(dir, lastOpenedName + "_" + (index-1));

		if (!file.exists()) {
			file = new File(dir, lastOpenedName + "_target_" + index);
			file2 = new File(dir, lastOpenedName + "_target_" + (index-1));
			if (!file.exists()) {
				file = new File(dir, lastOpenedName + "_dynamic_" + index);
				file2 = new File(dir, lastOpenedName + "_dynamic_" + (index-1));
			}
		}
		Log.i(TAG, "Renaming file: " + file.getName() + " to: " + file2.getName());

		if (file2.exists()) {
			if (!file2.delete()) {
				cT("Failed to delete old file");
			}
			Log.i(TAG, "renameAllScores: weird existing file: "+ file2.toString());
		}

		// Rename file (or directory)
		if (!file.renameTo(file2)) {
			// File was not successfully renamed
			cT("Failed to rename old file");
		}
	}

	//Saves the new number of items in the list
	private void saveListNumber() {
		String listNumberFile = lastOpenedName + "_list_number_file";

		FileOutputStream fos;
		try {
			fos = openFileOutput(listNumberFile, Context.MODE_PRIVATE);
			fos.write((scores.size() + delimiter).getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
			cT("List Error");
		}
	}


	//Save the changes to scores arraylist--------------------------------------------
	private void saveScores() {
		String name = lastOpenedName;
		try {
			String[] fileNames = new String[6];
			fileNames[0] = name+"_round_file";
			fileNames[1] = name+"_distance_file";
			fileNames[2] = name+"_face_file";
			fileNames[3] = name+"_score_file";
			fileNames[4] = name+"_time_file";
			fileNames[5] = name+"_date_file";

			FileOutputStream[] fos = new FileOutputStream[6];
			for (int i = 0; i < fos.length; i++) {
				fos[i] = openFileOutput(fileNames[i], Context.MODE_PRIVATE);
				for (int j = 0; j < scores.size(); j++) {
					fos[i].write((scores.get(j).getData(i) + delimiter).getBytes());
				}
				fos[i].close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	//Special Features------------------------------------------------------------
	private void exportToExcel(int position) {
		int counter = 1;
		File path = Environment.getExternalStoragePublicDirectory("ArcheryScorer");
		File file = new File(path, lastOpenedName + counter+ ".csv");
		Log.i("ListScores", "exportToExcel: file = " + file);

		while (file.exists()) {
			file = new File(path, lastOpenedName + ++counter + ".csv");
		} 
		path.mkdirs();

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			tempRead(lastOpenedName,position, fos);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Reads temporary ScoreCard for this name from file
	private void tempRead(String name, int index, FileOutputStream fos) {
		int type;
		File file = new File(getFilesDir(), lastOpenedName+"_"+index);
		if (file.exists()) {
			type = 0;
		} else {
			file = new File(getFilesDir(), lastOpenedName+"_target_"+index);
			if (file.exists()) {
				type = 1;
			} else {
				file = new File(getFilesDir(), lastOpenedName+"_dynamic_"+index);
				if (file.exists()) {
					type = 2;
				} else {
					Log.d(TAG, "tempRead: Failed to find file");
					return;
				}
			}
		}
		// 		String tempFile = name + "_" + index;

		// Reads and writes all textView data from previous close
		byte[] buffer = new byte[1024];
		StringBuffer tempFileContent = new StringBuffer("");
		String tempFileData = null;
		String[] temp;

		FileInputStream fis;
		try {
			fis = openFileInput(file.getName());
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
			Log.v(TAG, "tempRead: tempFileData: " + tempFileData);
			temp = tempFileData.split(delimiter);
			writeExcel(temp, fos, type);

		}
	}

	private void writeExcel(String[] temp, FileOutputStream fos, int type) {
		switch (type) {
		case 0:
			//Turns input data into information for ScoreCard
			if (temp.length > 200) {
				try {
					int counter = 0;
					fos.write((temp[counter++]+",").getBytes());
					String[] distFaces = new String[8];
					for (int i = 0; i < 4; i++) {
						distFaces[i*2] = temp[counter++];
						distFaces[i*2+1] = temp[counter++];
					}
					counter += 10;

					for (int i = 0; i < 4; i++) {
						fos.write((distFaces[i*2]+",").getBytes());
						fos.write((distFaces[i*2+1]+"\n").getBytes());
						for (int j = 0; j < 6; j++) {
							for (int k = 0; k < 8; k++) {
								fos.write((temp[counter++]+",").getBytes());
							}
							fos.write(("\n").getBytes());
						}
						//	 					fos.write((temp[counter++]+",").getBytes());
					}

				} catch (Exception e) {
					Log.e(TAG,"writeExcel: " + e.toString());
					cT("Failed to write to Excel file");
					e.printStackTrace();
				}
			}
			return;

		case 1:
		case 2:
			//Turns input data into information for ScoreCard
			if (temp.length > 10) {
				int distanceLength = Integer.parseInt(temp[0]);
				int counter = 0;

				String[] distanceNames = new String[distanceLength*2];

				try {
					fos.write((temp[++counter]+",").getBytes());
					//Account for the round name and the distance and face fields
					for (int i = 0; i < distanceLength; i++) {
						distanceNames[i*2] = temp[++counter];
						distanceNames[i*2+1] = temp[++counter];
					}

					int numArrows = Integer.parseInt(temp[++counter]);

					++counter;

					int distChangesSize = Integer.parseInt(temp[++counter]);
					int[] distChanges = new int[distChangesSize];
					for (int i = 0; i < distChangesSize; i++) {
						distChanges[i] = Integer.parseInt(temp[++counter]);
					}

					int scoreCardSize = Integer.parseInt(temp[++counter]);

					int increment = 0;
					int lastDistanceChange = 0;
					if (distChangesSize > 0) {
						lastDistanceChange = distChanges[increment++];
					}

					final int lines;
					if (scoreCardSize == numArrows + 1) {
						lines = (scoreCardSize+1)/(numArrows+2);
					} else {
						lines = scoreCardSize/(numArrows+2);
					}

					for (int i = 0; i < lines; i++) {
						if (i >= lastDistanceChange) {
							fos.write((distanceNames[increment*2]+",").getBytes());
							fos.write((distanceNames[increment*2+1]+"\n").getBytes());
							if (increment < distChangesSize) {
								lastDistanceChange = distChanges[increment++];
							} else {
								//Effectively at infinity
								lastDistanceChange = scoreCardSize;
								++increment;
							}
						}
						for (int j = 0; j < (numArrows + 2); j++) {
							fos.write((temp[++counter]+",").getBytes());
							if (lines == 1 && j == numArrows) {
								break;
							}
						}
						fos.write(("\n").getBytes());
					}
				} catch (Exception e) {
					Log.e(TAG, "enterData: " + e.toString());
					e.printStackTrace();
				}
			}

			return;

		default:
			return;
		}


	}


	private void openFacebook(int position) {
		String round = scores.get(position).getRound();
		String score = scores.get(position).getScore();
		int index = score.lastIndexOf("\n");
		if (index > 0) {
			score = score.substring(index, score.length()-1);
		}
		final String message = "Hey, I scored " + score + " in the " + round + " round";
		Intent intent = new Intent(this, FacebookActivity.class);
		intent.putExtra(EXTRA_MESSAGE, message);
		startActivity(intent);
	}

	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void clearSearch() {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				searchView.setQuery("", false);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					collapseSearch();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void collapseSearch() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			searchMenuItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW|MenuItem.SHOW_AS_ACTION_IF_ROOM);
			searchMenuItem.collapseActionView();
		}
	}
	
	private void clearOldSearch() {
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				editText.setText("");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	        createHoneyOptionsMenu(menu);
	        return true;
		}
		return false;
    }
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void createHoneyOptionsMenu(Menu menu) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getMenuInflater().inflate(R.menu.activity_saved_scores, menu);
	        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
	        searchView.setOnQueryTextListener(getTextListener());
	        searchView.setSubmitButtonEnabled(false);
	        searchView.setIconifiedByDefault(true);
	        getListView().setTextFilterEnabled(true);
	        searchView.setQueryHint("Search");
	        searchMenuItem = menu.getItem(0);
		}
	}
	

	//Create Toast----------------------------------------------------------------------
	private void cT(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	private void generateThumbnail() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean update = sharedPref.getBoolean(SettingsActivity.UPDATE_THUMBNAILS, true);
		if (update) {
			File file = new File(getFilesDir(), TAG+".png");

			try {
				OutputStream os = new FileOutputStream(file);

				getListView().getRootView().setDrawingCacheEnabled(true);
				getResizedBitmap(getListView().getRootView().getDrawingCache()).compress(CompressFormat.PNG, 100, os);
				getListView().getRootView().setDrawingCacheEnabled(false);

				os.close();
			} catch (FileNotFoundException e) {
				Log.e(TAG, "save: FileNotFoundException" + e.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Bitmap getResizedBitmap(Bitmap bm) {
	    int width = bm.getWidth();
	    int height = bm.getHeight();

	    return Bitmap.createScaledBitmap(bm, width/2, height/2, false);
	}

	//If on the score adapter when the back button is pressed, it returns to the previous adapter 
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			navigateUp();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

}
