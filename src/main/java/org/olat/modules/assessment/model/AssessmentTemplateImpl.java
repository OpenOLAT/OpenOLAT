package org.olat.modules.assessment.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;

@Entity(name="assessmenttemplate")
@Table(name="o_as_template")
public class AssessmentTemplateImpl implements Persistable, CreateInfo {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long key;

    @Column(name="creationdate", nullable=false)
    private Date creationDate = new Date();

    @Column(name="t_name", nullable=false)
    private String name;

    @Column(name="t_description", nullable=true)
    private String description;

    @Lob
    @Column(name="t_content", nullable=false)
    private String content; // serialized assessment structure (JSON/XML)

    @Column(name="fk_creator", nullable=true)
    private Long creatorKey;

    public AssessmentTemplateImpl() {
        // default
    }

    public Long getKey() {
        return key;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCreatorKey() {
        return creatorKey;
    }

    public void setCreatorKey(Long creatorKey) {
        this.creatorKey = creatorKey;
    }

    @Override
    public int hashCode() {
        return getKey() == null ? 1237 : getKey().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof AssessmentTemplateImpl other) {
            return getKey() != null && getKey().equals(other.getKey());
        }
        return false;
    }

    @Override
    public boolean equalsByPersistableKey(Persistable persistable) {
        return equals(persistable);
    }
}
