package benl.student.archeryscorer.layouts;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import benl.student.archeryscorer.R;
import benl.student.archeryscorer.TouchScreenInterface;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class DrawView extends View implements OnTouchListener {
    private TouchScreenInterface context;

    private List<Point> points = new ArrayList<Point>();
    private Paint paint = new Paint();
    private Paint ringPaint = new Paint();
    public Point screenSize = new Point();
    private Point centre = new Point();
    private boolean touching = false;
    private Point touch = new Point();
    private boolean fullTarget = true;
    private int outerMostRing;
    private boolean allPoints = true;
    
    private static final int arrowDiameter = 6;
    private static final String delimiter = ";";
    
    private int section = 0;
    private boolean inAnimation = false;
    private Point animationPoint = new Point();

    
    //Constructor-----------------------------------------------------------------------
    public DrawView(Context context) {
        super(context);
        this.context = (TouchScreenInterface) context;
        setFocusable(true);
        setFocusableInTouchMode(true);
        
        this.setOnTouchListener(this);

        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        
        ringPaint.setAntiAlias(true);
        ringPaint.setStyle(Style.STROKE); //Does this mean it will make the stroke size several pixels both left and right of the specified position?
        
//        setBackgroundResource(R.drawable.target);
//        int screenLayout = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        try {
//        	if (screenLayout >= Configuration.SCREENLAYOUT_SIZE_XLARGE) {
//        		setBackgroundResource(R.drawable.gradient);
//        	} else if (screenLayout >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
//        		setBackgroundResource(R.drawable.gradient_small);
//        	} else {
        		setBackgroundResource(R.drawable.gradient_xsmall);
//        	}
        } catch (Exception e) {
        	Toast.makeText(context, "Sorry, it appears I can't load the background", Toast.LENGTH_LONG).show();
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
    		setSizeHighAPI();
    	} else {
    		setSizeLowAPI();
    	}

    	centre.x = screenSize.x/2;
    	centre.y = screenSize.x/2;
    }
  
    //Finds the size of the screen
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void setSizeHighAPI() {
    	context.getWindowManager().getDefaultDisplay().getSize(screenSize);
    }
    
    @SuppressWarnings("deprecation")
	private void setSizeLowAPI() {
    	screenSize.y = context.getWindowManager().getDefaultDisplay().getHeight();
    	screenSize.x = context.getWindowManager().getDefaultDisplay().getWidth();
    }

    
    // Draw methods----------------------------------------------------------------------
    @Override
    public void onDraw(Canvas canvas) {
    	if (fullTarget) {
//    		drawTarget(canvas);
    		drawTarget(canvas,centre);
    		if (inAnimation) {
    			drawTarget(canvas, animationPoint);
    		}
		} else {
			drawSpotTarget(canvas, centre);
			if (inAnimation) {
				drawSpotTarget(canvas, animationPoint);
			}
		}
        drawArrows(canvas);
        if (touching) {
        	drawLines(canvas);
        }
    }
    
    private void drawTarget(Canvas canvas, Point centre) {
    	/**
    	 * Okay, so a target typically has 10 equispaced rings.
    	 * It should draw each circle on top of the other:
    	 * White, Black, Blue, Red, Yellow
    	 * 
    	 * Then you need to draw each of the rings (maybe should have a gradient on the outside to show a miss?)
    	 * Make sure to account for the fact that the black needs a white border
    	 * 
    	 * Numbers:
    	 * Remember to account for a miss space
    	 * Consider: 11 rings (1 is a miss), account for the double measurement of rings
    	 * radius = Width/22 (or 21) - to int
    	 * First start at 1, then radius * 3, 5, 7, 9
    	 */
    	section = screenSize.x / 21;
    	Log.i("DrawView", "drawTarget: centre.x: " + centre.x);
    	drawRing(canvas, centre, Color.YELLOW, section, section);
    	drawRing(canvas, centre, Color.RED, section*3, section);
    	drawRing(canvas, centre, 0xFF55BBFF, section*5, section);
    	drawRing(canvas, centre, Color.BLACK, section*7, section);
    	drawRing(canvas, centre, Color.WHITE, section*9, section);
    	
    	//Drawing border rings
    	int ringThickness = 1;
    	drawRing(canvas, centre, Color.BLACK, section/2-ringThickness, ringThickness);
    	for (int i = 1; i <= 10; i++) {
    		if (i == 7) {
            	drawRing(canvas, centre, Color.WHITE, section*i-ringThickness, ringThickness);
			} else {
	        	drawRing(canvas, centre, Color.BLACK, section*i-ringThickness, ringThickness);
			}
		}
    }
    
    private void drawSpotTarget(Canvas canvas, Point centre) {
    	section = screenSize.x / ((11-outerMostRing)*2+1);
    	
    	drawRing(canvas, centre, Color.YELLOW, section, section);
    	if (outerMostRing <= 8) {
    		if (outerMostRing == 8) {
            	drawRing(canvas, centre, Color.RED, (int) (section*2.5), section/2);
    		} else {
            	drawRing(canvas, centre, Color.RED, section*3, section);
    		}
		} 
    	if (outerMostRing <= 6) {
    		if (outerMostRing == 6) {
            	drawRing(canvas, centre, 0xFF55BBFF, (int) (section*4.5), section/2);
    		} else {
            	drawRing(canvas, centre, 0xFF55BBFF, section*5, section);
    		}
    	}
    	
    	//Drawing border rings
    	int ringThickness = 1;
    	drawRing(canvas, centre, Color.BLACK, section/2-ringThickness, ringThickness);
    	for (int i = 1; i <= (11-outerMostRing); i++) {
    		drawRing(canvas, centre, Color.BLACK, section*i-ringThickness, ringThickness);
			
		}
    }
    
    
    private void drawRing(Canvas canvas, Point point, int colour, int ringRadius, int ringThickness) {
    	ringPaint.setStrokeWidth(ringThickness*2);
    	ringPaint.setColor(colour);
    	canvas.drawCircle(point.x, point.y, ringRadius, ringPaint);
    }
    
    
    private void drawArrows(Canvas canvas) {
    	if (allPoints) {
    		for (Point point : points) {
        		paint.setColor(0xFF000000);
                canvas.drawCircle(point.x, point.y, arrowDiameter, paint);
        		paint.setColor(0xDDFF8800);
                canvas.drawCircle(point.x, point.y, arrowDiameter-2, paint);
                // Log.d(TAG, "Painting: "+point);
            }
		} else {
			int start = (int) (Math.floor((points.size()-1) / (context.getNumArrows())) * (context.getNumArrows()));
			for (int i = start; i < points.size(); i++) {
				Point point = points.get(i);
				paint.setColor(0xFF000000);
                canvas.drawCircle(point.x, point.y, arrowDiameter, paint);
        		paint.setColor(0xDDFF8800);
                canvas.drawCircle(point.x, point.y, arrowDiameter-2, paint);
			}
		}
    }
    
    private void drawLines(Canvas canvas) {
    	paint.setColor(getResources().getColor(R.drawable.green));
    	canvas.drawLine(touch.x, 0, touch.x, this.getHeight(), paint);
    	canvas.drawLine(0, touch.y, this.getWidth(), touch.y, paint);
    	
    }
    
    
    //Animations-------------------------------------------------------------------------
    public void setCentre(int location) {
//    	centre.x = location;
    	centre.y = location;
		animationPoint.x = centre.x;
		animationPoint.y = screenSize.x + centre.y;
		Log.i("DrawView", "setCentre: animationPoint: " + animationPoint.y);
    	invalidate();
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void drawAnimation() {
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    		setBackgroundResource(0);
        	ValueAnimator colorAnim = ObjectAnimator.ofInt(this, "Centre", -screenSize.x/2, screenSize.x/2);
            colorAnim.setDuration(1000);
//            colorAnim.setEvaluator(new ArgbEvaluator());
//            colorAnim.setRepeatCount(ValueAnimator.INFINITE);
//            colorAnim.setRepeatCount(1);
//            colorAnim.setRepeatMode(ValueAnimator.REVERSE);
            colorAnim.start();
            Log.i("DrawView", "drawAnimation: Animation Started");
            
            inAnimation = true;
            
            this.postDelayed(new Runnable() {
                @Override
                public void run() {
                	inAnimation = false;
                	setBackgroundResource(R.drawable.gradient);
                	Log.d("DrawView", "drawAnimation: run delayed code");
                }
            }, 1000);
    	}
    }
    
    
    //Touching methods---------------------------------------------------------------------
    PopupWindow pw = null;
    TextView tv = null;
    
    private PopupWindow drawPopup(int x, int y) {
    	LayoutInflater inflater = (LayoutInflater)	context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	View view = inflater.inflate(R.layout.popupwindow_drawview, null, false);
    	PopupWindow pw = new PopupWindow(view, 100,100);
    	tv = ((TextView) view.findViewById(R.id.textViewPopUp));
    	
    	// The code below assumes that the root container has an id called 'main'
    	pw.showAtLocation(this, Gravity.NO_GRAVITY, x, y); 
    	
    	return pw;
    }

    public boolean onTouch(View view, MotionEvent event) {
    	touch.set((int) event.getX(),(int) event.getY());
    	if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
    		pw = drawPopup(touch.x, touch.y);
    		tv.setText(findScore(touch.x, touch.y));
    		touching = true;
            invalidate();
            return true;
            
    	} else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
    		tv.setText(findScore(touch.x, touch.y));
    		if (pw != null) {
    			int[] location = new int[2];
    			this.getLocationInWindow(location);

    			int pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 15, getResources().getDisplayMetrics());
    			
				pw.update(touch.x+location[0], touch.y+location[1]-pixels, -1, -1);
				invalidate();
    		}
    		
    	} else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            pw.dismiss();
            Log.i("DrawView", "pw dismissed");
            
            touching = false;
            
    		Point point = new Point();
            point.x = (int) event.getX();
            point.y = (int) event.getY();
            points.add(point);
            
            try {
            	int buttonTag;
            	String score = findScore(point.x, point.y);
            	if (score.equals("M")) {
            		buttonTag = 0;
            	} else if (score.equals("X")) {
            		buttonTag = 11;
            	} else {
            		buttonTag = Integer.parseInt(score);
            	}
            	context.addEntry(buttonTag);
            	
			} catch (Exception e) {
				Log.e("DrawView","onTouch: IntegerParse failed" + e.getMessage());
				e.printStackTrace();
			}
            
            invalidate();
            return true;
		}
    	return super.onTouchEvent(event);
    }

    private String findScore(int x, int y) {
    	double distance = Math.hypot(x-centre.x, y-centre.y); 
    	if (distance < section*0.5 + arrowDiameter/2) {
			return "X";
		}
    	for (int i = 1; i < 11; i++) {
    		if (i == (12-outerMostRing) && !fullTarget) {
    			return "M";
    		} else if (distance < section*i + arrowDiameter/2) {
    			return (11 - i) + "";
    		}
		}
    	return "M";
    }

    
    //Interface methods---------------------------------------------------------------
    public void removeLastPoint() {
    	points.remove(points.size()-1);
    	invalidate();
    }
    
    public void swap() {
    	allPoints = !allPoints;
    	invalidate();
    }
    
    public void changeFace(int ring) {
    	outerMostRing = ring;
    	fullTarget = !fullTarget;
    	invalidate();
    }
    
    //Saving methods
    public void save(FileOutputStream fos) {
    	try {
			fos.write((screenSize.x + delimiter).getBytes());
			fos.write(((fullTarget ? "1" : "0") + delimiter).getBytes());
			Log.i("DrawView", "save: ternary = " + (fullTarget ? "1" : "0") + delimiter);
			fos.write((outerMostRing + delimiter).getBytes());
			
			fos.write((points.size() + delimiter).getBytes());
			for (Point point : points) {
				fos.write((point.x + delimiter).getBytes());
				fos.write((point.y + delimiter).getBytes());
			}
		} catch (Exception e) {
			Log.e("DrawView", "save" + e.toString());
			e.printStackTrace();
		}
    }

    public void enterPoints(String[] temp, int counter) {
    	try {
    		++counter;
//        	int savedScreenSize = Integer.parseInt(temp[++counter]); //To make scaling if not equal
//        	screenSize.x = savedScreenSize;
        	
        	String savedTarget = temp[++counter];
        	if (savedTarget.equals("0")) {
				fullTarget = false;
			}
        	
        	outerMostRing = Integer.parseInt(temp[++counter]);
        	
        	int numPoints = Integer.parseInt(temp[++counter]);
        	for (int i = 0; i < numPoints; i++) {
            	points.add(new Point(Integer.parseInt(temp[++counter]), Integer.parseInt(temp[++counter])));
			}
        	invalidate();
		} catch (Exception e) {
			Log.e("TargetView", "enterPoints: " + e.toString());
		}
    	
    }
    
}