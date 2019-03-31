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
package org.olat.modules.library.ui;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.components.link.Link;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSLeaf;
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
	private static final OLog log = Tracing.createLoggerFor(CatalogItem.class);
	
	private final VFSMetadata metadata;
	
	private Date publicationDate;
	
	private String relativePath;
	private String localizedLastModified;
	private String localizedLastModifiedShort;
	private String cssClass;
	private boolean selected;
	private Boolean thumbnailAvailable;
	
	private Link sendMailLink;
	private CommentAndRatingService commentAndRatingService;
	private UserCommentsAndRatingsController commentsAndRatingCtr;

	/**
	 * Creates a catalog item based on a file
	 * 
	 * @param file The file of the catalog
	 */
	public CatalogItem(VFSLeaf file, VFSMetadata metadata, boolean thumbnailAvailable, Locale locale) {
		this.metadata = metadata;
		this.thumbnailAvailable = Boolean.valueOf(thumbnailAvailable);
		if (metadata != null) {
			localizedLastModified = DateFormat.getDateInstance(DateFormat.FULL, locale).format(metadata.getLastModified());
			localizedLastModifiedShort = DateFormat.getDateInstance(DateFormat.SHORT, locale).format(metadata.getLastModified());
			relativePath = file.getRelPath();
			cssClass = metadata.getIconCssClass();
			publicationDate = calculateDateFromPublicationDateArray(metadata.getPublicationDate());
		} else {
			log.warn("Unable to create meta information for file \"" + metadata + "\".");
		}
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
				.append(metadata.getFilename());
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
	public String getLocalizedLastModified() {
		return localizedLastModified;
	}
	
	/**
	 * @return Returns the localizedLastModified in short format.
	 */
	public String getLocalizedLastModifiedShort() {
		return localizedLastModifiedShort;
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
		} else {
			return getName();
		}
	}
	
	public boolean isThumbnailAvailable() {
		return thumbnailAvailable.booleanValue();
	}

	public Link getSendMailLink() {
		return sendMailLink;
	}

	public void setSendMailLink(Link sendMailLink) {
		this.sendMailLink = sendMailLink;
	}

	public CommentAndRatingService getCommentAndRatingService() {
		return commentAndRatingService;
	}

	public void setCommentAndRatingService(CommentAndRatingService commentAndRatingService) {
		this.commentAndRatingService = commentAndRatingService;
	}

	public UserCommentsAndRatingsController getCommentsAndRatingCtr() {
		return commentsAndRatingCtr;
	}

	public void setCommentsAndRatingCtr(UserCommentsAndRatingsController commentsAndRatingCtr) {
		this.commentsAndRatingCtr = commentsAndRatingCtr;
	}
}
