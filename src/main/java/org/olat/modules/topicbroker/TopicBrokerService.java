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
package org.olat.modules.topicbroker;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 29 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface TopicBrokerService {
	
	public static final String TEASER_IMAGE_DIR = "topicteaserimage";
	public static final int TEASER_IMAGE_MAX_SIZE_KB = 2048;
	public static final Set<String> TEASER_IMAGE_MIME_TYPES = Set.of("image/gif", "image/jpg", "image/jpeg", "image/png");
	public static final String TEASER_VIDEO_DIR = "topicteaservideo";
	public static final int TEASER_VIDEO_MAX_SIZE_KB = 102400;
	public static final Set<String> TEASER_VIDEO_MIME_TYPES = Set.of("video/mp4");
	public static final int CUSTOM_FILE_MAX_SIZE_KB = 10240; // 10MB
	
	
	public TBBroker createBroker(Identity doer, RepositoryEntry repositoryEntry, String subIdent);

	public void deleteBroker(RepositoryEntry courseEntry, String ident);
	
	public TBBroker updateBroker(Identity doer, TBBrokerRef broker, Integer maxSelections, Date selectionStartDate,
			Date selectionEndDate, Integer requiredEnrollments, boolean participantCanEditRequiredEnrollments,
			boolean autoEnrollment, TBEnrollmentStrategyType autoEnrollmentStrategyType,
			boolean overlappingPeriodAllowed, boolean participantCanWithdraw, Date withdrawEndDate);

	public void updateEnrollmentProcessStart(Identity doer, TBBrokerRef broker);
	
	public void updateEnrollmentProcessDone(Identity doer, TBBrokerRef broker, boolean sendEmails);
	
	public void resetEnrollmentProcessStatus(Identity doer, TBBrokerRef broker);
	
	public void sendEnrollmentEmails(TBBroker broker, List<Identity> identities);
	
	public TBBroker getOrCreateBroker(Identity doer, RepositoryEntry repositoryEntry, String subIdent);

	public TBBroker getBroker(RepositoryEntry repositoryEntry, String subIdent);
	
	public TBBroker getBroker(TBBrokerRef broker);
	
	public VFSContainer getBrokerContainer(TBBrokerRef broker);
	
	public TBParticipant updateParticipant(Identity identity, TBParticipant participant);
	
	public TBParticipant getOrCreateParticipant(Identity doer, TBBroker broker, Identity participantIdentity);
	
	public List<TBParticipant> getParticipants(TBParticipantSearchParams participantSearchParams);
	
	public Set<Long> filterMembership(IdentityRef identity, Collection<Long> groupKeys);

	public List<TBGroupRestrictionInfo> getGroupRestrictionInfos(Translator translator, Set<Long> businessGroupKeys);

	public TBTopic createTopic(Identity doer, TBBrokerRef broker);
	
	public boolean isTopicIdentifierAvailable(TBBrokerRef broker, String identifier);
	
	public boolean isTopicIdentifierValid(String identifier);
	
	public TBTopic updateTopic(Identity doer, TBTopicRef topic, String identifier, String title, String description,
			Date beginDate, Date endDate, Integer minParticipants, Integer maxParticipants,
			Set<Long> groupRestricionKeys);
	
	public void moveTopic(Identity doer, TBTopicRef topic, boolean up);
	
	/**
	 * Updates the sort order of the topics. Prerequisite: The orderedIdentificators
	 * must contain the identifiers of all existing themes. If this is not the case,
	 * not a single sortOrder will be updated.
	 * 
	 * @param doer
	 * @param broker
	 * @param orderedIdentificators the new order of the topics
	 */
	public void updateTopicSortOrder(Identity doer, TBBrokerRef broker, List<String> orderedIdentificators);
	
	public void deleteTopicSoftly(Identity doer, TBTopicRef topic);
	
	public TBTopic getTopic(TBTopicRef topic);
	
	public List<TBTopic> getTopics(TBTopicSearchParams searchParams);
	
	public VFSLeaf storeTopicLeaf(Identity doer, TBTopicRef topic, String identifier, File file, String filename);
	
	public void deleteTopicLeaf(Identity doer, TBTopicRef topic, String identifier);
	
	public VFSLeaf getTopicLeaf(TBTopic topic, String identifier);
	
	public TBCustomFieldDefinition createCustomFieldDefinition(Identity doer, TBBrokerRef broker);

	public boolean isCustomFieldDefinitionNameAvailable(TBBroker broker, String name);
	
	public TBCustomFieldDefinition updateCustomFieldDefinition(Identity doer, TBCustomFieldDefinitionRef definition,
			String identifier, String name, TBCustomFieldType type, boolean displayInTable);
	
	public void moveCustomFieldDefinition(Identity doer, TBCustomFieldDefinitionRef definition, boolean up);
	
	public void deleteCustomFieldDefinitionSoftly(Identity doer, TBCustomFieldDefinitionRef definition);
	
	public TBCustomFieldDefinition getCustomFieldDefinition(TBCustomFieldDefinitionRef definition);
	
	public List<TBCustomFieldDefinition> getCustomFieldDefinitions(TBCustomFieldDefinitionSearchParams searchParams);

	public void createOrUpdateCustomField(Identity doer, TBCustomFieldDefinitionRef definition, TBTopicRef topic,
			String text);
	
	public void createOrUpdateCustomFieldFile(Identity doer, TBTopic topic, TBCustomFieldDefinition definition,
			File uploadFile, String uploadFileName);

	public void deleteCustomFieldPermanently(Identity doer, TBCustomFieldDefinitionRef definition, TBTopicRef topic);

	public void deleteCustomFieldFilePermanently(Identity doer, TBCustomFieldDefinition definition, TBTopic topic);
	
	public List<TBCustomField> getCustomFields(TBCustomFieldSearchParams searchParams);
	
	/**
	 * Create a selection.
	 * 
	 * @param doer
	 * @param participantIdentity the identity which is assigned to the topic
	 * @param topic the selected topic
	 * @param sortOrder the sort order (priority)
	 *                  The sort order of all existing selections with an equal or higher sort order are increased by one.
	 *                  May be null. Then the selection is added at the end.
	 */
	public void select(Identity doer, Identity participantIdentity, TBTopicRef topic, Integer sortOrder);
	
	public void unselect(Identity doer, Identity participantIdentity, TBTopicRef topic);

	public void moveSelection(Identity doer, Identity participantIdentity, TBTopicRef topic, boolean up);

	public void enroll(Identity doer, Identity participantIdentity, TBTopicRef topic, boolean process);
	
	public void withdraw(Identity doer, IdentityRef participantIdentity, TBTopicRef topic, boolean process);

	public void enrollAutomatically();

	public List<TBSelection> getSelections(TBSelectionSearchParams searchParams);

	public TBEnrollmentStats getEnrollmentStats(TBBroker broker, List<Identity> identities,
			List<TBParticipant> participants, List<TBTopic> topics, List<TBSelection> selections);
	
	public TBEnrollmentProcessor createProcessor(TBBroker broker, List<TBTopic> topics, List<TBSelection> selections,
			TBEnrollmentStrategy strategy, List<TBEnrollmentStrategy> debugStrategies);
	
	public void log(TBAuditLog.Action action, String before, String after, Identity doer, TBBroker broker,
			TBParticipant participant, TBTopic topic, TBSelection selection);
	
	public List<Identity> getAuditLogDoers(TBAuditLogSearchParams searchParams);
	
	public List<TBAuditLog> getAuditLog(TBAuditLogSearchParams searchParams, int firstResult, int maxResults);

}
