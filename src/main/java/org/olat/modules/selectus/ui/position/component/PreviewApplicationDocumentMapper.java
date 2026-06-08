/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.position.component;

import java.io.InputStream;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.DefaultMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;

/**
 * 
 * Initial date: 19 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PreviewApplicationDocumentMapper implements Mapper {

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		return new PreviewResource();
	}
	
	private static class PreviewResource extends DefaultMediaResource {

		@Override
		public long getCacheControlDuration() {
			return ServletUtil.CACHE_ONE_DAY;
		}

		@Override
		public String getContentType() {
			return "application/pdf";
		}

		@Override
		public InputStream getInputStream() {
			return PreviewApplicationDocumentMapper.class.getResourceAsStream("doc_preview.pdf");
		}
		
		@Override
		public void prepare(HttpServletResponse hres) {
			hres.setHeader("Content-Disposition", "filename*=UTF-8''DocumentPreview.pdf");
		}
	}
}
