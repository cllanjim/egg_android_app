/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.lingyang.sdk.view;

import android.content.Context;
import android.util.AttributeSet;


/**
 * Special GLSurfaceView for use with HWCameraEncoder
 * The tight coupling here allows richer touch interaction
 */
public class LYGLCameraEncoderView extends LYGLCameraView {


    public LYGLCameraEncoderView(Context context) {
        super(context);
    }

    public LYGLCameraEncoderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }



}
