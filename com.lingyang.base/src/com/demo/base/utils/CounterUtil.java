package com.lingyang.base.utils;

import com.lingyang.base.utils.CLog;

public class CounterUtil {
	
        long startTime = System.nanoTime();
        int frames = 0;
        
       /**
        * 计算每秒执行了多少次
        */
        public void logCount() {
            frames++;
            if(System.nanoTime() - startTime >= 1000000000) {
                CLog.d("FPSCounter-fps: " + frames);
                frames = 0;
                startTime = System.nanoTime();
            }
        }
   
}
