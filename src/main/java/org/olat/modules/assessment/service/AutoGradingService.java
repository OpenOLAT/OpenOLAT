package org.olat.modules.assessment.service;

import org.olat.modules.assessment.AssessmentEntry;

/**
 * Service responsible for automatic grading of objective assessments.
 */
public interface AutoGradingService {

    /**
     * Grade an assessment entry (attempt) and persist the computed result.
     * @param assessmentEntryKey primary key of the assessment entry to grade
     * @return the updated {@link AssessmentEntry} or null if not found
     */
    AssessmentEntry gradeAssessmentEntry(Long assessmentEntryKey);

}
