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
package org.olat.core.util.openxml;

import java.io.IOException;
import java.io.OutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.DefaultMediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 22.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class OpenXMLWorkbookResource extends DefaultMediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(OpenXMLWorkbookResource.class);
	
	private final String label;
	
	/**
	 * 
	 * @param filename The label.
	 */
	public OpenXMLWorkbookResource(String label) {
		CoreSpringFactory.autowireObject(this);
		this.label = label;
	}
	
	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_NO_CACHE;
	}

	@Override
	public String getContentType() {
		return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	}
	
	@Override
	public final void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding("UTF-8");
		} catch (Exception e) {
			log.error("", e);
		}

		String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);			
		hres.setHeader("Content-Description", urlEncodedLabel);
		
		try {
			generate(hres.getOutputStream());
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	protected abstract void generate(OutputStream out);
}
