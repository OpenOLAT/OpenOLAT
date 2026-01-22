package org.olat.modules.assessment.service;

import java.math.BigDecimal;
import java.util.List;

import org.olat.modules.assessment.model.PeerReviewImpl;
import org.olat.repository.RepositoryEntryRef;

/**
 * Service for peer review workflows and evaluations.
 */
public interface PeerReviewService {

    PeerReviewImpl createPeerReview(RepositoryEntryRef entry, String subIdent, Long reviewerKey, Long revieweeKey,
            BigDecimal score, String comment);

    PeerReviewImpl submitReview(Long reviewKey, BigDecimal score, String comment);

    List<PeerReviewImpl> getReviewsForReviewee(RepositoryEntryRef entry, String subIdent, Long revieweeKey);

    List<PeerReviewImpl> getReviewsByReviewer(RepositoryEntryRef entry, String subIdent, Long reviewerKey);
}
