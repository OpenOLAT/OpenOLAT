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
package org.olat.modules.quality.analysis.ui;

import static java.util.stream.Collectors.toList;
import static org.olat.modules.quality.analysis.LegendItem.item;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Organisation;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.LegendItem;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 Feb 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FilteredPrintController extends BasicController {
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private TaxonomyService taxonomyService;

	protected FilteredPrintController(UserRequest ureq, WindowControl wControl, Controller controller,
			AnalysisSearchParameter searchParams, boolean insufficientOnly, String title) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("filtered_print");
		
		mainVC.contextPut("mainTitle", title);
		mainVC.put("ctrl", controller.getInitialComponent());
		
		String legendFiltersPage = velocity_root + "/heatmap_legend.html";
		FormLayoutContainer legendFiltersLayout = FormLayoutContainer.createCustomFormLayout("legendFilters", getTranslator(), legendFiltersPage);
		legendFiltersLayout.contextPut("items", getLegendFilters(searchParams, insufficientOnly));
		legendFiltersLayout.contextPut("title", translate("heatmap.legend.filters"));
		mainVC.put("legendFilters", legendFiltersLayout.getComponent());
		
		MainPanel mainPanel = new MainPanel("groupByPrintPanel");
		mainPanel.setContent(mainVC);
		putInitialPanel(mainPanel);
	}
	

	private List<LegendItem> getLegendFilters(AnalysisSearchParameter searchParams, boolean insufficientOnly) {
		Formatter formatter = Formatter.getInstance(getLocale());
		List<LegendItem> items = new ArrayList<>();
		if (searchParams.getDateRangeFrom() != null) {
			items.add(item(translate("filter.date.range.from"), formatter.formatDate(searchParams.getDateRangeFrom())));
		}
		if (searchParams.getDateRangeTo() != null) {
			items.add(item(translate("filter.date.range.to"), formatter.formatDate(searchParams.getDateRangeTo())));
		}
		if (searchParams.getTopicIdentityRefs() != null && !searchParams.getTopicIdentityRefs().isEmpty()) {
			List<Long> keys = searchParams.getTopicIdentityRefs().stream().map(IdentityRef::getKey).collect(toList());
			String label = securityManager.loadIdentityByKeys(keys).stream()
					.map(i -> userManager.getUserDisplayName(i))
					.collect(Collectors.joining(", "));
			items.add(item(translate("filter.topic.identities"), label));
		}
		if (searchParams.getTopicOrganisationRefs() != null && !searchParams.getTopicOrganisationRefs().isEmpty()) {
			String label = searchParams.getTopicOrganisationRefs().stream()
					.map(ref -> organisationService.getOrganisation(ref))
					.map(Organisation::getDisplayName)
					.collect(Collectors.joining(", "));
			items.add(item(translate("filter.topic.organisations"), label));
		}
		if (searchParams.getTopicCurriculumRefs() != null && !searchParams.getTopicCurriculumRefs().isEmpty()) {
			String label = searchParams.getTopicCurriculumRefs().stream()
					.map(ref -> curriculumService.getCurriculum(ref))
					.map(Curriculum::getDisplayName)
					.collect(Collectors.joining(", "));
			items.add(item(translate("filter.topic.curriculums"), label));
		}
		if (searchParams.getTopicCurriculumElementRefs() != null && !searchParams.getTopicCurriculumElementRefs().isEmpty()) {
			String label = searchParams.getTopicCurriculumElementRefs().stream()
					.map(ref -> curriculumService.getCurriculumElement(ref))
					.map(CurriculumElement::getDisplayName)
					.collect(Collectors.joining(", "));
			items.add(item(translate("filter.topic.curriculum.elements"), label));
		}
		if (searchParams.getTopicRepositoryRefs() != null && !searchParams.getTopicRepositoryRefs().isEmpty()) {
			List<Long> keys = searchParams.getTopicRepositoryRefs().stream().map(RepositoryEntryRef::getKey).collect(toList());
			String label = repositoryManager.lookupRepositoryEntries(keys).stream()
					.map(RepositoryEntry::getDisplayname)
					.collect(Collectors.joining(", "));
			items.add(item(translate("filter.topic.repositories"), label));
		}
		if (searchParams.getContextOrganisationRefs() != null && !searchParams.getContextOrganisationRefs().isEmpty()) {
			String label = searchParams.getContextOrganisationRefs().stream()
					.map(ref -> organisationService.getOrganisation(ref))
					.map(Organisation::getDisplayName)
					.collect(Collectors.joining(", "));
			items.add(item(translate("filter.context.organisations"), label));
		}
		if (searchParams.getContextCurriculumRefs() != null && !searchParams.getContextCurriculumRefs().isEmpty()) {
			String label = searchParams.getContextCurriculumRefs().stream()
					.map(ref -> curriculumService.getCurriculum(ref))
					.map(Curriculum::getDisplayName)
					.collect(Collectors.joining(", "));
			items.add(item(translate("filter.context.curriculums"), label));
		}
		if (searchParams.getContextCurriculumElementRefs() != null && !searchParams.getContextCurriculumElementRefs().isEmpty()) {
			String label = searchParams.getContextCurriculumElementRefs().stream()
					.map(ref -> curriculumService.getCurriculumElement(ref))
					.map(CurriculumElement::getDisplayName)
					.collect(Collectors.joining(", "));
			items.add(item(translate("filter.context.curriculum.elements"), label));
		}
		if (searchParams.getContextCurriculumElementTypeRefs() != null && !searchParams.getContextCurriculumElementTypeRefs().isEmpty()) {
			String label = searchParams.getContextCurriculumElementTypeRefs().stream()
					.map(ref -> curriculumService.getCurriculumElementType(ref))
					.map(CurriculumElementType::getDisplayName)
					.collect(Collectors.joining(", "));
			items.add(item(translate("filter.context.curriculum.element.types"), label));
					
		}
		if (searchParams.getContextCurriculumOrganisationRefs() != null && !searchParams.getContextCurriculumOrganisationRefs().isEmpty()) {
			String label = searchParams.getContextCurriculumOrganisationRefs().stream()
					.map(ref -> organisationService.getOrganisation(ref))
					.map(Organisation::getDisplayName)
					.collect(Collectors.joining(", "));
			items.add(item(translate("filter.context.curriculum.organisations"), label));
		}
		if (searchParams.getContextTaxonomyLevelRefs() != null) {
			Translator taxonomyTanslator = Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale());
			String label = searchParams.getContextTaxonomyLevelRefs().stream()
					.map(ref -> taxonomyService.getTaxonomyLevel(ref))
					.map(level -> TaxonomyUIFactory.translateDisplayName(taxonomyTanslator, level))
					.collect(Collectors.joining(", "));
			items.add(item(translate("filter.context.taxonomy.level"), label));
		}
		if (searchParams.getContextLocations() != null && !searchParams.getContextLocations().isEmpty()) {
			String label = searchParams.getContextLocations().stream()
					.collect(Collectors.joining(", "));
			items.add(item(translate("filter.context.location"), label));
		}
		if (searchParams.getSeriesIndexes() != null && !searchParams.getSeriesIndexes().isEmpty()) {
			String label = searchParams.getSeriesIndexes().stream()
					.sorted()
					.map(String::valueOf)
					.collect(Collectors.joining(", "));
			items.add(item(translate("filter.series.index"), label));
		}
		if (searchParams.getContextRoles() != null && !searchParams.getContextRoles().isEmpty()) {
			String label = searchParams.getContextRoles().stream()
					.map(role -> AnalysisUIFactory.translateRole(getTranslator(), role))
					.collect(Collectors.joining(", "));
			items.add(item(translate("filter.context.role"), label));
		}
		if (searchParams.isWithUserInfosOnly()) {
			items.add(item(translate("filter.with.user.informations.label"), translate("filter.activated")));
		}
		if (insufficientOnly) {
			items.add(item(translate("heatmap.insufficient.select"), translate("filter.activated")));
		}
		return items;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
