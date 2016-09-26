package com.perfectcorp.youcamcollage.view.widget.glcollage;

import java.io.IOException;

class Triangle extends GLDrawObject {
	private static float coordinates[] = {   // in counterclockwise order:
			0.0f,  0.622008459f, 0.0f, // top
			-0.5f, -0.311004243f, 0.0f, // bottom left
			0.5f, -0.311004243f, 0.0f  // bottom right
//			-1.0f,  1.0f, 0.0f, // top
//			-1.0f, -1.0f, 0.0f, // bottom left
//			 1.0f, -1.0f, 0.0f  // bottom right
	};

	// Set color with red, green, blue and alpha (opacity) values
	private static float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

	Triangle() throws IOException {
		super(coordinates, color);
	}
}
