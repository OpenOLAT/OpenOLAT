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

package org.olat.core.util.mail.ui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.ForbiddenMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.manager.MailManager;
import org.olat.core.util.mail.model.DBMail;
import org.olat.core.util.mail.model.DBMailAttachmentData;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for MailAttachmentMapper
 * 
 * <P>
 * Initial Date:  28 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailAttachmentMapper implements Mapper {
	
	public static final String ATTACHMENT_CONTEXT =  "/attachments/";
	
	private final DBMail mail;
	private final MailManager mailManager;
	
	public MailAttachmentMapper(DBMail mail, MailManager mailManager) {
		this.mail = mail;
		this.mailManager = mailManager;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if(relPath != null && relPath.indexOf(ATTACHMENT_CONTEXT) >= 0) {
			int startIndex = relPath.indexOf(ATTACHMENT_CONTEXT);
			int endIndex = relPath.indexOf("/", startIndex + ATTACHMENT_CONTEXT.length());
			if(startIndex >= 0 && endIndex > startIndex) {
				String attachmentKey = relPath.substring(startIndex + ATTACHMENT_CONTEXT.length(), endIndex);
				try {
					Long key = new Long(attachmentKey);
					
					boolean rightMail = true;
					/*for(DBMailAttachment attachment:mail.getAttachments()) {
						if(key.equals(attachment.getKey())) {
							rightMail = true;
							break;
						}
					}*/
					
					if(rightMail) {
						DBMailAttachmentData datas = mailManager.getAttachmentWithData(key);
						BytesMediaResource resource = new BytesMediaResource(datas);
						return resource;	
					} else {
						//only show the attachment of the selected e-mail
						return  new ForbiddenMediaResource(relPath);
					}
				} catch(NumberFormatException e) {
					return new NotFoundMediaResource(relPath);
				}
			}
		}
		return new NotFoundMediaResource(relPath);
	}
	
	public class BytesMediaResource implements MediaResource {
		
		private final DBMailAttachmentData datas;
		
		public BytesMediaResource(DBMailAttachmentData datas) {
			this.datas = datas;
		}

		@Override
		public String getContentType() {
			if(StringHelper.containsNonWhitespace(datas.getMimetype())) {
				return datas.getMimetype();
			}
			if(StringHelper.containsNonWhitespace(datas.getName())) {
				String mimeType = WebappHelper.getMimeType(datas.getName());
				if(StringHelper.containsNonWhitespace(mimeType)) {
					return mimeType;
				}
			}
			return "application/octet-stream";
		}

		@Override
		public Long getSize() {
			if(datas.getDatas() == null) return 0l;
			return new Long(datas.getDatas().length);
		}

		@Override
		public InputStream getInputStream() {
			return new ByteArrayInputStream(datas.getDatas());
		}

		@Override
		public Long getLastModified() {
			return null;
		}

		@Override
		public void prepare(HttpServletResponse hres) {
			String fileName = datas.getName();
			hres.setHeader("Content-Disposition","filename=\"" + StringHelper.urlEncodeISO88591(fileName) + "\"");
			hres.setHeader("Content-Description",StringHelper.urlEncodeISO88591(fileName));
		}

		@Override
		public void release() {
			//
		}
	}
}
