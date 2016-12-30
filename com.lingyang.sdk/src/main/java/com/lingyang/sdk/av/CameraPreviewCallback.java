package com.lingyang.sdk.av;

import java.nio.ByteBuffer;

/**
 * Created by liaolei on 16/7/20.
 */
public interface CameraPreviewCallback {

    public static final int KEY_SEND_DATA_TYPE_ORIGINAL = 101;

    public static final int LEY_SEND_DATA_TYPE_PROCESSED = 102;

    public static final int IMAGE_FORMAT_PCM      = 1;
    public static final int IMAGE_FORMAT_YUV420P  = 2;
    public static final int IMAGE_FORMAT_YUV12    = 4;
    public static final int IMAGE_FORMAT_AUDIO    = 8;
    public static final int IMAGE_FORMAT_AAC      = 9;
    public static final int IMAGE_FORMAT_OPUS     = 10;
    public static final int IMAGE_FORMAT_H264     = 16;
    void onCameraPreviewFrame(ByteBuffer data, PreviewDataInfoEntity dataInfo);

}
