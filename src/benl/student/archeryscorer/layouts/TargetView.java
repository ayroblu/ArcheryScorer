package benl.student.archeryscorer.layouts;

import java.util.ArrayList;
import java.util.List;

import benl.student.archeryscorer.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.View;

public class TargetView extends View {
    private Context context;

    private List<Point> points = new ArrayList<Point>();
    private Paint paint = new Paint();
    private Paint ringPaint = new Paint();
    private Point screenSize = new Point();
    private Point centre = new Point();
    private boolean fullTarget = true;
    private int outerMostRing;
    private boolean allPoints = true;
    private int numArrows;
    
    private static final int arrowDiameter = 6;
    
    private int section = 0;
    private int end = 0;

    
    public TargetView(Context context) {
        super(context);
        this.context = context;
        setFocusable(true);
        setFocusableInTouchMode(true);
        
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        
        ringPaint.setAntiAlias(true);
        ringPaint.setStyle(Style.STROKE); //Does this mean it will make the stroke size several pixels both left and right of the specified position?
        
//        setBackgroundResource(R.drawable.target);
        setBackgroundResource(R.drawable.gradient);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
    		setSizeHighAPI();
    	} else {
    		setSizeLowAPI();
    	}
    }
    
    public void setNumArrows(int numArrows) {
        this.numArrows = numArrows;
    }
  
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void setSizeHighAPI() {
    	((Activity) context).getWindowManager().getDefaultDisplay().getSize(screenSize);
    }
    
    @SuppressWarnings("deprecation")
	private void setSizeLowAPI() {
    	screenSize.y = ((Activity) context).getWindowManager().getDefaultDisplay().getHeight();
    	screenSize.x = ((Activity) context).getWindowManager().getDefaultDisplay().getWidth();
    }

    
    @Override
    public void onDraw(Canvas canvas) {
    	if (fullTarget) {
    		drawTarget(canvas);
		} else {
			drawSpotTarget(canvas);
		}
        drawArrows(canvas);
    }
    
    private void drawTarget(Canvas canvas) {
//    	canvas.drawCircle(point.x, point.y, 5, paint);
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
    	centre.x = screenSize.x/2;
    	centre.y = screenSize.x/2;
    	section = screenSize.x / 21;
//    	0xFFFFFFFF
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
    
    private void drawSpotTarget(Canvas canvas) {
    	centre.x = screenSize.x/2;
    	centre.y = screenSize.x/2;
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
    		int begin = end * numArrows;
    		int last = (end+1) * numArrows;
    		try {
        		for (int i = begin; i < last; i++) {
        			Point point = points.get(i);
            		paint.setColor(0xFF000000);
                    canvas.drawCircle(point.x, point.y, arrowDiameter, paint);
            		paint.setColor(0xDDFF8800);
                    canvas.drawCircle(point.x, point.y, arrowDiameter-2, paint);
    			}
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    }
    
    public boolean alternatePoints() {
    	allPoints = !allPoints;
    	return allPoints;
    }
    
    public void setEnd(int index) {
    	switch (index) {
		case 0:
			// Full Left
			end = 0;
			break;
			
		case 1:
			// Left
			if (end>0) {
				--end;
			}
			break;
			
		case 2:
			// Change between end and all (not used)
			
			break;
			
		case 3:
			// Right
			if (end < getEndSize()) {
				++end;
			}
			break;
			
		case 4:
			// Full Right
			end = getEndSize();
			break;

		default:
			break;
		}
    	invalidate();
    }
    
    public int getEnd() {
    	return end;
    }
    
    public int getEndSize() {
    	return (int) Math.floor((points.size()-1)/numArrows);
    }
    
    public void enterPoints(String[] temp, int counter) {
    	try {
        	int savedScreenSize = Integer.parseInt(temp[++counter]); //To make scaling if not equal
        	screenSize.x = savedScreenSize;
        	
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