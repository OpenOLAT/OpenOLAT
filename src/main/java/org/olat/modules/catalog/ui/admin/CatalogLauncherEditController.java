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
package org.olat.modules.catalog.ui.admin;

import java.util.Collection;
import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherHandler;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.user.ui.organisation.OrganisationSelectionSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jun 3, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public abstract class CatalogLauncherEditController extends FormBasicController {
	
	private static final String KEY_CATALOG = "catalog";
	private static final String KEY_WEBCATALOG = "webcatalog";
	
	private TextElement nameEl;
	private FormLink nameLink;
	private MultipleSelectionElement enabledEl;
	private ObjectSelectionElement organisationsEl;
	
	private CloseableModalController cmc;
	private SingleKeyTranslatorController launcherNameTranslatorCtrl;
	
	private final CatalogLauncherHandler handler;
	private final String identifier;
	private CatalogLauncher catalogLauncher;
	
	@Autowired
	private CatalogV2Service catalogService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;

	protected CatalogLauncherEditController(UserRequest ureq, WindowControl wControl, CatalogLauncherHandler handler, CatalogLauncher catalogLauncher) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(CatalogV2UIFactory.class, ureq.getLocale(), getTranslator()));
		this.handler = handler;
		this.catalogLauncher = catalogLauncher;
		this.identifier = catalogLauncher!= null? catalogLauncher.getIdentifier(): catalogService.createLauncherIdentifier();
	}

	protected abstract String getConfig();
	
	protected CatalogLauncher getCatalogLauncher() {
		return catalogLauncher;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initTitle(formLayout);
		initEnabled(formLayout);
		initFormOrganisations(formLayout);
		initButtons(ureq, formLayout);
	}

	protected void initTitle(FormItemContainer formLayout) {
		FormLayoutContainer nameCont = FormLayoutContainer.createInputGroupLayout("nameCont", getTranslator(), null, null);
		nameCont.setLabel("admin.launcher.title", null);
		nameCont.setElementCssClass("o_inline_cont");
		nameCont.setRootForm(mainForm);
		formLayout.add(nameCont);
		
		String translateLauncherName = CatalogV2UIFactory.translateLauncherName(getTranslator(), handler, identifier);
		translateLauncherName = StringHelper.escapeHtml(translateLauncherName);
		nameEl = uifactory.addTextElement("admin.launcher.name", 100, translateLauncherName, nameCont);
		nameEl.setAriaLabel(translate("admin.launcher.name"));
		nameEl.setEnabled(false);
		nameEl.setDomReplacementWrapperRequired(false);
		
		nameLink = uifactory.addFormLink("rightAddOn", "translate", "translate", null, nameCont, Link.BUTTON);
		nameLink.setIconLeftCSS("o_icon o_icon-lg o_icon_language");
		nameLink.setElementCssClass("input-group-addon");
	}

	protected void initEnabled(FormItemContainer formLayout) {
		SelectionValues enabledSV = new SelectionValues();
		enabledSV.add(SelectionValues.entry(KEY_CATALOG, translate("admin.launcher.catalog"), null, "o_icon o_icon-fw o_icon_catalog_intern", null, true));
		enabledSV.add(SelectionValues.entry(KEY_WEBCATALOG, translate("admin.launcher.web.catalog"), null, "o_icon o_icon-fw o_icon_catalog_extern", null, true));
		
		enabledEl = uifactory.addCheckboxesButtonGroup("admin.launcher.available.in", "admin.launcher.available.in", formLayout, enabledSV);
		enabledEl.select(KEY_CATALOG, catalogLauncher == null || catalogLauncher.isEnabled());
		enabledEl.select(KEY_WEBCATALOG, catalogLauncher == null || catalogLauncher.isWebEnabled());
		enabledEl.addActionListener(FormEvent.ONCHANGE);
	}

	protected void initFormOrganisations(FormItemContainer formLayout) {
		if (!organisationModule.isEnabled()) {
			return;
		}
		
		List<Organisation> currentOrganisations = catalogLauncher != null
				? catalogService.getCatalogLauncherOrganisations(catalogLauncher)
				: List.of();
		OrganisationSelectionSource organisationSource = new OrganisationSelectionSource(
				currentOrganisations,
				() -> organisationService.getOrganisations());
		organisationsEl = uifactory.addObjectSelectionElement("organisations", "admin.launcher.organisations", formLayout,
				getWindowControl(), true, organisationSource);
		
		updateOrganisationUI();
	}
	
	private void updateOrganisationUI() {
		if (organisationsEl != null) {
			organisationsEl.setVisible(enabledEl.isKeySelected(KEY_CATALOG));
		}
	}

	protected void initButtons(UserRequest ureq, FormItemContainer formLayout) {
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == launcherNameTranslatorCtrl) {
			nameEl.setValue(StringHelper.escapeHtml(CatalogV2UIFactory.translateLauncherName(getTranslator(), handler, identifier)));
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(launcherNameTranslatorCtrl);
		removeAsListenerAndDispose(cmc);
		launcherNameTranslatorCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == nameLink) {
			doLauncherName(ureq);
		} else if (source == enabledEl) {
			updateOrganisationUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (catalogLauncher == null) {
			catalogLauncher = catalogService.createCatalogLauncher(handler.getType(), identifier);
		}
		
		catalogLauncher.setEnabled(enabledEl.isKeySelected(KEY_CATALOG));
		catalogLauncher.setWebEnabled(enabledEl.isKeySelected(KEY_WEBCATALOG));
		catalogLauncher.setConfig(getConfig());
		catalogLauncher = catalogService.update(catalogLauncher);
		
		if (organisationsEl == null) {
			catalogService.updateLauncherOrganisations(catalogLauncher, null);
		} else if (organisationsEl.isVisible()) {
			Collection<Organisation> organisations = organisationService.getOrganisation(OrganisationSelectionSource.toRefs(organisationsEl.getSelectedKeys()));
			catalogService.updateLauncherOrganisations(catalogLauncher, organisations);
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private void doLauncherName(UserRequest ureq) {
		if (guardModalController(launcherNameTranslatorCtrl)) return;
		
		String i18nKey = CatalogV2UIFactory.getLauncherNameI18nKey(identifier);
		
		launcherNameTranslatorCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), i18nKey, CatalogV2UIFactory.class);
		listenTo(launcherNameTranslatorCtrl);

		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), launcherNameTranslatorCtrl.getInitialComponent(), true,
				translate("admin.launcher.name"));
		listenTo(cmc);
		cmc.activate();
	}

}
