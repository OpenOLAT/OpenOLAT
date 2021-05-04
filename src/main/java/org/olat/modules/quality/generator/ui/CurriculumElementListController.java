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

import java.util.Arrays;
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
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class CurriculumElementListController extends FormBasicController implements TooledController {

	private static final String KEY_DELIMITER = ",";
	
	private FlexiTableElement tableEl;
	private CurriculumElementListDataModel tableModel;
	private Link addLink;
	private FormLink removeLink;
	
	private CloseableModalController cmc;
	private CurriculumElementSelectionController selectCtrl;
	private CurriculumElementRemoveConfirmationController removeConfirmationCtrl;
	
	private final TooledStackedPanel stackPanel;
	private final QualityGenerator generator;
	private final QualityGeneratorConfigs configs;
	
	@Autowired
	private QualityGeneratorService generatorService;
	@Autowired
	private CurriculumService curriculumService;

	public CurriculumElementListController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, QualityGenerator generator, QualityGeneratorConfigs configs) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.stackPanel = stackPanel;
		this.generator = generator;
		this.configs = configs;
		initForm(ureq);
	}

	protected abstract String getConfigKey();
	
	protected abstract String getTablePrefsKey();
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumElementListDataModel.Cols.displayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumElementListDataModel.Cols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumElementListDataModel.Cols.typeName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumElementListDataModel.Cols.begin));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumElementListDataModel.Cols.end));

		tableModel = new CurriculumElementListDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, true, getTranslator(), formLayout);
		tableEl.setEmptyTableMessageKey("curriculum.element.empty.table");
		tableEl.setAndLoadPersistedPreferences(ureq, getTablePrefsKey());
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		
		FormLayoutContainer buttons = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttons);
		buttons.setElementCssClass("o_button_group");
		removeLink = uifactory.addFormLink("curriculum.element.remove", buttons, Link.BUTTON);
		
		loadModel();
	}
	
	@Override
	public void initTools() {
		addLink = LinkFactory.createToolLink("curriculum.element.add", translate("curriculum.element.add"), this);
		addLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_gen_ce_add");
		stackPanel.addTool(addLink, Align.right);
	}

	private void loadModel() {
		List<CurriculumElementRef> elementRefs = getCurriculumElementRefs(configs, getConfigKey());
		List<CurriculumElement> curriculumElements = curriculumService.getCurriculumElements(elementRefs);
		tableModel.setObjects(curriculumElements);
		tableEl.reset(true, true, true);
		
		removeLink.setVisible(!curriculumElements.isEmpty());
		flc.setDirty(true);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == removeLink) {
			List<CurriculumElement> elements = getSelectedCurriculumElements();
			doConfirmRemove(ureq, elements);
		}
	}

	private List<CurriculumElement> getSelectedCurriculumElements() {
		return tableEl.getMultiSelectedIndex().stream()
				.map(index -> tableModel.getObject(index.intValue()))
				.collect(Collectors.toList());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == addLink) {
			doSelectCurriculumElement(ureq);
		} 
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == selectCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				String elementKey = selectCtrl.getCurriculumElementKey();
				doAddCurriculumElement(elementKey);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == removeConfirmationCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				List<CurriculumElement> elements = removeConfirmationCtrl.getCurriculumElements();
				doRemove(elements);
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

	protected static List<CurriculumElementRef> getCurriculumElementRefs(QualityGeneratorConfigs generatorConfigs,
			String configKey) {
		String whiteListConfig = generatorConfigs.getValue(configKey);
		String[] keys = StringHelper.containsNonWhitespace(whiteListConfig)
				? whiteListConfig.split(KEY_DELIMITER)
				: new String[0];
		List<CurriculumElementRef> elementRefs = Arrays.stream(keys)
				.map(Long::valueOf)
				.map(CurriculumElementRefImpl::new)
				.collect(Collectors.toList());
		return elementRefs;
	}
	
	public static void setCurriculumElementRefs(QualityGeneratorConfigs generatorConfigs,
			List<? extends CurriculumElementRef> elements, String configKey) {
		for (CurriculumElementRef element : elements) {
			doAddCurriculumElement(generatorConfigs, element.getKey().toString(), configKey);
		}
	}

	private static void doAddCurriculumElement(QualityGeneratorConfigs generatorConfigs, String elementKey, String configKey) {
		if (StringHelper.containsNonWhitespace(elementKey)) {
			String whiteListConfig = generatorConfigs.getValue(configKey);
			if (StringHelper.containsNonWhitespace(whiteListConfig)) {
				String[] keys = whiteListConfig.split(KEY_DELIMITER);
				if (!Arrays.asList(keys).contains(elementKey)) {
					whiteListConfig += KEY_DELIMITER + elementKey;
				}
			} else {
				whiteListConfig = elementKey;
			}
			generatorConfigs.setValue(configKey, whiteListConfig);
		}
	}

	private void doSelectCurriculumElement(UserRequest ureq) {
		List<Organisation> organisations = generatorService.loadGeneratorOrganisations(generator);
		
		selectCtrl = new CurriculumElementSelectionController(ureq, getWindowControl(), organisations);
		listenTo(selectCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectCtrl.getInitialComponent(), true, translate("curriculum.element.select.title"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doAddCurriculumElement(String elementKey) {
		doAddCurriculumElement(configs, elementKey, getConfigKey());
		loadModel();
	}

	private void doConfirmRemove(UserRequest ureq, List<CurriculumElement> elements) {
		if (elements.isEmpty()) {
			showWarning("curriculum.element.none.selected");
		} else {
			removeConfirmationCtrl = new CurriculumElementRemoveConfirmationController(ureq, getWindowControl(), elements);
			listenTo(removeConfirmationCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					removeConfirmationCtrl.getInitialComponent(), true, translate("curriculum.element.remove.confirm.title"));
			cmc.activate();
			listenTo(cmc);
		}
	}

	private void doRemove(List<CurriculumElement> elements) {
		List<String> keysToRemove = elements.stream()
				.map(CurriculumElementRef::getKey)
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
