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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.DocEditorOpenInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.InfoPanel;
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
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBCustomField;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionSearchParams;
import org.olat.modules.topicbroker.TBCustomFieldSearchParams;
import org.olat.modules.topicbroker.TBCustomFieldType;
import org.olat.modules.topicbroker.TBSecurityCallback;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBSelectionSearchParams;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicRef;
import org.olat.modules.topicbroker.TBTopicSearchParams;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.ui.TBTopicDataModel.TopicCols;
import org.olat.modules.topicbroker.ui.events.TBTopicEditEnrollmentsEvent;
import org.olat.modules.topicbroker.ui.events.TBTopicEditEvent;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public abstract class TBTopicListController extends FormBasicController implements FlexiTableComponentDelegate {

	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_UNDERBOOKED = "Underbooked";
	private static final String TAB_ID_WAITING_LIST = "WaitingList";
	private static final String CMD_EDIT = "edit";
	private static final String CMD_UP = "up";
	private static final String CMD_DOWN = "down";
	private static final String CMD_DELETE = "delete";
	private static final String CMD_OPEN_FILE = "open.file";
	
	private InfoPanel configPanel;
	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabUnderbooked;
	private FlexiFiltersTab tabWaitingList;
	private FormLink createLink;
	private DropdownItem addDropdown;
	private FormLink importLink;
	private TBTopicDataModel dataModel;
	private FlexiTableElement tableEl;
	private VelocityContainer detailsVC;

	private CloseableModalController cmc;
	private TBTopicEditController topicEditCtrl;
	private TBTopicSelectionsEditController selectionsEditCtrl;
	private Controller docEditorCtrl;
	private ConfirmationController deleteConfirmationCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ToolsController toolsCtrl;
	
	private TBBroker broker;
	private final TBSecurityCallback secCallback;
	private final List<TBCustomFieldDefinition> customFieldDefinitionsInTable;
	private List<Long> detailsOpenTopicKeys;
	private final Roles roles;
	private int counter = 0;
	
	@Autowired
	private TopicBrokerService topicBrokerService;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private UserManager userManager;

	public TBTopicListController(UserRequest ureq, WindowControl wControl, TBBroker broker, TBSecurityCallback secCallback) {
		super(ureq, wControl, Util.getPackageVelocityRoot(TBTopicListController.class) + "/topic_list.html");
		setTranslator(Util.createPackageTranslator(TBTopicListController.class, getLocale(), getTranslator()));
		this.broker = broker;
		this.secCallback = secCallback;
		
		TBCustomFieldDefinitionSearchParams definitionSearchParams = new TBCustomFieldDefinitionSearchParams();
		definitionSearchParams.setBroker(broker);
		customFieldDefinitionsInTable = topicBrokerService.getCustomFieldDefinitions(definitionSearchParams).stream()
				.filter(TBCustomFieldDefinition::isDisplayInTable)
				.sorted((d1, d2) -> Integer.compare(d1.getSortOrder(), d2.getSortOrder()))
				.toList();
		
		roles = ureq.getUserSession().getRoles();
		
		initForm(ureq);
		loadModel(ureq);
	}

	protected abstract String getFormInfo();
	
	protected abstract boolean isShowStatus();
	
	protected abstract boolean isShowSelections();
	
	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(3);
		
		tabAll = FlexiFiltersTabFactory.tab(
				TAB_ID_ALL,
				translate("all"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabAll);
		
		tabUnderbooked = FlexiFiltersTabFactory.tab(
				TAB_ID_UNDERBOOKED,
				translate("tab.underbooked"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabUnderbooked);
		
		tabWaitingList = FlexiFiltersTabFactory.tab(
				TAB_ID_WAITING_LIST,
				translate("tab.waiting.list.with"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabWaitingList);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabAll);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("info", getFormInfo());
		if (isShowSelections()) {
			configPanel = new InfoPanel("configs");
			configPanel.setTitle(translate("configuration"));
			configPanel.setPersistedStatusId(ureq, "tb-config-" + broker.getKey());
			formLayout.add("config", new ComponentWrapperElement(configPanel));
			updateBrokerConfigUI();
		}
		updateBrokerStatusUI();
		
		if (secCallback.canEditTopics()) {
			createLink = uifactory.addFormLink("topic.create", formLayout, Link.BUTTON);
			createLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
			
			addDropdown = uifactory.addDropdownMenu("topic.dropdown", null, null, formLayout, getTranslator());
			addDropdown.setOrientation(DropdownOrientation.right);
			addDropdown.setEmbbeded(true);
			
			importLink = uifactory.addFormLink("topics.import", formLayout, Link.LINK);
			importLink.setIconLeftCSS("o_icon o_icon-fw o_icon_import");
			importLink.setEnabled(false);
			addDropdown.addElement(importLink);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicCols.title));
		
		DefaultFlexiColumnModel minParticipantsColumn = new DefaultFlexiColumnModel(TopicCols.minParticipants);
		minParticipantsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		minParticipantsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(minParticipantsColumn);
		
		DefaultFlexiColumnModel maxParticipantsColumn = new DefaultFlexiColumnModel(TopicCols.maxParticipants);
		maxParticipantsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		maxParticipantsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(maxParticipantsColumn);
		
		if (isShowSelections()) {
			DefaultFlexiColumnModel enrolledColumn = new DefaultFlexiColumnModel(TopicCols.enrolled, new TextFlexiCellRenderer(EscapeMode.none));
			enrolledColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
			enrolledColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
			columnsModel.addFlexiColumnModel(enrolledColumn);
			
			DefaultFlexiColumnModel waitingListColumn = new DefaultFlexiColumnModel(TopicCols.waitingList, new TextFlexiCellRenderer(EscapeMode.none));
			waitingListColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
			waitingListColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
			columnsModel.addFlexiColumnModel(waitingListColumn);
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicCols.groupRestrictions));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicCols.createdBy));
		
		int columnIndex = TBTopicDataModel.CUSTOM_FIELD_OFFSET;
		for (TBCustomFieldDefinition customFieldDefinition : customFieldDefinitionsInTable) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(null, columnIndex++);
			columnModel.setHeaderLabel(customFieldDefinition.getName());
			columnsModel.addFlexiColumnModel(columnModel);
		}
		
		if (secCallback.canEditTopics()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicCols.upDown));
			
			StickyActionColumnModel toolsCol = new StickyActionColumnModel(TopicCols.tools);
			toolsCol.setAlwaysVisible(true);
			toolsCol.setSortable(false);
			toolsCol.setExportable(false);
			columnsModel.addFlexiColumnModel(toolsCol);
		}
		
		dataModel = new TBTopicDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "topic-broker-" + broker.getKey());
		tableEl.setSearchEnabled(true);
		
		String page = velocity_root + "/details.html";
		detailsVC = new VelocityContainer("details_" + counter++, "vc_details", page, getTranslator(), this);
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
		
		if (isShowSelections()) {
			initFilterTabs(ureq);
		}
	}

	public void reload(UserRequest ureq) {
		broker = topicBrokerService.getBroker(broker);
		updateBrokerStatusUI();
		updateBrokerConfigUI();
		loadConfigInfos(ureq);
		loadModel(ureq);
	}
	
	private void loadModel(UserRequest ureq) {
		TBTopicSearchParams searchParams = new TBTopicSearchParams();
		searchParams.setBroker(broker);
		List<TBTopic> topics = topicBrokerService.getTopics(searchParams);
		int topicsSize = topics.size();
		
		TBCustomFieldSearchParams customFieldsSearchParams = new TBCustomFieldSearchParams();
		customFieldsSearchParams.setBroker(broker);
		customFieldsSearchParams.setFetchDefinition(true);
		customFieldsSearchParams.setFetchVfsMetadata(true);
		Map<Long, Map<Long, TBCustomField>> topicToDefinitionToCustomFields = topicBrokerService
				.getCustomFields(customFieldsSearchParams).stream()
				.collect(Collectors.groupingBy(customField -> customField.getTopic().getKey(),
						Collectors.toMap(customField -> customField.getDefinition().getKey(), Function.identity())));
		
		Map<Long, List<TBSelection>> topicKeyToSelections = null;
		if (isShowSelections()) {
			TBSelectionSearchParams selectionSearchParams = new TBSelectionSearchParams();
			selectionSearchParams.setBroker(broker);
			topicKeyToSelections = topicBrokerService.getSelections(selectionSearchParams).stream()
					.collect(Collectors.groupingBy(selection -> selection.getTopic().getKey()));
		}
		
		topics.sort((r1, r2) -> Integer.compare(r1.getSortOrder(), r2.getSortOrder()));
		List<TBTopicRow> rows = new ArrayList<>(topics.size());
		for (int i = 0; i < topics.size(); i++) {
			TBTopic topic = topics.get(i);
			TBTopicRow row = new TBTopicRow(topic);
			row.setSortOrder(topic.getSortOrder());
			row.setCreatedByDisplayname(userManager.getUserDisplayName(topic.getCreator().getKey()));
			row.setMinEnrollments(topic.getMinParticipants() != null ? topic.getMinParticipants() : 0);
			
			forgeSelections(topicKeyToSelections, topic, row);
			
			forgeUpDown(row, topicsSize, i);
			
			rows.add(row);
		}
		
		applyFilters(rows);
		
		for (TBTopicRow row: rows) {
			if (!customFieldDefinitionsInTable.isEmpty()) {
				forgeCustomFields(
						row,
						topicToDefinitionToCustomFields.getOrDefault(row.getKey(), Map.of()));
			}
			forgeToolsLink(row);
		}
		
		applySearch(rows);
		
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
		
		if (detailsOpenTopicKeys != null && !detailsOpenTopicKeys.isEmpty()) {
			dataModel.getObjects().stream()
				.filter(row -> detailsOpenTopicKeys.contains(row.getKey()))
				.forEach(row -> {
					int index = dataModel.getObjects().indexOf(row);
					doShowDetails(ureq, row);
					tableEl.expandDetails(index);
				});
		}
	}

	private void applyFilters(List<TBTopicRow> rows) {
		if (tableEl.getSelectedFilterTab() == null || tableEl.getSelectedFilterTab() == tabAll) {
			return;
		}
		
		if (tableEl.getSelectedFilterTab() == tabUnderbooked) {
			rows.removeIf(row -> row.getNumEnrollments() >= row.getMinEnrollments());
		} else if (tableEl.getSelectedFilterTab() == tabWaitingList) {
			rows.removeIf(row -> row.getWaitingList() == 0);
		}
	}
	
	private void applySearch(List<TBTopicRow> rows) {
		String searchValue = tableEl.getQuickSearchString();
		if (StringHelper.containsNonWhitespace(searchValue)) {
			List<String> searchValues = Arrays.stream(searchValue.toLowerCase().split(" ")).filter(StringHelper::containsNonWhitespace).toList();
			rows.removeIf(row -> 
					containsNot(searchValues, row.getIdentifier())
					&& containsNot(searchValues, row.getTitle())
					&& containsNot(searchValues, row.getCustomFields())
				);
		}
	}
	
	private boolean containsNot(List<String> searchValues, List<TBCustomField> customFields) {
		if (customFields == null || customFields.isEmpty()) {
			return true;
		}
		for (TBCustomField customField : customFields) {
			if (!containsNot(searchValues, customField.getText()) || !containsNot(searchValues, customField.getFilename())) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean containsNot(List<String> searchValues, String candidate) {
		if (StringHelper.containsNonWhitespace(candidate)) {
			String candidateLowerCase = candidate.toLowerCase();
			return searchValues.stream().noneMatch(searchValue -> candidateLowerCase.indexOf(searchValue) >= 0);
		}
		return true;
	}

	private void forgeSelections(Map<Long, List<TBSelection>> topicKeyToSelections, TBTopic topic, TBTopicRow row) {
		if (topicKeyToSelections == null) {
			return;
		}
		List<TBSelection> selections = topicKeyToSelections.getOrDefault(topic.getKey(), List.of());
		int numEnrollments = (int)selections.stream().filter(TBSelection::isEnrolled).count();
		row.setNumEnrollments(numEnrollments);
		row.setEnrolledString(String.valueOf(numEnrollments));
		int waitingList = selections.size() - numEnrollments;
		row.setWaitingList(waitingList);
		row.setWaitingListString(String.valueOf(waitingList));
		
		if (numEnrollments > topic.getMaxParticipants()) {
			String enrolledString = "<span title=\"" + translate("topic.selections.message.enrollments.greater.max") + "\"><i class=\"o_icon o_icon_error\"></i> ";
			enrolledString += row.getEnrolledString();
			enrolledString += "</span>";
			row.setEnrolledString(enrolledString);
		}
		if (broker.getEnrollmentStartDate() == null) {
			if (waitingList < topic.getMinParticipants()) {
				String waitingListString = "<span title=\"" + translate("topic.selections.message.selections.less.min") + "\"><i class=\"o_icon o_icon_warn\"></i> ";
				waitingListString += row.getWaitingListString();
				waitingListString += "</span>";
				row.setWaitingListString(waitingListString);
			}
			if (waitingList > topic.getMaxParticipants()) {
				String waitingListString = "<span title=\"" + translate("topic.selections.message.selections.greater.max") + "\"><i class=\"o_icon o_icon_warn\"></i> ";
				waitingListString += row.getWaitingListString();
				waitingListString += "</span>";
				row.setWaitingListString(waitingListString);
			}
		} else {
			if (waitingList < topic.getMinParticipants()) {
				String waitingListString = "<span title=\"" + translate("topic.selections.message.selections.less.min") + "\"><i class=\"o_icon o_icon_warn\"></i> ";
				waitingListString += row.getWaitingListString();
				waitingListString += "</span>";
				row.setWaitingListString(waitingListString);
			}
			if (waitingList > topic.getMaxParticipants()) {
				String waitingListString = "<span title=\"" + translate("topic.selections.message.selections.greater.max") + "\"><i class=\"o_icon o_icon_warn\"></i> ";
				waitingListString += row.getWaitingListString();
				waitingListString += "</span>";
				row.setWaitingListString(waitingListString);
			}
		}
	}

	private void forgeUpDown(TBTopicRow row, int topicsSize, int topicIndex) {
		if (!isUpDownEnabled()) {
			return;
		}
		if (!secCallback.canEditTopics() || broker.getEnrollmentStartDate() != null) {
			return;
		}
		
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
	
	private boolean isUpDownEnabled() {
		return (tableEl.getSelectedFilterTab() == null || tableEl.getSelectedFilterTab() == tabAll)
				&& !StringHelper.containsNonWhitespace(tableEl.getQuickSearchString());
	}
	
	private void forgeCustomFields(TBTopicRow row, Map<Long, TBCustomField> definitionKeyToCustomField) {
		row.setCustomFields(new ArrayList<>(definitionKeyToCustomField.values()));
		List<FormItem> customFieldItems = new ArrayList<>(customFieldDefinitionsInTable.size());
		
		for (TBCustomFieldDefinition definition : customFieldDefinitionsInTable) {
			if (TBCustomFieldType.text == definition.getType()) {
				TBCustomField customField = definitionKeyToCustomField.get(definition.getKey());
				if (customField != null && StringHelper.containsNonWhitespace(customField.getText())) {
					String text = Formatter.truncate(customField.getText(), 200);
					StaticTextElement item = uifactory.addStaticTextElement("customfield_" + counter++, null, text, flc);
					item.setDomWrapperElement(DomWrapperElement.span);
					item.setStaticFormElement(false);
					customFieldItems.add(item);
				} else {
					customFieldItems.add(null);
				}
			} else if (TBCustomFieldType.file == definition.getType()) {
				FormLink link = null;
				TBCustomField customField = definitionKeyToCustomField.get(definition.getKey());
				if (customField != null && customField.getVfsMetadata() != null) {
					VFSLeaf topicLeaf = topicBrokerService.getTopicLeaf(row.getTopic(), definition.getIdentifier());
					if (topicLeaf != null && topicLeaf.exists()) {
						link = uifactory.addFormLink("openfile_" + counter++, CMD_OPEN_FILE, "", null, flc, Link.LINK + Link.NONTRANSLATED);
						link.setI18nKey(StringHelper.escapeHtml(topicLeaf.getName()));
						link.setIconLeftCSS("o_icon o_icon-fw " + CSSHelper.createFiletypeIconCssClassFor(topicLeaf.getName()));
						link.setUserObject(topicLeaf);
						
						DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(), roles, topicLeaf,
								customField.getVfsMetadata(), false, DocEditorService.MODES_EDIT);
						if (editorInfo.isNewWindow()) {
							link.setNewWindow(true, true, false);
						}
					}
				}
				customFieldItems.add(link);
			} else {
				customFieldItems.add(null);
			}
		}
		
		row.setCustomFieldItems(customFieldItems);
	}
	
	private void forgeToolsLink(TBTopicRow row) {
		if (!secCallback.canEditTopics()) {
			return;
		}
		
		FormLink toolsLink = uifactory.addFormLink("tools_" + row.getKey(), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon-fws o_icon-lg o_icon_actions");
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
	}
	
	private void updateBrokerStatusUI() {
		if (!isShowStatus()) {
			return;
		}
		
		flc.contextPut("statusLabel", TBUIFactory.getLabel(getTranslator(), broker));
	}
	
	private void updateBrokerConfigUI() {
		if (configPanel == null) {
			return;
		}
		
		String infos = TBUIFactory.getConfigInfos(getTranslator(), broker, true);
		configPanel.setInformations(infos);
	}
	
	private void loadConfigInfos(UserRequest ureq) {
		if (configPanel == null) {
			return;
		}
		
		configPanel.reloadStatus(ureq);
	}
	
	private void doShowDetails(UserRequest ureq, TBTopicRow row) {
		TBTopicDetailController detailsCtrl = new TBTopicDetailController(ureq, getWindowControl(), mainForm,
				row.getTopic(), row.getCustomFields(), secCallback, row.getNumEnrollments(), row.getWaitingList());
		listenTo(detailsCtrl);
		// Add as form item to catch the events...
		flc.add(detailsCtrl.getInitialFormItem());
		
		// ... and add the component to the details container.
		String detailsComponentName = "details_" + counter++;
		row.setDetailsComponentName(detailsComponentName);
		detailsVC.put(detailsComponentName, detailsCtrl.getInitialComponent());
	}
	
	private void setDetailsOpenTopics() {
		detailsOpenTopicKeys = tableEl.getDetailsIndex().stream()
				.map(i -> dataModel.getObject(i))
				.filter(Objects::nonNull)
				.map(TBTopicRow::getKey)
				.toList();
	}
	
	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return null;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (topicEditCtrl == source) {
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (deleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doDelete(ureq, (TBTopicRef)deleteConfirmationCtrl.getUserObject());
			}
			cmc.deactivate();
			cleanUp();
		} else if (source instanceof TBTopicDetailController detailsCtrl) {
			if (event instanceof TBTopicEditEvent teEvent) {
				doEditTopic(ureq, teEvent.getTopic());
			} else if (event instanceof TBTopicEditEnrollmentsEvent teEvent) {
				doEditSelections(ureq, teEvent.getTopic());
			}
		} else if (selectionsEditCtrl == source) {
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (docEditorCtrl == source) {
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
		removeAsListenerAndDispose(topicEditCtrl);
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(selectionsEditCtrl);
		removeAsListenerAndDispose(docEditorCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		topicEditCtrl = null;
		deleteConfirmationCtrl = null;
		selectionsEditCtrl = null;
		docEditorCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof UpDownEvent ude && source instanceof UpDown upDown) {
			Object userObject = upDown.getUserObject();
			if (userObject instanceof TBTopicRow row) {
				doMoveTopic(ureq, row, ude.getDirection());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createLink){
			doEditTopic(ureq, null);
		} else if (source == tableEl) {
			if (event instanceof DetailsToggleEvent) {
				DetailsToggleEvent dte = (DetailsToggleEvent)event;
				if (dte.isVisible()) {
					TBTopicRow row = dataModel.getObject(dte.getRowIndex());
					doShowDetails(ureq, row);
					setDetailsOpenTopics();
				}
			} else if (event instanceof FlexiTableFilterTabEvent) {
				loadModel(ureq);
			} else if (event instanceof FlexiTableSearchEvent ftse) {
				loadModel(ureq);
			}
		} else if (source instanceof FormLink link) {
			if (CMD_OPEN_FILE.equals(link.getCmd()) && link.getUserObject() instanceof VFSLeaf topicLeaf) {
				doOpenOrDownload(ureq, topicLeaf);
			} else if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof TBTopicRow row) {
				doOpenTools(ureq, row, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doEditTopic(UserRequest ureq, TBTopicRef topic) {
		if (guardModalController(topicEditCtrl)) return;
		
		TBTopic reloadedTopic = null;
		if (topic != null) {
			reloadedTopic = topicBrokerService.getTopic(topic);
			if (reloadedTopic == null) {
				loadModel(ureq);
				return;
			}
		}
		
		topicEditCtrl = new TBTopicEditController(ureq, getWindowControl(), broker, reloadedTopic);
		listenTo(topicEditCtrl);
		
		String title = topic == null? translate("topic.create"): translate("topic.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), topicEditCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doMoveTopic(UserRequest ureq, TBTopicRef topic, Direction direction) {
		topicBrokerService.moveTopic(getIdentity(), topic, Direction.UP == direction);
		loadModel(ureq);
	}
	
	private void doConfirmDelete(UserRequest ureq, TBTopicRef topic) {
		if (guardModalController(deleteConfirmationCtrl)) return;
		
		TBTopic reloadedTopic = topicBrokerService.getTopic(topic);
		if (reloadedTopic == null) {
			loadModel(ureq);
			return;
		}
		
		deleteConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(), 
				translate("topic.delete.confirmation.message", StringHelper.escapeHtml(reloadedTopic.getTitle())),
				translate("topic.delete.confirmation.confirm"),
				translate("topic.delete.confirmation.button"), true);
		deleteConfirmationCtrl.setUserObject(reloadedTopic);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteConfirmationCtrl.getInitialComponent(),
				true, translate("topic.delete"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(UserRequest ureq, TBTopicRef topic) {
		topicBrokerService.deleteTopicSoftly(getIdentity(), topic);
		loadModel(ureq);
	}

	private void doEditSelections(UserRequest ureq, TBTopicRef topic) {
		if (guardModalController(selectionsEditCtrl)) return;
		
		TBTopic reloadedTopic = null;
		if (topic != null) {
			reloadedTopic = topicBrokerService.getTopic(topic);
			if (reloadedTopic == null) {
				loadModel(ureq);
				return;
			}
		}
		
		selectionsEditCtrl = new TBTopicSelectionsEditController(ureq, getWindowControl(), broker, reloadedTopic);
		listenTo(selectionsEditCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectionsEditCtrl.getInitialComponent(), true, translate("enrollments.edit"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenOrDownload(UserRequest ureq, VFSLeaf topicLeaf) {
		if (topicLeaf == null || !topicLeaf.exists()) {
			showWarning("error.file.does.not.exist");
			return;
		}
		
		DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(),
				ureq.getUserSession().getRoles(), topicLeaf, topicLeaf.getMetaInfo(),
				false, DocEditorService.MODES_VIEW);
		if (editorInfo.isEditorAvailable()) {
			doOpenFile(ureq, topicLeaf);
		} else {
			doDownload(ureq, topicLeaf);
		}
	}
	
	private void doOpenFile(UserRequest ureq, VFSLeaf topicLeaf) {
		DocEditorConfigs configs = DocEditorConfigs.builder().build(topicLeaf);
		DocEditorOpenInfo docEditorOpenInfo = docEditorService.openDocument(ureq, getWindowControl(), configs,
				DocEditorService.MODES_VIEW);
		docEditorCtrl = listenTo(docEditorOpenInfo.getController());
	}
	
	private void doDownload(UserRequest ureq, VFSLeaf topicLeaf) {
		VFSMediaResource resource = new VFSMediaResource(topicLeaf);
		resource.setDownloadable(true);
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doOpenTools(UserRequest ureq, TBTopicRow row, FormLink link) {
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
		
		private final TBTopicRow row;
		private final List<String> names = new ArrayList<>(5);
		
		public ToolsController(UserRequest ureq, WindowControl wControl, TBTopicRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			putInitialPanel(mainVC);
			
			TBTopic topic = topicBrokerService.getTopic(row);
			if (topic != null) {
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
					doEditTopic(ureq, row);
				} else if (CMD_UP.equals(cmd)) {
					doMoveTopic(ureq, row, Direction.UP);
				} else if (CMD_DOWN.equals(cmd)) {
					doMoveTopic(ureq, row, Direction.DOWN);
				} else if (CMD_DELETE.equals(cmd)) {
					doConfirmDelete(ureq, row);
				}
			}
		}
	}
	
}
