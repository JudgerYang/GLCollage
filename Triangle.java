package com.perfectcorp.youcamcollage.view.widget.glcollage;

import android.opengl.Matrix;
import android.os.SystemClock;

import java.io.IOException;

class Triangle extends GLDrawObject {
	private static float coordinates[] = {   // in counterclockwise order:
			 0.0f,  1.0f, 0.0f, // top
			-1.0f, -1.0f, 0.0f, // bottom left
			 1.0f, -1.0f, 0.0f  // bottom right
	};

	// Set color with red, green, blue and alpha (opacity) values
	private static float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
	private float[] mRotationMatrix = new float[16];

	Triangle() throws IOException {
		super(coordinates, color);
	}

	@Override
	public void draw(float[] mvpMatrix) {
		float[] scratch = new float[16];

		// Create a rotation transformation for the triangle
		long time = SystemClock.uptimeMillis() % 4000L;
		float angle = 0.090f * ((int) time);
		Matrix.setRotateM(
				mRotationMatrix, 0, // result, result offset
				angle,
				0.0f,   // x
				0.0f,   // y
				1.0f    // z
		);

		// Combine the rotation matrix with the projection and camera view
		// Note that the mMVPMatrix factor *must be first* in order
		// for the matrix multiplication product to be correct.
		Matrix.multiplyMM(scratch, 0, mvpMatrix, 0, mRotationMatrix, 0);

		super.draw(scratch);
	}
}
