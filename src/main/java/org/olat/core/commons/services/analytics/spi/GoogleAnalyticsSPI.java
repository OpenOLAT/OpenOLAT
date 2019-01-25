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
package org.olat.core.commons.services.analytics.spi;

import org.olat.core.commons.services.analytics.AnalyticsSPI;
import org.olat.core.commons.services.analytics.ui.GoogleAnalyticsConfigFormController;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implements an analytics service based on Google Analytics. You need to have a
 * google analytics accoutn to use this. More info here.
 * https://analytics.google.com
 * 
 * Initial date: 15 feb. 2018<br>
 * 
 * @author Florian Gnägi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GoogleAnalyticsSPI extends AbstractSpringModule implements AnalyticsSPI {
	// config name for tracking id
	private static final String ANALYTICS_TRACKING_ID = "google.analytics.tracking.id";
	// the google analytics tracking id or NULL if not configured
	private String analyticsTrackingId;
	// the tracking code added to each page. kind of a cache so it does not have
	// to be put together every time, changes almost never.
	private String trackingInitCode;

	/**
	 * Constructor, used by spring. Implements the module interface to store configuration
	 * @param coordinatorManager
	 */
	@Autowired
	public GoogleAnalyticsSPI(CoordinatorManager coordinatorManager) {
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
	 * Read the config and init the settings
	 */
	private void updateProperties() {
		String trackingIdObj = getStringPropertyValue(ANALYTICS_TRACKING_ID, true);
		if (StringHelper.containsNonWhitespace(trackingIdObj)) {
			// loading of beans should be save, spi's are initialized before as
			// they are autowired in the providers list
			analyticsTrackingId = trackingIdObj;
		} else {
			analyticsTrackingId = null;
		}
		initTrackingInitCode();
	}

	@Override
	public Controller createAdminController(UserRequest ureq, WindowControl wControl) {
		return new GoogleAnalyticsConfigFormController(ureq, wControl);
	}

	/**
	 * @return the google analytics tracking ID or NULL if not set
	 */
	public String getAnalyticsTrackingId() {
		return analyticsTrackingId;
	}

	/**
	 * Set the google analytics tracking ID (the UA-123245-1 style ID) valid for
	 * this domain
	 * 
	 * @param analyticsTrackingId
	 */
	public void setAnalyticsTrackingId(String analyticsTrackingId) {
		if (StringHelper.containsNonWhitespace(analyticsTrackingId)) {
			this.analyticsTrackingId = analyticsTrackingId;
		} else {
			this.analyticsTrackingId = null;
		}
		setStringProperty(ANALYTICS_TRACKING_ID, this.analyticsTrackingId, true);
		initTrackingInitCode();
	}

	@Override
	public String getId() {
		return "googleAnalytics";
	}

	@Override
	public String getName() {
		return "Google Analytics";
	}

	@Override
	public boolean isValid() {
		return (this.analyticsTrackingId != null);
	}

	@Override
	public String analyticsInitPageJavaScript() {
		return this.trackingInitCode;
	}

	@Override
	public void analyticsCountPageJavaScript(StringBuilder sb, String title, String url) {
		if (isValid()) {
			// Currently only send page views with url and title. No support for tags so far
			sb.append("ga('send', 'pageview', { page: \"").append(url).append("\", title: \"")
					.append(Formatter.escapeDoubleQuotes(title)).append("\" });");
		}
	}

	@Override
	public void analyticsCountOnclickJavaScript(StringOutput sb) {
		if (isValid()) {
			// Currently only send page views with url and title. No support for tags so far
			sb.append("ga('send', 'pageview', { page: o_info.businessPath, title: jQuery(this).attr('download') });");
		}
	}

	/**
	 * Helper method to build the tracker page initialization code. Does not
	 * change often, thus store in variable for reuse
	 */
	private void initTrackingInitCode() {
		if (isValid()) {
			this.trackingInitCode = "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){ "
					+ "(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o), "
					+ "m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m) "
					+ "})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');"
					+ "ga('create', '" + analyticsTrackingId + "', 'auto'); "
					+ "ga('send', 'pageview');";
		} else {
			this.trackingInitCode = null;
		}
	}

}
