package com.vng.teg.logtool.web.server;

import com.vng.teg.logtool.web.schedule.ScheduledTasks;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by sonnguyen on 26/05/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class TestScheduledTasks extends BaseServiceTest{

    @Test
    public void testReportAllGames() throws Exception{
        scheduledTasks.reportAllGamesMorning();
    }

    @Test
    public void testAutoScan() throws Exception{
        scheduledTasks.autoScan();
    }

    @Autowired
    private ScheduledTasks scheduledTasks;
}
