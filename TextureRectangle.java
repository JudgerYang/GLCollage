package com.perfectcorp.youcamcollage.view.widget.glcollage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.perfectcorp.youcamcollage.R;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

class TextureRectangle extends GLDrawObject {
	private static final int COORDINATES_PER_TEXTURE_VERTEX = 2;

	private static final short DRAW_ORDER[] = {
			0, 1, 2,    // 1st triangle
			0, 2, 3,    // 2nd triangle
	};
	private static final RectF ORIGINAL_TEXTURE_RECT = new RectF(0, 1, 1, 0);
	private RectF mTextureRect = new RectF(ORIGINAL_TEXTURE_RECT);

	private ShortBuffer mDrawListBuffer;
	private FloatBuffer mTextureVertexBuffer;

	private final int mTextureId;
	private int mTextureCoordinatesHandle;
	private int mTextureHandle;

	private Runnable mSetImageRun = null;

	TextureRectangle(Context context, RectF rect) throws IOException {
		super(context, convertCoordinates(rect));

		int[] textures = new int[1];
		GLES20.glGenTextures(textures.length, textures, 0);
		mTextureId = textures[0];

		mTextureCoordinatesHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
		GLES20.glEnableVertexAttribArray(mTextureCoordinatesHandle);

		setTextureCoordinates(convertTextureCoordinates(mTextureRect));

		mTextureHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");

		ByteBuffer dlb = ByteBuffer.allocateDirect(DRAW_ORDER.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		mDrawListBuffer = dlb.asShortBuffer();
		mDrawListBuffer.put(DRAW_ORDER);
		mDrawListBuffer.position(0);
	}

	private static float[] convertCoordinates(RectF rect) {
		return new float[]{
				rect.left,  rect.top,    0f,
				rect.left,  rect.bottom, 0f,
				rect.right, rect.bottom, 0f,
				rect.right, rect.top,    0f,
		};
	}

	private static float[] convertTextureCoordinates(RectF rect) {
		return new float[]{
				rect.left,  rect.bottom,
				rect.left,  rect.top,
				rect.right, rect.top,
				rect.right, rect.bottom,
		};
	}

	public void setImage(final Bitmap bmp) {
		// Place the texture center-inside the rectangle.
		mTextureRect = new RectF(ORIGINAL_TEXTURE_RECT);
		float w = bmp.getWidth();
		float h = bmp.getHeight();
		float dx = 0, dy = 0;
		if (w > h) {
			dx = (w - h) / 2 / w;
		} else {
			dy = (h - w) / 2 / h;
		}

		mTextureRect.left += dx;
		mTextureRect.right -= dx;
		mTextureRect.bottom += dy;
		mTextureRect.top -= dy;

		setTextureCoordinates(convertTextureCoordinates(mTextureRect));

		mSetImageRun = new Runnable() {
			@Override
			public void run() {
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

				GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);
			}
		};
	}

	private float mDegree = 30;
	void setRotation(float degree) {
		mDegree = degree;
	}

	private void setTextureCoordinates(float[] textureCoordinates) {
		mTextureVertexBuffer = GLUtility.generateVertexBuffer(textureCoordinates);
	}

	@Override
	protected String getVertexShaderCode() {
		return readShaderFromResource(R.raw.shader_texture_rectangle_vertex);
	}

	@Override
	protected String getFragmentShaderCode() {
		return readShaderFromResource(R.raw.shader_texture_rectangle_fragment);
	}

	@Override
	protected void onDraw() {
		Runnable setImageRun = mSetImageRun;
		mSetImageRun = null;
		if (setImageRun != null) {
			setImageRun.run();
		}

		GLES20.glEnable(GL10.GL_TEXTURE_2D);
		// Enable the texture state
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

		// Point to our buffers
		GLES20.glVertexAttribPointer(mTextureCoordinatesHandle, 2, GLES20.GL_FLOAT, false, COORDINATES_PER_TEXTURE_VERTEX * GLUtility.BYTES_PER_FLOAT, mTextureVertexBuffer);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

		GLES20.glUniform1i(mTextureHandle, 0);

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, DRAW_ORDER.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
	}
}
