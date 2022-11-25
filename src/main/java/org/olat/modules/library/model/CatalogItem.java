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
package org.olat.modules.library.model;

import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.modules.library.LibraryManager;

/**
 * A catolog item represents a file and stores the relative path and the file's
 * metadata. It is meant to be pushed to the view (VC).
 * 
 * <P>
 * Initial Date: Jun 17, 2009 <br>
 * 
 * @author gwassmann
 */
public class CatalogItem {
	private static final Logger log = Tracing.createLoggerFor(CatalogItem.class);
	
	private final VFSMetadata metadata;
	
	private final String uuid;
	private final String filename;
	private final String relativePath;
	private final Date publicationDate;
	private final Date localizedLastModified;
	
	private final String cssClass;
	private boolean selected;
	
	private final long numOfComments;
	private final ItemRating ratings;
	private final boolean thumbnailAvailable;
	
	/**
	 * Creates a catalog item based on a file
	 * 
	 * @param file The file of the catalog
	 */
	public CatalogItem(VFSMetadata metadata, long numOfComments, ItemRating ratings, boolean thumbnailAvailable) {
		this.metadata = metadata;
		this.ratings = ratings;
		this.numOfComments = numOfComments;
		this.thumbnailAvailable = thumbnailAvailable;
		uuid = metadata.getUuid();
		localizedLastModified = metadata.getLastModified();
		filename = metadata.getFilename();
		relativePath = metadata.getRelativePath() + "/" + filename;
		cssClass = metadata.getIconCssClass();
		publicationDate = calculateDateFromPublicationDateArray(metadata.getPublicationDate());
	}
	
	private Date calculateDateFromPublicationDateArray(String[] pubDateArray) {
		if(pubDateArray == null || pubDateArray.length == 0) return null;
		try {
			Calendar cal = Calendar.getInstance();
			cal.clear();
			if(pubDateArray.length > 0 && pubDateArray[0] != null) {
				cal.set(Calendar.YEAR, Integer.parseInt(pubDateArray[0]));
			}
			if(pubDateArray.length > 1 && pubDateArray[1] != null) {
				cal.set(Calendar.MONTH, Integer.parseInt(pubDateArray[1]));
			}
			if(pubDateArray.length > 2 && pubDateArray[2] != null) {
				cal.set(Calendar.DATE, Integer.parseInt(pubDateArray[2]));
			}
			return cal.getTime();
		} catch (NumberFormatException e) {
			// can happen
			return null;
		}
	}
	
	public String getId() {
		return metadata == null ? "" : metadata.getUuid();
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public long getNumOfComments() {
		return numOfComments;
	}
	
	public ItemRating getRatings() {
		return ratings;
	}
	
	public String getFilename() {
		return filename;
	}

	/**
	 * Relative path getter. The path starts with /
	 * 
	 * @return The relative path
	 */
	public String getRelativePath() {
		return relativePath;
	}
	
	public String getAbsolutePathUUID() {
		try {
			StringBuilder sb = new StringBuilder(128);
			sb.append(Settings.getServerContextPathURI())
				.append("/library/")
				.append(metadata.getUuid())
				.append("/")
				.append(filename);
			return sb.toString();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	/**
	 * Relative path to shared folder
	 * 
	 * @return The relative path
	 */
	public String getShortPath() {
		String directoryPath = CoreSpringFactory.getImpl(LibraryManager.class).getDirectoryPath();
		int index = relativePath.indexOf(directoryPath);
		int start = index + directoryPath.length();
		return relativePath.substring(start);
	}

	/**
	 * Meta info getter
	 * 
	 * @return The meta info
	 */
	public VFSMetadata getMetaInfo() {
		return metadata;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return metadata.getFilename();
	}
	
	public String getComment() {
		return metadata.getComment();
	}
	
	public Date getPubDate() {
		return publicationDate;
	}
	
	/**
	 * @return Returns the folder path relative to the shared folder
	 */
	public String getFolder() {
		String shortPath = getShortPath();
		try {
			int end = shortPath.lastIndexOf('/');
			return shortPath.substring(1, end);
		} catch (Exception ex) {
			//Make it absolute secure
			return "/";
		}
	}

	/**
	 * @return Returns the localizedLastModified in full format.
	 */
	public Date getLocalizedLastModified() {
		return localizedLastModified;
	}

	/**
	 * Example: mm: 10, yyyy: 2009 => 10/2009
	 * 
	 * @return The publication date as string
	 */
	public String getPublicationDate() {
		String[] pubDate = metadata.getPublicationDate();
		String formattedDate = null;
		if (pubDate[0] != null) {
			formattedDate = pubDate[0];
		}
		if (pubDate[1] != null) {
			if (formattedDate != null) {
				formattedDate += "/" + pubDate[1];
			}
			else {
				formattedDate = pubDate[1];
			}
		}
		return formattedDate;
	}
	
	/**
	 * @return Returns the cssClass.
	 */
	public String getCssClass() {
		return cssClass;
	}
	
	/**
	 * @return The name to be displayed besides the file icon 
	 */
	public String getDisplayName() {
		String title = metadata.getTitle();
		if (title != null && !title.isEmpty()) {
			return title;
		}
		return getName();
	}
	
	public boolean isThumbnailAvailable() {
		return thumbnailAvailable;
	}

	@Override
	public int hashCode() {
		return uuid == null ? 283498 : uuid.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof CatalogItem) {
			CatalogItem item = (CatalogItem)obj;
			return uuid != null && uuid.equals(item.uuid);
		}
		return false;
	}
}
