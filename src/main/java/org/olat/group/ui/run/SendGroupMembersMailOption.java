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
package org.olat.group.ui.run;

import java.util.List;
import java.util.Locale;

import org.olat.commons.info.ui.SendMailOption;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;

/**
 * Initial Date: 15.03.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class SendGroupMembersMailOption implements SendMailOption {

	private BusinessGroupService groupService;
	private BusinessGroup businessGroup;
	
	
	public SendGroupMembersMailOption(BusinessGroupService groupService, BusinessGroup businessGroup) {
		this.groupService = groupService;
		this.businessGroup = businessGroup;
	}

	@Override
	public String getOptionKey() {
		return "send-mail-group-members";
	}

	@Override
	public String getOptionTranslatedName(Locale locale) {
		Translator translator = Util.createPackageTranslator(SendGroupMembersMailOption.class, locale);
		return translator.translate("wizard.step1.send_option.member");
	}

	@Override
	public List<Identity> getSelectedIdentities() {
		List<Identity> groupMembers = groupService.getMembers(businessGroup);
		return groupMembers;
	}

}
