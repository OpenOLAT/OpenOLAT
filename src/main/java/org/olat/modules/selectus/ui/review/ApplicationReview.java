/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.review;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.id.Identity;
import org.olat.core.util.ConsumableBoolean;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.review.ReviewResponse;
import org.olat.modules.selectus.ui.components.DateTimeCellRenderer;

/**
 * 
 * Initial date: 4 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationReview {
	
	private boolean open;
	private final String id;
	private Date creationDate;
	private int reviewNumber;
	private final String reviewerFullName;
	private final Identity reviewer;
	private final List<ReviewResponse> responses = new ArrayList<>();
	private final List<ApplicationReviewElement> elements = new ArrayList<>();
	private final List<ApplicationReviewComment> comments = new ArrayList<>();
	
	private int numOfComments = 0;
	
	private FormLink editButton;
	private FormLink deleteButton;
	private FormLayoutContainer commentsContainer;
	private ConsumableBoolean scrollTo;
	
	public ApplicationReview(Identity reviewer, String reviewerFullName, String id) {
		this.id = id;
		this.reviewer = reviewer;
		this.reviewerFullName = reviewerFullName;
	}

	public String getId() {
		return id;
	}
	
	public String getFullName() {
		return reviewerFullName;
	}
	
	public int getReviewNumber() {
		return reviewNumber;
	}
	
	public void setReviewNumber(int reviewNumber) {
		this.reviewNumber = reviewNumber;
	}
	
	public Date getDate() {
		return creationDate;
	}
	
	public String getCreationDate() {
		return DateTimeCellRenderer.format(creationDate);
	}
	
	public void setCreationDate(Date date) {
		if(creationDate == null || date.after(creationDate)) {
			creationDate = date;
		}
	}
	
	public boolean isOpen() {
		return open;
	}
	
	public void setOpen(boolean open) {
		this.open = open;
	}
	
	public Identity getReviewer() {
		return reviewer;
	}
	
	public boolean isEmpty() {
		boolean empty = true;
		for(ReviewResponse response:responses) {
			if(response.getIntegerValue() != null || StringHelper.containsNonWhitespace(response.getStringValue())) {
				empty &= false;
			}
		}
		return empty;
	}
	
	public List<ReviewResponse> getResponses() {
		return responses;
	}
	
	public void addResponse(ReviewResponse response) {
		if(response != null) {
			responses.add(response);
			setCreationDate(response.getCreationDate());
		}
	}
	
	public List<ApplicationReviewElement> getElements() {
		return elements;
	}
	
	public void addElement(ApplicationReviewElement element) {
		elements.add(element);
	}

	public int getNumOfComments() {
		return numOfComments;
	}

	public void setNumOfComments(int numOfComments) {
		this.numOfComments = numOfComments;
	}

	public List<ApplicationReviewComment> getComments() {
		return comments;
	}
	
	public void addComment(ApplicationReviewComment comment) {
		comments.add(comment);
	}

	public FormLink getEditButton() {
		return editButton;
	}

	public void setEditButton(FormLink editButton) {
		this.editButton = editButton;
	}

	public FormLink getDeleteButton() {
		return deleteButton;
	}

	public void setDeleteButton(FormLink deleteButton) {
		this.deleteButton = deleteButton;
	}

	public FormLayoutContainer getCommentsContainer() {
		return commentsContainer;
	}

	public void setCommentsContainer(FormLayoutContainer commentsContainer) {
		this.commentsContainer = commentsContainer;
	}
	
	public boolean isScrollTo() {
		return scrollTo != null && scrollTo.isTrue();
	}
	
	public void setScrollTo(boolean scrollTo) {
		this.scrollTo = null;
		if(scrollTo) {
			this.scrollTo = new ConsumableBoolean(true);
			setOpen(true);
		}
	}
}
