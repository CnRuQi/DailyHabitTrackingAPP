package com.example.dailyhabittrackingapp;

import com.example.dailyhabittrackingapp.bean.HabitBean;

import org.junit.Test;

import static org.junit.Assert.*;

public class HabitBeanTest {

    @Test
    public void testDefaultConstructor() {
        HabitBean bean = new HabitBean();
        assertNull(bean.getId());
        assertNull(bean.getTitle());
        assertNull(bean.getContent());
        assertNull(bean.getImageUri());
        assertNull(bean.getDate());
        assertNull(bean.getTime());
    }

    @Test
    public void testParameterizedConstructor() {
        HabitBean bean = new HabitBean(1, "运动", "跑步30分钟",
                "/photo.jpg", "2026/06/15", "08:30");

        assertEquals(Integer.valueOf(1), bean.getId());
        assertEquals("运动", bean.getTitle());
        assertEquals("跑步30分钟", bean.getContent());
        assertEquals("/photo.jpg", bean.getImageUri());
        assertEquals("2026/06/15", bean.getDate());
        assertEquals("08:30", bean.getTime());
    }

    @Test
    public void testSettersAndGetters() {
        HabitBean bean = new HabitBean();

        bean.setId(2);
        bean.setTitle("阅读");
        bean.setContent("读30页书");
        bean.setImageUri("");
        bean.setDate("2026/01/01");
        bean.setTime("12:00");

        assertEquals(Integer.valueOf(2), bean.getId());
        assertEquals("阅读", bean.getTitle());
        assertEquals("读30页书", bean.getContent());
        assertEquals("", bean.getImageUri());
        assertEquals("2026/01/01", bean.getDate());
        assertEquals("12:00", bean.getTime());
    }

    @Test
    public void testEmptyImageUri() {
        HabitBean bean = new HabitBean(3, "早起", null, null, "2026/06/15", "06:00");
        assertNull(bean.getImageUri());
        assertNull(bean.getContent());
    }
}
