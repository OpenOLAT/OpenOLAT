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

import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ConsumableBoolean;

import org.olat.modules.selectus.model.ApplicationComment;
import org.olat.modules.selectus.ui.components.DateTimeCellRenderer;

/**
 * 
 * Initial date: 14 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationReviewComment {
	
	private static final Logger log = Tracing.createLoggerFor(ApplicationReviewComment.class);
	
	private String comment;
	private String author;
	private int numOfAgree;
	private int numOfDisagree;
	
	private boolean edit;
	private boolean reply;
	private boolean visible;

	private final ApplicationReview review;
	private final ApplicationComment appComment;
	private ApplicationReviewComment parentComment;
	
	private TextElement newCommentEl;
	private FormLink newCommentButton;
	private FormLink cancelNewCommentButton;
	
	private FormLink agreeLink;
	private FormLink disagreeLink;
	private FormLink replyLink;
	private FormLink editLink;
	private FormLink deleteLink;
	private Translator translator;
	private ConsumableBoolean scrollTo;
	
	public ApplicationReviewComment(ApplicationReview review, ApplicationComment appComment, Translator translator) {
		this.review = review;
		this.appComment = appComment;
		this.translator = translator;
	}
	
	public int getIndent() {
		int indent = 0;
		for(ApplicationReviewComment parent=getParentComment(); parent != null; parent = parent.getParentComment()) {
			indent++;
		}
		return indent;
	}
	
	public boolean isDeleted() {
		return appComment != null && appComment.isDeleted();
	}
	
	public ApplicationReview getReview() {
		return review;
	}
	
	public Long getKey() {
		return appComment == null ? null : appComment.getKey();
	}
	
	public ApplicationComment getApplicationComment() {
		return appComment;
	}
	
	public ApplicationReviewComment getParentComment() {
		return parentComment;
	}

	public void setParentComment(ApplicationReviewComment parentComment) {
		this.parentComment = parentComment;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public Date getCreationDate() {
		return appComment == null ? null : appComment.getCreationDate();
	}
	
	public String getCreationDateUntilNow() {
		try(StringOutput sb = new StringOutput(100)) {
			Date date = getCreationDate();
			if(date != null) {
				long time = date.getTime();
				long now = new Date().getTime();
				if(now - time < 60 * 60 * 1000) {
					long minutes = (now - time) / (60 * 1000);
					if(minutes <= 1) {
						sb.append(translator.translate("time.ago.one.minute"));
					} else {
						sb.append(translator.translate("time.ago.minutes", new String[] { String.valueOf(minutes) }));
					}
				} else if (now - time < 24 * 60 * 60 * 1000) {
					long hours = (now - time) / (60 * 60 * 1000);
					if(hours <= 1) {
						sb.append(translator.translate("time.ago.one.hour"));
					} else {
						sb.append(translator.translate("time.ago.hour", new String[] { String.valueOf(hours) }));
					}
				} else {
					sb.append(DateTimeCellRenderer.format(date));
				}
			}
			return sb.toString();
		} catch (Exception e) {
			log.error("", e);
			return "";
		}
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
		if(edit) {
			reply = false;
		}
	}

	public boolean isReply() {
		return reply;
	}

	public void setReply(boolean reply) {
		this.reply = reply;
		if(reply) {
			edit = false;
		}
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public boolean isNewOnly() {
		return appComment == null;
	}

	public int getNumOfAgree() {
		return numOfAgree;
	}

	public void setNumOfAgree(int numOfAgree) {
		this.numOfAgree = numOfAgree;
	}

	public int getNumOfDisagree() {
		return numOfDisagree;
	}

	public void setNumOfDisagree(int numOfDisagree) {
		this.numOfDisagree = numOfDisagree;
	}

	public TextElement getNewCommentEl() {
		return newCommentEl;
	}

	public void setNewCommentEl(TextElement newCommentEl) {
		this.newCommentEl = newCommentEl;
	}

	public FormLink getNewCommentButton() {
		return newCommentButton;
	}

	public void setNewCommentButton(FormLink newCommentButton) {
		this.newCommentButton = newCommentButton;
	}

	public FormLink getCancelNewCommentButton() {
		return cancelNewCommentButton;
	}

	public void setCancelNewCommentButton(FormLink cancelNewCommentButton) {
		this.cancelNewCommentButton = cancelNewCommentButton;
	}

	public FormLink getAgreeLink() {
		return agreeLink;
	}

	public void setAgreeLink(FormLink agreeLink) {
		this.agreeLink = agreeLink;
	}

	public FormLink getDisagreeLink() {
		return disagreeLink;
	}

	public void setDisagreeLink(FormLink disagreeLink) {
		this.disagreeLink = disagreeLink;
	}

	public FormLink getReplyLink() {
		return replyLink;
	}

	public void setReplyLink(FormLink replyLink) {
		this.replyLink = replyLink;
	}

	public FormLink getEditLink() {
		return editLink;
	}

	public void setEditLink(FormLink editLink) {
		this.editLink = editLink;
	}

	public FormLink getDeleteLink() {
		return deleteLink;
	}

	public void setDeleteLink(FormLink deleteLink) {
		this.deleteLink = deleteLink;
	}
	
	public boolean isScrollTo() {
		return scrollTo != null && scrollTo.isTrue();
	}
	
	public void setScrollTo(boolean scrollTo) {
		this.scrollTo = null;
		if(scrollTo) {
			this.scrollTo = new ConsumableBoolean(true);
			review.setOpen(true);
		}
	}
}
