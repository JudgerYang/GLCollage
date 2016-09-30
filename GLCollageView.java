package com.perfectcorp.youcamcollage.view.widget.glcollage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.perfectcorp.youcamcollage.model.Collage;
import com.perfectcorp.youcamcollage.model.Grid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GLCollageView extends GLSurfaceView {
	private final GLCollageRenderer mRenderer;
	private final GestureDetector mGestureDetector;

	private final List<TextureRectangle> mTextureRectangleList = new ArrayList<>();

	public GLCollageView(Context context) {
		this(context, null);
	}

	public GLCollageView(Context context, AttributeSet attrs) {
		super(context, attrs);

		setEGLContextClientVersion(2);
		mRenderer = new GLCollageRenderer();
		setRenderer(mRenderer);

		mGestureDetector = new GestureDetector(context, new GestureListener());
	}

	public void setImage(final int index, final Bitmap bmp) {
		mRenderer.runOnDraw(new Runnable() {
			@Override
			public void run() {
				mTextureRectangleList.get(index).setImage(bmp);
			}
		});
	}

	public void setCollage(Collage collage) {
		if (collage == null) {
			return;
		}

		if (collage.grid != null) {
			float w = 2;//getWidth();
			float h = 2;//getHeight();
			float scale = Math.max(w, h) / 1600;
			float l = -1;
			float t = 1;
			float scaledBorderThickness = collage.grid.borderThickness * scale;

			w -= scaledBorderThickness * 2f;
			h -= scaledBorderThickness * 2f;

			// Calculate the template specified total weight of cols and rows.
			float colTotalWeight = 0;
			for (float colWeight : collage.grid.cols) {
				colTotalWeight += colWeight;
			}

			float rowTotalWeight = 0;
			for (float rowWeight : collage.grid.rows) {
				rowTotalWeight += rowWeight;
			}

			List<RectF> colRectList = new ArrayList<>();
			for (int i = 0; i < collage.grid.cols.size(); ++i) {
				float colWidth = w * collage.grid.cols.get(i) / colTotalWeight;
				collage.grid.cols.set(i, colWidth);
				float left = i == 0 ? l + scaledBorderThickness : colRectList.get(i - 1).right;
				float right = left + colWidth;
				colRectList.add(new RectF(left, 0, right, 0));

				// Update back the weight in real view pixels for easy calculation in dragging divider to change column size.
				collage.grid.cols.set(i, right - left);
			}

			List<RectF> rowRectList = new ArrayList<>();
			for (int i = 0; i < collage.grid.rows.size(); ++i) {
				float rowHeight = h * collage.grid.rows.get(i) / rowTotalWeight;
				collage.grid.rows.set(i, rowHeight);
				float top = i == 0 ? t - scaledBorderThickness : rowRectList.get(i - 1).bottom;
				float bottom = top - rowHeight;
				rowRectList.add(new RectF(0, top, 0, bottom));

				// Update back the weight in real view pixels for easy calculation in dragging divider to change row size.
				collage.grid.rows.set(i, bottom - top);
			}

			final List<RectF> cellRectList = new ArrayList<>();
			for (Grid.Cell cell : collage.grid.cells) {
				if (cell == null)
					continue;

				String[] colRange = cell.col.split("-");
				String[] rowRange = cell.row.split("-");
				if (colRange.length <= 0 || rowRange.length <= 0)
					continue;

				RectF cellRect = new RectF();

				cell.colBegin = Integer.valueOf(colRange[0]);
				cell.colEnd = (colRange.length >= 2) ? Integer.valueOf(colRange[1]) : cell.colBegin;
				if (cell.colBegin >= collage.grid.cols.size() || cell.colEnd >= collage.grid.cols.size() || cell.colBegin > cell.colEnd)
					throw new IndexOutOfBoundsException("cell.col: " + cell.col + ", cols.size: " + collage.grid.cols.size());

				cell.rowBegin = Integer.valueOf(rowRange[0]);
				cell.rowEnd = (rowRange.length >= 2) ? Integer.valueOf(rowRange[1]) : cell.rowBegin;
				if (cell.rowBegin >= collage.grid.rows.size() || cell.rowEnd >= collage.grid.rows.size() || cell.rowBegin > cell.rowEnd)
					throw new IndexOutOfBoundsException("cell.row: " + cell.row + ", rows.size: " + collage.grid.rows.size());

				cell.hasLeftDivider = cell.colBegin != 0;
				cell.hasRightDivider = cell.colEnd != (collage.grid.cols.size() - 1);
				cell.hasTopDivider = cell.rowBegin != 0;
				cell.hasBottomDivider = cell.rowEnd != (collage.grid.rows.size() - 1);

				cellRect.left = colRectList.get(cell.colBegin).left;
				cellRect.right = colRectList.get(cell.colEnd).right;
				cellRect.top = rowRectList.get(cell.rowBegin).top;
				cellRect.bottom = rowRectList.get(cell.rowEnd).bottom;
				cellRectList.add(cellRect);
			}

			mRenderer.runOnDraw(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < cellRectList.size(); ++i) {
						RectF cellRect = cellRectList.get(i);
						if (mTextureRectangleList.size() > i) {
							mTextureRectangleList.get(i).setRect(cellRect);
						} else {
							try {
								TextureRectangle texRect = new TextureRectangle(getContext(), cellRect);
								mTextureRectangleList.add(texRect);
								mRenderer.addDrawObject(texRect);
							} catch (IOException ignored) {
							}
						}
					}
				}
			});
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
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			mRotation += distanceX;
			return true;
		}
	}
}
