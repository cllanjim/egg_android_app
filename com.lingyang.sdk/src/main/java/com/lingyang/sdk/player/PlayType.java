/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.lingyang.sdk.player;

import com.antelope.sdk.streamer.ACProtocolType;
import com.lingyang.sdk.util.Preconditions;
import com.lingyang.sdk.util.SchemeUtil;

/**
 * @author liaolei
 */
public class PlayType {

    public static int getPlayType(String url) {
        int playType=0;
        if (url == null || url.trim().length() == 0) {
            playType = 0;
        } else {
            Preconditions.checkNotNull(SchemeUtil.getPathScheme(url));
            if (SchemeUtil.getPathScheme(url).equalsIgnoreCase(IPlayer.SCHEME_TOPVDN)) {
                int type = Integer.parseInt(SchemeUtil.getParamValue(url, IPlayer.PARAM_TYPE));
                if (type == IPlayer.TYPE_QSTP) {
                    playType = ACProtocolType.AC_PROTOCOL_QSTP;
                } else if (type == IPlayer.TYPE_QSUP) {
                    playType = ACProtocolType.AC_PROTOCOL_QSUP;
                } else if (type == IPlayer.TYPE_RECORD) {
                    playType = ACProtocolType.AC_PROTOCOL_RECORD;
                } 
            }  else if (SchemeUtil.getPathScheme(url).equalsIgnoreCase(IPlayer.SCHEME_RTMP)) {
                playType = ACProtocolType.AC_PROTOCOL_QSTP;
            } 
        }
        return playType;
    }
    
}
