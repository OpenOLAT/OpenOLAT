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
import org.olat.modules.lecture.model.LectureStatistics;
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
	
	public LectureBlock getLectureBlock(LectureBlockRef block);
	
	/**
	 * Lists the base groups attached to the specified lecture block.
	 * 
	 * @param block A lecture block
	 * @return A list of groups
	 */
	public List<Group> getLectureBlockToGroups(LectureBlockRef block);
	
	
	public List<Identity> getParticipants(LectureBlockRef block);
	
	public List<LectureBlockRollCall> getRollCalls(LectureBlockRef block);
	
	/**
	 * Create a roll call with some settings.
	 * 
	 * @param identity The participant of the lecture block
	 * @param lectureBlock The lecture block
	 * @param authorizedAbsence If there are authorized absence
	 * @return A new persisted roll call
	 */
	public LectureBlockRollCall createRollCall(Identity identity, LectureBlock lectureBlock, Boolean authorizedAbsence);
	
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
	 * @param lecturesAttendee The lectures where the participant was present
	 * @return The updated roll call
	 */
	public LectureBlockRollCall addRollCall(Identity identity, LectureBlock lectureBlock, LectureBlockRollCall rollCall, Integer... lecturesAttendee);
	
	/**
	 * Remove the specified lectures to the ones the identity follows.
	 * 
	 * @param identity The participant of the lecture
	 * @param lectureBlock The lecture block
	 * @param rollCall The roll call (optional)
	 * @param lecturesAttendee The lectures to remove from the "present" list
	 * @return The updated roll call
	 */
	public LectureBlockRollCall removeRollCall(Identity identity, LectureBlock lectureBlock, LectureBlockRollCall rollCall, Integer... lecturesAttendee);
	
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
	 * 
	 * @param entry
	 * @param identity
	 * @return
	 */
	public boolean hasLecturesAsTeacher(RepositoryEntryRef entry, Identity identity);
	
	public List<Identity> getTeachers(LectureBlock block);
	
	public void addTeacher(LectureBlock block, Identity teacher);
	
	public void removeTeacher(LectureBlock block, Identity teacher);
	
	
	/**
	 * Returns the statistics for the specified participant.
	 * 
	 * @param identity The participant
	 * @return A list of statistics
	 */
	public List<LectureStatistics> getParticipantLecturesStatistics(IdentityRef identity);

}
