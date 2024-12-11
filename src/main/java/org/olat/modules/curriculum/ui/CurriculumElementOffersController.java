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
package org.olat.modules.curriculum.ui;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Organisation;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.ui.CatalogBCFactory;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementToTaxonomyLevel;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.model.TaxonomyLevelNamePath;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.author.RepositoryCatalogInfoFactory;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Dec 6, 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementOffersController extends BasicController {
	
	private AccessConfigurationController accessConfigCtrl;
	
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CatalogV2Module catalogV2Module;

	public CurriculumElementOffersController(UserRequest ureq, WindowControl wControl,
			CurriculumElementRef elementRef, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		
		CurriculumElement element = curriculumService.getCurriculumElement(elementRef);
		element.getCurriculum().getOrganisation().getDisplayName(); // avoid LazyInitializationException
		
		Collection<Organisation> defaultOfferOrganisations = List.of(element.getCurriculum().getOrganisation());

		Translator translator = Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale());
		translator = Util.createPackageTranslator(AccessConfigurationController.class, getLocale(), translator);
		translator = Util.createPackageTranslator(RepositoryService.class, getLocale(), translator);
		
		List<TaxonomyLevel> taxonomyLevels = null;
		List<TaxonomyLevelNamePath> taxonomyLevelPath = null;
		Set<CurriculumElementToTaxonomyLevel> ce2taxonomyLevels = element.getTaxonomyLevels();
		if (ce2taxonomyLevels != null && !ce2taxonomyLevels.isEmpty()) {
			taxonomyLevels = ce2taxonomyLevels.stream()
				.map(CurriculumElementToTaxonomyLevel::getTaxonomyLevel)
				.toList();
			taxonomyLevelPath = TaxonomyUIFactory.getNamePaths(getTranslator(), taxonomyLevels);
		}
		
		String details;
		if (taxonomyLevelPath == null || taxonomyLevelPath.isEmpty()) {
			details = translator.translate("access.taxonomy.level.not.yet");
		} else {
			details = RepositoryCatalogInfoFactory.wrapTaxonomyLevels(taxonomyLevelPath);
		}
		String editBusinessPath = "[CurriculumAdmin:0][Implementations:0][CurriculumElement:" + elementRef.getKey() + "][Matadata:0]";

		boolean fullyBooked = false;
		CatalogInfo catalogInfo = new CatalogInfo(true, catalogV2Module.isWebPublishEnabled(), true,
				translator.translate("access.taxonomy.level"), details, false, null, fullyBooked, editBusinessPath,
				translator.translate("access.open.metadata"),
				CatalogBCFactory.get(false).getOfferUrl(element.getResource()), taxonomyLevels, true);
		
		accessConfigCtrl = new AccessConfigurationController(ureq, wControl, element.getResource(),
				element.getDisplayName(), true, false, false, true, defaultOfferOrganisations, catalogInfo,
				!secCallback.canEditCurriculumElement(element), false, null);
		listenTo(accessConfigCtrl);
		
		putInitialPanel(accessConfigCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == accessConfigCtrl) {
			if(event == Event.CHANGED_EVENT) {
				accessConfigCtrl.commitChanges();
			}
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

}
