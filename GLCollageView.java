package com.perfectcorp.youcamcollage.view.widget.glcollage;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import java.io.IOException;

public class GLCollageView extends GLSurfaceView {
	private final GLCollageRenderer mRenderer;

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
					GLDrawObject triangle = new Triangle();
					mRenderer.addDrawObject(triangle);
				} catch (IOException ignored) {
				}
			}
		});

		setRenderer(mRenderer);
	}
}
