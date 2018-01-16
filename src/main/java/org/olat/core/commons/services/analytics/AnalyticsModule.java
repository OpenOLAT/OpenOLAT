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
package org.olat.core.commons.services.analytics;

import java.util.List;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The analytics module provides a service to log user actions using an external
 * analytics server such as google analytics. This is not the same as the user
 * activity logger which logs mostly course events internally in a very fine
 * granular way. The analytics server just shows how users navigate with in the
 * system and is normally used anonymously.
 * 
 * Initial date: 15 feb. 2018<br>
 * 
 * @author Florian Gnägi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AnalyticsModule extends AbstractSpringModule {
	// module config key to remember the active SPI
	private static final String ANALYTICS_PROVIDER = "analytics.provider";
	// list of all availableSPI
	private List<AnalyticsSPI> analyticsProviders;
	// the currently configured SPI or NULL if disabled
	private AnalyticsSPI analyticsProvider = null;
	
	/**
	 * Spring constructor
	 * 
	 * @param coordinatorManager
	 */
	public AnalyticsModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	/**
	 * Internal helper to read the config from the module and init the module settings
	 */
	private void updateProperties() {
		String enabledSPIName = getStringPropertyValue(ANALYTICS_PROVIDER, true);
		if (StringHelper.containsNonWhitespace(enabledSPIName)) {
			for (AnalyticsSPI analyticsSPI : analyticsProviders) {
				if (enabledSPIName.equals(analyticsSPI.getId())) {
					analyticsProvider = analyticsSPI;
					return;
				}
			}
		}
		analyticsProvider = null;
	}

	/**
	 * @return true: analytics service is enabled; false: analytics is disabled
	 */
	public boolean isAnalyticsEnabled() {
		return (analyticsProvider != null);
	}

	/**
	 * @return the configured analytics service or NULL if not configured
	 */
	public AnalyticsSPI getAnalyticsProvider() {
		return analyticsProvider;
	}

	/**
	 * Set a new analytics service or NULL to disable analytics
	 * @param analyticsProvider
	 */
	public void setAnalyticsProvider(AnalyticsSPI analyticsProvider) {
		this.analyticsProvider = analyticsProvider;
		if (analyticsProvider == null) {
			removeProperty(ANALYTICS_PROVIDER, true);
		} else {
			setStringProperty(ANALYTICS_PROVIDER, analyticsProvider.getId(), true);			
		}
	}
	
	/**
	 * Set the list of available analytics service implementations
	 * @param analyticsSPIs
	 */
	@Autowired
	public void setAnalyticsProviders(List<AnalyticsSPI> analyticsSPIs){
		this.analyticsProviders = analyticsSPIs;
	}

	/**
	 * Get all available analytics service implementations
	 * @return
	 */
	public List<AnalyticsSPI> getAnalyticsProviders() {
		return analyticsProviders;
	}
}
