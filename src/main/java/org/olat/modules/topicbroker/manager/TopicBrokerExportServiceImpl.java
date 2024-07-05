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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBBrokerRef;
import org.olat.modules.topicbroker.TBCustomField;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionSearchParams;
import org.olat.modules.topicbroker.TBCustomFieldSearchParams;
import org.olat.modules.topicbroker.TBCustomFieldType;
import org.olat.modules.topicbroker.TBParticipantCandidates;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBSelectionSearchParams;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicSearchParams;
import org.olat.modules.topicbroker.TopicBrokerExportService;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.model.TBCustomFieldDefinitionExport;
import org.olat.modules.topicbroker.model.TBCustomFieldDefinitionsExport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 1 Jul 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class TopicBrokerExportServiceImpl implements TopicBrokerExportService {
	
	private static final String EXPORT_TEASER_IMAGE = "teaserimage";
	private static final String EXPORT_TEASER_VIDEO = "teaservideo";
	
	@Autowired
	private TopicBrokerService topicBrokerService;
	
	@Override
	public void createCustomFieldDefinitions(Identity doer, TBBroker broker, String customFieldDefinitionsXml) {
		if (!StringHelper.containsNonWhitespace(customFieldDefinitionsXml)) {
			return;
		}
		
		TBCustomFieldDefinitionsExport definitionsExport = TopicBrokerXStream.fromXml(customFieldDefinitionsXml, TBCustomFieldDefinitionsExport.class);
		if (definitionsExport == null) {
			return;
		}
		
		definitionsExport.getDefinitions().forEach(export -> {
			TBCustomFieldDefinition definition = topicBrokerService.createCustomFieldDefinition(doer, broker);
			topicBrokerService.updateCustomFieldDefinition(doer, definition, export.getIdentifier(), export.getName(),
					export.getType(), export.isDisplayInTable());
		});
	}

	@Override
	public String getCustomFieldDefinitionExportXml(TBBrokerRef broker) {
		TBCustomFieldDefinitionSearchParams searchParams = new TBCustomFieldDefinitionSearchParams();
		searchParams.setBroker(broker);
		List<TBCustomFieldDefinition> customFieldDefinitions = topicBrokerService.getCustomFieldDefinitions(searchParams);
		if (customFieldDefinitions.isEmpty()) {
			return null;
		}
		
		List<TBCustomFieldDefinitionExport> export = customFieldDefinitions.stream()
			.map(TBCustomFieldDefinitionExport::of)
			.toList();
		TBCustomFieldDefinitionsExport definitionsExport = new TBCustomFieldDefinitionsExport();
		definitionsExport.setDefinitions(export);
		
		return TopicBrokerXStream.toXml(definitionsExport);
	}
	
	@Override
	public MediaResource createMediaResource(UserRequest ureq, TBBrokerRef broker, TBParticipantCandidates participantCandidates) {
		TBBroker reloadedBorker = topicBrokerService.getBroker(broker);
		if (reloadedBorker == null) {
			return new NotFoundMediaResource();
		}
		
		TBCustomFieldDefinitionSearchParams definitionSearchParams = new TBCustomFieldDefinitionSearchParams();
		definitionSearchParams.setBroker(broker);
		List<TBCustomFieldDefinition> customFieldDefinitions = topicBrokerService.getCustomFieldDefinitions(definitionSearchParams)
				.stream()
				.sorted((d1, d2) -> Integer.compare(d1.getSortOrder(), d2.getSortOrder()))
				.toList();
		List<String> customFieldNames = customFieldDefinitions
				.stream()
				.filter(definition -> TBCustomFieldType.text == definition.getType())
				.map(TBCustomFieldDefinition::getName)
				.toList();
		
		TBCustomFieldSearchParams customFieldsSearchParams = new TBCustomFieldSearchParams();
		customFieldsSearchParams.setBroker(broker);
		Map<Long, Map<Long, TBCustomField>> topicToDefinitionToCustomFields = topicBrokerService
				.getCustomFields(customFieldsSearchParams)
				.stream()
				.collect(Collectors.groupingBy(customField -> customField.getTopic().getKey(),
						Collectors.toMap(customField -> customField.getDefinition().getKey(), Function.identity())));
		
		TBTopicSearchParams searchParams = new TBTopicSearchParams();
		searchParams.setBroker(broker);
		List<TBTopic> topics = topicBrokerService.getTopics(searchParams);
		
		Map<Long, List<String>> topicKeyToCustomFieldTexts = new HashMap<>();
		Map<String, Map<String, VFSLeaf>> topicIdentToFileIdentToLeaf = new HashMap<>();
		for (TBTopic topic : topics) {
			VFSLeaf topicLeaf = topicBrokerService.getTopicLeaf(topic, TopicBrokerService.TOPIC_TEASER_IMAGE);
			add(topicIdentToFileIdentToLeaf, topic, EXPORT_TEASER_IMAGE, topicLeaf);
			topicLeaf = topicBrokerService.getTopicLeaf(topic, TopicBrokerService.TOPIC_TEASER_VIDEO);
			add(topicIdentToFileIdentToLeaf, topic, EXPORT_TEASER_VIDEO, topicLeaf);
			
			Map<Long, TBCustomField> definitionKeyToCustomField = topicToDefinitionToCustomFields.get(topic.getKey());
			for (TBCustomFieldDefinition definition: customFieldDefinitions) {
				if (TBCustomFieldType.text == definition.getType()) {
					String text = null;
					if (definitionKeyToCustomField != null) {
						TBCustomField customField = definitionKeyToCustomField.get(definition.getKey());
						if (customField != null) {
							text = customField.getText();
						}
					}
					add(topicKeyToCustomFieldTexts, topic, text);
				} else if (TBCustomFieldType.file == definition.getType()) {
					topicLeaf = topicBrokerService.getTopicLeaf(topic, definition.getIdentifier());
					add(topicIdentToFileIdentToLeaf, topic, definition.getName(), topicLeaf);
				}
			}
		}
		
		List<Identity> identities = participantCandidates.getVisibleIdentities();
		TBSelectionSearchParams selectionSearchParams = new TBSelectionSearchParams();
		selectionSearchParams.setBroker(broker);
		selectionSearchParams.setIdentities(identities);
		selectionSearchParams.setEnrolledOrMaxSortOrder(reloadedBorker.getMaxSelections());
		selectionSearchParams.setFetchParticipant(true);
		selectionSearchParams.setFetchTopic(true);
		Map<Long, Map<Long, TBSelection>> identityKeyToTopicToSelections = topicBrokerService.getSelections(selectionSearchParams).stream()
				.collect(Collectors.groupingBy(selection -> selection.getParticipant().getIdentity().getKey(),
						Collectors.toMap(selection -> selection.getTopic().getKey(), Function.identity())));
		
		TopicBrokerExcelExport excelExport = new TopicBrokerExcelExport(ureq, topics, customFieldNames,
				topicKeyToCustomFieldTexts, identities, identityKeyToTopicToSelections);
		
		return new TopicBrokerMediaResource(reloadedBorker, topicIdentToFileIdentToLeaf, excelExport);
	}

	private void add(Map<Long, List<String>> topicKeyToCustomFieldTexts, TBTopic topic, String text) {
		topicKeyToCustomFieldTexts.computeIfAbsent(topic.getKey(), identigier -> new ArrayList<>(1))
				.add(text);
	}

	private void add(Map<String, Map<String, VFSLeaf>> topicIdentToFileIdentToLeaf, TBTopic topic, String fileIdentitfier, VFSLeaf topicLeaf) {
		topicIdentToFileIdentToLeaf.computeIfAbsent(topic.getIdentifier(), identigier -> new HashMap<>(1))
				.put(fileIdentitfier, topicLeaf);
	}

}
