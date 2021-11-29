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
package org.olat.course.assessment.ui.tool;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.ui.tool.event.CourseNodeIdentityEvent;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IdentitySelectionController extends BasicController {

	private final VelocityContainer mainVC;
	private Link moreLink;

	private final String nodeIdent;
	private final Supplier<AssessedIdentityListState> identityFilter;
	private int counter = 0;
	
	@Autowired
	private UserManager userManager;

	public IdentitySelectionController(UserRequest ureq, WindowControl wControl, String nodeIdent,
			List<Identity> identities, Supplier<AssessedIdentityListState> identityFilter) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		this.nodeIdent = nodeIdent;
		this.identityFilter = identityFilter;
		mainVC = createVelocityContainer("identity_selection");
		
		List<Link> links = identities.stream()
				.map(identity -> new IdentityDisplayName(identity, userManager.getUserDisplayName(identity)))
				.sorted((i1, i2) -> i1.getDisplayName().compareTo(i2.getDisplayName()))
				.limit(5)
				.map(this::createLink)
				.collect(Collectors.toList());
		mainVC.contextPut("links", links);
		
		if (identities.size() > links.size()) {
			int more = identities.size() - links.size();
			String moreText = translate("select.more.identities", Integer.toString(more));
			moreLink = LinkFactory.createCustomLink("o_more", "more", moreText, Link.LINK + Link.NONTRANSLATED, mainVC, this);
			moreLink.setIconLeftCSS("o_icon o_icon-fw"); // some space
		}
		
		putInitialPanel(mainVC);
	}

	private Link createLink(IdentityDisplayName identitydisplayname) {
		Link link = LinkFactory.createCustomLink("o_select_user_" + counter++, "select",
				identitydisplayname.getDisplayName(), Link.LINK + Link.NONTRANSLATED, mainVC, this);
		link.setIconLeftCSS("o_icon o_icon-fw o_icon_user");
		link.setUserObject(identitydisplayname.getIdentity());
		return link;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == moreLink) {
			fireEvent(ureq, new CourseNodeIdentityEvent(nodeIdent, null, identityFilter));
		} else if (source instanceof Link) {
			Link link = (Link)source;
			Identity assessedIdentity = (Identity)link.getUserObject();
			fireEvent(ureq, new CourseNodeIdentityEvent(nodeIdent, assessedIdentity, identityFilter));
		}
	}

	private static final class IdentityDisplayName {
		
		private final Identity identity;
		private final String displayName;
		
		private IdentityDisplayName(Identity identity, String displayName) {
			this.identity = identity;
			this.displayName = displayName;
		}

		public Identity getIdentity() {
			return identity;
		}

		public String getDisplayName() {
			return displayName;
		}
		
	}

}
