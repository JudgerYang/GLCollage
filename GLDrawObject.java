package com.perfectcorp.youcamcollage.view.widget.glcollage;

import android.content.Context;
import android.opengl.GLES20;

import com.google.common.base.Charsets;
import com.google.common.io.Closer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import static com.perfectcorp.youcamcollage.view.widget.glcollage.GLUtility.BYTES_PER_FLOAT;

abstract class GLDrawObject {
	private static final int COORDINATES_PER_VERTEX = 3;
	private static final int VERTEX_STRIDE = COORDINATES_PER_VERTEX * BYTES_PER_FLOAT;

	private final Context mContext;

	private final FloatBuffer mVertexBuffer;

	// GL handles
	final int mProgram;
	private final int mPositionHandle;
	private final int mMVPMatrixHandle;

	private final int mVertexCount;

	GLDrawObject(Context context, float[] vertexCoordinates) throws IOException {
		mContext = context;

		int vertexShader = GLUtility.loadShader(GLES20.GL_VERTEX_SHADER, getVertexShaderCode());
		int fragmentShader = GLUtility.loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShaderCode());

		mProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(mProgram, vertexShader);
		GLES20.glAttachShader(mProgram, fragmentShader);
		GLES20.glLinkProgram(mProgram);

		// get handle to vertex shader's vPosition member
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

		mVertexCount = vertexCoordinates.length / COORDINATES_PER_VERTEX;

		mVertexBuffer = GLUtility.generateVertexBuffer(vertexCoordinates);
	}

	abstract protected String getVertexShaderCode();

	abstract protected String getFragmentShaderCode();

	/**
	 * Override this method to do mvpMatrix transformation.
	 * @param mvpMatrix the matrix to be transformed.
	 * @return transformed matrix.
	 */
	protected float[] transformMVPMatrix(float[] mvpMatrix) {
		return mvpMatrix;
	}

	/**
	 * Override this method to do customized draw.
	 */
	protected void onDraw() {
		// Draw the triangle
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount);
	}

	public final void draw(float[] mvpMatrix) {
		mvpMatrix = transformMVPMatrix(mvpMatrix);

		// Add program to OpenGL ES environment
		GLES20.glUseProgram(mProgram);

		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, COORDINATES_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

		onDraw();

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
	}

	String readShaderFromResource(int resId) {
		com.google.common.io.Closer closer = Closer.create();
		InputStream is = closer.register(mContext.getResources().openRawResource(resId));
		try {
			return com.google.common.io.CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
		} catch (IOException ignored) {
			return "";
		} finally {
			try {
				closer.close();
			} catch (IOException ignored) {
			}
		}
	}
}
