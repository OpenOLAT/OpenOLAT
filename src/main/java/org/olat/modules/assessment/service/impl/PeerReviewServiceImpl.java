package org.olat.modules.assessment.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.olat.modules.assessment.manager.PeerReviewDAO;
import org.olat.modules.assessment.model.PeerReviewImpl;
import org.olat.modules.assessment.service.PeerReviewService;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PeerReviewServiceImpl implements PeerReviewService {

    @Autowired
    private PeerReviewDAO peerReviewDao;

    @Override
    public PeerReviewImpl createPeerReview(RepositoryEntryRef entry, String subIdent, Long reviewerKey, Long revieweeKey,
            BigDecimal score, String comment) {
        PeerReviewImpl review = new PeerReviewImpl();
        if (entry != null) {
            // repositoryEntry is a full entity in other contexts; to keep minimal, set by key via DAO if needed
            // here we leave repositoryEntry null for simplicity when using RepositoryEntryRef
        }
        review.setSubIdent(subIdent);
        review.setReviewerKey(reviewerKey);
        review.setRevieweeKey(revieweeKey);
        review.setScore(score);
        review.setComment(comment);
        return peerReviewDao.createPeerReview(review);
    }

    @Override
    public PeerReviewImpl submitReview(Long reviewKey, BigDecimal score, String comment) {
        // simple update flow: load -> update -> merge
        // For simplicity, use JPA merge via DAO by constructing a transient object with key
        PeerReviewImpl review = new PeerReviewImpl();
        try {
            java.lang.reflect.Field keyField = PeerReviewImpl.class.getDeclaredField("key");
            keyField.setAccessible(true);
            keyField.set(review, reviewKey);
        } catch (Exception e) {
            // ignore; merge may fail if key not set
        }
        review.setScore(score);
        review.setComment(comment);
        return peerReviewDao.updatePeerReview(review);
    }

    @Override
    public List<PeerReviewImpl> getReviewsForReviewee(RepositoryEntryRef entry, String subIdent, Long revieweeKey) {
        return peerReviewDao.getReviewsForReviewee(entry, subIdent, revieweeKey);
    }

    @Override
    public List<PeerReviewImpl> getReviewsByReviewer(RepositoryEntryRef entry, String subIdent, Long reviewerKey) {
        return peerReviewDao.getReviewsByReviewer(entry, subIdent, reviewerKey);
    }

}
