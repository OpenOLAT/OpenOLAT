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
package org.olat.ims.qti.fileresource;

import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti.editor.QTIEditorPackage;
import org.olat.ims.qti.editor.beecom.objects.QTIDocument;

/**
 * 
 * Initial date: 2 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTIReaderPackage implements QTIEditorPackage {
	
	private final VFSContainer baseDir;
	private final QTIDocument document;
	
	public QTIReaderPackage(VFSContainer baseDir, QTIDocument document) {
		this.baseDir = baseDir;
		this.document = document;
	}
	
	@Override
	public VFSContainer getBaseDir() {
		return baseDir;
	}
	
	@Override
	public String getMediaBaseURL() {
		return null;
	}
	
	@Override
	public QTIDocument getQTIDocument() {
		return document;
	}
	
	@Override
	public void serializeQTIDocument() {
		//
	}
}
