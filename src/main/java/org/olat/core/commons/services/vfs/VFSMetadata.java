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

import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 11 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface VFSMetadata extends VFSMetadataRef, ModifiedInfo, CreateInfo {
	
	public String getUuid();
	
	public void setUuid(String id);
	
	public String getRelativePath();
	
	public String getFilename();
	
	public Date getFileLastModified();
	
	public long getFileSize();
	
	public boolean isDirectory();
	
	public String getIconCssClass();
	
	public String getUri();
	
	public String getProtocol();
	
	public Identity getAuthor();
	
	public void setAuthor(Identity author);
	
	public String getTitle();
	
	public void setTitle(String title);
	
	public String getComment();
	
	public void setComment(String text);
	
	public int getDownloadCount();
	
	public String getCreator();
	
	public void setCreator(String creator);
	
	public String getPublisher();
	
	public void setPublisher(String publisher);
	
	public String getCity();
	
	public void setCity(String city);
	
	public String[] getPublicationDate();
	
	public void setPublicationDate(String month, String year);
	
	
	public String getUrl();

	public void setUrl(String url);

	public String getSource();

	public void setSource(String source);

	public String getLanguage();

	public void setLanguage(String language);
	
	public String getPages();
	
	public void setPages(String pages);
	
	
	public Boolean getCannotGenerateThumbnails();
	
	public void setCannotGenerateThumbnails(Boolean val);
	
	
	public LicenseType getLicenseType();
	
	public void setLicenseType(LicenseType type);
	
	public String getLicenseTypeName();
	
	public void setLicenseTypeName(String name);
	
	public String getLicenseText();
	
	public void setLicenseText(String text);
	
	public String getLicensor();
	
	public void setLicensor(String licensor);
	
	
	public boolean isLocked();
	
	public void setLocked(boolean locked);
	
	public Identity getLockedBy();
	
	public void setLockedBy(Identity lockedBy);
	
	public Date getLockedDate();
	
	public void setLockedDate(Date date);
	
	public void copyValues(VFSMetadata metadata);

}
