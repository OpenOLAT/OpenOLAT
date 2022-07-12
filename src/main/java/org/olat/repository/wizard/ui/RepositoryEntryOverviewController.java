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
package org.olat.repository.wizard.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryOverviewController extends BasicController {
	
	public static final Event REPLACE_EVENT = new Event("replace");
	
	private VelocityContainer mainVC;
	private Link replaceLink;
	private Link toggleFiguresLink;
	
	private Boolean figuresToggle = Boolean.FALSE;
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private RepositoryEntryLicenseHandler licenseHandler;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	protected RepositoryEntryOverviewController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		
		mainVC = createVelocityContainer("entry_overview");
		putInitialPanel(mainVC);
	}
	
	public void setRepositoryEntry(UserRequest ureq, RepositoryEntry entry, MoreFigures moreFigures) {
		mainVC.clear();
		mainVC.contextPut("entry", entry);
		
		String iconCssClass = RepositoyUIFactory.getIconCssClass(entry);
		mainVC.contextPut("iconCssClass", iconCssClass);
		
		VFSLeaf image = repositoryManager.getImage(entry.getKey(), entry.getOlatResource());
		if(image != null && vfsRepositoryService.isThumbnailAvailable(image)) {
			VFSLeaf thumbnail = vfsRepositoryService.getThumbnail(image, 150, 150, false);
			VFSMediaMapper thumbnailMapper = new VFSMediaMapper(thumbnail);
			String thumbnailUrl = registerCacheableMapper(ureq, null, thumbnailMapper);
			mainVC.contextPut("thumbnailUrl", thumbnailUrl);
		}
		
		List<Figure> mainFigures = createMainFigures(entry);
		mainFigures.addAll(moreFigures.getMainFigures(entry, getLocale()));
		mainVC.contextPut("mainFigures", mainFigures);
		
		List<Figure> additionalFigures = createAdditionalFigures(entry);
		mainVC.contextPut("additionalFigures", additionalFigures);
		
		replaceLink = LinkFactory.createButton("replace", mainVC, this);
		
		toggleFiguresLink = LinkFactory.createLink("figures.toggle", mainVC, this);
		toggleFiguresUI();
	}
	
	private List<Figure> createMainFigures(RepositoryEntry entry) {
		List<Figure> figures = new ArrayList<>();
		String creationDate = Formatter.getInstance(getLocale()).formatDateAndTime(entry.getCreationDate());
		figures.add(new Figure(translate("table.header.date"), creationDate));
		
		if (entry.getInitialAuthor() != null) {
			String initalAuthor = userManager.getUserDisplayName(entry.getInitialAuthor());
			initalAuthor = StringHelper.escapeHtml(initalAuthor);
			figures.add(new Figure(translate("cif.initialAuthor"), initalAuthor));
		}
		
		if (StringHelper.containsNonWhitespace(entry.getAuthors())) {
			figures.add(new Figure(translate("cif.authors"), entry.getAuthors()));
		}
		
		boolean licEnabled = licenseModule.isEnabled(licenseHandler);
		if (licEnabled) {
			License license = licenseService.loadOrCreateLicense(entry.getOlatResource());

			String licenseStr = "";
			String iconCssClass = LicenseUIFactory.getCssOrDefault(license.getLicenseType());
			if (StringHelper.containsNonWhitespace(iconCssClass)) {
				licenseStr = licenseStr.concat("<i class='o_icon " + iconCssClass + "'> </i> ");
			}
			String licenseName = LicenseUIFactory.translate(license.getLicenseType(), getLocale());
			if (StringHelper.containsNonWhitespace(licenseName)) {
				licenseStr = licenseStr.concat(licenseName);
			}
			if (StringHelper.containsNonWhitespace(licenseStr)) {
				figures.add(new Figure(translate("cif.license"), licenseStr));
			}
			
			String licensor = StringHelper.containsNonWhitespace(license.getLicensor())? license.getLicensor(): "";
			if (StringHelper.containsNonWhitespace(licensor)) {
				figures.add(new Figure(translate("cif.licensor"), licensor));
			}
			
			String licenseText = LicenseUIFactory.getFormattedLicenseText(license);
			if (StringHelper.containsNonWhitespace(licenseText)) {
				figures.add(new Figure(translate("cif.license.text"), licenseText));
			}
		}
		
		return figures;
	}

	private List<Figure> createAdditionalFigures(RepositoryEntry entry) {
		List<Figure> figures = new ArrayList<>();
		if (entry.getMainLanguage() != null) {
			figures.add(new Figure(translate("cif.mainLanguage"), entry.getMainLanguage()));
		}
		if (entry.getLocation() != null) {
			figures.add(new Figure(translate("cif.location"), entry.getLocation()));
		}
		if (entry.getExpenditureOfWork() != null) {
			figures.add(new Figure(translate("cif.expenditureOfWork"), entry.getExpenditureOfWork()));
		}
		addTaxonomyFigure(figures, entry);
		return figures;
	}

	private void addTaxonomyFigure(List<Figure> figures, RepositoryEntry entry) {
		List<TaxonomyLevel> taxonomyLevels = repositoryService.getTaxonomy(entry);
		if (taxonomyLevels.isEmpty()) return;
		
		String formatedLevels = taxonomyLevels.stream()
				.map(this::formatTaxonomyLevel)
				.sorted()
				.collect(Collectors.joining("<br>"));
		figures.add(new Figure(translate("cif.taxonomy.levels"), formatedLevels));
	}

	private String formatTaxonomyLevel(TaxonomyLevel level) {
		ArrayList<String> names = new ArrayList<>();
		addParentTaxonomyLevelNames(names, level);
		Collections.reverse(names);
		return String.join(" / ", names);
	}
	
	private void addParentTaxonomyLevelNames(List<String> names, TaxonomyLevel level) {
		names.add(TaxonomyUIFactory.translateDisplayName(getTranslator(), level));
		TaxonomyLevel parent = level.getParent();
		if (parent != null) {
			addParentTaxonomyLevelNames(names, parent);
		}
	}

	private void toggleFiguresUI() {
		mainVC.contextPut("figuresToggle", figuresToggle);
		if (figuresToggle.booleanValue()) {
			toggleFiguresLink.setCustomDisplayText(translate("toggle.hide"));
			toggleFiguresLink.setIconLeftCSS("o_icon o_icon_move_up");
		} else {
			toggleFiguresLink.setCustomDisplayText(translate("toggle.show"));
			toggleFiguresLink.setIconLeftCSS("o_icon o_icon_move_down");
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == toggleFiguresLink) {
			figuresToggle = Boolean.valueOf(!figuresToggle.booleanValue());
			toggleFiguresUI();
		} else if (source == replaceLink) {
			fireEvent(ureq, REPLACE_EVENT);
		}
	}
	
	public static final class Figure {
		
		private final String legend;
		private final String value;
		
		public Figure(String legend, String value) {
			this.legend = legend;
			this.value = value;
		}
		
		public String getLegend() {
			return legend;
		}
		
		public String getValue() {
			return value;
		}
		
	}
	
	public interface MoreFigures {
		
		public List<Figure> getMainFigures(RepositoryEntry entry, Locale locale);
		
	}
	
}
