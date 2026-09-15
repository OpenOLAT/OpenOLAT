/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 15 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryDetailsResponsiblePersonsController extends BasicController {

	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryService repositoryService;

	public RepositoryEntryDetailsResponsiblePersonsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean isOwner) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		VelocityContainer mainVC = createVelocityContainer("details_responsible_persons");

		Roles roles = ureq.getUserSession().getRoles();
		boolean visible = isOwner || roles.isAdministrator() || roles.isAuthor() || roles.isLearnResourceManager();
		mainVC.contextPut("visible", Boolean.valueOf(visible));

		if (visible) {
			String initialAuthor = entry.getInitialAuthor();
			String creator = initialAuthor;
			if (StringHelper.containsNonWhitespace(initialAuthor)) {
				String displayName = userManager.getUserDisplayName(initialAuthor);
				if (StringHelper.containsNonWhitespace(displayName) && !displayName.equals(initialAuthor)) {
					creator = displayName + " (" + initialAuthor + ")";
				}
			}
			mainVC.contextPut("creator", creator == null ? "" : creator);

			List<Long> ownerKeys = repositoryService.getMemberKeys(entry, RepositoryEntryRelationType.all, GroupRoles.owner.name());
			List<String> ownerLinkNames = new ArrayList<>(ownerKeys.size());
			Map<Long, String> ownerNames = userManager.getUserDisplayNamesByKey(ownerKeys);
			int counter = 0;
			for (Map.Entry<Long, String> owner:ownerNames.entrySet()) {
				String ownerName = StringHelper.escapeHtml(owner.getValue());

				Link ownerLink = LinkFactory.createCustomLink("owner-" + ++counter, "owner", ownerName, Link.NONTRANSLATED | Link.LINK, mainVC, this);
				ownerLink.setUserObject(owner.getKey());
				ownerLinkNames.add(ownerLink.getComponentName());
			}
			mainVC.contextPut("ownerlinknames", ownerLinkNames);
		}

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link && "owner".equals(link.getCommand())) {
			doOpenVisitCard(ureq, (Long)link.getUserObject());
		}
	}

	private void doOpenVisitCard(UserRequest ureq, Long ownerKey) {
		String businessPath = "[HomePage:" + ownerKey + "]";
		fireEvent(ureq, Event.DONE_EVENT);
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
}
