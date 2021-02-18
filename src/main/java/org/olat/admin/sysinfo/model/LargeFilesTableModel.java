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
package org.olat.admin.sysinfo.model;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 23 Dec 2019<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class LargeFilesTableModel extends DefaultFlexiTableDataModel<LargeFilesTableContentRow>
implements SortableFlexiTableDataModel<LargeFilesTableContentRow> {

	private final Locale locale;

	public LargeFilesTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<LargeFilesTableContentRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		LargeFilesTableContentRow file = getObject(row);
		return getValueAt(file, col);
	}

	@Override
	public Object getValueAt(LargeFilesTableContentRow row, int col) {
		switch(LargeFilesTableColumns.values()[col]) {
		case key: return row.getKey();
		case name: return row.getName();
		case size: return row.getSize();
		case context: return row.getContext();
		case contextInfo: return row.getContextInfo();
		case path: return row.getPath();
		case showPath: return row.getShowPath();
		case author: return row.getFileInitializedBy() != null ? row.getFileInitializedBy().getUser().getFirstName() + " " + row.getFileInitializedBy().getUser().getLastName() : null;
		case revision: return row.isRevision();
		case fileCategory: return row.getFileCategory();
		case fileType: return row.getFileType();
		case license: return row.getLicense() != null ? LicenseUIFactory.translate(row.getLicense(), locale) : null;
		case lastModifiedAt: return (row.getLastModifiedAt());
		case createdAt: return (row.getCreatedAt());
		case age: return row.getAge();
		case trashed: return row.isTrashed();
		case uuid: return row.getUuid();
		case downloadCount: return row.getDownloadCount();
		case title: return row.getTitle();
		case comment: return row.getComment();
		case publisher: return row.getPublisher();
		case creator: return row.getCreator();
		case source: return row.getSource();
		case pubDate: return returnNullSafeDate(row.getPubDate());
		case language: return row.getLanguage();
		case locked: return row.isLocked();
		case lockedBy: return row.getLockedBy() != null ? row.getLockedBy().getUser().getFirstName() + " " + row.getLockedBy().getUser().getLastName() : null;
		case lockedAt: return returnNullSafeDate(row.getLockedAt());
		case revisionComment: return row.getRevisionComment();
		case revisionNr: return row.getRevisionNr();
		case sendMail: return row.getFileInitializedBy() != null ? StringHelper.containsNonWhitespace(row.getFileInitializedBy().getUser().getEmail()) ? true : false : false;

		default: return "ERROR";
		}
	}

	@Override 
	public DefaultFlexiTableDataModel<LargeFilesTableContentRow> createCopyWithEmptyList() {
		return new LargeFilesTableModel(getTableColumnModel(), locale);
	}

	private Object returnNullSafeDate(Date d) {
		if (d == null || d.getTime() - 172800000 < 0) {
			return null;
		}
		return d;
	}

	public enum LargeFilesTableColumns implements FlexiSortableColumnDef {
		key("largefiles.id"),
		name("largefiles.name"),
		size("largefiles.size"),
		path("largefiles.path"),
		showPath("largefiles.path"),
		context("largefiles.context"),
		contextInfo("largefiles.context"),
		author("largefiles.author"),
		revision("largefiles.revision"),
		fileType("largefiles.filetype"),
		fileCategory("largefiles.filecategory"),
		createdAt("largefiles.createdat"),
		age("largefiles.age"),
		lastModifiedAt("largefiles.lastmodifiedat"),
		license("largefiles.license"),
		trashed("largefiles.trashed"),
		uuid("largefiles.uuid"),
		downloadCount("largefiles.downloads"),
		title("largefiles.title"),
		comment("largefiles.comment"),
		publisher("largefiles.publisher"),
		creator("largefiles.creator"),
		source("largefiles.source"),
		pubDate("largefiles.publishedAt"),
		language("largefiles.language"),
		locked("largefiles.locked"),
		lockedBy("largefiles.lockedby"),
		lockedAt("largefiles.lockedat"),
		revisionNr("largefiles.revisionnr"),
		revisionComment("largefiles.revisioncomment"),
		sendMail("largefiles.sendmail");

		private final String i18nHeaderKey;

		private LargeFilesTableColumns(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}

	}
}
