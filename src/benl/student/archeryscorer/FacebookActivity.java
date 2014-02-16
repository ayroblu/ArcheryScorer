package benl.student.archeryscorer;

import java.util.Arrays;
import java.util.List;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.widget.EditText;
//import android.view.Menu;
import com.facebook.*;
import com.facebook.model.*;

import android.content.Intent;

public class FacebookActivity extends Activity {

    private GraphUser user;
    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facebook);
		setupActionBar();
		
		Intent intent = getIntent();
		String message = intent.getStringExtra(ListScores.EXTRA_MESSAGE);
		((EditText) findViewById(R.id.editText1)).setText(message);
		
		// Start Facebook Login
		Session.openActiveSession(this,true, new Session.StatusCallback() {
			
			// Callback when session changes state
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				if (session.isOpened()) {
					
					// Make request to the /me API
					Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
						
						// Callback after Graph API response with user object
						@Override
						public void onCompleted(GraphUser user, Response response) {
//							Toast.makeText(getApplicationContext(), user.getName(), Toast.LENGTH_LONG).show();
//							Log.i("FaceBookActivity", "onCreate: StatusCallback-call: GraphUserCallback-onCompleted: " + user.getName());
							FacebookActivity.this.user = user;
						}
					});
				}
			}
			
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this,requestCode,resultCode,data);
	}
	
	
	public void post(View view) {
		Log.i("FacebookActivity", "post: performPublish()");
		performPublish();
	}
	
	
	private void postStatusUpdate() {
		try {
			if (user != null && hasPublishPermission()) {
				final String message = ((EditText) findViewById(R.id.button1)).getText().toString();
				Request request = Request.newStatusUpdateRequest(Session.getActiveSession(), message, new Request.Callback() {
					@Override
					public void onCompleted(Response response) {
						showPublishResult(message, response.getGraphObject(), response.getError());
					}
				});
				request.executeAsync();
	        } else {
	        	Log.i("FaceBookActivity", "PendingActionFail");
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
        
    }
	
	private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains("publish_actions");
    }
	
	private void showPublishResult(String message, GraphObject result, FacebookRequestError error) {
        String title = null;
        String alertMessage = null;
        if (error == null) {
            title = "Success";
//            String id = result.cast(GraphObjectWithId.class).getId();
            alertMessage = "Successfully Completed Post";
        } else {
            title = "Error";
            alertMessage = error.getErrorMessage();
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(alertMessage)
                .setPositiveButton("Okay", null)
                .show();
    }
	
	private void performPublish() {
        Session session = Session.getActiveSession();
        if (session != null) {
        	
            if (hasPublishPermission()) {
                // We can do the action right away.
                postStatusUpdate();
                Log.i("FacebookActivity", "performPublish: postStatusUpdate");
            } else {
                // We need to get new permissions, then complete the action when we get called back.
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, PERMISSIONS));
                Log.i("FacebookActivity", "performPublish: Publish Permission requested");
            }
        }
    }
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
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
	
}
