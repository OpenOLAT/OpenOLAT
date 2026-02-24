/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.mode;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.assessment.model.SafeExamBrowserConfiguration;

/**
 *
 * Initial date: 19 Feb 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class SafeExamBrowserTemplateDataModel
		extends DefaultFlexiTableDataModel<SafeExamBrowserTemplateRow>
		implements SortableFlexiTableDataModel<SafeExamBrowserTemplateRow> {

	private static final SEBTemplateCols[] COLS = SEBTemplateCols.values();

	private final Locale locale;

	public SafeExamBrowserTemplateDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public Object getValueAt(int row, int col) {
		SafeExamBrowserTemplateRow templateRow = getObject(row);
		return getValueAt(templateRow, col);
	}

	@Override
	public void sort(SortKey sortKey) {
		List<SafeExamBrowserTemplateRow> sorted = new SortableFlexiTableModelDelegate<>(sortKey, this, locale).sort();
		super.setObjects(sorted);
	}

	@Override
	public Object getValueAt(SafeExamBrowserTemplateRow row, int col) {
		SafeExamBrowserConfiguration config = row.getConfiguration();
		return switch (COLS[col]) {
			case name -> row.getName();
			case active -> Boolean.valueOf(row.isActive());
			case isDefault -> Boolean.valueOf(row.isDefault());
			case usages -> Long.valueOf(row.getUsage());
			case browserViewMode -> config != null ? Integer.valueOf(config.getBrowserViewMode()) : null;
			case allowQuit -> config != null ? Boolean.valueOf(config.isAllowQuit()) : null;
			case enableReload -> config != null ? Boolean.valueOf(config.isBrowserWindowAllowReload()) : null;
			case showTaskBar -> config != null ? Boolean.valueOf(config.isShowTaskBar()) : null;
			case showReloadButton -> config != null ? Boolean.valueOf(config.isShowReloadButton()) : null;
			case showTime -> config != null ? Boolean.valueOf(config.isShowTimeClock()) : null;
			case showKeyboard -> config != null ? Boolean.valueOf(config.isShowKeyboardLayout()) : null;
			case allowWlan -> config != null ? Boolean.valueOf(config.isAllowWlan()) : null;
			case audioControl -> config != null ? Boolean.valueOf(config.isAudioControlEnabled()) : null;
			case audioMute -> config != null ? Boolean.valueOf(config.isAudioMute()) : null;
			case allowAudioCapture -> config != null ? Boolean.valueOf(config.isAllowAudioCapture()) : null;
			case allowVideoCapture -> config != null ? Boolean.valueOf(config.isAllowVideoCapture()) : null;
			case allowSpellCheck -> config != null ? Boolean.valueOf(config.isAllowSpellCheck()) : null;
			case allowZoom -> config != null ? Boolean.valueOf(config.isAllowZoomInOut()) : null;
			case urlFilter -> config != null ? Boolean.valueOf(config.isUrlFilter()) : null;
			case edit -> Boolean.TRUE;
			case tools -> row.getToolsButton();
		};
	}

	public enum SEBTemplateCols implements FlexiSortableColumnDef {
		name("seb.template.name"),
		active("seb.template.active"),
		isDefault("seb.template.default"),
		usages("seb.template.usages"),
		browserViewMode("mode.safeexambrowser.browser.view.mode"),
		allowQuit("mode.safeexambrowser.allow.toexit"),
		enableReload("mode.safeexambrowser.enable.reload"),
		showTaskBar("mode.safeexambrowser.show.tasklist"),
		showReloadButton("mode.safeexambrowser.show.reload.button"),
		showTime("mode.safeexambrowser.show.timeclock"),
		showKeyboard("mode.safeexambrowser.show.keyboard"),
		allowWlan("mode.safeexambrowser.allow.wlan"),
		audioControl("mode.safeexambrowser.show.audio"),
		audioMute("mode.safeexambrowser.audio.mute"),
		allowAudioCapture("mode.safeexambrowser.allow.audio.capture"),
		allowVideoCapture("mode.safeexambrowser.allow.video.capture"),
		allowSpellCheck("mode.safeexambrowser.show.spellchecking"),
		allowZoom("mode.safeexambrowser.zoom"),
		urlFilter("mode.safeexambrowser.url.filter"),
		edit("table.header.edit"),
		tools("action.more");

		private final String i18nKey;

		private SEBTemplateCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != edit && this != tools;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
