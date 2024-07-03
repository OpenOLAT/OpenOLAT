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
package org.olat.modules.topicbroker.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.YesNoCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.updown.UpDown;
import org.olat.core.gui.components.updown.UpDownEvent;
import org.olat.core.gui.components.updown.UpDownEvent.Direction;
import org.olat.core.gui.components.updown.UpDownFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.util.StringHelper;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBCustomField;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionRef;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionSearchParams;
import org.olat.modules.topicbroker.TBCustomFieldSearchParams;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.ui.TBCustomFieldDefinitionDataModel.CustomFieldDefinitionCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBCustomFieldDefinitionListController extends FormBasicController {
	
	private static final String CMD_EDIT = "edit";
	private static final String CMD_UP = "up";
	private static final String CMD_DOWN = "down";
	private static final String CMD_DELETE = "delete";

	private FormLink createLink;
	private TBCustomFieldDefinitionDataModel dataModel;
	private FlexiTableElement tableEl;
	
	private CloseableModalController cmc;
	private TBCustomFieldDefinitionEditController definitionEditCtrl;
	private ConfirmationController deleteConfirmationCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ToolsController toolsCtrl;
	
	private final TBBroker broker;
	private final String info;
	
	@Autowired
	private TopicBrokerService topicBrokerService;

	public TBCustomFieldDefinitionListController(UserRequest ureq, WindowControl wControl, TBBroker broker, String info) {
		super(ureq, wControl, "custom_field_definition_list");
		this.broker = broker;
		this.info = info;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("info", info);
		
		createLink = uifactory.addFormLink("custom.field.def.create", formLayout, Link.BUTTON);
		createLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CustomFieldDefinitionCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CustomFieldDefinitionCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CustomFieldDefinitionCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CustomFieldDefinitionCols.displayInTable, new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CustomFieldDefinitionCols.upDown));
		
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(CustomFieldDefinitionCols.tools);
		toolsCol.setAlwaysVisible(true);
		toolsCol.setSortable(false);
		toolsCol.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsCol);
		
		dataModel = new TBCustomFieldDefinitionDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "topic-broker-custom-field-def" + broker.getKey());
	}
	
	private void loadModel() {
		TBCustomFieldDefinitionSearchParams searchParams = new TBCustomFieldDefinitionSearchParams();
		searchParams.setBroker(broker);
		List<TBCustomFieldDefinition> definitions = topicBrokerService.getCustomFieldDefinitions(searchParams);
		int definitionsSize = definitions.size();

		definitions.sort((r1, r2) -> Integer.compare(r1.getSortOrder(), r2.getSortOrder()));
		List<TBCustomFieldDefinitionRow> rows = new ArrayList<>(definitions.size());
		for (int i = 0; i < definitions.size(); i++) {
			TBCustomFieldDefinition definition = definitions.get(i);
			TBCustomFieldDefinitionRow row = new TBCustomFieldDefinitionRow(definition);
			row.setTypeName(TBUIFactory.getTranslatedType(getTranslator(), definition.getType()));
			
			forgeUpDown(row, definitionsSize, i);
			forgeToolsLink(row);
			
			rows.add(row);
		}
		
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}
	
	private void forgeUpDown(TBCustomFieldDefinitionRow row, int topicsSize, int topicIndex) {
		UpDown upDown = UpDownFactory.createUpDown("up_down_" + row.getKey(), UpDown.Layout.LINK_HORIZONTAL, flc.getFormItemComponent(), this);
		upDown.setUserObject(row);
		if (topicIndex == 0) {
			upDown.setTopmost(true);
		}
		if (topicIndex == topicsSize - 1) {
			upDown.setLowermost(true);
		} 
		row.setUpDown(upDown);
	}
	
	private void forgeToolsLink(TBCustomFieldDefinitionRow row) {
		FormLink toolsLink = uifactory.addFormLink("tools_" + row.getKey(), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon-fws o_icon-lg o_icon_actions");
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (definitionEditCtrl == source) {
			loadModel();
			cmc.deactivate();
			cleanUp();
		} else if (deleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doDelete((TBCustomFieldDefinitionRef)deleteConfirmationCtrl.getUserObject());
			}
			cmc.deactivate();
			cleanUp();
		} else if (definitionEditCtrl == source) {
			loadModel();
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		} else if (toolsCalloutCtrl == source) {
			cleanUp();
		} else if (toolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(definitionEditCtrl);
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		definitionEditCtrl = null;
		deleteConfirmationCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof UpDownEvent ude && source instanceof UpDown upDown) {
			Object userObject = upDown.getUserObject();
			if (userObject instanceof TBCustomFieldDefinitionRow row) {
				doMoveDefinition(row, ude.getDirection());
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createLink){
			doEditDefinition(ureq, null);
		} else if (source == tableEl) {
			if (event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			} else if (event instanceof FlexiTableSearchEvent ftse) {
				loadModel();
			}
		} else if (source instanceof FormLink link) {
			if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof TBCustomFieldDefinitionRow row) {
				doOpenTools(ureq, row, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doEditDefinition(UserRequest ureq, TBCustomFieldDefinitionRef definition) {
		if (guardModalController(definitionEditCtrl)) return;
		
		TBCustomFieldDefinition reloadedDefinition = null;
		if (definition != null) {
			reloadedDefinition = topicBrokerService.getCustomFieldDefinition(definition);
			if (reloadedDefinition == null) {
				loadModel();
				return;
			}
		}
		
		definitionEditCtrl = new TBCustomFieldDefinitionEditController(ureq, getWindowControl(), broker, reloadedDefinition);
		listenTo(definitionEditCtrl);
		
		String title = definition == null? translate("custom.field.def.create"): translate("custom.field.def.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), definitionEditCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doMoveDefinition(TBCustomFieldDefinitionRef definition, Direction direction) {
		topicBrokerService.moveCustomFieldDefinition(getIdentity(), definition, Direction.UP == direction);
		loadModel();
	}
	
	private void doConfirmDelete(UserRequest ureq, TBCustomFieldDefinitionRef definition) {
		if (guardModalController(deleteConfirmationCtrl)) return;
		
		TBCustomFieldDefinition reloadedDefinition = topicBrokerService.getCustomFieldDefinition(definition);
		if (reloadedDefinition == null) {
			loadModel();
			return;
		}
		
		TBCustomFieldSearchParams customFieldsSearchParams = new TBCustomFieldSearchParams();
		customFieldsSearchParams.setDefinition(reloadedDefinition);
		List<TBCustomField> customFields = topicBrokerService.getCustomFields(customFieldsSearchParams);
		
		deleteConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(), 
				translate("custom.field.def.delete.confirmation.message", StringHelper.escapeHtml(reloadedDefinition.getName()), String.valueOf(customFields.size())),
				translate("custom.field.def.delete.confirmation.confirm"),
				translate("custom.field.def.delete.confirmation.button"), true);
		deleteConfirmationCtrl.setUserObject(reloadedDefinition);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteConfirmationCtrl.getInitialComponent(),
				true, translate("custom.field.def.delete"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(TBCustomFieldDefinitionRef definition) {
		topicBrokerService.deleteCustomFieldDefinitionSoftly(getIdentity(), definition);
		loadModel();
	}
	
	private void doOpenTools(UserRequest ureq, TBCustomFieldDefinitionRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
	
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		
		private final TBCustomFieldDefinitionRow row;
		private final List<String> names = new ArrayList<>(3);
		
		public ToolsController(UserRequest ureq, WindowControl wControl, TBCustomFieldDefinitionRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			putInitialPanel(mainVC);
			
			TBCustomFieldDefinition definition = topicBrokerService.getCustomFieldDefinition(row);
			if (definition != null) {
				addLink("edit", CMD_EDIT, "o_icon o_icon-fw o_icon_edit");
				if (row.getUpDown() != null) {
					if (!row.getUpDown().isTopmost()) {
						addLink("move.up", CMD_UP, "o_icon o_icon-fw o_icon_move_up");
					}
					if (!row.getUpDown().isLowermost()) {
						addLink("move.down", CMD_DOWN, "o_icon o_icon-fw o_icon_move_down");
					}
				}
				
				names.add("divider");
				addLink("delete", CMD_DELETE, "o_icon o_icon-fw o_icon_delete_item");
			}
			mainVC.contextPut("names", names);
		}
		
		private void addLink(String name, String cmd, String iconCSS) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if (iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			names.add(name);
		}
		
		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if (CMD_EDIT.equals(cmd)) {
					doEditDefinition(ureq, row);
				} else if (CMD_UP.equals(cmd)) {
					doMoveDefinition(row, Direction.UP);
				} else if (CMD_DOWN.equals(cmd)) {
					doMoveDefinition(row, Direction.DOWN);
				} else if (CMD_DELETE.equals(cmd)) {
					doConfirmDelete(ureq, row);
				}
			}
		}
	}

}
