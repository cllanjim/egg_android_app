package com.lingyang.camera.entity;

import com.google.gson.Gson;
import com.lingyang.base.utils.CLog;

import java.io.Serializable;
import java.util.List;

public class RecordSection extends BaseResponse{

    public RecordSection() {
    }

    public String request_id;
    public String play_addr;
    public String cid;
    public List<Section> videos;
    public List<Server> servers;
    public List<Event> events;

    public List<Event> getEvents() {
        return events;
    }

    public List<Section> getSection() {
        for (Section section :
                videos) {
            section.setLocalServers(servers);
        }
        return videos;
    }

    public static class Server implements Serializable {
        public Server() {
        }

        public String ip;
        public Short port;

    }
    public static class Event implements Serializable{
        public Event() {
        }
        public long begin;
        public long end;
        public String url;
        private Event(long begin,long end,String url){
            this.begin=begin;
            this.end = end;
            this.url = url;
        }
        public Event getNewEvent(long begin, long end) {
            return new Event(begin, end, this.url);
        }
    }

    public String toGson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static class Section implements Serializable {

        public Section() {
        }

        public long from;
        public long to;
        private Byte server_index;
        protected List<Server> localServers;

        public void setLocalServers(List<Server> serverList) {
            localServers = serverList;
        }

        private Section(long from, long to, int serverIndex) {
            this.from = from;
            this.to = to;
            this.server_index = server_index;
        }

        public String getImageRootUrl() {
            if (localServers != null && server_index <= localServers.size()) {
                CLog.v("localServers:" + localServers);
                return String.format("http://%s:%s/", localServers.get(server_index).ip, localServers.get(server_index).port);
            } else
                return null;
        }

        public Section getNewSection(long from, long to) {
            return new Section(from, to, this.server_index);
        }

    }

}
