package benl.student.archeryscorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class ListSightMarks extends ListActivity {
	
	/**
	 * How data IO is handled:
	 * Opening:
	 * 	Read all names, sort to display in alphabetical order
	 * 
	 * Adding:
	 * 	Append data to file (to be sorted on re-addition). No I don't like that... Better to add to ArrayList then save.
	 * 	Refresh name list to reflect change (check for adapterType)
	 * 
	 * Editing:
	 * 	This is done through long clicking an item and selection in context menu
	 * 	Adjust for different adapterTypes
	 * 	Simple adapter requires renaming filename
	 * 	Sight Mark adapter requires editing ArrayLists then completely rewriting files
	 * 
	 * Delete:
	 * 	Delete current entry from arrayLists 
	 * 	If name deleted, remove all files with name
	 * 	If Sight Mark deleted, save all Sight Marks and Distances again, for current name
	 * 
	 * Leave page:
	 * 	Because everything is sorted, we need to save everything upon leaving that list.
	 */
	
	private int adapterType; //0 for simple, 1 for sight
	private String lastOpenName;
	
	private static final String delimiter = ";";
	private static final String distFileAppend = "_sight_mark_distance_file";
	private static final String sightFileAppend = "_sight_mark_file";
	private static final String windageFileAppend = "_windage_file";
	private static final String lastEditedFileAppend = "_last_edited_file";
	private static final String TAG = "ListSightMarks";
	
	private ArrayList<String> listOfNames;
	private ArrayList<Sights> listOfSightMarks;
	
	private SimpleListAdapter simpleAdapter;
	private SightMarksAdapter sightAdapter;
	
	//Simple object to store the distances and sight marks
	private class Sights {
		private String distance;
		private String mark;
		private String windage;
		private String lastEdited;
		
		Sights(String distance, String mark, String windage, String lastEdited) {
			this.distance = distance;
			this.mark = mark;
			this.windage = windage;
			this.lastEdited = lastEdited;
		}
		
		public void setSight(String distance, String mark, String windage, String lastEdited) {
			this.distance = distance;
			this.mark = mark;
			this.windage = windage;
			this.lastEdited = lastEdited;
		}
		
		public String getDistance() {
			return distance;
		}
		
		public String getMark() {
			return mark;
		}
	
		public String getWindage() {
			return windage;
		}
	
		public String getLastEdited() {
			return lastEdited;
		}
	}
	
	//The Adapters for the ListView
  	public class SimpleListAdapter extends ArrayAdapter<String> {
		public SimpleListAdapter(Context context, int textViewResourceId, ArrayList<String> listOfNames) {
			super(context, textViewResourceId, listOfNames);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.simple_listview_layout, parent, false);
			}
			
			((TextView) convertView.findViewById(R.id.textView1)).setText(listOfNames.get(position).toString());
			
			return convertView;
		}
	}
  	
  	public class SightMarksAdapter extends ArrayAdapter<Sights> {
		public SightMarksAdapter(Context context, int textViewResourceId, ArrayList<Sights> listOfSightMarks) {
			super(context, textViewResourceId, listOfSightMarks);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.listview_sight_layout, parent, false);
			}
