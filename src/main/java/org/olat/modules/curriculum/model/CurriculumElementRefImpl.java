package org.olat.modules.curriculum.model;

import org.olat.modules.curriculum.CurriculumElementRef;

/**
 * 
 * Initial date: 15 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementRefImpl implements CurriculumElementRef {
	
	private final Long key;
	
	public CurriculumElementRefImpl(Long key) {
		this.key = key;
	}

	@Override
	public Long getKey() {
		return key;
	}
}
