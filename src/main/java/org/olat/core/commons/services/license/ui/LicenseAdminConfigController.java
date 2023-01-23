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
package org.olat.core.commons.services.license.ui;

import static org.olat.core.commons.services.license.manager.LicensorConstantCreator.CONSTANT_CREATOR_TYPE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.license.LicenseHandler;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.manager.LicensorCreator;
import org.olat.core.commons.services.license.ui.LicenseTypeDataModel.LicenseTypeCols;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.repository.ui.author.OerPubRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LicenseAdminConfigController extends FormBasicController {
	
	private static final String[] keys = new String[] { "enabled" };
	private static final String[] values = new String[] { "on" };
	private static final String DEFAULT_LICENSE_TYPE_PREFIX = "dlt-";
	private static final String LICENSOR_CREATOR_TYPE_PREFIX = "lc-";
	private static final String CMD_UP = "up";
	private static final String CMD_DOWN = "down";
	private static final String CMD_TRANSLATE = "translate";
	private static final String CMD_EDIT = "edit";
	
	private MultipleSelectionElement enabledEl;
	private FormLink addLicenseTypeButton;
	private FlexiTableElement tableEl;
	private LicenseTypeDataModel dataModel;
	private Map<String, SingleSelection> defaultLicenseTypeEls = new HashMap<>();
	
	private CloseableModalController cmc;
	private EditLicenseTypeController editLicenseTypeCtrl;
	private SingleKeyTranslatorController translatorCtrl;
	private LicensorConstantController licensorConstantCtrl;
	
	private final List<LicenseHandler> licenseHandlers;
	private final List<LicensorCreator> licensorCreators;
	
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private LicenseService licenseService;

	public LicenseAdminConfigController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_config");
		licenseHandlers = new ArrayList<>(licenseModule.getHandlers());
		licensorCreators = new ArrayList<>(licenseModule.getLicenseCreators());
		Collections.sort(licensorCreators, (c1, c2) -> Integer.compare(c1.getSortOrder(), c2.getSortOrder()));
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initGeneralElements();
		initLicenseTypesTable();
		initHandlerConfigs();
	}

	private void initGeneralElements() {
		FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("general", getTranslator());
		generalCont.setFormTitle(translate("admin.menu.title"));
		generalCont.setFormContextHelp("manual_admin/administration/Licenses/");
		generalCont.setRootForm(mainForm);
		generalCont.setElementCssClass("o_sel_license_general");
		flc.add("general", generalCont);

		String[] enabledHandlerKeys = licenseHandlers.stream()
				.map(LicenseHandler::getType)
				.toArray(String[]::new);
		String[] enabledHandlerValues = licenseHandlers.stream()
				.map(handler -> handler.getTitle(getLocale()))
				.toArray(String[]::new);
		enabledEl = uifactory.addCheckboxesVertical("handler.enabled", "admin.enabled", generalCont, enabledHandlerKeys,
				enabledHandlerValues, 1);
		enabledEl.addActionListener(FormEvent.ONCHANGE);
		for (LicenseHandler handler: licenseHandlers) {
			boolean handlerEnabled = licenseModule.isEnabled(handler);
			enabledEl.select(handler.getType(), handlerEnabled);
		}
	}

	private void initLicenseTypesTable() {
		addLicenseTypeButton = uifactory.addFormLink("add.license.type", flc, Link.BUTTON);
		addLicenseTypeButton.setIconLeftCSS("o_icon o_icon_lic_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LicenseTypeCols.up, CMD_UP,
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer("", CMD_UP, "o_icon o_icon-lg o_icon_move_up"),
						null)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LicenseTypeCols.down, CMD_DOWN,
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer("", CMD_DOWN, "o_icon o_icon-lg o_icon_move_down"),
						null)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LicenseTypeCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LicenseTypeCols.nameTranslation, CMD_TRANSLATE,
				new StaticFlexiCellRenderer(CMD_TRANSLATE, new TextFlexiCellRenderer())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, LicenseTypeCols.oer.i18nKey(), LicenseTypeCols.oer.ordinal(),
				true, null, FlexiColumnModel.ALIGNMENT_LEFT, new OerPubRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LicenseTypeCols.text));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LicenseTypeCols.cssClass));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LicenseTypeCols.edit, CMD_EDIT,
				new BooleanCellRenderer(
						null,
						new StaticFlexiCellRenderer("", CMD_EDIT, "o_icon o_icon-lg o_icon_edit"))));
		
		// add a checkbox for every enabled license handler
		int index = LicenseTypeCols.values().length;
		for (LicenseHandler handler: licenseHandlers) {
			boolean handlerEnabled = licenseModule.isEnabled(handler);
			if (handlerEnabled) {
				DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(handler.getType(), index++);
				columnModel.setHeaderLabel(handler.getTitle(getLocale()));
				columnsModel.addFlexiColumnModel(columnModel);
			}
		}
		
		dataModel = new LicenseTypeDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "license.types", dataModel, getTranslator(), flc);
	}

	private void loadModel() {
		List<LicenseType> licenseTypes = licenseService.loadLicenseTypes();
		Collections.sort(licenseTypes);
		List<LicenseTypeRow> rows = new ArrayList<>();
		for (LicenseType licenseType: licenseTypes) {
			List<MultipleSelectionElement> formItems = createActivationCheckBoxes(licenseType, licenseHandlers);
			LicenseTypeRow licenseTypeRow = new LicenseTypeRow(licenseType, formItems);
			rows.add(licenseTypeRow);
		}
		dataModel.setObjects(rows);
		tableEl.reset();	
	}

	private List<MultipleSelectionElement> createActivationCheckBoxes(LicenseType licenseType,
			List<LicenseHandler> handlers) {
		List<MultipleSelectionElement> formItems = new ArrayList<>();
		for (LicenseHandler handler: handlers) {
			boolean handlerEnabled = licenseModule.isEnabled(handler);
			if (handlerEnabled) {
				String toggleName =  "toggle-" + licenseType.getKey() + handler.getType();
				MultipleSelectionElement toggle = uifactory.addCheckboxesHorizontal(toggleName, null, flc, keys, values);
				toggle.setUserObject(new HandlerLicenseType(licenseType, handler));
				toggle.addActionListener(FormEvent.ONCHANGE);
				toggle.setAjaxOnly(true);
				toggle.select(keys[0], licenseService.isActive(handler, licenseType));
				formItems.add(toggle);
			}
		}
		return formItems;
	}

	private void initHandlerConfigs() {
		List<FormLayoutContainer> handlerConfigs = new ArrayList<>(licenseHandlers.size());
		for (LicenseHandler handler: licenseHandlers) {
			boolean handlerEnabled = licenseModule.isEnabled(handler);
			if (handlerEnabled) {
				String handlerType = handler.getType();
				FormLayoutContainer handlerCont = FormLayoutContainer.createDefaultFormLayout("handler-" + handlerType, getTranslator());
				handlerCont.setRootForm(mainForm);
				flc.add("handler-" + handlerType, handlerCont);
				
				uifactory.addSpacerElement("spacer-" + handlerType, handlerCont, false);
				SpacerElement handlerName = uifactory.addSpacerElement("name-" + handlerType, handlerCont, true);
				handlerName.setLabel(handler.getTitle(getLocale()), null, false);
				
				SingleSelection defaultLicenseTypeEl = uifactory.addDropdownSingleselect(
						DEFAULT_LICENSE_TYPE_PREFIX + handlerType, "admin.default.license.type", handlerCont,
						new String[0], new String[0], null);
				defaultLicenseTypeEl.setUserObject(handler);
				defaultLicenseTypeEl.addActionListener(FormEvent.ONCHANGE);
				defaultLicenseTypeEls.put(handlerType, defaultLicenseTypeEl);
				reloadDefaultLicenseTypeEl(handler);
				
				String[] licensorCreatorKeys = licensorCreators.stream()
						.map(LicensorCreator::getType)
						.toArray(String[]::new);
				String[] licensorCreatorValues = licensorCreators.stream()
						.map(creator -> creator.getName(getLocale()))
						.toArray(String[]::new);
				SingleSelection licensorCreatorEl = uifactory.addDropdownSingleselect(
						LICENSOR_CREATOR_TYPE_PREFIX + handlerType, "admin.licensor.creator", handlerCont,
						licensorCreatorKeys, licensorCreatorValues, null);
				licensorCreatorEl.setUserObject(handler);
				String creatorType = licenseModule.getLicensorCreatorType(handler);
				if (creatorType != null) {
					licensorCreatorEl.select(creatorType, true);
				}
				licensorCreatorEl.addActionListener(FormEvent.ONCHANGE);
				
				if (CONSTANT_CREATOR_TYPE.equals(creatorType)) {
					String licensorConstant = licenseModule.getConstantLicensor(handler);
					uifactory.addStaticTextElement("lgc-" + handlerType, "admin.licensor.constant", licensorConstant, handlerCont);
					
					FormLink editLicensorConstantLink = uifactory.addFormLink("lgb-", "admin.licensor.constant.edit", "",
							handlerCont, Link.BUTTON);
					editLicensorConstantLink.setUserObject(handler);
				}
				
				handlerConfigs.add(handlerCont);
			}
		}
		flc.getFormItemComponent().contextPut("handlerConfigs", handlerConfigs);
	}
	
	private void reloadDefaultLicenseTypeEl(LicenseHandler handler) {
		SingleSelection defaultLicenseTypeEl = defaultLicenseTypeEls.get(handler.getType());
		if (defaultLicenseTypeEl != null) {
			List<LicenseType> activatedLicenseTypes = licenseService.loadActiveLicenseTypes(handler);
			Collections.sort(activatedLicenseTypes);
			String[] licenseTypeKeys = getLicenseTypeKeys(activatedLicenseTypes);
			String[] licenseTypeValues = getLicenseTypeValues(activatedLicenseTypes);
			defaultLicenseTypeEl.setKeysAndValues(licenseTypeKeys, licenseTypeValues, null);
			String defaultLicenseTypeKey = licenseModule.getDefaultLicenseTypeKey(handler);
			if (Arrays.asList(licenseTypeKeys).contains(defaultLicenseTypeKey)) {
				defaultLicenseTypeEl.select(defaultLicenseTypeKey, true);
			}
		}
	}
	
	private String[] getLicenseTypeKeys(List<LicenseType> licenseTypes) {
		return licenseTypes.stream()
				.map(LicenseType::getKey)
				.map(String::valueOf)
				.toArray(String[]::new);
	}

	private String[] getLicenseTypeValues(List<LicenseType> licenseTypes) {
		String[] handlerNames = new String[licenseTypes.size()];
		int count = 0;
		for (LicenseType licenseType: licenseTypes) {
			handlerNames[count++] = LicenseUIFactory.translate(licenseType, getLocale());
		}
		return handlerNames;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (editLicenseTypeCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == translatorCtrl) {
			loadModel();
			cmc.deactivate();
			cleanUp();
		} else if (licensorConstantCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doSaveLicensorConstant();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(licensorConstantCtrl);
		removeAsListenerAndDispose(editLicenseTypeCtrl);
		removeAsListenerAndDispose(translatorCtrl);
		removeAsListenerAndDispose(cmc);
		licensorConstantCtrl = null;
		editLicenseTypeCtrl = null;
		translatorCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (enabledEl == source) {
			doEnableHandlers();
		} else if (addLicenseTypeButton == source) {
			doAddLicenseType(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				LicenseTypeRow licenseTypeRow = dataModel.getObject(se.getIndex());
				if (CMD_UP.equals(cmd)) {
					doUp(se.getIndex());	
				} else if (CMD_DOWN.equals(cmd)) {
					doDown(se.getIndex());
				} else if (CMD_TRANSLATE.equals(cmd)) {
					doOpenTranslator(ureq, licenseTypeRow.getLicenseType());
				} else if (CMD_EDIT.equals(cmd)) {
					doEditLicenseType(ureq, licenseTypeRow.getLicenseType());
				}
			}
		} else if (source instanceof MultipleSelectionElement) {
			MultipleSelectionElement multipleSelectionElement = (MultipleSelectionElement) source;
			doActivateLicenseType(multipleSelectionElement);
		} else if (source instanceof SingleSelection) {
			SingleSelection singleSelection = (SingleSelection) source;
			String name = singleSelection.getName();
			if (name.startsWith(DEFAULT_LICENSE_TYPE_PREFIX)) {
				doSetDefaultLicenceType(source, singleSelection);
			} else if (name.startsWith(LICENSOR_CREATOR_TYPE_PREFIX)) {
				doSetLicensorCreator(source, singleSelection);
			}
		} else if (source instanceof FormLink) {
			Object userObject = source.getUserObject();
			if (userObject instanceof LicenseHandler) {
				LicenseHandler handler = (LicenseHandler) userObject;
				doEditLicensorConstant(ureq, handler);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doEnableHandlers() {
		Collection<String> selectedKeys = enabledEl.getSelectedKeys();
		for (LicenseHandler handler: licenseHandlers) {
			boolean enabled = selectedKeys.contains(handler.getType());
			licenseModule.setEnabled(handler.getType(), enabled);
		}
		initLicenseTypesTable();
		loadModel();
		initHandlerConfigs();
		showInfo("admin.start.indexer");
	}

	private void doUp(int index) {
		LicenseType downLicenseType = dataModel.getObject(index - 1).getLicenseType();
		LicenseType upLicenseType = dataModel.getObject(index).getLicenseType();;
		switchLicenseTypes(downLicenseType, upLicenseType);
	}

	private void doDown(int index) {
		LicenseType downLicenseType = dataModel.getObject(index).getLicenseType();
		LicenseType upLicenseType = dataModel.getObject(index + 1).getLicenseType();;
		switchLicenseTypes(downLicenseType, upLicenseType);
	}

	private void switchLicenseTypes(LicenseType downLicenseType, LicenseType upLicenseType) {
		int downSortOrder = downLicenseType.getSortOrder();
		int upSortOrder = upLicenseType.getSortOrder();
		downLicenseType.setSortOrder(upSortOrder);
		upLicenseType.setSortOrder(downSortOrder);
		licenseService.saveLicenseType(downLicenseType);
		licenseService.saveLicenseType(upLicenseType);
		loadModel();
	}
	
	private void doOpenTranslator(UserRequest ureq, LicenseType licenseType) {
		String i18nKey = LicenseUIFactory.LICENSE_TYPE_TRANS + licenseType.getName().toLowerCase();

		translatorCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), i18nKey,
				LicenseAdminController.class);
		listenTo(translatorCtrl);

		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), "close", translatorCtrl.getInitialComponent(), true,
				translate("admin.translate"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddLicenseType(UserRequest ureq) {
		editLicenseTypeCtrl = new EditLicenseTypeController(ureq, getWindowControl());
		listenTo(editLicenseTypeCtrl);
		
		String title = translate("add.license.type");
		cmc = new CloseableModalController(getWindowControl(), "close", editLicenseTypeCtrl.getInitialComponent(), true,
				title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditLicenseType(UserRequest ureq, LicenseType license) {
		editLicenseTypeCtrl = new EditLicenseTypeController(ureq, getWindowControl(), license);
		listenTo(editLicenseTypeCtrl);

		String title = translate("edit.license.type");
		cmc = new CloseableModalController(getWindowControl(), "close", editLicenseTypeCtrl.getInitialComponent(), true,
				title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doActivateLicenseType(MultipleSelectionElement multipleSelectionElement) {
		boolean doActivate = multipleSelectionElement.isAtLeastSelected(1)? true: false;
		HandlerLicenseType handlerLicenseType = (HandlerLicenseType) multipleSelectionElement.getUserObject();
		LicenseHandler handler = handlerLicenseType.getHandler();
		LicenseType licenseType = handlerLicenseType.getLicenseType();
		if (doActivate) {
			licenseService.activate(handler, licenseType);
			reloadDefaultLicenseTypeEl(handler);
		} else {
			if (isDefaultLicenseType(handler, licenseType)) {
				showWarning("error.is.default.license.type");
				loadModel();
			} else {
				licenseService.deactivate(handler, licenseType);
				reloadDefaultLicenseTypeEl(handler);
			}
		}
		// checkboxes of enabled handlers was deactivated after deactivating a license type.
		initGeneralElements();
	}
	
	private boolean isDefaultLicenseType(LicenseHandler handler, LicenseType licenseType) {
		String defaultLicenseTypeKey = licenseModule.getDefaultLicenseTypeKey(handler);
		String licenseTypeKey = String.valueOf(licenseType.getKey());
		return licenseTypeKey.equals(defaultLicenseTypeKey);
	}

	private void doSetDefaultLicenceType(FormItem source, SingleSelection singleSelection) {
		if (singleSelection.isOneSelected()) {
			String selectedKey = singleSelection.getSelectedKey();
			LicenseHandler handler = (LicenseHandler) source.getUserObject();
			LicenseType licenseType = licenseService.loadLicenseTypeByKey(selectedKey);
			licenseModule.setDefaultLicenseTypeKey(handler, String.valueOf(licenseType.getKey()));
		}
	}

	private void doSetLicensorCreator(FormItem source, SingleSelection singleSelection) {
		if (singleSelection.isOneSelected()) {
			String creatorType = singleSelection.getSelectedKey();
			LicenseHandler handler = (LicenseHandler) source.getUserObject();
			licenseModule.setLicensorCreatorType(handler, creatorType);
			initHandlerConfigs();
		}
	}

	private void doEditLicensorConstant(UserRequest ureq, LicenseHandler handler) {
		String licensor = licenseModule.getConstantLicensor(handler);
		licensorConstantCtrl = new LicensorConstantController(ureq, getWindowControl(), handler, licensor);
		listenTo(licensorConstantCtrl);

		String title = translate("admin.licensor.constant.title");
		cmc = new CloseableModalController(getWindowControl(), "close", licensorConstantCtrl.getInitialComponent(),
				true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSaveLicensorConstant() {
		LicenseHandler handler = licensorConstantCtrl.getHandler();
		String licensor = licensorConstantCtrl.getLicensor();
		licenseModule.setConstantLicensor(handler, licensor);
		initHandlerConfigs();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private static class HandlerLicenseType {
		
		private final LicenseType licenseType;
		private final LicenseHandler handler;
		
		HandlerLicenseType(LicenseType licenseType, LicenseHandler handler) {
			this.licenseType = licenseType;
			this.handler = handler;
		}

		LicenseType getLicenseType() {
			return licenseType;
		}

		LicenseHandler getHandler() {
			return handler;
		}
	}

}
