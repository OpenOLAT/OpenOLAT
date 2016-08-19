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
import java.util.List;

import org.olat.modules.portfolio.model.AccessRights;
import org.olat.repository.model.RepositoryEntrySecurity;

/**
 * 
 * Initial date: 22.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderSecurityCallbackFactory {
	
	public static final BinderSecurityCallback getCallbackForOwnedBinder(Binder binder) {
		return new BinderSecurityCallbackImpl(true, binder.getTemplate() != null);
	}
	
	public static final BinderSecurityCallback getCallbackForMyPageList() {
		return new BinderSecurityCallbackImpl(true, false);
	}
	
	/**
	 * The callback for the deleted pages doens't allow anything.
	 * @return
	 */
	public static final BinderSecurityCallback getCallbackForDeletedPages() {
		return new BinderSecurityCallbackForDeletedPages();
	}
	
	public static final BinderSecurityCallback getReadOnlyCallback() {
		return new BinderSecurityCallbackImpl(false, false);
	}
	
	public static final BinderSecurityCallback getCallbackForTemplate(RepositoryEntrySecurity security) {
		return new BinderSecurityCallbackForTemplate(security.isEntryAdmin());
	}
	
	public static final BinderSecurityCallback getCallbackForCoach(Binder binder, List<AccessRights> rights) {
		return new BinderSecurityCallbackImpl(rights, binder.getTemplate() != null);
	}
	
	/**
	 * Invitee can only comment binders
	 * @return
	 */
	public static final BinderSecurityCallback getCallbackForInvitation(List<AccessRights> rights) {
		return new BinderSecurityCallbackForInvitation(rights);
	}
	
	/**
	 * If you can see the business group, you can edit and view the binder.
	 * @return
	 */
	public static final BinderSecurityCallback getCallbackForBusinessGroup() {
		return new BinderSecurityCallbackImpl(true, false);
	}

	
	private static class BinderSecurityCallbackForDeletedPages extends DefaultBinderSecurityCallback {
		//all false;	
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
	}
	
	private static class BinderSecurityCallbackImpl implements BinderSecurityCallback {
		
		/**
		 * The binder is linked to a template.
		 */
		private final boolean task;
		private final boolean owner;
		private final List<AccessRights> rights;
		
		public BinderSecurityCallbackImpl(boolean owner, boolean task) {
			this.task = task;
			this.owner = owner;
			this.rights = Collections.emptyList();
		}
		
		public BinderSecurityCallbackImpl(List<AccessRights> rights, boolean task) {
			this.owner = false;
			this.task = task;
			this.rights = rights;
		}
		
		@Override
		public boolean canEditBinder() {
			return owner;
		}

		@Override
		public boolean canEditMetadataBinder() {
			return owner;
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
		public boolean canInstantiateAssignment() {
			return owner;
		}

		@Override
		public boolean canAddPage(Section section) {
			if(section == null) {
				return owner;
			}
			if(owner) {
				return section != null
						&& !SectionStatus.isClosed(section)
						&& section.getSectionStatus() != SectionStatus.submitted;
			}
			return false;
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
					);
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
			
			if(rights != null && PageStatus.isClosed(page)) {
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
				return true;
			}
			
			if(element instanceof Page) {
				Page page = (Page)element;
				if(page.getPageStatus() == null || page.getPageStatus() == PageStatus.draft) {
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
		public boolean canComment(PortfolioElement element) {
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
		public boolean canAssess(Section section) {
			if(rights != null) {
				for(AccessRights right:rights) {
					if(PortfolioRoles.coach.equals(right.getRole()) && right.matchElementAndAncestors(section)) {
						return true;
					}
				}
			}
			return false;
		}
	}
	
	private static class DefaultBinderSecurityCallback implements BinderSecurityCallback {

		@Override
		public boolean canEditBinder() {
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
		public boolean canInstantiateAssignment() {
			return false;
		}

		@Override
		public boolean canAddPage(Section section) {
			return false;
		}

		@Override
		public boolean canEditPage(Page page) {
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
		public boolean canEditAccessRights(PortfolioElement element) {
			return false;
		}

		@Override
		public boolean canViewElement(PortfolioElement element) {
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
		public boolean canAssess(Section section) {
			return false;
		}
	}
}