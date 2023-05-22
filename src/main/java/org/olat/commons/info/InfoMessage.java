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

package org.olat.commons.info;

import java.util.Date;
import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;

/**
 * 
 * <P>
 * Initial Date:  27 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public interface InfoMessage {
	
	public Long getKey();
	
	public Date getCreationDate();
	
	public Date getModificationDate();
	
	public void setModificationDate(Date modificationDate);
	
	public String getTitle();

	public void setTitle(String title);

	public String getMessage();

	public void setMessage(String message);
	
	public String getAttachmentPath();
	
	public void setAttachmentPath(String path);

	public Long getResId();

	public String getResName();

	public String getResSubPath();

	public String getBusinessPath();

	/**
	 * retrieve status of infoMessage, if it is published or not
	 *
	 * @return true/false
	 */
	boolean isPublished();

	/**
	 * set published status of infoMessage
	 *
	 * @param published true/false
	 */
	void setPublished(boolean published);

	/**
	 * retrieve Date on which the infoMessage got published
	 *
	 * @return date
	 */
	Date getPublishDate();

	/**
	 * set date on which the infoMessage got published
	 *
	 * @param publishDate
	 */
	void setPublishDate(Date publishDate);

	/**
	 * retrieve list of recipients to whom the infoMessage should be sent to via email
	 *
	 * @return Comma separated string. Possible values: {null, subscriber, owner, coach, participant}
	 */
	String getSendMailTo();

	/**
	 * sett of recipients to whom the infoMessage should be sent to via email
	 *
	 * @param sendMailTo Comma separated string. Possible values: {null, subscriber, owner, coach, participant}
	 */
	void setSendMailTo(String sendMailTo);

	/**
	 * retrieve a set of InfoMessageToGroup objects which belong to a specific infoMessage
	 *
	 * @return set of infoMessageToGroup
	 */
	Set<InfoMessageToGroup> getGroups();

	/**
	 * retrieve a set of InfoMessageToCurriculumElement objects which belong to a specific infoMessage
	 *
	 * @return set of InfoMessageToCurriculumElement
	 */
	Set<InfoMessageToCurriculumElement> getCurriculumElements();

	public Identity getAuthor();
	
	public Identity getModifier();
	
	public void setModifier(Identity modifier);

	public OLATResourceable getOLATResourceable();
}
