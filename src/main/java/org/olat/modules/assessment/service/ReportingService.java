package org.olat.modules.assessment.service;

import org.olat.modules.assessment.service.GradeStatistics;
import org.olat.repository.RepositoryEntryRef;

/**
 * Service to compute performance summaries and grade statistics for assessments.
 */
public interface ReportingService {

    /**
     * Compute grade statistics for the given repository entry and sub identifier.
     * @param repositoryEntry repository entry reference (course or test)
     * @param subIdent course node or sub identifier
     * @return computed statistics (count may be zero)
     */
    GradeStatistics computeStatistics(RepositoryEntryRef repositoryEntry, String subIdent);

}
