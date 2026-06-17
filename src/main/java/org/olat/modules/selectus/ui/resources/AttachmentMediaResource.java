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
package org.olat.modules.selectus.ui.resources;

import java.io.InputStream;
import java.io.OutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 12 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AttachmentMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(AttachmentMediaResource.class);
	
	private final Attachment attachment;
	private final String name;

	public AttachmentMediaResource(Attachment attachment) {
		this.attachment = attachment;
		name = attachment.getName();
	}
	
	public AttachmentMediaResource(String name, Attachment attachment) {
		this.attachment = attachment;
		this.name = name;
	}
	
	public AttachmentMediaResource(Reference reference, Attachment attachment) {
		this.attachment = attachment;
		String refName = reference.getKey() + "_" + reference.getFirstName() + "_" + reference.getLastName()
			+ "_" + attachment.getName();
		name = RecruitingHelper.normalizeFilename(refName) + ".pdf";
	}
	
	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_ONE_HOUR;
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		if(StringHelper.containsNonWhitespace(name)) {
			return WebappHelper.getMimeType(name);
		}
		if(StringHelper.containsNonWhitespace(attachment.getName())) {
			return WebappHelper.getMimeType(attachment.getName());
		}
		return "application/pdf";
	}

	@Override
	public Long getSize() {
		return Long.valueOf(attachment.getSize().longValue());
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
	public void prepare(HttpServletResponse hres) {
		String filename = StringHelper.urlEncodeUTF8(name);
		hres.setHeader("Content-Disposition", "filename*=UTF-8''" + filename);
		
		try (OutputStream out=hres.getOutputStream()) {
			byte[] datas = CoreSpringFactory.getImpl(RecruitingService.class).getAttachmentDatas(attachment);
			DBFactory.getInstance().commitAndCloseSession();
			out.write(datas);
		} catch(Exception e) {
			log.error("", e);
		}
	}

	@Override
	public void release() {
		//
	}
}
