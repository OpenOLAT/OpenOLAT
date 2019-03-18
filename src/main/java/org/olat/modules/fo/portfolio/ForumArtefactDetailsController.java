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
package org.olat.modules.fo.portfolio;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DownloadComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSItemMetaFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;

/**
 * 
 * Description:<br>
 * Show the specific part of the ForumArtefact
 * 
 * <P>
 * Initial Date:  11 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ForumArtefactDetailsController extends BasicController {

	private final VelocityContainer vC;

	public ForumArtefactDetailsController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact) {
		super(ureq, wControl);
		ForumArtefact fArtefact = (ForumArtefact)artefact;
		vC = createVelocityContainer("messageDetails");
		EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		vC.contextPut("text", ePFMgr.getArtefactFullTextContent(fArtefact));
		VFSContainer artContainer = ePFMgr.getArtefactContainer(artefact);
		if (artContainer != null && !artContainer.getItems(new VFSSystemItemFilter()).isEmpty()){
			List<VFSItem> attachments = new ArrayList<>(artContainer.getItems(new VFSItemMetaFilter()));
			int i=1; //vc-shift!
			for (VFSItem vfsItem : attachments) {
				VFSLeaf file = (VFSLeaf) vfsItem;
				DownloadComponent downlC = new DownloadComponent("download"+i, file, true,
						file.getName() + " (" + String.valueOf(file.getSize() / 1024) + " KB)", null,
						CSSHelper.createFiletypeIconCssClassFor(file.getName()));
				vC.put("download"+i, downlC);
				i++;
			}
			vC.contextPut("attachments", attachments);
			vC.contextPut("hasAttachments", true);
		} 
		
		putInitialPanel(vC);		
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

