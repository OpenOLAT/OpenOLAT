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
package org.olat.course.nodes.livestream.paella;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 13 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PaellaFactory {
	
	public static Stream[] createStreams(String[] urls, PlayerProfile playerProfile) {
		if (urls.length > 0) {
			String[] filteredUrls = playerProfile.filterUrls(urls);
			return createStreams(filteredUrls);
		}
		return null;
	}
	
	private static Stream[] createStreams(String[] urls) {
		List<Stream> streamList = new ArrayList<>(2);
		if (urls.length > 0) {
			Stream stream1 = createStream("stream1", urls[0], true);
			streamList.add(stream1);
		}
		if (urls.length > 1) {
			Stream stream1 = createStream("stream2", urls[1], false);
			streamList.add(stream1);
		}
		return streamList.toArray(new Stream[streamList.size()]);
	}

	private static Stream createStream(String content, String url, boolean mainAudio) {
		Stream stream = new Stream();
		stream.setContent(content);
		Sources sources = createSources(url);
		stream.setSources(sources);
		if (mainAudio) {
			stream.setRole("mainAudio");
		}
		return stream;
	}

	private static Sources createSources(String url) {
		Sources sources = new Sources();
		addSource(sources, url);
		return sources;
	}
	
	private static void addSource(Sources sources, String url) {
		String suffix = getSuffix(url);
		if (suffix == null) return;
	
		switch (suffix) {
		case "m3u8":
			addM3U8Source(sources, url);
			break;
		case "mp4":
			addMP4Source(sources, url);
			break;
		default:
			break;
		}
	}

	private static void addM3U8Source(Sources sources, String url) {
		Source source = createSource(url);
		sources.setHls(new Source[] {source});
	}
	
	private static void addMP4Source(Sources sources, String url) {
		Source source = createSource(url);
		sources.setMp4(new Source[] {source});
	}

	private static Source createSource(String url) {
		Source source = new Source();
		source.setSrc(url);
		source.setMimetype("video/mp4");
		return source;
	}

	private static String getSuffix(String url) {
		int i = url.lastIndexOf('.');
		return i > 0? url.substring(i+1): null;
	}

}
