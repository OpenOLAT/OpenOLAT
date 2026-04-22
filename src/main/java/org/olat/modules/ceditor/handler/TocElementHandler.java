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
package org.olat.modules.ceditor.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.ceditor.CloneElementHandler;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.TocElement;
import org.olat.modules.ceditor.model.TocSettings;
import org.olat.modules.ceditor.model.jpa.TitlePart;
import org.olat.modules.ceditor.model.jpa.TocPart;
import org.olat.modules.ceditor.ui.BlockLayoutClassFactory;
import org.olat.modules.ceditor.ui.PageRunControllerElement;
import org.olat.modules.ceditor.ui.TocEditorController;
import org.olat.modules.ceditor.ui.TocInspectorController;
import org.olat.modules.ceditor.ui.TocRunController;
import org.olat.modules.ceditor.ui.TocRunController.TitleEntry;
import org.olat.modules.ceditor.ui.TocRunController.TocRenderData;

/**
 * Initial date: 15 Apr 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TocElementHandler implements PageElementHandler, PageElementStore<TocElement>,
		SimpleAddPageElementHandler, CloneElementHandler {

	private final Page page;
	private final PageService pageService;

	public TocElementHandler(Page page, PageService pageService) {
		this.page = page;
		this.pageService = pageService;
	}

	@Override
	public String getType() {
		return "toc";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_list_num";
	}

	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.other;
	}

	@Override
	public int getSortOrder() {
		return 20;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, RenderingHints options) {
		if (!(element instanceof TocPart tocPart)) {
			return null;
		}

		if (options.isEditable()) {
			TocEditorController ctrl = new TocEditorController(ureq, wControl, tocPart, this::computeRenderData, true);
			return new PageRunControllerElement(ctrl);
		} else {
			TocRenderData renderData = computeRenderData(tocPart);
			TocRunController ctrl = new TocRunController(ureq, wControl, renderData);
			return new PageRunControllerElement(ctrl);
		}
	}

	private TocRenderData computeRenderData(TocPart tocPart) {
		List<PagePart> allParts = pageService.getAllPagePartsFlat(page);
		int tocIndex = indexOf(allParts, tocPart);
		if (tocIndex < 0) {
			TocSettings fallback = tocPart.getTocSettings();
			AlertBoxSettings fallbackAlertBox = fallback.getAlertBoxSettings();
			boolean fallbackBoxTitleActive = fallbackAlertBox != null && fallbackAlertBox.isShowAlertBox()
					&& StringHelper.containsNonWhitespace(fallbackAlertBox.getTitle());
			String fallbackInlineTitle = fallbackBoxTitleActive ? "" : fallback.getTitle();
			return new TocRenderData(fallbackInlineTitle, List.of(), BlockLayoutClassFactory.buildClass(fallback, false));
		}

		// Use fresh TocPart from DB so settings are always up to date
		TocSettings settings = tocPart.getTocSettings();
		if (allParts.get(tocIndex) instanceof TocPart freshPart) {
			settings = freshPart.getTocSettings();
		}

		// Step 1: determine current level from nearest preceding title
		int currentLevel = 0;
		for (int i = tocIndex - 1; i >= 0; i--) {
			if (allParts.get(i) instanceof TitlePart preceding) {
				currentLevel = preceding.getTitleSettings().getSize();
				break;
			}
		}

		// Step 2+3: collect following titles in this section, filtered by visibleLevels
		List<TitleEntry> entries = new ArrayList<>();
		for (int i = tocIndex + 1; i < allParts.size(); i++) {
			if (!(allParts.get(i) instanceof TitlePart title)) {
				continue;
			}
			int level = title.getTitleSettings().getSize();
			if (level <= currentLevel) {
				break;
			}
			if (settings.getVisibleLevels().contains(level)) {
				String rawContent = title.getContent();
				String text = StringHelper.containsNonWhitespace(rawContent)
						? StringHelper.unescapeHtml(FilterFactory.getHtmlTagsFilter().filter(rawContent))
						: "";
				entries.add(new TitleEntry(title.getKey(), text, level - currentLevel - 1));
			}
		}
		AlertBoxSettings alertBoxSettings = settings.getAlertBoxSettings();
		boolean boxTitleActive = alertBoxSettings != null && alertBoxSettings.isShowAlertBox()
				&& StringHelper.containsNonWhitespace(alertBoxSettings.getTitle());
		String inlineTitle = boxTitleActive ? "" : settings.getTitle();
		return new TocRenderData(inlineTitle, entries, BlockLayoutClassFactory.buildClass(settings, false));
	}

	private int indexOf(List<PagePart> parts, TocPart target) {
		for (int i = 0; i < parts.size(); i++) {
			if (parts.get(i).getKey().equals(target.getKey())) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public PageElementEditorController getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if (element instanceof TocPart tocPart) {
			return new TocEditorController(ureq, wControl, tocPart, this::computeRenderData, false);
		}
		return null;
	}

	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if (element instanceof TocElement tocElement) {
			return new TocInspectorController(ureq, wControl, tocElement, this);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		TocPart part = new TocPart();
		TocSettings settings = new TocSettings();
		settings.setTitle(Util.createPackageTranslator(TocEditorController.class, locale).translate("toc.title.example"));
		part.setTocSettings(settings);
		return part;
	}

	@Override
	public PageElement clonePageElement(PageElement element) {
		if (element instanceof TocPart tocPart) {
			return tocPart.copy();
		}
		return null;
	}

	@Override
	public TocElement savePageElement(TocElement element) {
		return CoreSpringFactory.getImpl(PageService.class).updatePart((TocPart) element);
	}
}
