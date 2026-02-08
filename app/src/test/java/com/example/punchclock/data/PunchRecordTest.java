package com.example.punchclock.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PunchRecordTest {

    @Test
    public void testPunchRecordCreation() {
        PunchRecord record = new PunchRecord(1L, 1000L);

        assertEquals(1L, record.projectId);
        assertEquals(1000L, record.date);
    }
}
