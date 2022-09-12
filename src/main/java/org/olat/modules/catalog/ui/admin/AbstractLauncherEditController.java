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
package org.olat.modules.catalog.ui.admin;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultiSelectionFilterElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.OrganisationUIFactory;
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
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractLauncherEditController extends FormBasicController {
	
	private static final String[] ON_KEYS = new String[] { "on" };
	
	private StaticTextElement nameEl;
	private FormLink nameLink;
	private MultipleSelectionElement enabledEl;
	private MultiSelectionFilterElement organisationsEl;
	
	private CloseableModalController cmc;
	private SingleKeyTranslatorController launcherNameTranslatorCtrl;
	
	private final CatalogLauncherHandler handler;
	private final String identifier;
	private CatalogLauncher catalogLauncher;
	private List<Organisation> allOrganisations;
	
	@Autowired
	private CatalogV2Service catalogService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;

	public AbstractLauncherEditController(UserRequest ureq, WindowControl wControl, CatalogLauncherHandler handler, CatalogLauncher catalogLauncher) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(CatalogV2UIFactory.class, ureq.getLocale(), getTranslator()));
		this.handler = handler;
		this.catalogLauncher = catalogLauncher;
		this.identifier = catalogLauncher!= null? catalogLauncher.getIdentifier(): catalogService.createLauncherIdentifier();
	}
	
	protected String getDescription() {
		return null;
	}

	protected abstract void initForm(FormItemContainer generalCont);
	
	/**
	 * Subclasses may add additional parts to the form.
	 * 
	 * @param formLayout 
	 * @param ureq
	 */
	protected void addFormPart(FormItemContainer formLayout, UserRequest ureq) {
		// May be overridden by sub classes.
	}

	protected abstract String getConfig();
	
	protected CatalogLauncher getCatalogLauncher() {
		return catalogLauncher;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("general", getTranslator());
		generalCont.setRootForm(mainForm);
		formLayout.add(generalCont);
		String description = getDescription();
		if (StringHelper.containsNonWhitespace(description)) {
			generalCont.setFormInfo(description);
		}
		
		FormLayoutContainer nameCont = FormLayoutContainer.createButtonLayout("nameCont", getTranslator());
		nameCont.setLabel("admin.launcher.name", null);
		nameCont.setElementCssClass("o_inline_cont");
		nameCont.setRootForm(mainForm);
		generalCont.add(nameCont);
		
		String translateLauncherName = CatalogV2UIFactory.translateLauncherName(getTranslator(), handler, identifier);
		nameEl = uifactory.addStaticTextElement("admin.launcher.name", null, translateLauncherName, nameCont);
		
		nameLink = uifactory.addFormLink("admin.launcher.name.edit", nameCont);
		
		String[] onValues = new String[]{ translate("on") };
		enabledEl = uifactory.addCheckboxesHorizontal("admin.launcher.enabled", generalCont, ON_KEYS, onValues);
		enabledEl.select(ON_KEYS[0], catalogLauncher == null || catalogLauncher.isEnabled());
		
		if (organisationModule.isEnabled()) {
			initFormOrganisations(generalCont);
		}
		
		initForm(generalCont);
		
		addFormPart(formLayout, ureq);
		
		FormLayoutContainer buttonsWrapperCont = FormLayoutContainer.createDefaultFormLayout("buttonsWrapper", getTranslator());
		buttonsWrapperCont.setRootForm(mainForm);
		formLayout.add(buttonsWrapperCont);
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsWrapperCont.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void initFormOrganisations(FormItemContainer formLayout) {
		allOrganisations = organisationService.getOrganisations();
		SelectionValues organisationSV = OrganisationUIFactory.createSelectionValues(allOrganisations);
		organisationsEl = uifactory.addCheckboxesFilterDropdown("organisations", "admin.launcher.organisations",
				formLayout, getWindowControl(), organisationSV);
		
		if (catalogLauncher != null) {
			List<Organisation> launchersOrganisations = catalogService.getCatalogLauncherOrganisations(catalogLauncher);
			launchersOrganisations.forEach(organisation -> organisationsEl.select(organisation.getKey().toString(), true));
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == launcherNameTranslatorCtrl) {
			nameEl.setValue(CatalogV2UIFactory.translateLauncherName(getTranslator(), handler, identifier));
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
		
		catalogLauncher.setEnabled(enabledEl.isAtLeastSelected(1));
		catalogLauncher.setConfig(getConfig());
		catalogLauncher = catalogService.update(catalogLauncher);
		
		Collection<Organisation> organisations = null;
		if (organisationsEl != null) {
			Collection<String> selectedKeys = organisationsEl.getSelectedKeys();
			organisations = allOrganisations.stream()
					.filter(org -> selectedKeys.contains(org.getKey().toString()))
					.collect(Collectors.toSet());
		}
		catalogService.updateLauncherOrganisations(catalogLauncher, organisations);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private void doLauncherName(UserRequest ureq) {
		if (guardModalController(launcherNameTranslatorCtrl)) return;
		
		String i18nKey = CatalogV2UIFactory.getLauncherNameI18nKey(identifier);
		
		launcherNameTranslatorCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), i18nKey, CatalogV2UIFactory.class);
		listenTo(launcherNameTranslatorCtrl);

		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), "close", launcherNameTranslatorCtrl.getInitialComponent(), true,
				translate("admin.launcher.name"));
		listenTo(cmc);
		cmc.activate();
	}

}
