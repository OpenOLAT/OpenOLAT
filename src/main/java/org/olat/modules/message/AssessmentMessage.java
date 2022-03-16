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
package org.olat.modules.message;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 14 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentMessage extends ModifiedInfo, CreateInfo {
	
	public Long getKey();
	
	public String getMessage();

	public void setMessage(String message);

	public Date getPublicationDate();

	public void setPublicationDate(Date publicationDate);

	public Date getExpirationDate();

	public void setExpirationDate(Date expirationDate);

	public AssessmentMessagePublicationEnum getPublicationType();

	public void setPublicationType(AssessmentMessagePublicationEnum publicationType);
	
	
	public RepositoryEntry getEntry();

	public String getResSubPath();

	public Identity getAuthor();

}
