/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.lecture;

import java.util.List;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.model.LectureBlockAndRollCall;
import org.olat.modules.lecture.model.LectureStatistics;
import org.olat.modules.lecture.model.ParticipantLectureStatistics;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface LectureService {
	
	
	/**
	 * Get (or create) the configuration object for the specified repossitory
	 * entry.
	 * 
	 * @param entry
	 * @return A persisted configuration
	 */
	public RepositoryEntryLectureConfiguration getRepositoryEntryLectureConfiguration(RepositoryEntry entry);
	
	/**
	 * Update the specified configuration
	 * @param config The configuration to merge
	 * @return A merged configuration
	 */
	public RepositoryEntryLectureConfiguration updateRepositoryEntryLectureConfiguration(RepositoryEntryLectureConfiguration config);
	
	/**
	 * Create but not persist a new lecture block.
	 * 
	 * @param entry The repository entry which own the block
	 * @return A new lecture block
	 */
	public LectureBlock createLectureBlock(RepositoryEntry entry);
	
	/**
	 * Merge or persist the specified lecture block and return
	 * the fresh block.
	 * 
	 * @param lectureBlock The block to merge or persist
	 * @return The merged block
	 */
	public LectureBlock save(LectureBlock lectureBlock, List<Group> groups);
	
	/**
	 * Reload the lecture block.
	 * @param block
	 * @return A fresh lecture block
	 */
	public LectureBlock getLectureBlock(LectureBlockRef block);
	
	/**
	 * Returns all configured reasons.
	 * 
	 * @return A list of reasons
	 */
	public List<Reason> getAllReasons();
	
	/**
	 * Load a reason by its primary key.
	 * 
	 * @param key The primary key
	 * @return A reason
	 */
	public Reason getReason(Long key);
	
	/**
	 * 
	 * @param title
	 * @param description
	 * @return
	 */
	public Reason createReason(String title, String description);
	
	/**
	 * Updates the reason and return the freshest.
	 * 
	 * @param reason The reaosn to update
	 * @return A merged reason
	 */
	public Reason updateReason(Reason reason);

	
	/**
	 * Lists the base groups attached to the specified lecture block.
	 * 
	 * @param block A lecture block
	 * @return A list of groups
	 */
	public List<Group> getLectureBlockToGroups(LectureBlockRef block);
	
	/**
	 * Returns the list of participants of a lecture block.
	 * 
	 * @param block The lecture block
	 * @return A list of identities
	 */
	public List<Identity> getParticipants(LectureBlockRef block);
	
	/**
	 * Return all possible participants in a lecture block linked to the specified
	 * repository.
	 * 
	 * @param entry The course / repository entry
	 * @return A list of identities
	 */
	public List<Identity> getParticipants(RepositoryEntry entry);
	
	/**
	 * Return all possible participants in a lecture block linked to the specified
	 * repository entry and where the specified teacher is linked.
	 * 
	 * @param entry The course / repository entry
	 * @param teacher The teacher
	 * @return A list of identities
	 */
	public List<Identity> getParticipants(RepositoryEntry entry, Identity teacher);
	
	
	/**
	 * The method will start the roll call, generate all roll call, generate missing
	 * summaries...
	 * 
	 * @param teacher
	 * @param lectureblock
	 * @return The list of participants
	 */
	public List<Identity> startLectureBlock(Identity teacher, LectureBlock lectureblock);
	
	
	public List<LectureBlockRollCall> getRollCalls(LectureBlockRef block);
	
	/**
	 * Create a roll call with some settings.
	 * 
	 * @param identity The participant of the lecture block
	 * @param lectureBlock The lecture block
	 * @param authorizedAbsence If there are authorized absence
	 * @return A new persisted roll call
	 */
	public LectureBlockRollCall getOrCreateRollCall(Identity identity, LectureBlock lectureBlock,
			Boolean authorizedAbsence, String absenceReason);
	
	/**
	 * Standard merge
	 * 
	 * @param rollCall The lecture block roll call
	 * @return The merge roll call
	 */
	public LectureBlockRollCall updateRollCall(LectureBlockRollCall rollCall);
	
	/**
	 * Add the specified lectures to the ones the identity follows.
	 * 
	 * @param identity The participant of the lecture
	 * @param lectureBlock The lecture block
	 * @param roolCall The roll call (optional)
	 * @param absences The lectures where the participant was absent
	 * @return The updated roll call
	 */
	public LectureBlockRollCall addRollCall(Identity identity, LectureBlock lectureBlock, LectureBlockRollCall rollCall, List<Integer> absences);
	
	/**
	 * 
	 * @param identity
	 * @param lectureBlock
	 * @param rollCall
	 * @param comment
	 * @param absences
	 * @return
	 */
	public LectureBlockRollCall addRollCall(Identity identity, LectureBlock lectureBlock, LectureBlockRollCall rollCall,
			String comment, List<Integer> absences);
	
	/**
	 * Remove the specified lectures to the ones the identity follows.
	 * 
	 * @param identity The participant of the lecture
	 * @param lectureBlock The lecture block
	 * @param rollCall The roll call (optional)
	 * @param absences The lectures to remove from the "absence" list
	 * @return The updated roll call
	 */
	public LectureBlockRollCall removeRollCall(Identity identity, LectureBlock lectureBlock, LectureBlockRollCall rollCall, List<Integer> absences);

	
	/**
	 * Will close all lecture block which end date are after the
	 * setting.
	 * 
	 */
	public void autoCloseRollCall();
	
	public void sendReminders();
	
	
	/**
	 * 
	 * @param entry
	 * @return
	 */
	public List<LectureBlock> getLectureBlocks(RepositoryEntryRef entry);
	
	/**
	 * Returns the lecture block for the specified learning resource
	 * and the specified identity which has the "teacher" role for the
	 * block.
	 * 
	 * @param entry A repository entry
	 * @return A list of lecture blocks
	 */
	public List<LectureBlock> getLectureBlocks(RepositoryEntryRef entry, IdentityRef teacher);

	/**
	 * This method will check 2 things. First is the lectures for the specified
	 * repository entry enabled and if it is the case, it will checks that the
	 * identity is a teacher in at least one lecture block.
	 * 
	 * @param entry The course / repository entry
	 * @param identity The identity as teacher
	 * @return true if the lecture is enabled and the identity a teach
	 */
	public boolean hasLecturesAsTeacher(RepositoryEntryRef entry, Identity identity);
	
	public List<Identity> getTeachers(LectureBlock block);
	
	public void addTeacher(LectureBlock block, Identity teacher);
	
	public void removeTeacher(LectureBlock block, Identity teacher);
	
	/**
	 * The method will not set the date of admission.
	 * 
	 * @param entry
	 * @param identity
	 * @return
	 */
	public LectureParticipantSummary getOrCreateParticipantSummary(RepositoryEntry entry, Identity identity);
	
	
	public LectureParticipantSummary saveParticipantSummary(LectureParticipantSummary summary);
	
	
	/**
	 * Returns the statistics for the specified participant.
	 * 
	 * @param identity The participant
	 * @return A list of statistics
	 */
	public List<LectureStatistics> getParticipantLecturesStatistics(IdentityRef identity);
	
	/**
	 * Return all the statistics for a course / repository entry.
	 * 
	 * @param entry The course / repository entry
	 * @return Statistics per user
	 */
	public List<ParticipantLectureStatistics> getParticipantsLecturesStatistics(RepositoryEntryRef entry);
	
	/**
	 * 
	 * @param entry
	 * @param participant
	 * @return
	 */
	public List<LectureBlockAndRollCall> getParticipantLectureBlocks(RepositoryEntryRef entry, IdentityRef participant);

}