//			((TextView) convertView.findViewById(R.id.textView1)).setText(sightDistances.get(position).toString());
//			((TextView) convertView.findViewById(R.id.textView2)).setText(sightMarks.get(position).toString());
			//TODO
			((TextView) convertView.findViewById(R.id.textViewDistance)).setText(listOfSightMarks.get(position).getDistance()+"m");
			((TextView) convertView.findViewById(R.id.textViewLastEdited)).setText(listOfSightMarks.get(position).getLastEdited());
			((TextView) convertView.findViewById(R.id.textViewHeight)).setText(listOfSightMarks.get(position).getMark());
			((TextView) convertView.findViewById(R.id.textViewWindage)).setText(listOfSightMarks.get(position).getWindage());
			//End
			return convertView;
		}
	}
  	
	
  	
  	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		
		listOfSightMarks = new ArrayList<Sights>(); //TODO:
		
		findAllNames();
		
		if (listOfNames.size() == 0) {
			cT("No sight marks!");
		}
		
		registerForContextMenu(getListView());
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	//Runs the simple adapter for names
	private void findAllNames() {
		try {

			listOfNames = new ArrayList<String> ();
			
			//Lists all the files in the directory
			String[] listOfFiles = getFilesDir().list();
			listOfNames = new ArrayList<String> ();
			
			//Removes all the unnecessary files
			for (int i = listOfFiles.length-1; i >= 0; --i) {
				int length = listOfFiles[i].length();
				
				if (length > sightFileAppend.length() 
						&& listOfFiles[i].substring(length-sightFileAppend.length(), 
								length).equals(sightFileAppend)) {
					Log.d(TAG,listOfFiles[i]);
					listOfNames.add(listOfFiles[i].substring(0, length - sightFileAppend.length()));
				} 
			} //for
			
			Collections.sort(listOfNames);
			
			setSimpleList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}//method
	
	private void setSimpleList() {
		try {
			simpleAdapter = new SimpleListAdapter(this, R.layout.simple_listview_layout, listOfNames);
			setListAdapter(simpleAdapter);
			
			//Waits for item click
			OnItemClickListener itemClicked = new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,	int arg2, long arg3) {
					if (adapterType == 0) {
						findSightMarks(listOfNames.get(arg2));
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
	private void findSightMarks(String name) {
		try {
			String[] fileNames = new String[4];
			fileNames[0] = name+distFileAppend;
			fileNames[1] = name+sightFileAppend;
			fileNames[2] = name+windageFileAppend;
			fileNames[3] = name+lastEditedFileAppend;
			
			String[] data = new String[fileNames.length];
			
			
			for (int i = 0; i < fileNames.length; i++) {
				data[i] = readFromFile(fileNames[i]);
			}
			
			//Lists all the files in the directory
//			sightDistances = new ArrayList<String> (Arrays.asList(data[0].split(delimiter)));
//			sightMarks = new ArrayList<String> (Arrays.asList(data[1].split(delimiter)));
			
			//TODO
			listOfSightMarks = new ArrayList<Sights>();
			String[] data0 = null;
			String[] data1 = null;
			String[] data2 = null;
			String[] data3 = null;
			if (data[0] != null && data[1] != null) {
				data0 = data[0].split(delimiter);
				data1 = data[1].split(delimiter);
				if (data[2] == null) {
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < data1.length-1; ++i) {
						sb.append(" "+delimiter+" ");
					}
					data[2] = sb.toString();
				}
				data[2] = data[2].replaceAll(";", " ; ");
				data2 = data[2].split(delimiter);
				if (data[3] == null) {
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < data1.length-1; ++i) {
						sb.append(" "+delimiter+" ");
					}
					data[3] = sb.toString();
				}
				data3 = data[3].split(delimiter);
				for (int i = 0; i < data0.length; i++) {
					listOfSightMarks.add(new Sights(data0[i], data1[i], data2[i], data3[i]));
				}
			}
			
			//End
			
			Collections.sort(listOfSightMarks, new Comparator<Sights>() {
				@Override
				public int compare(Sights lhs, Sights rhs) {
					//Return 0 for same, 
					try {
						return Integer.signum(Integer.parseInt(lhs.getDistance()) - Integer.parseInt(rhs.getDistance()));
					} catch (Exception e) {
						cT("Comparator Fail");
						return 0;
					}
				}   
			});
			
			setListOfSightMarks();
			
			lastOpenName = name;
			
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
			Log.d(TAG,"fileData length: "+fileContent.length());
			if (fileContent.length() == 0) {
				return null;
			}
			return fileContent.substring(0,fileContent.lastIndexOf(delimiter));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//Takes 2 array lists as input filling each textView with a certain list.
	private void setListOfSightMarks() {
		try {
			sightAdapter = new SightMarksAdapter(this, R.layout.listview_sight_layout, listOfSightMarks);
			setListAdapter(sightAdapter);
			
			adapterType = 1;
			setTitle("Sight Marks");
			
		} catch (Exception e) {
			e.printStackTrace();
			cT("List Failed");
		}
	}
	

	
	//Menu and ActionBar actions
  	@Override
  	public boolean onCreateOptionsMenu(Menu menu) {
  		// Creates an option menu from menu button
  		getMenuInflater().inflate(R.menu.activity_sight_marks, menu);
  		return true;
  	}
  	
  	@Override
  	public boolean onOptionsItemSelected(MenuItem item) {
  		// Handle item selection
  		
  		switch (item.getItemId()) {
  		case R.id.menu_add:
  			if (adapterType == 0) {
	  			alertName();
  			} else if (adapterType == 1) {
  				alertSight();
  			}
  			return true;

  		case android.R.id.home:
			navigateUp();
			return true;
			
  		default:
  			return super.onOptionsItemSelected(item);
  		}
  	}
  	
  	private void alertName() {
		final EditText inputName = new EditText(this);

		//Alert popup to add details as Sight Marks
		new AlertDialog.Builder(this)
		.setView(createAlertLayout(inputName))
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("New Archer")
		.setMessage("Enter Name")
		.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Add the new Name
				String name = inputName.getText().toString();
				if (!name.equals("")) {
					addName(name);
				}
			}

		})
		.setNegativeButton("Cancel", null)
		.show().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}
	
  	private void addName(String name) {
  		listOfNames.add(name);
  		createSightMarkFiles(name);
  		Collections.sort(listOfNames);
  		simpleAdapter.notifyDataSetChanged();
  	}
  
  	private void alertSight() {
  		final EditText inputDistance = new EditText(this);
  		final EditText inputSightMark = new EditText(this);
  		final EditText inputWindage = new EditText(this);

  		//Alert popup to add details as Sight Marks
  		new AlertDialog.Builder(this)
  		.setView(createAlertLayout(inputDistance, inputSightMark, inputWindage))
  		.setIcon(android.R.drawable.ic_dialog_alert)
  		.setTitle("New Sight Mark")
  		.setMessage("Enter Distance and Sight Mark")
  		.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
  			@Override
  			public void onClick(DialogInterface dialog, int which) {
  				//Adds sight mark
  				String distance = inputDistance.getText().toString();
  				String sightMark = inputSightMark.getText().toString();
  				String windage = inputWindage.getText().toString();
  				Calendar c = Calendar.getInstance(); 
  				String lastEdited = tString(c.get(Calendar.DATE)) + "/" + tString(c.get(Calendar.MONTH)+1) + "/" + c.get(Calendar.YEAR) + "\n" +
  						tString(c.get(Calendar.HOUR_OF_DAY)) + ":" + tString(c.get(Calendar.MINUTE)) + ":" + tString(c.get(Calendar.SECOND));
  				
  				if (!distance.equals("") && !sightMark.equals("")) {
  					addSightMark(distance, sightMark, windage, lastEdited);
  				}
  			}

  		})
  		.setNegativeButton("Cancel", null)
  		.show().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
  	}
  	
  	private void addSightMark(String distance, String sightMark, String windage, String lastEdited) {
  		listOfSightMarks.add(new Sights(distance, sightMark, windage, lastEdited));
		
		Collections.sort(listOfSightMarks, new Comparator<Sights>() {
			@Override
			public int compare(Sights lhs, Sights rhs) {
				//Return 0 for same, 
				try {
					return Integer.signum(Integer.parseInt(lhs.getDistance()) - Integer.parseInt(rhs.getDistance()));
				} catch (Exception e) {
					Log.e(TAG, "Comparator Fail");
					return 0;
				}
			}   
		});

		//TODO: Maybe I should change the sort to a numerical sort... Also, the sightMarks need to be sorted with the sightDistances 
		
		//NotifyDataSetChanged
		if (adapterType == 0) {
			simpleAdapter.notifyDataSetChanged();
		} else {
			sightAdapter.notifyDataSetChanged();
		}
		
		saveOpenedSightMarks();
  	}
  	
  	private LinearLayout createAlertLayout(EditText inputName) {
  		final LinearLayout ll = new LinearLayout(this);
  		
  		inputName.setGravity(Gravity.CENTER);
  		inputName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
  		inputName.setHint("Name");
  		inputName.setHintTextColor(getResources().getColor(R.drawable.green)); // 0x22AA22 - This needs changing -- Changed...?

  		ll.addView(inputName, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
  		return ll;
  	}
  	
  	private LinearLayout createAlertLayout(EditText inputDistance, EditText inputSightMark, EditText inputWindage) {
  		final LinearLayout ll = new LinearLayout(this);
  		final LinearLayout llSight = new LinearLayout(this);
  		llSight.setOrientation(LinearLayout.VERTICAL);
  		ll.setGravity(Gravity.CENTER_VERTICAL);
  		
  		inputDistance.setGravity(Gravity.CENTER);
  		inputDistance.setInputType(InputType.TYPE_CLASS_NUMBER);
  		inputDistance.setHint("Distance");
  		inputDistance.setHintTextColor(getResources().getColor(R.drawable.green));

  		inputSightMark.setGravity(Gravity.CENTER);
  		inputSightMark.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
  		inputSightMark.setHint("SightMark");
  		inputSightMark.setHintTextColor(getResources().getColor(R.drawable.green));

  		inputWindage.setGravity(Gravity.CENTER);
  		inputWindage.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
  		inputWindage.setHint("Windage");
  		inputWindage.setHintTextColor(getResources().getColor(R.drawable.green));
  		
  		ll.addView(inputDistance, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
  		llSight.addView(inputSightMark, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
  		llSight.addView(inputWindage, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
  		ll.addView(llSight, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
  		
  		return ll;
  	}
  	
  	
  	
  	//Context Menu
  	@Override
  	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
  		super.onCreateContextMenu(menu, v, menuInfo);
  		getMenuInflater().inflate(R.menu.context_sight_marks, menu);
  	}
  	
  	@Override
  	public boolean onContextItemSelected(MenuItem item) {
  		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    
	    switch (item.getItemId()) {
	        case R.id.menu_edit:
	        	if (adapterType == 0) {
	        		alertName(info.position);
	        	} else if (adapterType == 1) {
	        		alertSight(info.position);
	        	}
	        	return true;
	        	
	        case R.id.menu_delete:
	        	if (adapterType == 0) {
	        		deleteName(info.position);
	        	} else if (adapterType == 1) {
		        	deleteSightMark(info.position);
	        	}
	        	return true;
	        	
	        default:
	            return super.onContextItemSelected(item);
	    }
  	}

  	private void alertName(final int position) {
		final EditText inputName = new EditText(this);

		inputName.setText(listOfNames.get(position));
		
		//Alert popup to add details as Sight Marks
		new AlertDialog.Builder(this)
		.setView(createAlertLayout(inputName))
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Edit Name")
		.setMessage("Enter Name")
		.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Add the new Name
				String name = inputName.getText().toString();
				if (!name.equals("")) {
					editName(name, position);
				}
			}

		})
		.setNegativeButton("Cancel", null)
		.show().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}
  	
  	private void editName(String name, final int position) {
  		if (adapterType == 0 && !(listOfNames.contains(name) && listOfNames.get(position).equals(name))) {
			//Rename file
			File file = new File(listOfNames.get(position));
			file.renameTo(new File(name));
			
			//AdjustList
			listOfNames.set(position, name);
			Collections.sort(listOfNames);
			
			simpleAdapter.notifyDataSetChanged();
				
		}
  	}
  	
  	private void alertSight(final int position) {
  		final EditText inputDistance = new EditText(this);
  		final EditText inputSightMark = new EditText(this);
  		final EditText inputWindage = new EditText(this);
  		
  		inputDistance.setText(listOfSightMarks.get(position).getDistance());
  		inputSightMark.setText(listOfSightMarks.get(position).getMark());
  		inputWindage.setText(listOfSightMarks.get(position).getWindage());

  		//Alert popup to add details as Sight Marks
  		new AlertDialog.Builder(this)
  		.setView(createAlertLayout(inputDistance, inputSightMark, inputWindage))
  		.setIcon(android.R.drawable.ic_dialog_alert)
  		.setTitle("Edit Sight Mark")
  		.setMessage("Enter Distance and Sight Mark")
  		.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
  			@Override
  			public void onClick(DialogInterface dialog, int which) {
  				//Adds sight mark
  				String distance = inputDistance.getText().toString();
  				String sightMark = inputSightMark.getText().toString();
  				String windage = inputWindage.getText().toString();
  				Calendar c = Calendar.getInstance(); 
  				String lastEdited = tString(c.get(Calendar.DATE)) + "/" + tString(c.get(Calendar.MONTH)+1) + "/" + c.get(Calendar.YEAR) + "\n" +
  						tString(c.get(Calendar.HOUR_OF_DAY)) + ":" + tString(c.get(Calendar.MINUTE)) + ":" + tString(c.get(Calendar.SECOND));
  				
  				if (!distance.equals("") && !sightMark.equals("")) {
  					editSightMark(distance, sightMark, windage, lastEdited, position);
  				}
  			}

  		})
  		.setNegativeButton("Cancel", null)
  		.show().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
  	}
  	
  	private void editSightMark(String distance, String sightMark, String windage, String lastEdited, final int position) {
  		listOfSightMarks.get(position).setSight(distance, sightMark, windage, lastEdited);
		sightAdapter.notifyDataSetChanged();
		
		saveOpenedSightMarks();
  	}
  	
  	private void deleteName(int position) {
  		boolean success = true;
  		
  		File dir = getFilesDir();
  		
  		File file = new File(dir, listOfNames.get(position) + distFileAppend);
  		success &= file.delete();
  		file = new File(dir, listOfNames.get(position) + sightFileAppend);
  		success &= file.delete();
  		file = new File(dir, listOfNames.get(position) + windageFileAppend);
  		success &= file.delete();
  		file = new File(dir, listOfNames.get(position) + lastEditedFileAppend);
  		success &= file.delete();
  		if (success) {
  			listOfNames.remove(position);
  			simpleAdapter.notifyDataSetChanged();
  		} else {
  			Toast.makeText(this, "File deletetion failed!", Toast.LENGTH_SHORT).show();
  		}
  	}
  	
  	private void deleteSightMark(final int position) {
  		//Alert popup to add details as Sight Marks
  		new AlertDialog.Builder(this)
  		.setIcon(android.R.drawable.ic_dialog_alert)
  		.setTitle("Delete Sight Mark")
  		.setMessage("Are you sure you want to delete this Sight Mark?")
  		.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

  			@Override
  			public void onClick(DialogInterface dialog, int which) {
  				//Delete item
  				if (adapterType == 0) {
  					listOfNames.remove(position);
  					simpleAdapter.notifyDataSetChanged();
  				} else if (adapterType == 1) {
  					listOfSightMarks.remove(position);
  					sightAdapter.notifyDataSetChanged();
  					saveOpenedSightMarks();
  				}
  				
  			}

  		})
  		.setNegativeButton("Cancel", null)
  		.show().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
  	}
  	
  	
  	//Saves the last opened sight marks
  	private void saveOpenedSightMarks() {
  		//Only works in the sightAdapter screen
  		if (adapterType == 1) {
	  		String name = lastOpenName;
			
			//Names the files
	  		String[] fileNames = new String[4];
	  		fileNames[0] = name + distFileAppend;
	  		fileNames[1] = name + sightFileAppend;
	  		fileNames[2] = name + windageFileAppend;
	  		fileNames[3] = name + lastEditedFileAppend;
	  		
			
			//Writes to the two files, distances and sight marks
	  		FileOutputStream[] fos = new FileOutputStream[fileNames.length];
			
			try {
				for (int i = 0; i < fos.length; ++i)
					fos[i] = openFileOutput(fileNames[i], Context.MODE_PRIVATE);
				for (int i = 0; i < listOfSightMarks.size(); i++) {
					fos[0].write((listOfSightMarks.get(i).getDistance() + delimiter).getBytes());
					fos[1].write((listOfSightMarks.get(i).getMark() + delimiter).getBytes());
					fos[2].write((listOfSightMarks.get(i).getWindage() + " " + delimiter).getBytes());
					fos[3].write((listOfSightMarks.get(i).getLastEdited() + delimiter).getBytes());
				}
				for (int i = 0; i < fos.length; ++i)
					fos[i].close();
				
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, e.getMessage());
				cT("Sight Mark Save Failed");
			}
  		}
  	}
  	
  	private void createSightMarkFiles(String name) {
  		try {
  			FileOutputStream fos = openFileOutput(name + distFileAppend, Context.MODE_PRIVATE);
			fos.close();
			fos = openFileOutput(name + sightFileAppend, Context.MODE_PRIVATE);
			fos.close();
			fos = openFileOutput(name + windageFileAppend, Context.MODE_PRIVATE);
			fos.close();
			fos = openFileOutput(name + lastEditedFileAppend, Context.MODE_PRIVATE);
			fos.close();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
  	}
  	
	//Create Toast
  	private void cT(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	// Converts time integers to 2 significant figure strings
	private static String tString(int t) {
		String s = t + "";
		if (s.length() == 1) {
			return "0" + s;
		}
		return s;
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

	private void navigateUp() {
		if (adapterType == 1) {
			generateThumbnail();
			findAllNames();
		} else {
			NavUtils.navigateUpFromSameTask(this);
		}
	}
	
	//If on the sight adapter when the back button is pressed, it returns to the previous adapter 
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
