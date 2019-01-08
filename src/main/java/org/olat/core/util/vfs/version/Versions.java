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
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * Description:<br>
 * The interface which enabled the versioning
 * <P>
 * Initial Date: 21 sept. 2009 <br>
 * 
 * @author srosse
 */
public interface Versions {

	/**
	 * @return true if the file is versioned
	 */
	public boolean isVersioned();

	/**
	 * @return the current version number
	 */
	public String getRevisionNr();
	
	/**
	 * @return the current author of the file
	 */
	public String getAuthor();

	/**
	 * @return creator of the file
	 */
	public String getCreator();
	
	/**
	 * @return comment on the current version
	 */
	public String getComment();

	/**
	 * @return the list of revisions
	 */
	public List<VFSRevision> getRevisions();

	/**
	 * add a new version of the file
	 * @param identity
	 * @param comment
	 * @param newVersion
	 * @return
	 */
	public boolean addVersion(Identity identity, String comment, InputStream newVersion);

	/**
	 * move the file
	 * @param container
	 * @return true if successful
	 */
	public boolean move(VFSContainer container);
	
	/**
	 * Copy the file
	 * @param container
	 * @return true if successful
	 */
	public boolean copy(VFSContainer container);

	/**
	 * restore the file to the revision given as parameter
	 * @param identity
	 * @param revision
	 * @return
	 */
	public boolean restore(Identity identity, VFSRevision revision, String comment);

	/**
	 * Delete the list of revisions given as parameter
	 * @param identity
	 * @param revisionsToDelete
	 * @return
	 */
	public boolean delete(Identity identity, List<VFSRevision> revisionsToDelete);
}
