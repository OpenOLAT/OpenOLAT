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
package org.olat.portfolio.ui.structel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.portfolio.EPLoggingAction;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.ui.EPArtefactPoolRunController;
import org.olat.portfolio.ui.artefacts.view.EPArtefactChoosenEvent;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Controller to select an Element which has to be added to given
 * PortfolioStructure. All possible elements are disabled per default:
 * <UL>
 * <LI>use setShowLink to enable elements which can be added on this level of
 * structure</LI>
 * </UL>
 * <P>
 * Initial Date: 20.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPAddElementsController extends BasicController {

	private PortfolioStructure portfolioStructure;
	private VelocityContainer addLinkVC;
	private Link addStructLink;
	@Autowired
	private EPFrontendManager ePFMgr;
	public static final String ADD_ARTEFACT = "Artefact";
	public static final String ADD_PAGE = "page";
	public static final String ADD_STRUCTUREELEMENT = "struct";
	public static final String ADD_PORTFOLIOSTRUCTURE = "map";
	private static final Set<String> typeSet = new HashSet<>();
	static {
		typeSet.add(ADD_ARTEFACT);
		typeSet.add(ADD_PAGE);
		typeSet.add(ADD_STRUCTUREELEMENT);
		typeSet.add(ADD_PORTFOLIOSTRUCTURE);
	}
	private final Map<String, Boolean> typeMap = new HashMap<>();
	private CloseableModalController artefactBox;
	private EPArtefactPoolRunController artefactPoolCtrl;
	private Link linkArtefactLink;
	private String activeType;

	public EPAddElementsController(UserRequest ureq, WindowControl wControl, PortfolioStructure portStruct) {
		super(ureq, wControl);
		this.portfolioStructure = portStruct;
		addLinkVC = createVelocityContainer("addLink");
		addStructLink = LinkFactory.createCustomLink("popupLink", "add", translate("addPopup.title"),
				Link.BUTTON | Link.NONTRANSLATED, addLinkVC, this);
		addStructLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		addStructLink.setVisible(false);

		linkArtefactLink = LinkFactory.createCustomLink("linkArtefact", "link", translate("addArtefact"),
				Link.BUTTON | Link.NONTRANSLATED, addLinkVC, this);
		linkArtefactLink.setTooltip(translate("linkArtefact.tooltip"));
		linkArtefactLink.setCustomEnabledLinkCSS("o_eportfolio_add_link o_eportfolio_link");
		linkArtefactLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");

		for (String key : typeSet) {
			typeMap.put(key, Boolean.FALSE);
		}

		putInitialPanel(addLinkVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == linkArtefactLink) {
			popUpAddArtefactBox(ureq);
		} else if (source == addStructLink) {
			if (ADD_PAGE.equals(activeType)) {
				String title = translate("new.page.title");
				String description = translate("new.page.desc");
				PortfolioStructure newPage = ePFMgr.createAndPersistPortfolioPage(portfolioStructure, title, description);
				DBFactory.getInstance().commit();
				fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.ADDED, newPage));
			} else if (ADD_STRUCTUREELEMENT.equals(activeType)) {
				String title = translate("new.structure.title");
				String description = translate("new.structure.desc");
				PortfolioStructure newStruct = ePFMgr.createAndPersistPortfolioStructureElement(portfolioStructure, title, description);
				DBFactory.getInstance().commit();
				fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.ADDED, newStruct));
			} else if (ADD_PORTFOLIOSTRUCTURE.equals(activeType)) {
				// show tree-with maps to choose from
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == artefactPoolCtrl) {
			if(event instanceof EPArtefactChoosenEvent) {
				// finally an artefact was choosen
				EPArtefactChoosenEvent artCEv = (EPArtefactChoosenEvent) event;
				artefactBox.deactivate();
				AbstractArtefact choosenArtefact = artCEv.getArtefact();
				// check for a yet existing link to this artefact
				if (ePFMgr.isArtefactInStructure(choosenArtefact, portfolioStructure)) {
					showWarning("artefact.already.in.structure");
				} else {
					boolean successfullLink = ePFMgr.addArtefactToStructure(getIdentity(), choosenArtefact, portfolioStructure);
					if (successfullLink) {
						getWindowControl().setInfo(
								getTranslator().translate("artefact.choosen", new String[] { choosenArtefact.getTitle(), portfolioStructure.getTitle() }));
						ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(choosenArtefact));
						ThreadLocalUserActivityLogger.log(EPLoggingAction.EPORTFOLIO_ARTEFACT_SELECTED, getClass());
					} else {
						showError("restrictions.not.conform");
					}
					fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.ADDED, portfolioStructure));
				}
			} else if(event == Event.DONE_EVENT) {
				artefactBox.deactivate();
				fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.ADDED, portfolioStructure));
			}
		} 
	}

	public void setShowLink(String... types) {
		int addAmount = 0;
		for (String type : types) {
			if (typeSet.contains(type)) {
				typeMap.put(type, Boolean.TRUE);
				if (!type.equals(ADD_ARTEFACT)) {
					prepareAddLink(type);
					activeType = type;
					addAmount++;
				}
			}
		}

		if (addAmount > 1) throw new AssertException(
				"its not possible anymore to have more than one structure element type to be added. if needed, implement links of this controller in callout again.");
		linkArtefactLink.setVisible(typeMap.get(ADD_ARTEFACT));
	}

	private void prepareAddLink(String type) {
		addStructLink.setVisible(true);
		String title = translate("add." + type);
		addStructLink.setTooltip(title);
		addStructLink.setCustomDisplayText(title);
		addStructLink.setCustomEnabledLinkCSS("o_eportfolio_add_link o_ep_" + type + "_icon");
	}

	private void popUpAddArtefactBox(UserRequest ureq) {
		if (artefactPoolCtrl == null) {
			artefactPoolCtrl = new EPArtefactPoolRunController(ureq, getWindowControl(), true, true);
			listenTo(artefactPoolCtrl);
		}
		artefactBox = new CloseableModalController(getWindowControl(),"close",artefactPoolCtrl.getInitialComponent(),true, translate("choose.artefact.title"));
		artefactPoolCtrl.setPreSelectedStruct(portfolioStructure);

		listenTo(artefactBox);
		artefactBox.activate();
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing
	}

}
