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
package org.olat.repository.ui.list;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;

/**
 *
 * Initial date: Mar 3, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class GuestHeaderConfig extends BasicDetailsHeaderConfig {

	public GuestHeaderConfig(RepositoryEntry entry, Identity identity) {
		super(identity);
		
		if (entry.isPublicVisible()) {
			AccessResult acResult = CoreSpringFactory.getImpl(ACService.class)
					.isAccessible(entry, identity, Boolean.FALSE, true, null, false);
			if (acResult.isAccessible()) {
				openEnabled();
			}
		}
	}

}
