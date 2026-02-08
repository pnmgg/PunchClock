package com.example.punchclock.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ProjectTest {

    @Test
    public void testProjectCreation() {
        long now = System.currentTimeMillis();
        Project project = new Project("Test Project", "Description", "Test Category", 123456, now, 1L);

        assertEquals("Test Project", project.name);
        assertEquals("Description", project.description);
        assertEquals("Test Category", project.category);
        assertEquals(123456, project.color);
        assertEquals(now, project.createdAt);
        assertEquals(1L, project.userId);
    }
}
