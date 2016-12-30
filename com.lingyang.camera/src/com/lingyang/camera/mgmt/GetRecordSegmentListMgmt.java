package com.lingyang.camera.mgmt;

import com.google.gson.Gson;
import com.lingyang.base.utils.CLog;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.entity.RecordSection;
import com.lingyang.camera.util.HttpUtil;


/**
 * 文件名: GetRecordSegmentListMgmt
 * 描    述: [该类的简要描述]
 * 创建人: 杜舒
 * 创建时间: 2016/5/31
 */
public class GetRecordSegmentListMgmt {
    private static RecordSection mRecordSection;

    public GetRecordSegmentListMgmt() {
    }

    //    uname	string	用户ID	“Test0c61baf8”
//    expire	int	token 过期时间戳	1429846954
//    access_token	string	授权后的令牌	“bbd37875430a…”
//    cid	string	摄像头 ID	“4C6D9400C70F154E”
//    start	int	起始时间	0
//    end
    public static void getRecordSegmentList(long start, long end,
                                            String uname, String cid,
                                            String expire, String access_token,
                                            final IRecordSegmentListCallBackListener
                                                    recordSectionCallBackListener) {
        String url;
        if (start == 0 || end == 0) {
            url = String.format("%s/v1/%s/cameras/videos?expire=%s&access_token=%s",
                    Const.APP_SERVER_HOST, Const.TOPVDN_CLOUD_APPID, expire, access_token);
        } else {
            url = String.format("%s/v1/%s/camera/videos?start=%s&end=%s&uname=%s&cid=%s&expire=%s&access_token=%s",
                    Const.APP_SERVER_HOST, Const.TOPVDN_CLOUD_APPID, start, end, uname, cid, expire, access_token);
        }
//        url = "http://api.topvdn.com/v2/record/1003175/timeline?client_token=1003175_3356491776_1470897351_837bc93f852f1965e996fec054c7ee9d&start=1470758400&end=1470810957";
        CLog.d("---" + url);
        HttpUtil.CallBack callback = new HttpUtil.CallBack() {
            @Override
            public void onRequestComplete(String result) {
                if (recordSectionCallBackListener != null) {
                    if (result != null && result.length() != 0) {
                        Gson gson = new Gson();
//                        mRecordSection = gson.fromJson("{\n" +
//                                "    \"cid\": 1003175,\n" +
//                                "    \"events\": [\n" +
//                                "        {\n" +
//                                "            \"begin\": 1470803970,\n" +
//                                "            \"end\": 1470804600,\n" +
//                                "            \"url\": \"http://hls6.public.topvdn.cn/hls/1003136/index.m3u8\"\n" +
//                                "        }\n" +
//                                "    ],\n" +
//                                "    \"request_id\": \"5cdebe33c9ed48aa814e8276853b678c\",\n" +
//                                "    \"servers\": [\n" +
//                                "        {\n" +
//                                "            \"ip\": \"183.57.151.208\",\n" +
//                                "            \"port\": 80\n" +
//                                "        }\n" +
//                                "    ],\n" +
//                                "    \"videos\": [\n" +
//                                "        {\n" +
//                                "            \"from\": 1470798510,\n" +
//                                "            \"server_index\": 0,\n" +
//                                "            \"to\": 1470802320\n" +
//                                "        },\n" +
//                                "        {\n" +
//                                "            \"from\": 1470802350,\n" +
//                                "            \"server_index\": 0,\n" +
//                                "            \"to\": 1470803940\n" +
//                                "        },\n" +
//                                "        {\n" +
//                                "            \"from\": 1470803970,\n" +
//                                "            \"server_index\": 0,\n" +
//                                "            \"to\": 1470804600\n" +
//                                "        },\n" +
//                                "        {\n" +
//                                "            \"from\": 1470804660,\n" +
//                                "            \"server_index\": 0,\n" +
//                                "            \"to\": 1470806340\n" +
//                                "        },\n" +
//                                "        {\n" +
//                                "            \"from\": 1470806370,\n" +
//                                "            \"server_index\": 0,\n" +
//                                "            \"to\": 1470806520\n" +
//                                "        },\n" +
//                                "        {\n" +
//                                "            \"from\": 1470806610,\n" +
//                                "            \"server_index\": 0,\n" +
//                                "            \"to\": 1470806670\n" +
//                                "        },\n" +
//                                "        {\n" +
//                                "            \"from\": 1470806700,\n" +
//                                "            \"server_index\": 0,\n" +
//                                "            \"to\": 1470807030\n" +
//                                "        },\n" +
//                                "        {\n" +
//                                "            \"from\": 1470807240,\n" +
//                                "            \"server_index\": 0,\n" +
//                                "            \"to\": 1470807450\n" +
//                                "        },\n" +
//                                "        {\n" +
//                                "            \"from\": 1470807570,\n" +
//                                "            \"server_index\": 0,\n" +
//                                "            \"to\": 1470807600\n" +
//                                "        },\n" +
//                                "        {\n" +
//                                "            \"from\": 1470807660,\n" +
//                                "            \"server_index\": 0,\n" +
//                                "            \"to\": 1470807960\n" +
//                                "        },\n" +
//                                "        {\n" +
//                                "            \"from\": 1470808110,\n" +
//                                "            \"server_index\": 0,\n" +
//                                "            \"to\": 1470808140\n" +
//                                "        },\n" +
//                                "        {\n" +
//                                "            \"from\": 1470808290,\n" +
//                                "            \"server_index\": 0,\n" +
//                                "            \"to\": 1470808560\n" +
//                                "        },\n" +
//                                "        {\n" +
//                                "            \"from\": 1470808620,\n" +
//                                "            \"server_index\": 0,\n" +
//                                "            \"to\": 1470808770\n" +
//                                "        },\n" +
//                                "        {\n" +
//                                "            \"from\": 1470808800,\n" +
//                                "            \"server_index\": 0,\n" +
//                                "            \"to\": 1470808830\n" +
//                                "        },\n" +
//                                "        {\n" +
//                                "            \"from\": 1470808890,\n" +
//                                "            \"server_index\": 0,\n" +
//                                "            \"to\": 1470808920\n" +
//                                "        }\n" +
//                                "    ]\n" +
//                                "}", RecordSection.class);
                        mRecordSection = gson.fromJson(result,RecordSection.class);
                        recordSectionCallBackListener.onResult(mRecordSection);
                    } else {
                        recordSectionCallBackListener.onResult(null);
                    }
                }
            }
        };
        HttpUtil.doGetAsyn(url, callback);

    }

    public interface IRecordSegmentListCallBackListener {
        void onResult(RecordSection result);
    }
}
