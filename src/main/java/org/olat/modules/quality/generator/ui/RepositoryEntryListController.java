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
package org.olat.modules.quality.generator.ui;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseModule;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.ui.RepositoryEntryListDataModel.Cols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class RepositoryEntryListController extends FormBasicController implements TooledController {

	private static final String KEY_DELIMITER = ",";
	
	private FlexiTableElement tableEl;
	private RepositoryEntryListDataModel tableModel;
	private Link addLink;
	private FormLink removeLink;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController selectCtrl;
	private RepositoryEntryRemoveConfirmationController removeConfirmationCtrl;
	
	private final TooledStackedPanel stackPanel;
	private final QualityGeneratorConfigs configs;
	
	@Autowired
	private RepositoryService repositoryService;

	public RepositoryEntryListController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, QualityGeneratorConfigs configs) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.stackPanel = stackPanel;
		this.configs = configs;
		initForm(ureq);
	}
	
	protected abstract String getConfigKey();

	protected abstract String getTablePrefKey();

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.displayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.begin));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.end));

		tableModel = new RepositoryEntryListDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, true, getTranslator(), formLayout);
		tableEl.setEmtpyTableMessageKey("repository.entry.empty.table");
		tableEl.setAndLoadPersistedPreferences(ureq, getTablePrefKey());
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		
		FormLayoutContainer buttons = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttons);
		buttons.setElementCssClass("o_button_group");
		removeLink = uifactory.addFormLink("repository.entry.remove", buttons, Link.BUTTON);
		
		loadModel();
	}
	
	@Override
	public void initTools() {
		addLink = LinkFactory.createToolLink("repository.entry.select", translate("repository.entry.select"), this);
		addLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_gen_re_add");
		stackPanel.addTool(addLink, Align.right);
	}

	private void loadModel() {
		List<Long> entryKeys = getRepositoryEntryRefs(configs, getConfigKey()).stream()
				.map(RepositoryEntryRef::getKey)
				.collect(toList());
		List<RepositoryEntry> entries = repositoryService.loadByKeys(entryKeys);
		tableModel.setObjects(entries);
		tableEl.reset(true, true, true);
		
		removeLink.setVisible(!entries.isEmpty());
		flc.setDirty(true);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == removeLink) {
			List<RepositoryEntry> entries = getSelectedRepositoryEntries();
			doConfirmRemove(ureq, entries);
		}
	}

	private List<RepositoryEntry> getSelectedRepositoryEntries() {
		return tableEl.getMultiSelectedIndex().stream()
				.map(index -> tableModel.getObject(index.intValue()))
				.collect(Collectors.toList());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == addLink) {
			doSelectRepositoryEntry(ureq);
		} 
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == selectCtrl) {
			List<RepositoryEntry> selectedEntries = Collections.emptyList();
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				selectedEntries = Collections.singletonList(selectCtrl.getSelectedEntry());
			} else if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRIES_SELECTED) {
				selectedEntries = selectCtrl.getSelectedEntries();
			}
			doAddRepositoryEntries(selectedEntries);
			cmc.deactivate();
			cleanUp();
		} else if (source == removeConfirmationCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				List<RepositoryEntry> entries = removeConfirmationCtrl.getRepositoryEntrys();
				doRemove(entries);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(removeConfirmationCtrl);
		removeAsListenerAndDispose(selectCtrl);
		removeAsListenerAndDispose(cmc);
		removeConfirmationCtrl = null;
		selectCtrl = null;
		cmc = null;
	}

	protected static List<RepositoryEntryRef> getRepositoryEntryRefs(QualityGeneratorConfigs generatorConfigs, String configKey) {
		String whiteListConfig = generatorConfigs.getValue(configKey);
		String[] keys = StringHelper.containsNonWhitespace(whiteListConfig)
				? whiteListConfig.split(KEY_DELIMITER)
				: new String[0];
		List<RepositoryEntryRef> elementRefs = Arrays.stream(keys)
				.map(Long::valueOf)
				.map(RepositoryEntryRefImpl::new)
				.collect(toList());
		return elementRefs;
	}
	
	public static void setRepositoryEntryRefs(QualityGeneratorConfigs generatorConfigs,
			List<? extends RepositoryEntryRef> entries, String configKey) {
		for (RepositoryEntryRef entry : entries) {
			doAddRepositoryEntry(generatorConfigs, entry.getKey().toString(), configKey);
		}
	}

	private static void doAddRepositoryEntry(QualityGeneratorConfigs generatorConfigs, String entryKey, String configKey) {
		if (StringHelper.containsNonWhitespace(entryKey)) {
			String whiteListConfig = generatorConfigs.getValue(configKey);
			if (StringHelper.containsNonWhitespace(whiteListConfig)) {
				String[] keys = whiteListConfig.split(KEY_DELIMITER);
				if (!Arrays.asList(keys).contains(entryKey)) {
					whiteListConfig += KEY_DELIMITER + entryKey;
				}
			} else {
				whiteListConfig = entryKey;
			}
			generatorConfigs.setValue(configKey, whiteListConfig);
		}
	}
	
	private void doSelectRepositoryEntry(UserRequest ureq) {
		selectCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq,
				new String[] { CourseModule.getCourseTypeName() }, translate("repository.entry.select.title"),
				false, false, true, false, true, false);
		listenTo(selectCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectCtrl.getInitialComponent(), true, translate("repository.entry.select.title"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doAddRepositoryEntries(List<RepositoryEntry> selectedEntries) {
		setRepositoryEntryRefs(configs, selectedEntries, getConfigKey());
		loadModel();
	}

	private void doConfirmRemove(UserRequest ureq, List<RepositoryEntry> entries) {
		if (entries.isEmpty()) {
			showWarning("repository.entry.none.selected");
		} else {
			removeConfirmationCtrl = new RepositoryEntryRemoveConfirmationController(ureq, getWindowControl(), entries);
			listenTo(removeConfirmationCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					removeConfirmationCtrl.getInitialComponent(), true, translate("repository.entry.remove.confirm.title"));
			cmc.activate();
			listenTo(cmc);
		}
	}

	private void doRemove(List<RepositoryEntry> entries) {
		List<String> keysToRemove = entries.stream()
				.map(RepositoryEntryRef::getKey)
				.map(String::valueOf)
				.collect(Collectors.toList());
		
		String whiteListConfig = configs.getValue(getConfigKey());
		String[] splittedKeys = StringHelper.containsNonWhitespace(whiteListConfig)
				? whiteListConfig.split(KEY_DELIMITER)
				: new String[0];
		List<String> currentKeys = Arrays.stream(splittedKeys).collect(Collectors.toList());
		currentKeys.removeAll(keysToRemove);
		
		String keys = currentKeys.stream().collect(joining(KEY_DELIMITER));
		configs.setValue(getConfigKey(), keys);
		loadModel();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
