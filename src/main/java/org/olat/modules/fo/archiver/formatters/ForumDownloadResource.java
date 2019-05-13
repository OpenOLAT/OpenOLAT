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
package org.olat.modules.fo.archiver.formatters;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.openxml.DocReference;
import org.olat.core.util.openxml.OpenXMLDocumentWriter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumCallback;
import org.olat.modules.fo.archiver.ForumArchiveManager;

/**
 * 
 * Initial date: 13.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumDownloadResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(ForumDownloadResource.class);
	
	private final Forum forum;
	private final ForumCallback foCallback;
	private Long topMessageId;
	
	private String label;
	private VFSContainer mediaContainer;
	private Locale locale;
	
	public ForumDownloadResource(String label, Forum forum, ForumCallback foCallback, Long topMessageId, VFSContainer mediaContainer, Locale locale) {
		this.locale = locale;
		this.forum = forum;
		this.label = label;
		this.foCallback = foCallback;
		this.topMessageId = topMessageId;
		this.mediaContainer = mediaContainer;
	}
	
	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_NO_CACHE;
	}
	
	@Override
	public boolean acceptRanges() {
		return false;
	}
	
	@Override
	public String getContentType() {
		return "application/zip";
	}

	@Override
	public Long getSize() {
		return null;
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
	public void release() {
		//
	}
	

	@Override
	public void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding("UTF-8");
		} catch (Exception e) {
			log.error("", e);
		}

		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			String secureLabel = StringHelper.transformDisplayNameToFileSystemName(label);

			String file = secureLabel + ".zip";
			hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(file));			
			hres.setHeader("Content-Description", StringHelper.urlEncodeUTF8(label));

			ZipEntry test = new ZipEntry(secureLabel + ".docx");
			zout.putNextEntry(test);
			Map<File,DocReference> attachments = exportForum(zout);
			zout.closeEntry();
			
			if(attachments != null && attachments.size() > 0) {
				for(Map.Entry<File,DocReference> attachmentEntry : attachments.entrySet()) {
					File attachment = attachmentEntry.getKey();
					DocReference ref = attachmentEntry.getValue();
					zout.putNextEntry(new ZipEntry("attachments/" + ref.getFilename()));
					copyShielded(attachment, zout);
					zout.closeEntry();
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void copyShielded(File attachment, ZipOutputStream zout) {
		try(OutputStream out = new ShieldOutputStream(zout)) {
			Files.copy(attachment.toPath(), out);
		} catch(Exception e) {
			log.error("", e);
		}
	}
	
	private Map<File,DocReference> exportForum(OutputStream out) {
		try(ZipOutputStream zout = new ZipOutputStream(out)) {
			zout.setLevel(9);
			
			ForumOpenXMLFormatter openXmlFormatter = new ForumOpenXMLFormatter(mediaContainer, locale);
			if(topMessageId != null) {
				CoreSpringFactory.getImpl(ForumArchiveManager.class)
					.applyFormatterForOneThread(openXmlFormatter, forum.getKey(), topMessageId);
			} else {
				CoreSpringFactory.getImpl(ForumArchiveManager.class)
					.applyFormatter(openXmlFormatter, forum.getKey(), foCallback);
			}
			
			OpenXMLDocumentWriter writer = new OpenXMLDocumentWriter();
			writer.createDocument(zout, openXmlFormatter.getOpenXMLDocument());
			return openXmlFormatter.getAttachments();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
