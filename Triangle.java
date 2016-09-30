package com.perfectcorp.youcamcollage.view.widget.glcollage;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.perfectcorp.youcamcollage.R;

import java.io.IOException;

class Triangle extends GLDrawObject {
	private static float coordinates[] = {   // in counterclockwise order:
			 0.0f,  1.0f, 0.0f, // top
			-1.0f, -1.0f, 0.0f, // bottom left
			 1.0f, -1.0f, 0.0f  // bottom right
	};

	// GL handles
	private final int mColorHandle;

	// Set color with red, green, blue and alpha (opacity) values
	private final float mColor[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
	private float[] mRotationMatrix = new float[16];

	Triangle(Context context) throws IOException {
		super(context, coordinates);

		// Bind custom attributes.
		mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

		setRotation(0);
	}

	void setRotation(float angle) {
		Matrix.setRotateM(
				mRotationMatrix, 0, // result, result offset
				angle,
				0.0f,   // x
				0.0f,   // y
				1.0f    // z
		);
	}

	@Override
	protected String getVertexShaderCode() {
		return readShaderFromResource(R.raw.shader_triangle_vertex);
	}

	@Override
	protected String getFragmentShaderCode() {
		return readShaderFromResource(R.raw.shader_triangle_fragment);
	}

	@Override
	protected float[] transformMVPMatrix(float[] mvpMatrix) {
		float[] scratch = new float[16];

		// Combine the rotation matrix with the projection and camera view
		// Note that the mMVPMatrix factor *must be first* in order
		// for the matrix multiplication product to be correct.
		Matrix.multiplyMM(scratch, 0, mvpMatrix, 0, mRotationMatrix, 0);

		return scratch;
	}

	@Override
	protected void onDraw() {
		GLES20.glUniform4fv(mColorHandle, 1, mColor, 0);
		super.onDraw();
	}
}
