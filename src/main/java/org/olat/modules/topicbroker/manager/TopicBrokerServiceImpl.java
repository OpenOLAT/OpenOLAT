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

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.nodes.topicbroker.TopicBrokerCourseNodeParticipantCandidates;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.BusinessGroupRefImpl;
import org.olat.modules.topicbroker.TBAuditLog;
import org.olat.modules.topicbroker.TBAuditLogSearchParams;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBBrokerRef;
import org.olat.modules.topicbroker.TBBrokerSearchParams;
import org.olat.modules.topicbroker.TBCustomField;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionRef;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionSearchParams;
import org.olat.modules.topicbroker.TBCustomFieldSearchParams;
import org.olat.modules.topicbroker.TBCustomFieldType;
import org.olat.modules.topicbroker.TBEnrollmentStats;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBParticipantRef;
import org.olat.modules.topicbroker.TBParticipantSearchParams;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBSelectionSearchParams;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicRef;
import org.olat.modules.topicbroker.TBTopicSearchParams;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.model.TBBrokerImpl;
import org.olat.modules.topicbroker.model.TBCustomFieldDefinitionImpl;
import org.olat.modules.topicbroker.model.TBCustomFieldImpl;
import org.olat.modules.topicbroker.model.TBSelectionImpl;
import org.olat.modules.topicbroker.model.TBTopicImpl;
import org.olat.modules.topicbroker.ui.events.TBBrokerChangedEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 29 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class TopicBrokerServiceImpl implements TopicBrokerService {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TBBrokerDAO brokerDao;
	@Autowired
	private TBParticipantDAO participantDao;
	@Autowired
	private TBTopicDAO topicDao;
	@Autowired
	private TopicBrokerStorage tbStorage;
	@Autowired
	private TBCustomFieldDefinitionDAO customFieldDefinitionDao;
	@Autowired
	private TBCustomFieldDAO customFieldDao;
	@Autowired
	private TBSelectionDAO selectionDao;
	@Autowired
	private TBAuditLogDAO auditLogDao;
	@Autowired
	private Coordinator coordinator;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	
	@Override
	public TBBroker createBroker(Identity doer, RepositoryEntry repositoryEntry, String subIdent) {
		TBBroker broker = brokerDao.createBroker(repositoryEntry, subIdent);
		String after = TopicBrokerXStream.toXml(broker);
		auditLogDao.create(TBAuditLog.Action.brokerCreate, null, after, doer, broker);
		
		return broker;
	}
	
	@Override
	public void deleteBroker(RepositoryEntry repositoryEntry, String subIdent) {
		TBBroker broker = brokerDao.loadBroker(repositoryEntry, subIdent);
		if (broker == null) {
			return;
		}
		
		auditLogDao.delete(broker);
		customFieldDao.deleteCustomFields(broker);
		customFieldDefinitionDao.deleteDefinitions(broker);
		selectionDao.deleteSelections(broker);
		topicDao.deleteTopics(broker);
		participantDao.deleteParticipants(broker);
		tbStorage.deleteLeafs(broker);
		brokerDao.deleteBroker(broker);
	}
	
	@Override
	public TBBroker updateBroker(Identity doer, TBBrokerRef broker, Integer maxSelections, Date selectionStartDate,
			Date selectionEndDate, Integer requiredEnrollments, boolean participantCanEditRequiredEnrollments,
			boolean autoEnrollment, boolean participantCanWithdraw, Date withdrawEndDate) {
		TBBroker reloadedBroker = getBroker(broker);
		if (reloadedBroker == null) {
			return null;
		}
		
		String before = TopicBrokerXStream.toXml(reloadedBroker);
		boolean contentChanged = false;
		if (!Objects.equals(reloadedBroker.getMaxSelections(), maxSelections)) {
			reloadedBroker.setMaxSelections(maxSelections);
			contentChanged = true;
		}
		if (!Objects.equals(reloadedBroker.getSelectionStartDate(), selectionStartDate)) {
			reloadedBroker.setSelectionStartDate(selectionStartDate);
			contentChanged = true;
		}
		if (!Objects.equals(reloadedBroker.getSelectionEndDate(), selectionEndDate)) {
			reloadedBroker.setSelectionEndDate(selectionEndDate);
			contentChanged = true;
		}
		if (!Objects.equals(reloadedBroker.getRequiredEnrollments(), requiredEnrollments)) {
			reloadedBroker.setRequiredEnrollments(requiredEnrollments);
			contentChanged = true;
		}
		if (reloadedBroker.isParticipantCanEditRequiredEnrollments() != participantCanEditRequiredEnrollments) {
			reloadedBroker.setParticipantCanEditRequiredEnrollments(participantCanEditRequiredEnrollments);
			contentChanged = true;
		}
		if (reloadedBroker.isAutoEnrollment() != autoEnrollment) {
			reloadedBroker.setAutoEnrollment(autoEnrollment);
			contentChanged = true;
		}
		if (reloadedBroker.isParticipantCanWithdraw() != participantCanWithdraw) {
			reloadedBroker.setParticipantCanWithdraw(participantCanWithdraw);
			contentChanged = true;
		}
		if (!Objects.equals(reloadedBroker.getWithdrawEndDate(), withdrawEndDate)) {
			reloadedBroker.setWithdrawEndDate(withdrawEndDate);
			contentChanged = true;
		}
		
		if (contentChanged) {
			reloadedBroker = brokerDao.updateBroker(reloadedBroker);
			
			String after = TopicBrokerXStream.toXml(reloadedBroker);
			auditLogDao.create(TBAuditLog.Action.brokerUpdateContent, before, after, doer, reloadedBroker);
			
			// Why this event? The course is loaded with every publication anyway!
			// The event is necessary because changes of the course life cycle can also lead
			// to the (immediate) termination of the selection period.
			dbInstance.commit();
			coordinator.getEventBus().fireEventToListenersOf(TBBrokerChangedEvent.EVENT, reloadedBroker);
		}
		
		return reloadedBroker;
	}
	
	@Override
	public void updateEnrollmentProcessStart(Identity doer, TBBrokerRef broker) {
		TBBroker reloadedBroker = getBroker(broker);
		if (reloadedBroker == null || reloadedBroker.getEnrollmentStartDate() != null) {
			return;
		}
		
		if (reloadedBroker instanceof TBBrokerImpl brokerImpl) {
			String before = TopicBrokerXStream.toXml(brokerImpl);
			
			brokerImpl.setEnrollmentStartDate(new Date());
			reloadedBroker = brokerDao.updateBroker(brokerImpl);
			
			String after = TopicBrokerXStream.toXml(reloadedBroker);
			auditLogDao.create(TBAuditLog.Action.brokerEnrollmentStart, before, after, doer, reloadedBroker);
		}
	}

	@Override
	public void updateEnrollmentProcessDone(Identity doer, TBBrokerRef broker) {
		TBBroker reloadedBroker = getBroker(broker);
		if (reloadedBroker == null || reloadedBroker.getEnrollmentDoneDate() != null) {
			return;
		}
		
		if (reloadedBroker instanceof TBBrokerImpl brokerImpl) {
			String before = TopicBrokerXStream.toXml(brokerImpl);
			
			brokerImpl.setEnrollmentDoneDate(new Date());
			reloadedBroker = brokerDao.updateBroker(brokerImpl);
			
			String after = TopicBrokerXStream.toXml(reloadedBroker);
			auditLogDao.create(TBAuditLog.Action.brokerEnrollmentDone, before, after, doer, reloadedBroker);
		}
	}
	
	@Override
	public TBBroker getOrCreateBroker(Identity doer, RepositoryEntry repositoryEntry, String subIdent) {
		TBBroker broker = getBroker(repositoryEntry, subIdent);
		if (broker == null) {
			broker = createBroker(doer, repositoryEntry, subIdent);
		}
		return broker;
	}
	
	@Override
	public TBBroker getBroker(RepositoryEntry repositoryEntry, String subIdent) {
		return  brokerDao.loadBroker(repositoryEntry, subIdent);
	}
	
	@Override
	public TBBroker getBroker(TBBrokerRef broker) {
		TBBrokerSearchParams params = new TBBrokerSearchParams();
		params.setBroker(broker);
		List<TBBroker> brokers = brokerDao.loadBrokers(params);
		return !brokers.isEmpty()? brokers.get(0): null;
	}
	
	private TBParticipant createParticipant(Identity doer, TBBroker broker, Identity participantIdentity) {
		TBParticipant participant = participantDao.createParticipant(broker, participantIdentity);
		
		String after = TopicBrokerXStream.toXml(participant);
		auditLogDao.create(TBAuditLog.Action.participantCreate, null, after, doer, participant);
		
		return participant;
	}
	
	@Override
	public TBParticipant updateParticipant(Identity doer, TBParticipant participant) {
		TBParticipant reloadedParticipant = getParticipant(participant);
		if (reloadedParticipant == null) {
			return participant;
		}
		
		String before = TopicBrokerXStream.toXml(reloadedParticipant);
		
		boolean contentChanged = false;
		if (!Objects.equals(reloadedParticipant.getBoost(), participant.getBoost())) {
			reloadedParticipant.setBoost(participant.getBoost());
			contentChanged = true;
		}
		if (!Objects.equals(reloadedParticipant.getRequiredEnrollments(), participant.getRequiredEnrollments())) {
			reloadedParticipant.setRequiredEnrollments(participant.getRequiredEnrollments());
			contentChanged = true;
		}
		
		if (contentChanged) {
			reloadedParticipant = participantDao.updateParticipant(reloadedParticipant);
			
			String after = TopicBrokerXStream.toXml(reloadedParticipant);
			auditLogDao.create(TBAuditLog.Action.participantUpdateContent, before, after, doer, participant);
		}
		
		return reloadedParticipant;
	}
	
	@Override
	public TBParticipant getOrCreateParticipant(Identity doer, TBBroker broker, Identity participantIdentity) {
		return getOrCreateParticipant(doer, broker, participantIdentity, false);
	}

	private TBParticipant getOrCreateParticipant(Identity doer, TBBroker broker, Identity participantIdentity, boolean reload) {
		TBParticipant participant = getParticipant(broker, participantIdentity);
		if (participant == null) {
			participant = createParticipant(doer, broker, participantIdentity);
			if (reload) {
				// reload to avoid hibernate classes in audit log xml
				participant = getParticipant(broker, participantIdentity);
			}
		}
		return participant;
	}
	
	private TBParticipant getParticipant(TBBrokerRef broker, IdentityRef identity) {
		TBParticipantSearchParams searchParams = new TBParticipantSearchParams();
		searchParams.setBroker(broker);
		searchParams.setIdentity(identity);
		List<TBParticipant> participants = getParticipants(searchParams);
		
		return !participants.isEmpty()? participants.get(0): null;
	}
	
	private TBParticipant getParticipant(TBParticipantRef participant) {
		TBParticipantSearchParams searchParams = new TBParticipantSearchParams();
		searchParams.setParticipant(participant);
		searchParams.setFetchBroker(true);
		searchParams.setFetchIdentity(true);
		List<TBParticipant> participants = getParticipants(searchParams);
		
		return !participants.isEmpty()? participants.get(0): null;
	}
	
	@Override
	public List<TBParticipant> getParticipants(TBParticipantSearchParams searchParams) {
		return participantDao.loadParticipants(searchParams);
	}
	
	@Override
	public Set<Long> filterMembership(IdentityRef identity, Collection<Long> groupKeys) {
		if (groupKeys == null || groupKeys.isEmpty()) {
			return Set.of();
		}
		
		List<? extends BusinessGroupRef> groupRefs = groupKeys.stream().map(BusinessGroupRefImpl::new).toList();
		return businessGroupRelationDao 
				.filterMembership(groupRefs, identity, GroupRoles.participant.name())
				.stream()
				.map(BusinessGroup::getKey)
				.collect(Collectors.toSet());
	}

	@Override
	public TBTopic createTopic(Identity doer, TBBrokerRef broker) {
		TBBroker reloadedBroker = getBroker(broker);
		if (broker == null) {
			return null;
		}
		
		TBTopic topic = topicDao.createTopic(doer, reloadedBroker);
		String after = TopicBrokerXStream.toXml(topic);
		auditLogDao.create(TBAuditLog.Action.topicCreate, null, after, doer, topic);
		
		return topic;
	}
	
	@Override
	public boolean isTopicIdentifierAvailable(TBBrokerRef broker, String identifier) {
		TBTopicSearchParams searchParams = new TBTopicSearchParams();
		searchParams.setBroker(broker);
		searchParams.setIdentifier(identifier);
		return topicDao.loadTopics(searchParams).isEmpty();
	}

	@Override
	public TBTopic updateTopic(Identity doer, TBTopic topic) {
		TBTopic reloadedTopic = getTopic(topic);
		if (reloadedTopic == null) {
			return topic;
		}
		
		String before = TopicBrokerXStream.toXml(reloadedTopic);
		
		boolean contentChanged = false;
		if (!Objects.equals(reloadedTopic.getIdentifier(), topic.getIdentifier())) {
			reloadedTopic.setIdentifier(topic.getIdentifier());
			contentChanged = true;
		}
		if (!Objects.equals(reloadedTopic.getTitle(), topic.getTitle())) {
			reloadedTopic.setTitle(topic.getTitle());
			contentChanged = true;
		}
		if (!Objects.equals(reloadedTopic.getDescription(), topic.getDescription())) {
			reloadedTopic.setDescription(topic.getDescription());
			contentChanged = true;
		}
		if (!Objects.equals(reloadedTopic.getMinParticipants(), topic.getMinParticipants())) {
			reloadedTopic.setMinParticipants(topic.getMinParticipants());
			contentChanged = true;
		}
		if (!Objects.equals(equalsString(reloadedTopic.getGroupRestrictionKeys()), equalsString(topic.getGroupRestrictionKeys()))) {
			reloadedTopic.setGroupRestrictionKeys(topic.getGroupRestrictionKeys());
			contentChanged = true;
		}
		
		if (contentChanged) {
			reloadedTopic = topicDao.updateTopic(reloadedTopic);
			
			String after = TopicBrokerXStream.toXml(reloadedTopic);
			auditLogDao.create(TBAuditLog.Action.topicUpdateContent, before, after, doer, topic);
		}
		
		return reloadedTopic;
	}
	
	private String equalsString(Set<Long> keys) {
		if (keys == null || keys.isEmpty()) {
			return null;
		}
		return keys.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));
	}

	@Override
	public void moveTopic(Identity doer, TBTopicRef topic, boolean up) {
		TBTopic reloadedTopic = getTopic(topic);
		if (reloadedTopic == null) return;
		
		int sortOrder = reloadedTopic.getSortOrder();
		TBTopic swapTopic = topicDao.loadNext(reloadedTopic, up);
		if (swapTopic == null) return;
		int swapSortOrder = swapTopic.getSortOrder();
		
		String before = TopicBrokerXStream.toXml(reloadedTopic);
		String beforeSwap = TopicBrokerXStream.toXml(swapTopic);
		
		((TBTopicImpl)reloadedTopic).setSortOrder(swapSortOrder);
		((TBTopicImpl)swapTopic).setSortOrder(sortOrder);
		reloadedTopic = topicDao.updateTopic(reloadedTopic);
		swapTopic = topicDao.updateTopic(swapTopic);
		
		String after = TopicBrokerXStream.toXml(reloadedTopic);
		String afterSwap = TopicBrokerXStream.toXml(swapTopic);
		
		auditLogDao.create(TBAuditLog.Action.topicUpdateSortOrder, before, after, doer, reloadedTopic);
		auditLogDao.create(TBAuditLog.Action.topicUpdateSortOrder, beforeSwap, afterSwap, doer, swapTopic);
	}
	
	@Override
	public void deleteTopicSoftly(Identity doer, TBTopicRef topic) {
		TBTopic reloadedTopic = getTopic(topic);
		if (reloadedTopic == null) {
			return;
		}
		
		TBSelectionSearchParams searchParams = new TBSelectionSearchParams();
		searchParams.setTopic(reloadedTopic);
		searchParams.setFetchIdentity(true);
		searchParams.setFetchTopic(true);
		getSelections(searchParams).forEach(selection -> unselect(doer,
				selection.getParticipant().getIdentity(),
				selection.getTopic(), 
				selection));
		
		String before = TopicBrokerXStream.toXml(reloadedTopic);
		
		((TBTopicImpl)reloadedTopic).setSortOrder(-1);
		((TBTopicImpl)reloadedTopic).setDeletedBy(doer);
		((TBTopicImpl)reloadedTopic).setDeletedDate(new Date());
		reloadedTopic = topicDao.updateTopic(reloadedTopic);
		
		String after = TopicBrokerXStream.toXml(reloadedTopic);
		auditLogDao.create(TBAuditLog.Action.topicDeleteSoftly, before, after, doer, reloadedTopic);
	}

	@Override
	public TBTopic getTopic(TBTopicRef topic) {
		return getTopic(topic, true);
	}

	private TBTopic getTopic(TBTopicRef topic, boolean active) {
		TBTopicSearchParams searchParams = new TBTopicSearchParams();
		searchParams.setTopic(topic);
		searchParams.setFetchIdentities(true);
		searchParams.setFetchBroker(true);
		if (!active) {
			searchParams.setDeleted(null);
		}
		List<TBTopic> topics = getTopics(searchParams);
		
		return !topics.isEmpty()? topics.get(0): null;
	}
	
	@Override
	public List<TBTopic> getTopics(TBTopicSearchParams searchParams) {
		return topicDao.loadTopics(searchParams);
	}

	@Override
	public VFSLeaf storeTopicLeaf(Identity doer, TBTopicRef topic, String identifier, File file, String filename) {
		TBTopic reloadedTopic = getTopic(topic);
		if (reloadedTopic == null) {
			return null;
		}
		
		VFSLeaf topicLeaf = getTopicLeaf(reloadedTopic, identifier);
		String filenameBefore = null;
		if (topicLeaf != null) {
			filenameBefore = topicLeaf.getName();
		}
		
		tbStorage.storeTopicLeaf(reloadedTopic, identifier, doer, file, filename);
		
		topicLeaf = getTopicLeaf(reloadedTopic, identifier);
		String filenameAfter = null;
		if (topicLeaf != null) {
			filenameAfter = topicLeaf.getName();
		}
		
		String before = StringHelper.containsNonWhitespace(filenameBefore)
				? TopicBrokerXStream.toXml(new TBAuditLog.TBFileAuditLog(identifier, filenameBefore))
				: null;
		String after = TopicBrokerXStream.toXml(new TBAuditLog.TBFileAuditLog(identifier, filenameAfter));
		
		auditLogDao.create(TBAuditLog.Action.topicUpdateFile, before, after, doer, reloadedTopic);
		
		return topicLeaf;
	}

	@Override
	public void deleteTopicLeaf(Identity doer, TBTopicRef topic, String identifier) {
		TBTopic reloadedTopic = getTopic(topic);
		if (reloadedTopic == null) {
			return;
		}
		
		VFSLeaf topicLeaf = getTopicLeaf(reloadedTopic, identifier);
		if (topicLeaf != null) {
			String before = TopicBrokerXStream.toXml(new TBAuditLog.TBFileAuditLog(identifier, topicLeaf.getName()));
			auditLogDao.create(TBAuditLog.Action.topicDeleteFile, before, null, doer, reloadedTopic);
			tbStorage.deleteTopicLeaf(reloadedTopic, identifier);
		}
	}

	@Override
	public VFSLeaf getTopicLeaf(TBTopic topic, String identifier) {
		if (topic == null) {
			return null;
		}
		
		return tbStorage.getTopicLeaf(topic, identifier);
	}
	
	@Override
	public TBCustomFieldDefinition createCustomFieldDefinition(Identity doer, TBBrokerRef broker) {
		TBBroker reloadedBroker = getBroker(broker);
		if (broker == null) {
			return null;
		}
		
		String identifier = UUID.randomUUID().toString();
		identifier = identifier.substring(0, identifier.indexOf('-'));
		TBCustomFieldDefinition definition = customFieldDefinitionDao.createDefinition(reloadedBroker, identifier);
		String after = TopicBrokerXStream.toXml(definition);
		auditLogDao.create(TBAuditLog.Action.cfDefinitionCreate, null, after, doer, definition);
		
		return definition;
	}
	
	@Override
	public boolean isCustomFieldDefinitionNameAvailable(TBBroker broker, String name) {
		TBCustomFieldDefinitionSearchParams searchParams = new TBCustomFieldDefinitionSearchParams();
		searchParams.setBroker(broker);
		searchParams.setName(name);
		return customFieldDefinitionDao.loadDefinitions(searchParams).isEmpty();
	}
	
	@Override
	public TBCustomFieldDefinition updateCustomFieldDefinition(Identity doer, TBCustomFieldDefinitionRef definition,
			String identifier, String name, TBCustomFieldType type, boolean displayInTable) {
		TBCustomFieldDefinition reloadedDefinition = getCustomFieldDefinition(definition, true);
		if (reloadedDefinition == null) {
			return null;
		}
		
		String before = TopicBrokerXStream.toXml(reloadedDefinition);
		
		boolean contentChanged = false;
		if (!Objects.equals(reloadedDefinition.getIdentifier(), identifier)) {
			reloadedDefinition.setIdentifier(identifier);
			contentChanged = true;
		}
		if (!Objects.equals(reloadedDefinition.getName(), name)) {
			reloadedDefinition.setName(name);
			contentChanged = true;
		}
		if (!Objects.equals(reloadedDefinition.getType(), type)) {
			reloadedDefinition.setType(type);
			contentChanged = true;
		}
		if (reloadedDefinition.isDisplayInTable() != displayInTable) {
			reloadedDefinition.setDisplayInTable(displayInTable);
			contentChanged = true;
		}
		
		if (contentChanged) {
			reloadedDefinition = customFieldDefinitionDao.updateDefinition(reloadedDefinition);
			
			String after = TopicBrokerXStream.toXml(reloadedDefinition);
			auditLogDao.create(TBAuditLog.Action.cfDefinitionUpdateContent, before, after, doer, reloadedDefinition);
		}
		
		return reloadedDefinition;
	}
	
	@Override
	public void moveCustomFieldDefinition(Identity doer, TBCustomFieldDefinitionRef definition, boolean up) {
		TBCustomFieldDefinition reloadedDefinition = getCustomFieldDefinition(definition, true);
		if (reloadedDefinition == null) return;
		
		int sortOrder = reloadedDefinition.getSortOrder();
		TBCustomFieldDefinition swapDefinition = customFieldDefinitionDao.loadNext(reloadedDefinition, up);
		if (swapDefinition == null) return;
		int swapSortOrder = swapDefinition.getSortOrder();
		
		String before = TopicBrokerXStream.toXml(reloadedDefinition);
		String beforeSwap = TopicBrokerXStream.toXml(swapDefinition);
		
		((TBCustomFieldDefinitionImpl)reloadedDefinition).setSortOrder(swapSortOrder);
		((TBCustomFieldDefinitionImpl)swapDefinition).setSortOrder(sortOrder);
		reloadedDefinition = customFieldDefinitionDao.updateDefinition(reloadedDefinition);
		swapDefinition = customFieldDefinitionDao.updateDefinition(swapDefinition);
		
		String after = TopicBrokerXStream.toXml(reloadedDefinition);
		String afterSwap = TopicBrokerXStream.toXml(swapDefinition);
		
		auditLogDao.create(TBAuditLog.Action.cfDefinitionUpdateSortOrder, before, after, doer, reloadedDefinition);
		auditLogDao.create(TBAuditLog.Action.cfDefinitionUpdateSortOrder, beforeSwap, afterSwap, doer, swapDefinition);
	}
	
	@Override
	public void deleteCustomFieldDefinitionSoftly(Identity doer, TBCustomFieldDefinitionRef definition) {
		TBCustomFieldDefinition reloadedDefinition = getCustomFieldDefinition(definition, true);
		if (reloadedDefinition == null) {
			return;
		}
		
		String before = TopicBrokerXStream.toXml(reloadedDefinition);
		
		((TBCustomFieldDefinitionImpl)reloadedDefinition).setSortOrder(-1);
		((TBCustomFieldDefinitionImpl)reloadedDefinition).setDeletedDate(new Date());
		reloadedDefinition = customFieldDefinitionDao.updateDefinition(reloadedDefinition);
		
		String after = TopicBrokerXStream.toXml(reloadedDefinition);
		auditLogDao.create(TBAuditLog.Action.cfDefinitionDeleteSoftly, before, after, doer, reloadedDefinition);
	}
	
	@Override
	public TBCustomFieldDefinition getCustomFieldDefinition(TBCustomFieldDefinitionRef definition) {
		return getCustomFieldDefinition(definition, true);
	}
	
	private TBCustomFieldDefinition getCustomFieldDefinition(TBCustomFieldDefinitionRef definition, boolean active) {
		TBCustomFieldDefinitionSearchParams searchParams = new TBCustomFieldDefinitionSearchParams();
		searchParams.setDefinition(definition);
		searchParams.setFetchBroker(true);
		if (!active) {
			searchParams.setDeleted(null);
		}
		List<TBCustomFieldDefinition> definitions = getCustomFieldDefinitions(searchParams);
		
		return !definitions.isEmpty()? definitions.get(0): null;
	}
	
	@Override
	public List<TBCustomFieldDefinition> getCustomFieldDefinitions(TBCustomFieldDefinitionSearchParams searchParams) {
		return customFieldDefinitionDao.loadDefinitions(searchParams);
	}

	private TBCustomField createCustomFile(Identity doer, TBCustomFieldDefinitionRef definition, TBTopicRef topic) {
		TBCustomFieldDefinition reloadedDefinition = getCustomFieldDefinition(definition);
		if (reloadedDefinition == null) {
			return null;
		}
		TBTopic reloadedTopic = getTopic(topic);
		if (reloadedTopic == null) {
			return null;
		}
		
		TBCustomField customField = customFieldDao.createCustomField(reloadedDefinition, reloadedTopic);
		String after = TopicBrokerXStream.toXml(customField);
		
		auditLogDao.create(TBAuditLog.Action.customFieldUpdateContent, null, after, doer,
				reloadedDefinition, reloadedTopic);
		
		return customField;
	}
	
	@Override
	public void createOrUpdateCustomField(Identity doer, TBCustomFieldDefinitionRef definition, TBTopicRef topic,
			String text) {
		createOrUpdateCustomField(doer, definition, topic, text, null, null);
	}
	
	private void createOrUpdateCustomField(Identity doer, TBCustomFieldDefinitionRef definition, TBTopicRef topic,
			String text, VFSMetadata vfsMetadata, String filename) {
		TBCustomField reloadedCustomField = getCustomField(definition, topic, false);
		if (reloadedCustomField == null) {
			reloadedCustomField = createCustomFile(doer, definition, topic);
			if (reloadedCustomField == null) {
				return;
			}
		}
		
		String before = TopicBrokerXStream.toXml(reloadedCustomField);
		
		boolean contentChanged = false;
		if (!Objects.equals(reloadedCustomField.getText(), text)) {
			reloadedCustomField.setText(text);
			contentChanged = true;
		}
		if (!Objects.equals(reloadedCustomField.getFilename(), filename)) {
			reloadedCustomField.setFilename(filename);
			contentChanged = true;
		}
		if (!Objects.equals(reloadedCustomField.getVfsMetadata(), vfsMetadata)) {
			((TBCustomFieldImpl)reloadedCustomField).setVfsMetadata(vfsMetadata);
			contentChanged = true;
		}
		
		if (contentChanged) {
			reloadedCustomField = customFieldDao.updateCustomField(reloadedCustomField);
			
			String after = TopicBrokerXStream.toXml(reloadedCustomField);
			auditLogDao.create(TBAuditLog.Action.customFieldUpdateContent, before, after, doer,
					reloadedCustomField.getDefinition(), reloadedCustomField.getTopic());
		}
	}
	
	@Override
	public void createOrUpdateCustomFieldFile(Identity doer, TBTopic topic, TBCustomFieldDefinition definition,
			File uploadFile, String uploadFileName) {
		TBCustomField reloadedCustomField = getCustomField(definition, topic, false);
		if (reloadedCustomField instanceof TBCustomFieldImpl impl) {
			impl.setVfsMetadata(null);
		}
		
		VFSLeaf topicLeaf = storeTopicLeaf(doer, topic, definition.getIdentifier(), uploadFile, uploadFileName);
		createOrUpdateCustomField(doer, definition, topic, null, topicLeaf.getMetaInfo(), uploadFileName);
	}

	@Override
	public void deleteCustomFieldPermanently(Identity doer, TBCustomFieldDefinitionRef definition, TBTopicRef topic) {
		TBCustomField reloadedCustomField = getCustomField(definition, topic, false);
		if (reloadedCustomField == null) {
			return;
		}
		
		String before = TopicBrokerXStream.toXml(reloadedCustomField);
		customFieldDao.deleteCustomField(reloadedCustomField);
		auditLogDao.create(TBAuditLog.Action.customFieldDeletePermanently, before, null, doer,
				reloadedCustomField.getDefinition(), reloadedCustomField.getTopic());
	}
	
	@Override
	public void deleteCustomFieldFilePermanently(Identity doer, TBCustomFieldDefinition definition, TBTopic topic) {
		deleteCustomFieldPermanently(doer, definition, topic);
		deleteTopicLeaf(doer, topic, definition.getIdentifier());
	}
	
	private TBCustomField getCustomField(TBCustomFieldDefinitionRef definition, TBTopicRef topic, boolean active) {
		TBCustomFieldSearchParams searchParams = new TBCustomFieldSearchParams();
		searchParams.setDefinition(definition);
		searchParams.setTopic(topic);
		if (!active) {
			searchParams.setDeletedDefinition(null);
			searchParams.setDeletedTopic(null);
		}
		searchParams.setFetchBroker(true);
		searchParams.setFetchDefinition(true);
		searchParams.setFetchTopic(true);
		searchParams.setFetchIdentities(true);
		List<TBCustomField> customFields = getCustomFields(searchParams);
		
		return !customFields.isEmpty()? customFields.get(0): null;
	}
	
	@Override
	public List<TBCustomField> getCustomFields(TBCustomFieldSearchParams searchParams) {
		return customFieldDao.loadCustomFields(searchParams);
	}
	
	@Override
	public void select(Identity doer, Identity participantIdentity, TBTopicRef topic, Integer sortOrder) {
		TBTopic reloadedTopic = getTopic(topic, true);
		if (reloadedTopic == null) {
			return;
		}
		
		List<TBSelection> selections = getSelections(reloadedTopic.getBroker(), participantIdentity);
		Collections.sort(selections, (s1, s2) -> Integer.compare(s1.getSortOrder(), s2.getSortOrder()));
		
		TBSelection selection = selections.stream()
				.filter(s -> s.getTopic().getKey().equals(reloadedTopic.getKey()))
				.findFirst()
				.orElseGet(() -> null);
		
		// Determine the real sort order
		int highestExistingSortOrder = 0;
		if (!selections.isEmpty()) {
			Collections.sort(selections, (s1, s2) -> Integer.compare(s1.getSortOrder(), s2.getSortOrder()));
			highestExistingSortOrder = selections.get(selections.size()-1).getSortOrder();
		}
		
		int realSortOrder;
		if (sortOrder == null) {
			realSortOrder = highestExistingSortOrder + 1;
			if (selection != null) {
				realSortOrder = realSortOrder - 1;
			}
		} else if (highestExistingSortOrder < sortOrder) {
			realSortOrder = highestExistingSortOrder + 1;
		} else {
			realSortOrder = sortOrder;
		}
		
		if (selection != null && realSortOrder == selection.getSortOrder()) {
			// Selection exists and has the right sort order.
			return;
		}
		
		if (selection == null) {
			TBParticipant participant = getOrCreateParticipant(doer, reloadedTopic.getBroker(), participantIdentity, true);
			selection = selectionDao.createSelection(doer, participant, reloadedTopic, realSortOrder);
			
			String after = TopicBrokerXStream.toXml(selection);
			auditLogDao.create(TBAuditLog.Action.selectionCreate, null, after, doer, selection);
		} else {
			// Remove the selection to add it later again at the right order.
			Long selectionKey = selection.getKey();
			selections = selections.stream()
					.filter(s -> !selectionKey.equals(s.getKey()))
					.collect(Collectors.toList());
		}
		
		selections.add(realSortOrder-1, selection);
		
		updateSortOrders(doer, selections);
	}
	
	@Override
	public void unselect(Identity doer, Identity participantIdentity, TBTopicRef topic) {
		TBTopic reloadedTopic = getTopic(topic, true);
		if (reloadedTopic == null) {
			return;
		}
		
		TBSelection selection = getSelection(participantIdentity, topic);
		if (selection == null) {
			return;
		}
		
		unselect(doer, participantIdentity, reloadedTopic, selection);
	}

	private void unselect(Identity doer, Identity participantIdentity, TBTopic reloadedTopic, TBSelection selection) {
		if (selection.isEnrolled()) {
			withdraw(doer, selection, false);
			selection = getSelection(participantIdentity, reloadedTopic);
		}
		
		String before = TopicBrokerXStream.toXml(selection);
		auditLogDao.create(TBAuditLog.Action.selectionDelete, before, null, doer, selection);
		selectionDao.deleteSelection(selection);
		
		List<TBSelection> selections = getSelections(reloadedTopic.getBroker(), participantIdentity);
		Collections.sort(selections, (s1, s2) -> Integer.compare(s1.getSortOrder(), s2.getSortOrder()));
		updateSortOrders(doer, selections);
	}
	
	@Override
	public void moveSelection(Identity doer, Identity participantIdentity, TBTopicRef topic, boolean up) {
		TBTopic reloadedTopic = getTopic(topic, true);
		if (reloadedTopic == null) {
			return;
		}
		
		TBSelection selection = getSelection(participantIdentity, topic);
		if (selection == null) {
			return;
		}
		if (up && selection.getSortOrder() == 1) {
			// Is already on top
			return;
		}
		
		List<TBSelection> selections = getSelections(reloadedTopic.getBroker(), participantIdentity);
		if (!up && selection.getSortOrder() == selections.size() + 1) {
			// Is already at bottom
			return;
		}
		
		int sortOrder = up? selection.getSortOrder() - 1: selection.getSortOrder() + 1;
		selection = updateSortOrder(doer, selection, sortOrder);
		
		Collections.sort(selections, (s1, s2) -> Integer.compare(s1.getSortOrder(), s2.getSortOrder()));
		
		Long selectionKey = selection.getKey();
		selections = selections.stream()
				.filter(s -> !selectionKey.equals(s.getKey()))
				.collect(Collectors.toList());	
		selections.add(sortOrder - 1, selection);
		
		updateSortOrders(doer, selections);
	}

	private void updateSortOrders(Identity doer, List<TBSelection> selections) {
		for (int i = 1; i <= selections.size(); i++) {
			TBSelection selectionToReorder = selections.get(i-1);
			if (selectionToReorder.getSortOrder() != i) {
				updateSortOrder(doer, selectionToReorder, i);
			}
		}
	}

	private TBSelection updateSortOrder(Identity doer, TBSelection selection, int sortOrder) {
		String before = TopicBrokerXStream.toXml(selection);
		((TBSelectionImpl)selection).setSortOrder(sortOrder);
		selection = selectionDao.updateSelection(selection);
		String after = TopicBrokerXStream.toXml(selection);
		auditLogDao.create(TBAuditLog.Action.selectionUpdateSortOrder, before, after, doer, selection);
		return selection;
	}
	
	@Override
	public void enroll(Identity doer, Identity participantIdentity, TBTopicRef topic, boolean process) {
		TBSelection selection = getSelection(participantIdentity, topic);
		if (selection == null) {
			select(doer, participantIdentity, topic, null);
			selection = getSelection(participantIdentity, topic);
		}
		
		String before = TopicBrokerXStream.toXml(selection);
		((TBSelectionImpl)selection).setEnrolled(true);
		selection = selectionDao.updateSelection(selection);
		String after = TopicBrokerXStream.toXml(selection);
		auditLogDao.create(getSelectionEnrollAction(doer, process), before, after, doer, selection);
	}
	
	private TBAuditLog.Action getSelectionEnrollAction(Identity doer, boolean proccess) {
		if (proccess) {
			if (doer == null) {
				return TBAuditLog.Action.selectionEnrollProcessAuto;
			}
			return TBAuditLog.Action.selectionEnrollProcessMan;
		}
		
		return TBAuditLog.Action.selectionEnrollManually;
	}
	
	@Override
	public void withdraw(Identity doer, IdentityRef participantIdentity, TBTopicRef topic, boolean process) {
		TBSelection selection = getSelection(participantIdentity, topic);
		if (selection == null) {
			return;
		}
		
		withdraw(doer, selection, process);
	}

	private void withdraw(Identity doer, TBSelection selection, boolean process) {
		String before = TopicBrokerXStream.toXml(selection);
		((TBSelectionImpl)selection).setEnrolled(false);
		selection = selectionDao.updateSelection(selection);
		String after = TopicBrokerXStream.toXml(selection);
		auditLogDao.create(getSelectionWithdrawAction(doer, process), before, after, doer, selection);
	}
	
	private TBAuditLog.Action getSelectionWithdrawAction(Identity doer, boolean proccess) {
		if (proccess) {
			if (doer == null) {
				return TBAuditLog.Action.selectionWithdrawProcessAuto;
			}
			return TBAuditLog.Action.selectionWithdrawProcessMan;
		}
		
		return TBAuditLog.Action.selectionWithdrawManually;
	}
	
	@Override
	public void enrollAutomatically() {
		TBBrokerSearchParams searchParams = new TBBrokerSearchParams();
		searchParams.setAutoEnrollment(Boolean.TRUE);
		searchParams.setEnrollmentStartNull(Boolean.TRUE);
		searchParams.setSelectionEndDateBefore(new Date());
		List<TBBroker> brokers = brokerDao.loadBrokers(searchParams);
		
		brokers.forEach(this::enrollAutomatically);
	}

	// The status check and the candidates actually belong in a provider.
	private void enrollAutomatically(TBBroker broker) {
		updateEnrollmentProcessStart(null, broker);
		dbInstance.commitAndCloseSession();
		
		// Update the dates without running the enrollment process to avoid getting the
		// same broker during the next job run if repository entry is not in a right status.
		RepositoryEntry repositoryEntry = repositoryService.loadByKey(broker.getRepositoryEntry().getKey());
		if (repositoryEntry != null && repositoryEntry.getEntryStatus().decommissioned()) {
			
			TopicBrokerCourseNodeParticipantCandidates participantCandidates = new TopicBrokerCourseNodeParticipantCandidates(null, repositoryEntry, true);
			
			TBSelectionSearchParams selectionSearchParams = new TBSelectionSearchParams();
			selectionSearchParams.setBroker(broker);
			selectionSearchParams.setEnrolledOrIdentities(participantCandidates.getAllIdentities());
			selectionSearchParams.setEnrolledOrMaxSortOrder(broker.getMaxSelections());
			selectionSearchParams.setFetchIdentity(true);
			List<TBSelection> selections = getSelections(selectionSearchParams);
			
			TBTopicSearchParams topicSearchParams = new TBTopicSearchParams();
			topicSearchParams.setBroker(broker);
			List<TBTopic> topics = getTopics(topicSearchParams);
			
			new DefaultEnrollmentProcess(broker, topics, selections).persist(null);
		}
		updateEnrollmentProcessDone(null, broker);
		dbInstance.commitAndCloseSession();
	}
	
	private TBSelection getSelection(IdentityRef identity, TBTopicRef topic) {
		TBSelectionSearchParams searchParams = new TBSelectionSearchParams();
		searchParams.setIdentity(identity);
		searchParams.setTopic(topic);
		searchParams.setFetchIdentity(true);
		searchParams.setFetchTopic(true);
		searchParams.setFetchBroker(true);
		List<TBSelection> selections = getSelections(searchParams);
		
		return !selections.isEmpty()? selections.get(0): null;
	}
	
	public List<TBSelection> getSelections(TBBrokerRef broker, Identity identity) {
		TBSelectionSearchParams searchParams = new TBSelectionSearchParams();
		searchParams.setBroker(broker);
		searchParams.setIdentity(identity);
		searchParams.setFetchIdentity(true);
		searchParams.setFetchTopic(true);
		searchParams.setFetchBroker(true);
		return getSelections(searchParams);
	}

	@Override
	public List<TBSelection> getSelections(TBSelectionSearchParams searchParams) {
		return selectionDao.loadSelections(searchParams);
	}

	@Override
	public TBEnrollmentStats getEnrollmentStats(TBBroker broker, List<Identity> identities,
			List<TBParticipant> participants, List<TBSelection> selections) {
		return new TBEnrollmentStatsCalculation(broker, identities, participants, selections);
	}
	
	@Override
	public void log(TBAuditLog.Action action, String before, String after, Identity doer, TBBroker broker,
			TBParticipant participant, TBTopic topic, TBSelection selection) {
		auditLogDao.create(action, before, after, doer, broker, participant, topic, null, selection);
	}
	
	@Override
	public List<TBAuditLog> getAuditLog(TBAuditLogSearchParams searchParams, int firstResult, int maxResults) {
		return auditLogDao.loadAuditLogs(searchParams, firstResult, maxResults);
	}

}
