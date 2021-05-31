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
package org.olat.modules.video.spi.youtube;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.CoreSpringFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.modules.qpool.manager.MetadataConverterHelper;
import org.olat.modules.qpool.model.LOMDuration;
import org.olat.modules.video.spi.youtube.model.YoutubeMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The video id extraction is based on: https://gist.github.com/jvanderwee/b30fdb496acff43aef8e which
 * is the best I found.
 * 
 * Initial date: 1 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class YoutubeProvider extends AbstractSpringModule implements ConfigOnOff {
	
	private static final Logger log = Tracing.createLoggerFor(YoutubeProvider.class);
	
	private static final String youTubeUrlRegEx = "^(https?)?(://)?(www.)?(m.)?((youtube.com)|(youtu.be))/";
    private static final String[] videoIdRegex = { "\\?vi?=([^&]*)","watch\\?.*v=([^&]*)", "(?:embed|vi?)/([^/?]*)", "^([A-Za-z0-9\\-]*)"};
	
    private static final String YOUTUBE_API_KEY = "youtube.api.key";
	
	@Value("${youtube.api.key}")
	private String apiKey;
	
	@Autowired
	public YoutubeProvider(CoordinatorManager coordinatorManager) {
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
		apiKey = getStringPropertyValue(YOUTUBE_API_KEY, apiKey);
	}
	
	@Override
	public boolean isEnabled() {
		return StringHelper.containsNonWhitespace(apiKey);
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
		setStringProperty(YOUTUBE_API_KEY, apiKey, true);
	}
	
	public ResourceEvaluation evaluate(String url) {
		YoutubeMetadata data = getSnippet(url);
		return data == null ? null : data.toResourceEvaluation();
	}
	
	public YoutubeMetadata getSnippet(String url) {
		return getSnippet(url, apiKey);
	}
	
	public YoutubeMetadata getSnippet(String url, String key) {
		String videoId = getVideoId(url);
		String googleUrl = "https://www.googleapis.com/youtube/v3/videos/?key=" + key + "&part=snippet,contentDetails,status&id=" + videoId;
		HttpGet get = new HttpGet(googleUrl);
		
		YoutubeMetadata data = null;		
		HttpClientService httpClientService = CoreSpringFactory.getImpl(HttpClientService.class);
		try(CloseableHttpClient client = httpClientService.createThreadSafeHttpClient(true);
				CloseableHttpResponse response = client.execute(get)) {
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 200) {
				String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
				data = parseMetadata(content, videoId);
			} else {
				String msg = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
				log.error("Cannot retrieve Youtube Video Metadata: error: {} message: {}", Integer.valueOf(statusCode), msg);
			}
		} catch(Exception e) {
			log.error("", e);
		}
		return data;
	}

	protected static String getVideoId(String url) {
		return extractVideoIdFromUrl(url);
	}
	
	public static String extractVideoIdFromUrl(String url) {
        String youTubeLinkWithoutProtocolAndDomain = youTubeLinkWithoutProtocolAndDomain(url);
        for(String regex : videoIdRegex) {
            Pattern compiledPattern = Pattern.compile(regex);
            Matcher matcher = compiledPattern.matcher(youTubeLinkWithoutProtocolAndDomain);
            if(matcher.find()){
                return matcher.group(1);
            }
        }
        return null;
    }

    private static String youTubeLinkWithoutProtocolAndDomain(String url) {
        Pattern compiledPattern = Pattern.compile(youTubeUrlRegEx);
        Matcher matcher = compiledPattern.matcher(url);

        if(matcher.find()){
            return url.replace(matcher.group(), "");
        }
        return url;
    }
	
	protected static YoutubeMetadata parseMetadata(String content, String videoId) throws JSONException {
		YoutubeMetadata data = new YoutubeMetadata();

		JSONObject subjo = new JSONObject(content);
		JSONArray items = subjo.getJSONArray("items");
		for(int i=items.length(); i-->0; ) {
			JSONObject item = items.getJSONObject(i);
			String id = item.getString("id");
			if(id.equals(videoId)) {
				data = parseMetadataItem(item);
				break;
			}
		}
		return data;
	}
	
	private static final String[] thumbnailsResAttrs = new String[] { "maxres", "standard", "high", "medium", "default" };
	
	protected static YoutubeMetadata parseMetadataItem(JSONObject item) throws JSONException {
		YoutubeMetadata data = new YoutubeMetadata();
		JSONObject snippet = item.getJSONObject("snippet");
		data.setTitle(snippet.optString("title"));
		String description = snippet.optString("description");
		if(StringHelper.containsNonWhitespace(description)) {
			description = Formatter.escWithBR(description).toString();
			data.setDescription(description);
		}
		data.setAuthors(snippet.optString("channelTitle"));
		
		JSONObject thumbnails = snippet.optJSONObject("thumbnails");
		if(thumbnails != null) {
			for(String thumbnailsResAttr:thumbnailsResAttrs) {
				JSONObject thumbnail = thumbnails.optJSONObject(thumbnailsResAttr);
				if(thumbnail != null) {
					String thumbnailUrl = thumbnail.optString("url");
					if(StringHelper.containsNonWhitespace(thumbnailUrl)) {
						data.setThumbnailUrl(thumbnailUrl);
						break;
					}
				}
			}
		}
		
		JSONObject contentDetails = item.getJSONObject("contentDetails");
		String duration = contentDetails.optString("duration");
		if(StringHelper.containsNonWhitespace(duration)) {
			LOMDuration lomDuration = MetadataConverterHelper.convertDuration(duration);
			long durationInSeconds = MetadataConverterHelper.convertToSeconds(lomDuration);
			data.setDuration(durationInSeconds);
		}

		JSONObject status = item.getJSONObject("status");
		String license = status.optString("license");
		if(StringHelper.containsNonWhitespace(license)) {
			if(license.equalsIgnoreCase("creativeCommon")) {
				data.setLicense("CC BY");
			} else {
				data.setLicense(license);
			}
			data.setLicensor(snippet.optString("channelTitle"));
		}
		return data;
	}
}
