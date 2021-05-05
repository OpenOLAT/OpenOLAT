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
package org.olat.modules.scorm;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.DefaultMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.logging.Tracing;


/**
 * 
 * Initial date: 30 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ScormAgainMapper implements Mapper {
	
	private static final Logger log = Tracing.createLoggerFor(ScormAgainMapper.class);
	
	private final ScormSessionController sessionController;
	
	public ScormAgainMapper(ScormSessionController controller) {
		this.sessionController = controller;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		try {
			if(relPath.endsWith(".json")) {
				commitData(relPath, request);
				return new ResultMediaResource(true);
			}
			return new ResultMediaResource(false);
		} catch (IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	private void commitData(String relPath, HttpServletRequest request)
	throws IOException {
		String value = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
		JSONObject cmi = new JSONObject(value);
		Map<String,String> cmis = new HashMap<>();
		for(Iterator<String> keyIterator = cmi.keys(); keyIterator.hasNext(); ) {
			String key = keyIterator.next();
			if(key.startsWith("cmi.")) {
				Object val = cmi.get(key);
				cmis.put(key, val.toString());
			}

		}
		log.debug("Cmis send: {}", cmis.size());
		String scoId = getScoId(relPath);
		sessionController.lmsCommit(scoId, true, cmis);
	}
	
	private String getScoId(String relPath) {
		String scoId = null;
		
		int last = relPath.lastIndexOf('/');
		if(last >= 0) {
			scoId = relPath.substring(last + 1);
			if(scoId.endsWith(".json")) {
				scoId = scoId.substring(0, scoId.length() -5);
			}
		}
		
		return scoId;
	}
	
	private final class ResultMediaResource extends DefaultMediaResource {
		
		private final boolean result;
		
		public ResultMediaResource(boolean result) {
			this.result = result;
		}
		
		@Override
		public long getCacheControlDuration() {
			return ServletUtil.CACHE_NO_CACHE;
		}

		@Override
		public void prepare(HttpServletResponse hres) {
			hres.setStatus(HttpServletResponse.SC_OK);
			try(Writer w=hres.getWriter()) {
				w.append("{ \"result\": ").append(Boolean.toString(result)).append(", \"errorCode\": 0 }");
			} catch(IOException e) {
				log.error("", e);
			}
		}
	}
}
