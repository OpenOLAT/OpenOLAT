/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.review;

/**
 * 
 * Initial date: 21 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationTextCollectionElement {
	
	private final String text;
	private final Reviewer reviewer;
	private final ReviewElementDefinition elementDefinition;
	
	public ApplicationTextCollectionElement(ReviewElementDefinition elementDefinition, Reviewer reviewer, String text) {
		this.elementDefinition = elementDefinition;
		this.reviewer = reviewer;
		this.text = text;
	}
	
	public String getText() {
		return text;
	}

	public Reviewer getReviewer() {
		return reviewer;
	}
	
	public ReviewElementDefinition getElementDefinition() {
		return elementDefinition;
	}
}
