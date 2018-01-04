/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.dialog.ui;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.course.nodes.dialog.DialogElement;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 4 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DialogElementRow extends UserPropertiesRow {
	
	private final DialogElement element;
	
	private DownloadLink downloadLink;
	
	private int numOfMessages;
	private int numOfUnreadMessages;
	
	public DialogElementRow(DialogElement element, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(element.getAuthor(), userPropertyHandlers, locale);
		this.element = element;
	}
	
	public Date getCreationDate() {
		return element.getCreationDate();
	}
	
	public String getFilename() {
		return element.getFilename();
	}
	
	public Long getSize() {
		return element.getSize();
	}

	public int getNumOfMessages() {
		return numOfMessages;
	}

	public void setNumOfMessages(int numOfMessages) {
		this.numOfMessages = numOfMessages;
	}

	public int getNumOfUnreadMessages() {
		return numOfUnreadMessages;
	}

	public void setNumOfUnreadMessages(int numOfUnreadMessages) {
		this.numOfUnreadMessages = numOfUnreadMessages;
	}
	
	public Long getDialogElementKey() {
		return element.getKey();
	}

	public DownloadLink getDownloadLink() {
		return downloadLink;
	}

	public void setDownloadLink(DownloadLink downloadLink) {
		this.downloadLink = downloadLink;
	}
}
