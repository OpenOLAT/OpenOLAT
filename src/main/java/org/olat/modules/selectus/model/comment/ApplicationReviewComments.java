/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.comment;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 21 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationReviewComments {
	
	private List<ApplicationReviewComment> comments = new ArrayList<>();
	
	public ApplicationReviewComments() {
		//
	}
	
	public long size() {
		long size = 0l;
		for(ApplicationReviewComment comment:comments) {
			size += comment.size();	
		}
		return size;
	}
	
	public List<ApplicationReviewComment> getComments() {
		return comments;
	}
	
	public void addComment(ApplicationReviewComment comment) {
		comments.add(comment);
	}

}
