/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.review;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 3 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="rreviewelementdefinition")
@Table(name="fx_r_review_element_def")
public class ReviewElementDefinitionImpl implements ReviewElementDefinition, Persistable {

	private static final long serialVersionUID = 8441946123708368757L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	protected Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="r_label", nullable=true, insertable=true, updatable=true)
	private String label;
	@Column(name="r_type", nullable=false, insertable=true, updatable=true)
	private String elementType;
	
	@ManyToOne(targetEntity=PositionReviewDefinitionImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_pos_rev_id", nullable=false, insertable=true, updatable=false)
	private PositionReviewDefinition positionReviewDefinition;
	
	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}
	
	@Override
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}
	
	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public ReviewElementType getType() {
		return StringHelper.containsNonWhitespace(elementType) ? ReviewElementType.valueOf(elementType) : null;
	}
	
	public void setType(ReviewElementType type) {
		if(type == null) {
			elementType = null;
		} else {
			elementType = type.name();
		}
	}

	public String getElementType() {
		return elementType;
	}

	public void setElementType(String elementType) {
		this.elementType = elementType;
	}

	public PositionReviewDefinition getPositionReviewDefinition() {
		return positionReviewDefinition;
	}

	public void setPositionReviewDefinition(PositionReviewDefinition positionReviewDefinition) {
		this.positionReviewDefinition = positionReviewDefinition;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 7863545 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ReviewElementDefinitionImpl) {
			ReviewElementDefinitionImpl def = (ReviewElementDefinitionImpl)obj;
			return getKey() != null && getKey().equals(def.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
