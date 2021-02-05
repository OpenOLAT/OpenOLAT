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
package org.olat.modules.portfolio;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.modules.assessment.Role;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.repository.RepositoryEntrySecurity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderSecurityCallbackFactory {
	
	public static final BinderSecurityCallback getCallbackForOwnedBinder(Binder binder) {
		Binder template = binder.getTemplate();
		BinderDeliveryOptions deliveryOptions = getDeliveryOptions(binder);
		boolean binderAssignments = hasBinderAssignments(binder);
		return new BinderSecurityCallbackImpl(true, template != null, binderAssignments, deliveryOptions);
	}
	
	public static final BinderSecurityCallback getCallbackForDeletedBinder() {
		return new BinderSecurityCallbackForDeletedBinder();
	}
	
	public static final BinderSecurityCallback getCallbackForMyPageList() {
		return new BinderSecurityCallbackImpl(true, false, true, null);
	}
	
	/**
	 * The callback for the deleted pages doens't allow anything.
	 * @return
	 */
	public static final BinderSecurityCallback getCallbackForDeletedPages() {
		return new BinderSecurityCallbackForDeletedPages();
	}
	
	public static final BinderSecurityCallback getReadOnlyCallback() {
		return new ReadOnlyBinderSecurityCallback();
	}
	
	public static final BinderSecurityCallback getCallbackForTemplate(RepositoryEntrySecurity security) {
		return new BinderSecurityCallbackForTemplate(security.isEntryAdmin());
	}
	
	public static final BinderSecurityCallback getCallbackForCoach(Binder binder, List<AccessRights> rights) {
		Binder template = binder.getTemplate();
		BinderDeliveryOptions deliveryOptions = getDeliveryOptions(binder);
		return new BinderSecurityCallbackImpl(rights, template != null, false, deliveryOptions);
	}
	
	public static final BinderSecurityCallback getCallbackForCourseCoach(Binder binder, List<AccessRights> rights) {
		Binder template = binder.getTemplate();
		BinderDeliveryOptions deliveryOptions = getDeliveryOptions(binder);
		return new BinderSecurityCallbackForCoach(rights, template != null, false, deliveryOptions);
	}
	
	/**
	 * Invitee can only comment binders
	 * @return
	 */
	public static final BinderSecurityCallback getCallbackForInvitation(List<AccessRights> rights) {
		return new BinderSecurityCallbackForInvitation(rights);
	}
	
	private static final BinderDeliveryOptions getDeliveryOptions(Binder binder) {
		Binder template = binder.getTemplate();
		BinderDeliveryOptions deliveryOptions = null;
		if(template != null) {
			deliveryOptions = CoreSpringFactory.getImpl(PortfolioService.class)
					.getDeliveryOptions(template.getOlatResource());
		}
		return deliveryOptions;
	}
	
	private static final boolean hasBinderAssignments(Binder binder) {
		Binder template = binder.getTemplate();
		boolean binderAssignments = false;
		if(template != null) {
			binderAssignments = CoreSpringFactory.getImpl(PortfolioService.class)
					.hasBinderAssignmentTemplate(binder);
		}
		return binderAssignments;
	}
	
	/**
	 * If you can see the business group, you can edit and view the binder.
	 * @return
	 */
	public static final BinderSecurityCallback getCallbackForBusinessGroup() {
		return new BinderSecurityCallbackGroup(true, false, false, null);
	}
	
	private static class ReadOnlyBinderSecurityCallback extends BinderSecurityCallbackImpl {
		
		public ReadOnlyBinderSecurityCallback() {
			super(false, false, false, null);
		}

		@Override
		public boolean canPageUserInfosStatus() {
			return false;
		}
	}

	private static class BinderSecurityCallbackForDeletedPages extends DefaultBinderSecurityCallback {

		@Override
		public boolean canRestorePage(Page page) {
			return page.getPageStatus() == PageStatus.deleted;
		}

		@Override
		public Role getRole() {
			return Role.user;
		}

	}
	
	private static class BinderSecurityCallbackForDeletedBinder extends DefaultBinderSecurityCallback {

		@Override
		public boolean canDeleteBinder(Binder binder) {
			return true;
		}

		@Override
		public boolean canViewAccessRights(PortfolioElement element) {
			return true;
		}

		@Override
		public boolean canViewAccessRights() {
			return true;
		}

		@Override
		public boolean canViewElement(PortfolioElement element) {
			return true;
		}

		@Override
		public boolean canViewPendingAssignments(Section section) {
			return true;
		}

		@Override
		public boolean canViewEmptySection(Section section) {
			return true;
		}

		@Override
		public boolean canViewAssess(PortfolioElement element) {
			return true;
		}

		@Override
		public boolean canViewAssessment() {
			return true;
		}	
	}

	/**
	 * Can only view / comment the pages, the sections they are allowed
	 * to view or the whole binder.
	 * 
	 * Initial date: 20.07.2016<br>
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 *
	 */
	private static class BinderSecurityCallbackForInvitation extends DefaultBinderSecurityCallback {
		
		private final List<AccessRights> rights;
		
		public BinderSecurityCallbackForInvitation(List<AccessRights> rights) {
			this.rights = rights;
		}
		
		@Override
		public boolean canComment(PortfolioElement element) {
			if(element instanceof Page) {
				Page page = (Page)element;
				if(page.getPageStatus() == null || page.getPageStatus() == PageStatus.draft) {
					return false;
				}
			}
			
			if(rights != null) {
				for(AccessRights right:rights) {
					if(right.getRole() == PortfolioRoles.readInvitee && right.matchElementAndAncestors(element)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean canViewElement(PortfolioElement element) {
			if(element instanceof Page) {
				Page page = (Page)element;
				if(page.getPageStatus() == null || page.getPageStatus() == PageStatus.draft) {
					if(rights != null) {
						for(AccessRights right:rights) {
							if(right.getRole() == PortfolioRoles.readInvitee && right.matchElementAndAncestors(element)) {
								return true;
							}
						}
					}
					return false;
				}
			}
			
			if(rights != null) {
				for(AccessRights right:rights) {
					if(right.getRole() == PortfolioRoles.readInvitee && right.matchElementAndAncestors(element)) {
						return true;
					}
				}
			}
			return false;
		}
	}
	
	private static class BinderSecurityCallbackForTemplate extends DefaultBinderSecurityCallback {
		
		private final boolean admin;
		
		public BinderSecurityCallbackForTemplate(boolean admin) {
			this.admin = admin;
		}

		@Override
		public boolean canEditBinder() {
			return admin;
		}

		@Override
		public boolean canEditMetadataBinder() {
			return admin;
		}
		
		@Override
		public boolean canAddSection() {
			return admin;
		}
		
		@Override
		public boolean canEditSection() {
			return admin;
		}
		
		@Override
		public boolean canSectionBeginAndEnd() {
			return true;
		}

		@Override
		public boolean canNewAssignment() {
			return admin;
		}

		@Override
		public boolean canViewElement(PortfolioElement element) {
			return true;
		}

		@Override
		public boolean canViewPendingAssignments(Section section) {
			return true;
		}

		@Override
		public boolean canNewBinderAssignment() {
			return admin;
		}
	}
	
	private static class BinderSecurityCallbackGroup extends BinderSecurityCallbackImpl {
		
		public BinderSecurityCallbackGroup(boolean owner, boolean task, boolean binderAssignments, BinderDeliveryOptions deliveryOptions) {
			super(owner, task, binderAssignments, deliveryOptions);
		}

		@Override
		public boolean canDeleteBinder(Binder binder) {
			return false;
		}
	}
	
	private static class BinderSecurityCallbackForCoach extends BinderSecurityCallbackImpl {

		public BinderSecurityCallbackForCoach(List<AccessRights> rights, boolean task, boolean binderAssignments, BinderDeliveryOptions deliveryOptions) {
			super(rights, task, binderAssignments, deliveryOptions);
		}

		@Override
		public boolean canViewAssess(PortfolioElement element) {
			return true;
		}

		@Override
		public boolean canDeleteBinder(Binder binder) {
			return false;
		}
		
		@Override
		public boolean canBookmark() {
			return true;
		}

		@Override
		public boolean canPageUserInfosStatus() {
			return true;
		}
	}
	
	private static class BinderSecurityCallbackImpl implements BinderSecurityCallback {
		
		/**
		 * The binder is linked to a template.
		 */
		private final boolean task;
		private final boolean owner;
		private final boolean binderAssignments;
		private final List<AccessRights> rights;
		private final BinderDeliveryOptions deliveryOptions;
		
		@Autowired
		private PortfolioService portfolioService;
		
		public BinderSecurityCallbackImpl(boolean owner, boolean task, boolean binderAssignments, BinderDeliveryOptions deliveryOptions) {
			CoreSpringFactory.autowireObject(this);
			this.task = task;
			this.owner = owner;
			this.rights = Collections.emptyList();
			this.deliveryOptions = deliveryOptions;
			this.binderAssignments = binderAssignments;
		}
		
		public BinderSecurityCallbackImpl(List<AccessRights> rights, boolean task, boolean binderAssignments, BinderDeliveryOptions deliveryOptions) {
			CoreSpringFactory.autowireObject(this);
			this.owner = false;
			this.task = task;
			this.rights = rights;
			this.deliveryOptions = deliveryOptions;
			this.binderAssignments = binderAssignments;
		}
		
		@Override
		public boolean canEditBinder() {
			return owner;
		}

		@Override
		public boolean canExportBinder() {
			return owner;
		}

		@Override
		public boolean canMoveToTrashBinder(Binder binder) {
			if(owner) {
				if(binder.getBinderStatus() == null || binder.getBinderStatus() == BinderStatus.open) {
					if(task) {
						return deliveryOptions.isAllowDeleteBinder();
					} else {
						return true;
					}
				}
			}
			return false;
		}
		
		@Override
		public boolean canDeleteBinder(Binder binder) {
			if(owner) {
				if(binder.getBinderStatus() == BinderStatus.deleted) {
					if(task) {
						return deliveryOptions.isAllowDeleteBinder();
					} else {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean canEditMetadataBinder() {
			return owner && !task;
		}

		@Override
		public boolean canAddSection() {
			return owner && !task;
		}

		@Override
		public boolean canEditSection() {
			return owner && !task;
		}

		@Override
		public boolean canCloseSection(Section section) {
			if(task && rights != null) {
				for(AccessRights right:rights) {
					if(PortfolioRoles.coach.equals(right.getRole())
							&& right.matchElementAndAncestors(section)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean canSectionBeginAndEnd() {
			return false;
		}

		@Override
		public boolean canNewAssignment() {
			return false;
		}

		@Override
		public boolean canNewBinderAssignment() {
			return false;
		}

		@Override
		public boolean canInstantiateAssignment() {
			return owner;
		}

		@Override
		public boolean canInstantianteBinderAssignment() {
			return owner && deliveryOptions != null && deliveryOptions.isAllowTemplatesFolder() && binderAssignments;
		}

		@Override
		public boolean canAddPage(Section section) {
			if(section == null) {
				return owner && (deliveryOptions == null || deliveryOptions.isAllowNewEntries());
			}
			return owner && !SectionStatus.isClosed(section)
					&& (deliveryOptions == null || deliveryOptions.isAllowNewEntries());
		}
		
		@Override
		public boolean canNewPageWithoutAssignment() {
			return deliveryOptions == null || deliveryOptions.isOptionalTemplateForEntry();
		}

		/**
		 * The owner can only edit the page in task which are in draft or in revision.
		 * Free binder, without task are editable until the page is closed.
		 */
		@Override
		public boolean canEditPage(Page page) {
			return owner &&
					(
							(task && (page.getPageStatus() == null || page.getPageStatus() == PageStatus.draft || page.getPageStatus() == PageStatus.inRevision))
							||
							(!task && !PageStatus.isClosed(page))
					)
					&& !portfolioService.isPageBodyClosed(page);
		}

		@Override
		public boolean canEditPageMetadata(Page page, List<Assignment> assignments) {
			if(owner) {
				if(task) {
					return assignments == null || assignments.isEmpty();
				}
				return true;
			}
			return false;
		}
		
		/**
		 * Owner can always edit page categories, regardless of the page state
		 */
		@Override
		public boolean canEditCategories(Page page) {
			return owner;
		}

		@Override
		public boolean canPublish(Page page) {
			return owner && (page.getPageStatus() == null || page.getPageStatus() == PageStatus.draft || page.getPageStatus() == PageStatus.inRevision);
		}

		@Override
		public boolean canRevision(Page page) {
			if(owner) return false;
			
			if(rights != null && page.getPageStatus() == PageStatus.published) {
				for(AccessRights right:rights) {
					if(PortfolioRoles.coach.equals(right.getRole())
							&& right.matchElementAndAncestors(page)) {
						return true;
					}
				}
			}
			
			return false;
		}

		@Override
		public boolean canDeletePage(Page page) {
			return owner && page.getPageStatus() != PageStatus.published
					&& page.getPageStatus() != PageStatus.closed
					&& page.getPageStatus() != PageStatus.deleted;
		}
		
		@Override
		public boolean canRestorePage(Page page) {
			return owner && page.getPageStatus() == PageStatus.deleted;
		}

		@Override
		public boolean canClose(Page page) {
			if(owner) {
				return !task && !PageStatus.isClosed(page);
			}
			
			if(rights != null && (page.getPageStatus() == PageStatus.published || page.getPageStatus() == PageStatus.inRevision)) {
				for(AccessRights right:rights) {
					if(PortfolioRoles.coach.equals(right.getRole())
							&& right.matchElementAndAncestors(page)) {
						return true;
					}
				}
			}
			
			return false;
		}

		@Override
		public boolean canReopen(Page page) {
			if(owner) {
				return !task && PageStatus.isClosed(page);
			}
			
			if(rights != null && PageStatus.isClosed(page) && page.getPageStatus() != PageStatus.published) {
				for(AccessRights right:rights) {
					if(PortfolioRoles.coach.equals(right.getRole())
							&& right.matchElementAndAncestors(page)) {
						return true;
					}
				}
			}
			
			return false;
		}

		@Override
		public boolean canEditAccessRights(PortfolioElement element) {
			return owner;
		}

		@Override
		public boolean canViewAccessRights(PortfolioElement element) {
			if(owner) return true;

			if(rights != null) {
				for(AccessRights right:rights) {
					if(PortfolioRoles.coach.equals(right.getRole())
							&& right.matchElementAndAncestors(element)) {
						return true;
					}
				}
			}
			
			return false;
		}

		@Override
		public boolean canViewAccessRights() {
			if(owner) return true;
			
			if(rights != null) {
				for(AccessRights right:rights) {
					if(PortfolioRoles.coach.equals(right.getRole())) {
						return true;
					}
				}
			}
			
			return false;
		}

		@Override
		public boolean canViewEmptySection(Section section) {
			if(owner) return true;
			
			//need to be recursive, if page -> section too -> binder too???
			if(rights != null) {
				for(AccessRights right:rights) {
					if(PortfolioRoles.coach.equals(right.getRole())
							&& right.matchElementAndAncestors(section)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean canViewElement(PortfolioElement element) {
			if(owner) {
				if(task) {
					if(element instanceof Section) {
						Section section = (Section)element;
						if(section.getBeginDate() != null && section.getBeginDate().after(new Date())) {
							return false;
						}
					} else if(element instanceof Page) {
						Page page = (Page)element;
						Section section = page.getSection();
						if(section != null && section.getBeginDate() != null && section.getBeginDate().after(new Date())) {
							return false;
						}
					}
				}
				return true;
			}
			
			if(element instanceof Page) {
				Page page = (Page)element;
				if(page.getPageStatus() == null || page.getPageStatus() == PageStatus.draft) {
					return false;
				}
			}
			
			//need to be recursive, if page -> section too -> binder too???
			if(rights != null) {
				for(AccessRights right:rights) {
					if((PortfolioRoles.reviewer.equals(right.getRole()) || PortfolioRoles.coach.equals(right.getRole()))
							&& right.matchElementAndAncestors(element)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean canViewTitleOfElement(PortfolioElement element) {
			if(owner) {
				if(task) {
					if(element instanceof Section) {
						Section section = (Section)element;
						if(section.getBeginDate() != null && section.getBeginDate().after(new Date())) {
							return false;
						}
					} else if(element instanceof Page) {
						Page page = (Page)element;
						Section section = page.getSection();
						if(section != null && section.getBeginDate() != null && section.getBeginDate().after(new Date())) {
							return false;
						}
					}
				}
				return true;
			}
			
			if(element instanceof Page) {
				Page page = (Page)element;
				if(page.getPageStatus() == null || page.getPageStatus() == PageStatus.draft) {
					//need to be recursive, if page -> section too -> binder too???
					if(rights != null) {
						for(AccessRights right:rights) {
							if((PortfolioRoles.reviewer.equals(right.getRole()) || PortfolioRoles.coach.equals(right.getRole()))
									&& right.matchElementAndAncestors(element)) {
								return true;
							}
						}
					}
					return owner;
				}
			}
			
			//need to be recursive, if page -> section too -> binder too???
			if(rights != null) {
				for(AccessRights right:rights) {
					if((PortfolioRoles.reviewer.equals(right.getRole()) || PortfolioRoles.coach.equals(right.getRole()))
							&& right.matchElementAndAncestors(element)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean canViewPendingAssignments(Section section) {
			if(owner) {
				Date beginDate = section.getBeginDate();
				if(beginDate != null && beginDate.after(new Date())) {
					return false;
				}
				return true;
			}
			
			if(rights != null) {
				for(AccessRights right:rights) {
					if((PortfolioRoles.reviewer.equals(right.getRole()) || PortfolioRoles.coach.equals(right.getRole()))
							&& right.matchElementAndAncestors(section)) {
						return true;
					}
				}
			}

			return false;
		}

		@Override
		public boolean canComment(PortfolioElement element) {
			if(element.getType() == PortfolioElementType.page) {
				Page page = (Page)element;
				if(page.getPageStatus() == null || page.getPageStatus() == PageStatus.draft) {
					return false;
				}
			}

			if(owner) return true;
			
			if(rights != null) {
				for(AccessRights right:rights) {
					if((PortfolioRoles.reviewer.equals(right.getRole()) || PortfolioRoles.coach.equals(right.getRole()))
							&& right.matchElementAndAncestors(element)) {
						return true;
					}
				}
			}
			return true;
		}

		@Override
		public boolean canReview(PortfolioElement element) {
			if(rights != null) {
				for(AccessRights right:rights) {
					if(PortfolioRoles.reviewer.equals(right.getRole()) && right.matchElementAndAncestors(element)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean canViewAssess(PortfolioElement element) {
			if(owner) return true;
			return canAssess(element);
		}

		@Override
		public boolean canAssess(PortfolioElement element) {
			if(rights != null) {
				for(AccessRights right:rights) {
					if(PortfolioRoles.coach.equals(right.getRole()) && right.matchElementAndAncestors(element)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean canViewAssessment() {
			if(owner) {
				return true;
			}
			if(rights != null) {
				for(AccessRights right:rights) {
					if(PortfolioRoles.coach.equals(right.getRole())) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean canBookmark() {
			return !owner;
		}

		@Override
		public boolean canPageUserInfosStatus() {
			return !owner;
		}

		@Override
		public Role getRole() {
			return owner ? Role.user : Role.coach;
		}
	}
	
	private static class DefaultBinderSecurityCallback implements BinderSecurityCallback {

		@Override
		public boolean canEditBinder() {
			return false;
		}

		@Override
		public boolean canExportBinder() {
			return false;
		}

		@Override
		public boolean canMoveToTrashBinder(Binder binder) {
			return false;
		}

		@Override
		public boolean canDeleteBinder(Binder binder) {
			return false;
		}

		@Override
		public boolean canEditMetadataBinder() {
			return false;
		}

		@Override
		public boolean canAddSection() {
			return false;
		}

		@Override
		public boolean canEditSection() {
			return false;
		}

		@Override
		public boolean canCloseSection(Section section) {
			return false;
		}

		@Override
		public boolean canSectionBeginAndEnd() {
			return false;
		}

		@Override
		public boolean canNewAssignment() {
			return false;
		}
		
		@Override
		public boolean canNewBinderAssignment() {
			return false;
		}

		@Override
		public boolean canInstantiateAssignment() {
			return false;
		}

		@Override
		public boolean canInstantianteBinderAssignment() {
			return false;
		}

		@Override
		public boolean canAddPage(Section section) {
			return false;
		}

		@Override
		public boolean canNewPageWithoutAssignment() {
			return false;
		}

		@Override
		public boolean canEditPage(Page page) {
			return false;
		}

		@Override
		public boolean canEditPageMetadata(Page page, List<Assignment> assignments) {
			return false;
		}
		
		@Override
		public boolean canEditCategories(Page page) {
			return false;
		}
		
		@Override
		public boolean canPublish(Page page) {
			return false;
		}

		@Override
		public boolean canRevision(Page page) {
			return false;
		}

		@Override
		public boolean canClose(Page page) {
			return false;
		}

		@Override
		public boolean canReopen(Page page) {
			return false;
		}

		@Override
		public boolean canDeletePage(Page page) {
			return false;
		}

		@Override
		public boolean canRestorePage(Page page) {
			return false;
		}

		@Override
		public boolean canEditAccessRights(PortfolioElement element) {
			return false;
		}

		@Override
		public boolean canViewAccessRights(PortfolioElement element) {
			return false;
		}

		@Override
		public boolean canViewAccessRights() {
			return false;
		}

		@Override
		public boolean canViewElement(PortfolioElement element) {
			return false;
		}

		@Override
		public boolean canViewTitleOfElement(PortfolioElement element) {
			return canViewElement(element);
		}

		@Override
		public boolean canViewPendingAssignments(Section section) {
			return false;
		}

		@Override
		public boolean canViewEmptySection(Section section) {
			return false;
		}

		@Override
		public boolean canComment(PortfolioElement element) {
			return false;
		}

		@Override
		public boolean canReview(PortfolioElement element) {
			return false;
		}

		@Override
		public boolean canAssess(PortfolioElement element) {
			return false;
		}

		@Override
		public boolean canViewAssess(PortfolioElement element) {
			return false;
		}

		@Override
		public boolean canViewAssessment() {
			return false;
		}

		@Override
		public boolean canBookmark() {
			return false;
		}

		@Override
		public boolean canPageUserInfosStatus() {
			return false;
		}

		@Override
		public Role getRole() {
			return Role.coach;
		}
	}
}