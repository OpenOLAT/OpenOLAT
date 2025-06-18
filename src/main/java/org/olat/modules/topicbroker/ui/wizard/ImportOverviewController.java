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


import static org.olat.modules.topicbroker.TopicBrokerExportService.EXPORT_TEASER_IMAGE;
import static org.olat.modules.topicbroker.TopicBrokerExportService.EXPORT_TEASER_VIDEO;

import java.util.List;

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
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionSearchParams;
import org.olat.modules.topicbroker.TBCustomFieldType;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.model.TBImportTopic;
import org.olat.modules.topicbroker.ui.TBTopicDataModel;
import org.olat.modules.topicbroker.ui.TBUIFactory;
import org.olat.modules.topicbroker.ui.wizard.ImportOverviewController.TBTopicImportDataModel.TopicImportCols;
import org.springframework.beans.factory.annotation.Autowired;

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

	public ImportOverviewController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		setTranslator(Util.createPackageTranslator(TBUIFactory.class, getLocale(), getTranslator()));
		
		importContext = (ImportContext)getFromRunContext("importContext");
		TBCustomFieldDefinitionSearchParams definitionSearchParams = new TBCustomFieldDefinitionSearchParams();
		definitionSearchParams.setBroker(importContext.getBroker());
		definitions = topicBrokerService.getCustomFieldDefinitions(definitionSearchParams).stream()
				.filter(definition -> importContext.getTempFilesDir() != null? true: TBCustomFieldType.text == definition.getType())
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicImportCols.description));
		
		DefaultFlexiColumnModel minParticipantsColumn = new DefaultFlexiColumnModel(TopicImportCols.minParticipants);
		minParticipantsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		minParticipantsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(minParticipantsColumn);
		
		DefaultFlexiColumnModel maxParticipantsColumn = new DefaultFlexiColumnModel(TopicImportCols.maxParticipants);
		maxParticipantsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		maxParticipantsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(maxParticipantsColumn);
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicImportCols.beginDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicImportCols.endDate));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicImportCols.groupRestrictions));
		
		if (importContext.getTempFilesDir() != null) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicImportCols.teaserImage));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicImportCols.teaserVideo));
		}
		
		int columnIndex = TBTopicDataModel.CUSTOM_FIELD_OFFSET;
		for (TBCustomFieldDefinition customFieldDefinition : definitions) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(null, columnIndex++);
			columnModel.setHeaderLabel(StringHelper.escapeHtml(customFieldDefinition.getName()));
			columnsModel.addFlexiColumnModel(columnModel);
		}
		
		DefaultFlexiColumnModel messageColumn = new DefaultFlexiColumnModel(TopicImportCols.importMessage, new TextFlexiCellRenderer(EscapeMode.none));
		messageColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(messageColumn);
		
		dataModel = new TBTopicImportDataModel(columnsModel, definitions);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		
		ImportTopicConverter topicConverter = new ImportTopicConverter(
				getLocale(),
				importContext.getBroker(),
				definitions,
				importContext.getGroupRestrictionCandidates(),
				importContext.getInput(),
				importContext.getTempFilesDir());
		List<TBImportTopic> topics = topicConverter.getTopics();
		importContext.setTopics(topics);
		dataModel.setObjects(topics);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	public static class TBTopicImportDataModel extends DefaultFlexiTableDataModel<TBImportTopic> {
		
		public static final int CUSTOM_FIELD_OFFSET = 100;
		private static final TopicImportCols[] COLS = TopicImportCols.values();
		
		private final List<TBCustomFieldDefinition> customFieldDefinitions;

		public TBTopicImportDataModel(FlexiTableColumnModel columnsModel, List<TBCustomFieldDefinition> customFieldDefinitions) {
			super(columnsModel);
			this.customFieldDefinitions = customFieldDefinitions;
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			TBImportTopic topic = getObject(row);
			return getValueAt(topic, col);
		}

		public Object getValueAt(TBImportTopic row, int col) {
			if (col >= CUSTOM_FIELD_OFFSET) {
				if (row.getCustomFieldDefinitionToValue() == null) {
					return null;
				}
				TBCustomFieldDefinition customFieldDefinition = customFieldDefinitions.get(col - CUSTOM_FIELD_OFFSET);
				return row.getCustomFieldDefinitionToValue().get(customFieldDefinition);
			}
			
			switch(COLS[col]) {
			case identifier: return row.getIdentifier();
			case title: return row.getTitle();
			case description: return row.getDescription();
			case minParticipants: return row.getMinParticipants();
			case maxParticipants: return row.getMaxParticipants();
			case beginDate: return row.getBeginDate();
			case endDate: return row.getEndDate();
			case groupRestrictions: return row.getGroupRestrictions();
			case teaserImage: return row.getIdentifierToFile() != null && row.getIdentifierToFile().containsKey(EXPORT_TEASER_IMAGE)
					? row.getIdentifierToFile().get(EXPORT_TEASER_IMAGE).getName() : null;
			case teaserVideo: return row.getIdentifierToFile() != null && row.getIdentifierToFile().containsKey(EXPORT_TEASER_VIDEO)
					? row.getIdentifierToFile().get(EXPORT_TEASER_VIDEO).getName() : null;
			case importMessage: return StringHelper.containsNonWhitespace(row.getMessage())
					? "<span><i class=\"o_icon o_icon_error\"></i> " + row.getMessage() + "</span>"
					: null;
			default: return null;
			}
		}
		
		public enum TopicImportCols implements FlexiColumnDef {
			identifier("topic.identifier"),
			title("topic.title"),
			description("topic.description"),
			beginDate("topic.begin.date"),
			endDate("topic.end.date"),
			minParticipants("topic.participants.min"),
			maxParticipants("topic.participants.max"),
			groupRestrictions("topic.group.restriction"),
			teaserImage("topic.teaser.image"),
			teaserVideo("topic.teaser.video"),
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
