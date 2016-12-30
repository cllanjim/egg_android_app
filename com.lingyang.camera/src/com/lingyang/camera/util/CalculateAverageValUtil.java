package com.lingyang.camera.util;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.executor.queue.LinkedBlockingQueue;

public class CalculateAverageValUtil {
    LinkedBlockingQueue<Long> mQueue;
    int mCapacity;

    public CalculateAverageValUtil(int capacity) {
        mCapacity = capacity;
        mQueue = new LinkedBlockingQueue<Long>(capacity);
    }

    public void putValue(long val) throws InterruptedException {
        CLog.v("putValue -" + val + " mQueue.size()-" + mQueue.size());
        if (mQueue.size() == mCapacity) {
            CLog.v("poll-" + mQueue.poll());
        }
        CLog.v("offer-" + mQueue.offer(val));
    }

    public int getAverageVal() {
        int total = 0;
        for (Long aMQueue : mQueue) {
            total += aMQueue;
        }
        return total / mQueue.size();
    }
}
