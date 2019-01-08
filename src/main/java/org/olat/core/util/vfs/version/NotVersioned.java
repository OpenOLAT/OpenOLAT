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
package org.olat.core.util.vfs.version;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;

/**
 * Description:<br>
 * This class implements the @see org.olat.core.util.vfs.version.Versions interface for a file
 * which is not versioned and will never be.
 * 
 * <P>
 * Initial Date: 21 sept. 2009 <br>
 * 
 * @author srosse
 */
public class NotVersioned implements Versions {

	@Override
	public boolean isVersioned() {
		return false;
	}

	@Override
	public List<VFSRevision> getRevisions() {
		return Collections.emptyList();
	}

	@Override
	public String getAuthor() {
		return "";
	}

	@Override
	public String getCreator() {
		return "";
	}

	@Override
	public String getComment() {
		return "";
	}

	@Override
	public String getRevisionNr() {
		return "";
	}

	@Override
	public boolean addVersion(Identity identity, String comment, InputStream newVersion) {
		return false;
	}

	@Override
	public boolean move(VFSContainer container) {
		return false;
	}

	@Override
	public boolean copy(VFSContainer container) {
		return false;
	}

	@Override
	public boolean delete(Identity identity, List<VFSRevision> versionsToDelete) {
		return false;
	}

	@Override
	public boolean restore(Identity identity, VFSRevision version, String comment) {
		return false;
	}
}
