package com.lingyang.camera.entity;

import java.io.Serializable;

/**
 * 文件名: CameraState
 * 描    述: [该类的简要描述]
 * 创建人: 杜舒
 * 创建时间: 2016/6/3
 */
public class CameraState {
    //    {
//        "p": {
//                  "play_addr": "",
//                "state": 0,
//                "cid": "42A76400A50F0C4E"
//    },
//        "t": 1
//    }
    public int t;
    public PayLoad p;

    protected int getT() {
        return t;
    }

    protected PayLoad getP() {
        return p;

    }
    public static class PayLoad implements Serializable{
        public String play_addr;
        public int state;
        public String cid;

        @Override
        public String toString() {
            return "PayLoad{" +
                    "play_addr='" + play_addr + '\'' +
                    ", state=" + state +
                    ", cid='" + cid + '\'' +
                    '}';
        }
    }
}
