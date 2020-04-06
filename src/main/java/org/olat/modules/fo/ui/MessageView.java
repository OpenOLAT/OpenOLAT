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
package org.olat.modules.fo.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.modules.fo.MessageLight;
import org.olat.user.DisplayPortraitController;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 11.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MessageView extends MessageLightView {
	
	private final String body;
	
	private String formattedCreationDate;
	private String formattedLastModified;
	
	private boolean modified;
	private String modifierFirstName;
	private String modifierLastName;
	private String modifierPseudonym;
	
	private String creatorFirstname;
	private String creatorLastname;
	
	private boolean author;
	private boolean threadTop;
	private boolean closed;
	private boolean moved;
	
	private List<VFSItem> attachments;
	private VFSContainer messageContainer;
	
	private DisplayPortraitController portrait;
	
	public MessageView(MessageLight message, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(message, userPropertyHandlers, locale);
		body = message.getBody();
	}

	public String getBody() {
		return body;
	}

	public String getFormattedCreationDate() {
		return formattedCreationDate;
	}

	public void setFormattedCreationDate(String formattedCreationDate) {
		this.formattedCreationDate = formattedCreationDate;
	}

	public String getFormattedLastModified() {
		return formattedLastModified;
	}

	public void setFormattedLastModified(String formattedLastModified) {
		this.formattedLastModified = formattedLastModified;
	}

	public String getCreatorFirstname() {
		return creatorFirstname;
	}

	public void setCreatorFirstname(String creatorFirstname) {
		this.creatorFirstname = creatorFirstname;
	}

	public String getCreatorLastname() {
		return creatorLastname;
	}

	public void setCreatorLastname(String creatorLastname) {
		this.creatorLastname = creatorLastname;
	}

	public boolean isMoved() {
		return moved;
	}

	public void setMoved(boolean moved) {
		this.moved = moved;
	}

	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

	public String getModifierFirstName() {
		return modifierFirstName;
	}

	public void setModifierFirstName(String modifierFirstName) {
		this.modifierFirstName = modifierFirstName;
	}

	public String getModifierLastName() {
		return modifierLastName;
	}

	public void setModifierLastName(String modifierLastName) {
		this.modifierLastName = modifierLastName;
	}

	public String getModifierPseudonym() {
		return modifierPseudonym;
	}

	public void setModifierPseudonym(String modifierPseudonym) {
		this.modifierPseudonym = modifierPseudonym;
	}

	public boolean isAuthor() {
		return author;
	}

	public void setAuthor(boolean author) {
		this.author = author;
	}

	@Override
	public boolean isThreadTop() {
		return threadTop;
	}

	public void setThreadTop(boolean threadTop) {
		this.threadTop = threadTop;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public List<VFSItem> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<VFSItem> attachments) {
		this.attachments = attachments;
	}
	
	public boolean hasAttachments() {
		return attachments != null && attachments.size() > 0;
	}

	public VFSContainer getMessageContainer() {
		return messageContainer;
	}

	public void setMessageContainer(VFSContainer msgContainer) {
		this.messageContainer = msgContainer;
	}

	public DisplayPortraitController getPortrait() {
		return portrait;
	}

	public void setPortrait(DisplayPortraitController portrait) {
		this.portrait = portrait;
	}

}
