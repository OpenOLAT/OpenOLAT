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
import java.util.Locale;
import java.util.Objects;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;

/**
 * Initial Date: 18.03.2020
 *
 * @author aboeckle, alexander.boeckle@frentix.com, www.frentix.com
 */
public class SendMailGroupOption implements SendMailOption {

	private final GroupRoles role;
	private final BusinessGroup businessGroup;
	private final String label;

	public SendMailGroupOption(BusinessGroup businessGroup, GroupRoles role, Locale locale) {
		this.role = role;
		this.businessGroup = businessGroup;
		String key = role == GroupRoles.coach
				? "wizard.step1.send_option.group.coach"
				: "wizard.step1.send_option.group.participant";
		this.label = Util.createPackageTranslator(SendMailGroupOption.class, locale)
				.translate(key, StringHelper.escapeHtml(businessGroup.getName()));
	}

	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}

	public GroupRoles getRole() {
		return role;
	}

	@Override
	public String getOptionKey() {
		return "send-mail-group-" + role.name() + "-" + businessGroup.getKey();
	}

	@Override
	public String getOptionName() {
		return label + " (" + getSelectedIdentities().size() + ")";
	}

	@Override
	public List<Identity> getSelectedIdentities() {
		return Objects.requireNonNull(CoreSpringFactory.getImpl(BusinessGroupService.class)).getMembers(businessGroup, role.name());
	}
}
