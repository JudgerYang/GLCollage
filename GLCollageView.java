package com.perfectcorp.youcamcollage.view.widget.glcollage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.io.IOException;

public class GLCollageView extends GLSurfaceView {
	private final GLCollageRenderer mRenderer;
	private final GestureDetector mGestureDetector;

	Triangle mTriangle;
	TextureRectangle mRect;

	public GLCollageView(Context context) {
		this(context, null);
	}

	public GLCollageView(Context context, AttributeSet attrs) {
		super(context, attrs);

		setEGLContextClientVersion(2);
		mRenderer = new GLCollageRenderer();
		mRenderer.runOnDraw(new Runnable() {
			@Override
			public void run() {
				try {
					mTriangle = new Triangle(getContext());
					mRenderer.addDrawObject(mTriangle);

					mRect = new TextureRectangle(getContext(), new RectF(0, 1, 1, 0));
					mRenderer.addDrawObject(mRect);
				} catch (IOException ignored) {
				}
			}
		});

		setRenderer(mRenderer);

		mGestureDetector = new GestureDetector(context, new GestureListener());
	}

	public void setImage(int index, Bitmap bmp) {
		if (index == 0) {
			mRect.setImage(bmp);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mGestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
	}

	private class GestureListener extends GestureDetector.SimpleOnGestureListener {
		private float mRotation = 0;
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			mRotation = 0;
			if (mRect != null) {
				mRect.setRotation(mRotation);
			}
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			mRotation = ((mRotation + distanceX) % 360 + 360) % 360;
			if (mRect != null) {
				mRect.setRotation(mRotation);
			}
			return true;
		}
	}
}
