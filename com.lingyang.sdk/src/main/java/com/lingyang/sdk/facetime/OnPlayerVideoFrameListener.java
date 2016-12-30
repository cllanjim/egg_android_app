package com.lingyang.sdk.facetime;

import java.nio.ByteBuffer;

/**
 * Created by liaolei on 16/7/20.
 */
public interface OnPlayerVideoFrameListener {

    int onVideoFrameAvailable(ByteBuffer data, int offset, int size, int width, int height, long timestamp);
}
