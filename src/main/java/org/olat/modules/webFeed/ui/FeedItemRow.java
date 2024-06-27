/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.webFeed.ui;

import java.util.Date;
import java.util.Set;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.webFeed.Item;

/**
 * Initial date: Mai 21, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class FeedItemRow {

	private final Item item;
	private final FormLink feedEntryLink;
	private final FormLink commentLink;
	private FormLink toolsLink;
	private FormItem ratingFormItem;

	private String formattedTags;
	private Set<Long> tagKeys;


	public FeedItemRow(Item item, FormLink feedEntryLink, FormLink commentLink) {
		this.item = item;
		this.feedEntryLink = feedEntryLink;
		this.commentLink = commentLink;
	}

	public Item getItem() {
		return item;
	}

	public Date getPublishDate() {
		return item.getPublishDate();
	}

	public String getAuthor() {
		return item.getAuthor();
	}

	public String getChangedFrom() {
		return item.getModifier();
	}

	public FormItem getRatingFormItem() {
		return ratingFormItem;
	}

	public void setRatingFormItem(FormItem ratingFormItem) {
		this.ratingFormItem = ratingFormItem;
	}

	public FeedItemStatusEnum getStatus() {
		FeedItemStatusEnum status;
		if (item.isDraft()) {
			status = FeedItemStatusEnum.draft;
		} else if (item.isScheduled()) {
			status = FeedItemStatusEnum.planned;
		} else {
			status = FeedItemStatusEnum.published;
		}
		return status;
	}

	public FormLink getFeedEntryLink() {
		return feedEntryLink;
	}

	public FormLink getCommentLink() {
		return commentLink;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	public Set<Long> getTagKeys() {
		return tagKeys;
	}

	public void setTagKeys(Set<Long> tagKeys) {
		this.tagKeys = tagKeys;
	}

	public String getFormattedTags() {
		return formattedTags;
	}

	public void setFormattedTags(String formattedTags) {
		this.formattedTags = formattedTags;
	}
}
