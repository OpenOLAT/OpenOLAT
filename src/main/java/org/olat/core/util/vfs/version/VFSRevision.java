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

/**
 * 
 * Description:<br>
 * This interface describes a revision of a file
 * 
 * <P>
 * Initial Date: 21 sept. 2009 <br>
 * 
 * @author srosse
 */
public interface VFSRevision {

	/**
	 * @return author of the revision
	 */
	public String getAuthor();

	/**
	 * @return timestamp of the creation of this revision
	 */
	public long getLastModified();

	/**
	 * @return the revision number
	 */
	public String getRevisionNr();

	/**
	 * @return comment
	 */
	public String getComment();

	/**
	 * @return name of the file
	 */
	public String getName();

	/**
	 * @return size of the file
	 */
	public long getSize();

	/**
	 * @return InputStream of the file
	 */
	public InputStream getInputStream();
}
