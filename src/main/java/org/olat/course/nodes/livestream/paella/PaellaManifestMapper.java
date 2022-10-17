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

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * 
 * Initial date: 6 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PaellaManifestMapper implements Mapper {

	private static final Logger log = Tracing.createLoggerFor(PaellaManifestMapper.class);
	
	private final ObjectMapper mapper;
	
	private final Stream[] streams;
	private final String title;

	public PaellaManifestMapper(Stream[] streams, String title) {
		this.streams = streams;
		this.title = StringHelper.containsNonWhitespace(title)? title: "OpenOlat live stream";
		 mapper = new ObjectMapper();
		 mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		StringMediaResource resource = new StringMediaResource();
		resource.setContentType("application/json; charset=UTF-8");
		
		Manifest manifest = new Manifest();
		Metadata metadata = new Metadata();
		metadata.setDuration(Double.valueOf(13));
		metadata.setTitle(title);
		String perview = StaticMediaDispatcher.getStaticURI("images/transparent.gif");
		metadata.setPreview(perview);
		manifest.setMetadata(metadata);
		manifest.setStreams(streams);
		
		resource.setData(objectToJson(manifest));
		return resource;
	}
	
	private String objectToJson(Object o)  {
		String json = null;
		try {
			json = mapper.writeValueAsString(o);
		} catch (Exception e) {
			json = "{}";
		}
		log.debug(json);
		return json;
	}

}
