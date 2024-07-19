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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.DocEditorOpenInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.dropdown.Dropdown.SpacerItem;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.InfoPanel;
import org.olat.core.gui.components.updown.UpDown;
import org.olat.core.gui.components.updown.UpDownEvent;
import org.olat.core.gui.components.updown.UpDownEvent.Direction;
import org.olat.core.gui.components.updown.UpDownFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.lightbox.LightboxController;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBCustomField;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionSearchParams;
import org.olat.modules.topicbroker.TBCustomFieldSearchParams;
import org.olat.modules.topicbroker.TBCustomFieldType;
import org.olat.modules.topicbroker.TBGroupRestrictionInfo;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBSelectionSearchParams;
import org.olat.modules.topicbroker.TBSelectionStatus;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicRef;
import org.olat.modules.topicbroker.TBTopicSearchParams;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.ui.TBSelectionDataModel.SelectionCols;
import org.olat.modules.topicbroker.ui.events.TBBrokerChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBSelectionController extends FormBasicController implements FlexiTableComponentDelegate, GenericEventListener {
	
	private static final String CMD_UP = "up";
	private static final String CMD_DOWN = "down";
	private static final String CMD_SELECT = "select";
	private static final String CMD_UNSELECT = "unselect";
	private static final String CMD_SELECT_FIRST = "select_first";
	private static final String CMD_SELECT_LAST = "select_last";
	private static final String CMD_SELECT_POS = "select_pos_";
	private static final String CMD_DETAILS = "details";
	private static final String CMD_OPEN_FILE = "open.file";
	
	private InfoPanel configPanel;
	private SingleSelection maxEnrollmentsEl;
	private final TBSelectionStatusRenderer statusRenderer;
	private TBSelectionDataModel selectionDataModel;
	private FlexiTableElement selectionTableEl;
	private TBSelectionDataModel topicDataModel;
	private FlexiTableElement topicTableEl;
	
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private SelectionToolsController selectionToolsCtrl;
	private TopicToolsController topicToolsCtrl;
	private LightboxController lightboxCtrl;
	private Controller docEditorCtrl;
	private TBSelectionDetailController detailCtrl;

	private TBBroker broker;
	private final TBPeriodEvaluator periodEvaluator;
	private TBParticipant participant;
	private Set<Long> participantGroupKeys;
	private final List<TBCustomFieldDefinition> customFieldDefinitions;
	private final List<TBCustomFieldDefinition> customFieldDefinitionsInTable;
	private int selectionsSize;
	private final Roles roles;
	private int counter = 0;

	@Autowired
	private TopicBrokerService topicBrokerService;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private Coordinator coordinator;
	
	public TBSelectionController(UserRequest ureq, WindowControl wControl, TBBroker broker) {
		super(ureq, wControl, "selection");
		this.broker = broker;
		periodEvaluator = new TBPeriodEvaluator(broker);
		participant = topicBrokerService.getOrCreateParticipant(getIdentity(), broker, getIdentity());
		statusRenderer = new TBSelectionStatusRenderer();
		
		TBCustomFieldDefinitionSearchParams definitionSearchParams = new TBCustomFieldDefinitionSearchParams();
		definitionSearchParams.setBroker(broker);
		customFieldDefinitions = topicBrokerService.getCustomFieldDefinitions(definitionSearchParams).stream()
				.sorted((d1, d2) -> Integer.compare(d1.getSortOrder(), d2.getSortOrder()))
				.toList();
		customFieldDefinitionsInTable = customFieldDefinitions.stream()
				.filter(TBCustomFieldDefinition::isDisplayInTable)
				.toList();
		
		roles = ureq.getUserSession().getRoles();
		coordinator.getEventBus().registerFor(this, null, broker);
		
		initForm(ureq);
		loadModel(false);
	}
	
	@Override
	public void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		configPanel = new InfoPanel("configs");
		configPanel.setTitle(translate("config.overview.selection.title"));
		configPanel.setPersistedStatusId(ureq, "tb-selection-config-" + broker.getKey());
		formLayout.add("config", new ComponentWrapperElement(configPanel));
		updateBrokerConfigUI();
		
		updateBrokerStatusUI();
		
		if (broker.isParticipantCanEditRequiredEnrollments()) {
			FormLayoutContainer myEnrollmentsCont = FormLayoutContainer.createDefaultFormLayout("myEnrollments", getTranslator());
			myEnrollmentsCont.setFormTitle(translate("selection.individual.enrollments"));
			myEnrollmentsCont.setFormInfo(translate("selection.individual.enrollments.info"));
			myEnrollmentsCont.setRootForm(mainForm);
			formLayout.add(myEnrollmentsCont);
			
			SelectionValues maxEnrollmentsSV = new SelectionValues();
			for (int i = 0; i < broker.getRequiredEnrollments().intValue(); i++) {
				maxEnrollmentsSV.add(SelectionValues.entry(String.valueOf(i), String.valueOf(i)));
			}
			maxEnrollmentsSV.add(SelectionValues.entry(
					String.valueOf(broker.getRequiredEnrollments()),
					translate("participant.max.enrollments.default", String.valueOf(broker.getRequiredEnrollments()))));
			if (participant.getRequiredEnrollments() != null && participant.getRequiredEnrollments() > broker.getRequiredEnrollments()) {
				maxEnrollmentsSV.add(SelectionValues.entry(String.valueOf(
						participant.getRequiredEnrollments()),
						String.valueOf(participant.getRequiredEnrollments())));
			}
			maxEnrollmentsEl = uifactory.addDropdownSingleselect("selection.individual.enrollments.my", myEnrollmentsCont, maxEnrollmentsSV.keys(), maxEnrollmentsSV.values());
			maxEnrollmentsEl.addActionListener(FormEvent.ONCHANGE);
			updateMaxEnrollmentsEnabledUI();
		}
		
		initSelectionTable(formLayout);
		initTopicsTable(formLayout);
	}

	private void initSelectionTable(FormItemContainer formLayout) {
		FlexiTableColumnModel selectionColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		selectionColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectionCols.priority, new TextFlexiCellRenderer(EscapeMode.none)));
		
		selectionColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectionCols.title));
		selectionColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectionCols.status, statusRenderer));
		
		DefaultFlexiColumnModel minParticipantsColumn = new DefaultFlexiColumnModel(SelectionCols.minParticipants);
		minParticipantsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		minParticipantsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		selectionColumnsModel.addFlexiColumnModel(minParticipantsColumn);
		
		DefaultFlexiColumnModel maxParticipantsColumn = new DefaultFlexiColumnModel(SelectionCols.maxParticipants);
		maxParticipantsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		maxParticipantsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		selectionColumnsModel.addFlexiColumnModel(maxParticipantsColumn);
		
		int columnIndex = TBSelectionDataModel.CUSTOM_FIELD_OFFSET;
		for (TBCustomFieldDefinition customFieldDefinition : customFieldDefinitions) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(null, columnIndex++);
			columnModel.setHeaderLabel(StringHelper.escapeHtml(customFieldDefinition.getName()));
			columnModel.setDefaultVisible(customFieldDefinition.isDisplayInTable());
			selectionColumnsModel.addFlexiColumnModel(columnModel);
		}
		
		if (periodEvaluator.isSelectionPeriod()) {
			selectionColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectionCols.upDown));
		}
		
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(SelectionCols.selectionTools);
		toolsCol.setAlwaysVisible(true);
		toolsCol.setSortable(false);
		toolsCol.setExportable(false);
		selectionColumnsModel.addFlexiColumnModel(toolsCol);
		
		selectionDataModel = new TBSelectionDataModel(selectionColumnsModel);
		selectionTableEl = uifactory.addTableElement(getWindowControl(), "selectionTable", selectionDataModel, 20, false, getTranslator(), formLayout);
	}

	private void initTopicsTable(FormItemContainer formLayout) {
		FlexiTableColumnModel selectionColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		selectionColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectionCols.title, CMD_DETAILS));
		selectionColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectionCols.status, statusRenderer));
		
		DefaultFlexiColumnModel minParticipantsColumn = new DefaultFlexiColumnModel(SelectionCols.minParticipants);
		minParticipantsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		minParticipantsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		selectionColumnsModel.addFlexiColumnModel(minParticipantsColumn);
		
		DefaultFlexiColumnModel maxParticipantsColumn = new DefaultFlexiColumnModel(SelectionCols.maxParticipants);
		maxParticipantsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		maxParticipantsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		selectionColumnsModel.addFlexiColumnModel(maxParticipantsColumn);
		
		int columnIndex = TBSelectionDataModel.CUSTOM_FIELD_OFFSET;
		for (TBCustomFieldDefinition customFieldDefinition : customFieldDefinitionsInTable) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(null, columnIndex++);
			columnModel.setHeaderLabel(StringHelper.escapeHtml(customFieldDefinition.getName()));
			selectionColumnsModel.addFlexiColumnModel(columnModel);
		}
		
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(SelectionCols.topicTools);
		toolsCol.setAlwaysVisible(true);
		toolsCol.setSortable(false);
		toolsCol.setExportable(false);
		selectionColumnsModel.addFlexiColumnModel(toolsCol);
		
		topicDataModel = new TBSelectionDataModel(selectionColumnsModel);
		topicTableEl = uifactory.addTableElement(getWindowControl(), "topicTable", topicDataModel, 20, false, getTranslator(), formLayout);
		topicTableEl.setSearchEnabled(true);
		
		if (periodEvaluator.isBeforeSelectionPeriod()) {
			topicTableEl.setEmptyTableSettings("topics.available.empty.message.selection.not.started",
					"topics.available.empty.message.selection.not.started.hint", "o_icon_topicbroker");
		} else {
			topicTableEl.setEmptyTableSettings("topics.available.empty.message.no.topics", null, "o_icon_topicbroker");
		}

		topicTableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		topicTableEl.setRendererType(FlexiTableRendererType.custom);
		topicTableEl.setCssDelegate(new TopicCssDelegate());
		VelocityContainer rowVC = createVelocityContainer("topic_row");
		rowVC.setDomReplacementWrapperRequired(false);
		topicTableEl.setRowRenderer(rowVC, this);
	}
	
	private void loadModel(boolean refreshPeriodEvaluator) {
		if (refreshPeriodEvaluator) {
			refreshPeriodEvaluator();
		}
		
		participant = topicBrokerService.getOrCreateParticipant(getIdentity(), broker, getIdentity());
		updateMaxEnrollmentsUI();
		
		TBTopicSearchParams searchParams = new TBTopicSearchParams();
		searchParams.setBroker(broker);
		List<TBTopic> topics = topicBrokerService.getTopics(searchParams);
		
		TBCustomFieldSearchParams customFieldsSearchParams = new TBCustomFieldSearchParams();
		customFieldsSearchParams.setBroker(broker);
		customFieldsSearchParams.setFetchDefinition(true);
		customFieldsSearchParams.setFetchVfsMetadata(true);
		Map<Long, Map<Long, TBCustomField>> topicToDefinitionToCustomFields = topicBrokerService
				.getCustomFields(customFieldsSearchParams).stream()
				.collect(Collectors.groupingBy(customField -> customField.getTopic().getKey(),
						Collectors.toMap(customField -> customField.getDefinition().getKey(), Function.identity())));
		
		TBSelectionSearchParams selectionSearchParams = new TBSelectionSearchParams();
		selectionSearchParams.setBroker(broker);
		selectionSearchParams.setIdentity(getIdentity());
		List<TBSelection> selections = topicBrokerService.getSelections(selectionSearchParams);
		Map<Long, TBSelection> topicKeyToSelection = selections.stream()
				.collect(Collectors.toMap(selection -> selection.getTopic().getKey(), Function.identity()));
		selectionsSize = selections.size();
		int maxEnrollments = TBUIFactory.getRequiredEnrollments(broker, participant);
		int numEnrollments = (int)selections.stream().filter(TBSelection::isEnrolled).count();
	
		loadParticipantGroupKeys(topics);
		
		List<TBSelectionRow> topicRows = new ArrayList<>(topics.size());
		List<TBSelectionRow> selectionRows = new ArrayList<>();
		for (TBTopic topic : topics) {
			TBSelectionRow row = new TBSelectionRow();
			row.setTopic(topic);
			row.setTitleAbbr(TBUIFactory.getTitleAbbr(topic.getTitle()));
			row.setParticipants(TBUIFactory.getParticipantRange(getTranslator(), topic));
			row.setTopicSortOrder(topic.getSortOrder());
			
			TBSelection selection = topicKeyToSelection.get(topic.getKey());
			if (selection != null) {
				row.setSelectionRef(selection);
				row.setSelectionSortOrder(selection.getSortOrder());
				row.setEnrolled(selection.isEnrolled());
				forgeStatus(row, maxEnrollments, numEnrollments);
				row.setPriorityLabel(TBUIFactory.getPriorityLabelAsRow(getTranslator(), row.getStatus(), selection.getSortOrder()));
				forgeUpDown(row);
				selectionRows.add(row);
			}
			
			if (!customFieldDefinitionsInTable.isEmpty()) {
				forgeCustomFields(
						row,
						topicToDefinitionToCustomFields.getOrDefault(row.getTopic().getKey(), Map.of())
						);
			}
			forgeThumbnail(row, topic);
			forgeToolsLink(row);
			forgeCardButtons(row);
			
			topicRows.add(row);
		}
		
		selectionRows.sort((r1, r2) -> Integer.compare(r1.getSelectionSortOrder(), r2.getSelectionSortOrder()));
		fillEmptySelectionRows(selectionRows);
		selectionDataModel.setObjects(selectionRows);
		selectionTableEl.reset(false, false, true);
		
		if (!periodEvaluator.isBeforeSelectionPeriod()) {
			applySearch(topicRows);
			applyGroupRestricions(topicRows);
			topicRows.sort((r1, r2) -> Integer.compare(r1.getTopicSortOrder(), r2.getTopicSortOrder()));
			topicDataModel.setObjects(topicRows);
			topicTableEl.reset(false, false, true);
		}
		
		updateSelectionMessage();
	}

	private void applySearch(List<TBSelectionRow> rows) {
		String searchValue = topicTableEl.getQuickSearchString();
		if (StringHelper.containsNonWhitespace(searchValue)) {
			List<String> searchValues = Arrays.stream(searchValue.toLowerCase().split(" ")).filter(StringHelper::containsNonWhitespace).toList();
			rows.removeIf(row -> 
					containsNot(searchValues, row.getTitle())
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

	private void applyGroupRestricions(List<TBSelectionRow> rows) {
		rows.removeIf(row -> row.getGroupRestrictionKeys() != null
				&& row.getGroupRestrictionKeys().stream().noneMatch(key -> participantGroupKeys.contains(key)));
	}
	
	private void forgeCustomFields(TBSelectionRow row, Map<Long, TBCustomField> definitionKeyToCustomField) {
		row.setCustomFields(new ArrayList<>(definitionKeyToCustomField.values()));
		List<FormItem> customFieldItems = new ArrayList<>(customFieldDefinitionsInTable.size());
		
		for (TBCustomFieldDefinition definition : customFieldDefinitionsInTable) {
			if (TBCustomFieldType.text == definition.getType()) {
				TBCustomField customField = definitionKeyToCustomField.get(definition.getKey());
				if (customField != null && StringHelper.containsNonWhitespace(customField.getText())) {
					String text = TBUIFactory.formatPrettyText(customField.getText(), 200);
					StaticTextElement item = uifactory.addStaticTextElement("customfield_" + counter++, null, text, flc);
					item.setLabel("noTransOnlyParam", new String[] {StringHelper.escapeHtml(definition.getName())});
					item.setDomWrapperElement(DomWrapperElement.div);
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
						link.setLabel("noTransOnlyParam", new String[] {StringHelper.escapeHtml(definition.getName())});
						link.setI18nKey(StringHelper.escapeHtml(topicLeaf.getName()));
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

	private void forgeUpDown(TBSelectionRow row) {
		if (!periodEvaluator.isSelectionPeriod()) {
			return;
		}
		
		UpDown upDown = UpDownFactory.createUpDown("up_down_" + row.getSelectionRef().getKey(), UpDown.Layout.LINK_HORIZONTAL, flc.getFormItemComponent(), this);
		upDown.setUserObject(row);
		if (row.getSelectionSortOrder() == 1) {
			upDown.setTopmost(true);
		}
		if (row.getSelectionSortOrder() == selectionsSize) {
			upDown.setLowermost(true);
		}
		
		row.setUpDown(upDown);
	}
	
	private void forgeThumbnail(TBSelectionRow row, TBTopic topic) {
		VFSLeaf topicLeaf = topicBrokerService.getTopicLeaf(topic, TopicBrokerService.TOPIC_TEASER_IMAGE);
		if (topicLeaf != null) {
			VFSMediaMapper mapper = new VFSMediaMapper(topicLeaf);
			MapperKey mapperKey = mapperService.register(null, getThumbnailMapperId(topic, topicLeaf), mapper, 10);
			row.setThumbnailUrl(mapperKey.getUrl());
		}
	}
	
	private String getThumbnailMapperId(TBTopic topic, VFSLeaf topicLeaf) {
		return TopicBrokerService.TOPIC_TEASER_IMAGE + topic.getKey() + "::" + topicLeaf.getLastModified();
	}
	
	private void forgeToolsLink(TBSelectionRow row) {
		// Selection tools
		if (periodEvaluator.isSelectionPeriod()) {
			forgeSelectionToolsLink(row);
		}
		
		if (periodEvaluator.isWithdrawPeriod()) {
			if (periodEvaluator.isSelectionPeriod() || row.isEnrolled()) {
				forgeSelectionToolsLink(row);
			}
		}
		
		// Topic tools
		if (row.getSelectionRef() == null) {
			if (periodEvaluator.isSelectionPeriod()) {
				forgeTopicToolsLink(row);
			}
		} else {
			if (periodEvaluator.isWithdrawPeriod()) {
				if (periodEvaluator.isSelectionPeriod() || row.isEnrolled()) {
					forgeTopicToolsLink(row);
				}
			}
		}
	}

	private void forgeSelectionToolsLink(TBSelectionRow row) {
		FormLink selectionToolsLink = uifactory.addFormLink("tools_" + row.getTopic().getKey(), "selectionTools", "", null, null, Link.NONTRANSLATED);
		selectionToolsLink.setIconLeftCSS("o_icon o_icon-fws o_icon-lg o_icon_actions");
		selectionToolsLink.setUserObject(row);
		row.setSelectionToolsLink(selectionToolsLink);
	}

	private void forgeTopicToolsLink(TBSelectionRow row) {
		FormLink topicToolsLink = uifactory.addFormLink("tools_" + row.getTopic().getKey(), "topicTools", "", null, null, Link.NONTRANSLATED);
		topicToolsLink.setIconLeftCSS("o_icon o_icon-fws o_icon-lg o_icon_actions");
		topicToolsLink.setUserObject(row);
		row.setTopicToolsLink(topicToolsLink);
	}
	
	private void forgeCardButtons(TBSelectionRow row) {
		FormLink detailsButton = uifactory.addFormLink("details_" + row.getTopic().getKey(), CMD_DETAILS, "topic.details", null, flc, Link.BUTTON);
		detailsButton.setUserObject(row);
		row.setDetailsButton(detailsButton);
		
		if (periodEvaluator.isWithdrawPeriod() && row.getSelectionRef() != null) {
			if (periodEvaluator.isSelectionPeriod() || row.isEnrolled()) {
				FormLink unselectButton = uifactory.addFormLink("unselect_" + row.getTopic().getKey(), CMD_UNSELECT, "withdraw", null, flc, Link.BUTTON);
				unselectButton.setUserObject(row);
				row.setUnselectButton(unselectButton);
			}
		}
		
		if (periodEvaluator.isSelectionPeriod() && row.getSelectionRef() == null) {
			FormLink selectButton = uifactory.addFormLink("select_" + row.getTopic().getKey(), CMD_SELECT, "select", null, flc, Link.BUTTON);
			selectButton.setPrimary(true);
			selectButton.setUserObject(row);
			row.setSelectButton(selectButton);
			
			DropdownItem selectDropdown = uifactory.addDropdownMenu("selectdd_" + row.getTopic().getKey(), null, null, flc, getTranslator());
			selectDropdown.setOrientation(DropdownOrientation.right);
			selectDropdown.setPrimary(true);
			row.setSelectDropdown(selectDropdown);
			
			FormLink selectFirstLink = uifactory.addFormLink("selectf_" + row.getTopic().getKey(), CMD_SELECT_FIRST, "", null, flc, Link.NONTRANSLATED);
			selectFirstLink.setI18nKey(translate("select.pos.first"));
			selectFirstLink.setIconLeftCSS("o_icon o_icon-fw o_icon_tb_select_first");
			selectFirstLink.setUserObject(row);
			selectDropdown.addElement(selectFirstLink);
			
			FormLink selectLastLink = uifactory.addFormLink("selectl_" + row.getTopic().getKey(), CMD_SELECT_LAST, "", null, flc, Link.NONTRANSLATED);
			selectLastLink.setI18nKey(translate("select.pos.last"));
			selectLastLink.setIconLeftCSS("o_icon o_icon-fw o_icon_tb_select_last");
			selectLastLink.setUserObject(row);
			selectDropdown.addElement(selectLastLink);
			
			selectDropdown.addElement(new SpacerItem("space"));
			
			for (int i = 1; i <= selectionsSize + 1; i++) {
				String cmd = CMD_SELECT_POS + i;
				FormLink selectPosLink = uifactory.addFormLink("selectp_" + row.getTopic().getKey() + "_" + i, cmd, "", null, flc, Link.NONTRANSLATED);
				selectPosLink.setI18nKey(translate("select.pos", String.valueOf(i)));
				selectPosLink.setUserObject(row);
				selectDropdown.addElement(selectPosLink);
			}
		}
	}

	private void fillEmptySelectionRows(List<TBSelectionRow> selectionRows) {
		for (int i = selectionRows.size() + 1; i <= broker.getMaxSelections(); i++) {
			TBSelectionRow row = new TBSelectionRow();
			row.setSelectionSortOrder(i);
			row.setPriorityLabel(TBUIFactory.getPriorityLabelAsRow(getTranslator(), TBSelectionStatus.fillIn, i));
			selectionRows.add(row);
		}
	}

	private void forgeStatus(TBSelectionRow row, int maxEnrollments, int numEnrollments) {
		TBSelectionStatus status = TBUIFactory.getSelectionStatus(broker, maxEnrollments, numEnrollments,
				true, row.isEnrolled(), row.getSelectionSortOrder());
		row.setStatus(status);
		row.setTranslatedStatus(TBUIFactory.getTranslatedStatus(getTranslator(), row.getStatus()));
		row.setStatusLabel(statusRenderer.render(getTranslator(), row));
	}

	private void loadParticipantGroupKeys(List<TBTopic> topics) {
		if (participantGroupKeys != null) {
			return;
		}
		
		Set<Long> allGroupRestrictionKeys = topics.stream()
				.map(TBTopic::getGroupRestrictionKeys)
				.filter(Objects::nonNull)
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
		participantGroupKeys = topicBrokerService.filterMembership(getIdentity(), allGroupRestrictionKeys);
	}
	
	private void updateMaxEnrollmentsEnabledUI() {
		if (maxEnrollmentsEl != null) {
			maxEnrollmentsEl.setEnabled(periodEvaluator.isSelectionPeriod());
		}
	}

	private void updateMaxEnrollmentsUI() {
		if (maxEnrollmentsEl != null) {
			if (participant.getRequiredEnrollments() != null) {
				maxEnrollmentsEl.select(participant.getRequiredEnrollments().toString(), true);
			} else {
				maxEnrollmentsEl.select(String.valueOf(broker.getRequiredEnrollments()), true);
			}
		}
	}

	private void updateSelectionMessage() {
		if (selectionsSize < broker.getMaxSelections()) {
			String selectionMsg = translate("selection.msg.not.all.selected", new String[] {
					String.valueOf(selectionsSize), String.valueOf(broker.getMaxSelections()),
					String.valueOf(broker.getMaxSelections() - selectionsSize)});
			flc.contextPut("selectionMsg", selectionMsg);
		} else {
			flc.contextRemove("selectionMsg");
		}
	}
	
	private void updateBrokerStatusUI() {
		flc.contextPut("statusLabel", TBUIFactory.getLabel(getTranslator(), broker));
	}
	
	private void updateBrokerConfigUI() {
		String infos = "<div>" + translate("config.overview.selection.hint") + "</div>";
		infos += "<br>";
		infos += "<ul class=\"list-unstyled\">";
		infos += TBUIFactory.getConfigInfos(getTranslator(), broker, false);
		infos += "</ul>";
		configPanel.setInformations(infos);
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>();
		if (rowObject instanceof TBSelectionRow selectionRow) {
			if (selectionRow.getCustomFieldItems() != null) {
				for (FormItem formItem : selectionRow.getCustomFieldItems()) {
					if (formItem != null) {
						cmps.add(formItem.getComponent());
					}
				}
			}
			if (selectionRow.getDetailsButton() != null) {
				cmps.add(selectionRow.getDetailsButton().getComponent());
			}
			if (selectionRow.getUnselectButton() != null) {
				cmps.add(selectionRow.getUnselectButton().getComponent());
			}
			if (selectionRow.getSelectButton() != null) {
				cmps.add(selectionRow.getSelectButton().getComponent());
			}
			if (selectionRow.getSelectDropdown() != null) {
				cmps.add(selectionRow.getSelectDropdown().getComponent());
				for (FormItem formItem : selectionRow.getSelectDropdown().getFormItems()) {
					cmps.add(formItem.getComponent());
				}
			}
		}
		return cmps;
	}
	
	@Override
	public void event(Event event) {
		if (event instanceof TBBrokerChangedEvent) {
			reloadBroker();
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == lightboxCtrl) {
			loadModel(true);
			cleanUp();
		} else if (docEditorCtrl == source) {
			cleanUp();
		} else if (selectionToolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		} else if (topicToolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		} else if (toolsCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(detailCtrl);
		removeAsListenerAndDispose(lightboxCtrl);
		removeAsListenerAndDispose(docEditorCtrl);
		removeAsListenerAndDispose(selectionToolsCtrl);
		removeAsListenerAndDispose(topicToolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		detailCtrl = null;
		lightboxCtrl = null;
		docEditorCtrl = null;
		topicToolsCtrl = null;
		selectionToolsCtrl = null;
		toolsCalloutCtrl = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof UpDownEvent ude && source instanceof UpDown upDown) {
			Object userObject = upDown.getUserObject();
			if (userObject instanceof TBSelectionRow row) {
				doMoveSelection(row.getTopic(), ude.getDirection());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == maxEnrollmentsEl) {
			doUpdateParticipant();
			loadModel(true);
		} else if (topicTableEl == source) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				TBSelectionRow row = topicDataModel.getObject(se.getIndex());
				if (CMD_DETAILS.equals(cmd)) {
					doOpenDetails(ureq, row.getTopic(), row.getCustomFields());
				}
			} else if (event instanceof FlexiTableSearchEvent ftse) {
				loadModel(true);
			}
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if (link.getUserObject() instanceof TBSelectionRow row) {
				if (CMD_DETAILS.equals(cmd)) {
					doOpenDetails(ureq, row.getTopic(), row.getCustomFields());
				} else if (CMD_SELECT.equals(cmd)) {
					doSelectTopic(row.getTopic(), null);
				} else if (CMD_SELECT_FIRST.equals(cmd)) {
					doSelectTopic(row.getTopic(), Integer.valueOf(1));
				} else if (CMD_SELECT_LAST.equals(cmd)) {
					doSelectTopic(row.getTopic(), null);
				} else if (cmd.startsWith(CMD_SELECT_POS)) {
					int sortOrder = Integer.valueOf(cmd.substring(CMD_SELECT_POS.length()));
					doSelectTopic(row.getTopic(), sortOrder);
				} else if (CMD_UNSELECT.equals(cmd)) {
					doUnselectTopic(row.getTopic());
				} else if ("selectionTools".equals(cmd)) {
					doOpenSelectionTools(ureq, row, link);
				} else if ("topicTools".equals(cmd)) {
					doOpenTopicTools(ureq, row, link);
				}
			} else if (link.getUserObject() instanceof VFSLeaf topicLeaf) {
				if (CMD_OPEN_FILE.equals(link.getCmd())) {
					doOpenOrDownload(ureq, topicLeaf);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void reloadBroker() {
		broker = topicBrokerService.getBroker(broker);
		periodEvaluator.setBroker(broker);
		loadModel(true);
		updateBrokerStatusUI();
		updateBrokerConfigUI();
		
		if (detailCtrl != null) {
			detailCtrl.setBroker(broker);
		}
	}

	private void refreshPeriodEvaluator() {
		periodEvaluator.refresh();
		
		if (periodEvaluator.isPeriodChanged()) {
			// Reload the broker to get the current status
			broker = topicBrokerService.getBroker(broker);
			periodEvaluator.setBroker(broker);
			updateMaxEnrollmentsEnabledUI();
			updateBrokerStatusUI();
			updateBrokerConfigUI();
		}
	}

	private void doUpdateParticipant() {
		Integer maxEnrollments = null;
		if (maxEnrollmentsEl.isOneSelected() && !maxEnrollmentsEl.getSelectedKey().equals(String.valueOf(broker.getRequiredEnrollments()))) {
			Integer selectedMaxEnrollments = Integer.valueOf(maxEnrollmentsEl.getSelectedKey());
			if (broker.getRequiredEnrollments().intValue() != selectedMaxEnrollments.intValue()) {
				maxEnrollments = selectedMaxEnrollments;
			}
		}
		participant.setRequiredEnrollments(maxEnrollments);
		
		participant = topicBrokerService.updateParticipant(getIdentity(), participant);
	}
	
	private void doSelectTopic(TBTopicRef topic, Integer sortOrder) {
		refreshPeriodEvaluator();
		if (periodEvaluator.isSelectionPeriod()) {
			topicBrokerService.select(getIdentity(), getIdentity(), topic, sortOrder);
		}
		
		loadModel(false);
	}
	
	private void doUnselectTopic(TBTopicRef topic) {
		refreshPeriodEvaluator();
		if (periodEvaluator.isSelectionPeriod()) {
			topicBrokerService.unselect(getIdentity(), getIdentity(), topic);
		} else if (periodEvaluator.isWithdrawPeriod()) {
			topicBrokerService.withdraw(getIdentity(), getIdentity(), topic, false);
		}
		
		loadModel(false);
	}

	private void doMoveSelection(TBTopicRef topic, Direction direction) {
		refreshPeriodEvaluator();
		if (periodEvaluator.isSelectionPeriod()) {
			topicBrokerService.moveSelection(getIdentity(), getIdentity(), topic, Direction.UP == direction);
		}
		
		loadModel(false);
	}
	
	private void doOpenDetails(UserRequest ureq, TBTopic topic, List<TBCustomField> customFields) {
		removeAsListenerAndDispose(detailCtrl);
		
		List<TBGroupRestrictionInfo> groupRestrictions = null;
		if (topic.getGroupRestrictionKeys() != null) {
			groupRestrictions = topicBrokerService.getGroupRestrictionInfos(getTranslator(), topic.getGroupRestrictionKeys());
		}
		detailCtrl = new TBSelectionDetailController(ureq, getWindowControl(), broker, participant, topic, groupRestrictions, customFields);
		listenTo(detailCtrl);
		
		lightboxCtrl = new LightboxController(ureq, getWindowControl(), detailCtrl);
		listenTo(lightboxCtrl);
		lightboxCtrl.activate();
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
	
	private void doOpenSelectionTools(UserRequest ureq, TBSelectionRow row, FormLink link) {
		removeAsListenerAndDispose(selectionToolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		selectionToolsCtrl = new SelectionToolsController(ureq, getWindowControl(), row);
		listenTo(selectionToolsCtrl);
	
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				selectionToolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doOpenTopicTools(UserRequest ureq, TBSelectionRow row, FormLink link) {
		removeAsListenerAndDispose(topicToolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		topicToolsCtrl = new TopicToolsController(ureq, getWindowControl(), row);
		listenTo(topicToolsCtrl);
	
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				topicToolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private class SelectionToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		
		private final TBSelectionRow row;
		private final List<String> names = new ArrayList<>(2);
		
		public SelectionToolsController(UserRequest ureq, WindowControl wControl, TBSelectionRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			putInitialPanel(mainVC); 
			
			if (periodEvaluator.isSelectionPeriod()) {
				if (!row.getUpDown().isTopmost()) {
					addLink("move.up", CMD_UP, "o_icon o_icon-fw o_icon_move_up");
				}
				if (!row.getUpDown().isLowermost()) {
					addLink("move.down", CMD_DOWN, "o_icon o_icon-fw o_icon_move_down");
				}
				if (periodEvaluator.isWithdrawPeriod()) {
					names.add("divider");
				}
			}
			
			if (periodEvaluator.isWithdrawPeriod()) {
				if (periodEvaluator.isSelectionPeriod() || row.isEnrolled()) {
					addLink("withdraw", CMD_UNSELECT, "o_icon o_icon-fw o_icon_tb_withdraw");
				}
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
				if (CMD_UP.equals(cmd)) {
					doMoveSelection(row.getTopic(), Direction.UP);
				} else if (CMD_DOWN.equals(cmd)) {
					doMoveSelection(row.getTopic(), Direction.DOWN);
				} else if (CMD_UNSELECT.equals(cmd)) {
					doUnselectTopic(row.getTopic());
				}
			}
		}
	}
	
	private class TopicToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		
		private final TBSelectionRow row;
		private final List<String> names = new ArrayList<>();
		
		public TopicToolsController(UserRequest ureq, WindowControl wControl, TBSelectionRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			putInitialPanel(mainVC);
			
			if (row.getSelectionRef() == null) {
				if (periodEvaluator.isSelectionPeriod()) {
					addLink("select.pos.first", CMD_SELECT_FIRST, "o_icon o_icon-fw o_icon_tb_select_first");
					addLink("select.pos.last", CMD_SELECT_LAST, "o_icon o_icon-fw o_icon_tb_select_last");
					
					names.add("divider");
					
					for (int i = 1; i <= selectionsSize + 1; i++) {
						String cmd = CMD_SELECT_POS + i;
						String name = "selectp_" + row.getTopic().getKey() + "_" + i;
						Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.NONTRANSLATED);
						link.setCustomDisplayText(translate("select.pos", String.valueOf(i)));
						mainVC.put(name, link);
						names.add(name);
					}
				}
			} else {
				if (periodEvaluator.isWithdrawPeriod()) {
					if (periodEvaluator.isSelectionPeriod() || row.isEnrolled()) {
						addLink("withdraw", CMD_UNSELECT, "o_icon o_icon-fw o_icon_tb_withdraw");
					}
				}
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
				if (CMD_SELECT_FIRST.equals(cmd)) {
					doSelectTopic(row.getTopic(), Integer.valueOf(1));
				} else if (CMD_SELECT_LAST.equals(cmd)) {
					doSelectTopic(row.getTopic(), null);
				} else if (cmd.startsWith(CMD_SELECT_POS)) {
					int sortOrder = Integer.valueOf(cmd.substring(CMD_SELECT_POS.length()));
					doSelectTopic(row.getTopic(), sortOrder);
				} else if (CMD_UNSELECT.equals(cmd)) {
					doUnselectTopic(row.getTopic());
				}
			}
		}
	}
	
	private final class TopicCssDelegate extends DefaultFlexiTableCssDelegate {
		
		@Override
		public String getWrapperCssClass(FlexiTableRendererType type) {
			return null;
		}
		
		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			if (FlexiTableRendererType.custom == type) {
				return "o_tb_topic_table o_block_small_top";
			}
			return null;
		}
		
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_tb_topic_row";
		}
	}

}
