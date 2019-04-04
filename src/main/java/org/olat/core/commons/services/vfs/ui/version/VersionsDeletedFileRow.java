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
package org.olat.core.commons.services.vfs.ui.version;

import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 3 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VersionsDeletedFileRow {
	
	private final Long revisionKey;
	private final int revisionNr;
	private final long size;
	private final String relativePath;
	private final String filename;
	
	public VersionsDeletedFileRow(VFSRevision revision) {
		revisionKey = revision instanceof Persistable ? ((Persistable)revision).getKey() : null;
		revisionNr = revision.getRevisionNr();
		size = revision.getSize();
		relativePath = revision.getMetadata().getRelativePath();
		filename = revision.getMetadata().getFilename();
	}
	
	public Long getRevisionKey() {
		return revisionKey;
	}
	
	public int getRevisionNr() {
		return revisionNr;
	}
	
	public long getSize() {
		return size;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public String getFilename() {
		return filename;
	}
	
	
	

}
