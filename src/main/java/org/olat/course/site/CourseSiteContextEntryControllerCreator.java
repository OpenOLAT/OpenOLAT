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

import org.olat.basesecurity.Invitation;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.UserSession;
import org.olat.course.site.model.CourseSiteConfiguration;
import org.olat.course.site.model.LanguageConfiguration;
import org.olat.modules.catalog.ui.CatalogRepositoryEntryHeaderConfig;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.invitation.InvitationModule;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.invitation.InvitationStatusEnum;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.AccessDeniedFactory;
import org.olat.repository.ui.list.DetailsHeaderConfig;
import org.olat.repository.ui.list.RepositoryEntryInfosController;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.model.SearchReservationParameters;

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
			return AccessDeniedFactory.createRepositoryEntryDoesNotExist(ureq, wControl);
		}
		
		UserSession usess = ureq.getUserSession();
		if(usess.isInLockModeProcess() && !usess.matchPrimaryLockResource(re.getOlatResource())) {
			return null;
		}
		
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntrySecurity reSecurity = rm.isAllowed(ureq, re);
		Roles roles = usess.getRoles();
		if(re.getEntryStatus() == RepositoryEntryStatusEnum.trash || re.getEntryStatus() == RepositoryEntryStatusEnum.deleted) {
			if(!reSecurity.isEntryAdmin() && !roles.isLearnResourceManager() && !roles.isAdministrator()) {
				return AccessDeniedFactory.createRepositoryEntryDeleted(ureq, wControl);
			}
		}

		if (!reSecurity.canLaunch() && !reSecurity.isMember() && tryInvitation(re, usess.getIdentity())) {
			reSecurity = rm.isAllowed(ureq, re);
		}

		CurriculumService curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);
		if (!reSecurity.canLaunch()) {
			if(re.getEntryStatus() == RepositoryEntryStatusEnum.closed) {
				return AccessDeniedFactory.createRepositoryStatusClosed(ureq, wControl);
			} else if (reSecurity.isMember() && CoreSpringFactory.getImpl(ACService.class).isAccessRefusedByStatus(re, usess.getIdentity())) {
				CurriculumElement curriculumElement = isInSingleCourseImplementation(curriculumService, re);
				return AccessDeniedFactory.createRepositoryEntryStatusNotPublished(ureq, wControl, curriculumElement, re, reSecurity.isParticipant());
			} else if (reSecurity.isMember() || reSecurity.isMasterCoach()) {
				CurriculumElement curriculumElement = isInSingleCourseImplementation(curriculumService, re);
				return AccessDeniedFactory.createRepositoryEntryStatusNotPublished(ureq, wControl, curriculumElement, re, reSecurity.isParticipant());
			}
		}
		
		ACService acService = CoreSpringFactory.getImpl(ACService.class);
		if (!reSecurity.canLaunch() && !reSecurity.isMember() && !roles.isInviteeOnly() && !roles.isGuestOnly()) {
			SearchReservationParameters searchParams;
			List<CurriculumElement> curriculumElements = curriculumService.getCurriculumElements(re);
			if (!curriculumElements.isEmpty()) {
				List<OLATResource> resources = curriculumElements.stream().map(CurriculumElement::getResource).toList();
				searchParams = new SearchReservationParameters(resources);
			} else {
				searchParams = new SearchReservationParameters(List.of(re.getOlatResource()));
			}
			searchParams.setIdentities(List.of(ureq.getIdentity()));
			List<ResourceReservation> reservations = acService.getReservations(searchParams);
			if (!reservations.isEmpty()) {
				CurriculumElement curriculumElement = isInSingleCourseImplementation(curriculumService, re);
				return AccessDeniedFactory.createReservationPending(ureq, wControl, curriculumElement, re, reservations, reSecurity.isParticipant());
			}
		}
		
		if (!reSecurity.canLaunch() && !reSecurity.isMember() && !roles.isInviteeOnly() && re.isPublicVisible()
				&& re.getRuntimeType() == RepositoryEntryRuntimeType.standalone) {
			if (acService.isAccessToResourcePending(re.getOlatResource(), ureq.getIdentity())) {
				return AccessDeniedFactory.createBookingPending(ureq, wControl, null, re, reSecurity.isParticipant());
			}
			AccessResult acResult = acService.isAccessible(re, ureq.getIdentity(), Boolean.FALSE, roles.isGuestOnly(), null, false);
			if (!acResult.isAccessible()) {
				if (!acResult.getAvailableMethods().isEmpty()) {
					boolean autoBooked = tryAutoBooking(ureq, acService, acResult);
					if (autoBooked) {
						reSecurity = rm.isAllowed(ureq, re);
					} else {
						DetailsHeaderConfig config = new CatalogRepositoryEntryHeaderConfig(re, ureq.getIdentity(), roles, true);
						return new RepositoryEntryInfosController(ureq, wControl, re, config, false);
					}
				} else {
					List<? extends OrganisationRef> offerOrganisations = acService.getOfferOrganisations(ureq.getIdentity());
					List<Offer> offersNotInRange = acService.getOffers(re, true, false, null, true, null, offerOrganisations);
					if (!offersNotInRange.isEmpty()) {
						return AccessDeniedFactory.createOfferNotNow(ureq, wControl, offersNotInRange);
					}
				}
			}
		}
		
		if (!reSecurity.canLaunch()) {
			 if (roles.isGuestOnly()) {
				return AccessDeniedFactory.createNoGuestAccess(ureq, wControl);
			} else if (AccessDeniedFactory.isNotInAuthorOrganisation(re, roles)) {
				return AccessDeniedFactory.createNotInAuthorOrganisation(ureq, wControl, ureq.getIdentity());
			} else if (!reSecurity.isMember()) {
				return AccessDeniedFactory.createNotMember(ureq, wControl, re);
			} else {
				return AccessDeniedFactory.createNoAccess(ureq, wControl);
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
	
	private boolean tryInvitation(RepositoryEntry re, Identity identity) {
		boolean hasInvitation = false;
		InvitationModule invitationModule = CoreSpringFactory.getImpl(InvitationModule.class);
		if(invitationModule.isInvitationEnabled() && invitationModule.isCourseInvitationEnabled()) {
			InvitationService invitationService = CoreSpringFactory.getImpl(InvitationService.class);
			Invitation invitation = invitationService.findInvitation(re, identity);
			if(invitation != null && invitation.getStatus() == InvitationStatusEnum.active) {
				invitationService.acceptInvitation(invitation, identity);
				hasInvitation = true;
			}
		}
		return hasInvitation;
	}
	
	private boolean tryAutoBooking(UserRequest ureq, ACService acService, AccessResult acResult) {
		if(acResult.getAvailableMethods().size() == 1) {
			OfferAccess offerAccess = acResult.getAvailableMethods().get(0);
			if(offerAccess.getOffer().isAutoBooking() && !offerAccess.getMethod().isNeedUserInteraction()) {
				acResult = acService.accessResource(ureq.getIdentity(), offerAccess, OrderStatus.PAYED, null, ureq.getIdentity());
				 if(acResult.isAccessible()) {
					 return true;
				 }
			}
		}
		return false;
	}
	
	private CurriculumElement isInSingleCourseImplementation(CurriculumService curriculumService, RepositoryEntry entry) {
		List<CurriculumElement> elements = curriculumService.getCurriculumElements(entry);
		if(elements.size() == 1 && elements.get(0).isSingleCourseImplementation()) {
			return elements.get(0);
		}
		return null;
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
			if(siteDef instanceof CourseSiteDef courseSiteDef) {
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
