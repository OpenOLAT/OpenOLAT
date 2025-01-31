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
package org.olat.modules.curriculum.ui.widgets;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.CurriculumElementCatalogStatusEvaluator;
import org.olat.modules.curriculum.ui.CurriculumListManagerController;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.model.TaxonomyLevelNamePath;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.CatalogInfo.CatalogStatusEvaluator;
import org.olat.resource.accesscontrol.model.OfferAndAccessInfos;
import org.olat.resource.accesscontrol.ui.OfferCatalogInfo;
import org.olat.resource.accesscontrol.ui.OfferCatalogInfo.OfferCatalogStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 29, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OffersWidgetController extends FormBasicController {
	
	private FormLink offersLink;
	private FormLink minimizeButton;
	
	private AtomicBoolean minimized;
	private final String preferencesId;
	private CurriculumElement curriculumElement;
	
	@Autowired
	private ACService acService;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private CatalogV2Module catalogV2Module;
	@Autowired
	private CurriculumService curriculumService;

	public OffersWidgetController(UserRequest ureq, WindowControl wControl, CurriculumElement curriculumElement) {
		super(ureq, wControl, "offers_widget", Util.createPackageTranslator(CurriculumComposerController.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(OfferCatalogInfo.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.curriculumElement = curriculumElement;
		preferencesId = "widget-lectures-cur-el-" + curriculumElement.getKey();
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		offersLink = uifactory.addFormLink("curriculum.offers", formLayout);
		offersLink.setIconRightCSS("o_icon o_icon-fw o_icon_course_next");
		
		Boolean minimizedObj = (Boolean)ureq.getUserSession()
				.getGuiPreferences()
				.get(OffersWidgetController.class, preferencesId, Boolean.FALSE);
		minimized = new AtomicBoolean(minimizedObj != null && minimizedObj.booleanValue());
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("minimized", minimized);
		}
		
		minimizeButton = uifactory.addFormLink("curriculum.minimize", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		minimizeButton.setTitle(translate("curriculum.minimize"));
		minimizeButton.setElementCssClass("o_button_details");
		updateMinimizeButton();
	}

	public void loadModel() {
		curriculumElement = curriculumService.getCurriculumElement(curriculumElement);
		loadOffers();
	}
	
	private void loadOffers() {
		// Offer status
		CatalogStatusEvaluator statusEvaluator = CurriculumElementCatalogStatusEvaluator.create(catalogV2Module, curriculumElement.getElementStatus());
		List<OfferCatalogInfo> offerCatalogInfos = acService.findOfferAndAccessByResource(curriculumElement.getResource(), true)
				.stream()
				.map(OfferAndAccessInfos::offer)
				.map(offer -> OfferCatalogInfo.create(offer, statusEvaluator))
				.toList();
		
		int numPending = 0;
		int numBookable = 0;
		int numFinished = 0;
		int numNotAvailable = 0;
		
		Date pendingFrom = null;
		boolean bookableEnds = true;
		Date bookableTo = null;
		
		for (OfferCatalogInfo offerCatalogInfo : offerCatalogInfos) {
			if (OfferCatalogInfo.OfferCatalogStatus.pending == offerCatalogInfo.getStatus()) {
				numPending ++;
				if (pendingFrom == null || pendingFrom.after(offerCatalogInfo.getFrom())) {
					pendingFrom = offerCatalogInfo.getFrom();
				}
			} else if (OfferCatalogInfo.OfferCatalogStatus.bookable == offerCatalogInfo.getStatus()) {
				numBookable ++;
				if (offerCatalogInfo.getTo() == null) {
					bookableEnds = false;
				} else if (bookableTo == null || bookableTo.before(offerCatalogInfo.getTo())) {
					bookableTo = offerCatalogInfo.getTo();
				}
			} else if (OfferCatalogInfo.OfferCatalogStatus.finished == offerCatalogInfo.getStatus()) {
				numFinished ++;
			} else if (OfferCatalogInfo.OfferCatalogStatus.notAvailable == offerCatalogInfo.getStatus()) {
				numNotAvailable ++;
			}
		}
		
		flc.contextPut("labelPending", OfferCatalogInfo.getStatusLightLabel(getTranslator(), OfferCatalogStatus.pending));
		String translateNumPending = translateNumOffers(numPending);
		if (numBookable < 1 && pendingFrom != null) {
			long days = DateUtils.countDays(new Date(), pendingFrom);
			if (days == 1) {
				translateNumPending += " - <small>" + translate("curriculum.offers.offers.starts.single") + "</small>";
			} else if (days > 1) {
				translateNumPending += " - <small>" + translate("curriculum.offers.offers.starts.multi", String.valueOf(days)) + "</small>";
			}
		}
		flc.contextPut("numPending", translateNumPending);
		
		flc.contextPut("labelBookable", OfferCatalogInfo.getStatusLightLabel(getTranslator(), OfferCatalogStatus.bookable));
		String translateNumBookable = translateNumOffers(numBookable);
		if (numBookable > 0 && bookableEnds && bookableTo != null) {
			long days = DateUtils.countDays(new Date(), bookableTo);
			if (days == 1) {
				translateNumBookable += " - <small>" + translate("curriculum.offers.offers.ends.single") + "</small>";
			} else if (days > 1) {
				translateNumBookable += " - <small>" + translate("curriculum.offers.offers.ends.multi", String.valueOf(days)) + "</small>";
			}
			
		}
		flc.contextPut("numBookable", translateNumBookable);
		
		flc.contextPut("labelFinished", OfferCatalogInfo.getStatusLightLabel(getTranslator(), OfferCatalogStatus.finished));
		flc.contextPut("numFinished", translateNumOffers(numFinished));
		
		flc.contextPut("labelNotAvailable", OfferCatalogInfo.getStatusLightLabel(getTranslator(), OfferCatalogStatus.notAvailable));
		flc.contextPut("numNotAvailable", translateNumOffers(numNotAvailable));
		
		
		// Catalog status
		boolean fullyBooked = curriculumService.isMaxParticipantsReached(curriculumElement);
		
		List<OfferCatalogInfo> internalCatalogInfos = offerCatalogInfos.stream()
				.filter(Objects::nonNull)
				.filter(OfferCatalogInfo::isPublished)
				.toList();
		OfferCatalogStatus internalCatalogStatus = OfferCatalogInfo.getCatalogStatus(internalCatalogInfos, statusEvaluator, fullyBooked);
		flc.contextPut("internalCatalogCss", OfferCatalogInfo.getLabelCss(internalCatalogStatus));
		flc.contextPut("internalCatalogIconCss", OfferCatalogInfo.getIconCss(internalCatalogStatus));
		flc.contextPut("internalCatalogLabelName", OfferCatalogInfo.getLabelName(getTranslator(), internalCatalogStatus));
		
		if (catalogV2Module.isWebPublishEnabled()) {
			List<OfferCatalogInfo> externalCatalogInfos = offerCatalogInfos.stream()
					.filter(Objects::nonNull)
					.filter(OfferCatalogInfo::isPublished)
					.filter(OfferCatalogInfo::isWebPublished)
					.toList();
			OfferCatalogStatus externalCatalogStatus = OfferCatalogInfo.getCatalogStatus(externalCatalogInfos, statusEvaluator, fullyBooked);
			flc.contextPut("externalCatalogCss", OfferCatalogInfo.getLabelCss(externalCatalogStatus));
			flc.contextPut("externalCatalogIconCss", OfferCatalogInfo.getIconCss(externalCatalogStatus));
			flc.contextPut("externalCatalogLabelName", OfferCatalogInfo.getLabelName(getTranslator(), externalCatalogStatus));
		}
		
		
		// Taxonomy
		flc.contextPut("taxonomyEnabled", taxonomyModule.isEnabled());
		if (taxonomyModule.isEnabled()) {
			List<TaxonomyLevelNamePath> taxonomyLevels = TaxonomyUIFactory.getNamePaths(getTranslator(), curriculumService.getTaxonomy(curriculumElement));
			flc.contextPut("taxonomyLevels", taxonomyLevels);
			
			EmptyState emptyTaxonomy = EmptyStateFactory.create("empty.taxonomy", flc.getFormItemComponent(), this);
			emptyTaxonomy.setMessageI18nKey("curriculum.offers.taxonomy.empty");
			emptyTaxonomy.setIconCss("o_icon_tag");
		}
	}
	
	private String translateNumOffers(int numOffers) {
		return numOffers == 1
				? translate("curriculum.offers.offers.num.single")
				: translate("curriculum.offers.offers.num.multi", String.valueOf(numOffers));
	}
	
	private void updateMinimizeButton() {
		if(minimized.get()) {
			minimizeButton.setIconLeftCSS("o_icon o_icon_details_expand");
		} else {
			minimizeButton.setIconLeftCSS("o_icon o_icon_details_collaps");
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(offersLink == source) {
			List<ContextEntry> entries = BusinessControlFactory.getInstance()
					.createCEListFromResourceType(CurriculumListManagerController.CONTEXT_OFFERS);
			fireEvent(ureq, new ActivateEvent(entries));
		} else if(minimizeButton == source) {
			toogle(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void toogle(UserRequest ureq) {
		minimized.set(!minimized.get());
		updateMinimizeButton();
		ureq.getUserSession().getGuiPreferences()
			.putAndSave(OffersWidgetController.class, preferencesId, Boolean.valueOf(minimized.get()));
	}
	
}
