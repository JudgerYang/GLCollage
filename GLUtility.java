package com.perfectcorp.youcamcollage.view.widget.glcollage;

import android.opengl.GLES20;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

class GLUtility {
	static final int BYTES_PER_FLOAT = 4;
	private static final int BYTES_PER_SHORT = 2;

	// 0------3
	// |      |
	// |      |
	// 1----- 2
	// Triangle 1: v1 -> v2 -> v3
	// Triangle 2: v1 -> v3 -> v4
	public static final float[] UNIT_SQUARE = {
			-1.0f,  1.0f, 0.0f,
			-1.0f, -1.0f, 0.0f,
			 1.0f, -1.0f, 0.0f,
			 1.0f,  1.0f, 0.0f,
	};
	public static final short[] UNIT_SQUARE_DRAW_ORDER = {0, 1, 2, 0, 2, 3};

	public static FloatBuffer generateVertexBuffer(float[] coordinates) {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(coordinates.length * BYTES_PER_FLOAT);
		byteBuffer.order(ByteOrder.nativeOrder());

		FloatBuffer vertexBuffer = byteBuffer.asFloatBuffer();
		vertexBuffer.put(coordinates);

		// set the buffer to read the first coordinate
		vertexBuffer.position(0);

		return vertexBuffer;
	}

	public static ShortBuffer generateDrawOrderBuffer(short[] drawOrder) {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(drawOrder.length * BYTES_PER_SHORT);
		byteBuffer.order(ByteOrder.nativeOrder());

		ShortBuffer drawOrderBuffer = byteBuffer.asShortBuffer();
		drawOrderBuffer.put(drawOrder);

		// set the buffer to read the first coordinate
		drawOrderBuffer.position(0);

		return drawOrderBuffer;
	}

	@IntDef({GLES20.GL_VERTEX_SHADER, GLES20.GL_FRAGMENT_SHADER})
	@Retention(RetentionPolicy.SOURCE)
	@interface ShaderType {}
	static int loadShader(@ShaderType int type, String shaderCode){
		int shader = GLES20.glCreateShader(type);

		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		return shader;
	}
}
