package com.lingyang.sdk.av;

import android.hardware.Camera;

import java.util.List;

/**
 * 手机直播
 *
 * @author 波
 */
public interface ICameraOperation {

    /**
     * Request a Camera by cameraId. This will take effect immediately
     * or as soon as the camera preview becomes active.
     * <p/>
     * Called from UI thread
     *
     * @param camera
     */
    void setCameraType(int camera);

    /**
     * 切换至另一个的摄像头
     */
    void switchCamera();

    /**
     * 设置闪光类型
     *
     * @param desiredFlash {@link Camera.Parameters#FLASH_MODE_TORCH}
     *                     or {@link Camera.Parameters#FLASH_MODE_OFF} etc
     */
    void setFlashMode(String desiredFlash);

    /**
     * 获取当前摄像机编号
     * @return
     */
    int getCurrentCamera();

    /**
     * 获取期望的摄像机编号
     * @return
     */
    int getDesiredCamera();

    /**
     * 切换闪光类型
     * 在  TORCH 之间 OFF切换，设置效果在预览状态下会立刻生效
     */
    void toggleFlashMode();

    void toggleFlash();

    /**
     * 返回闪光模式
     * @return
     */
    String getFlashMode();

    /**
     * 获取当前摄像机支持的预览列表
     */
    List<Camera.Size> getSupportedPreviewSizes();

}
