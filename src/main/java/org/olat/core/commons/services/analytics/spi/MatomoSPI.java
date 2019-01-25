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
import org.olat.core.commons.services.analytics.ui.MatomoConfigFormController;
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
 * 
 * Initial date: 25 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MatomoSPI extends AbstractSpringModule implements AnalyticsSPI {
	
	private static final String TRACKER_CODE = "matomoTrackerCode";
	private static final String SITE_ID = "matomoSiteId";
	private static final String TRACKER_URL = "matomoTrackerurl";

	private String siteId;
	private String trackerUrl;
	private String trackerJsCode;
	
	@Autowired
	public MatomoSPI(CoordinatorManager coordinatorManager) {
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
	
	private void updateProperties() {
		String trackerObj = getStringPropertyValue(TRACKER_CODE, true);
		if (StringHelper.containsNonWhitespace(trackerObj)) {
			trackerJsCode = trackerObj;
		}
		
		String siteIdObj = getStringPropertyValue(SITE_ID, true);
		if (StringHelper.containsNonWhitespace(siteIdObj)) {
			siteId = siteIdObj;
		}
		
		String trackerUrlObj = getStringPropertyValue(TRACKER_URL, true);
		if (StringHelper.containsNonWhitespace(trackerUrlObj)) {
			trackerUrl = trackerUrlObj;
		}
	}

	@Override
	public String getId() {
		return "matomo";
	}

	@Override
	public String getName() {
		return "Matomo (Piwik)";
	}

	public String getTrackerJsCode() {
		return trackerJsCode;
	}

	public void setTrackerJsCode(String trackerJsCode) {
		this.trackerJsCode = trackerJsCode;
		setStringProperty(TRACKER_CODE, trackerJsCode, true);
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
		setStringProperty(SITE_ID, siteId, true);
	}

	public String getTrackerUrl() {
		return trackerUrl;
	}

	public void setTrackerUrl(String trackerUrl) {
		this.trackerUrl = trackerUrl;
		setStringProperty(TRACKER_URL, trackerUrl, true);
	}

	@Override
	public Controller createAdminController(UserRequest ureq, WindowControl wControl) {
		return new MatomoConfigFormController(ureq, wControl);
	}

	@Override
	public boolean isValid() {
		return StringHelper.isLong(siteId) && StringHelper.containsNonWhitespace(trackerUrl);
	}

	@Override
	public String analyticsInitPageJavaScript() {
		StringBuilder sb = new StringBuilder();
		if(isValid()) {
			sb.append("var _paq = window._paq || [];\n")
			  .append("_paq.push(['trackPageView']);\n")
			  .append("_paq.push(['enableLinkTracking']);\n")
			  .append("  (function() {\n")
			  .append("    var u='").append(trackerUrl).append("';\n")
			  .append("   _paq.push(['setTrackerUrl', u+'matomo.php']);\n")
	          .append("   _paq.push(['setSiteId', '").append(siteId).append("']);\n")
	          .append("    var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];\n")
	          .append("    g.type='text/javascript'; g.async=true; g.defer=true; g.src=u+'matomo.js'; s.parentNode.insertBefore(g,s);\n")
	          .append("  })();\n");
		}
		return sb.toString();
	}
	
	@Override
	public void analyticsCountOnclickJavaScript(StringOutput sb) {
		if(!isValid()) return;

		sb.append("try{")
		  .append("_paq.push(['setDocumentTitle', jQuery(this).attr('download')]);")
		  .append("_paq.push(['setCustomUrl', o_info.businessPath]);")
		  .append("_paq.push(['trackPageView']);")
		  .append("} catch(e) { if(window.console) console.log(e) }");
	}

	@Override
	public void analyticsCountPageJavaScript(StringBuilder sb, String title, String url) {
		if(!isValid()) return;

		sb.append("try{\n")
		  .append("_paq.push([\"setDocumentTitle\", \"").append(Formatter.escapeDoubleQuotes(title)).append("\"]);\n")
		  .append("_paq.push([\"setCustomUrl\", \"").append(url).append("\"]);\n")
		  .append("_paq.push([\"trackPageView\"]);\n")
		  .append("} catch(e) { if(window.console) console.log(e) }");
	}
}
