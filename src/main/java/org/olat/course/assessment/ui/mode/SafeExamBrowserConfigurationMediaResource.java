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
package org.olat.course.assessment.ui.mode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 25 janv. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SafeExamBrowserConfigurationMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(SafeExamBrowserConfigurationMediaResource.class);
	
	private final String plist;
	
	public SafeExamBrowserConfigurationMediaResource(String plist) {
		this.plist = plist;
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		return "application/xml";
	}

	@Override
	public Long getSize() {
		return Long.valueOf(plist.getBytes(StandardCharsets.UTF_8).length);
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_ONE_HOUR;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		try(OutputStream out=hres.getOutputStream()) {
			hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''config.seb");			
			hres.setHeader("Content-Description", "config.seb");
			out.write(plist.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void release() {
		//
	}
}
