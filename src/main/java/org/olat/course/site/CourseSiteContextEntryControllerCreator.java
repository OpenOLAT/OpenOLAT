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
package org.olat.course.site;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.course.site.model.CourseSiteConfiguration;
import org.olat.course.site.model.LanguageConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;

/**
 * <h3>Description:</h3>
 * <p>
 * This class can create run controllers for repository entries in the given
 * context
 * <p>
 * Initial Date: 19.08.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class CourseSiteContextEntryControllerCreator extends DefaultContextEntryControllerCreator {

	private RepositoryEntry repoEntry;
	private SiteDefinitions siteDefinitions;
	
	@Override
	public ContextEntryControllerCreator clone() {
		return new CourseSiteContextEntryControllerCreator();
	}

	@Override
	public Controller createController(List<ContextEntry> ces, UserRequest ureq, WindowControl wControl) {
		RepositoryEntry re = getRepositoryEntry(ureq, ces.get(0));
		return createLaunchController(re, ureq, wControl);
	}
	
	/**
	 * Create a launch controller used to launch the given repo entry.
	 * @param re
	 * @param initialViewIdentifier if null the default view will be started, otherwise a controllerfactory type dependant view will be activated (subscription subtype)
	 * @param ureq
	 * @param wControl
	 * @return null if no entry was found, a no access message controller if not allowed to launch or the launch 
	 * controller if successful.
	 */
	private Controller createLaunchController(RepositoryEntry re, UserRequest ureq, WindowControl wControl) {
		if (re == null) {
			return messageController(ureq, wControl, "repositoryentry.not.existing");
		}
		
		UserSession usess = ureq.getUserSession();
		if(usess.isInAssessmentModeProcess() && !usess.matchPrimaryLockResource(re.getOlatResource())) {
			return null;
		}
		
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntrySecurity reSecurity = rm.isAllowed(ureq, re);
		Roles roles = usess.getRoles();
		if(re.getEntryStatus() == RepositoryEntryStatusEnum.trash || re.getEntryStatus() == RepositoryEntryStatusEnum.deleted) {
			if(!reSecurity.isEntryAdmin() && !roles.isLearnResourceManager() && !roles.isAdministrator()) {
				return messageController(ureq, wControl, "repositoryentry.deleted");
			}
		}
		
		if (!reSecurity.canLaunch()) {
			if(isPublicVisible(re, reSecurity, ureq.getIdentity(), roles)) {
				reSecurity = rm.isAllowed(ureq, re);
			} else {
				return messageController(ureq, wControl, "launch.noaccess");
			}
		}

		RepositoryService rs = CoreSpringFactory.getImpl(RepositoryService.class);
		rs.incrementLaunchCounter(re);
		RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(re);
	
		WindowControl bwControl;
		OLATResourceable businessOres = re;
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(businessOres);
		if(ce.equals(wControl.getBusinessControl().getCurrentContextEntry())) {
			bwControl = wControl;
		} else {
			bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, wControl);
		}
		
		MainLayoutController ctrl = handler.createLaunchController(re, reSecurity, ureq, bwControl);
		if (ctrl == null) {
			throw new AssertException("could not create controller for repositoryEntry "+re); 
		}
		return ctrl;	
	}
	
	private boolean isPublicVisible(RepositoryEntry re, RepositoryEntrySecurity reSecurity, Identity identity, Roles roles) {
		if (re.isPublicVisible()) {
			AccessResult accessResult = CoreSpringFactory.getImpl(ACService.class).isAccessible(re, identity, reSecurity.isMember(), roles.isGuestOnly(), true);
			return accessResult.isAccessible() || !accessResult.getAvailableMethods().isEmpty();
		}
		return false;
	}

	private Controller messageController(UserRequest ureq, WindowControl wControl, String i18nMesageKey) {
		Translator trans = Util.createPackageTranslator(RepositoryService.class, ureq.getLocale());
		String text = trans.translate(i18nMesageKey);
		Controller c = MessageUIFactory.createInfoMessage(ureq, wControl, null, text);
		
		// use on column layout
		LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, c);
		layoutCtr.addDisposableChildController(c); // dispose content on layout dispose
		return layoutCtr;
	}

	@Override
	public String getTabName(ContextEntry ce, UserRequest ureq) {
		RepositoryEntry re = getRepositoryEntry(ureq, ce);
		CourseSiteDef siteDef = getCourseSite(ureq, re);
		if(siteDef != null) {
			return "Hello";
		}
		return re == null ? "" : re.getDisplayname();
	}
	
	@Override
	public String getSiteClassName(List<ContextEntry> ces, UserRequest ureq) {
		RepositoryEntry re = getRepositoryEntry(ureq, ces.get(0));
		CourseSiteDef siteDef = getCourseSite(ureq, re);
		if(siteDef != null) {
			return siteDef.getClass().getName().replace("Def", "");
		}
		return null;
	}
	
	@Override
	public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
		return getRepositoryEntry(ureq, ce) != null;
	}

	private SiteDefinitions getSitesDefinitions() {
		if(siteDefinitions == null) {
			siteDefinitions = CoreSpringFactory.getImpl(SiteDefinitions.class);
		}
		return siteDefinitions;
	}
	
	private CourseSiteDef getCourseSite(UserRequest ureq, RepositoryEntry re) {
		if(re == null) return null;
		
		List<SiteDefinition> siteDefList = getSitesDefinitions().getSiteDefList();
		for(SiteDefinition siteDef:siteDefList) {
			if(siteDef instanceof CourseSiteDef) {
				CourseSiteDef courseSiteDef = (CourseSiteDef)siteDef;
				CourseSiteConfiguration config = courseSiteDef.getCourseSiteconfiguration();
				LanguageConfiguration langConfig = courseSiteDef.getLanguageConfiguration(ureq, config);
				if(langConfig == null) continue;
				
				String softKey = langConfig.getRepoSoftKey();
				if(re.getSoftkey() != null && re.getSoftkey().equals(softKey)) {
					return courseSiteDef;
				}
			}
		}
		return null;
	}
	
	private RepositoryEntry getRepositoryEntry(UserRequest ureq, ContextEntry ce) {
		if(repoEntry == null) {
			if(ce.getOLATResourceable() instanceof RepositoryEntry) {
				repoEntry = (RepositoryEntry)ce.getOLATResourceable();
			} else {
				OLATResourceable ores = ce.getOLATResourceable();
				if("CourseSite".equals(ores.getResourceableTypeName())) {
					int id = ores.getResourceableId().intValue();
					CourseSiteDef courseSiteDef = null;
					List<SiteDefinition> siteDefList = getSitesDefinitions().getSiteDefList();
					if(id == 2) {
						for(SiteDefinition siteDef:siteDefList) {
							if(siteDef instanceof CourseSiteDef2) {
								courseSiteDef = (CourseSiteDef)siteDef;
							}
						}
					} else if(id == 1) {
						for(SiteDefinition siteDef:siteDefList) {
							if(siteDef instanceof CourseSiteDef) {
								courseSiteDef = (CourseSiteDef)siteDef;
							}
						}
					}
					
					if(courseSiteDef != null) {
						CourseSiteConfiguration config = courseSiteDef.getCourseSiteconfiguration();
						LanguageConfiguration langConfig = courseSiteDef.getLanguageConfiguration(ureq, config);
						if(langConfig != null) {
							String softKey = langConfig.getRepoSoftKey();
							RepositoryManager rm = RepositoryManager.getInstance();
							repoEntry = rm.lookupRepositoryEntryBySoftkey(softKey, false);
						}
					}
				} else {
					RepositoryManager rm = RepositoryManager.getInstance();
					repoEntry = rm.lookupRepositoryEntry(ores.getResourceableId());
				}
			}
		}
		return repoEntry;
	}
}
