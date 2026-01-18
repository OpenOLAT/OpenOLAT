package org.olat.modules.assessment.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;
import org.olat.modules.assessment.manager.PeerReviewDAO;
import org.olat.modules.assessment.model.PeerReviewImpl;
import org.olat.modules.assessment.service.impl.PeerReviewServiceImpl;
import org.olat.repository.RepositoryEntryRef;

public class PeerReviewServiceTest {

    @Test
    public void createAndQueryReview() {
        PeerReviewDAO fakeDao = new PeerReviewDAO() {
            java.util.List<PeerReviewImpl> store = new java.util.ArrayList<>();
            @Override
            public PeerReviewImpl createPeerReview(PeerReviewImpl review) {
                try {
                    java.lang.reflect.Field keyField = PeerReviewImpl.class.getDeclaredField("key");
                    keyField.setAccessible(true);
                    keyField.set(review, (long)(store.size()+1));
                } catch (Exception e) {
                }
                store.add(review);
                return review;
            }
            @Override
            public java.util.List<PeerReviewImpl> getReviewsForReviewee(RepositoryEntryRef entry, String subIdent, Long revieweeKey) {
                java.util.List<PeerReviewImpl> result = new java.util.ArrayList<>();
                for (PeerReviewImpl r: store) if (r.getRevieweeKey().equals(revieweeKey)) result.add(r);
                return result;
            }
        };

        PeerReviewServiceImpl svc = new PeerReviewServiceImpl();
        try {
            java.lang.reflect.Field daoField = PeerReviewServiceImpl.class.getDeclaredField("peerReviewDao");
            daoField.setAccessible(true);
            daoField.set(svc, fakeDao);
        } catch (Exception e) {
        }

        PeerReviewImpl r = svc.createPeerReview(null, "node1", 11L, 22L, BigDecimal.valueOf(4.5), "good job");
        assertNotNull(r);
        List<PeerReviewImpl> reviews = svc.getReviewsForReviewee(null, "node1", 22L);
        assertEquals(1, reviews.size());
        assertEquals(BigDecimal.valueOf(4.5), reviews.get(0).getScore());
    }
}
