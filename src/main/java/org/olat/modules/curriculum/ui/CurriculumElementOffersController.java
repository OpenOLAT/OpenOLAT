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
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.ui.CatalogBCFactory;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementToTaxonomyLevel;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryService;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.CatalogInfo.CatalogStatusEvaluator;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController;
import org.olat.resource.accesscontrol.ui.AccessSegmentedOverviewController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Dec 6, 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementOffersController extends BasicController {
	
	private AccessSegmentedOverviewController accessConfigCtrl;
	
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CatalogV2Module catalogV2Module;

	public CurriculumElementOffersController(UserRequest ureq, WindowControl wControl,
			CurriculumElementRef elementRef, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(TaxonomyUIFactory.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(AccessConfigurationController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		
		CurriculumElement element = curriculumService.getCurriculumElement(elementRef);
		element.getCurriculum().getOrganisation().getDisplayName(); // avoid LazyInitializationException
		
		Collection<Organisation> defaultOfferOrganisations = List.of(element.getCurriculum().getOrganisation());
		
		String details = null;
		Set<CurriculumElementToTaxonomyLevel> ce2taxonomyLevels = element.getTaxonomyLevels();
		Set<TaxonomyLevel> taxonomyLevels = null;
		if (ce2taxonomyLevels != null && !ce2taxonomyLevels.isEmpty()) {
			taxonomyLevels = ce2taxonomyLevels.stream()
				.map(CurriculumElementToTaxonomyLevel::getTaxonomyLevel)
				.collect(Collectors.toSet());
			details = TaxonomyUIFactory.getTags(getTranslator(), taxonomyLevels);
		}
		if (!StringHelper.containsNonWhitespace(details)) {
			details = translate("access.taxonomy.level.not.yet");
		}
		
		String editBusinessPath = "[CurriculumAdmin:0][Implementations:0][CurriculumElement:" + elementRef.getKey() + "][Metadata:0]";
		
		boolean fullyBooked = curriculumService.isMaxParticipantsReached(element);
		boolean startDateAvailable = element.getBeginDate() != null;
		CatalogInfo catalogInfo = new CatalogInfo(true, catalogV2Module.isWebPublishEnabled(), false, true, true,
				translate("access.taxonomy.level"), details, null, getStatusEvaluator(element.getElementStatus()),
				translate("offer.available.in.status.curriculum.element"),
				fullyBooked, startDateAvailable, editBusinessPath,
				translate("access.open.metadata"), CatalogBCFactory.get(false).getOfferUrl(element.getResource()),
				catalogV2Module.isWebPublishEnabled() ? CatalogBCFactory.get(true).getOfferUrl(element.getResource()) : null,
				taxonomyLevels, true);

		accessConfigCtrl = new AccessSegmentedOverviewController(ureq, wControl, element.getResource(),
				element.getDisplayName(), true, false, false, true, defaultOfferOrganisations, catalogInfo,
				!secCallback.canEditCurriculumElement(element), false, null);
		listenTo(accessConfigCtrl);
		
		putInitialPanel(accessConfigCtrl.getInitialComponent());
	}

	public void updateStatus(CurriculumElementStatus status) {
		accessConfigCtrl.setStatusEvaluator(getStatusEvaluator(status));
	}

	private CatalogStatusEvaluator getStatusEvaluator(CurriculumElementStatus status) {
		return CurriculumElementCatalogStatusEvaluator.create(catalogV2Module, status);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
