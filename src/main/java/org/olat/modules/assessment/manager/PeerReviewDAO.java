package org.olat.modules.assessment.manager;

import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.assessment.model.PeerReviewImpl;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PeerReviewDAO {

    @Autowired
    private DB dbInstance;

    public PeerReviewImpl createPeerReview(PeerReviewImpl review) {
        dbInstance.getCurrentEntityManager().persist(review);
        return review;
    }

    public PeerReviewImpl updatePeerReview(PeerReviewImpl review) {
        return dbInstance.getCurrentEntityManager().merge(review);
    }

    public List<PeerReviewImpl> getReviewsForReviewee(RepositoryEntryRef entry, String subIdent, Long revieweeKey) {
        StringBuilder sb = new StringBuilder();
        sb.append("select r from peerreview r where r.revieweeKey=:revieweeKey");
        if (entry != null) {
            sb.append(" and r.repositoryEntry.key=:entryKey");
        }
        if (subIdent != null) {
            sb.append(" and r.subIdent=:subIdent");
        }
        TypedQuery<PeerReviewImpl> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), PeerReviewImpl.class)
                .setParameter("revieweeKey", revieweeKey);
        if (entry != null) query.setParameter("entryKey", entry.getKey());
        if (subIdent != null) query.setParameter("subIdent", subIdent);
        return query.getResultList();
    }

    public List<PeerReviewImpl> getReviewsByReviewer(RepositoryEntryRef entry, String subIdent, Long reviewerKey) {
        StringBuilder sb = new StringBuilder();
        sb.append("select r from peerreview r where r.reviewerKey=:reviewerKey");
        if (entry != null) {
            sb.append(" and r.repositoryEntry.key=:entryKey");
        }
        if (subIdent != null) {
            sb.append(" and r.subIdent=:subIdent");
        }
        TypedQuery<PeerReviewImpl> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), PeerReviewImpl.class)
                .setParameter("reviewerKey", reviewerKey);
        if (entry != null) query.setParameter("entryKey", entry.getKey());
        if (subIdent != null) query.setParameter("subIdent", subIdent);
        return query.getResultList();
    }
}
