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

package org.olat.commons.info;

import java.io.File;
import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;

/**
 * 
 * <P>
 * Initial Date:  27 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
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

	public void setAttachments(File[] attachments);

	public Long getResId();

	public String getResName();

	public String getResSubPath();

	public String getBusinessPath();

	public Identity getAuthor();
	
	public Identity getModifier();
	
	public void setModifier(Identity modifier);

	public OLATResourceable getOLATResourceable();

}
