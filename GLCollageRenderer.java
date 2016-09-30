package com.perfectcorp.youcamcollage.view.widget.glcollage;

import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class GLCollageRenderer implements android.opengl.GLSurfaceView.Renderer {
	private final List<GLDrawObject> mDrawObjectList = new ArrayList<>();
	private final Queue<Runnable> mRunOnDraw = new ConcurrentLinkedQueue<>();

	// mMVPMatrix is an abbreviation for "Model View Projection Matrix"
	private final float[] mMVPMatrix = new float[16];
	private final float[] mProjectionMatrix = new float[16];
	private final float[] mViewMatrix = new float[16];

	// Position the eye behind the origin.
	private final GLUtility.GLPoint mEye = new GLUtility.GLPoint(0f, 0f, 3f);
	// We are looking toward the distance
	private final GLUtility.GLPoint mLook = new GLUtility.GLPoint(0f, 0f, -4f);
	// This is where our head would be pointing were we holding the camera.
	private final GLUtility.GLPoint mUp = new GLUtility.GLPoint(0f, 1f, 0f);

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// Set the background frame color
		GLES20.glClearColor(0.3f, 0.0f, 0.0f, 1.0f);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);

		RectF projectionRect = new RectF(-1, 1, 1, -1);
		if (width > height) {
			// (-1,  1)      |      (1,  1)
			//        *------+------*
			//        |      |      |
			//   -----+------+------+-----
			//        |      |      |
			//        *------+------*
			// (-1, -1)      |      (1, -1)
			float ratio = (float) width / height;
			projectionRect.left = -ratio;
			projectionRect.right = ratio;
		} else {
			// (-1,  1)  |  (1,  1)
			//        *--+--*
			//        |  |  |
			//   -----+--+--+-----
			//        |  |  |
			//        *--+--*
			// (-1, -1)  |  (1, -1)
			float ratio = (float) height / width;
			projectionRect.top = ratio;
			projectionRect.bottom = -ratio;
		}

		// This projection matrix is applied to object coordinates in the onDrawFrame() method,
		// such that the unit square (rect: -1, 1, 1, -1) is center inside of this GLSurfaceView.
		Matrix.frustumM(
				mProjectionMatrix,      // result
				0,                      // result offset
				projectionRect.left,    // left
				projectionRect.right,   // right
				projectionRect.bottom,  // bottom
				projectionRect.top,     // top
				3,                      // near
				7                       // far
		);

		// Set the camera position (View matrix)
		Matrix.setLookAtM(
				mViewMatrix,    // result
				0,              // result offset
				mEye.x, mEye.y, mEye.z,
				mLook.x, mLook.y, mLook.z,
				mUp.z, mUp.y, mUp.z
		);

		// Calculate the projection and view transformation
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		// Redraw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		Runnable r;
		while ((r = mRunOnDraw.poll()) != null) {
			r.run();
		}

		for (GLDrawObject drawObject : mDrawObjectList) {
			drawObject.draw(mMVPMatrix);
		}
	}

	void addDrawObject(final GLDrawObject drawObject) {
		runOnDraw(new Runnable() {
			@Override
			public void run() {
				mDrawObjectList.add(drawObject);
			}
		});
	}

	void runOnDraw(Runnable runnable) {
		mRunOnDraw.add(runnable);
	}
}
