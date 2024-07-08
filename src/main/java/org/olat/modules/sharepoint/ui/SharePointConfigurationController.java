/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.sharepoint.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.admin.user.UserAdminController;
import org.olat.basesecurity.OAuth2Tokens;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.sharepoint.SharePointModule;
import org.olat.modules.sharepoint.model.SiteAndDriveConfiguration;
import org.olat.modules.sharepoint.model.SitesAndDrivesConfiguration;
import org.olat.modules.sharepoint.ui.SitesAndDrivesFlatTableModel.ConfigurationCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharePointConfigurationController extends FormBasicController {
	
	private FormToggle moduleEnabledEl;
	private FormToggle sitesEnabledEl;
	private FormToggle oneDriveEnabledEl;
	
	private MultipleSelectionElement adminPropsEl;

	private TextElement searchSitesEl;
	private TextElement excludeSitesAndDrivesEl;
	private TextElement excludeLabelsEl;
	private FormLink addSitesAndDrivesButton;
	private FormLayoutContainer sitesAndDrivesContainer;
	private FlexiTableElement sitesAndDrivesConfigurationTableEl;
	private SitesAndDrivesFlatTableModel sitesAndDrivesConfigurationModel;
	
	private CloseableModalController cmc;
	private ConfirmRemoveSiteOrDriveController confirmRemoveCtrl;
	private SharePointSearchSitesAndDrivesController searchSitesAndDrivesCtrl; 
	
	@Autowired
	private SharePointModule sharePointModule;
	
	public SharePointConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(UserAdminController.class, ureq.getLocale()));
		
		initForm(ureq);
		updateUI();
		loadSitesConfiguration();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("sharepoint.title");
		setFormInfo("sharepoint.intro");
		
		moduleEnabledEl = uifactory.addToggleButton("sharepoint.enable", "sharepoint.enable", translate("on"), translate("off"), formLayout);
		moduleEnabledEl.toggle(sharePointModule.isEnabled());
		moduleEnabledEl.addActionListener(FormEvent.ONCHANGE);
		
		sitesEnabledEl = uifactory.addToggleButton("sites.enable", "sharepoint.sites.enable", translate("on"), translate("off"), formLayout);
		sitesEnabledEl.toggle(sharePointModule.isSitesEnabled());
		sitesEnabledEl.addActionListener(FormEvent.ONCHANGE);
		
		oneDriveEnabledEl = uifactory.addToggleButton("onedrive.enable", "sharepoint.onedrive.enable", translate("on"), translate("off"), formLayout);
		oneDriveEnabledEl.toggle(sharePointModule.isOneDriveEnabled());
		oneDriveEnabledEl.addActionListener(FormEvent.ONCHANGE);

		SelectionValues adminPropPK = new SelectionValues();
		adminPropPK.add(SelectionValues.entry(OrganisationRoles.sysadmin.name(), translate("role.sysadmin")));
		adminPropPK.add(SelectionValues.entry(OrganisationRoles.administrator.name(), translate("role.administrator")));
		adminPropPK.add(SelectionValues.entry(OrganisationRoles.learnresourcemanager.name(), translate("role.learnresourcemanager")));
		adminPropPK.add(SelectionValues.entry(OrganisationRoles.author.name(), translate("role.author")));
		adminPropPK.add(SelectionValues.entry(OrganisationRoles.user.name(), translate("role.user")));
		
		adminPropsEl = uifactory.addCheckboxesVertical("sharepoint.roles.enabled", formLayout, adminPropPK.keys(), adminPropPK.values(), 1);
		List<String> enabledRoles = sharePointModule.getRolesEnabledList();
		for(String enabledRole:enabledRoles) {
			adminPropsEl.select(enabledRole, true);
		}
		
		sitesAndDrivesContainer = uifactory.addCustomFormLayout("list.sites", "sharepoint.list.sites.drives",
				velocity_root + "/sites_drives.html", formLayout);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigurationCols.siteDisplayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigurationCols.siteName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ConfigurationCols.siteId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigurationCols.driveName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ConfigurationCols.driveId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("remove", translate("remove"), "remove"));

		sitesAndDrivesConfigurationModel = new SitesAndDrivesFlatTableModel(columnsModel);
		sitesAndDrivesConfigurationTableEl = uifactory.addTableElement(getWindowControl(), "configuration.sites.drives", sitesAndDrivesConfigurationModel, 25, false, getTranslator(), sitesAndDrivesContainer);
		
		addSitesAndDrivesButton = uifactory.addFormLink("add.sites.and.drives", sitesAndDrivesContainer, Link.BUTTON);
		
		String sitesSearch = sharePointModule.getSitesSearch();
		searchSitesEl = uifactory.addTextElement("sites.search", 128, sitesSearch, formLayout);
		
		String exclusionSitesAndDrives = toTextArea(sharePointModule.getExcludeSitesAndDrives());
		excludeSitesAndDrivesEl = uifactory.addTextAreaElement("exclusion.sites", "exclusion.sites", 4000, 4, 60, false, false, false, exclusionSitesAndDrives, formLayout);
		
		String exclusionLabels = toTextArea(sharePointModule.getExcludeLabels());
		excludeLabelsEl = uifactory.addTextAreaElement("exclusion.labels", "exclusion.labels", 4000, 4, 60, false, false, false, exclusionLabels, formLayout);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void updateUI() {
		boolean enabled = moduleEnabledEl.isOn();

		sitesEnabledEl.setVisible(enabled);
		oneDriveEnabledEl.setVisible(enabled);
		excludeLabelsEl.setVisible(enabled);
		
		boolean sitesEnabled = sitesEnabledEl.isOn();
		searchSitesEl.setVisible(enabled && sitesEnabled);
		excludeSitesAndDrivesEl.setVisible(enabled && sitesEnabled);
		adminPropsEl.setVisible(enabled && sitesEnabled);
		sitesAndDrivesContainer.setVisible(enabled && sitesEnabled);
	}
	
	private void loadSitesConfiguration() {
		SitesAndDrivesConfiguration sitesConfiguration = sharePointModule.getSitesConfiguration();
		if(sitesConfiguration != null && sitesConfiguration.getConfigurationList() != null) {
			List<SiteAndDriveConfiguration> list = sitesConfiguration.getConfigurationList();
			if(list.size() > 1) {
				Collections.sort(list, new SiteAndDriveConfigurationComparator(getLocale()));
			}
			sitesAndDrivesConfigurationModel.setObjects(list);
		}
		sitesAndDrivesConfigurationTableEl.reset(true, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(searchSitesAndDrivesCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doFinalizeAddSitesAndDrives(searchSitesAndDrivesCtrl.getSelectedSiteAndDrives());
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmRemoveCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doFinalizeConfiguration(confirmRemoveCtrl.getConfiguration());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(searchSitesAndDrivesCtrl);
		removeAsListenerAndDispose(confirmRemoveCtrl);
		removeAsListenerAndDispose(cmc);
		searchSitesAndDrivesCtrl = null;
		confirmRemoveCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(sitesAndDrivesConfigurationTableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				SiteAndDriveConfiguration configuration = sitesAndDrivesConfigurationModel.getObject(se.getIndex());
				if("remove".equals(cmd)) {
					doConfirmRemoveConfiguration(ureq, configuration);
				}
			}
		} else if(moduleEnabledEl == source || sitesEnabledEl == source) {
			updateUI();
		} else if(addSitesAndDrivesButton == source) {
			doAddSitesAndDrives(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = moduleEnabledEl.isOn();
		sharePointModule.setEnabled(enabled);
		sharePointModule.setSitesEnabled(enabled && sitesEnabledEl.isOn());
		sharePointModule.setOneDriveEnabled(enabled && oneDriveEnabledEl.isOn());
		sharePointModule.setRolesEnabledList(adminPropsEl.getSelectedKeys());
		
		if(enabled) {
			String sitesSearch = searchSitesEl.getValue();
			sharePointModule.setSitesSearch(sitesSearch);
			
			List<SiteAndDriveConfiguration> configurationsList = sitesAndDrivesConfigurationModel.getObjects();
			SitesAndDrivesConfiguration configuration = new SitesAndDrivesConfiguration(configurationsList);
			sharePointModule.setSitesConfiguration(configuration);
			
			List<String> excludeSitesAndDrives = toList(excludeSitesAndDrivesEl.getValue());
			sharePointModule.setExcludeSitesAndDrives(excludeSitesAndDrives);
			
			List<String> exclusionLabels = toList(excludeLabelsEl.getValue());
			sharePointModule.setExcludeLabels(exclusionLabels);
		}
	}
	
	private List<String> toList(String val) {
		if(StringHelper.containsNonWhitespace(val)) {
			String[] arrayString = val.split("[\r\n]");
			List<String> list = new ArrayList<>();
			for(String string:arrayString) {
				if(StringHelper.containsNonWhitespace(string)) {
					list.add(string);
				}
			}
			return list;
		}
		return List.of();
	}
	
	private String toTextArea(List<String> list) {
		if(list == null || list.isEmpty()) return "";
		return String.join("\n", list);
	}
	
	private void doAddSitesAndDrives(UserRequest ureq) {
		OAuth2Tokens tokens = ureq.getUserSession().getOAuth2Tokens();
		if(tokens == null) {
			showWarning("warning.need.azure.account");
		} else {
			searchSitesAndDrivesCtrl = new SharePointSearchSitesAndDrivesController(ureq, getWindowControl());
			listenTo(searchSitesAndDrivesCtrl);
			
			String title = translate("search.sites.and.drives.title");
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					searchSitesAndDrivesCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doFinalizeAddSitesAndDrives(List<SiteAndDriveConfiguration> configurations) {
		List<SiteAndDriveConfiguration> currentConfigurations = sitesAndDrivesConfigurationModel.getObjects();
		Set<SiteAndDriveConfiguration> deduplicatedConfiguration = new HashSet<>();
		deduplicatedConfiguration.addAll(currentConfigurations);
		deduplicatedConfiguration.addAll(configurations);
		
		List<SiteAndDriveConfiguration> mergedConfigurations = new ArrayList<>(deduplicatedConfiguration);
		if(mergedConfigurations.size() > 1) {
			Collections.sort(mergedConfigurations, new SiteAndDriveConfigurationComparator(getLocale()));
		}
		sitesAndDrivesConfigurationModel.setObjects(mergedConfigurations);
		sitesAndDrivesConfigurationTableEl.reset(true, true, true);
	}
	
	private void doConfirmRemoveConfiguration(UserRequest ureq, SiteAndDriveConfiguration configuration) {
		confirmRemoveCtrl = new ConfirmRemoveSiteOrDriveController(ureq, getWindowControl(), configuration);
		listenTo(confirmRemoveCtrl);
		
		String title = translate("remove");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				confirmRemoveCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doFinalizeConfiguration(SiteAndDriveConfiguration configuration) {
		List<SiteAndDriveConfiguration> currentConfigurations = sitesAndDrivesConfigurationModel.getObjects();
		currentConfigurations.remove(configuration);
		if(currentConfigurations.size() > 1) {
			Collections.sort(currentConfigurations, new SiteAndDriveConfigurationComparator(getLocale()));
		}
		sitesAndDrivesConfigurationModel.setObjects(currentConfigurations);
		sitesAndDrivesConfigurationTableEl.reset(true, true, true);
	}
}
