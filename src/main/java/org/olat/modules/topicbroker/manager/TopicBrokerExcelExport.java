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
package org.olat.modules.topicbroker.manager;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.ui.TBParticipantDataModel;
import org.olat.modules.topicbroker.ui.TBUIFactory;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Jul 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TopicBrokerExcelExport {
	
	private static final Logger log = Tracing.createLoggerFor(TopicBrokerExcelExport.class);
	
	private final List<TBTopic> topics;
	private final List<String> customFieldNames;
	private final Map<Long, List<String>> topicKeyToCustomFieldTexts;
	private final boolean participantsSheet;
	private final List<Identity> identities;
	private final Map<Long, Map<Long, TBSelection>> identityKeyToTopicToSelections;
	private final Translator translator;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private UserManager userManager;

	public TopicBrokerExcelExport(UserRequest ureq, List<TBTopic> topics, List<String> customFieldNames,
			Map<Long, List<String>> topicKeyToCustomFieldTexts, boolean participantsSheet,
			List<Identity> identities, Map<Long, Map<Long, TBSelection>> identityKeyToTopicToSelections) {
		this.topics = topics;
		this.customFieldNames = customFieldNames;
		this.topicKeyToCustomFieldTexts = topicKeyToCustomFieldTexts;
		this.participantsSheet = participantsSheet;
		this.identities = identities;
		this.identityKeyToTopicToSelections = identityKeyToTopicToSelections;
		CoreSpringFactory.autowireObject(this);

		Collections.sort(topics, (t1, t2) -> t1.getTitle().compareToIgnoreCase(t2.getTitle()));
		
		translator = userManager.getPropertyHandlerTranslator(
				Util.createPackageTranslator(TBUIFactory.class, ureq.getLocale()));
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(TBParticipantDataModel.USAGE_IDENTIFIER, isAdministrativeUser);
	}
	
	public void export(OutputStream out) {
		List<String> sheetNames = participantsSheet
				? List.of(translator.translate("export.topics"), translator.translate("export.participants"))
				: List.of(translator.translate("export.topics"));
	
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, sheetNames.size(), sheetNames)) {
			addTopics(workbook);
			if (participantsSheet) {
				addSelections(workbook);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	private void addTopics(OpenXMLWorkbook workbook) {
		OpenXMLWorksheet worksheet = workbook.nextWorksheet();
		addTopicHeader(worksheet);
		addTopicContent(workbook, worksheet);
	}
	
	private void addTopicHeader(OpenXMLWorksheet worksheet) {
		worksheet.setHeaderRows(1);
		
		int col = 0;
		Row row = worksheet.newRow();
		row.addCell(col++, translator.translate("topic.identifier"));
		row.addCell(col++, translator.translate("topic.title"));
		row.addCell(col++, translator.translate("topic.description"));
		row.addCell(col++, translator.translate("topic.participants.min"));
		row.addCell(col++, translator.translate("topic.participants.max"));
		row.addCell(col++, translator.translate("topic.group.restriction"));
		
		for (String customFieldName : customFieldNames) {
			row.addCell(col++, customFieldName);
		}
	}
	
	private void addTopicContent(OpenXMLWorkbook workbook, OpenXMLWorksheet worksheet) {
		for (TBTopic topic : topics) {
			List<String> customFieldTexts = topicKeyToCustomFieldTexts.getOrDefault(topic.getKey(), List.of());
			addTopicContent(workbook, worksheet, topic, customFieldTexts);
		}
	}
	
	private void addTopicContent(OpenXMLWorkbook workbook, OpenXMLWorksheet worksheet, TBTopic topic, List<String> customFieldTexts) {
		Row row = worksheet.newRow();
		
		int col = 0;
		row.addCell(col++, topic.getIdentifier());
		row.addCell(col++, topic.getTitle());
		row.addCell(col++, topic.getDescription(), workbook.getStyles().getBottomAlignStyle());
		row.addCell(col++, topic.getMinParticipants(), workbook.getStyles().getIntegerStyle());
		row.addCell(col++, topic.getMaxParticipants(), workbook.getStyles().getIntegerStyle());
		String groupRestrictions = null;
		if (topic.getGroupRestrictionKeys() != null) {
			groupRestrictions = topic.getGroupRestrictionKeys().stream().map(String::valueOf).collect(Collectors.joining(","));
		}
		row.addCell(col++,groupRestrictions);
		for (String customFieldText : customFieldTexts) {
			row.addCell(col++, customFieldText, workbook.getStyles().getBottomAlignStyle());
		}
	}

	private void addSelections(OpenXMLWorkbook workbook) {
		OpenXMLWorksheet worksheet = workbook.nextWorksheet();
		addSelectionHeader(workbook, worksheet);
		addSelectionContent(workbook, worksheet);
	}

	private void addSelectionHeader(OpenXMLWorkbook workbook, OpenXMLWorksheet worksheet) {
		worksheet.setHeaderRows(1);
		
		Row row = worksheet.newRow();
		
		int col = 0;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			row.addCell(col++, translator.translate(userPropertyHandler.i18nColumnDescriptorLabelKey()));
		}
		
		for (TBTopic topic : topics) {
			row.addCell(col++, translator.translate("export.topic.priority", topic.getIdentifier()), workbook.getStyles().getBottomAlignStyle());
			row.addCell(col++, translator.translate("export.topic.selected", topic.getIdentifier()), workbook.getStyles().getBottomAlignStyle());
		}
	}

	private void addSelectionContent(OpenXMLWorkbook workbook, OpenXMLWorksheet worksheet) {
		for (Identity identity : identities) {
			addSelectionContent(workbook, worksheet, identity);
		}
	}

	private void addSelectionContent(OpenXMLWorkbook workbook, OpenXMLWorksheet worksheet, Identity identity) {
		Row row = worksheet.newRow();
		
		int col = 0;
		
		User user = identity.getUser();
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			row.addCell(col++, userPropertyHandler.getUserProperty(user, translator.getLocale()));
		}
		
		Map<Long, TBSelection> topicKeyToSelection = identityKeyToTopicToSelections.get(identity.getKey());
		if (topicKeyToSelection == null) {
			return;
		}
		
		for (TBTopic topic : topics) {
			TBSelection selection = topicKeyToSelection.get(topic.getKey());
			if (selection != null) {
				row.addCell(col++, selection.getSortOrder(), workbook.getStyles().getIntegerStyle());
				if (selection.isEnrolled()) {
					row.addCell(col++, "X");
				} else {
					col++;
				}
			} else {
				col++;
				col++;
			}
		}
	}

}
