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

import java.nio.FloatBuffer;

/**
 * This class essentially represents a viewport-sized sprite that will be rendered with
 * a texture, usually from an external source like the camera or video decoder.
 *
 * @hide
 */
public class CircleFrame extends BaseFrameShape {

    /**
     * Prepares the object.
     *
     * @param program The program to use.  FullFrame takes ownership, and will release
     *                the program when no longer needed.
     */
    public CircleFrame(Texture2dProgram program) {
        super(program);
        TEX_COORDS_BUF = getBufCoords();
        mRectDrawable = new Drawable2d(Drawable2d.Prefab.CIRCLE);
        mProgram = program;
        Matrix.setIdentityM(IDENTITY_MATRIX, 0);
    }

    private FloatBuffer getBufCoords() {
        int vertexCount = 100;
        float radius = 0.5f;
        float center_x = 0.5f;
        float center_y = 0.5f;

        // Create a buffer for vertex data
        // (x,y) for each vertex
        float buffer[] = new float[vertexCount * 2];
        int idx = 0;

        // Center vertex for triangle fan
        buffer[idx++] = center_x;
        buffer[idx++] = center_y;

        // Outer vertices of the circle
        int outerVertexCount = vertexCount - 1;

        for (int i = 0; i < outerVertexCount; ++i) {
            float percent = (i / (float) (outerVertexCount - 1));
            float rad = (float) (percent * 2 * Math.PI);//计算每个部分的弧度

            //Vertex position
            float outer_x = (float) (center_x + radius * Math.cos(rad));
            float outer_y = (float) (center_y + radius * Math.sin(rad));

            buffer[idx++] = outer_x;
            buffer[idx++] = outer_y;
        }
        return GlUtil.createFloatBuffer(buffer);
    }

    /**
     * Draws a viewport-filling rect, texturing it with the specified texture object.
     */
    public void drawFrame(float[] mvpMatrix,int textureId, float[] texMatrix) {
        // Use the identity matrix for MVP so our 2x2 FULL_RECTANGLE covers the viewport.
        synchronized (mDrawLock) {
//            if (mCorrectVerticalVideo && !mScaleToFit &&
//                    (requestedOrientation == SCREEN_ROTATION.VERTICAL
//                            || requestedOrientation == SCREEN_ROTATION.UPSIDEDOWN_VERTICAL)) {
//                Matrix.scaleM(texMatrix, 0, 0.316f, 1.0f, 1f);
//            }
            mProgram.draw(mvpMatrix, mRectDrawable.getVertexArray(), 0,
                    mRectDrawable.getVertexCount(), mRectDrawable.getCoordsPerVertex(),
                    mRectDrawable.getVertexStride(),
                    texMatrix, TEX_COORDS_BUF, textureId, TEX_COORDS_STRIDE, GLES20.GL_TRIANGLE_FAN);
        }
    }


}