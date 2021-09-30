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
package org.olat.course.learningpath.obligation;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.course.run.scoring.ObligationContext;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.curriculum.CurriculumElementRef;

/**
 * 
 * Initial date: 20 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TestingObligationContext implements ObligationContext {
	
	private List<BusinessGroupRef> businessGroupRefs = new ArrayList<>();
	private List<OrganisationRef> organisationRefs = new ArrayList<>();
	private List<CurriculumElementRef> curriculumElementRefs = new ArrayList<>();
	
	public void addBusinessGroupRef(BusinessGroupRef businessGroupRef) {
		businessGroupRefs.add(businessGroupRef);
	}
	
	public void addOrganisationRef(OrganisationRef organisationRef) {
		organisationRefs.add(organisationRef);
	}
	
	public void addCurriculumElementRef(CurriculumElementRef curriculumElementRef) {
		curriculumElementRefs.add(curriculumElementRef);
	}
	
	@Override
	public boolean isParticipant(Identity identity, BusinessGroupRef businessGroupRef) {
		return businessGroupRefs.stream().anyMatch(ref -> ref.getKey().equals(businessGroupRef.getKey()));
	}

	@Override
	public boolean isMember(Identity identity, OrganisationRef organisationRef) {
		return organisationRefs.stream().anyMatch(ref -> ref.getKey().equals(organisationRef.getKey()));
	}

	@Override
	public boolean isParticipant(Identity identity, CurriculumElementRef curriculumElementRef) {
		return curriculumElementRefs.stream().anyMatch(ref -> ref.getKey().equals(curriculumElementRef.getKey()));
	}

}
