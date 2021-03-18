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
package org.olat.core.commons.services.vfs;

import java.util.Date;

import org.olat.core.id.Identity;

/**
 * 
 * Description:<br>
 * This interface describes a revision of a file
 * 
 * <P>
 * Initial Date: 21 sept. 2009 <br>
 * 
 * @author srosse
 */
public interface VFSRevision {

	public Identity getFileLastModifiedBy();
	
	public Identity getFileInitializedBy();

	/**
	 * @return timestamp of the creation of this revision
	 */
	public Date getFileLastModified();
	
	/**
	 * @return The name of the file where this revision is stored.
	 */
	public String getFilename();

	/**
	 * @return the revision number
	 */
	public int getRevisionNr();
	
	public Integer getRevisionTempNr();
	
	public Date getCreationDate();

	/**
	 * @return comment
	 */
	public String getRevisionComment();


	/**
	 * @return size of the file
	 */
	public long getSize();
	
	public VFSMetadata getMetadata();

}
