package org.olat.modules.curriculum.model;

import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;

/**
 * 
 * Initial date: 29 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementInfos implements CurriculumElementRef {
	
	private final CurriculumElement curriculumElement;
	private final long numOfResources;
	
	public CurriculumElementInfos(CurriculumElement curriculumElement, long numOfResources) {
		this.curriculumElement = curriculumElement;
		this.numOfResources = numOfResources;
	}
	
	@Override
	public Long getKey() {
		return curriculumElement.getKey();
	}

	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	public long getNumOfResources() {
		return numOfResources;
	}
}
