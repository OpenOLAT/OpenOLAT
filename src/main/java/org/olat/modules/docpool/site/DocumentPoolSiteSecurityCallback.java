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
package org.olat.modules.docpool.site;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.modules.docpool.DocumentPoolModule;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 Oct 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("taxonomySiteSecurityCallback")
public class DocumentPoolSiteSecurityCallback implements SiteSecurityCallback {

	@Autowired
	private DocumentPoolModule docPoolModule;
	@Autowired
	private TaxonomyService taxonomyService;

	@Override
	public boolean isAllowedToLaunchSite(UserRequest ureq) {
		UserSession usess = ureq == null ? null : ureq.getUserSession();
		if(usess == null) return false;
		
		Roles roles = usess.getRoles();
		if(roles == null || roles.isInvitee() || roles.isGuestOnly()) {
			return false;
		}
		if (roles.isAdministrator()) {
			return true;
		}
		
		String taxonomyKey = docPoolModule.getTaxonomyTreeKey();
		if(StringHelper.isLong(taxonomyKey)) {
			TaxonomyRef taxonomy = new TaxonomyRefImpl(Long.valueOf(taxonomyKey));
			return taxonomyService.hasTaxonomyCompetences(taxonomy, ureq.getIdentity(), ureq.getRequestTimestamp());
		}
		return false;
	}
}
