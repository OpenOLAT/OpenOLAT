package org.olat.modules.assessment.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.assessment.service.GradeStatistics;
import org.olat.modules.assessment.service.ReportingService;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Basic reporting service that computes simple aggregates (mean, median, stddev).
 */
@Service
public class ReportingServiceImpl implements ReportingService {

    private AssessmentEntryDAO assessmentEntryDAO;

    @Autowired
    public ReportingServiceImpl(AssessmentEntryDAO assessmentEntryDAO) {
        this.assessmentEntryDAO = assessmentEntryDAO;
    }

    @Override
    public GradeStatistics computeStatistics(RepositoryEntryRef repositoryEntry, String subIdent) {
        List<AssessmentEntry> entries = assessmentEntryDAO.loadAssessmentEntryBySubIdent(repositoryEntry, subIdent);
        List<BigDecimal> scores = new ArrayList<>();
        for (AssessmentEntry e : entries) {
            BigDecimal s = e.getScore();
            if (s != null) scores.add(s);
        }

        if (scores.isEmpty()) {
            return new GradeStatistics(0, null, null, null, null, null);
        }

        Collections.sort(scores);
        int n = scores.size();

        // mean
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal s : scores) sum = sum.add(s);
        BigDecimal mean = sum.divide(BigDecimal.valueOf(n), BigDecimal.ROUND_HALF_UP);

        // median
        BigDecimal median;
        if (n % 2 == 1) {
            median = scores.get(n / 2);
        } else {
            median = scores.get(n / 2 - 1).add(scores.get(n / 2)).divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_UP);
        }

        // stddev (population)
        double variance = 0d;
        double meanDouble = mean.doubleValue();
        for (BigDecimal s : scores) {
            double d = s.doubleValue() - meanDouble;
            variance += d * d;
        }
        variance = variance / n;
        BigDecimal stddev = BigDecimal.valueOf(Math.sqrt(variance));

        BigDecimal min = scores.get(0);
        BigDecimal max = scores.get(n - 1);

        return new GradeStatistics(n, mean, median, stddev, min, max);
    }
}
