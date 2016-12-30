/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lingyang.sdk.av.gles;

import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * This class essentially represents a sizezble sprite that will be rendered with
 * a texture, usually from an external source like the camera or video decoder.
 * <p/>
 * Placeholder. Not yet implemented.
 *
 * @hide
 */
public class TriangleFrame extends BaseFrameShape {
    private static float TEX_COORDS[] = {
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.5f, 1.0f,     // 2 top
    };
    private Texture2dProgram mProgram;

    /**
     * Prepares the object.
     *
     * @param program The program to use.  FullFrame takes ownership, and will release
     *                the program when no longer needed.
     */
    public TriangleFrame(Texture2dProgram program) {
        super(program);
        TEX_COORDS_BUF = GlUtil.createFloatBuffer(TEX_COORDS);
        mRectDrawable = new Drawable2d(Drawable2d.Prefab.TRIANGLE);
        mProgram = program;
        Matrix.setIdentityM(IDENTITY_MATRIX, 0);
    }


    /**
     * Draws a rectangle in an area defined by TEX_COORDS
     */
    public void drawFrame(float[] mvpMatrix,int textureId, float[] texMatrix) {
        // Use the identity matrix for MVP so our 2x2 FULL_RECTANGLE covers the viewport.
        mProgram.draw(mvpMatrix, mRectDrawable.getVertexArray(), 0,
                mRectDrawable.getVertexCount(), mRectDrawable.getCoordsPerVertex(),
                mRectDrawable.getVertexStride(),
                texMatrix, TEX_COORDS_BUF, textureId, TEX_COORDS_STRIDE, GLES20.GL_TRIANGLE_STRIP);
    }

}