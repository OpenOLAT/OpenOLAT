/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.taxonomy.ui.importwizard;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.manager.importexport.FileLevel;
import org.olat.modules.taxonomy.manager.importexport.FileLevelType;
import org.olat.modules.taxonomy.manager.importexport.FileRow;
import org.olat.modules.taxonomy.manager.importexport.FileTranslation;

/**
 * Table model of the review steps, for level types and levels.
 * 
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class ImportReviewTableModel extends DefaultFlexiTableDataModel<FileRow> {

	private final Locale locale;
	private final Map<FileRow,FormLink> changeLinks;

	public ImportReviewTableModel(FlexiTableColumnModel columnModel, Locale locale, Map<FileRow,FormLink> changeLinks) {
		super(columnModel);
		this.locale = locale;
		this.changeLinks = changeLinks;
	}

	@Override
	public Object getValueAt(int row, int col) {
		FileRow exRow = getObject(row);
		ReviewCols column = ReviewCols.values()[col];
		if(column == ReviewCols.row) {
			return exRow.getRowNum() > 0 ? Integer.valueOf(exRow.getRowNum()) : null;
		} else if(column == ReviewCols.status) {
			FormLink changeLink = changeLinks.get(exRow);
			return changeLink != null ? changeLink : exRow.getStatus();
		} else if(column == ReviewCols.messages) {
			return exRow.getMessages();
		}

		if(exRow instanceof FileLevelType type) {
			return switch(column) {
				case identifier -> type.getIdentifier();
				case title -> type.getDisplayName();
				case type -> String.join(", ", type.getSubTypes());
				default -> null;
			};
		}
		if(exRow instanceof FileLevel level) {
			return switch(column) {
				case path -> level.getPath();
				case identifier -> level.getIdentifier();
				case title -> getTitle(level);
				case type -> level.getType();
				case sortOrder -> level.getSortOrder();
				case images -> getImages(level);
				default -> null;
			};
		}
		return null;
	}

	private String getTitle(FileLevel level) {
		FileTranslation translation = level.getTranslation(locale.getLanguage());
		if(translation != null && StringHelper.containsNonWhitespace(translation.title())) {
			return translation.title();
		}
		return level.getTranslations().values().stream()
				.map(FileTranslation::title)
				.filter(StringHelper::containsNonWhitespace)
				.findFirst().orElse(null);
	}

	private String getImages(FileLevel level) {
		List<String> images = new java.util.ArrayList<>(2);
		if(level.getTeaserImage() != null) {
			images.add(level.getTeaserImage().filename());
		}
		if(level.getBackgroundImage() != null) {
			images.add(level.getBackgroundImage().filename());
		}
		return images.isEmpty() ? null : String.join(", ", images);
	}

	public enum ReviewCols implements FlexiColumnDef {
		row("importexport.row"),
		status("importexport.status"),
		path("importexport.level.path"),
		identifier("importexport.level.identifier"),
		title("importexport.level.title"),
		type("importexport.level.type"),
		sortOrder("importexport.level.sort.order"),
		images("importexport.images"),
		messages("importexport.messages");

		private final String i18nKey;

		private ReviewCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
