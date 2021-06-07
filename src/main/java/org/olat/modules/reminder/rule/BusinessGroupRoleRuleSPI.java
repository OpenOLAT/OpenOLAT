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
package org.olat.modules.reminder.rule;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.BusinessGroupRefImpl;
import org.olat.group.model.BusinessGroupReference;
import org.olat.modules.reminder.IdentitiesProviderRuleSPI;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.ui.BusinessGroupRoleEditor;
import org.olat.modules.reminder.ui.ReminderAdminController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 08.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BusinessGroupRoleRuleSPI implements IdentitiesProviderRuleSPI {

	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	
	@Override
	public int getSortValue() {
		return 100;
	}
	
	@Override
	public String getLabelI18nKey() {
		return "rule.group.member";
	}
	
	@Override
	public String getStaticText(ReminderRule rule, RepositoryEntry entry, Locale locale) {
		if (rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			Translator translator = Util.createPackageTranslator(ReminderAdminController.class, locale);
			translator = Util.createPackageTranslator(BusinessGroupRoleEditor.class, locale, translator);
			String groupKey = r.getRightOperand();
			
			BusinessGroup group = null;
			if (StringHelper.isLong(groupKey)) {
				Long key = Long.parseLong(groupKey);
				group = businessGroupService.loadBusinessGroup(key);
			}
			
			String groupName = group != null ? group.getName() : translator.translate("missing.value");
			translator.translate("rule.group.member.text", new String[] { groupName });
		}
		return null;
	}

	@Override
	public RuleEditorFragment getEditorFragment(ReminderRule rule, RepositoryEntry entry) {
		return new BusinessGroupRoleEditor(rule, entry);
	}
	
	@Override
	public ReminderRule clone(ReminderRule rule, CourseEnvironmentMapper envMapper) {
		ReminderRuleImpl clone = (ReminderRuleImpl)rule.clone();
		String groupKey = clone.getRightOperand();
		
		boolean found = false;
		if(StringHelper.isLong(groupKey)) {
			Long key = Long.parseLong(groupKey);
			for(BusinessGroupReference ref:envMapper.getGroups()) {
				if(key.equals(ref.getOriginalKey()) && ref.getKey() != null) {
					clone.setRightOperand(ref.getKey().toString());
					found = true;
				}
			}
		}
		
		return found ? clone : null;
	}

	@Override
	public List<Identity> evaluate(RepositoryEntry entry, ReminderRule rule) {
		List<Identity> identities = null;
		
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			String groupKey = r.getRightOperand();
			if(StringHelper.isLong(groupKey)) {
				Long key = Long.parseLong(groupKey);
				BusinessGroupRef groupRef = new BusinessGroupRefImpl(key);
				identities = businessGroupRelationDao.getMembers(groupRef, GroupRoles.coach.name(), GroupRoles.participant.name());
			}
		}
		
		return identities == null ? Collections.<Identity>emptyList() : identities;
	}
}