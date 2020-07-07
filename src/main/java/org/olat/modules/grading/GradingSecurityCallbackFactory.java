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
package org.olat.modules.grading;

import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.grading.model.GradingSecurity;

/**
 * 
 * Initial date: 27 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingSecurityCallbackFactory {
	
	public static final GradingSecurityCallback getManagerCalllback(Identity identity, Roles roles) {
		return new GradingSecurityCallbackImpl(identity, false, true, roles);
	}
	
	public static final GradingSecurityCallback getSecurityCalllback(Identity identity, GradingSecurity gradingSec) {
		return new GradingSecurityCallbackImpl(identity, gradingSec.isGrader(),
				gradingSec.isGradedResourcesManager(), gradingSec.getRoles());
	}
	
	public static final GradingSecurityCallback mySecurityCalllback(GradingSecurityCallback secCallback) {
		return new MyGradingSecurityCallback(secCallback);
	}
	
	private static class MyGradingSecurityCallback implements GradingSecurityCallback {
		
		private final GradingSecurityCallback delegate;
		
		public MyGradingSecurityCallback(GradingSecurityCallback delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean canManage() {
			return false;
		}

		@Override
		public boolean canGrade() {
			return delegate.canGrade();
		}

		@Override
		public boolean canGrade(GradingAssignment assignment) {
			return delegate.canGrade(assignment);
		}

		@Override
		public boolean canReport() {
			return false;
		}

		@Override
		public boolean canViewRecordedRealMinutes() {
			return delegate.canViewRecordedRealMinutes();
		}
	}
	
	private static class GradingSecurityCallbackImpl implements GradingSecurityCallback {
		
		private final boolean grader;
		private final boolean manager;
		private final Roles roles;
		private final Identity identity;
		
		private final GradingModule gradingModule;
		
		public GradingSecurityCallbackImpl(Identity identity, boolean grader, boolean manager, Roles roles) {
			this.roles = roles;
			this.grader = grader;
			this.manager = manager;
			this.identity = identity;
			gradingModule = CoreSpringFactory.getImpl(GradingModule.class);
		}

		@Override
		public boolean canManage() {
			return manager;
		}
		
		@Override
		public boolean canReport() {
			return manager;
		}

		@Override
		public boolean canGrade() {
			return grader;
		}

		@Override
		public boolean canGrade(GradingAssignment assignment) {
			if(!grader || assignment == null || assignment.getGrader() == null) return false;
			
			return (assignment.getAssignmentStatus() == GradingAssignmentStatus.assigned || assignment.getAssignmentStatus() == GradingAssignmentStatus.inProcess)
					&& (assignment.getGrader().getIdentity() != null && identity.equals(assignment.getGrader().getIdentity()));
		}

		@Override
		public boolean canViewRecordedRealMinutes() {
			List<String> permissions = gradingModule.getCorrectionRealMinutesVisibility();
			return (roles.isAdministrator() && permissions.contains(OrganisationRoles.administrator.name()))
					|| (roles.isLearnResourceManager() && permissions.contains(OrganisationRoles.learnresourcemanager.name()))
					|| (roles.isAuthor() && permissions.contains(OrganisationRoles.author.name()))
					|| (grader && permissions.contains(GradingRoles.grader.name()));
		}	
	}
}
