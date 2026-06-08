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
package org.olat.modules.selectus.ui;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;

import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.resources.AttachmentMediaResource;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  12 avr. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PositionDocumentMapper implements Mapper  {
	
	private Position position;

	public PositionDocumentMapper(Position position) {
		this.position = position;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if(relPath.startsWith("/")) {
			relPath = relPath.substring(1, relPath.length());
		}
		
		int indexPos = relPath.indexOf('/');
		if(indexPos <= 0) {
			return new NotFoundMediaResource();
		}
		
		String posStr = relPath.substring(0, indexPos);
		String name = relPath.substring(indexPos + 1);
		
		MediaResource resource = null;
		if(position.getDocument1() != null && "1".equals(posStr) && name.equals(position.getDocument1().getName())) {
			resource = handleAttachment(position.getDocument1());
		} else if(position.getDocument2() != null && "2".equals(posStr) && name.equals(position.getDocument2().getName())) {
			resource = handleAttachment(position.getDocument2());
		} else if(position.getDocument3() != null && "3".equals(posStr) && name.equals(position.getDocument3().getName())) {
			resource = handleAttachment(position.getDocument3());
		}
		
		if(resource == null) {
			return new NotFoundMediaResource();
		}
		return resource;
	}
	
	private MediaResource handleAttachment(Attachment attachment) {
		return new AttachmentMediaResource(attachment);
	}
}
