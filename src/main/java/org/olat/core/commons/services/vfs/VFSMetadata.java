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

import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

import java.util.Date;

/**
 * 
 * Initial date: 11 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface VFSMetadata extends VFSMetadataRef, ModifiedInfo, CreateInfo {

	int TRANSCODING_STATUS_WAITING = -1;
	int TRANSCODING_STATUS_STARTED = 0;
	int TRANSCODING_STATUS_DONE = 100;
	int TRANSCODING_STATUS_INEFFICIENT = -2;
	int TRANSCODING_STATUS_ERROR = -3;
	int TRANSCODING_STATUS_TIMEOUT = -4;

	public boolean isDeleted();
	
	public String getUuid();
	
	public void setUuid(String id);
	
	/**
	 * The relative path in bcroot doesn't start with /. / is reserved
	 * for the bcroot directory itself.
	 * 
	 * @return The relative path of the file without the filename
	 */
	public String getRelativePath();
	
	/**
	 * @return The name of the file
	 */
	public String getFilename();
	
	public Date getFileLastModified();
	
	public Identity getFileLastModifiedBy();
	
	public Identity getFileInitializedBy();
	
	public void setFileInitializedBy(Identity identity);
	
	public long getFileSize();

	Integer getTranscodingStatus();

	boolean isInTranscoding();

	boolean isTranscoded();

	public boolean isDirectory();
	
	public String getIconCssClass();
	
	public String getUri();
	
	/**
	 * Return the protocol used to save and retrive the file.
	 * "file" ist for the standard operations done by OpenOLAT
	 * VFS implementation.
	 * 
	 * @return the procotole
	 */
	public String getProtocol();
	
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
	
	/**
	 * @return true if the thumbnails cannot be generated for this file.
	 */
	public Boolean getCannotGenerateThumbnails();
	
	/**
	 * @param val true if the thumbnails cannot be generated for this file.
	 */
	public void setCannotGenerateThumbnails(Boolean val);
	
	
	public LicenseType getLicenseType();
	
	public void setLicenseType(LicenseType type);
	
	public String getLicenseTypeName();
	
	public void setLicenseTypeName(String name);
	
	public String getLicenseText();
	
	public void setLicenseText(String text);
	
	public String getLicensor();
	
	public void setLicensor(String licensor);
	
	public Date getExpirationDate();
	
	public void setExpirationDate(Date expiration);

	/**
	 * @return true if the file is locked (VFS).
	 */
	public boolean isLocked();
	
	public void setLocked(boolean locked);
	
	/**
	 * @return The person who locked the file or null
	 */
	public Identity getLockedBy();
	
	public void setLockedBy(Identity lockedBy);
	
	public Date getLockedDate();
	
	public void setLockedDate(Date date);
	
	
	public long getRevisionNr();
	
	public void setRevisionNr(long nr);
	
	/**
	 * Indicates if it is a temporary version.
	 * 
	 * @return the number of the temporary revision or null
	 */
	public Integer getRevisionTempNr();
	
	public void setRevisionTempNr(Integer nr);
	
	public String getRevisionComment();
	
	public void setRevisionComment(String text);
	
	
	public String getMigrated();
	
	public void copyValues(VFSMetadata metadata, boolean override);

}
