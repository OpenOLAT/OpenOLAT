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
package org.olat.portfolio.ui.artefacts.collect;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.portfolio.EPLoggingAction;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.artefacts.FileArtefact;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<br>
 * Persists the collected data after using the wizzard for new artefacts
 * 
 * <P>
 * Initial Date: 01.09.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPArtefactWizzardStepCallback implements StepRunnerCallback {

	private VFSContainer tempUpload;
	private EPFrontendManager ePFMgr;

	/**
	 * 
	 * @param tempUpload
	 */
	public EPArtefactWizzardStepCallback(VFSContainer tempUpload) {
		this.tempUpload = tempUpload;
	}

	public EPArtefactWizzardStepCallback() {
		// default without a specified temp-folder, it still might be defined during wizzard and added to runcontext
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepRunnerCallback#execute(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.core.gui.control.generic.wizard.StepsRunContext)
	 */
	@Override
	public Step execute(UserRequest ureq2, WindowControl wControl, StepsRunContext runContext) {
		boolean hasChanges = false;
		if (runContext.containsKey("artefact")) {
			hasChanges = true;
			AbstractArtefact locArtefact = (AbstractArtefact) runContext.get("artefact");
			ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
			PortfolioModule portfolioModule = (PortfolioModule) CoreSpringFactory.getBean("portfolioModule");
			
			// set the defined signature level, if its not from inside olat
			if (locArtefact.getSignature() < 0 && ( (runContext.containsKey("copyright.accepted") && (Boolean) runContext.get("copyright.accepted")) || !portfolioModule.isCopyrightStepEnabled() ) ) {
				locArtefact.setSignature(-1 * locArtefact.getSignature());
			}
			
			ePFMgr.updateArtefact(locArtefact);

			if (runContext.containsKey("tempArtFolder")) {
				// a new text or file-artefact was created, copy everything to destination
				VFSContainer tmpFolder = (VFSContainer) runContext.get("tempArtFolder");
				copyFromTempToArtefactContainer(locArtefact, tmpFolder);
			} else if (tempUpload != null) {
				// an artefact was collected in bc, only copy the selected file				
				copyFromBCToArtefactContainer(locArtefact, tempUpload);
			}
			
			
			// add to a structure if any was selected
			if (runContext.containsKey("selectedStructure")){
				PortfolioStructure parentStructure = (PortfolioStructure) runContext.get("selectedStructure");
				if (parentStructure != null){
					ePFMgr.addArtefactToStructure(ureq2.getIdentity(), locArtefact, parentStructure);
				}
			}
			
			@SuppressWarnings("unchecked")
			List<String> allTags = (List<String>) runContext.get("artefactTagsList");
			ePFMgr.setArtefactTags(ureq2.getIdentity(), locArtefact, allTags);

			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(locArtefact));
			ThreadLocalUserActivityLogger.log(EPLoggingAction.EPORTFOLIO_ARTEFACT_ADDED, getClass());
		}

		return hasChanges ? StepsMainRunController.DONE_MODIFIED : StepsMainRunController.DONE_UNCHANGED;
	}

	private void copyFromTempToArtefactContainer(AbstractArtefact artefact, VFSContainer tmp) {
		if (tmp != null) {
			VFSContainer artFolder = ePFMgr.getArtefactContainer(artefact);
			List<VFSItem> items = tmp.getItems();
			for (VFSItem vfsItem : items) {
				artFolder.copyFrom(vfsItem);
			}
		}
	}
	
	private void copyFromBCToArtefactContainer(AbstractArtefact artefact, VFSContainer tmp) {
		if (tmp != null) {
			VFSContainer artFolder = ePFMgr.getArtefactContainer(artefact);
			VFSItem bcFile = tmp.resolve(((FileArtefact)artefact).getFilename());
			if (bcFile != null){
				artFolder.copyFrom(bcFile);
			}
		}
	}

}
