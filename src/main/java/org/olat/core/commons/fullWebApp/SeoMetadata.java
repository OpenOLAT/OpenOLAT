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
package org.olat.core.commons.fullWebApp;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.StringHelper;
import org.olat.modules.oaipmh.OAIPmhModule;

/**
 * Small value object holding all SEO-related metadata for a page.
 * By default the page is set to "nofollow" with no extra SEO data.
 * Only public dispatchers (ResourceInfoDispatcher, WebCatalogDispatcher)
 * populate the fields to enable indexing, canonical URLs, Open Graph
 * tags, and JSON-LD structured data.
 *
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class SeoMetadata {

	private String robotsMetaContent = "nofollow";
	private String metaDescription;
	private String metaKeywords;
	private String metaOrganisationName;
	private String canonicalUrl;
	private String ogImageUrl;
	private String structuredDataJson;

	public SeoMetadata() {
		resetMetadata();	
	}
	
	/**
	 * Reset everything except the robotsMetaContent
	 */
	public void resetMetadata() {
		metaDescription = null;
		metaKeywords = null;
		metaOrganisationName = null;
		ogImageUrl = null;
		structuredDataJson = null;		
		
		OAIPmhModule oaiPmhModule = CoreSpringFactory.getImpl(OAIPmhModule.class);
		if (oaiPmhModule != null && oaiPmhModule.isSearchEngineEnabled()) {
			if (StringHelper.containsNonWhitespace(oaiPmhModule.getSeoKeywords())) {
				metaKeywords = oaiPmhModule.getSeoKeywords();
			}
			if (StringHelper.containsNonWhitespace(oaiPmhModule.getSeoOrganisationName())) {
				metaOrganisationName = oaiPmhModule.getSeoOrganisationName();
			}
		}
	}

	public String getRobotsMetaContent() {
		return robotsMetaContent;
	}

	public void setRobotsMetaContent(String robotsMetaContent) {
		this.robotsMetaContent = robotsMetaContent;
	}

	public String getMetaDescription() {
		return metaDescription;
	}

	public void setMetaDescription(String metaDescription) {
		this.metaDescription = metaDescription;
	}

	public String getMetaKeywords() {
		return metaKeywords;
	}

	public void setMetaKeywords(String metaKeywords) {
		this.metaKeywords = metaKeywords;
	}

	public String getMetaOrganisationName() {
		return metaOrganisationName;
	}

	public void setMetaOrganisationName(String metaOrganisationName) {
		this.metaOrganisationName = metaOrganisationName;
	}

	public String getCanonicalUrl() {
		return canonicalUrl;
	}

	public void setCanonicalUrl(String canonicalUrl) {
		this.canonicalUrl = canonicalUrl;
	}

	public String getOgImageUrl() {
		return ogImageUrl;
	}

	public void setOgImageUrl(String ogImageUrl) {
		this.ogImageUrl = ogImageUrl;
	}

	public String getStructuredDataJson() {
		return structuredDataJson;
	}

	public void setStructuredDataJson(String structuredDataJson) {
		this.structuredDataJson = structuredDataJson;
	}

	/**
	 * @return true if the page is marked as indexable (robots contains "index")
	 */
	public boolean isIndexable() {
		return robotsMetaContent != null && robotsMetaContent.contains("index");
	}
}
