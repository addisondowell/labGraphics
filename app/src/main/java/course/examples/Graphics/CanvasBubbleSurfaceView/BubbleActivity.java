package course.examples.Graphics.CanvasBubbleSurfaceView;

import java.util.Random;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import static java.lang.Float.floatToIntBits;
import static java.lang.Thread.sleep;

/* @author A. Porter
 * Revised by S. Anderson
 * Further Editing by Addison Dowell
 * Wanted to make it very evident from the offset what is openly wrong with the app
 * The only issue I have consistently encountered is that the device correctly is able to
 * determine the location of the Bubble's bitmap area maybe 1/8 of the time. However, all the
 * other functions seem to work.
 *
 * For purpose of testing without this, the code provides the supposed location of the
 * bitmap which you can click on for the purpose of testing other functions within the code.
 */
public class BubbleActivity extends Activity {

	BubbleView mBubbleView;
	TextView fps;
	TextView score;
	ImageView ball;
	long scoreNum = 0;
	float ohBoyX = 0;
	float ohBoyY = 0;
	/** Simply create layout and view. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.frame);
		// decode resource into a bitmap
		final BubbleView bubbleView = new BubbleView(getApplicationContext(),
				BitmapFactory.decodeResource(getResources(), R.drawable.b256));

		fps = new TextView(getApplicationContext());
		score = new TextView(getApplicationContext());
		ball = new ImageView(getApplicationContext());

		relativeLayout.addView(bubbleView);
		relativeLayout.addView(fps);

		score.setText("Score: " + Long.toString(scoreNum));
		score.setPadding(0, 30, 0, 0);

		relativeLayout.addView(score);
		relativeLayout.addView(ball);
	}


	/*
	  SurfaceView is dedicated drawing surface in the view hierarchy.
      SurfaceHolder.Callback determines changes to SurfaceHolder via surfaceXXX
      callbacks.
	 */
	private class BubbleView extends SurfaceView implements
			SurfaceHolder.Callback {

		private Bitmap mBitmap;
		private int mBitmapHeightAndWidth, mBitmapHeightAndWidthAdj;
		private final DisplayMetrics mDisplay;
		private final int mDisplayWidth, mDisplayHeight;
		private float mX, mY, mDx, mDy, mRotation;
		private final SurfaceHolder mSurfaceHolder;
		private final Paint mPainter = new Paint(); // control style and color
		private Thread mDrawingThread;

		private static final int MOVE_STEP = 1;
		private static final float ROT_STEP = 0.5f;

		private int challengeFactor = 0;
		private long speedFactor = 0;

		public BubbleView(Context context, Bitmap bitmap) {
			super(context);

			mBitmapHeightAndWidth = (int) getResources().getDimension(
					R.dimen.image_height);

			this.mBitmap = Bitmap.createScaledBitmap(bitmap,
				mBitmapHeightAndWidth,
				mBitmapHeightAndWidth,
				false);


			mBitmapHeightAndWidthAdj = mBitmapHeightAndWidth / 2;

			mDisplay = new DisplayMetrics();
			// get display width/height
			BubbleActivity.this.getWindowManager().getDefaultDisplay()
					.getMetrics(mDisplay);
			mDisplayWidth = mDisplay.widthPixels;
			mDisplayHeight = mDisplay.heightPixels;

			// Give bubble random coords and speed at creation
			Random r = new Random();
			mX = (float) r.nextInt(mDisplayHeight);
			mY = (float) r.nextInt(mDisplayWidth);
			mDx = (float) r.nextInt(mDisplayHeight) / mDisplayHeight;
			mDx *= r.nextInt() == 1 ? MOVE_STEP : -1 * MOVE_STEP;
			mDy = (float) r.nextInt(mDisplayWidth) / mDisplayWidth;
			mDy *= r.nextInt(2) == 1 ? MOVE_STEP : -1 * MOVE_STEP;
			mDx *= 0.1;
			mDy *= 0.1;
			mRotation = 1.0f;

			mPainter.setAntiAlias(true); // smooth edges of bitmap
			// This will take care of changes to the bitmap
			mSurfaceHolder = getHolder();
			mSurfaceHolder.addCallback(this);

		}

		/** drawing and rotation */
		private void drawBubble(Canvas canvas) {
			canvas.drawColor(Color.DKGRAY);
			mRotation += ROT_STEP;

			canvas.rotate(mRotation, mY + ohBoyY, mX + ohBoyY);
			canvas.drawBitmap(mBitmap, mY, mX, mPainter);
		}

		/** True iff bubble can move. */
		private boolean move() {
			mX += mDx;
			mY += mDy;
			if (mX < 0 - mBitmapHeightAndWidth && mY < 0 - mBitmapHeightAndWidth) {
				return false;
			}
			if (mX > mDisplayWidth && mY > mDisplayHeight){
				return false;
			}
			if (mX < 0 - mBitmapHeightAndWidth && mY > mDisplayHeight){
				return false;
			}
			if (mX > mDisplayWidth && mY < 0 - mBitmapHeightAndWidth){
				return false;
			}
			if (mX > mDisplayWidth + mBitmapHeightAndWidth){
				return false;
			}
			if (mY > mDisplayHeight + mBitmapHeightAndWidth){
				return false;
			}
			if(mX < 0 - mBitmapHeightAndWidth){
				return false;
			}
			if(mY < 0 - mBitmapHeightAndWidth){
				return false;
			}
			else {
				return true;
			}
		}

		public void restart(){
			Random r = new Random();
			mX = (float) r.nextInt((mDisplayHeight - 75) + 25);
			mY = (float) r.nextInt((mDisplayWidth - 75) + 25);
			mDx = (float) r.nextInt(mDisplayHeight) / mDisplayHeight;
			mDx *= r.nextInt() == 1 ? MOVE_STEP : -1 * MOVE_STEP;
			mDy = (float) r.nextInt(mDisplayWidth) / mDisplayWidth;
			mDy *= r.nextInt(2) == 1 ? MOVE_STEP : -1 * MOVE_STEP;
			mRotation = 1.0f;
			int checkr = (int) scoreNum;
			if(scoreNum > 0){
				mBitmapHeightAndWidth = mBitmapHeightAndWidth - checkr * (mBitmapHeightAndWidth / 10);
				mBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.b256),
						mBitmapHeightAndWidth,
						mBitmapHeightAndWidth,
						false);
				Log.d("Size", "X:" + Float.toString(mX + mBitmapHeightAndWidth) + " Y:"+Float.toString(mY + mBitmapHeightAndWidth));
			}
			surfaceCreated(mSurfaceHolder);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event)
		{
			float x = event.getX();
			float y = event.getY();

			switch(event.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					//Check if the x and y position of the touch is inside the bitmap
					Log.d("Coordinates", "Dynamic " + Float.toString(mX) + " " + Float.toString(mY));
					Log.d("Coordinates", "Input " + Float.toString(x) + " " + Float.toString(y));

					if( x > mX && x < mX + mBitmapHeightAndWidth && y > mY && y < mY + mBitmapHeightAndWidth)
					{
						scoreNum += 1;
						ohBoyY +=10;
						ohBoyX +=10;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								score.setText("Score: " + Long.toString(scoreNum));
							}
						});
						restart();
					}
					return true;
			}
			return false;
		}

		/** Does nothing for surface change */
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
		}

		/** When surface created, this creates its thread AND starts it running. */
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// Run as separate thread.
			final RelativeLayout relativeLayout = new RelativeLayout(getContext());
			System.out.println(getContext());
			mDrawingThread = new Thread(new Runnable() {
				public void run() {
					Canvas canvas = null;
					// While bubble within view, lock and draw.
					while (!Thread.currentThread().isInterrupted() && move()) {
						canvas = mSurfaceHolder.lockCanvas();
						final long originTime = System.currentTimeMillis();
						if (null != canvas) { // Lock canvas while updating bitmap
							move();
							drawBubble(canvas);
							mSurfaceHolder.unlockCanvasAndPost(canvas);
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									long framesPer = 1000 / (System.currentTimeMillis() - originTime);
									fps.setText("FPS: " + Long.toString(framesPer));
								}
							});
						}
					}

				}
			});
			mDrawingThread.start();
			if(move() == false){
				Toast toast = Toast.makeText(getContext(), "Game Over!", Toast.LENGTH_LONG);
				toast.show();
				surfaceDestroyed(mSurfaceHolder);
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				scoreNum = 0;
				restart();
			}

		}
		/** Surface destroyed; stop thread. */
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (null != mDrawingThread)
				mDrawingThread.interrupt();
		}

	}
}