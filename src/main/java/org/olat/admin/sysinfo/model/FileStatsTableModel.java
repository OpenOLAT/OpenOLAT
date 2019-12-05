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

public class FileStatsTableModel extends DefaultFlexiTableDataModel<FileStatsTableContentRow>
implements SortableFlexiTableDataModel<FileStatsTableContentRow> {
	
	private final Locale locale;
	
	public FileStatsTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<FileStatsTableContentRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		FileStatsTableContentRow file = getObject(row);
		return getValueAt(file, col);
	}
	
	@Override
	public Object getValueAt(FileStatsTableContentRow row, int col) {
		switch(FileStatsTableColumns.values()[col]) {
			case key: return notNull(row.getKey());
			case name: return notNull(row.getName());
			case size: return notNull(row.getSize());
			case path: return notNull(row.getPath());
			case author: return notNull(row.getAuthor() != null ? row.getAuthor().getUser().getFirstName() + " " + row.getAuthor().getUser().getLastName() : null);
			case revision: return notNull(row.isRevision());
			case fileCategory: return notNull(row.getFileCategory());
			case fileType: return notNull(row.getFileType());
			case license: return notNull(row.getLicense() != null ? LicenseUIFactory.translate(row.getLicense(), locale) : null);
			case lastModifiedAt: return notNull(row.getLastModifiedAt());
			case createdAt: return notNull(row.getCreatedAt());
			case trashed: return notNull(row.isTrashed());
			case uuid: return notNull(row.getUuid());
			case downloadCount: return notNull(row.getDownloadCount());
			case title: return notNull(row.getTitle());
			case comment: return notNull(row.getComment());
			case publisher: return notNull(row.getPublisher());
			case creator: return notNull(row.getCreator());
			case source: return notNull(row.getSource());
			case pubDate: return notNull(row.getPubDate());
			case language: return notNull(row.getLanguage());
			case locked: return notNull(row.isLocked());
			case lockedBy: return notNull(row.getLockedBy());
			case lockedAt: return notNull(row.getLockedAt());
			case revisionComment: return notNull(row.getRevisionComment());
			case revisionNr: return notNull(row.getRevisionNr());	
			
			default: return "ERROR";
		}
	}
	
	@Override 
	public DefaultFlexiTableDataModel<FileStatsTableContentRow> createCopyWithEmptyList() {
		return new FileStatsTableModel(getTableColumnModel(), locale);
	}
	
	private Object notNull(Object o) {
		return o != null ? o : "";
	}
	
	public enum FileStatsTableColumns implements FlexiSortableColumnDef {
		key("largefiles.id"),
		name("largefiles.name"),
		size("largefiles.size"),
		path("largefiles.path"),
		author("largefiles.author"),
		revision("largefiles.revision"),
		fileType("largefiles.filetype"),
		fileCategory("largefiles.filecategory"),
		createdAt("largefiles.createdat"),
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
		revisionComment("largefiles.revisioncomment");
		
		private final String i18nHeaderKey;
		
		private FileStatsTableColumns(String i18nHeaderKey) {
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
