package org.olat.modules.assessment.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.assessment.model.AssessmentEntryImpl;
import org.olat.modules.assessment.service.impl.ReportingServiceImpl;
import org.olat.repository.RepositoryEntryRef;

public class ReportingServiceTest {

    @Test
    public void computeStatisticsSimple() {
        List<AssessmentEntry> entries = new ArrayList<>();
        AssessmentEntryImpl e1 = new AssessmentEntryImpl(); e1.setScore(BigDecimal.valueOf(10));
        AssessmentEntryImpl e2 = new AssessmentEntryImpl(); e2.setScore(BigDecimal.valueOf(20));
        AssessmentEntryImpl e3 = new AssessmentEntryImpl(); e3.setScore(BigDecimal.valueOf(30));
        entries.add(e1); entries.add(e2); entries.add(e3);

        AssessmentEntryDAO fakeDao = new AssessmentEntryDAO() {
            @Override
            public List<AssessmentEntry> loadAssessmentEntryBySubIdent(RepositoryEntryRef entry, String subIdent) {
                return entries;
            }
        };

        ReportingServiceImpl svc = new ReportingServiceImpl(fakeDao);
        GradeStatistics stats = svc.computeStatistics(null, "node1");
        assertNotNull(stats);
        assertEquals(3L, stats.getCount());
        assertEquals(BigDecimal.valueOf(20), stats.getMean());
        assertEquals(BigDecimal.valueOf(20), stats.getMedian());
    }
}
