/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.commons.info.model;

import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for InfoMessage
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

	public Long getResId();

	public String getResName();

	public String getResSubPath();

	public String getBusinessPath();

	public Identity getAuthor();
	
	public Identity getModifier();
	
	public void setModifier(Identity modifier);

	public OLATResourceable getOLATResourceable();
}
