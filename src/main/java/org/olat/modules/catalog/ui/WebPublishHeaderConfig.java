/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.catalog.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.list.BasicDetailsHeaderConfig;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;

/**
 *
 * Initial date: Mar 3, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class WebPublishHeaderConfig extends BasicDetailsHeaderConfig {

	public WebPublishHeaderConfig(RepositoryEntry entry) {
		super(null);

		ACService acService = CoreSpringFactory.getImpl(ACService.class);

		if (acService.isGuestAccessible(entry, true)) {
			String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
			guestStartUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath) + "?guest=true";
		} else if (entry.isPublicVisible()) {
			AccessResult acResult = acService.isAccessible(entry, null, Boolean.FALSE, false, Boolean.TRUE, false);
			if (acResult.isAccessible()) {
				openEnabled();
			} else {
				availableMethods = acResult.getAvailableMethods();
			}
		}
	}
	
	public WebPublishHeaderConfig(CurriculumElement element) {
		super(null);

		ACService acService = CoreSpringFactory.getImpl(ACService.class);

		AccessResult acResult = acService.isAccessible(element, null, Boolean.FALSE, false, Boolean.TRUE, false);
		if (acResult.isAccessible()) {
			openEnabled();
		} else {
			availableMethods = acResult.getAvailableMethods();
		}
	}


	@Override
	public boolean isOffersWebPublish() {
		return true;
	}

}
