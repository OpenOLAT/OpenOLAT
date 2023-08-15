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
package org.olat.core.commons.services.pdf.manager;

import java.io.OutputStream;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.pdf.PdfOutputOptions;
import org.olat.core.commons.services.pdf.ui.AthenaPdfSettingsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 6 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AthenaPdfSPI extends AbstractPdfSPI {
	
	private static final Logger log = Tracing.createLoggerFor(AthenaPdfSPI.class);
	
	private static final String ATHENAPDF_URL = "athena.pdf.url";
	private static final String ATHENAPDF_KEY = "athena.pdf.key";

	@Value("${athena.pdf.url:@null}")
	private String serviceUrl;
	@Value("${athena.pdf.key:arachnys-weaver}")
	private String serviceKey;

	@Autowired
	public AthenaPdfSPI(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	public String getId() {
		return "AthenaPDF";
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
		serviceUrl = getStringPropertyValue(ATHENAPDF_URL, serviceUrl);
		serviceKey = getStringPropertyValue(ATHENAPDF_KEY, serviceKey);
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
		setStringProperty(ATHENAPDF_URL, serviceUrl, true);
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
		setStringProperty(ATHENAPDF_KEY, serviceKey, true);
	}

	@Override
	public Controller createAdminController(UserRequest ureq, WindowControl wControl) {
		return new AthenaPdfSettingsController(ureq, wControl);
	}
	
	@Override
	protected void render(String key, String rootFilename, PdfOutputOptions options, OutputStream out) {
		try(CloseableHttpClient httpclient = httpClientService.createHttpClient()) {
			
			StringBuilder sb = new StringBuilder(128);
			sb.append(serviceUrl);
			if(!serviceUrl.endsWith("/")) {
				sb.append("/");
			}
			sb.append("convert?auth=")
			  .append(serviceKey)
			  .append("&url=")
			  .append(Settings.getServerContextPathURI())
			  .append("/pdfd/")
			  .append(key)
			  .append("/")
			  .append(rootFilename);

			String uri = sb.toString();
			HttpGet get = new HttpGet(uri);
	
			RequestConfig config = RequestConfig.copy(RequestConfig.DEFAULT)
				.setCookieSpec(CookieSpecs.DEFAULT)
				.build();
			get.setConfig(config);
			get.addHeader("Accept", "application/pdf");
			get.addHeader("Accept-Language", "en");
			
			executeRequest(httpclient, get, out);
		} catch(Exception e) {
			log.error("", e);
		}
	}
}
