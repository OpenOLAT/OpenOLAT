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

package org.olat.home;

import org.olat.NewControllerFactory;
import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.id.context.SiteContextEntryControllerCreator;
import org.olat.home.controllerCreators.GuestHomeCEControllerCreator;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  18 janv. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class HomeModule extends AbstractOLATModule {

	@Override
	public void init() {
		NewControllerFactory.getInstance().addContextEntryControllerCreator(HomeSite.class.getSimpleName(),
				new SiteContextEntryControllerCreator(HomeSite.class));
		
		NewControllerFactory.getInstance().addContextEntryControllerCreator("Guest",
				new GuestHomeCEControllerCreator());
		
	}

	@Override
	protected void initDefaultProperties() {
		//
	}

	@Override
	protected void initFromChangedProperties() {
		//
	}
	
	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

}
