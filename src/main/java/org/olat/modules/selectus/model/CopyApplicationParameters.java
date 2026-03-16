/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.util.List;

/**
 * 
 * Initial date: 8 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CopyApplicationParameters {
	
	private List<Copy> toCopy;

	public List<Copy> getToCopy() {
		return toCopy;
	}

	public void setToCopy(List<Copy> toCopy) {
		this.toCopy = toCopy;
	}

	public enum Copy {
		profileInformations,
		applicationDocuments,
		refereesAndLetters,
		expertsAndAssessments,
		comparativeExperts,
		memo,
		committeeComment,
		applicationStatus,
		tags,
		decision
	}
}
