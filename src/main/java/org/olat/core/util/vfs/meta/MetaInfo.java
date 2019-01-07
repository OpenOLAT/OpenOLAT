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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.core.util.vfs.meta;

import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;

public interface MetaInfo {

	/**
	 * Rename the given meta info file
	 * 
	 * @param meta
	 * @param newName
	 */
	public void rename(String newName);

	/**
	 * Move/Copy the given meta info to the target directory.
	 * 
	 * @param targetDir
	 * @param move
	 */
	public void moveCopyToDir(VFSItem target, boolean move);

	/**
	 * Delete all associated meta info including sub files/directories
	 */
	public void deleteAll();

	/**
	 * Copy values from froMeta into this object except name, download count and UUID.
	 * 
	 * @param fromMeta the metadata to copy from
	 */
	public void copyValues(MetaInfo fromMeta);

	/**
	 * Delete this meta info
	 * 
	 * @return True upon success.
	 */
	public boolean delete();
	
	/**
	 * @return the unique id of the file or create one if it previously not exists
	 */
	public String getUUID();
	
	/**
	 * @return The last modification date of the metadata
	 */
	public Date getMetaLastModified();

	/**
	 * @return name of the initial author (OLAT user name)
	 */
	public String getAuthor();
	
	public Long getAuthorIdentityKey();
	
	public boolean hasAuthorIdentity();

	/**
	 * Corresponds to DublinCore:description
	 * 
	 * @return comment
	 */
	public String getComment();

	public String getName();

	/**
	 * DublinCore compatible
	 */
	public String getTitle();

	/**
	 * DublinCore compatible
	 */
	public String getPublisher();

	/**
	 * In this context, the creator is the person or organization that is
	 * primarily responsible for making the content. The author, by contrast, is
	 * an OLAT user who uploaded the file.
	 * 
	 * @return The writer of the resource
	 */
	public String getCreator();

	/**
	 * DublinCore compatible
	 */
	public String getSource();

	/**
	 * @return The city or location of publication
	 */
	public String getCity();

	public String getPages();

	/**
	 * DublinCore compatible
	 */
	public String getLanguage();

	public String getUrl();

	/**
	 * Corresponds to DublinCore:date + refinement
	 * 
	 * @return The date in form of a {year, month} array.
	 */
	public String[] getPublicationDate();

	/**
	 * @return True if this is a directory
	 */
	public boolean isDirectory();

	/**
	 * @return Last modified timestamp
	 */
	public long getLastModified();

	/**
	 * @return size of file
	 */
	public long getSize();

	/**
	 * @return formatted representation of size of file
	 */
	public String getFormattedSize();
	
	public void setAuthor(Identity identy);

	/**
	 * @param string
	 */
	public void setComment(String string);

	public void setTitle(String title);

	public void setPublisher(String publisher);

	public void setCreator(String creator);

	public void setSource(String source);

	public void setCity(String city);

	public void setPages(String pages);

	public void setLanguage(String language);

	public void setUrl(String url);

	public void setPublicationDate(String month, String year);
	
	public String getLicenseTypeKey();
	
	public void setLicenseTypeKey(String key);
	
	public String getLicenseTypeName();
	
	public void setLicenseTypeName(String name);
	
	public String getLicenseText();
	
	public void setLicenseText(String text);
	
	public String getLicensor();
	
	public void setLicensor(String licensor);
	
	
	public boolean isThumbnailAvailable();
	
	/**
	 * The 
	 * 
	 * 
	 * @param maxWidth
	 * @param maxHeight
	 * @param fill True if you want to fill the surface defined above (overflow are cut)
	 * @return
	 */
	public VFSLeaf getThumbnail(int maxWidth, int maxHeight, boolean fill);
	
	/**
	 * Thumbnails are cleared and the metadata file is written on the disk
	 */
	public void clearThumbnails();

	/**
	 * Writes the meta data to file. If no changes have been made, does not
	 * write anything.
	 * 
	 * @return True upon success.
	 */
	public boolean write();

	/**
	 * Increases the download count by one.
	 */
	public void increaseDownloadCount();

	/**
	 * @return The download count
	 */
	public int getDownloadCount();
	
	/**
	 * @return An icon css class that represents this type of file
	 */
	public String getIconCssClass();
}