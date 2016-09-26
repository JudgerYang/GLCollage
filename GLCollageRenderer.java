package com.perfectcorp.youcamcollage.view.widget.glcollage;

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

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// Set the background frame color
		GLES20.glClearColor(0.3f, 0.0f, 0.0f, 1.0f);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);

		float ratio = (float) width / height;

		// this projection matrix is applied to object coordinates in the onDrawFrame() method
		Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		// Redraw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		Runnable r;
		while ((r = mRunOnDraw.poll()) != null) {
			r.run();
		}

		// Set the camera position (View matrix)
		Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

		// Calculate the projection and view transformation
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

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
