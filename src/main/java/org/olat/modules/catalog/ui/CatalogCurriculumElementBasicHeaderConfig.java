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

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ParticipantsAvailability.ParticipantsAvailabilityNum;

/**
 * 
 * Initial date: Dec 2, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CatalogCurriculumElementBasicHeaderConfig extends BasicDetailsHeaderConfig {

	protected final CurriculumElement curriculumElement;
	protected final CurriculumService curriculumService;
	protected final ACService acService;

	public CatalogCurriculumElementBasicHeaderConfig(CurriculumService curriculumService, ACService acService,
			CurriculumElement curriculumElement, Identity identity) {
		super(identity);
		this.curriculumService = curriculumService;
		this.acService = acService;
		this.curriculumElement = curriculumElement;
	}

	protected ParticipantsAvailabilityNum loadParticipantsAvailabilityNum() {
		Long numParticipants = curriculumService.getCurriculumElementKeyToNumParticipants(List.of(curriculumElement), true).get(curriculumElement.getKey());
		return acService.getParticipantsAvailability(curriculumElement.getMaxParticipants(), numParticipants, false);
	}

}