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

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.admin.user.UserAdminController;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogV1MigrationService;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.CatalogV2Module.CatalogV1Migration;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.repository.RepositoryModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 May 2022<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogSettingsController extends FormBasicController {
	
	public static final Event MIGRATED_EVENT = new Event("vat.v1.event");

	private static final String CMD_TAXONOMY = "taxonomy";
	private static final String KEY_NONE = "none";
	private static final String KEY_V1 = "v1";
	private static final String KEY_V2 = "v2";
	
	private DialogBoxController migrateDialogCtrl;

	private FormLayoutContainer generalCont;
	private SingleSelection enabledEl;
	private FormLayoutContainer taxonomiesCont;
	private MultipleSelectionElement taxonomyEditRolesEl;
	private FormLayoutContainer migrationCont;
	private FormLayoutContainer migrationStartCont;
	private FormLink migrationStartLink;
	private StaticTextElement migrationRunningEl;

	@Autowired
	private CatalogV2Module catalogV2Module;
	@Autowired
	private CatalogV1MigrationService migrationService;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private TaskExecutorManager taskExecutorManager;

	public CatalogSettingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(CatalogV2UIFactory.class, ureq.getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(UserAdminController.class, ureq.getLocale(), getTranslator()));
		initForm(ureq);
		updateUI();
		updateMigrationUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		generalCont = FormLayoutContainer.createDefaultFormLayout("general", getTranslator());
		generalCont.setFormTitle(translate("admin.settings"));
		generalCont.setRootForm(mainForm);
		formLayout.add("general", generalCont);
		
		SelectionValues enabledSV = new SelectionValues();
		enabledSV.add(entry(KEY_NONE, translate("admin.enabled.none")));
		enabledSV.add(entry(KEY_V1, translate("admin.enabled.v1")));
		enabledSV.add(entry(KEY_V2, translate("admin.enabled.v2")));
		enabledEl = uifactory.addRadiosVertical("admin.enabled", generalCont, enabledSV.keys(), enabledSV.values());
		enabledEl.addActionListener(FormEvent.ONCHANGE);
		if (catalogV2Module.isEnabled()) {
			enabledEl.select(KEY_V2, true);
		} else if (repositoryModule.isCatalogEnabled()) {
			enabledEl.select(KEY_V1, true);
		} else {
			enabledEl.select(KEY_NONE, true);
		}
		
		taxonomiesCont = FormLayoutContainer.createCustomFormLayout("taxonomies", getTranslator(), velocity_root + "/taxonomy_links.html");
		taxonomiesCont.setLabel("admin.taxonomy", null);
		taxonomiesCont.setRootForm(mainForm);
		generalCont.add(taxonomiesCont);
		
		Set<Long> taxonomyKeys = repositoryModule.getTaxonomyRefs().stream()
				.map(TaxonomyRef::getKey)
				.collect(Collectors.toSet());
		List<Taxonomy> taxonomies = taxonomyService.getTaxonomyList().stream()
				.filter(taxonomy -> taxonomyKeys.contains(taxonomy.getKey()))
				.sorted((t1, t2) -> t1.getDisplayName().compareTo(t2.getDisplayName()))
				.collect(Collectors.toList());
		List<FormLink> taxonomyLinks = new ArrayList<>(taxonomies.size());
		for (Taxonomy taxonomy: taxonomies) {
			FormLink link = uifactory.addFormLink("delete_" + taxonomy.getKey(), CMD_TAXONOMY, "delete", null, taxonomiesCont, Link.LINK + Link.NONTRANSLATED);
			link.setI18nKey(taxonomy.getDisplayName());
			link.setUserObject(taxonomy);
			taxonomyLinks.add(link);
		}
		taxonomiesCont.contextPut("taxonomyLinks", taxonomyLinks);
		
		SelectionValues taxonomyEditRolesSV = new SelectionValues();
		taxonomyEditRolesSV.add(SelectionValues.entry(OrganisationRoles.learnresourcemanager.name(), translate("role." + OrganisationRoles.learnresourcemanager)));
		taxonomyEditRolesEl = uifactory.addCheckboxesVertical("admin.taxonomy.edit.roles", generalCont, taxonomyEditRolesSV.keys(), taxonomyEditRolesSV.values(), 1);
		taxonomyEditRolesEl.addActionListener(FormEvent.ONCHANGE);
		catalogV2Module.getTaxonomyEditRoles().stream().forEach(role -> taxonomyEditRolesEl.select(role.name(), true));
		
		migrationCont = FormLayoutContainer.createDefaultFormLayout("migrations", getTranslator());
		migrationCont.setFormTitle(translate("admin.migration"));
		migrationCont.setFormDescription(translate("admin.migration.desc"));
		migrationCont.setRootForm(mainForm);
		formLayout.add("migrations", migrationCont);
		
		migrationStartCont = FormLayoutContainer.createButtonLayout("migratonButton", getTranslator());
		migrationStartCont.setRootForm(mainForm);
		migrationCont.add(migrationStartCont);
		migrationStartLink = uifactory.addFormLink("admin.migration.start", migrationStartCont, Link.BUTTON);
		
		migrationRunningEl = uifactory.addStaticTextElement("admin.migration.running", null,
				translate("admin.migration.running"), migrationCont);
	}

	private void updateUI() {
		if (catalogV2Module.isEnabled()) {
			generalCont.setFormContextHelp("manual_user/catalog/catalog2.0#angebot-erstellen");
		} else {
			generalCont.setFormContextHelp("");
		}
		taxonomiesCont.setVisible(catalogV2Module.isEnabled());
		taxonomyEditRolesEl.setVisible(catalogV2Module.isEnabled());
	}

	private void updateMigrationUI() {
		migrationCont.setVisible(false);
		migrationStartCont.setVisible(false);
		migrationRunningEl.setVisible(false);
		if (catalogV2Module.isEnabled()) {
			if (CatalogV1Migration.pending == catalogV2Module.getCatalogV1Migration()) {
				migrationCont.setVisible(true);
				migrationStartCont.setVisible(true);
			} else if (CatalogV1Migration.running == catalogV2Module.getCatalogV1Migration()) {
				migrationCont.setVisible(true);
				migrationRunningEl.setVisible(true);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (migrateDialogCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				migrationStartCont.setVisible(false);
				migrationRunningEl.setVisible(true);
				flc.setDirty(true);
				taskExecutorManager.execute(() -> {
					try {
						migrationService.migrate(getIdentity());
					} catch (Exception e) {
						logError("", e);
					}
					updateMigrationUI();
					fireEvent(ureq, MIGRATED_EVENT);
				});
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enabledEl) {
			doSetEnabled();
			updateMigrationUI();
			fireEvent(ureq, FormEvent.CHANGED_EVENT);
		} else if (source == taxonomyEditRolesEl) {
			Set<OrganisationRoles> taxonomyEditRoles = taxonomyEditRolesEl.getSelectedKeys().stream()
					.map(OrganisationRoles::valueOf)
					.collect(Collectors.toSet());
			catalogV2Module.setTaxonomyEditRoles(taxonomyEditRoles);
		} else if (source == migrationStartLink) {
			doConfirmMigraion(ureq);
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if (CMD_TAXONOMY.equals(link.getCmd())) {
				Taxonomy taxonomy = (Taxonomy)link.getUserObject();
				doOpenTaxonomy(ureq, taxonomy);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doSetEnabled() {
		if (KEY_V2.equals(enabledEl.getSelectedKey())) {
			catalogV2Module.setEnabled(true);
			repositoryModule.setCatalogEnabled(false);
		} else if (KEY_V1.equals(enabledEl.getSelectedKey())) {
			catalogV2Module.setEnabled(false);
			repositoryModule.setCatalogEnabled(true);
		} else if (KEY_NONE.equals(enabledEl.getSelectedKey())) {
			catalogV2Module.setEnabled(false);
			repositoryModule.setCatalogEnabled(false);
		}
		updateUI();
	}

	private void doOpenTaxonomy(UserRequest ureq, Taxonomy taxonomy) {
		try {
			String businessPath = "[AdminSite:0][taxonomy:0][Taxonomy:" + taxonomy.getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} catch (Exception e) {
			//
		}
	}

	private void doConfirmMigraion(UserRequest ureq) {
		String title = translate("admin.migration.confirm.title");
		String text = translate("admin.migration.confirm.text");
		migrateDialogCtrl = activateYesNoDialog(ureq, title, text, migrateDialogCtrl);
	}

}
