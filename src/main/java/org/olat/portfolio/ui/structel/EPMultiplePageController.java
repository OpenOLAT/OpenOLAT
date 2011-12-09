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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.structel.EPPage;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.ui.structel.view.EPTOCReadOnlyController;

/**
 * Description:<br>
 * shows multiple pages in a map and handles the paging of them
 * 
 * <P>
 * Initial Date: 23.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPMultiplePageController extends BasicController {

	private List<PortfolioStructure> pageList;
	private List<Long> pageListByKeys;
	private Controller pageCtrl;
	private int previousPage;
	private final VelocityContainer vC;
	private final EPSecurityCallback secCallback;
	private final EPFrontendManager ePFMgr;

	public EPMultiplePageController(UserRequest ureq, WindowControl wControl, List<PortfolioStructure> pageList,
			EPSecurityCallback secCallback) {
		super(ureq, wControl);
		this.pageList = pageList;
		this.pageListByKeys = new ArrayList<Long>(pageList.size());
		this.secCallback = secCallback;
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		
		vC = createVelocityContainer("multiPages");
		
		init(ureq);

		putInitialPanel(vC);
	}
	
	public EPPage getSelectedPage() {
		if(pageCtrl instanceof EPPageViewController) {
			return ((EPPageViewController)pageCtrl).getPage();
		}
		return null;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	protected void init(UserRequest ureq) {
		if (pageList != null && pageList.size() != 0) {

			// create toc link
			Link tocLink = LinkFactory.createLink("toc", vC, this);
			tocLink.setUserObject(-1);

			int i = 1;
			ArrayList<Link> pageLinkList = new ArrayList<Link>();
			for (PortfolioStructure page : pageList) {
				pageListByKeys.add(page.getKey());
				String pageTitle = ((EPPage) page).getTitle();
				String shortPageTitle = Formatter.truncate(pageTitle, 20);
				Link pageLink = LinkFactory.createCustomLink("pageLink" + i, "pageLink" + i, shortPageTitle, Link.LINK + Link.NONTRANSLATED, vC,
						this);
				pageLink.setUserObject(i - 1);
				pageLink.setTooltip(pageTitle, false);
				pageLinkList.add(pageLink);
				i++;
			}
			vC.contextPut("pageLinkList", pageLinkList);
			setAndInitActualPage(ureq, -1, false);
		}
	}
	
	protected void selectPage(UserRequest ureq, PortfolioStructure page) {
		int count = 0;
		for(PortfolioStructure structure:pageList) {
			if(structure.getKey().equals(page.getKey())) {
				setAndInitActualPage(ureq, count, false);
				break;
			}
			count++;
		}
	}

	private void setAndInitActualPage(UserRequest ureq, int pageNum, boolean withComments) {
		removeAsListenerAndDispose(pageCtrl);
		if (pageNum == -1){
			// this is the toc
			EPPage page = (EPPage)pageList.get(0);
			PortfolioStructure map = ePFMgr.loadStructureParent(page);
			pageCtrl = new EPTOCReadOnlyController(ureq, getWindowControl(), map, secCallback);
			// disable toc-link
			enDisableTOC(false);
		} else {
			EPPage page = (EPPage)pageList.get(pageNum);
			PortfolioStructure map = ePFMgr.loadStructureParent(page);
			pageCtrl = new EPPageViewController(ureq, getWindowControl(), map, page, withComments, secCallback);
			// enable toc-link
			enDisableTOC(true);
		}
		
		vC.put("pageCtrl", pageCtrl.getInitialComponent());
		vC.contextPut("actualPage", pageNum + 1);
		listenTo(pageCtrl);
		// disable actual page itself
		Link actLink = (Link) vC.getComponent("pageLink" + String.valueOf((pageNum + 1)));
		if (actLink != null) actLink.setEnabled(false);
		// enable previous page
		Link prevLink = (Link) vC.getComponent("pageLink" + String.valueOf((previousPage)));
		if (prevLink !=null) prevLink.setEnabled(true);
		previousPage = pageNum+1;
	}
	
	private void enDisableTOC(boolean enabled){
		Link tocLink = (Link) vC.getComponent("toc");
		if (tocLink !=null) tocLink.setEnabled(enabled);
		vC.contextPut("tocactive", !enabled);
	}
	

	@Override
	protected void event(UserRequest ureq, Component source, @SuppressWarnings("unused") Event event) {
		if (source instanceof Link) {
			Link link = (Link) source;
			int pageNum = (Integer) link.getUserObject();
			setAndInitActualPage(ureq, pageNum, false);
		}
		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == pageCtrl) {
			if (source instanceof EPTOCReadOnlyController){
				// activate selected structure from toc!
				if (event instanceof EPStructureEvent){
					EPStructureEvent epEv = (EPStructureEvent) event;
					PortfolioStructure selStruct = epEv.getStructure();
					if(event.getCommand().equals(EPStructureEvent.SELECT)) {
						findAndActivatePage(ureq, selStruct, false);
					} else if(event.getCommand().equals(EPStructureEvent.SELECT_WITH_COMMENTS)) {
						findAndActivatePage(ureq, selStruct, true);
					}
				}				
			}			
		} 
	}
	
	private void findAndActivatePage(UserRequest ureq, PortfolioStructure selStruct, boolean withComments){
		if (pageListByKeys.contains(selStruct.getKey())){
			int pos = pageListByKeys.indexOf(selStruct.getKey());
			if (pos != -1) setAndInitActualPage(ureq, pos, withComments);
		} else {
			// lookup parents, as this could be an artefact or a structureElement
			PortfolioStructure parentStruct = ePFMgr.loadStructureParent(selStruct);
			if (parentStruct != null) findAndActivatePage(ureq, parentStruct, withComments);
		}
		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// nothing
	}


}
