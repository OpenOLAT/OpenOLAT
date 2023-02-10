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
package org.olat.modules.oaipmh.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.oaipmh.OAIPmhModule;
import org.olat.modules.oaipmh.OAIService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Initial date: Feb 10, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OAISEOJob extends JobWithDB {

	private static final Logger log = Tracing.createLoggerFor(OAISEOJob.class);

	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		doExecute();
	}

	private boolean doExecute() {
		OAIService oaiService = CoreSpringFactory.getImpl(OAIService.class);
		OAIPmhModule oaiPmhModule = CoreSpringFactory.getImpl(OAIPmhModule.class);
		List<String> urlList = new ArrayList<>();

		if (oaiService == null || oaiPmhModule == null) {
			log.info("Skipping execution of SEO propagation job.");
			return false;
		}

		boolean allOk = false;

		if (isValidInputUrl(oaiPmhModule.getSearchEngineGoogleUrl())) {
			urlList.add(oaiPmhModule.getSearchEngineGoogleUrl());
		}
		if (isValidInputUrl(oaiPmhModule.getSearchEngineCustomSitemapUrl())) {
			urlList.add(oaiPmhModule.getSearchEngineCustomSitemapUrl());
		}
		if (isValidInputUrl(oaiPmhModule.getSearchEngineBingUrl())) {
			urlList.add(oaiPmhModule.getSearchEngineBingUrl());
		}
		if (isValidInputUrl(oaiPmhModule.getSearchEngineYandexUrl())) {
			urlList.add(oaiPmhModule.getSearchEngineYandexUrl());
		}
		if (isValidInputUrl(oaiPmhModule.getSearchEngineCustomIndexnowUrl())) {
			urlList.add(oaiPmhModule.getSearchEngineCustomIndexnowUrl());
		}

		if (!urlList.isEmpty()) {
			oaiService.propagateSearchEngines(urlList);
			allOk = true;
		}
		DBFactory.getInstance().commitAndCloseSession();

		return allOk;
	}

	private boolean isValidInputUrl(String url) {
		boolean allOk = false;
		UrlValidator urlValidator = new UrlValidator(new String[]{"https"});

		// check first on ServerDomainName, because we don't want test servers getting indexed
		if (!Settings.getServerDomainName().equals("testing.frentix.com")
				&& StringHelper.containsNonWhitespace(url)
				&& urlValidator.isValid(url)
				&& (url.contains("sitemap") || url.contains("indexnow"))) {
			allOk = true;
		}

		return allOk;
	}
}
