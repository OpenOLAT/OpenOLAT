package org.olat.modules.assessment.model;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;
import org.olat.repository.RepositoryEntry;

@Entity(name="peerreview")
@Table(name="o_as_peer_review")
public class PeerReviewImpl implements Persistable, CreateInfo {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long key;

    @Column(name="creationdate", nullable=false)
    private Date creationDate = new Date();

    @ManyToOne
    @JoinColumn(name="fk_entry", nullable=true)
    private RepositoryEntry repositoryEntry;

    @Column(name="subident", nullable=true)
    private String subIdent;

    @Column(name="fk_reviewer", nullable=false)
    private Long reviewerKey;

    @Column(name="fk_reviewee", nullable=false)
    private Long revieweeKey;

    @Column(name="score", nullable=true)
    private BigDecimal score;

    @Column(name="comment", nullable=true)
    private String comment;

    public PeerReviewImpl() {
        // default
    }

    public Long getKey() {
        return key;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public RepositoryEntry getRepositoryEntry() {
        return repositoryEntry;
    }

    public void setRepositoryEntry(RepositoryEntry repositoryEntry) {
        this.repositoryEntry = repositoryEntry;
    }

    public String getSubIdent() {
        return subIdent;
    }

    public void setSubIdent(String subIdent) {
        this.subIdent = subIdent;
    }

    public Long getReviewerKey() {
        return reviewerKey;
    }

    public void setReviewerKey(Long reviewerKey) {
        this.reviewerKey = reviewerKey;
    }

    public Long getRevieweeKey() {
        return revieweeKey;
    }

    public void setRevieweeKey(Long revieweeKey) {
        this.revieweeKey = revieweeKey;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    @Override
    public int hashCode() {
        return getKey() == null ? 97123 : getKey().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof PeerReviewImpl other) {
            return getKey() != null && getKey().equals(other.getKey());
        }
        return false;
    }

    @Override
    public boolean equalsByPersistableKey(Persistable persistable) {
        return equals(persistable);
    }
}
