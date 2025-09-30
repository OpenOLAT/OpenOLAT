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
package org.olat.user.ui.organisation;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationChangeEvent;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Organisation;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.user.ui.organisation.OrganisationSelectionSource.OrganisationOption;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: Sep 11, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.comm
 *
 */
@Service
public class OrganisationOptionCache implements GenericEventListener, InitializingBean, DisposableBean {
	
	@Autowired
	private OrganisationService organisationService;
	
	private CoordinatorManager coordinatorManager;
	private List<OrganisationOption> options;
	
	@Autowired
	public OrganisationOptionCache(CoordinatorManager coordinatorManager) {
		this.coordinatorManager = coordinatorManager;
	}
	
	@Override
	public void event(Event event) {
		if(event instanceof OrganisationChangeEvent) {
			options = null;
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		coordinatorManager.getCoordinator().getEventBus()
			.registerFor(this, null, OrganisationService.ORGANISATIONS_CHANGED_EVENT_CHANNEL);
		coordinatorManager.getCoordinator().getEventBus()
			.registerFor(this, null, OresHelper.createOLATResourceableInstance(Organisation.class, Long.valueOf(0l)));
	}

	@Override
	public void destroy() throws Exception {
		coordinatorManager.getCoordinator().getEventBus()
			.deregisterFor(this, OrganisationService.ORGANISATIONS_CHANGED_EVENT_CHANNEL);
	}
	
	public List<OrganisationOption> getOptions(Collection<String> organisationKeys) {
		if (organisationKeys == null || organisationKeys.isEmpty()) {
			return List.of();
		}
		
		if (options == null) {
			options = loadOptions();
		}
		return options.stream()
				.filter(option -> organisationKeys.contains(option.getKey()))
				.collect(Collectors.toList());
	}
	
	private List<OrganisationOption> loadOptions() {
		List<Organisation> organisations = organisationService.getOrganisations();
		return OrganisationSelectionSource.toOptions(organisations);
	}
	
}
