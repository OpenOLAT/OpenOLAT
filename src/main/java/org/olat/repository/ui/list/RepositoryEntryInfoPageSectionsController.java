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
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.sections.Section;
import org.olat.core.gui.components.sections.Sections;
import org.olat.core.gui.components.sections.SectionsFactory;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.manager.CatalogManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 18 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryInfoPageSectionsController extends BasicController {

	private static final String CMD_CATEGORY = "category";

	private final boolean hasContent;

	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private CatalogManager catalogManager;

	public RepositoryEntryInfoPageSectionsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));

		RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(entry);
		VFSContainer mediaContainer = handler.getMediaContainer(entry);
		String baseUrl = mediaContainer != null
				? registerMapper(ureq, new VFSContainerMapper(mediaContainer.getParentContainer()))
				: null;

		List<Section> sections = new ArrayList<>();
		addTextSection(sections, "description", "cif.description", entry.getDescription(), baseUrl);
		addTextSection(sections, "objectives", "cif.objectives", entry.getObjectives(), baseUrl);
		addTextSection(sections, "requirements", "cif.requirements", entry.getRequirements(), baseUrl);
		addTextSection(sections, "credits", "cif.credits", entry.getCredits(), baseUrl);

		if (repositoryModule.isCatalogEnabled()) {
			addCategoriesSection(sections, catalogManager.getCatalogEntriesReferencing(entry));
		}

		hasContent = !sections.isEmpty();
		Sections sectionsCmp = SectionsFactory.createSections("sections", null);
		sectionsCmp.setSections(sections);
		putInitialPanel(sectionsCmp);
	}

	private void addCategoriesSection(List<Section> sections, List<CatalogEntry> categories) {
		if (categories.isEmpty()) {
			return;
		}
		List<Component> categoryLinks = new ArrayList<>(categories.size());
		for (CatalogEntry category : categories) {
			if (category.getParent() != null) {
				categoryLinks.add(categoryLink(category));
			}
		}
		if (categoryLinks.isEmpty()) {
			return;
		}
		sections.add(SectionsFactory.createLinksSection("categories", translate("cif.categories"), categoryLinks));
	}

	private Link categoryLink(CatalogEntry category) {
		String id = "cat_" + category.getKey();
		String title = StringHelper.escapeHtml(category.getParent().getName());
		Link link = LinkFactory.createCustomLink(id, CMD_CATEGORY, title, Link.LINK | Link.NONTRANSLATED, null, this);
		link.setIconLeftCSS("o_icon o_icon-fw o_icon_catalog");
		link.setUserObject(category.getKey());
		return link;
	}

	private void addTextSection(List<Section> sections, String id, String titleI18nKey, String text, String baseUrl) {
		String html = getFormattedText(text, baseUrl);
		if (StringHelper.containsNonWhitespace(html)) {
			sections.add(SectionsFactory.createTextSection(id, translate(titleI18nKey), html));
		}
	}

	private String getFormattedText(String text, String baseUrl) {
		if (!StringHelper.containsNonWhitespace(text)) return null;

		String formattedText = StringHelper.xssScan(text);
		if (baseUrl != null) {
			formattedText = FilterFactory.getBaseURLToMediaRelativeURLFilter(baseUrl).filter(formattedText);
		}
		return Formatter.formatLatexFormulas(formattedText);
	}

	public boolean hasContent() {
		return hasContent;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link && CMD_CATEGORY.equals(link.getCommand())) {
			doOpenCategory(ureq, (Long) link.getUserObject());
		}
	}

	private void doOpenCategory(UserRequest ureq, Long categoryKey) {
		String businessPath = "[CatalogEntry:" + categoryKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

}
