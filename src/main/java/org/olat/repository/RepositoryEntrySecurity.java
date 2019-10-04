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
package org.olat.repository;

/**
 * 
 * Initial date: 2 Oct 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface RepositoryEntrySecurity {

	boolean isOwner();

	/**
	 * 
	 * @return true if the user is coach of the repository entry, a group
	 *   or a curriculum element linked to the repository entry.
	 */
	boolean isCoach();

	/**
	 * 
	 * @return true if the user is participant of the repository entry, a group
	 *   or a curriculum element linked to the repository entry.
	 */
	boolean isParticipant();

	boolean isMasterCoach();

	boolean isEntryAdmin();

	boolean canLaunch();

	boolean isReadOnly();

	boolean isCourseParticipant();

	boolean isCourseCoach();

	boolean isGroupParticipant();

	boolean isGroupCoach();

	boolean isGroupWaiting();

	boolean isCurriculumParticipant();

	boolean isCurriculumCoach();

	boolean isMember();

	/**
	 * @return true if the user has the role author in an organization
	 * 		linked by the repository entry
	 */
	boolean isAuthor();

	/**
	 * @return true if the user has the role principal
	 */
	boolean isPrincipal();

	/**
	 * @return true if the user has the role principal but
	 * 		is not a member or an administrator of the repository
	 * 		entry.
	 */
	boolean isOnlyPrincipal();

	boolean isOnlyMasterCoach();

}