package org.olat.modules.curriculum.model;

import org.olat.modules.curriculum.CurriculumElementTypeRef;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementTypeRefImpl implements CurriculumElementTypeRef {
	
	private final Long key;
	
	public CurriculumElementTypeRefImpl(Long key) {
		this.key = key;
	}
	
	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 78254 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CurriculumElementTypeRefImpl) {
			CurriculumElementTypeRefImpl ref = (CurriculumElementTypeRefImpl)obj;
			return getKey() != null && getKey().equals(ref.getKey());
		}
		return super.equals(obj);
	}
}
