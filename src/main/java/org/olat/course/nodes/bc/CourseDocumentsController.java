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
package org.olat.course.nodes.bc;

import java.util.List;

import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 30 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseDocumentsController extends BasicController implements Activateable2 {

	public static final String SUBSCRIPTION_SUBIDENTIFIER = "documents";
	
	private final FolderRunController folderCtrl;
	
	public CourseDocumentsController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		SubscriptionContext subContext = new SubscriptionContext(courseEntry, SUBSCRIPTION_SUBIDENTIFIER);
		VFSSecurityCallback secCallback = CourseDocumentsFactory
				.getSecurityCallback(userCourseEnv, ureq.getUserSession().getRoles().isGuestOnly(), subContext);
		VFSContainer rootContainer = CourseDocumentsFactory.getFileContainer(userCourseEnv.getCourseEnvironment());
		rootContainer.setLocalSecurityCallback(secCallback);
		folderCtrl = new FolderRunController(rootContainer, true, false, true, ureq, getWindowControl());
		listenTo(folderCtrl);
		putInitialPanel(folderCtrl.getInitialComponent());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (folderCtrl != null) {
			folderCtrl.activate(ureq, entries, state);
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
