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
package org.olat.course.nodes.pf.ui;

import java.util.List;

import org.olat.core.commons.services.folder.ui.FolderController;
import org.olat.core.commons.services.folder.ui.FolderControllerConfig;
import org.olat.core.commons.services.folder.ui.FolderEmailFilter;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.pf.manager.PFManager;
import org.olat.course.nodes.pf.manager.PFView;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
/**
*
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
public class PFParticipantController extends BasicController {	
	
	private Link backLink;
	private TimerComponent timerCmp;
	private VelocityContainer mainVC;
	private FolderController folderCtrl;
	private ContextualSubscriptionController contextualSubscriptionCtr;
	
	private final PFView pfView;
	private final boolean isCoach;
	private final boolean readOnly;
	private final PFCourseNode pfNode;
	private final CourseEnvironment courseEnv;
	private final Identity assessedIdentity;
	private final FolderControllerConfig folderConfig;

	@Autowired
	private PFManager pfManager;

	public PFParticipantController(UserRequest ureq, WindowControl wControl, PFCourseNode pfNode,
			UserCourseEnvironment userCourseEnv, Identity assessedIdentity, PFView view, boolean isCoach, boolean readOnly, boolean withBack) {

		super(ureq, wControl);	
		mainVC = createVelocityContainer("participant");

		this.pfNode = pfNode;
		this.isCoach = isCoach;
		this.assessedIdentity = assessedIdentity;
		courseEnv = userCourseEnv.getCourseEnvironment();
		this.readOnly = readOnly || userCourseEnv.isCourseReadOnly();
				
		if (!(userCourseEnv.isCoach() || userCourseEnv.isAdmin()) && !(isCoach && readOnly)) {
			OLATResource course = courseEnv.getCourseGroupManager().getCourseResource();
			String businessPath = wControl.getBusinessControl().getAsString();
			SubscriptionContext subsContext = new SubscriptionContext(course, pfNode.getIdent());
			PublisherData publisherData = new PublisherData(OresHelper.calculateTypeName(PFCourseNode.class),
					String.valueOf(course.getResourceableId()), businessPath);
			contextualSubscriptionCtr = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext,
					publisherData);
			listenTo(contextualSubscriptionCtr);
			mainVC.put("contextualSubscription", contextualSubscriptionCtr.getInitialComponent());			
		}
		
		folderConfig = FolderControllerConfig.builder()
				.withDisplaySubscription(false)
				.withDisplayWebDAVLinkEnabled(false)
				.withDisplayQuotaLink(isCoach)
				.withSearchEnabled(false)
				.withUnzipEnabled(false)
				.withMail(FolderEmailFilter.never)
				.build();
		
		if(withBack) {
			backLink = LinkFactory.createLinkBack(mainVC, this);
		}

		initLimitMessages(ureq);

		//CourseFreeze
		pfView = view == null ? pfManager.providePFView(pfNode) : view;
		String path;
		switch(pfView) {
			case displayDrop:
				path = translate(PFCourseNode.FOLDER_DROP_BOX);
				break;
			case displayReturn:
				path = translate(PFCourseNode.FOLDER_RETURN_BOX);
				break;
			default:
				path = null;
				break;
		}
		initFolderController(ureq, path);
		
		putInitialPanel(mainVC);	
	}

	private void initLimitMessages(UserRequest ureq) {
		if (pfNode.hasParticipantBoxConfigured() && pfNode.hasLimitCountConfigured()) {
			mainVC.contextPut("limit", pfNode.getLimitCount());			
		}
		
		timerCmp = PFUIHelper.initTimeframeMessage(ureq, pfNode, mainVC, this, getTranslator());
	}
	
	private void initFolderController(UserRequest ureq, String path) {
		removeAsListenerAndDispose(folderCtrl);

		VFSContainer frc = pfManager.provideParticipantFolder(pfNode, pfView, getTranslator(), courseEnv,
				assessedIdentity, isCoach, readOnly);
		folderCtrl = new FolderController(ureq, getWindowControl(), frc, folderConfig);

		if (StringHelper.containsNonWhitespace(path)) {
			List<ContextEntry> entries = List.of(BusinessControlFactory.getInstance()
					.createContextEntry(OresHelper.createOLATResourceableTypeWithoutCheck("path=" + path)));
			folderCtrl.activate(ureq, entries, null);
			
		}
		listenTo(folderCtrl);
		mainVC.put("folder", folderCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(backLink == source) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if(timerCmp == source) {
			if(event instanceof TimesUpEvent) {
				initLimitMessages(ureq);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(folderCtrl == source) {
			recalculatSecurityCallback(ureq);
		}
	}
	
	private void recalculatSecurityCallback(UserRequest ureq) {
		String path = folderCtrl.getCurrentPath();
		initFolderController(ureq, path);
	}

	/**
	 * Remove the subscription panel but let the subscription context active
	 */
	public void disableSubscriptionController() {
		if (contextualSubscriptionCtr != null) {
			mainVC.remove(contextualSubscriptionCtr.getInitialComponent());
		}
	}	
}
