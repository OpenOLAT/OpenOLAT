package org.olat.admin.sysinfo.model;

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
			case key: return returnNullSafe(row.getKey());
			case name: return returnNullSafe(row.getName());
			case size: return returnNullSafe(row.getSize());
			case path: return returnNullSafe(row.getPathInfo());
			case author: return returnNullSafe(row.getAuthor() != null ? row.getAuthor().getUser().getFirstName() + " " + row.getAuthor().getUser().getLastName() : null);
			case revision: return returnNullSafe(row.isRevision());
			case fileCategory: return returnNullSafe(row.getFileCategory());
			case fileType: return returnNullSafe(row.getFileType());
			case license: return returnNullSafe(row.getLicense() != null ? LicenseUIFactory.translate(row.getLicense(), locale) : null);
			case lastModifiedAt: return returnNullSafe(row.getLastModifiedAt());
			case createdAt: return returnNullSafe(row.getCreatedAt());
			case age: return returnNullSafe(row.getAge());
			case trashed: return returnNullSafe(row.isTrashed());
			case uuid: return returnNullSafe(row.getUuid());
			case downloadCount: return returnNullSafe(row.getDownloadCount());
			case title: return returnNullSafe(row.getTitle());
			case comment: return returnNullSafe(row.getComment());
			case publisher: return returnNullSafe(row.getPublisher());
			case creator: return returnNullSafe(row.getCreator());
			case source: return returnNullSafe(row.getSource());
			case pubDate: return returnNullSafe(row.getPubDate());
			case language: return returnNullSafe(row.getLanguage());
			case locked: return returnNullSafe(row.isLocked());
			case lockedBy: return returnNullSafe(row.getLockedBy() != null ? row.getLockedBy().getUser().getFirstName() + " " + row.getLockedBy().getUser().getLastName() : null);
			case lockedAt: return returnNullSafe(row.getLockedAt());
			case revisionComment: return returnNullSafe(row.getRevisionComment());
			case revisionNr: return returnNullSafe(row.getRevisionNr());
			case sendMail: return returnNullSafe(row.getAuthor() != null ? StringHelper.containsNonWhitespace(row.getAuthor().getUser().getEmail()) ? true : false : false);
			
			default: return "ERROR";
		}
	}
	
	@Override 
	public DefaultFlexiTableDataModel<LargeFilesTableContentRow> createCopyWithEmptyList() {
		return new LargeFilesTableModel(getTableColumnModel(), locale);
	}
	
	private Object returnNullSafe(Object o) {
		return o != null ? o : "";
	}
	
	public enum LargeFilesTableColumns implements FlexiSortableColumnDef {
		key("largefiles.id"),
		name("largefiles.name"),
		size("largefiles.size"),
		path("largefiles.path"),
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
