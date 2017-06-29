package org.olat.modules.lecture.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.olat.core.id.Persistable;

/**
 * Mapping used to update the log of a lecture block.
 * 
 * Initial date: 28 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="lectureblocklog")
@Table(name="o_lecture_block")
public class LectureBlockLog implements Persistable {

	private static final long serialVersionUID = -8262907202789409370L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Column(name="l_log", nullable=true, insertable=false, updatable=true)
	private String log;

	@Override
	public Long getKey() {
		return key;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	@Override
	public int hashCode() {
		return key == null ? 56483658 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof LectureBlockLog) {
			LectureBlockLog blockLog = (LectureBlockLog)obj;
			return key != null && key.equals(blockLog.key);
		}
		return super.equals(obj);
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
