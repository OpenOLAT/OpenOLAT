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
package org.olat.commons.info.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.SearchMemberParameters;

/**
 * Initial Date: 18.03.2020
 * @author aboeckle, alexander.boeckle@frentix.com, www.frentix.com
 */
public class SendMailCurriculumOption implements SendMailOption {

	private final List<CurriculumRoles> roles;
	private CurriculumElement curriculumElement;
	
	public SendMailCurriculumOption(CurriculumElement curriculumElement, List<CurriculumRoles> roles) {
		this.roles = roles;
		this.curriculumElement = curriculumElement;
	}

	@Override
	public String getOptionKey() {
		return "send-mail-curriculum-" + curriculumElement.getKey();
	}

	@Override
	public String getOptionName() {
		return curriculumElement.getDisplayName();
	}

	@Override
	public List<Identity> getSelectedIdentities() {
		SearchMemberParameters params = new SearchMemberParameters();
		params.setRoles(roles);
		
		List<CurriculumMember> curriculaMembers = CoreSpringFactory.getImpl(CurriculumService.class).getMembers(curriculumElement, params);
		
		if (curriculaMembers == null || curriculaMembers.isEmpty()) {
			return null;
		}
		
		return curriculaMembers.stream().map(member -> member.getIdentity()).collect(Collectors.toList());
	}
}
