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

package org.olat.modules.wiki.portfolio;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.wiki.Wiki;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiPage;
import org.olat.portfolio.EPAbstractHandler;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * Initial Date:  7 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class WikiArtefactHandler extends EPAbstractHandler<WikiArtefact>  {

	@Override
	public String getType() {
		return WikiArtefact.ARTEFACT_TYPE;
	}

	@Override
	public WikiArtefact createArtefact() {
		return new WikiArtefact();
	}
	
	/**
	 * @see org.olat.portfolio.EPAbstractHandler#prefillArtefactAccordingToSource(org.olat.portfolio.model.artefacts.AbstractArtefact, java.lang.Object)
	 */
	@Override
	public void prefillArtefactAccordingToSource(AbstractArtefact artefact, Object source) {
		super.prefillArtefactAccordingToSource(artefact, source);
		
		WikiPage page = null;
		OLATResourceable ores = null;
		if (source instanceof OLATResourceable){
			ores = (OLATResourceable) source;
			// fxdiff: FXOLAT-148 a wiki from a businessgroup needs to be wrapped accordingly!
			if (artefact.getBusinessPath().contains(BusinessGroup.class.getSimpleName())) {
				ores = OresHelper.createOLATResourceableInstance(BusinessGroup.class, ores.getResourceableId());
			}
			Wiki wiki = WikiManager.getInstance().getOrLoadWiki(ores);
			String pageName = getPageName(artefact.getBusinessPath());
			page = wiki.getPage(pageName, true);
		} else if (source instanceof WikiPage) {
			page = (WikiPage)source;
		}
		
		if(page != null) {
			artefact.setSource(getSourceInfo(artefact.getBusinessPath(), ores));
			artefact.setTitle(page.getPageName());
			artefact.setFulltextContent(page.getContent());
			artefact.setSignature(70);
		}
	}
	
	private String getSourceInfo(String businessPath, OLATResourceable ores){
		String sourceInfo = null;
		String[] parts = businessPath.split(":");
		if (parts.length<2) return sourceInfo;
		String id = parts[1].substring(0, parts[1].lastIndexOf("]"));
		if (parts[0].indexOf("BusinessGroup")!=-1){
			BusinessGroup bGroup = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(new Long(id));
			if (bGroup != null) sourceInfo = bGroup.getName();
		} else if (parts[0].indexOf("RepositoryEntry") != -1){
			RepositoryEntry repo = RepositoryManager.getInstance().lookupRepositoryEntry(ores, false);
			if (repo!= null) sourceInfo = repo.getDisplayname();
		}
		return sourceInfo;
	}
	
	private String getPageName(String businessPath) {
		int start = businessPath.lastIndexOf("page=");
		int stop = businessPath.lastIndexOf(":0]");
		if(start < stop && start > 0 && stop > 0) {
			return businessPath.substring(start + 5, stop);
		} else {
			return null;
		}
	}

	@Override
	public Controller createDetailsController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, boolean readOnlyMode) {
		return new WikiArtefactDetailsController(ureq, wControl, artefact);
	}
}