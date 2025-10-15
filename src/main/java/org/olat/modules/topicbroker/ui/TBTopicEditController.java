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
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.DeleteFileElementEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
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
import org.olat.modules.topicbroker.TopicBrokerService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBTopicEditController extends FormBasicController {

	private TextElement identifierEl;
	private TextElement titleEl;
	private TextAreaElement descriptionEl;
	private FormLayoutContainer participantsCont;
	private TextElement minParticipantsEl;
	private TextElement maxParticipantsEl;
	private DateChooser beginEndEl;
	private MultipleSelectionElement groupRestrictionsEl;
	private FileElement teaserImageEl;
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
		
		String identifier = topic != null? topic.getIdentifier(): null;
		identifierEl = uifactory.addTextElement("topic.identifier", TBTopic.IDENTIFIER_MAX_LENGTH, identifier, standardCont);
		identifierEl.setElementCssClass("o_sel_tb_identifier");
		identifierEl.setMandatory(true);
		identifierEl.setFocus(true);
		
		String title = topic != null? topic.getTitle(): null;
		titleEl = uifactory.addTextElement("topic.title", TBTopic.TITLE_MAX_LENGTH, title, standardCont);
		titleEl.setElementCssClass("o_sel_tb_title");
		titleEl.setMandatory(true);
		
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
		
		String maxParticipants = topic != null && topic.getMaxParticipants() != null
				? topic.getMaxParticipants().toString()
				: null;
		maxParticipantsEl = uifactory.addTextElement("topic.participants.max", null, 320, maxParticipants, participantsCont);
		maxParticipantsEl.setElementCssClass("o_sel_tb_max_participants");
		maxParticipantsEl.setDisplaySize(15);
		
		beginEndEl = uifactory.addDateChooser("topic.execution.period", null, standardCont);
		beginEndEl.setSecondDate(true);
		beginEndEl.setSeparator("to.separator");
		if (topic != null) {
			beginEndEl.setDate(topic.getBeginDate());
			beginEndEl.setSecondDate(topic.getEndDate());
		}
		
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
					"topic.group.restriction", standardCont, businessGroupSV.keys(), businessGroupSV.values());
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
					"topic.group.restriction", translate("topic.group.restriction.no.candidates"), standardCont);
			groupRestrictionNoGroupsEl.setHelpTextKey("topic.group.restriction.help", null);
		}
		
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
		
		teaserVideoEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "topic.teaser.video", standardCont);
		teaserVideoEl.setMaxUploadSizeKB(TEASER_VIDEO_MAX_SIZE_KB, null, null);
		teaserVideoEl.limitToMimeType(TEASER_VIDEO_MIME_TYPES, "error.mimetype", new String[]{ TEASER_VIDEO_MIME_TYPES.toString()} );
		teaserVideoEl.setReplaceButton(true);
		teaserVideoEl.setDeleteEnabled(true);
		teaserVideoEl.setPreview(ureq.getUserSession(), true);
		teaserVideoEl.addActionListener(FormEvent.ONCHANGE);
		VFSLeaf teaserVideo = topicBrokerService.getTopicLeaf(topic, TopicBrokerService.TEASER_VIDEO_DIR);
		if (teaserVideo instanceof LocalFileImpl localFile) {
			teaserVideoEl.setInitialFile(localFile.getBasefile());
		}
		
		initCustomFields(formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
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
		if (source instanceof FileElement fileEl) {
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
		} else if (identifierEl.getValue().length() > TBTopic.TITLE_MAX_LENGTH) {
			identifierEl.setErrorKey("form.error.toolong", String.valueOf(TBTopic.TITLE_MAX_LENGTH));
			allOk &= false;
		}
		
		teaserImageEl.validate();
		teaserVideoEl.validate();
		
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
		
		return allOk;
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
		topic = topicBrokerService.updateTopic(getIdentity(), topic, identifierEl.getValue(), titleEl.getValue(),
				descriptionEl.getValue(), beginEndEl.getDate(), beginEndEl.getSecondDate(),
				Integer.parseInt(minParticipantsEl.getValue()), Integer.parseInt(maxParticipantsEl.getValue()),
				groupRestrictionKeys);
		
		if (teaserImageEl.getUploadFile() != null) {
			topicBrokerService.storeTopicLeaf(getIdentity(), topic, TopicBrokerService.TEASER_IMAGE_DIR, teaserImageEl.getUploadFile(), teaserImageEl.getUploadFileName());
		} else if (teaserImageEl.getInitialFile() == null) {
			topicBrokerService.deleteTopicLeaf(getIdentity(), topic, TopicBrokerService.TEASER_IMAGE_DIR);
		}
		
		if (teaserVideoEl.getUploadFile() != null) {
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

}
