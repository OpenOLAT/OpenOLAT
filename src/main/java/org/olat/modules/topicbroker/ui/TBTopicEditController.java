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

import static org.olat.modules.topicbroker.TopicBrokerService.CUSTOM_FILE_MAX_SIZE_KB;
import static org.olat.modules.topicbroker.TopicBrokerService.TEASER_IMAGE_MAX_SIZE_KB;
import static org.olat.modules.topicbroker.TopicBrokerService.TEASER_IMAGE_MIME_TYPES;
import static org.olat.modules.topicbroker.TopicBrokerService.TEASER_VIDEO_MAX_SIZE_KB;
import static org.olat.modules.topicbroker.TopicBrokerService.TEASER_VIDEO_MIME_TYPES;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.DeleteFileElementEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.RepeatableFieldGroupColumnWidth;
import org.olat.core.gui.components.form.flexible.impl.elements.RepeatableFieldGroupController;
import org.olat.core.gui.components.form.flexible.impl.elements.RepeatableFieldGroupDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.RepeatableFieldGroupRow;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.topicbroker.TBBrokerRef;
import org.olat.modules.topicbroker.TBCustomField;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionSearchParams;
import org.olat.modules.topicbroker.TBCustomFieldSearchParams;
import org.olat.modules.topicbroker.TBCustomFieldType;
import org.olat.modules.topicbroker.TBGroupRestrictionCandidates;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicDate;
import org.olat.modules.topicbroker.TBTopicDateSearchParams;
import org.olat.modules.topicbroker.TBTopicDateValues;
import org.olat.modules.topicbroker.TBTopicExecutionType;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.repository.ui.ExecutionPeriodHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBTopicEditController extends FormBasicController implements RepeatableFieldGroupDelegate<TBTopicEditController.DateRow> {
	
	private static final String PERIOD_NONE = "none";
	private static final String PERIOD_PRIVATE = "private";
	private static final String PERIOD_ONEDAY = "oneday";
	private static final String DETAIL_LOCATION = "location";
	private static final String DETAIL_DESCRIPTION = "description";

	private TextElement identifierEl;
	private TextElement titleEl;
	private TextAreaElement descriptionEl;
	private FormLayoutContainer participantsCont;
	private TextElement minParticipantsEl;
	private TextElement maxParticipantsEl;
	private SingleSelection executionTypeEl;
	private SingleSelection periodTypeEl;
	private DateChooser beginEndEl;
	private FormToggle datesWithinRangeEl;
	private MultipleSelectionElement dateDetailsEl;
	private RepeatableFieldGroupController<DateRow> datesCtrl;
	private MultipleSelectionElement groupRestrictionsEl;
	private FileElement teaserImageEl;
	private FormToggle teaserVideoToggleEl;
	private FileElement teaserVideoEl;
	private List<TextAreaElement> customTextEls;
	private List<FileElement> customFileEls;
	
	private final TBBrokerRef broker;
	private TBTopic topic;
	private final TBGroupRestrictionCandidates groupRestrictionCandidates;
	private int counter = 0;
	
	@Autowired
	private TopicBrokerService topicBrokerService;

	protected TBTopicEditController(UserRequest ureq, WindowControl wControl, TBBrokerRef broker, TBTopic topic,
			TBGroupRestrictionCandidates groupRestrictionCandidates) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.broker = broker;
		this.topic = topic;
		this.groupRestrictionCandidates = groupRestrictionCandidates;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer standardCont = FormLayoutContainer.createVerticalFormLayout("standard", getTranslator());
		standardCont.setFormTitle(translate("topic.fields.standard"));
		standardCont.setRootForm(mainForm);
		standardCont.setElementCssClass("o_sel_tb_standard_fields");
		formLayout.add(standardCont);
		
		String title = topic != null? topic.getTitle(): null;
		titleEl = uifactory.addTextElement("topic.title", TBTopic.TITLE_MAX_LENGTH, title, standardCont);
		titleEl.setElementCssClass("o_sel_tb_title");
		titleEl.setMandatory(true);
		
		String identifier = topic != null? topic.getIdentifier(): null;
		identifierEl = uifactory.addTextElement("topic.identifier", TBTopic.IDENTIFIER_MAX_LENGTH, identifier, standardCont);
		identifierEl.setElementCssClass("o_sel_tb_identifier");
		identifierEl.setMandatory(true);
		
		teaserImageEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "topic.teaser.image", standardCont);
		teaserImageEl.setMaxUploadSizeKB(TEASER_IMAGE_MAX_SIZE_KB, null, null);
		teaserImageEl.limitToMimeType(TEASER_IMAGE_MIME_TYPES, "error.mimetype", new String[]{ TEASER_IMAGE_MIME_TYPES.toString()} );
		teaserImageEl.setReplaceButton(true);
		teaserImageEl.setDeleteEnabled(true);
		teaserImageEl.setPreview(ureq.getUserSession(), true);
		teaserImageEl.addActionListener(FormEvent.ONCHANGE);
		VFSLeaf teaserImage = topicBrokerService.getTopicLeaf(topic, TopicBrokerService.TEASER_IMAGE_DIR);
		if (teaserImage instanceof LocalFileImpl localFile) {
			teaserImageEl.setInitialFile(localFile.getBasefile());
		}
		
		VFSLeaf teaserVideo = topicBrokerService.getTopicLeaf(topic, TopicBrokerService.TEASER_VIDEO_DIR);
		teaserVideoToggleEl = uifactory.addToggleButton("topic.teaser.video.with", "topic.teaser.video.with", translate("on"), translate("off"), standardCont);
		teaserVideoToggleEl.addActionListener(FormEvent.ONCHANGE);
		teaserVideoToggleEl.toggle(teaserVideo != null);
		
		teaserVideoEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "topic.teaser.video", standardCont);
		teaserVideoEl.setMandatory(true, "form.legende.mandatory");
		teaserVideoEl.setMaxUploadSizeKB(TEASER_VIDEO_MAX_SIZE_KB, null, null);
		teaserVideoEl.limitToMimeType(TEASER_VIDEO_MIME_TYPES, "error.mimetype", new String[]{ TEASER_VIDEO_MIME_TYPES.toString()} );
		teaserVideoEl.setReplaceButton(true);
		teaserVideoEl.setDeleteEnabled(true);
		teaserVideoEl.setPreview(ureq.getUserSession(), true);
		teaserVideoEl.addActionListener(FormEvent.ONCHANGE);
		teaserVideoEl.setVisible(teaserVideoToggleEl.isOn());
		if (teaserVideo instanceof LocalFileImpl localFile) {
			teaserVideoEl.setInitialFile(localFile.getBasefile());
		}
		
		String description = topic != null? topic.getDescription(): null;
		descriptionEl = uifactory.addTextAreaElement("topic.description", 4, 6, description, standardCont);
		
		participantsCont = FormLayoutContainer.createCustomFormLayout("participants", getTranslator(), velocity_root + "/topic_participants_edit.html");
		participantsCont.setLabel("topic.participants.num", null);
		participantsCont.setMandatory(true);
		participantsCont.setRootForm(mainForm);
		standardCont.add(participantsCont);
		
		String minParticipants = topic != null && topic.getMinParticipants() != null
				? topic.getMinParticipants().toString()
				: null;
		minParticipantsEl = uifactory.addTextElement("topic.participants.min", null, 20, minParticipants, participantsCont);
		minParticipantsEl.setElementCssClass("o_sel_tb_min_participants");
		minParticipantsEl.setDisplaySize(15);
		minParticipantsEl.setAriaLabel(translate("topic.participants.num") + " " + translate("abbrev.min"));
		
		String maxParticipants = topic != null && topic.getMaxParticipants() != null
				? topic.getMaxParticipants().toString()
				: null;
		maxParticipantsEl = uifactory.addTextElement("topic.participants.max", null, 320, maxParticipants, participantsCont);
		maxParticipantsEl.setElementCssClass("o_sel_tb_max_participants");
		maxParticipantsEl.setDisplaySize(15);
		maxParticipantsEl.setAriaLabel(translate("topic.participants.num") + " " + translate("abbrev.max"));
		
		initExecution(formLayout, ureq);
		
		FormLayoutContainer restrictionsCont = FormLayoutContainer.createVerticalFormLayout("restrictions", getTranslator());
		restrictionsCont.setFormTitle(translate("topic.fields.restrictions"));
		restrictionsCont.setRootForm(mainForm);
		formLayout.add(restrictionsCont);
		
		Set<Long> businessGroupKeys = new HashSet<>();
		if (groupRestrictionCandidates.getBusinessGroupKeys() != null) {
			businessGroupKeys.addAll(groupRestrictionCandidates.getBusinessGroupKeys());
		}
		if (topic != null && topic.getGroupRestrictionKeys() != null) {
			businessGroupKeys.addAll(topic.getGroupRestrictionKeys());
		}
		if (!businessGroupKeys.isEmpty()) {
			SelectionValues businessGroupSV = new SelectionValues();
			topicBrokerService.getGroupRestrictionInfos(getTranslator(), businessGroupKeys).stream()
					.sorted((i1, i2) -> i1.getGroupName().compareToIgnoreCase(i2.getGroupName()))
					.forEach(info -> businessGroupSV.add(
							SelectionValues.entry(info.getGroupKey().toString(),
							info.getGroupName())));
			
			groupRestrictionsEl = uifactory.addCheckboxesDropdown("topic.group.restriction",
					"topic.group.restriction", restrictionsCont, businessGroupSV.keys(), businessGroupSV.values());
			groupRestrictionsEl.setHelpTextKey("topic.group.restriction.help", null);
			if (topic != null && topic.getGroupRestrictionKeys() != null) {
				for (Long groupKey : topic.getGroupRestrictionKeys()) {
					String key = groupKey.toString();
					if (groupRestrictionsEl.getKeys().contains(key)) {
						groupRestrictionsEl.select(key, true);
					}
				}
			}
		} else {
			StaticTextElement groupRestrictionNoGroupsEl = uifactory.addStaticTextElement("topic.group.restriction",
					"topic.group.restriction", translate("topic.group.restriction.no.candidates"), restrictionsCont);
			groupRestrictionNoGroupsEl.setHelpTextKey("topic.group.restriction.help", null);
		}
		
		initCustomFields(formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	private void initExecution(FormItemContainer formLayout, UserRequest ureq) {
		FormLayoutContainer executionCont = FormLayoutContainer.createVerticalFormLayout("execution", getTranslator());
		executionCont.setFormTitle(translate("topic.fields.execution"));
		executionCont.setRootForm(mainForm);
		formLayout.add(executionCont);
		
		SelectionValues executionTypeSV = new SelectionValues();
		executionTypeSV.add(SelectionValues.entry(TBTopicExecutionType.period.name(), translate("topic.execution.type.period"),
				translate("topic.execution.type.period.desc"), null, null, true));
		executionTypeSV.add(SelectionValues.entry(TBTopicExecutionType.dates.name(), translate("topic.execution.type.dates"),
				translate("topic.execution.type.dates.desc"), null, null, true));
		executionTypeEl = uifactory.addCardSingleSelectHorizontal("topic.execution.type", "topic.execution.type", executionCont, executionTypeSV);
		executionTypeEl.setHelpTextKey("topic.execution.type.help", null);
		executionTypeEl.setHelpUrlForManualPage("manual_user/learningresources/Course_Element_Topic_Broker/");
		executionTypeEl.addActionListener(FormEvent.ONCHANGE);
		TBTopicExecutionType executionType = topic != null && topic.getExecutionType() != null
				? topic.getExecutionType()
				: TBTopicExecutionType.period;
		executionTypeEl.select(executionType.name(), true);
		
		Date beginDate = topic != null? topic.getBeginDate(): null;
		Date endDate = topic != null? topic.getEndDate(): null;
		SelectionValues periodTypeSV = new SelectionValues();
		periodTypeSV.add(SelectionValues.entry(PERIOD_NONE, translate("topic.execution.period.none")));
		periodTypeSV.add(SelectionValues.entry(PERIOD_PRIVATE, translate("topic.execution.period.range")));
		periodTypeSV.add(SelectionValues.entry(PERIOD_ONEDAY, translate("topic.execution.period.oneday")));
		periodTypeEl = uifactory.addRadiosHorizontal("topic.execution.period", executionCont, periodTypeSV.keys(), periodTypeSV.values());
		periodTypeEl.addActionListener(FormEvent.ONCHANGE);
		periodTypeEl.select(ExecutionPeriodHelper.getDatesType(beginDate, endDate), true);
		
		beginEndEl = uifactory.addDateChooser("begin.end", "topic.execution.date.range", null, executionCont);
		beginEndEl.setMandatory(true);
		beginEndEl.setSecondDate(true);
		beginEndEl.setSeparator("to.separator");
		beginEndEl.setAriaLabel(translate("topic.execution.date.start.aria"));
		beginEndEl.setSecondAriaLabel(translate("topic.execution.date.end.aria"));
		beginEndEl.setDate(beginDate);
		beginEndEl.setSecondDate(endDate);
		beginEndEl.addActionListener(FormEvent.ONCHANGE);
		
		List<TBTopicDate> topicDates = List.of();
		if (topic != null) {
			TBTopicDateSearchParams searchParams = new TBTopicDateSearchParams();
			searchParams.setTopic(topic);
			topicDates = topicBrokerService.getTopicDates(searchParams);
		}
		
		datesWithinRangeEl = uifactory.addToggleButton("topic.execution.dates.within.range", "topic.execution.dates.within.range",
				translate("on"), translate("off"), executionCont);
		datesWithinRangeEl.setHelpTextKey("topic.execution.dates.within.range.help", null);
		datesWithinRangeEl.addActionListener(FormEvent.ONCHANGE);
		datesWithinRangeEl.toggle(TBTopicExecutionType.period == executionType && !topicDates.isEmpty());
		
		SelectionValues dateDetailsSV = new SelectionValues();
		dateDetailsSV.add(SelectionValues.entry(DETAIL_LOCATION, translate("topic.date.location")));
		dateDetailsSV.add(SelectionValues.entry(DETAIL_DESCRIPTION, translate("topic.date.description")));
		dateDetailsEl = uifactory.addCheckboxesHorizontal("topic.date.details", executionCont, dateDetailsSV.keys(), dateDetailsSV.values());
		dateDetailsEl.addActionListener(FormEvent.ONCHANGE);
		dateDetailsEl.select(DETAIL_LOCATION, topicDates.stream().anyMatch(date -> StringHelper.containsNonWhitespace(date.getLocation())));
		dateDetailsEl.select(DETAIL_DESCRIPTION, topicDates.stream().anyMatch(date -> StringHelper.containsNonWhitespace(date.getDescription())));
		
		datesCtrl = new RepeatableFieldGroupController<>(ureq, getWindowControl(), mainForm, getTranslator(),
				translate("topic.date"), translate("topic.date.add"), this);
		listenTo(datesCtrl);
		executionCont.add("dates", datesCtrl.getInitialFormItem());
		for (TBTopicDate topicDate : topicDates) {
			DateRow row = datesCtrl.addRow();
			row.setKey(topicDate.getKey());
			row.getDateEl().setDate(topicDate.getStartDate());
			row.getStartEl().setDate(topicDate.getStartDate());
			row.getEndEl().setDate(topicDate.getEndDate());
			row.getLocationEl().setValue(topicDate.getLocation());
			row.getDescriptionEl().setValue(topicDate.getDescription());
		}
		if (topicDates.isEmpty()) {
			datesCtrl.addRow();
		}
		
		updateExecutionUI();
	}
	
	@Override
	public DateRow createRow(String id, FormItemContainer container) {
		DateChooser dateEl = uifactory.addDateChooser(id + "_date", "topic.date.date", null, container);
		dateEl.setMandatory(true);
		dateEl.addActionListener(FormEvent.ONCHANGE);
		Date today = DateUtils.getStartOfDay(new Date());
		DateChooser startEl = uifactory.addDateChooser(id + "_start", "topic.date.start", today, container);
		startEl.setMandatory(true);
		startEl.setTimeOnly(true);
		startEl.addActionListener(FormEvent.ONCHANGE);
		DateChooser endEl = uifactory.addDateChooser(id + "_end", "topic.date.end", today, container);
		endEl.setMandatory(true);
		endEl.setTimeOnly(true);
		TextElement locationEl = uifactory.addTextElement(id + "_location", "topic.date.location", TBTopicDate.LOCATION_MAX_LENGTH, null, container);
		TextElement descriptionEl = uifactory.addTextAreaElement(id + "_description", "topic.date.description", TBTopicDate.DESCRIPTION_MAX_LENGTH, 2, -1, true, false, null, container);
		DateRow row = new DateRow(dateEl, startEl, endEl, locationEl, descriptionEl);
		dateEl.setUserObject(row);
		startEl.setUserObject(row);
		updateRowUI(row);
		return row;
	}
	
	@Override
	public void copyRow(DateRow source, DateRow target) {
		target.getDateEl().setDate(source.getDateEl().getDate());
		target.getStartEl().setDate(source.getStartEl().getDate());
		target.getEndEl().setDate(source.getEndEl().getDate());
		target.getLocationEl().setValue(source.getLocationEl().getValue());
		target.getDescriptionEl().setValue(source.getDescriptionEl().getValue());
	}
	
	@Override
	public String getColumnCssClass(int column) {
		return switch (column) {
			case 0, 1, 2 -> RepeatableFieldGroupColumnWidth.content.getCssClass();
			case 4 -> RepeatableFieldGroupColumnWidth.full.getCssClass();
			default -> RepeatableFieldGroupDelegate.super.getColumnCssClass(column);
		};
	}
	
	private boolean isPeriodType() {
		return executionTypeEl.isOneSelected() && TBTopicExecutionType.period.name().equals(executionTypeEl.getSelectedKey());
	}
	
	private String getPeriodType() {
		return periodTypeEl.isOneSelected()? periodTypeEl.getSelectedKey(): PERIOD_NONE;
	}
	
	private boolean isOneDay() {
		return isPeriodType() && PERIOD_ONEDAY.equals(getPeriodType());
	}
	
	private boolean isWithBeginEnd() {
		return isPeriodType() && !PERIOD_NONE.equals(getPeriodType());
	}
	
	private boolean isDatesVisible() {
		return !isPeriodType() || (isWithBeginEnd() && datesWithinRangeEl.isOn());
	}
	
	private void updateExecutionUI() {
		boolean withBeginEnd = isWithBeginEnd();
		boolean oneDay = isOneDay();
		periodTypeEl.setVisible(isPeriodType());
		beginEndEl.setVisible(withBeginEnd);
		beginEndEl.setSecondDate(!oneDay);
		beginEndEl.setLabel(oneDay? "topic.execution.period.date": "topic.execution.date.range", null);
		beginEndEl.setAriaLabel(oneDay? null: translate("topic.execution.date.start.aria"));
		beginEndEl.setSecondAriaLabel(oneDay? null: translate("topic.execution.date.end.aria"));
		datesWithinRangeEl.setVisible(withBeginEnd);
		
		boolean datesVisible = isDatesVisible();
		dateDetailsEl.setVisible(datesVisible);
		datesCtrl.getInitialFormItem().setVisible(datesVisible);
		datesCtrl.getRows().forEach(this::updateRowUI);
		if (!datesVisible) {
			for (DateRow row : datesCtrl.getRows()) {
				row.getFormItems().forEach(FormItem::clearError);
				updatePastWarning(row);
			}
		}
	}
	
	private void updateRowUI(DateRow row) {
		row.getDateEl().setVisible(!isOneDay());
		row.getLocationEl().setVisible(dateDetailsEl.isKeySelected(DETAIL_LOCATION));
		row.getDescriptionEl().setVisible(dateDetailsEl.isKeySelected(DETAIL_DESCRIPTION));
	}
	
	private void initCustomFields(FormItemContainer formLayout) {
		TBCustomFieldDefinitionSearchParams definitionSearchParams = new TBCustomFieldDefinitionSearchParams();
		definitionSearchParams.setBroker(broker);
		List<TBCustomFieldDefinition> definitions = topicBrokerService.getCustomFieldDefinitions(definitionSearchParams);
		if (definitions.isEmpty()) {
			return;
		}
		
		FormLayoutContainer customCont = FormLayoutContainer.createVerticalFormLayout("custom", getTranslator());
		customCont.setFormTitle(translate("topic.fields.custom"));
		customCont.setRootForm(mainForm);
		formLayout.add(customCont);
		
		Map<Long, TBCustomField> definitionKeyToCustomFields = Map.of();
		if (topic != null) {
			TBCustomFieldSearchParams searchParams = new TBCustomFieldSearchParams();
			searchParams.setTopic(topic);
			definitionKeyToCustomFields = topicBrokerService.getCustomFields(searchParams).stream()
					.collect(Collectors.toMap(customField -> customField.getDefinition().getKey(), Function.identity()));
		}
		
		definitions.sort((d1, d2) -> Integer.compare(d1.getSortOrder(), d2.getSortOrder()));
		for (TBCustomFieldDefinition definition : definitions) {
			TBCustomField customField = definitionKeyToCustomFields.get(definition.getKey());
			if (TBCustomFieldType.text == definition.getType()) {
				initCustomTextField(customCont, definition, customField);
			} else if (TBCustomFieldType.file == definition.getType()) {
				initCustomFileField(customCont, definition);
			}
		}
	}

	private void initCustomTextField(FormLayoutContainer customCont, TBCustomFieldDefinition definition, TBCustomField customField) {
		if (customTextEls == null) {
			customTextEls = new ArrayList<>(1);
		}
		
		String text = customField != null? customField.getText(): null;
		TextAreaElement textEl = uifactory.addTextAreaElement("text_" + counter++, null, 2000, 2, 72, true, false, true, text, customCont);
		textEl.setLabel("noTransOnlyParam", new String[] {StringHelper.escapeHtml(definition.getName())});
		textEl.setUserObject(definition);
		customTextEls.add(textEl);
	}

	private void initCustomFileField(FormLayoutContainer customCont, TBCustomFieldDefinition definition) {
		if (customFileEls == null) {
			customFileEls = new ArrayList<>(1);
		}
		
		VFSLeaf topicLeaf = topicBrokerService.getTopicLeaf(topic, definition.getIdentifier());
		FileElement fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "file_" + counter++, null, customCont);
		fileEl.setMaxUploadSizeKB(CUSTOM_FILE_MAX_SIZE_KB, null, null);
		fileEl.setReplaceButton(true);
		fileEl.setDeleteEnabled(true);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		fileEl.setLabel("noTransOnlyParam", new String[] {StringHelper.escapeHtml(definition.getName())});
		if (topicLeaf instanceof LocalFileImpl localFile) {
			fileEl.setInitialFile(localFile.getBasefile());
		}
		fileEl.setUserObject(definition);
		customFileEls.add(fileEl);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == teaserVideoToggleEl) {
			teaserVideoEl.setVisible(teaserVideoToggleEl.isOn());
		} else if (source == executionTypeEl) {
			if (isPeriodType() && datesCtrl.getRows().stream().anyMatch(row -> row.getDateEl().getDate() != null)) {
				datesWithinRangeEl.toggle(true);
			}
			updateExecutionUI();
		} else if (source == periodTypeEl || source == datesWithinRangeEl || source == dateDetailsEl) {
			updateExecutionUI();
		} else if (source == beginEndEl) {
			datesCtrl.getRows().forEach(this::updatePastWarning);
		} else if (source instanceof DateChooser dateChooser && dateChooser.getUserObject() instanceof DateRow row) {
			updatePastWarning(row);
		} else if (source instanceof FileElement fileEl) {
			if (DeleteFileElementEvent.DELETE.equals(event.getCommand())) {
				fileEl.setInitialFile(null);
				if (fileEl.getUploadFile() != null) {
					fileEl.reset();
				}
				fileEl.clearError();
				markDirty();
			} else if (fileEl.isUploadSuccess()) {
				fileEl.clearError();
				markDirty();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		identifierEl.clearError();
		if (!StringHelper.containsNonWhitespace(identifierEl.getValue())) {
			identifierEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if (identifierEl.getValue().length() > TBTopic.IDENTIFIER_MAX_LENGTH) {
			identifierEl.setErrorKey("form.error.toolong", String.valueOf(TBTopic.IDENTIFIER_MAX_LENGTH));
			allOk &= false;
		} else if ((topic == null || !topic.getIdentifier().equalsIgnoreCase(identifierEl.getValue()))
				&& !topicBrokerService.isTopicIdentifierAvailable(broker, identifierEl.getValue())) {
			identifierEl.setErrorKey("error.identifier.not.available");
			allOk &= false;
		}
		
		titleEl.clearError();
		if (!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if (titleEl.getValue().length() > TBTopic.TITLE_MAX_LENGTH) {
			titleEl.setErrorKey("form.error.toolong", String.valueOf(TBTopic.TITLE_MAX_LENGTH));
			allOk &= false;
		}
		
		allOk &= teaserImageEl.validate();
		if (teaserVideoEl.isVisible()) {
			allOk &= teaserVideoEl.validate();
		}
		
		boolean minParticipantsOk = true;
		participantsCont.clearError();
		if (StringHelper.containsNonWhitespace(minParticipantsEl.getValue())) {
			try {
				int minParticipants = Integer.parseInt(minParticipantsEl.getValue());
				if (minParticipants < 0) {
					participantsCont.setErrorKey("form.error.positive.integer");
					allOk &= false;
					minParticipantsOk &= false;
				}
			} catch (NumberFormatException e) {
				participantsCont.setErrorKey("form.error.positive.integer");
				allOk &= false;
				minParticipantsOk &= false;
			}
		} else {
			participantsCont.setErrorKey("form.legende.mandatory");
			allOk &= false;
			minParticipantsOk &= false;
		}
		if (StringHelper.containsNonWhitespace(maxParticipantsEl.getValue())) {
			try {
				int maxParticipants = Integer.parseInt(maxParticipantsEl.getValue());
				if (maxParticipants < 1) {
					participantsCont.setErrorKey("form.error.positive.integer");
					allOk &= false;
				} else if (minParticipantsOk && Integer.parseInt(minParticipantsEl.getValue()) > Integer.parseInt(maxParticipantsEl.getValue())) {
					participantsCont.setErrorKey("error.participants.min.greater.max");
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				participantsCont.setErrorKey("form.error.positive.integer");
				allOk &= false;
			}
		} else {
			participantsCont.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		beginEndEl.clearError();
		if (beginEndEl.isVisible()) {
			if (isOneDay()) {
				if (beginEndEl.getDate() == null) {
					beginEndEl.setErrorKey("form.legende.mandatory");
					allOk &= false;
				}
			} else if (beginEndEl.getDate() == null && beginEndEl.getSecondDate() == null) {
				beginEndEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if (beginEndEl.getDate() != null && beginEndEl.getSecondDate() != null && beginEndEl.getDate().after(beginEndEl.getSecondDate())) {
				beginEndEl.setErrorKey("form.error.first.after.second.date");
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	@Override
	public boolean validateRow(DateRow row) {
		updatePastWarning(row);
		if (!isDatesVisible()) {
			return true;
		}
		
		boolean allOk = true;
		if (row.getDateEl().isVisible() && !row.getDateEl().hasError() && row.getDateEl().getDate() == null) {
			row.getDateEl().setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		if (!row.getStartEl().hasError() && row.getStartEl().getDate() == null) {
			row.getStartEl().setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		if (!row.getEndEl().hasError() && row.getEndEl().getDate() == null) {
			row.getEndEl().setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		Date day = getRowDay(row);
		if (allOk && day != null) {
			Date start = getRowStart(row);
			Date end = getRowEnd(row);
			if (DateUtils.isSameMinute(start, end) && DateUtils.isSameMinute(start, DateUtils.getStartOfDay(start))) {
				row.getStartEl().setErrorKey("form.legende.mandatory");
				row.getEndEl().setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if (!end.after(start)) {
				row.getEndEl().setErrorKey("error.date.end.before.start");
				allOk &= false;
			}
			if (isPeriodType() && !isOneDay()
					&& ((beginEndEl.getDate() != null && day.before(DateUtils.getStartOfDay(beginEndEl.getDate())))
						|| (beginEndEl.getSecondDate() != null && day.after(DateUtils.getEndOfDay(beginEndEl.getSecondDate()))))) {
				row.getDateEl().setErrorKey("error.date.not.in.range");
				allOk &= false;
			}
		}
		return allOk;
	}
	
	private void updatePastWarning(DateRow row) {
		row.getDateEl().clearWarning();
		row.getStartEl().clearWarning();
		if (isDatesVisible() && getRowDay(row) != null && row.getStartEl().getDate() != null && getRowStart(row).before(new Date())) {
			FormItem warningEl = row.getDateEl().isVisible()? row.getDateEl(): row.getStartEl();
			warningEl.setWarningKey("warning.date.in.past");
		}
	}
	
	private Date getRowDay(DateRow row) {
		return isOneDay()? beginEndEl.getDate(): row.getDateEl().getDate();
	}
	
	private Date getRowStart(DateRow row) {
		return DateUtils.copyTime(getRowDay(row), row.getStartEl().getDate());
	}
	
	private Date getRowEnd(DateRow row) {
		return DateUtils.copyTime(getRowDay(row), row.getEndEl().getDate());
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (topic == null) {
			topic = topicBrokerService.createTopic(getIdentity(), broker);
		}
		if (topic == null) {
			showError("error.topic.create");
			return;
		}
		
		Set<Long> groupRestrictionKeys = null;
		if (groupRestrictionsEl != null && !groupRestrictionsEl.getSelectedKeys().isEmpty()) {
			groupRestrictionKeys = groupRestrictionsEl.getSelectedKeys().stream().map(Long::valueOf).collect(Collectors.toSet());
		}
		topic.setGroupRestrictionKeys(groupRestrictionKeys);
		TBTopicExecutionType executionType = isPeriodType()? TBTopicExecutionType.period: TBTopicExecutionType.dates;
		Date beginDate = null;
		Date endDate = null;
		if (beginEndEl.isVisible()) {
			beginDate = beginEndEl.getDate();
			endDate = isOneDay()? beginEndEl.getDate(): beginEndEl.getSecondDate();
		}
		topic = topicBrokerService.updateTopic(getIdentity(), topic, identifierEl.getValue(), titleEl.getValue(),
				descriptionEl.getValue(), executionType, beginDate, endDate,
				Integer.parseInt(minParticipantsEl.getValue()), Integer.parseInt(maxParticipantsEl.getValue()),
				groupRestrictionKeys);
		
		List<TBTopicDateValues> dateValues = new ArrayList<>();
		if (isDatesVisible()) {
			boolean withLocation = dateDetailsEl.isKeySelected(DETAIL_LOCATION);
			boolean withDescription = dateDetailsEl.isKeySelected(DETAIL_DESCRIPTION);
			for (DateRow row : datesCtrl.getRows()) {
				dateValues.add(new TBTopicDateValues(row.getKey(), getRowStart(row), getRowEnd(row),
						withLocation? row.getLocationEl().getValue(): null,
						withDescription? row.getDescriptionEl().getValue(): null));
			}
		}
		topicBrokerService.updateTopicDates(getIdentity(), topic, dateValues);
		
		if (teaserImageEl.getUploadFile() != null) {
			topicBrokerService.storeTopicLeaf(getIdentity(), topic, TopicBrokerService.TEASER_IMAGE_DIR, teaserImageEl.getUploadFile(), teaserImageEl.getUploadFileName());
		} else if (teaserImageEl.getInitialFile() == null) {
			topicBrokerService.deleteTopicLeaf(getIdentity(), topic, TopicBrokerService.TEASER_IMAGE_DIR);
		}
		
		if (!teaserVideoToggleEl.isOn()) {
			topicBrokerService.deleteTopicLeaf(getIdentity(), topic, TopicBrokerService.TEASER_VIDEO_DIR);
		} else if (teaserVideoEl.getUploadFile() != null) {
			topicBrokerService.storeTopicLeaf(getIdentity(), topic, TopicBrokerService.TEASER_VIDEO_DIR, teaserVideoEl.getUploadFile(), teaserVideoEl.getUploadFileName());
		} else if (teaserVideoEl.getInitialFile() == null) {
			topicBrokerService.deleteTopicLeaf(getIdentity(), topic, TopicBrokerService.TEASER_VIDEO_DIR);
		}
		
		if (customTextEls != null) {
			for (TextAreaElement textEl : customTextEls) {
				if (textEl.getUserObject() instanceof TBCustomFieldDefinition definition) {
					String text = textEl.getValue();
					if (StringHelper.containsNonWhitespace(text)) {
						topicBrokerService.createOrUpdateCustomField(getIdentity(), definition, topic, text);
					} else {
						topicBrokerService.deleteCustomFieldPermanently(getIdentity(), definition, topic);
					}
				}
			}
		}
		
		if (customFileEls != null) {
			for (FileElement fileElement : customFileEls) {
				if (fileElement.getUserObject() instanceof TBCustomFieldDefinition definition) {
					if (fileElement.getUploadFile() != null) {
						topicBrokerService.createOrUpdateCustomFieldFile(getIdentity(), topic, definition, fileElement.getUploadFile(), fileElement.getUploadFileName());
					} else if (fileElement.getInitialFile() == null) {
						topicBrokerService.deleteCustomFieldFilePermanently(getIdentity(), definition, topic);
					}
				}
			}
		}
		
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

	public static class DateRow implements RepeatableFieldGroupRow {
		
		private Long key;
		private final DateChooser dateEl;
		private final DateChooser startEl;
		private final DateChooser endEl;
		private final TextElement locationEl;
		private final TextElement descriptionEl;
		
		private DateRow(DateChooser dateEl, DateChooser startEl, DateChooser endEl, TextElement locationEl, TextElement descriptionEl) {
			this.dateEl = dateEl;
			this.startEl = startEl;
			this.endEl = endEl;
			this.locationEl = locationEl;
			this.descriptionEl = descriptionEl;
		}
		
		public Long getKey() {
			return key;
		}
		
		public void setKey(Long key) {
			this.key = key;
		}
		
		public DateChooser getDateEl() {
			return dateEl;
		}
		
		public DateChooser getStartEl() {
			return startEl;
		}
		
		public DateChooser getEndEl() {
			return endEl;
		}
		
		public TextElement getLocationEl() {
			return locationEl;
		}
		
		public TextElement getDescriptionEl() {
			return descriptionEl;
		}
		
		@Override
		public List<FormItem> getFormItems() {
			return List.of(dateEl, startEl, endEl, locationEl, descriptionEl);
		}
	}
	
}
