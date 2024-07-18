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
package org.olat.modules.topicbroker.ui.wizard;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionSearchParams;
import org.olat.modules.topicbroker.TBCustomFieldType;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.model.TBImportTopic;
import org.olat.modules.topicbroker.model.TBTransientTopic;
import org.olat.modules.topicbroker.ui.TBTopicDataModel;
import org.olat.modules.topicbroker.ui.TBUIFactory;
import org.olat.modules.topicbroker.ui.wizard.ImportOverviewController.TBTopicImportDataModel.TopicImportCols;
import org.springframework.beans.factory.annotation.Autowired;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

/**
 * 
 * Initial date: 17 Jul 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ImportOverviewController extends StepFormBasicController {
	
	private TBTopicImportDataModel dataModel;
	private FlexiTableElement tableEl;
	
	private final ImportContext importContext;
	private final List<TBCustomFieldDefinition> definitions;

	@Autowired
	private TopicBrokerService topicBrokerService;
	@Autowired
	private BusinessGroupService businessGroupService;

	public ImportOverviewController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		setTranslator(Util.createPackageTranslator(TBUIFactory.class, getLocale(), getTranslator()));
		
		importContext = (ImportContext)getFromRunContext("importContext");
		TBCustomFieldDefinitionSearchParams definitionSearchParams = new TBCustomFieldDefinitionSearchParams();
		definitionSearchParams.setBroker(importContext.getBroker());
		definitions = topicBrokerService.getCustomFieldDefinitions(definitionSearchParams).stream()
				.filter(definition -> TBCustomFieldType.text == definition.getType())
				.sorted((d1, d2) -> Integer.compare(d1.getSortOrder(), d2.getSortOrder()))
				.toList();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("import.overview.title");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicImportCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicImportCols.title));
		
		DefaultFlexiColumnModel minParticipantsColumn = new DefaultFlexiColumnModel(TopicImportCols.minParticipants);
		minParticipantsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		minParticipantsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(minParticipantsColumn);
		
		DefaultFlexiColumnModel maxParticipantsColumn = new DefaultFlexiColumnModel(TopicImportCols.maxParticipants);
		maxParticipantsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		maxParticipantsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(maxParticipantsColumn);
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicImportCols.groupRestrictions));
		
		int columnIndex = TBTopicDataModel.CUSTOM_FIELD_OFFSET;
		for (TBCustomFieldDefinition customFieldDefinition : definitions) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(null, columnIndex++);
			columnModel.setHeaderLabel(StringHelper.escapeHtml(customFieldDefinition.getName()));
			columnsModel.addFlexiColumnModel(columnModel);
		}
		
		DefaultFlexiColumnModel messageColumn = new DefaultFlexiColumnModel(TopicImportCols.importMessage, new TextFlexiCellRenderer(EscapeMode.none));
		messageColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(messageColumn);
		
		dataModel = new TBTopicImportDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		
		
		List<TBImportTopic> topics = getTopics();
		importContext.setTopics(topics);
		dataModel.setObjects(topics);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private List<TBImportTopic> getTopics() {
		List<String[]> lines = getLines(importContext.getInput());
		
		Set<String> identifiersInLines = new HashSet<>(lines.size());
		List<TBImportTopic> importTopics = lines.stream()
				.map(line -> toTopic(line, identifiersInLines))
				.filter(Objects::nonNull)
				.toList();
		
		Set<Long> allGroupRestrictionKeys = importTopics.stream()
				.map(TBImportTopic::getTopic)
				.map(TBTopic::getGroupRestrictionKeys)
				.filter(Objects::nonNull)
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
		Map<Long, String> groupKeyToName = businessGroupService
				.loadShortBusinessGroups(allGroupRestrictionKeys)
				.stream()
				.collect(Collectors.toMap(BusinessGroupShort::getKey, BusinessGroupShort::getName));
		
		for (TBImportTopic importTopic : importTopics) {
			Set<Long> restrictionKeys = importTopic.getTopic().getGroupRestrictionKeys();
			if (restrictionKeys != null && restrictionKeys.isEmpty()) {
				String groupNames = restrictionKeys.stream()
						.map(key -> groupKeyToName.get(key))
						.filter(Objects::nonNull)
						.sorted()
						.map(name -> "<i class=\"o_icon o o_icon-fw o_icon_group\"></i> " + StringHelper.escapeHtml(name))
						.collect(Collectors.joining("&nbsp;&nbsp;"));
				importTopic.setGroupRestrictions(groupNames);
			}
		}
		
		return importTopics;
	}

	private TBImportTopic toTopic(String[] line, Set<String> identifiersInLines) {
		TBTransientTopic topic = new TBTransientTopic();
		TBImportTopic importTopic = new TBImportTopic();
		importTopic.setTopic(topic);
		
		// Last to first, so that the error of the most left column in the row is displayed.
		try {
			if (!definitions.isEmpty()) {
				List<String> customFieldValues = new ArrayList<>(definitions.size());
				Map<TBCustomFieldDefinition, String> definitionToValue = new HashMap<>(definitions.size());
				for (int i = 0; i < definitions.size(); i++) {
					TBCustomFieldDefinition definition = definitions.get(i);
					int column = 6 + i;
					String customFieldValue = line.length > column? line[column]: null;
					if (customFieldValue != null && customFieldValue.isBlank()) {
						customFieldValue = null;
					}
					customFieldValues.add(customFieldValue);
					definitionToValue.put(definition, customFieldValue);
				}
				importTopic.setCustomFieldValues(customFieldValues);
				importTopic.setCustomFieldDefinitionToValue(definitionToValue);
			}
			
			
			String groupRestrictionInput = line.length > 5? line[5]: "";
			if (StringHelper.containsNonWhitespace(groupRestrictionInput)) {
				Set<Long> groupRestrictionKeys = Arrays.stream(groupRestrictionInput.split(","))
						.filter(StringHelper::isLong)
						.map(Long::valueOf)
						.collect(Collectors.toSet());
				groupRestrictionKeys.retainAll(importContext.getGroupRestrictionCandidates().getBusinessGroupKeys());
				topic.setGroupRestrictionKeys(groupRestrictionKeys);
			}
			
			if (line.length > 4) {
				String maxParticipantsInput = line[4];
				if (StringHelper.containsNonWhitespace(maxParticipantsInput)) {
					importTopic.setMaxParticipants(maxParticipantsInput);
					if (StringHelper.isLong(maxParticipantsInput)) {
						Integer maxParticipants = Integer.valueOf(maxParticipantsInput);
						if (maxParticipants >= 0) {
							topic.setMaxParticipants(maxParticipants);
						} else {
							importTopic.setMessage(translate("import.error.positiv.integer.participants.max"));
						}
					} else {
						importTopic.setMessage(translate("import.error.positiv.integer.participants.max"));
					}
				} else {
					importTopic.setMessage(translate("import.error.mandatory.participants.max"));
				}
			} else {
				importTopic.setMessage(translate("import.error.mandatory.participants.max"));
			}
			
			
			if (line.length > 3) {
				String minParticipantsInput = line[3];
				if (StringHelper.containsNonWhitespace(minParticipantsInput)) {
					importTopic.setMinParticipants(minParticipantsInput);
					if (StringHelper.isLong(minParticipantsInput)) {
						Integer minParticipants = Integer.valueOf(minParticipantsInput);
						if (minParticipants >= 0) {
							topic.setMinParticipants(minParticipants);
						} else {
							importTopic.setMessage(translate("import.error.positiv.integer.participants.min"));
						}
					} else {
						importTopic.setMessage(translate("import.error.positiv.integer.participants.min"));
					}
				} else {
					importTopic.setMessage(translate("import.error.mandatory.participants.min"));
				}
			} else {
				importTopic.setMessage(translate("import.error.mandatory.participants.min"));
			}
			
			if (topic.getMinParticipants() != null && topic.getMaxParticipants() != null && topic.getMinParticipants() > topic.getMaxParticipants()) {
				importTopic.setMessage(translate("import.error.participants.max.lower.min"));
			}
			
			
			if (line.length > 2) {
				String description = line[2];
				importTopic.setDescription(description);
				topic.setDescription(description);
			}
			
			
			if (line.length > 1) {
				String title = line[1];
				importTopic.setTitle(title);
				topic.setTitle(title);
				if (!StringHelper.containsNonWhitespace(topic.getTitle())) {
					importTopic.setMessage(translate("import.error.mandatory.title"));
				}
			} else {
				importTopic.setMessage(translate("import.error.mandatory.title"));
			}
			
			String identifier = line[0];
			importTopic.setIdentifier(identifier);
			topic.setIdentifier(identifier);
			if (identifiersInLines.contains(identifier)) {
				importTopic.setMessage(translate("import.error.identifier.multi"));
			} else {
				identifiersInLines.add(identifier);
			}
		} catch (Exception e) {
			importTopic.setMessage(translate("import.error.unknown"));
		}
		
		return importTopic;
	}
	
	private List<String[]> getLines(String input) {
		CSVParser parser = new CSVParserBuilder()
				.withSeparator('\t')
				.build();
		
		List<String[]> lines = new ArrayList<>();
		try(CSVReader reader = new CSVReaderBuilder(new StringReader(input))
					.withCSVParser(parser)
					.build()) {
			
			String [] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				if(nextLine.length > 0) {
					lines.add(nextLine);
				}
			}
		} catch (IOException | CsvValidationException e) {
			logError("", e);
		}
		return lines;
	}
	
	public static class TBTopicImportDataModel extends DefaultFlexiTableDataModel<TBImportTopic> {
		
		public static final int CUSTOM_FIELD_OFFSET = 100;
		private static final TopicImportCols[] COLS = TopicImportCols.values();

		public TBTopicImportDataModel(FlexiTableColumnModel columnsModel) {
			super(columnsModel);
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			TBImportTopic topic = getObject(row);
			return getValueAt(topic, col);
		}

		public Object getValueAt(TBImportTopic row, int col) {
			if (col >= CUSTOM_FIELD_OFFSET) {
				return row.getCustomFieldValues().get(col - CUSTOM_FIELD_OFFSET);
			}
			
			switch(COLS[col]) {
			case identifier: return row.getIdentifier();
			case title: return row.getTitle();
			case minParticipants: return row.getMinParticipants();
			case maxParticipants: return row.getMaxParticipants();
			case groupRestrictions: return row.getGroupRestrictions();
			case importMessage: return StringHelper.containsNonWhitespace(row.getMessage())
					? "<span><i class=\"o_icon o_icon_error\"></i> " + row.getMessage() + "</span>"
					: null;
			default: return null;
			}
		}
		
		public enum TopicImportCols implements FlexiColumnDef {
			identifier("topic.identifier"),
			title("topic.title"),
			minParticipants("topic.participants.min"),
			maxParticipants("topic.participants.max"),
			groupRestrictions("topic.group.restriction"),
			importMessage("import.message");
			
			private final String i18nKey;
			
			private TopicImportCols(String i18nKey) {
				this.i18nKey = i18nKey;
			}
			
			@Override
			public String i18nHeaderKey() {
				return i18nKey;
			}
		}
	}

}
