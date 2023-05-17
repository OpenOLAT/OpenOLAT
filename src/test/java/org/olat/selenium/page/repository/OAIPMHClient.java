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
package org.olat.selenium.page.repository;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XMLFactories;
import org.olat.restapi.RestConnection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriBuilderException;

/**
 * 
 * Initial date: 3 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OAIPMHClient {

	private static final Logger log = Tracing.createLoggerFor(OAIPMHClient.class);
	
	private final URL deploymentUrl;
	
	public OAIPMHClient(URL deploymentUrl) {
		this.deploymentUrl = deploymentUrl;
	}
	
	public String getOAIPMHIndex() {
		try {
			URI url = getOaiPmhURIBuilder()
					.queryParam("verb", "listRecords")
					.queryParam("metadataprefix", "oai_dc")
					.build();

			RestConnection restConnection = new RestConnection(deploymentUrl);
			HttpGet method = restConnection.createGet(url, "application/xml", false);
			HttpResponse response = restConnection.execute(method);
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
		} catch (IllegalArgumentException | UriBuilderException | URISyntaxException | IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	public String getSitemap() {
		try {
			URI url = getResourceInfoURIBuilder()
					.path("sitemap.xml").build();
			RestConnection restConnection = new RestConnection(deploymentUrl);
			HttpGet method = restConnection.createGet(url, "application/xml", false);
			HttpResponse response = restConnection.execute(method);
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
		} catch (IllegalArgumentException | UriBuilderException | URISyntaxException | IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	public OAIPMHClient assertOnOAIPMH(String xml) {
        try(StringReader source = new StringReader(xml)) {
        	DocumentBuilderFactory documentBuilderFactory = XMLFactories.newDocumentBuilderFactory();
        	documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setExpandEntityReferences(false);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(source));
            Element rootElement = document.getDocumentElement();
			Assert.assertEquals("OAI-PMH", rootElement.getTagName());
        } catch(ParserConfigurationException | SAXException | IOException e) {
			log.error("", e);
			Assert.fail();
		}
        return this;
	}
	
	public OAIPMHClient assertOnTitle(String xml, String title) {
		Assert.assertTrue(xml.contains("<dc:title>" + title + "</dc:title>"));
		return this;
	}
	
	public OAIPMHClient assertOnContributer(String xml, String contributer) {
		Assert.assertTrue(xml.contains("<dc:contributer>" + contributer + "</dc:contributer>"));
		return this;
	}
	
	public OAIPMHClient assertOnSitemap(String xml) {
        try(StringReader source = new StringReader(xml)) {
        	DocumentBuilderFactory documentBuilderFactory = XMLFactories.newDocumentBuilderFactory();
        	documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setExpandEntityReferences(false);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(source));
            Element rootElement = document.getDocumentElement();
			Assert.assertEquals("urlset", rootElement.getTagName());
        } catch(ParserConfigurationException | SAXException | IOException e) {
			log.error("", e);
			Assert.fail();
		}
        return this;
	}
	
	public OAIPMHClient assertOnUrlLoc(String xml, String browserUrl) {
		try {
			int index = browserUrl.indexOf("RepositoryEntry");
			int startId = browserUrl.indexOf("/", index) + 1;
			int endId = browserUrl.indexOf("/", startId);
			String id = browserUrl.substring(startId, endId);
			String url = getResourceInfoURIBuilder().path(id).build().toString();
			Assert.assertTrue(xml.contains("<loc>" + url + "</loc>"));
		} catch (IllegalArgumentException | UriBuilderException | MalformedURLException | URISyntaxException e) {
			log.error("", e);
			Assert.fail();
		}
		return this;
	}
	
	public UriBuilder getOaiPmhURIBuilder()
	throws URISyntaxException, MalformedURLException {
		return UriBuilder.fromUri(deploymentUrl.toURI()).path("oaipmh");
	}
	
	public UriBuilder getResourceInfoURIBuilder()
	throws URISyntaxException, MalformedURLException {
		return UriBuilder.fromUri(deploymentUrl.toURI()).path("resourceinfo");
	}
}
