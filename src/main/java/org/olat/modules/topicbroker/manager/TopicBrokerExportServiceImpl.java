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

import static org.olat.modules.topicbroker.TopicBrokerService.TEASER_IMAGE_DIR;
import static org.olat.modules.topicbroker.TopicBrokerService.TEASER_VIDEO_DIR;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
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
import org.olat.modules.topicbroker.model.TBImportTopic;
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
		
		Map<Long, List<String>> topicKeyToCustomFieldTexts = new HashMap<>(topics.size());
		Map<String, Map<String, VFSLeaf>> topicIdentToFileIdentToLeaf = new HashMap<>(topics.size());
		for (TBTopic topic : topics) {
			VFSLeaf topicLeaf = topicBrokerService.getTopicLeaf(topic, TopicBrokerService.TEASER_IMAGE_DIR);
			add(topicIdentToFileIdentToLeaf, topic, EXPORT_TEASER_IMAGE, topicLeaf);
			topicLeaf = topicBrokerService.getTopicLeaf(topic, TopicBrokerService.TEASER_VIDEO_DIR);
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
		
		TBSelectionSearchParams selectionSearchParams = new TBSelectionSearchParams();
		selectionSearchParams.setBroker(broker);
		selectionSearchParams.setEnrolledOrMaxSortOrder(reloadedBorker.getMaxSelections());
		selectionSearchParams.setFetchIdentity(true);
		selectionSearchParams.setFetchTopic(true);
		
		Set<Identity> identities;
		if (participantCandidates.isAllIdentitiesVisible()) {
			identities = new HashSet<>(participantCandidates.getAllIdentities());
			selectionSearchParams.setEnrolledOrIdentities(identities);
		} else {
			identities = new HashSet<>(participantCandidates.getVisibleIdentities());
			selectionSearchParams.setIdentities(identities);
		}
		List<TBSelection> selections = topicBrokerService.getSelections(selectionSearchParams);
		Map<Long, Map<Long, TBSelection>> identityKeyToTopicToSelections = selections.stream()
				.collect(Collectors.groupingBy(selection -> selection.getParticipant().getIdentity().getKey(),
						Collectors.toMap(selection -> selection.getTopic().getKey(), Function.identity())));
		List<Identity> selectionIdentities = selections.stream()
				.map(selection -> selection.getParticipant().getIdentity())
				.collect(Collectors.toList());
		identities.addAll(selectionIdentities);
		
		TopicBrokerFilesExport filesExport = new TopicBrokerFilesExport(reloadedBorker, topicIdentToFileIdentToLeaf);
		
		TopicBrokerExcelExport excelExport = new TopicBrokerExcelExport(ureq, topics, customFieldNames,
				topicKeyToCustomFieldTexts, true, true, new ArrayList<>(identities), identityKeyToTopicToSelections);
		
		Map<Long, List<TBSelection>> topicKeyToEnrollments = selections.stream()
				.filter(TBSelection::isEnrolled)
				.collect(Collectors.groupingBy(selection -> selection.getTopic().getKey()));
		Map<String, TopicBrokerExcelExport> topicIdentToExcelExport = new HashMap<>(topics.size());
		for (TBTopic topic : topics) {
			List<TBTopic> topicList = new ArrayList<>(1);
			topicList.add(topic);
			List<TBSelection> enrollments = topicKeyToEnrollments.get(topic.getKey());
			List<Identity> topicIdentities = enrollments != null && !enrollments.isEmpty()
					? enrollments.stream().map(selection -> selection.getParticipant().getIdentity()).collect(Collectors.toList())
					: new ArrayList<>(0);
			TopicBrokerExcelExport topicExcelExport = new TopicBrokerExcelExport(ureq, topicList, customFieldNames,
					topicKeyToCustomFieldTexts, true, false, topicIdentities, identityKeyToTopicToSelections);
			topicIdentToExcelExport.put(topic.getIdentifier(), topicExcelExport);
		}
		
		return new TopicBrokerMediaResource(reloadedBorker, filesExport, excelExport, topicIdentToExcelExport);
	}

	private void add(Map<Long, List<String>> topicKeyToCustomFieldTexts, TBTopic topic, String text) {
		topicKeyToCustomFieldTexts.computeIfAbsent(topic.getKey(), identigier -> new ArrayList<>(1))
				.add(text);
	}

	private void add(Map<String, Map<String, VFSLeaf>> topicIdentToFileIdentToLeaf, TBTopic topic, String fileIdentitfier, VFSLeaf topicLeaf) {
		topicIdentToFileIdentToLeaf.computeIfAbsent(topic.getIdentifier(), identigier -> new HashMap<>(1))
				.put(fileIdentitfier, topicLeaf);
	}
	
	@Override
	public MediaResource createTopicImportTemplateMediaResource(UserRequest ureq, TBBrokerRef broker, String filename) {
		TBCustomFieldDefinitionSearchParams definitionSearchParams = new TBCustomFieldDefinitionSearchParams();
		definitionSearchParams.setBroker(broker);
		List<String> customFieldNames = topicBrokerService.getCustomFieldDefinitions(definitionSearchParams)
				.stream()
				.filter(definition -> TBCustomFieldType.text == definition.getType())
				.sorted((d1, d2) -> Integer.compare(d1.getSortOrder(), d2.getSortOrder()))
				.map(TBCustomFieldDefinition::getName)
				.toList();
		
		TopicBrokerExcelExport excelExport = new TopicBrokerExcelExport(ureq, Collections.emptyList(), customFieldNames,
				Map.of(), false, true, List.of(), Map.of());
		return new TopicBrokerExcelMediaResource(excelExport, filename);
	}
	
	@Override
	public MediaResource createFilesImportTemplateMediaResource(UserRequest ureq, TBBrokerRef broker, String filename) {
		TBCustomFieldDefinitionSearchParams definitionSearchParams = new TBCustomFieldDefinitionSearchParams();
		definitionSearchParams.setBroker(broker);
		List<TBCustomFieldDefinition> customFieldDefinitions = topicBrokerService.getCustomFieldDefinitions(definitionSearchParams)
				.stream()
				.filter(definition -> TBCustomFieldType.file == definition.getType())
				.sorted((d1, d2) -> Integer.compare(d1.getSortOrder(), d2.getSortOrder()))
				.toList();
		
		TBTopicSearchParams searchParams = new TBTopicSearchParams();
		searchParams.setBroker(broker);
		List<TBTopic> topics = topicBrokerService.getTopics(searchParams);
		
		Map<String, Map<String, VFSLeaf>> topicIdentToFileIdentToLeaf = new HashMap<>(topics.size());
		for (TBTopic topic : topics) {
			add(topicIdentToFileIdentToLeaf, topic, EXPORT_TEASER_IMAGE, null);
			add(topicIdentToFileIdentToLeaf, topic, EXPORT_TEASER_VIDEO, null);
			
			customFieldDefinitions.forEach(definition -> add(topicIdentToFileIdentToLeaf, topic, definition.getName(), null));
		}
		
		TopicBrokerFilesExport filesExport = new TopicBrokerFilesExport(broker, topicIdentToFileIdentToLeaf);
		return new TopicBrokerFilesMediaResource(broker, filesExport, filename);
	}

	@Override
	public void createOrUpdateTopics(Identity doer, TBBroker broker, List<TBImportTopic> importTopics, File tempFilesDir) {
		TBTopicSearchParams searchParams = new TBTopicSearchParams();
		searchParams.setBroker(broker);
		searchParams.setFetchBroker(true);
		searchParams.setFetchIdentities(true);
		Map<String, TBTopic> identToTopic = topicBrokerService.getTopics(searchParams).stream()
				.collect(Collectors.toMap(TBTopic::getIdentifier, Function.identity(), (u, v) -> v));
		
		List<String> orderedIdentificators = new ArrayList<>(importTopics.size());
		for (TBImportTopic importTopic : importTopics) {
			if (!StringHelper.containsNonWhitespace(importTopic.getMessage())) {
				TBTopic topicImported = importTopic.getTopic();
				TBTopic topicExisting = identToTopic.get(topicImported.getIdentifier());
				
				if (!importTopic.isFilesOnly()) {
					if (topicExisting == null) {
						topicExisting = topicBrokerService.createTopic(doer, broker);
					}
					topicBrokerService.updateTopic(doer, topicExisting, topicImported.getIdentifier(),
							topicImported.getTitle(), topicImported.getDescription(), topicImported.getBeginDate(),
							topicImported.getEndDate(), topicImported.getMinParticipants(),
							topicImported.getMaxParticipants(), topicImported.getGroupRestrictionKeys());
					orderedIdentificators.add(topicImported.getIdentifier());
					
					Map<TBCustomFieldDefinition,String> definitionToValue = importTopic.getCustomFieldDefinitionToValue();
					if (definitionToValue != null) {
						for (Entry<TBCustomFieldDefinition, String> entry : definitionToValue.entrySet()) {
							if (TBCustomFieldType.text == entry.getKey().getType()) {
								topicBrokerService.createOrUpdateCustomField(doer, entry.getKey(), topicExisting, entry.getValue());
							}
						}
					}
				}
				
				if (topicExisting != null && importTopic.getIdentifierToFile() != null && !importTopic.getIdentifierToFile().isEmpty()) {
					File file = importTopic.getIdentifierToFile().get(EXPORT_TEASER_IMAGE);
					if (file != null && file.exists()) {
						topicBrokerService.storeTopicLeaf(doer, topicExisting, TEASER_IMAGE_DIR, file, file.getName());
					}
					
					file = importTopic.getIdentifierToFile().get(EXPORT_TEASER_VIDEO);
					if (file != null && file.exists()) {
						topicBrokerService.storeTopicLeaf(doer, topicExisting, TEASER_VIDEO_DIR, file, file.getName());
					}
					
					Map<TBCustomFieldDefinition,String> definitionToValue = importTopic.getCustomFieldDefinitionToValue();
					if (definitionToValue != null) {
						for (Entry<TBCustomFieldDefinition, String> entry : definitionToValue.entrySet()) {
							if (TBCustomFieldType.file == entry.getKey().getType()) {
								file = importTopic.getIdentifierToFile().get(entry.getKey().getName());
								if (file != null && file.exists()) {
									topicBrokerService.createOrUpdateCustomFieldFile(doer, topicExisting, entry.getKey(), file, file.getName());
								}
							}
						}
					}
				}
			}
		}
		
		// clean up
		FileUtils.deleteDirsAndFiles(tempFilesDir, true, true);
		
		topicBrokerService.updateTopicSortOrder(doer, broker, orderedIdentificators);
	}

}
