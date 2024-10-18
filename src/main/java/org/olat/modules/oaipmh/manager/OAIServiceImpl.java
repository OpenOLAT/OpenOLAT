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


import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.olat.core.commons.services.robots.RobotsProvider;
import org.olat.core.commons.services.robots.SitemapProvider;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.modules.oaipmh.DataProvider;
import org.olat.modules.oaipmh.OAIPmhMetadataProvider;
import org.olat.modules.oaipmh.OAIPmhModule;
import org.olat.modules.oaipmh.OAIService;
import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.model.Granularity;
import org.olat.modules.oaipmh.common.services.impl.SimpleResumptionTokenFormat;
import org.olat.modules.oaipmh.common.services.impl.UTCDateProvider;
import org.olat.modules.oaipmh.common.util.URLEncoder;
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.common.xml.XmlWriter;
import org.olat.modules.oaipmh.dataprovider.builder.OAIRequestParametersBuilder;
import org.olat.modules.oaipmh.dataprovider.exceptions.OAIException;
import org.olat.modules.oaipmh.dataprovider.model.Context;
import org.olat.modules.oaipmh.dataprovider.model.MetadataFormat;
import org.olat.modules.oaipmh.dataprovider.model.MetadataItems;
import org.olat.modules.oaipmh.dataprovider.repository.MetadataItemRepository;
import org.olat.modules.oaipmh.dataprovider.repository.MetadataSetRepository;
import org.olat.modules.oaipmh.dataprovider.repository.Repository;
import org.olat.modules.oaipmh.dataprovider.repository.RepositoryConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.ResourceInfoDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class OAIServiceImpl implements OAIService, RobotsProvider, SitemapProvider {
	
	private static final Logger log = Tracing.createLoggerFor(OAIServiceImpl.class);
	private static final String METADATA_DEFAULT_PREFIX = "oai_dc";

	@Autowired
	private List<OAIPmhMetadataProvider> metadataProviders;
	@Autowired
	private OAIPmhModule oaiPmhModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private HttpClientService httpClientService;

	@Override
	public MediaResource handleOAIRequest(
			String requestVerbParameter,
			String requestIdentifierParameter,
			String requestMetadataPrefixParameter,
			String requestResumptionTokenParameter,
			String requestFromParameter,
			String requestUntilParameter,
			String requestSetParameter) {

		MetadataSetRepository setRepository = new MetadataSetRepository();
		MetadataItemRepository itemRepository = new MetadataItemRepository();
		RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration().withDefaults();

		Context context = new Context();
		Repository repository = new Repository()
				.withSetRepository(setRepository)
				.withItemRepository(itemRepository)
				.withResumptionTokenFormatter(new SimpleResumptionTokenFormat())
				.withConfiguration(repositoryConfiguration);

		StringMediaResource mr = new StringMediaResource();
		String result = "";

		if (!StringHelper.containsNonWhitespace(requestMetadataPrefixParameter)
				&& requestResumptionTokenParameter == null
				&& !requestVerbParameter.equalsIgnoreCase("listmetadataformats")
				&& !requestVerbParameter.equalsIgnoreCase("identify")
				&& !requestVerbParameter.equalsIgnoreCase("listsets")) {
			requestMetadataPrefixParameter = METADATA_DEFAULT_PREFIX;
		}

		if (StringHelper.containsNonWhitespace(requestMetadataPrefixParameter)) {
			context.withMetadataFormat(requestMetadataPrefixParameter, MetadataFormat.identity());
		} else {
			context.withMetadataFormat(METADATA_DEFAULT_PREFIX, MetadataFormat.identity());
		}

		if (requestSetParameter != null) {
			String[] setSpec = requestSetParameter.split(":");
			setRepository.withSet(setSpec[1], requestSetParameter);
		}

		DataProvider dataProvider = new DataProvider(context, repository);

		try {
			Date fromParameter = null;
			Date untilParameter = null;
			if (requestFromParameter != null) {
				fromParameter = new UTCDateProvider().parse(requestFromParameter, Granularity.Day);
			}
			if (requestUntilParameter != null) {
				untilParameter = new UTCDateProvider().parse(requestUntilParameter, Granularity.Day);
			}

			OAIRequestParametersBuilder requestBuilder = new OAIRequestParametersBuilder();
			requestBuilder.withVerb(requestVerbParameter)
					.withFrom(fromParameter)
					.withUntil(untilParameter)
					.withIdentifier(requestIdentifierParameter)
					.withMetadataPrefix(requestMetadataPrefixParameter)
					.withResumptionToken(requestResumptionTokenParameter)
					.withSet(requestSetParameter);

			List<MetadataItems> metadataItems =
					repositoryItems(requestMetadataPrefixParameter, setRepository);
			itemRepository.withRepositoryItems(metadataItems);

			result = write(dataProvider.handle(requestBuilder));
		} catch (OAIException | XMLStreamException | XmlWriteException | ParseException e) {
			throw new RuntimeException(e);
		}

		mr.setContentType("application/xml");
		mr.setEncoding("UTF-8");
		mr.setData(result);

		return mr;
	}

	@Override
	public Map<String, Integer> propagateSearchEngines(List<String> searchEngineUrls) {
		List<RepositoryEntry> repositoryEntries = repositoryService.loadRepositoryForMetadata(RepositoryEntryStatusEnum.published);
		HttpResponse response = null;
		List<String> urlList = new ArrayList<>();
		JSONObject json = null;
		Map<String, Integer> searchEngineToResponseCode = new HashMap<>();

		for (RepositoryEntry entry : repositoryEntries) {
			urlList.add(ResourceInfoDispatcher.getUrl(entry.getKey().toString()));
		}
		if (urlList.size() == 0) {
			// nothing to publish
			return searchEngineToResponseCode;
		}

		if (oaiPmhModule.getUuid().equals("none")) {
			// init uuid needed for indexnow if not already done
			oaiPmhModule.setUuid(UUID.randomUUID().toString());
		}


		for (String url : searchEngineUrls) {
			try (CloseableHttpClient httpClient = httpClientService.createHttpClient()) {
				if (url.contains("sitemap")) {					
					// 1) Submit to sitemap capable search engines
					String sitemapUrlEncoded = URLEncoder.encode(ResourceInfoDispatcher.getUrl("sitemap.xml"));
					url = String.format(url, sitemapUrlEncoded);
					HttpGet getRequest = new HttpGet(url);
					response = httpClient.execute(getRequest);
				} else if (url.contains("indexnow")) {
					// 2) Submit list to indexnow capable search engines
					HttpPost postRequest = new HttpPost(url);
					if (json == null) {
						// build only once
						json = buildIndexNowPostJson(urlList);
					}
					StringEntity postParams = new StringEntity(json.toString());
					postParams.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
					postParams.setContentEncoding("UTF-8");
					postRequest.setEntity(postParams);
					response = httpClient.execute(postRequest);
				}
				if (response != null) {
					// Ensures that the entity content is fully consumed and the content stream, if exists, is closed.
					EntityUtils.consume(response.getEntity());
					searchEngineToResponseCode.put(new URI(url).getHost(), response.getStatusLine().getStatusCode());
				}
			} catch (Exception e) {
				searchEngineToResponseCode.clear();
				searchEngineToResponseCode.put("Error: " + url, HttpStatus.SC_INTERNAL_SERVER_ERROR);
				log.warn("Error when submitting URL::" + url + " to search engine", e);
			}
		}
		return searchEngineToResponseCode;
	}

	/**
	 * Helper to build the json object with the given URL list to submit to indexnow search engines
	 * @param urlList
	 * @return json
	 */
	private JSONObject buildIndexNowPostJson(List<String> urlList) {
		JSONObject json = new JSONObject();
		json.put("host", Settings.createServerURI());
		json.put("key", oaiPmhModule.getUuid());
		// set keyLocation behind resourceinfo/ so only urls like .../resourceinfo/1234 can be indexed
		// https://www.indexnow.org/documentation
		json.put("keyLocation", ResourceInfoDispatcher.getUrl(oaiPmhModule.getUuid() + ".txt"));
		json.put("urlList", urlList);
		return json;
	}

	private List<MetadataItems> repositoryItems(String metadataprefix, MetadataSetRepository setRepository) {
		OAIPmhMetadataProvider provider =
				getMetadataProvider(metadataprefix).orElse(getMetadataProvider(METADATA_DEFAULT_PREFIX).get());
		return provider.getMetadata(setRepository);
	}

	private Optional<OAIPmhMetadataProvider> getMetadataProvider(String metadataprefix) {
		return metadataProviders.stream().filter(m -> m.getMetadataPrefix().equals(metadataprefix)).findFirst();
	}


	private String write(XmlWritable handle) throws XMLStreamException, XmlWriteException {
		return XmlWriter.toString(writer -> writer.write(handle));
	}

	@Override
	public List<String> getRobotAllows() {
		if (oaiPmhModule.isEnabled() && oaiPmhModule.isSearchEngineEnabled()) {
			return List.of(Settings.getServerContextPath() + "/" + ResourceInfoDispatcher.RESOURCEINFO_PATH);
		}
		return null;
	}

	@Override
	public List<String> getSitemapUrls() {
		if (oaiPmhModule.isEnabled() && oaiPmhModule.isSearchEngineEnabled()) {
			return List.of(ResourceInfoDispatcher.getUrl("sitemap.xml"));
		}
		return null;
	}
	
}
