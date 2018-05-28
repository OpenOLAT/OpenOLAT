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

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.portfolio.EPAbstractHandler;
import org.olat.portfolio.model.artefacts.AbstractArtefact;

/**
 * 
 * Description:<br>
 * The ArtefactHandler for Forums
 * 
 * <P>
 * Initial Date:  11.06.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class ForumArtefactHandler extends EPAbstractHandler<ForumArtefact> {
	
	/**
	 * @see org.olat.portfolio.EPAbstractHandler#prefillArtefactAccordingToSource(org.olat.portfolio.model.artefacts.AbstractArtefact, java.lang.Object)
	 */
	@Override
	public void prefillArtefactAccordingToSource(AbstractArtefact artefact, Object source) {
		super.prefillArtefactAccordingToSource(artefact, source);
		if (source instanceof OLATResourceable){
			OLATResourceable ores = (OLATResourceable) source;
			ForumManager fMgr = CoreSpringFactory.getImpl(ForumManager.class);
			Message fm = fMgr.loadMessage(ores.getResourceableId());
			String thread = fm.getThreadtop() != null ? fm.getThreadtop().getTitle() + " - " : "";
			artefact.setTitle(thread + fm.getTitle());
			
			VFSContainer msgContainer = fMgr.getMessageContainer(fm.getForum().getKey(), fm.getKey());
			if (msgContainer != null) {
				List<VFSItem> foAttach = msgContainer.getItems();
				if (foAttach.size()!=0){
					artefact.setFileSourceContainer(msgContainer);
				}
			}
			
			artefact.setSignature(70); 
			artefact.setFulltextContent(fm.getBody());
		}
	}

	@Override
	public ForumArtefact createArtefact() {
		ForumArtefact artefact = new ForumArtefact();
		return artefact;
	}

	@Override
	public String getType() {
		return ForumArtefact.FORUM_ARTEFACT_TYPE;
	}

	@Override
	public Controller createDetailsController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, boolean readOnlyMode) {
		return new ForumArtefactDetailsController(ureq, wControl, artefact);
	}
}