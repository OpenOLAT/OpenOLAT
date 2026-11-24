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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateVariant;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dashboard.DashboardUIFactory;
import org.olat.core.gui.control.generic.dashboard.MaxHeightScrollableDelegate;
import org.olat.core.gui.control.generic.dashboard.TableWidgetController;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.CurriculumElementCatalogStatusEvaluator;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.CatalogInfo.CatalogStatusEvaluator;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.OfferComparator;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.OfferAndAccessInfos;
import org.olat.resource.accesscontrol.ui.OfferCatalogInfo;
import org.olat.resource.accesscontrol.ui.OfferCatalogInfo.OfferCatalogStatus;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 29, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OffersWidgetController extends TableWidgetController {
	
	private static final String OFFERS_PATH = "[Offers:0]";
	
	private FormLayoutContainer catalogCont;
	private FlexiTableElement offersTableEl;
	private OffersWidgetDataModel offersTableModel;
	
	private CurriculumElement curriculumElement;
	
	@Autowired
	private ACService acService;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private CatalogV2Module catalogV2Module;
	@Autowired
	private CurriculumService curriculumService;
	
	public OffersWidgetController(UserRequest ureq, WindowControl wControl, CurriculumElement curriculumElement) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CurriculumComposerController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(OfferCatalogInfo.class, getLocale(), getTranslator()));
		this.curriculumElement = curriculumElement;
		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected String getTitle() {
		return "<i class=\"o_icon o_icon_catalog_intern\"> </i> " + translate("curriculum.offers");
	}
	
	@Override
	protected String createTopArea(FormLayoutContainer widgetCont) {
		String page = Util.getPackageVelocityRoot(OffersWidgetController.class) + "/offers_widget_catalog.html";
		catalogCont = FormLayoutContainer.createCustomFormLayout("catalogCont", getTranslator(), page);
		catalogCont.setRootForm(mainForm);
		widgetCont.add(catalogCont);
		return catalogCont.getComponent().getComponentName();
	}
	
	@Override
	protected String createTable(FormLayoutContainer widgetCont) {
		offersTableModel = new OffersWidgetDataModel(FlexiTableDataModelFactory.createFlexiTableColumnModel());
		offersTableEl = uifactory.addTableElement(getWindowControl(), "offersTable", offersTableModel, 25, false, getTranslator(), widgetCont);
		offersTableEl.setCustomizeColumns(false);
		offersTableEl.setNumOfRowsEnabled(false);
		offersTableEl.setRendererType(FlexiTableRendererType.custom);
		offersTableEl.setCssDelegate(MaxHeightScrollableDelegate.DELEGATE);
		VelocityContainer rowVC = createVelocityContainer("offers_widget_row");
		rowVC.setDomReplacementWrapperRequired(false);
		offersTableEl.setRowRenderer(rowVC, null);
		offersTableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withVariant(EmptyStateVariant.auto)
				.withIconCss("o_ac_offer_bookable_icon")
				.withMessageTranslated(translate("widget.offers.empty"))
				.build());
		return offersTableEl.getComponent().getComponentName();
	}
	
	@Override
	protected List<String> createFooterButtons(FormLayoutContainer widgetCont) {
		FormLink detailsLink = DashboardUIFactory.createDetailsLink(widgetCont);
		setUrl(detailsLink, OFFERS_PATH, "[CurriculumAdmin:0][Implementations:0][CurriculumElement:" + curriculumElement.getKey() + "]" + OFFERS_PATH);
		return List.of(detailsLink.getComponent().getComponentName());
	}
	
	public void loadModel() {
		curriculumElement = curriculumService.getCurriculumElement(curriculumElement);
		CatalogStatusEvaluator statusEvaluator = CurriculumElementCatalogStatusEvaluator.create(catalogV2Module, curriculumElement.getElementStatus());
		List<OfferAndAccessInfos> offersAndAccess = acService.findOfferAndAccessByResource(curriculumElement.getResource(), true)
				.stream()
				.sorted(Comparator.comparing(OfferAndAccessInfos::offer, new OfferComparator()))
				.toList();
		
		List<OfferCatalogInfo> offerCatalogInfos = new ArrayList<>(offersAndAccess.size());
		List<OfferWidgetRow> rows = new ArrayList<>(offersAndAccess.size());
		for (OfferAndAccessInfos offerAndAccess : offersAndAccess) {
			OfferCatalogInfo offerCatalogInfo = OfferCatalogInfo.create(offerAndAccess.offer(), statusEvaluator);
			offerCatalogInfos.add(offerCatalogInfo);
			OfferWidgetRow row = forgeRow(offerAndAccess, offerCatalogInfo);
			if (row != null) {
				rows.add(row);
			}
		}
		offersTableModel.setObjects(rows);
		offersTableEl.reset(true, true, true);
		
		setFooterText(rows.size() == 1
				? translate("widget.offers.footer.single", String.valueOf(rows.size()))
				: translate("widget.offers.footer", String.valueOf(rows.size())));
		
		loadCatalogStatus(offerCatalogInfos);
	}
	
	private void loadCatalogStatus(List<OfferCatalogInfo> offerCatalogInfos) {
		boolean fullyBooked = curriculumService.isMaxParticipantsReached(curriculumElement);
		
		List<OfferCatalogInfo> internalCatalogInfos = offerCatalogInfos.stream()
				.filter(OfferCatalogInfo::isPublished)
				.toList();
		putCatalogStatus("internal", OfferCatalogInfo.getCatalogStatus(internalCatalogInfos, fullyBooked));
		
		if (catalogV2Module.isWebPublishEnabled()) {
			List<OfferCatalogInfo> externalCatalogInfos = internalCatalogInfos.stream()
					.filter(OfferCatalogInfo::isWebPublished)
					.toList();
			putCatalogStatus("external", OfferCatalogInfo.getCatalogStatus(externalCatalogInfos, fullyBooked));
		}
	}
	
	private void putCatalogStatus(String prefix, OfferCatalogStatus status) {
		catalogCont.contextPut(prefix + "Css", OfferCatalogInfo.getLabelCss(status));
		catalogCont.contextPut(prefix + "IconCss", OfferCatalogInfo.getIconCss(status));
		catalogCont.contextPut(prefix + "LabelName", OfferCatalogInfo.getLabelName(getTranslator(), status));
	}
	
	private OfferWidgetRow forgeRow(OfferAndAccessInfos offerAndAccess, OfferCatalogInfo offerCatalogInfo) {
		Offer offer = offerAndAccess.offer();
		OfferAccess offerAccess = offerAndAccess.offerAccess();
		
		String iconCss;
		String typeName;
		String price = null;
		if (offer.isOpenAccess()) {
			iconCss = "o_ac_openaccess_icon";
			typeName = translate("offer.open.access.name");
		} else if (offer.isGuestAccess()) {
			iconCss = "o_ac_guest_icon";
			typeName = translate("offer.guest.name");
		} else {
			AccessMethodHandler handler = offerAccess == null? null: acModule.getAccessMethodHandler(offerAccess.getMethod().getType());
			if (handler == null) {
				return null;
			}
			iconCss = offerAccess.getMethod().getMethodCssClass() + "_icon";
			typeName = handler.getMethodName(getLocale());
			if (handler.isPaymentMethod() && !offer.getPrice().isEmpty()) {
				price = PriceFormat.fullFormatVat(getTranslator(), acModule, offer.getPrice());
			}
		}
		
		String statusHtml = OfferCatalogInfo.getStatusLightLabel(getTranslator(), offerCatalogInfo.getStatus());
		return new OfferWidgetRow(iconCss, offer.getLabel(), typeName, price, statusHtml, getPeriod(offerCatalogInfo));
	}
	
	private String getPeriod(OfferCatalogInfo offerCatalogInfo) {
		Date now = new Date();
		Date from = offerCatalogInfo.getFrom();
		Date to = offerCatalogInfo.getTo();
		if (from != null && from.after(now)) {
			long days = DateUtils.countDays(now, from);
			return days == 1
					? translate("widget.offers.starts.in.single")
					: translate("widget.offers.starts.in", String.valueOf(days));
		}
		if (to != null && to.after(now)) {
			long days = DateUtils.countDays(now, to);
			if (days == 0) {
				return translate("widget.offers.ends.today");
			}
			return days == 1
					? translate("widget.offers.ends.in.single")
					: translate("widget.offers.ends.in", String.valueOf(days));
		}
		if (to != null) {
			return translate("widget.offers.ended", Formatter.getInstance(getLocale()).formatDate(to));
		}
		return null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink link && link.getUserObject() instanceof String path) {
			doActivate(ureq, path);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doActivate(UserRequest ureq, String path) {
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(path);
		fireEvent(ureq, new ActivateEvent(entries));
	}
	
}
