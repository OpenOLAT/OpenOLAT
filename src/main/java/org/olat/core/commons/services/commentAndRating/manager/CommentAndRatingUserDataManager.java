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
package org.olat.core.commons.services.commentAndRating.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.manager.PageDAO;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.manager.QuestionItemDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserDataExportable;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CommentAndRatingUserDataManager implements UserDataDeletable, UserDataExportable {
	
	private static final OLog log = Tracing.createLoggerFor(CommentAndRatingUserDataManager.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private QuestionItemDAO questionItemDao;
	@Autowired
	private UserRatingsDAO userRatingsDao;
	@Autowired
	private UserCommentsDAO userCommentsDao;
	@Autowired
	private RepositoryManager repositoryManager;
	
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		int rows = userRatingsDao.deleteRatings(identity);
		log.audit(rows + " rating deleted");
		int comments = userCommentsDao.deleteAllComments(identity);
		log.audit(comments + " rating erased");
	}

	@Override
	public String getExporterID() {
		return "comments.ratings";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		exportRatings(identity, manifest, archiveDirectory);
		exportComments(identity, manifest, archiveDirectory);
	}
	
	private void exportComments(Identity identity, ManifestBuilder manifest, File archiveDirectory) {
		List<UserComment> comments = userCommentsDao.getComments(identity);
		if(comments == null || comments.isEmpty()) return;
		dbInstance.commitAndCloseSession();
		
		File noteArchive = new File(archiveDirectory, "Comments.xlsx");
		try(OutputStream out = new FileOutputStream(noteArchive);
			OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);
			
			Row header = sheet.newRow();
			header.addCell(0, "Comment");
			header.addCell(1, "Resource");
			header.addCell(2, "URL");
			
			for(UserComment comment:comments) {
				Row row = sheet.newRow();
				row.addCell(0, comment.getComment());
				Location location = resolveLocation(comment.getResName(), comment.getResId());
				if(StringHelper.containsNonWhitespace(location.getName())) {
					row.addCell(1, location.getName());
				}
				if(StringHelper.containsNonWhitespace(location.getUrl())) {
					row.addCell(2, location.getUrl());
				}
			}
		} catch (IOException e) {
			log.error("Unable to export xlsx", e);
		}
		manifest.appendFile("Bookings.xlsx");
	}
	
	private void exportRatings(Identity identity, ManifestBuilder manifest, File archiveDirectory) {
		List<UserRating> ratings = userRatingsDao.getAllRatings(identity);
		if(ratings == null || ratings.isEmpty()) return;
		dbInstance.commitAndCloseSession();
		
		File ratingsArchive = new File(archiveDirectory, "Ratings.xlsx");
		try(OutputStream out = new FileOutputStream(ratingsArchive);
			OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);
			
			Row header = sheet.newRow();
			header.addCell(0, "Rating");
			header.addCell(1, "Resource");
			header.addCell(2, "URL");
			
			for(UserRating rating:ratings) {
				Row row = sheet.newRow();
				row.addCell(0, rating.getRating().toString());
				Location location = resolveLocation(rating.getResName(), rating.getResId());
				if(StringHelper.containsNonWhitespace(location.getName())) {
					row.addCell(1, location.getName());
				}
				if(StringHelper.containsNonWhitespace(location.getUrl())) {
					row.addCell(2, location.getUrl());
				}
			}
		} catch (IOException e) {
			log.error("Unable to export xlsx", e);
		}
		manifest.appendFile("Bookings.xlsx");
	}
	
	private Location resolveLocation (String resName, Long resId) {
		String name = null;
		String businessPath = null;
		if("RepositoryEntry".equals(resName)) {
			RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(resId, false);
			if(entry != null) {
				name = entry.getDisplayname();
				businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
			}
		} else if("QuestionItem".equals(resName)) {
			QuestionItem item = questionItemDao.loadById(resId);
			if(item != null) {
				name = item.getTitle();
				businessPath = "[QPool:0][QuestionItem:" + item.getKey() + "]";
			}
		} else if("Page".equals(resName)) {
			Page page = pageDao.loadByKey(resId);
			if(page != null) {
				name = page.getTitle();
				if(page.getSection() != null) {
					Binder binder = page.getSection().getBinder();
					businessPath = "[PortfolioV2:0][Binder:" + binder.getKey() + "][Entries:0][Entry:" + page.getKey() + "]";
				} else {
					businessPath = "[PortfolioV2:0][MyPages:0][Entry:" + page.getKey() + "]";
				}
			}
		} else if("EPDefaultMap".equals(resName)) {
			name = "Mappe (v 1.0)";
		} else if("LibrarySite".equals(resName)) {
			name = "Library";
		} else {
			OLATResourceable resourceable = OresHelper.createOLATResourceableInstance(resName, resId);
			RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(resourceable, false);
			if(entry != null) {
				name = entry.getDisplayname();
				businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
			}
		}

		String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
		return new Location(name, url);
	}
	
	private static class Location {
		
		private final String name;
		private final String url;
		
		public Location(String name, String url) {
			this.url = url;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String getUrl() {
			return url;
		}
	}
}
