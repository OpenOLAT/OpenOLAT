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
package org.olat.modules.openbadges.ui.wizard;

import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.course.member.wizard.ImportMemberByUsernamesController;
import org.olat.course.member.wizard.MembersByNameContext;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;

/**
 * Initial date: 2025-08-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class IssueGlobalBadgeFinish implements StepRunnerCallback {

	private final String badgeClassRootId;
	private final OpenBadgesManager openBadgesManager;
	private final Identity doer;

	public IssueGlobalBadgeFinish(String badgeClassRootId, OpenBadgesManager openBadgesManager, Identity doer) {
		this.badgeClassRootId = badgeClassRootId;
		this.openBadgesManager = openBadgesManager;
		this.doer = doer;
	}
	
	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		if (runContext.get(ImportMemberByUsernamesController.RUN_CONTEXT_KEY) instanceof MembersByNameContext ctx) {
			Set<Identity> recipients = ctx.getIdentities();
			BadgeClass reloadedBadgeClass = openBadgesManager.getCurrentBadgeClass(badgeClassRootId);
			openBadgesManager.issueBadgeManually(reloadedBadgeClass, recipients, doer);
		}
		return StepsMainRunController.DONE_MODIFIED;
	}
}
