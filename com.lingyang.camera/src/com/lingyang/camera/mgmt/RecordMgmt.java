package com.lingyang.camera.mgmt;


import com.lingyang.camera.entity.RecordSection;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件名: RecordMgmt
 * 描    述: 根据日期 小时 时间戳获取录像时间片
 * 创建人:刘波
 * 创建时间: 2015/9
 */
public class RecordMgmt {

    /**
     * 根据日期小时获取录像时间片
     */
    public static List<RecordSection.Section> getSectionListByTimeStamp(RecordSection recordSection, long from, long to) {
        List<RecordSection.Section> list = new ArrayList<RecordSection.Section>();
        List<RecordSection.Section> sections = recordSection.getSection();
        for (RecordSection.Section section : sections) {
            list.addAll(getSectionListByHourFromTimeSpan(section, from, to));
        }
        return list;
    }
    private static List<RecordSection.Section> getSectionListByHourFromTimeSpan(RecordSection.Section section, long from, long to) {
        List<RecordSection.Section> list = new ArrayList<RecordSection.Section>();
        if (section.from >= from && section.to <= to) {
            list.add(section.getNewSection(section.from, section.to));
        } else if (section.from <= from && section.to >= from && section.to <= to) {
            list.add(section.getNewSection(from, section.to));
        } else if (section.from <= from && section.to >= to) {
            list.add(section.getNewSection(from, to));
        } else if (section.from >= from && section.from <= to && section.to >= to) {
            list.add(section.getNewSection(section.from, to));
        }
        return list;
    }


    public static List<RecordSection.Event> getEventListByTimeStamp(RecordSection recordSection, long from, long to) {
        List<RecordSection.Event> list = new ArrayList<RecordSection.Event>();
        List<RecordSection.Event> sections = recordSection.getEvents();
        for (RecordSection.Event event : sections) {
            list.addAll(getEventListByHourFromTimeSpan(event, from, to));
        }
        return list;
    }


    private static List<RecordSection.Event> getEventListByHourFromTimeSpan(RecordSection.Event event, long from, long to) {
        List<RecordSection.Event> list = new ArrayList<RecordSection.Event>();
        if (event.begin >= from && event.end <= to) {
            list.add(event.getNewEvent(event.begin, event.end));
        } else if (event.begin <= from && event.end >= from && event.end <= to) {
            list.add(event.getNewEvent(event.begin, event.end));
        } else if (event.begin <= from && event.end >= to) {
            list.add(event.getNewEvent(from, to));
        } else if (event.begin >= from && event.begin <= to && event.end >= to) {
            list.add(event.getNewEvent(event.begin, to));
        }
        return list;
    }


}
