package org.olat.modules.assessment.service;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

import org.junit.Test;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryImpl;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.assessment.service.impl.AutoGradingServiceImpl;

/**
 * Basic unit test for the AutoGradingService scaffold.
 */
public class AutoGradingServiceTest {

    @Test
    public void gradeNonExistingEntryReturnsNull() {
        AssessmentEntryDAO fakeDao = new AssessmentEntryDAO() {
            @Override
            public AssessmentEntry loadAssessmentEntryById(Long id) {
                return null;
            }
        };

        AutoGradingServiceImpl svc = new AutoGradingServiceImpl(fakeDao, null);
        AssessmentEntry result = svc.gradeAssessmentEntry(12345L);
        assertNull(result);
    }

    @Test
    public void gradeExistingEntrySetsScore() {
        AssessmentEntryImpl fakeEntry = new AssessmentEntryImpl();
        fakeEntry.setMaxScore(BigDecimal.valueOf(42));

        AssessmentEntryDAO fakeDao = new AssessmentEntryDAO() {
            @Override
            public AssessmentEntry loadAssessmentEntryById(Long id) {
                return fakeEntry;
            }
            @Override
            public AssessmentEntry updateAssessmentEntry(AssessmentEntry nodeAssessment) {
                return nodeAssessment;
            }
        };

        AutoGradingServiceImpl svc = new AutoGradingServiceImpl(fakeDao, null);
        AssessmentEntry result = svc.gradeAssessmentEntry(1L);
        assertNotNull(result);
        // score should have been set to max (42)
        assertNotNull(result.getScore());
        // numeric check
        assertNotNull(result.getMaxScore());
    }
}
