/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.lingyang.sdk.player;

public class PlayOptions {

    private int minAtlpBufferTime;
    private int maxAtlpBufferTime;
    private int minAulpBufferTime;
    private int maxAulpBufferTime;
    private boolean mIsMute;

    public PlayOptions() {
    }

    public int getMinAtlpBufferTime() {
        return minAtlpBufferTime;
    }

    public void setMinAtlpBufferTime(int time) {
        minAtlpBufferTime = time;
    }

    public int getMaxAtlpBufferTime() {
        return maxAtlpBufferTime;
    }

    public void setMaxAtlpBufferTime(int time) {
        maxAtlpBufferTime = time;
    }

    public int getMinAulpBufferTime() {
        return minAulpBufferTime;
    }

    public void setMinAulpBufferTime(int time) {
        minAulpBufferTime = time;
    }

    public int getMaxAulpBufferTime() {
        return maxAulpBufferTime;
    }

    public void setMaxAulpBufferTime(int time) {
        maxAulpBufferTime = time;
    }

    public void setMute(boolean isMute) {
        mIsMute = isMute;
    }

    public boolean IsMute() {
        return mIsMute;
    }

    public static class Builder {
        private int minAtlpBufferTime;
        private int maxAtlpBufferTime;
        private int minAulpBufferTime;
        private int maxAulpBufferTime;
        private boolean mIsMute;

        public Builder() {
            setDefaultOption();
        }

        private void setDefaultOption() {
            minAtlpBufferTime = 1000;
            maxAtlpBufferTime = 5000;
            minAulpBufferTime = 100;
            maxAulpBufferTime = 5000;
            mIsMute = false;
        }

        public Builder withMinAtlpBufferTime(int time) {
            minAtlpBufferTime = time;
            return this;
        }

        public Builder withMaxAtlpBufferTime(int time) {
            maxAtlpBufferTime = time;
            return this;
        }

        public Builder withMinAulpBufferTime(int time) {
            minAulpBufferTime = time;
            return this;
        }

        public Builder withMaxAulpBufferTime(int time) {
            maxAulpBufferTime = time;
            return this;
        }

        public Builder withMute(boolean isMute) {
            mIsMute = isMute;
            return this;
        }

        public PlayOptions build() {
            PlayOptions session = new PlayOptions();
            session.setMaxAtlpBufferTime(maxAtlpBufferTime);
            session.setMinAtlpBufferTime(minAtlpBufferTime);
            session.setMaxAulpBufferTime(maxAulpBufferTime);
            session.setMinAulpBufferTime(minAulpBufferTime);
            session.setMute(mIsMute);
            return session;
        }

    }
}
