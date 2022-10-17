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
package org.olat.course.nodes.ms;

import java.io.File;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 1 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DocumentsMapper implements Mapper {
	
	private final List<File> documents;
	
	public DocumentsMapper(List<File> documents) {
		this.documents = documents;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if(StringHelper.containsNonWhitespace(relPath)) {
			if(relPath.startsWith("/")) {
				relPath = relPath.substring(1, relPath.length());
			}
		
			if(documents != null) {
				for(File document:documents) {
					if(relPath.equals(document.getName())) {
						return new FileMediaResource(document, true);
					}
				}
			}
		}
		return new NotFoundMediaResource();
	}
}
