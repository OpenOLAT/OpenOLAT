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

import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;

/**
 * Initial Date: 18.03.2020
 * @author aboeckle, alexander.boeckle@frentix.com, www.frentix.com
 */
public class SendMailGroupOption implements SendMailOption {

	private final List<GroupRoles> roles;
	private BusinessGroup businessGroup;
	
	public SendMailGroupOption(BusinessGroup businessGroup, List<GroupRoles> roles) {
		this.roles = roles;
		this.businessGroup = businessGroup;
	}

	@Override
	public String getOptionKey() {
		return "send-mail-group-" + businessGroup.getKey();
	}

	@Override
	public String getOptionName() {
		return businessGroup.getName();
	}

	@Override
	public List<Identity> getSelectedIdentities() {
		String[] rolesArray = new String[roles.size()];
		for (int i = 0; i < roles.size(); i++) {
			rolesArray[i] = roles.get(i).name();
		}
		
		return CoreSpringFactory.getImpl(BusinessGroupService.class).getMembers(businessGroup, rolesArray);
	}
}
