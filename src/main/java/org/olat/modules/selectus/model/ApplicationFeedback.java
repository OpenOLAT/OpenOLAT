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
package org.olat.modules.selectus.model;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 24 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ApplicationFeedback  extends CreateInfo, ModifiedInfo {
	
	public Long getKey();
	
	public String getComment();
	
	public void setComment(String comment);
	
	public Date getCommentDate();

	public void setCommentDate(Date commentDate);

	public Date getDeadline();

	public void setDeadline(Date deadline);
	
	public ReferenceStatus getReferenceStatus();
	
	public void setReferenceStatus(ReferenceStatus status);
	
	public Date getRequest();

	public void setRequest(Date request);

	public Date getLastReminder();

	public void setLastReminder(Date lastReminder);
	
	public Application getApplication();
	
	public Identity getIdentity();
	
	public ApplicationsFeedbackConfiguration getConfiguration();
	
}
