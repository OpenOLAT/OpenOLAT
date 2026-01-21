package org.olat.modules.assessment.service.impl;

import java.math.BigDecimal;

import org.olat.core.id.Identity;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.assessment.service.AutoGradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Minimal implementation of automatic grading for objective assessments.
 * This initial version applies a simple policy: if the assessment entry
 * has a {@code maxScore} set and no {@code score}, set the {@code score}
 * to the {@code maxScore} and persist. This is a scaffold to be
 * extended with QTI-based scoring logic.
 */
@Service
public class AutoGradingServiceImpl implements AutoGradingService {

    private AssessmentEntryDAO assessmentEntryDAO;
    private AssessmentService assessmentService;
    private QTI21Service qtiService;

    public AutoGradingServiceImpl() {
        // for Spring
    }

    /**
     * Constructor useful for unit tests.
     */
    public AutoGradingServiceImpl(AssessmentEntryDAO assessmentEntryDAO, AssessmentService assessmentService) {
        this.assessmentEntryDAO = assessmentEntryDAO;
        this.assessmentService = assessmentService;
    }

    @Autowired
    public void setAssessmentEntryDAO(AssessmentEntryDAO assessmentEntryDAO) {
        this.assessmentEntryDAO = assessmentEntryDAO;
    }

    @Autowired
    public void setAssessmentService(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @Autowired
    public void setQtiService(QTI21Service qtiService) {
        this.qtiService = qtiService;
    }

    @Override
    public AssessmentEntry gradeAssessmentEntry(Long assessmentEntryKey) {
        if (assessmentEntryKey == null || assessmentEntryDAO == null) return null;

        AssessmentEntry entry = assessmentEntryDAO.loadAssessmentEntryById(assessmentEntryKey);
        if (entry == null) return null;

        // skip if already scored
        if (entry.getScore() != null) {
            return entry;
        }

        // If a QTI test is associated, try to compute the score from the last test session
        if (qtiService != null && entry.getReferenceEntry() != null && entry.getRepositoryEntry() != null) {
            Identity identity = entry.getIdentity();
            try {
                AssessmentTestSession session = qtiService.getLastAssessmentTestSessions(entry.getRepositoryEntry(), entry.getSubIdent(), entry.getReferenceEntry(), identity);
                if (session != null) {
                    // ensure session scores are up-to-date
                    session = qtiService.recalculateAssessmentTestSessionScores(session.getKey());
                    BigDecimal finalScore = session.getFinalScore();
                    if (finalScore != null) {
                        entry.setScore(finalScore);
                        if (session.getMaxScore() != null) {
                            entry.setMaxScore(session.getMaxScore());
                        }
                        if (session.getPassed() != null) {
                            entry.setPassed(session.getPassed());
                        }
                        return assessmentEntryDAO.updateAssessmentEntry(entry);
                    }
                }
            } catch (Exception e) {
                // fall back to default behavior on errors
            }
        }

        // fallback: set score equal to maxScore or 100 if unknown
        BigDecimal max = entry.getMaxScore();
        BigDecimal newScore = (max != null) ? max : BigDecimal.valueOf(100);
        entry.setScore(newScore);
        return assessmentEntryDAO.updateAssessmentEntry(entry);
    }

}
