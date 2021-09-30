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
package org.olat.course.run.scoring;

import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.curriculum.CurriculumElementRef;

/**
 * 
 * Initial date: 2 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ObligationContext {

	/**
	 * Checks whether the identity is participant of the BusinessGroup or not.
	 * 
	 * @param identitiy 
	 * @param businessGroupRef
	 * @return
	 */
	boolean isParticipant(Identity identity, BusinessGroupRef businessGroupRef);
	
	/**
	 * Checks whether the identity is member of the Organisation or not.
	 * 
	 *
	 * @param identitiy
	 * @param organisationRef
	 * @return
	 */
	boolean isMember(Identity identity, OrganisationRef organisationRef);

	/**
	 * Checks whether the identity is participant of the CurriculumElement or not.
	 *
	 * @param identity
	 * @param curriculumElementRef
	 * @return
	 */
	boolean isParticipant(Identity identity, CurriculumElementRef curriculumElementRef);

}
