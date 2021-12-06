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
package org.olat.admin.site.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.SiteAlternativeControllerCreator;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.core.gui.control.navigation.SiteViewSecurityCallback;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Configuration of the list of sites: order, security callback, alternative controllers...
 * 
 * 
 * Initial date: 18.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SitesConfigurationController extends FormBasicController {
	
	@Autowired
	private SiteDefinitions sitesModule;
	private final Map<String,SiteDefinition> siteDefs;
	
	private final String[] secKeys;
	private final String[] secValues;
	
	private final String[] altKeys;
	private final String[] altValues;
	
	private SiteDefModel model;
	private FlexiTableElement tableEl;
	
	private boolean needAlternative = false;
	private final Map<String,SiteSecurityCallback> securityCallbacks;
	
	public SitesConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "sites_order");
		
		siteDefs = sitesModule.getAllSiteDefinitionsList();
		securityCallbacks = CoreSpringFactory.getBeansOfType(SiteSecurityCallback.class);
		//security callbacks
		secKeys = new String[securityCallbacks.size()];
		secValues = new String[securityCallbacks.size()];
		int count = 0;
		for(Map.Entry<String, SiteSecurityCallback> secEntry:securityCallbacks.entrySet()) {
			secKeys[count] = secEntry.getKey();
			String translation = translate(secEntry.getKey());
			if(translation.length() < 125) {
				secValues[count++] = translation;
			} else {
				secValues[count++] = secEntry.getKey();
			}
			
			if(secEntry.getValue() instanceof SiteViewSecurityCallback) {
				needAlternative = true;
			}
		}
		
		//alternative controller
		Map<String,SiteAlternativeControllerCreator> alternativeControllers = CoreSpringFactory.getBeansOfType(SiteAlternativeControllerCreator.class);
		altKeys = new String[alternativeControllers.size() + 1];
		altValues = new String[alternativeControllers.size() + 1];
		int countAlt = 0;
		altKeys[countAlt] = "none";
		altValues[countAlt++] = translate("site.alternative.none");
		for(Map.Entry<String, SiteAlternativeControllerCreator> altEntry:alternativeControllers.entrySet()) {
			altKeys[countAlt] = altEntry.getKey();
			String translation = translate(altEntry.getKey());
			if(translation.length() < 125) {
				altValues[countAlt++] = translation;
			} else {
				altValues[countAlt++] = altEntry.getKey();
			}
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SiteCols.enabled.i18nKey(), SiteCols.enabled.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SiteCols.title.i18nKey(), SiteCols.title.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SiteCols.type.i18nKey(), SiteCols.type.ordinal(), false, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SiteCols.secCallback.i18nKey(), SiteCols.secCallback.ordinal()));
		if(needAlternative) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SiteCols.altController.i18nKey(), SiteCols.altController.ordinal()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SiteCols.defaultOrder.i18nKey(), SiteCols.defaultOrder.ordinal(), false, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("up", SiteCols.up.ordinal(), "up",
				new StaticFlexiCellRenderer("", "up", "o_icon o_icon-lg o_icon_move_up", translate("up"))));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("down", SiteCols.down.ordinal(), "down",
				new StaticFlexiCellRenderer("", "down", "o_icon o_icon-lg o_icon_move_down", translate("down"))));

		model = new SiteDefModel(columnsModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "sitesTable", model, getTranslator(), formLayout);
		tableEl.setRendererType(FlexiTableRendererType.classic);
		tableEl.setCustomizeColumns(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "sites-admin");
		
		reload();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("up".equals(se.getCommand())) {
					SiteDefRow row = model.getObject(se.getIndex());
					moveUp(row);
					doUpdateOrders();
					tableEl.getComponent().setDirty(true);
				} else if("down".equals(se.getCommand())) {
					SiteDefRow row = model.getObject(se.getIndex());
					moveDown(row);
					doUpdateOrders();
					tableEl.getComponent().setDirty(true);
				}
			}
		} else if(source instanceof SingleSelection) {
			if(source.getName().startsWith("site.security.") && source.getUserObject() instanceof SiteDefRow) {
				SiteDefRow row = (SiteDefRow)source.getUserObject();
				String selectCallbackId = row.getSecurityCallbackEl().getSelectedKey();
				boolean needAlt = (securityCallbacks.containsKey(selectCallbackId)
						&& securityCallbacks.get(selectCallbackId) instanceof SiteViewSecurityCallback);
				
				if(row.getAlternativeControllerEl().isVisible() != needAlt) {
					row.getAlternativeControllerEl().setVisible(needAlt);
					tableEl.reset();
				}
			}
			doSaveSettings();
		} else if(source instanceof MultipleSelectionElement || source instanceof SingleSelection) {
			doSaveSettings();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	protected void reload() {
		List<SiteDefRow> configs = new ArrayList<>();
		for(Map.Entry<String, SiteDefinition> entryDef:siteDefs.entrySet()) {
			String id = entryDef.getKey();
			SiteDefinition siteDef = entryDef.getValue();
			String title = translate(siteDef.getClass().getSimpleName());
			SiteConfiguration config = sitesModule.getConfigurationSite(id);
			SiteDefRow row = new SiteDefRow(siteDef, config, title, flc);
			configs.add(row);
		}
		Collections.sort(configs, new RowOrderComparator());
		model.setObjects(configs);
	}
	
	private void moveUp(SiteDefRow row) {
		List<SiteDefRow> rows = model.getObjects();
		int currentIndex = rows.indexOf(row);
		if(currentIndex > 0) {
			rows.remove(currentIndex);
			rows.add(currentIndex - 1, row);
		}
		model.setObjects(rows);
	}
	
	private void moveDown(SiteDefRow row) {
		List<SiteDefRow> rows = model.getObjects();
		int currentIndex = rows.indexOf(row);
		if(currentIndex >= 0 && currentIndex + 1 < rows.size()) {
			rows.remove(currentIndex);
			rows.add(currentIndex + 1, row);
		}
		model.setObjects(rows);
	}
	
	private void doUpdateOrders() {
		int count = 0;
		List<SiteConfiguration> configs = new ArrayList<>();
		for(SiteDefRow row:model.getObjects()) {
			SiteConfiguration config = row.getRawConfiguration(false);
			config.setOrder(count++);
			configs.add(config);
		}
		sitesModule.setSitesConfiguration(configs);
	}
	
	private void doSaveSettings() {
		int count = 0;
		List<SiteConfiguration> configs = new ArrayList<>();
		for(SiteDefRow row:model.getObjects()) {
			SiteConfiguration config = row.getRawConfiguration(true);
			config.setOrder(count++);
			configs.add(config);
		}
		sitesModule.setSitesConfiguration(configs);
	}
	
	private enum SiteCols {
		enabled("site.enabled"),
		title("site.title"),
		secCallback("site.security"),
		altController("site.alternative"),
		type("site.type"),
		defaultOrder("site.defaultOrder"),
		up("up"),
		down("down");
		
		private final String i18n;
		
		private SiteCols(String i18n) {
			this.i18n = i18n;
		}
		
		public String i18nKey() {
			return i18n;
		}
	}
	
	private class SiteDefRow {
		
		private final String title;
		private final SiteDefinition siteDef;
		private final SingleSelection secCallbackEl, altControllerEl;
		private final MultipleSelectionElement enableSiteEl;
		private final SiteConfiguration config;
		
		public SiteDefRow(SiteDefinition siteDef, SiteConfiguration config, String title, FormItemContainer formLayout) {
			this.title = title;
			this.siteDef = siteDef;
			this.config = config;
			
			String id = config.getId();
			
			secCallbackEl = uifactory.addDropdownSingleselect("site.security." + id, "site.security", formLayout, secKeys, secValues, null);
			if(siteDef.isFeatureEnabled()) {
				secCallbackEl.addActionListener(FormEvent.ONCHANGE);
			} else {
				secCallbackEl.setEnabled(false);
			}
			secCallbackEl.setUserObject(this);
			
			boolean needAlt = false;
			if(StringHelper.containsNonWhitespace(config.getSecurityCallbackBeanId())) {
				for(String secKey:secKeys) {
					if(secKey.equals(config.getSecurityCallbackBeanId())) {
						secCallbackEl.select(secKey, true);
						needAlt = (securityCallbacks.containsKey(secKey)
								&& securityCallbacks.get(secKey) instanceof SiteViewSecurityCallback);
					}
				}
			}
			
			enableSiteEl = uifactory.addCheckboxesHorizontal("site.enable." + id, null, formLayout, new String[]{ "x" }, new String[]{ "" });
			if(siteDef.isFeatureEnabled()) {
				enableSiteEl.addActionListener(FormEvent.ONCHANGE);
			} else {
				enableSiteEl.setEnabled(false);
			}
			
			altControllerEl = uifactory.addDropdownSingleselect("site.alternative." + id, "site.alternative", formLayout, altKeys, altValues, null);
			altControllerEl.addActionListener(FormEvent.ONCHANGE);
			altControllerEl.setVisible(needAlt);
			if(StringHelper.containsNonWhitespace(config.getAlternativeControllerBeanId())) {
				for(String altKey:altKeys) {
					if(altKey.equals(config.getAlternativeControllerBeanId())) {
						altControllerEl.select(altKey, true);
					}
				}
			}
			
			if("olatsites_admin".equals(id)) {
				enableSiteEl.setEnabled(false);
				altControllerEl.setEnabled(false);
				secCallbackEl.setEnabled(false);
			}
		}
		
		public boolean isEnabled() {
			return config.isEnabled();
		}
		
		public SiteDefinition getSiteDef() {
			return siteDef;
		}
		
		public SingleSelection getSecurityCallbackEl() {
			return secCallbackEl;
		}
		
		public SingleSelection getAlternativeControllerEl() {
			return altControllerEl;
		}
		
		public MultipleSelectionElement getEnabledEl() {
			return enableSiteEl;
		}
		
		public String getTitle() {
			return title;
		}

		public int getOrder() {
			return config.getOrder();
		}
		
		public SiteConfiguration getRawConfiguration(boolean update) {
			if(update) {
				config.setEnabled(enableSiteEl.isAtLeastSelected(1));
				if(secCallbackEl.isOneSelected()) {
					config.setSecurityCallbackBeanId(secCallbackEl.getSelectedKey());
				} else {
					config.setSecurityCallbackBeanId(null);
				}
				if(altControllerEl.isOneSelected() && !"none".equals(altControllerEl.getSelectedKey())) {
					config.setAlternativeControllerBeanId(altControllerEl.getSelectedKey());
				} else {
					config.setAlternativeControllerBeanId(null);
				}
			}
			return config;
		}
	}
	
	private static class RowOrderComparator implements Comparator<SiteDefRow> {

		@Override
		public int compare(SiteDefRow s1, SiteDefRow s2) {
			int o1 = s1.getOrder();
			int o2 = s2.getOrder();
			return o1 - o2;
		}

	}
	
	private static class SiteDefModel extends DefaultFlexiTableDataModel<SiteDefRow> {

		public SiteDefModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			SiteDefRow def = getObject(row);
			switch(SiteCols.values()[col]) {
				case enabled: {
					MultipleSelectionElement enableSiteEl = def.getEnabledEl();
					if(def.isEnabled()) {
						enableSiteEl.select("x", true);
					}
					return enableSiteEl;
				}
				case title: return def.getTitle();
				case secCallback: {
					return def.getSecurityCallbackEl();
				}
				case altController: return def.getAlternativeControllerEl();
				case type: return def.getSiteDef().getClass().getSimpleName();
				case defaultOrder: return def.getOrder();
				default: return "";
			}
		}
	}
}
