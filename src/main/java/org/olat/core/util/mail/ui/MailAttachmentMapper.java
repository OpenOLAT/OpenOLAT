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

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;

/**
 * Initial Date:  28 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailAttachmentMapper implements Mapper {
	
	public static final String ATTACHMENT_CONTEXT =  "/attachments/";
	
	private final MailManager mailManager;
	
	public MailAttachmentMapper(MailManager mailManager) {
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
					Long key = Long.valueOf(attachmentKey);
					VFSLeaf datas = mailManager.getAttachmentDatas(key);
					return new VFSMediaResource(datas);	
				} catch(NumberFormatException e) {
					return new NotFoundMediaResource();
				}
			}
		}
		return new NotFoundMediaResource();
	}
}
