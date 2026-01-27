/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.certificationprogram.CertificationProgramService;

/**
 * 
 * Initial date: 3 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramSecurityCallbackFactory {
	
	public static CertificationProgramSecurityCallback getSecurityCallback(Identity identity, Roles roles) {
		boolean admin = roles.isCurriculumManager() || roles.isAdministrator();
		boolean principal = roles.isPrincipal();
		boolean owner = admin
				? false
				: CoreSpringFactory.getImpl(CertificationProgramService.class).isCertificationProgramOwner(identity);
		return new CertificationProgramSecurityCallbackImpl(admin, principal, owner);
	}

	private static class CertificationProgramSecurityCallbackImpl implements CertificationProgramSecurityCallback {
		
		private final boolean admin;
		private final boolean owner;
		private final boolean principal;
		
		public CertificationProgramSecurityCallbackImpl(boolean admin, boolean principal, boolean owner) {
			this.admin = admin;
			this.owner = owner;
			this.principal = principal;
		}

		@Override
		public boolean canViewCertificationPrograms() {
			return admin || principal || owner;
		}

		@Override
		public boolean canNewCertificationProgram() {
			return admin;
		}
		
		@Override
		public boolean canEditCertificationProgram() {
			return admin || owner;
		}

		@Override
		public boolean canAddMember() {
			return admin || owner;
		}

		@Override
		public boolean canChangeCertificateStatus() {
			return canAddMember();
		}
	}
}
