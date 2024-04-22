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
package org.olat.home;

import java.util.List;

import org.olat.core.commons.modules.bc.BriefcaseWebDAVMergeSource;
import org.olat.core.commons.services.folder.ui.FolderController;
import org.olat.core.commons.services.folder.ui.FolderControllerConfig;
import org.olat.core.commons.services.folder.ui.FolderEmailFilter;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Mar 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class PersonalFolderController extends BasicController implements Activateable2 {

	private FolderController folderCtrl;
	
	@Autowired
	private UserManager userManager;

	public PersonalFolderController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("personal_folder");
		putInitialPanel(mainVC);
		
		BriefcaseWebDAVMergeSource rootContainer = new BriefcaseWebDAVMergeSource(getIdentity(),
				ureq.getUserSession().getRoles(), userManager.getUserDisplayName(getIdentity()));
		FolderControllerConfig config = FolderControllerConfig.builder()
				.withSearchResourceUrl("[Identity:" + getIdentity().getKey() + "][userfolder:0]")
				.withMail(FolderEmailFilter.publicOnly)
				.build();
		folderCtrl = new FolderController(ureq, wControl, rootContainer, config);
		listenTo(folderCtrl);
		mainVC.put("folder", folderCtrl.getInitialComponent());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		folderCtrl.activate(ureq, entries, state);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
