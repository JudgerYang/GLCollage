package com.perfectcorp.youcamcollage.view.widget.glcollage;

import android.opengl.GLES20;

import java.io.IOException;
import java.nio.FloatBuffer;

import static com.perfectcorp.youcamcollage.view.widget.glcollage.GLUtility.BYTES_PER_FLOAT;

class GLDrawObject {
	private static final int COORDINATES_PER_VERTEX = 3;

	private static final String vertexShaderCode =
			"uniform mat4 uMVPMatrix;" +
			"attribute vec4 vPosition;" +
			"void main() {" +
			"  gl_Position = uMVPMatrix * vPosition;" +
			"}";

	private static final String fragmentShaderCode =
			"precision mediump float;" +
			"uniform vec4 vColor;" +
			"void main() {" +
			"  gl_FragColor = vColor;" +
			"}";

	private final FloatBuffer mVertexBuffer;
	private final float mColor[];

	private final int mProgram;

	private final int vertexCount;

	GLDrawObject(float[] vertexCoordinates, float[] color) throws IOException {
		int vertexShader = GLUtility.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		int fragmentShader = GLUtility.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

		mProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(mProgram, vertexShader);
		GLES20.glAttachShader(mProgram, fragmentShader);
		GLES20.glLinkProgram(mProgram);

		vertexCount = vertexCoordinates.length / COORDINATES_PER_VERTEX;

		mVertexBuffer = GLUtility.generateVertexBuffer(vertexCoordinates);

		mColor = color;
	}

	public void draw(float[] mvpMatrix) {
		// Add program to OpenGL ES environment
		GLES20.glUseProgram(mProgram);

		// get handle to vertex shader's vPosition member
		int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(positionHandle);

		// Prepare the triangle coordinate data
		int vertexStride = COORDINATES_PER_VERTEX * BYTES_PER_FLOAT;
		GLES20.glVertexAttribPointer(positionHandle, COORDINATES_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);

		int colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
		GLES20.glUniform4fv(colorHandle, 1, mColor, 0);

		int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

		// Draw the triangle
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(positionHandle);
	}
}
