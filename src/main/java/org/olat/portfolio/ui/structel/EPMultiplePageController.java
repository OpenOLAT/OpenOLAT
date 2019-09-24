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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.structel.EPAbstractMap;
import org.olat.portfolio.model.structel.EPPage;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureRef;
import org.olat.portfolio.ui.structel.view.EPChangelogController;
import org.olat.portfolio.ui.structel.view.EPTOCReadOnlyController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * shows multiple pages in a map and handles the paging of them
 * 
 * <P>
 * Initial Date: 23.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPMultiplePageController extends BasicController implements Activateable2 {

	private List<PageTab> pageList;
	private Controller currentActivePageCtrl;
	private EPTOCReadOnlyController tocPageCtrl;
	private EPChangelogController changelogPageCtrl;
	private final VelocityContainer vC;
	private final EPSecurityCallback secCallback;
	@Autowired
	private EPFrontendManager ePFMgr;

	private Link tocLink; // the first tab, link to TOC
	private Link changelogLink; // the last tab, link to Changelog

	private static final int PAGENUM_TOC = -1; // pagenum of toc (first tab)
	private static final int PAGENUM_CL = -2; // pagenum of changelog (last tab)

	public EPMultiplePageController(UserRequest ureq, WindowControl wControl, List<PortfolioStructure> pages, EPSecurityCallback secCallback) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		
		pageList = new ArrayList<>(pages.size());
		for(PortfolioStructure page:pages) {
			if(page instanceof EPPage) {
				pageList.add(new PageTab(page.getKey(), page.getTitle()));
			}
		}

		vC = createVelocityContainer("multiPages");
		init(ureq);
		putInitialPanel(vC);
	}

	public EPPage getSelectedPage() {
		if (currentActivePageCtrl instanceof EPPageViewController) {
			return ((EPPageViewController) currentActivePageCtrl).getPage();
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
			tocLink = LinkFactory.createLink("toc", vC, this);
			tocLink.setDomReplacementWrapperRequired(false);
			tocLink.setUserObject(PAGENUM_TOC);

			// create changelog link
			changelogLink = LinkFactory.createLink("changelog", vC, this);
			changelogLink.setDomReplacementWrapperRequired(false);
			changelogLink.setUserObject(PAGENUM_CL);

			int i = 1;
			List<Link> pageLinkList = new ArrayList<>();
			for (PageTab page : pageList) {
				String pageTitle =StringHelper.escapeHtml(page.getTitle());
				String shortPageTitle = Formatter.truncate(pageTitle, 20);
				Link pageLink = LinkFactory
						.createCustomLink("pageLink" + i, "pageLink" + i, shortPageTitle, Link.LINK + Link.NONTRANSLATED, vC, this);
				pageLink.setDomReplacementWrapperRequired(false);
				pageLink.setUserObject(i - 1);
				pageLink.setTooltip(pageTitle);
				pageLinkList.add(pageLink);
				i++;
			}
			vC.contextPut("pageLinkList", pageLinkList);
			setAndInitActualPage(ureq, PAGENUM_TOC, false);
		}
	}

	protected void selectPage(UserRequest ureq, PortfolioStructure page) {
		int count = 0;
		for (PageTab structure : pageList) {
			if (structure.getKey().equals(page.getKey())) {
				setAndInitActualPage(ureq, count, false);
				break;
			}
			count++;
		}
	}

	/**
	 * 
	 * @param ureq
	 * @param pageNum
	 * @param withComments
	 */
	private void setAndInitActualPage(UserRequest ureq, int pageNum, boolean withComments) {
		disposeNormalPageController();

		if (pageNum == -1) {
			setAndInitTOCPage(ureq);
		} else if (pageNum == PAGENUM_CL) {
			setAndInitCLPage(ureq);
		} else {
			setAndInitNormalPage(ureq, pageNum, withComments);
		}

		vC.put("pageCtrl", currentActivePageCtrl.getInitialComponent());
		vC.contextPut("actualPage", pageNum + 1);
	}

	/**
	 * disposes the currentActivePageCtrl if its not the toc and not the
	 * changelog
	 */
	private void disposeNormalPageController() {
		if (currentActivePageCtrl instanceof EPPageViewController) {
			removeAsListenerAndDispose(currentActivePageCtrl);
		}
	}

	/**
	 * 
	 * @param ureq
	 */
	private void setAndInitTOCPage(UserRequest ureq) {
		// this is the toc
		if (tocPageCtrl == null) {
			PageTab page = pageList.get(0);
			PortfolioStructure map = ePFMgr.loadStructureParent(page);
			tocPageCtrl = new EPTOCReadOnlyController(ureq, getWindowControl(), map, secCallback);
			listenTo(tocPageCtrl);
		} else {
			tocPageCtrl.refreshTOC(ureq);
		}
		disableLINK_LC(false);
		disableLink_TOC(true);
		currentActivePageCtrl = tocPageCtrl;
		addToHistory(ureq, OresHelper.createOLATResourceableType("TOC"), null);
	}

	/**
	 * 
	 * @param ureq
	 */
	private void setAndInitCLPage(UserRequest ureq) {
		if (changelogPageCtrl == null) {
			changelogPageCtrl = instantiateCLController(ureq);
			listenTo(changelogPageCtrl);
		} else {
			changelogPageCtrl.refreshNewsList();
		}
		currentActivePageCtrl = changelogPageCtrl;
		disableLink_TOC(false);
		disableLINK_LC(true);
		addToHistory(ureq, OresHelper.createOLATResourceableType("CL"), null);
	}

	/**
	 * 
	 * @param ureq
	 * @return
	 */
	private EPChangelogController instantiateCLController(UserRequest ureq) {
		PageTab page = pageList.get(0);
		EPAbstractMap abstrMap = (EPAbstractMap)ePFMgr.loadStructureParent(page);
		return new EPChangelogController(ureq, getWindowControl(), abstrMap);
	}

	/**
	 * 
	 * @param ureq
	 * @param pageNumberToInit
	 * @param withComments
	 */
	private void setAndInitNormalPage(UserRequest ureq, int pageNumberToInit, boolean withComments) {
		PageTab tab = pageList.get(pageNumberToInit);
		PortfolioStructure structureElement = ePFMgr.loadPortfolioStructureByKey(tab);
		if(structureElement instanceof EPPage) {
			EPPage page = (EPPage)structureElement;
			PortfolioStructure map = ePFMgr.loadStructureParent(tab);
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(EPPage.class, page.getKey()), null);
			currentActivePageCtrl = new EPPageViewController(ureq, bwControl, map, page, withComments, secCallback);
			listenTo(currentActivePageCtrl);
			// enable toc and changelog links
			disableLink_TOC(false);
			disableLINK_LC(false);
		}
	}

	private void disableLink_TOC(boolean disable) {
		vC.contextPut("toc_enabled", disable);
	}

	private void disableLINK_LC(boolean disable) {
		vC.contextPut("changelog_enabled", disable);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link link = (Link) source;
			int pageNum = PAGENUM_TOC;
			try {
				pageNum = Integer.parseInt(link.getUserObject().toString());
			} catch (NumberFormatException nfe) {
				// somehow the link has a invalid pageNum, display the TOC
			}

			setAndInitActualPage(ureq, pageNum, false);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == currentActivePageCtrl) {
			if (source instanceof EPTOCReadOnlyController || source instanceof EPChangelogController) {
				// activate selected structure from toc or changelog
				if (event instanceof EPStructureEvent) {
					handleEPStructureEvent(ureq, (EPStructureEvent) event);
				} else if (event instanceof EPMapKeyEvent) {
					handleEPMapKeyEvent(ureq, (EPMapKeyEvent) event);
				}
			}
		}
	}

	/**
	 * 
	 * @param ureq
	 * @param epEv
	 */
	private void handleEPStructureEvent(UserRequest ureq, EPStructureEvent epEv) {
		PortfolioStructure selStruct = epEv.getStructure();
		if (epEv.getCommand().equals(EPStructureEvent.SELECT)) {
			findAndActivatePage(ureq, selStruct, false);
		} else if (epEv.getCommand().equals(EPStructureEvent.SELECT_WITH_COMMENTS)) {
			findAndActivatePage(ureq, selStruct, true);
		}
	}

	/**
	 * 
	 * @param ureq
	 * @param epEv
	 */
	private void handleEPMapKeyEvent(UserRequest ureq, EPMapKeyEvent epEv) {
		Long mapKey = epEv.getMapKey();
		if (mapKey != null && mapKey > 1) {
			findAndActivatePageByKey(ureq, mapKey);
		} else {
			setAndInitActualPage(ureq, PAGENUM_TOC, false);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty())
			return;

		OLATResourceable ores = entries.get(0).getOLATResourceable();
		if ("TOC".equals(ores.getResourceableTypeName())) {
			setAndInitActualPage(ureq, PAGENUM_TOC, false);
		} else if ("CL".equals(ores.getResourceableTypeName())) {
			setAndInitActualPage(ureq, PAGENUM_TOC, false);
		} else if ("EPPage".equals(ores.getResourceableTypeName())) {
			Long pageKey = ores.getResourceableId();
			if (containsPage(pageKey)) {
				int pos = indexOfPage(pageKey);
				if (pos != -1) {
					setAndInitActualPage(ureq, pos, false);
				}
			}
		}
	}

	private void findAndActivatePage(UserRequest ureq, PortfolioStructure selStruct, boolean withComments) {
		if (containsPage(selStruct.getKey())) {
			int pos = indexOfPage(selStruct.getKey());
			if (pos != -1)
				setAndInitActualPage(ureq, pos, withComments);
		} else {
			// lookup parents, as this could be an artefact or a
			// structureElement
			PortfolioStructure parentStruct = ePFMgr.loadStructureParent(selStruct);
			if (parentStruct != null)
				findAndActivatePage(ureq, parentStruct, withComments);
		}
	}

	private void findAndActivatePageByKey(UserRequest ureq, Long key) {
		if (containsPage(key)) {
			int pos = indexOfPage(key);
			if (pos != -1)
				setAndInitActualPage(ureq, pos, false);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing
	}
	
	private int indexOfPage(Long key) {
		if(key == null || pageList == null || pageList.isEmpty()) return -1;
		
		int count=0;
		for(PageTab pageTab:pageList) {
			if(pageTab.getKey().equals(key)) {
				break;
			}
			count++;
		}
		return count;
	}
	
	private boolean containsPage(Long key) {
		if(key == null || pageList == null || pageList.isEmpty()) return false;
		
		for(PageTab pageTab:pageList) {
			if(pageTab.getKey().equals(key)) {
				return true;
			}
		}
		return false;
	}
	
	private class PageTab implements PortfolioStructureRef {
		
		private final Long pageKey;
		private final String title;
		
		public PageTab(Long pageKey, String title) {
			this.pageKey = pageKey;
			this.title = title;
		}
		
		@Override
		public Long getKey() {
			return pageKey;
		}
		
		public String getTitle() {
			return title;
		}
	}
}
