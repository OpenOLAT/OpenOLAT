package org.olat.modules.curriculum.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRepositoryEntryRelation;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="repoentrytocurriculumelement")
@Table(name="o_re_to_curriculum_element")
public class CurriculumRepositoryEntryRelationImpl implements CurriculumRepositoryEntryRelation, Persistable {

	private static final long serialVersionUID = -4889700699342402333L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
    @Column(name="c_master", nullable=true, insertable=true, updatable=false)
	private boolean master;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_entry", nullable=true, insertable=true, updatable=true)
    private RepositoryEntry entry;

	@ManyToOne(targetEntity=CurriculumElementImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_curriculum_element", nullable=true, insertable=true, updatable=false)
    private CurriculumElement curriculumElement;

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
	public boolean isMaster() {
		return master;
	}

	public void setMaster(boolean master) {
		this.master = master;
	}

	@Override
	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	@Override
	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	public void setCurriculumElement(CurriculumElement curriculumElement) {
		this.curriculumElement = curriculumElement;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 391598261 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CurriculumRepositoryEntryRelationImpl) {
			CurriculumRepositoryEntryRelationImpl rel = (CurriculumRepositoryEntryRelationImpl)obj;
			return getKey() != null && getKey().equals(rel.getKey());
		}
		
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
