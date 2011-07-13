/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.portfolio;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalWindowWrapperController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.ui.EPArtefactPoolRunController;
import org.olat.portfolio.ui.EPMapRunController;
import org.olat.portfolio.ui.EPMapRunViewOption;
import org.olat.portfolio.ui.PortfolioAdminController;
import org.olat.portfolio.ui.artefacts.collect.ArtefactWizzardStepsController;
import org.olat.portfolio.ui.artefacts.edit.EPReflexionWrapperController;
import org.olat.portfolio.ui.artefacts.view.EPArtefactViewController;
import org.olat.portfolio.ui.artefacts.view.EPMultiArtefactsController;
import org.olat.portfolio.ui.artefacts.view.EPMultipleArtefactSmallReadOnlyPreviewController;
import org.olat.portfolio.ui.artefacts.view.EPMultipleArtefactsAsTableController;
import org.olat.portfolio.ui.structel.EPMapViewController;
import org.olat.portfolio.ui.structel.edit.EPStructureDetailsController;

/**
 * UIFactory for ePortfolio to get Controllers from outside the ePortfolio-scope
 * 
 * Important: Methods need to be static, so that they can be called by FactoryCreator!
 * 
 * <P>
 * Initial Date: 11.06.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPUIFactory {

	/**
	 * get the artefact pool controller
	 * used directly over extension-config, therefore needs to be static
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public static Controller createPortfolioPoolController(UserRequest ureq, WindowControl wControl) {
			return new EPArtefactPoolRunController(ureq, wControl);
	}
	/**
	 * get a controller for admin-setup of e Portfolio
	 * used directly over extension-config, therefore needs to be static
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public static Controller createPortfolioAdminController(UserRequest ureq, WindowControl wControl) {
		return new PortfolioAdminController(ureq, wControl);
	}

	/**
	 * get a controller with all user maps (without structureds map or templates)
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public static Controller createPortfolioMapsController(UserRequest ureq, WindowControl wControl) {
		return new EPMapRunController(ureq, wControl, true, EPMapRunViewOption.MY_DEFAULTS_MAPS, null);
	}
	
	/**
	 * Get a controller with all maps I can see from other users,
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public static Controller createPortfolioMapsFromOthersController(UserRequest ureq, WindowControl wControl) {
		return new EPMapRunController(ureq, wControl, false, EPMapRunViewOption.OTHERS_MAPS, null);
	}
	
	/**
	 * Get a controller with all maps I can see from other users,
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public static Controller createPortfolioMapsVisibleToOthersController(UserRequest ureq, WindowControl wControl, Identity choosenOwner) {
		return new EPMapRunController(ureq, wControl, false, EPMapRunViewOption.OTHER_MAPS, choosenOwner);
	}
	
	/**
	 * Get a controller with all user structured maps (but not templates)
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public static Controller createPortfolioStructuredMapsController(UserRequest ureq, WindowControl wControl) {
		return new EPMapRunController(ureq, wControl, false, EPMapRunViewOption.MY_EXERCISES_MAPS, null);
	}
	
	public static Controller createPortfolioStructureMapController(UserRequest ureq, WindowControl wControl, PortfolioStructureMap map,
			EPSecurityCallback secCallback) {
		return new EPMapViewController(ureq, wControl, map, false, secCallback);
	}
	
	public static Controller createMapViewController(UserRequest ureq, WindowControl wControl,
			PortfolioStructureMap map, EPSecurityCallback secCallback) {
		EPMapViewController mapViewController = 
			new EPMapViewController(ureq, wControl, map, false, secCallback);
		return mapViewController;
	}
	
	/**
	 * initiate the artefact-collection wizzard, first get link which then is handled by ctrl itself to open the wizzard
	 * @param ureq
	 * @param wControl
	 * @param ores the resourcable from which an artefact should be created
	 * @param subPath
	 * @param businessPath
	 * @return
	 */
	public static Controller createArtefactCollectWizzardController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, String businessPath) {
		PortfolioModule portfolioModule = (PortfolioModule) CoreSpringFactory.getBean("portfolioModule");
		EPArtefactHandler<?> handler = portfolioModule.getArtefactHandler(ores.getResourceableTypeName());
		if (portfolioModule.isEnabled() && handler != null && handler.isEnabled()) {
			ArtefactWizzardStepsController artWizzCtrl = new ArtefactWizzardStepsController(ureq, wControl, ores, businessPath);
			return artWizzCtrl;
		}
		return null;
	}
	
	/**
	 * opens an artefact in an overlay window with all available details in read-only mode
	 * @param artefact
	 * @param ureq
	 * @param wControl
	 * @param title of the popup
	 * @return a controller to listenTo
	 */
	public static CloseableModalWindowWrapperController getAndActivatePopupArtefactController(AbstractArtefact artefact, UserRequest ureq, WindowControl wControl, String title) {
		EPArtefactViewController artefactCtlr;
		artefactCtlr = new EPArtefactViewController(ureq, wControl, artefact, true);
		CloseableModalWindowWrapperController artefactBox = new CloseableModalWindowWrapperController(ureq, wControl, title, artefactCtlr.getInitialComponent(), "artefactBox" + artefact.getKey());
		artefactBox.setInitialWindowSize(600, 500);
		artefactBox.activate();
		return artefactBox;
	}
	
	/**
	 * get artefacts in a table or as small previews depending on users-view-settings
	 * @param ureq
	 * @param wControl
	 * @param artefacts all artefacts to display
	 * @param struct PortfolioStructure wherein the artefacts are
	 * @return EPMultiArtefactsController
	 */
	public static EPMultiArtefactsController getConfigDependentArtefactsControllerForStructure(UserRequest ureq, WindowControl wControl, List<AbstractArtefact> artefacts, PortfolioStructure struct, EPSecurityCallback secCallback){
		String viewMode = struct.getArtefactRepresentationMode();
		if (artefacts.size() != 0) {
			EPMultiArtefactsController artefactCtrl;
			if (EPStructureDetailsController.VIEWMODE_TABLE.equals(viewMode)){
				artefactCtrl = new EPMultipleArtefactsAsTableController(ureq, wControl, artefacts, struct, false, secCallback);
			} else {
				artefactCtrl = new EPMultipleArtefactSmallReadOnlyPreviewController(ureq, wControl, artefacts, struct, secCallback);
			}
			return artefactCtrl;
		}
		return null;
	}
	
	/**
	 * get the correct reflexion popup depending on permissions and mode as also context
	 * @param ureq
	 * @param wControl
	 * @param secCallback
	 * @param artefact
	 * @param struct - might be null -> means not linked to a structure
	 * @return
	 */
	public static Controller getReflexionPopup(UserRequest ureq, WindowControl wControl, EPSecurityCallback secCallback, AbstractArtefact artefact, PortfolioStructure struct){
		return new EPReflexionWrapperController(ureq, wControl, secCallback, artefact, struct);
	}


}
