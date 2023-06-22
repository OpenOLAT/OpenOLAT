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
package org.olat.course.nodes.dialog.ui;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.dialog.DialogElement;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial date: 4 janv. 2018<br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class DialogElementRow extends UserPropertiesRow {

	private final DialogElement element;
	private DownloadLink downloadLink;
	private FormLink toolsLink;
	private Long numOfThreads;
	private Long numOfUnreadThreads;
	private Long numOfMessages;
	private Long numOfUnreadMessages;
	private Date lastActivityDate;
	private boolean thumbnailAvailable;
	private String thumbnailUrl;
	private String modified;
	private String publishedByCardView;
	private String authoredByCardView;
	private FormLink selectLink;

	public DialogElementRow(DialogElement element, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(element.getAuthor(), userPropertyHandlers, locale);
		this.element = element;
	}

	/**
	 * get specific string 'published by' for card view of dialog files
	 *
	 * @return publishedBy name and specific String information
	 */
	public String getPublishedByCardView() {
		return publishedByCardView;
	}

	/**
	 * set specific string 'published by' for card view of dialog files
	 *
	 * @param publishedByCardView
	 */
	public void setPublishedByCardView(String publishedByCardView) {
		this.publishedByCardView = publishedByCardView;
	}

	/**
	 * get specific string 'authored by' for card view of dialog files
	 *
	 * @return authoredBy name and specific String information
	 */
	public String getAuthoredByCardView() {
		return authoredByCardView;
	}

	/**
	 * set specific string 'authored by' for card view of dialog files
	 *
	 * @param authoredByCardView
	 */
	public void setAuthoredByCardView(String authoredByCardView) {
		this.authoredByCardView = authoredByCardView;
	}

	/**
	 * get username, who published the dialog file
	 *
	 * @return String (Firstname, Lastname)
	 */
	public String getPublishedBy() {
		return super.getIdentityProps()[1] + ", " + super.getIdentityProps()[2];
	}

	/**
	 * get filtered String of name, who authored the original dialog file
	 *
	 * @return String name of author, can be null
	 */
	public String getAuthoredBy() {
		return element != null ? StringHelper.xssScan(element.getAuthoredBy()) : null;
	}

	/**
	 * Creationdate of dialog file discussion
	 *
	 * @return date value
	 */
	public Date getCreationDate() {
		return element != null ? element.getCreationDate() : null;
	}

	/**
	 * latest activity of dialog file (including forum activities)
	 *
	 * @return date value
	 */
	public Date getLastActivityDate() {
		return lastActivityDate;
	}

	/**
	 * set latest activity of dialog file (including forum activities)
	 *
	 * @param lastActivityDate
	 */
	public void setLastActivityDate(Date lastActivityDate) {
		this.lastActivityDate = lastActivityDate;
	}

	/**
	 * retrieve filename of dialog element
	 *
	 * @return String value with dialog filename
	 */
	public String getFilename() {
		return element != null ? element.getFilename() : null;
	}

	/**
	 * get size of (dialog) file
	 *
	 * @return long value of size
	 */
	public Long getSize() {
		return element != null ? element.getSize() : null;
	}

	/**
	 * retrieve formlink for respective dialog element row
	 *
	 * @return Formlink object
	 */
	public FormLink getToolsLink() {
		return toolsLink;
	}

	/**
	 * set formlink for respective dialog element row
	 *
	 * @param toolsLink
	 */
	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	/**
	 * retrieve how many threads the row has in its forum
	 *
	 * @return long value, number of threads
	 */
	public Long getNumOfThreads() {
		return numOfThreads;
	}

	/**
	 * set number of threads the row has in its forum
	 *
	 * @param numOfThreads
	 */
	public void setNumOfThreads(Long numOfThreads) {
		this.numOfThreads = numOfThreads;
	}

	/**
	 * retrieve how many unread/new threads the row has in its forum
	 *
	 * @return Long value, number of new threads
	 */
	public Long getNumOfUnreadThreads() {
		return numOfUnreadThreads;
	}

	/**
	 * set number of unread/new threads the row has in its forum
	 *
	 * @param numOfUnreadThreads
	 */
	public void setNumOfUnreadThreads(Long numOfUnreadThreads) {
		this.numOfUnreadThreads = numOfUnreadThreads;
	}

	/**
	 * retrieve how many messages the row has in its forum
	 *
	 * @return long value, number of messages
	 */
	public Long getNumOfMessages() {
		return numOfMessages;
	}

	/**
	 * set number of messages the row has in its forum
	 *
	 * @param numOfMessages
	 */
	public void setNumOfMessages(Long numOfMessages) {
		this.numOfMessages = numOfMessages;
	}

	/**
	 * retrieve how many unread/new messages the row has in its forum
	 *
	 * @return Long value, number of new messages
	 */
	public Long getNumOfUnreadMessages() {
		return numOfUnreadMessages;
	}

	/**
	 * set number of unread/new messages the row has in its forum
	 *
	 * @param numOfUnreadMessages
	 */
	public void setNumOfUnreadMessages(Long numOfUnreadMessages) {
		this.numOfUnreadMessages = numOfUnreadMessages;
	}

	/**
	 * retrieve key/id of selected dialog element
	 *
	 * @return Long value, key/id of dialogElement object
	 */
	public Long getDialogElementKey() {
		return element != null ? element.getKey() : null;
	}

	/**
	 * retrieve DownloadLink object for respective dialogElement
	 * used for downloading file of dialogElement objects
	 *
	 * @return
	 */
	public DownloadLink getDownloadLink() {
		return downloadLink;
	}

	/**
	 * set DownloadLink object for respective dialogElement
	 *
	 * @param downloadLink
	 */
	public void setDownloadLink(DownloadLink downloadLink) {
		this.downloadLink = downloadLink;
	}

	/**
	 * retrieve specific string for 'modified/lastActivity' for card view of dialogElement files
	 *
	 * @return String value containing specific string and also lastActivity datetime
	 */
	public String getModified() {
		return modified;
	}

	/**
	 * set specific string 'modified/lastActivity' for card view of dialogElement files
	 *
	 * @param modified
	 */
	public void setModified(String modified) {
		this.modified = modified;
	}

	/**
	 * retrieve name of dialog file for card view rendering title/name
	 *
	 * @return String value, name of file
	 */
	public String getSelectLinkName() {
		return selectLink != null ? selectLink.getComponent().getComponentName() : null;
	}

	/**
	 * retrieve FormLink, which is used for card view selection
	 *
	 * @return FormLink object, directing to selected dialog file
	 */
	public FormLink getSelectLink() {
		return selectLink;
	}

	/**
	 * set FormLink, which is used for card view selection
	 *
	 * @param selectLink
	 */
	public void setSelectLink(FormLink selectLink) {
		this.selectLink = selectLink;
	}

	/**
	 * retrieve status, if thumbnail is available for dialog file
	 *
	 * @return true is thumbnail is available, false otherwise
	 */
	public boolean isThumbnailAvailable() {
		return thumbnailAvailable;
	}

	/**
	 * set if thumbnail is available or not (true/false)
	 *
	 * @param thumbnailAvailable
	 */
	public void setThumbnailAvailable(boolean thumbnailAvailable) {
		this.thumbnailAvailable = thumbnailAvailable;
	}

	/**
	 * retrieve url for thumbnail
	 *
	 * @return String value of thumbnail URL
	 */
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	/**
	 * set url for thumbnail
	 *
	 * @param thumbnailUrl
	 */
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
}
