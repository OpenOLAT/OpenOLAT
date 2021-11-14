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
package org.olat.course.site.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nModule;
import org.olat.course.site.model.CourseSiteConfiguration;
import org.olat.course.site.model.LanguageConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseSiteAdminController extends FormBasicController {
	
	private FormLink okButton;
	private MultipleSelectionElement enableToolbar;
	private TextElement iconCssClassEl;
	private FormLayoutContainer tableLayout;
	
	private FlexiTableElement tableEl;
	private CourseSiteDataModel model;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController selectCtrl;
	
	private CourseSiteConfiguration siteConfiguration;
	private final RepositoryManager repositoryManager;
	
	@Autowired
	private I18nModule i18nModule;
	
	public CourseSiteAdminController(UserRequest ureq, WindowControl wControl, CourseSiteConfiguration siteConfiguration) {
		super(ureq, wControl);
		
		this.siteConfiguration = siteConfiguration;
		this.repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.menu.title");

		enableToolbar = uifactory.addCheckboxesHorizontal("site.enable.toolbar", "site.enable.toolbar", formLayout, new String[]{ "x" }, new String[]{ "" });
		enableToolbar.addActionListener(FormEvent.ONCHANGE);
		if(siteConfiguration.isToolbar()) {
			enableToolbar.select("x", true);
		}
		
		String cssClass = siteConfiguration.getNavIconCssClass();
		iconCssClassEl = uifactory.addTextElement("site.iconCssClass", "site.icon", 32, cssClass, formLayout);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CSCols.defLanguage.i18nKey(), CSCols.defLanguage.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CSCols.language.i18nKey(), CSCols.language.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CSCols.title.i18nKey(), CSCols.title.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CSCols.courseId.i18nKey(), CSCols.courseId.ordinal(), false, null));
		FlexiCellRenderer renderer = new StaticFlexiCellRenderer("openre", new TextFlexiCellRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CSCols.courseTitle.i18nKey(), CSCols.courseTitle.ordinal(), "openre", renderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("remove", translate("remove"), "remove"));

		String page = velocity_root + "/lang_options.html";
		tableLayout = FormLayoutContainer.createCustomFormLayout("site.options.lang", getTranslator(), page);
		tableLayout.setRootForm(mainForm);
		tableLayout.setLabel("site.courses", null);
		formLayout.add(tableLayout);
		
		model = new CourseSiteDataModel(columnsModel);
		
		List<LanguageConfigurationRow> configs = new ArrayList<>();
		Map<String,LanguageConfiguration> langToConfigMap = new HashMap<>();
		if(siteConfiguration.getConfigurations() != null) {
			for(LanguageConfiguration langConfig : siteConfiguration.getConfigurations()) {
				langToConfigMap.put(langConfig.getLanguage(), langConfig);
			}
		}
		
		for(String langKey:i18nModule.getEnabledLanguageKeys()) {
			if(langToConfigMap.containsKey(langKey)) {
				LanguageConfiguration langConfig = langToConfigMap.get(langKey);
				RepositoryEntry re = repositoryManager.lookupRepositoryEntryBySoftkey(langConfig.getRepoSoftKey(), false);
				configs.add(new LanguageConfigurationRow(langConfig, re, tableLayout));
			} else {
				configs.add(new LanguageConfigurationRow(new LanguageConfiguration(langKey), null, tableLayout));
			}
		}

		model.setObjects(configs);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "languageTable", model, getTranslator(), tableLayout);
		tableEl.setRendererType(FlexiTableRendererType.classic);
		tableEl.setCustomizeColumns(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "course-site-admin");

		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsLayout);
		okButton = uifactory.addFormLink("save", "save", null, buttonsLayout, Link.BUTTON);
		okButton.setCustomEnabledLinkCSS("btn btn-primary");
		//uifactory.addFormSubmitButton("save", "save", formLayout);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			cleanUp();
		} else if(selectCtrl == source) {
			cmc.deactivate();
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				LanguageConfigurationRow row = (LanguageConfigurationRow)selectCtrl.getUserObject();
				RepositoryEntry re = selectCtrl.getSelectedEntry();
				row.setRepositoryEntry(re);
				tableEl.reset();
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(selectCtrl);
		removeAsListenerAndDispose(cmc);
		cmc = null;
		selectCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == enableToolbar) {
			okButton.setCustomEnabledLinkCSS("btn btn-primary o_button_dirty");
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("remove".equals(se.getCommand())) {
					LanguageConfigurationRow row = model.getObject(se.getIndex());
					doReset(row);
					okButton.getComponent().setDirty(true);
					okButton.setCustomEnabledLinkCSS("btn btn-primary o_button_dirty");
				} else if("select".equals(se.getCommand())) {
					LanguageConfigurationRow row = model.getObject(se.getIndex());
					doSelecCourse(ureq, row);
					okButton.getComponent().setDirty(true);
					okButton.setCustomEnabledLinkCSS("btn btn-primary o_button_dirty");
				} else if("openre".equals(se.getCommand())) {
					LanguageConfigurationRow row = model.getObject(se.getIndex());
					RepositoryEntry re = row.getRepositoryEntry();
					if(re != null) {
						NewControllerFactory.getInstance().launch("[RepositoryEntry:" + re.getKey() + "]", ureq, getWindowControl());
					}
				}
			}
		} else if(source == okButton) {
			okButton.setCustomEnabledLinkCSS("btn btn-primary");
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	private void doReset(LanguageConfigurationRow row) {
		row.reset();
		tableEl.reset(true, true, true);
	}
	
	private void doSelecCourse(UserRequest ureq, LanguageConfigurationRow row) {
		removeAsListenerAndDispose(selectCtrl);
		selectCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq, new String[]{ "CourseModule" }, translate("select"),
				true, true, false, false, true, false);
		selectCtrl.setUserObject(row);
		listenTo(selectCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectCtrl.getInitialComponent(), true, translate("select"));
		cmc.activate();
		listenTo(cmc);	
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	public CourseSiteConfiguration saveConfiguration() {
		List<LanguageConfiguration> langConfigList = new ArrayList<>();
		for(LanguageConfigurationRow row:model.getObjects()) {
			if(StringHelper.containsNonWhitespace(row.getSoftKey())) {
				langConfigList.add(row.getRawObject());
			}	
		}
		
		siteConfiguration.setToolbar(enableToolbar.isAtLeastSelected(1));
		siteConfiguration.setNavIconCssClass(iconCssClassEl.getValue());
		siteConfiguration.setConfigurations(langConfigList);
		return siteConfiguration;
	}
	
	public class LanguageConfigurationRow {
		private LanguageConfiguration langConfig;
		private TextElement titleEl;
		private MultipleSelectionElement defLangEl;
		private RepositoryEntry repoEntry;
		
		public LanguageConfigurationRow(LanguageConfiguration configuration, RepositoryEntry repoEntry,
				FormItemContainer formLayout) {
			this.langConfig = configuration;
			this.repoEntry = repoEntry;
			
			String language = configuration.getLanguage();
			titleEl = uifactory.addTextElement("site.title." + language, "site.title",
					null, 32, configuration.getTitle(), formLayout);
			formLayout.add("site.flexi.title.hook." + language, titleEl);
			defLangEl = uifactory.addCheckboxesHorizontal("site.def." + language, null,
					formLayout, new String[]{ "x"}, new String[]{ "" });
			
			if(configuration.isDefaultConfiguration()) {
				defLangEl.select("x", true);
			}
		}
		
		public boolean isDefaultConfiguration() {
			return langConfig.isDefaultConfiguration();
		}

		public MultipleSelectionElement getDefLangEl() {
			return defLangEl;
		}

		public String getTitle() {
			return langConfig.getTitle();
		}
		
		public TextElement getTitleEl() {
			return titleEl;
		}
		
		public String getLanguage() {
			return langConfig.getLanguage();
		}
		
		public String getSoftKey() {
			return langConfig.getRepoSoftKey();
		}

		public String getRepoEntryDisplayName() {
			return repoEntry == null ? null : repoEntry.getDisplayname();
		}
		
		public RepositoryEntry getRepositoryEntry() {
			return repoEntry;
		}

		public void setRepositoryEntry(RepositoryEntry re) {
			repoEntry= re;
			langConfig.setTitle(re.getDisplayname());
			langConfig.setRepoSoftKey(re.getSoftkey());
			if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
				titleEl.setValue(re.getDisplayname());
			}
		}
		
		public void reset() {
			langConfig.setTitle(null);
			langConfig.setRepoSoftKey(null);
			titleEl.setValue("");
			defLangEl.uncheckAll();
			repoEntry = null;
		}
		
		public LanguageConfiguration getRawObject() {
			boolean defLang = defLangEl.isAtLeastSelected(1);
			langConfig.setDefaultConfiguration(defLang);
			String title = titleEl.getValue();
			langConfig.setTitle(title);
			return langConfig;
		}
	}
	
	private enum CSCols {
		defLanguage("site.default.language"),
		language("site.language"),
		title("site.title"),
		courseId("site.course.id"),
		courseTitle("site.course.title");
		
		private final String i18n;
		
		private CSCols(String i18n) {
			this.i18n = i18n;
		}
		
		public String i18nKey() {
			return i18n;
		}
	}
	
	private class CourseSiteDataModel extends DefaultFlexiTableDataModel<LanguageConfigurationRow> {
		
		public CourseSiteDataModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public CourseSiteDataModel createCopyWithEmptyList() {
			return new CourseSiteDataModel(getTableColumnModel());
		}

		@Override
		public Object getValueAt(int row, int col) {
			LanguageConfigurationRow id = getObject(row);
			switch(CSCols.values()[col]) {
				case defLanguage: return id.getDefLangEl();
				case language: return id.getLanguage();
				case title: return id.getTitleEl();
				case courseId: return id.getSoftKey();
				case courseTitle: return id.getRepoEntryDisplayName();
				default: return "???";
			}
		}
	}
}