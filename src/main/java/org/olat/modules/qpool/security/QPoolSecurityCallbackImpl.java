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
package org.olat.modules.qpool.security;

import org.olat.core.id.Roles;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QuestionPoolModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 05.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
@Scope("prototype")
public class QPoolSecurityCallbackImpl implements QPoolSecurityCallback {

	private boolean admin = false;
	private boolean poolAdmin = false;
	private boolean olatAuthor = false;
	
	@Autowired
	private QuestionPoolModule qpoolModule;

	@Override
	public void setRoles(Roles roles) {
		admin = roles.isAdministrator();
		poolAdmin = roles.isPoolManager();
		olatAuthor = roles.isAuthor();
	}

	@Override
	public boolean canUseCollections() {
		return qpoolModule.isCollectionsEnabled();
	}

	@Override
	public boolean canUsePools() {
		return qpoolModule.isPoolsEnabled();
	}

	@Override
	public boolean canUseGroups() {
		return qpoolModule.isSharesEnabled();
	}

	@Override
	public boolean canUseReviewProcess() {
		return qpoolModule.isReviewProcessEnabled();
	}

	@Override
	public boolean canUseTaxonomy() {
		return qpoolModule.isTaxonomyEnabled();
	}

	@Override
	public boolean canUseEducationalContext() {
		return qpoolModule.isEducationalContextEnabled();
	}

	@Override
	public boolean canNewQuestions() {
		return admin || olatAuthor || poolAdmin;
	}

	@Override
	public boolean canEditQuestions() {
		return admin || olatAuthor || poolAdmin;
	}

	@Override
	public boolean canShareQuestions() {
		return admin || olatAuthor || poolAdmin;
	}

	@Override
	public boolean canCreateTest() {
		return admin || olatAuthor;
	}


	@Override
	public boolean canEditAllQuestions() {
		return admin || (poolAdmin && qpoolModule.isPoolAdminAllowedToEditMetadata());
	}

	@Override
	public boolean canConfigReviewProcess() {
		return admin || (poolAdmin && qpoolModule.isPoolAdminAllowedToConfigReviewProcess());
	}

	@Override
	public boolean canConfigTaxonomies() {
		return admin || (poolAdmin && qpoolModule.isPoolAdminAllowedToConfigTaxonomy());
	}

	@Override
	public boolean canConfigPools() {
		return admin || (poolAdmin && qpoolModule.isPoolAdminAllowedToConfigPools());
	}

	@Override
	public boolean canConfigItemTypes() {
		return admin || (poolAdmin && qpoolModule.isPoolAdminAllowedToConfigItemTypes());
	}

	@Override
	public boolean canConfigEducationalContext() {
		return admin || (poolAdmin && qpoolModule.isPoolAdminAllowedToConfigEducationalContext());
	}

}
