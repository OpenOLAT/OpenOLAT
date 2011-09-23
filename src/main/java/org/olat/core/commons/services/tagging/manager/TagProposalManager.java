package org.olat.core.commons.services.tagging.manager;

import java.util.List;

public interface TagProposalManager {
	
	/**
	 * get some good tags for the given text
	 * @param referenceText
	 * @param onlyExisting if true, returns only such tags, that yet exist
	 * @return
	 */
	public List<String> proposeTagsForInputText(String referenceText, boolean onlyExisting);

}
