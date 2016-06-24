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
		return new BinderSecurityCallbackImpl(true, binder.getTemplate() != null, binder.getEntry() != null);
	}
	
	public static final BinderSecurityCallback getCallbackForMyPageList() {
		return new BinderSecurityCallbackImpl(true, false, false);
	}
	
	public static final BinderSecurityCallback getReadOnlyCallback() {
		return new BinderSecurityCallbackImpl(false, false, false);
	}
	
	public static final BinderSecurityCallback getCallbackForTemplate(RepositoryEntrySecurity security) {
		return new BinderSecurityCallbackForTemplate(security.isEntryAdmin());
	}
	
	public static final BinderSecurityCallback getCallbackForCoach(Binder binder, List<AccessRights> rights) {
		return new BinderSecurityCallbackImpl(rights, binder.getTemplate() != null, binder.getEntry() != null);
	}
	
	private static class BinderSecurityCallbackForTemplate implements BinderSecurityCallback {
		
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
		public boolean canAddPage() {
			return false;
		}

		@Override
		public boolean canEditPage(Page page) {
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
	
	private static class BinderSecurityCallbackImpl implements BinderSecurityCallback {
		
		private final boolean owner;
		private final boolean hasEntry;
		private final boolean hasTemplate;
		private final boolean newSectionAllowed;
		private final List<AccessRights> rights;
		
		public BinderSecurityCallbackImpl(boolean owner, boolean hasTemplate, boolean hasEntry) {
			this.owner = owner;
			this.hasEntry = hasEntry;
			this.hasTemplate = hasTemplate;
			this.newSectionAllowed = !hasTemplate;
			this.rights = Collections.emptyList();
		}
		
		public BinderSecurityCallbackImpl(List<AccessRights> rights, boolean hasTemplate, boolean hasEntry) {
			this.owner = false;
			this.hasEntry = hasEntry;
			this.hasTemplate = hasTemplate;
			this.newSectionAllowed = false;
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
			return owner && newSectionAllowed;
		}

		@Override
		public boolean canEditSection() {
			return owner && newSectionAllowed;
		}
		
		@Override
		public boolean canAddPage() {
			return owner;
		}

		@Override
		public boolean canEditPage(Page page) {
			return owner && (page.getPageStatus() == null || page.getPageStatus() != PageStatus.closed);
		}

		@Override
		public boolean canEditAccessRights(PortfolioElement element) {
			return owner;
		}

		@Override
		public boolean canViewElement(PortfolioElement element) {
			if(owner) return true;
			
			//need to be recursive, if page -> section too -> binder too???
			if(rights != null) {
				for(AccessRights right:rights) {
					if(right.matchElement(element)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean canComment(PortfolioElement element) {
			if(rights != null) {
				for(AccessRights right:rights) {
					if(right.matchElement(element) && PortfolioRoles.reviewer.equals(right.getRole())) {
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
					if(right.matchElement(element) && PortfolioRoles.reviewer.equals(right.getRole())) {
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
					if(right.matchElement(section) && PortfolioRoles.coach.equals(right.getRole())) {
						return true;
					}
				}
			}
			return false;
		}
	}
}