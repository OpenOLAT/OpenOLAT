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
package org.olat.modules.message.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.message.AssessmentMessage;
import org.olat.modules.message.AssessmentMessageStatusEnum;
import org.olat.modules.message.model.AssessmentMessageInfos;

/**
 * 
 * Initial date: 15 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentMessageRow {
	
	private final String authorFullName;
	private AssessmentMessageInfos messageInfos;
	private AssessmentMessageStatusEnum messageStatus;
	
	private FormLink toolLink;
	
	public AssessmentMessageRow(AssessmentMessageInfos messageInfos, String authorFullName,
			AssessmentMessageStatusEnum status) {
		this.messageStatus = status;
		this.messageInfos = messageInfos;
		this.authorFullName = authorFullName;
	}
	
	public void updateInfos(AssessmentMessageInfos infos, AssessmentMessageStatusEnum status) {
		this.messageInfos = infos;
		this.messageStatus = status;
	}
	
	public String getAuthorFullName() {
		return authorFullName;
	}
	
	public Long getKey() {
		return messageInfos.getMessage().getKey();
	}
	
	public AssessmentMessage getMessage() {
		return messageInfos.getMessage();
	}
	
	public AssessmentMessageStatusEnum getStatus() {
		return messageStatus;
	}
	
	public String getContent() {
		return messageInfos.getMessage().getMessage();
	}
	
	public Date getCreationDate() {
		return messageInfos.getMessage().getCreationDate();
	}
	
	public Date getPublicationDate() {
		return messageInfos.getMessage().getPublicationDate();
	}
	
	public Date getExpirationDate() {
		return messageInfos.getMessage().getExpirationDate();
	}
	
	public long getNumOfRead() {
		return messageInfos.getNumOfRead();
	}

	public FormLink getToolLink() {
		return toolLink;
	}

	public void setToolLink(FormLink toolLink) {
		this.toolLink = toolLink;
	}
}
