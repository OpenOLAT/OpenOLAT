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
package org.olat.course.nodes.wiki;

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.UserSession;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.wiki.DryRunAssessmentProvider;
import org.olat.modules.wiki.PersistingAssessmentProvider;
import org.olat.modules.wiki.WikiAssessmentProvider;
import org.olat.modules.wiki.WikiMainController;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiReadOnlySecurityCallback;
import org.olat.modules.wiki.WikiSecurityCallback;
import org.olat.modules.wiki.WikiSecurityCallbackImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class WikiToolController extends BasicController implements Activateable2 {

	public static final String SUBSCRIPTION_SUBIDENTIFIER = "wiki";
	
	private WikiMainController wikiCtrl;
	private MessageController noWikiCtrl;
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;

	public WikiToolController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		
		String wikiSoftKey = userCourseEnv.getCourseEnvironment().getCourseConfig().getWikiSoftKey();
		RepositoryEntry wikiEntry = repositoryManager.lookupRepositoryEntryBySoftkey(wikiSoftKey, false);
		if (wikiEntry != null) {
			//check role
			UserSession usess = ureq.getUserSession();
			boolean isAdmininstrator = userCourseEnv.isAdmin();
			boolean isGuestOnly = usess.getRoles().isGuestOnly();
			boolean isResourceOwner = isAdmininstrator || repositoryService.hasRole(getIdentity(), wikiEntry, GroupRoles.owner.name());

			// Check for jumping to certain wiki page
			BusinessControl bc = wControl.getBusinessControl();
			ContextEntry ce = bc.popLauncherContextEntry();
			
			String resName = CourseModule.getCourseTypeName();
			Long resId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
			OLATResourceable courseOres = OresHelper.createOLATResourceableInstance(resName, resId);
			SubscriptionContext subsContext = new SubscriptionContext(courseOres, SUBSCRIPTION_SUBIDENTIFIER);
			WikiSecurityCallback callback;
			WikiAssessmentProvider assessmentProvider;
			if(userCourseEnv.isCourseReadOnly()) {
				callback = new WikiReadOnlySecurityCallback(isGuestOnly, (isAdmininstrator || isResourceOwner));
				assessmentProvider = DryRunAssessmentProvider.create();
			} else {
				callback = new WikiSecurityCallbackImpl(Boolean.TRUE, isAdmininstrator, isGuestOnly, false,
						isResourceOwner, subsContext);
				assessmentProvider = userCourseEnv.isParticipant()
						? PersistingAssessmentProvider.create(wikiEntry, getIdentity(), false)
						: DryRunAssessmentProvider.create();
			}
			
			
			if ( ce != null ) { //jump to a certain context
				OLATResourceable ores = ce.getOLATResourceable();
				String typeName = ores.getResourceableTypeName();
				String page = typeName.substring("page=".length());
				if(page.endsWith(":0")) {
					page = page.substring(0, page.length() - 2);
				}
				wikiCtrl = WikiManager.getInstance().createWikiMainController(ureq, wControl, wikiEntry.getOlatResource(), callback, assessmentProvider, page);
			} else {
				wikiCtrl = WikiManager.getInstance().createWikiMainController(ureq, wControl, wikiEntry.getOlatResource(), callback, assessmentProvider, null);
			}
			listenTo(wikiCtrl);
			putInitialPanel(wikiCtrl.getInitialComponent());
		} else {
			String title = translate("tool.no.wiki.title");
			String text = translate("tool.no.wiki.text");
			noWikiCtrl = MessageUIFactory.createInfoMessage(ureq, wControl, title, text);
			listenTo(noWikiCtrl);
			putInitialPanel(noWikiCtrl.getInitialComponent());
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (wikiCtrl != null) {
			wikiCtrl.activate(ureq, entries, state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
