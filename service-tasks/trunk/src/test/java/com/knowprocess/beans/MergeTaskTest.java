package com.knowprocess.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.knowprocess.core.model.ProcessMetaData;

public class MergeTaskTest {

    @Test
    public void testMerge() {
        ProcessMetaData srcBean = new ProcessMetaData();
        ProcessMetaData tBean = new ProcessMetaData();
        assertNotNull(srcBean.getStart());
        MergeTask svc = new MergeTask();
        try {
            svc.merge(srcBean, tBean);
            assertNotNull(tBean.getStart());
            assertEquals(srcBean.getStart(), tBean.getStart());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
