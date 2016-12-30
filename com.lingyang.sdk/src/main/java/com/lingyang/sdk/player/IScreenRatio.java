package com.lingyang.sdk.player;

/**
 * 视频比例缩放转化
 */
public interface IScreenRatio {
    /**
     * 视频等比最佳
     */
    int TYPE_PLAYER_RATIO_PROP_BEST = 0;
    /**
     * 视频等比全屏
     */
    int TYPE_PLAYER_RATIO_PROP_FULL = 1;
    /**
     * 16x9
     */
    int TYPE_PLAYER_RATIO_PROP_16X9 = 2;
    /**
     * 5x4
     */
    int TYPE_PLAYER_RATIO_PROP_5X4 = 3;
    /**
     * 4x3
     */
    int TYPE_PLAYER_RATIO_PROP_4X3 = 4;

    /**
     * 3x4
     */
    int TYPE_PLAYER_RATIO_PROP_3X4 = 5;
    /**
     * 9x16
     */
    int TYPE_PLAYER_RATIO_PROP_9X16 = 6;

    /**
     * 设置播放器显示比例
     *
     * @param ratioType {@link #TYPE_PLAYER_RATIO_PROP_BEST}
     *                   {@link #TYPE_PLAYER_RATIO_PROP_FULL}
     *                   {@link #TYPE_PLAYER_RATIO_PROP_16X9}
     *                   or  {@link #TYPE_PLAYER_RATIO_PROP_4X3}
     *                   or  {@link #TYPE_PLAYER_RATIO_PROP_FULL}
     *                   or  {@link #TYPE_PLAYER_RATIO_PROP_9X16}
     *                   or  {@link #TYPE_PLAYER_RATIO_PROP_3X4}
     */
    void setScreenRatio(int ratioType);

}
