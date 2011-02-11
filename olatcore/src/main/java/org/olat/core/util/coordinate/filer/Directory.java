/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.core.util.coordinate.filer;

import java.io.File;

import org.olat.core.id.OLATResourceable;

/**
 * Description:<br>
 * This class represents a space to read and save data.
 * A directory
 * 
 * <P>
 * Initial Date:  09.11.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public interface Directory {
	
	/**
	 * returns a Directory object which represents the complete namespace reached.
	 * e.g. <FileTransaction>.getDirectory(coursefactory.class, "coursedata").getDirectory(mycourse).getDirectory(anIdentity) is a Directory which
	 * has a structure is meant to be for a certain course and and certain identity.<br>
	 * <FileTransaction>.getDirectory(coursefactory.class, "coursedata").getDirectory(anIdentity).getDirectory(course) would also be possible.
	 * <br>(the former mapping to a path ..../course_123/identity_456 and the latter to .../identity_456/course_123)<br>
	 * Deleting a directory will delete all subdirectories created out of this Directory.
	 * 
	 * 
	 * 
	 * @param ores the olatResourceable
	 * @return the Directory
	 */
	public Directory getDirectoryFor(OLATResourceable ores);
	
	/**
	 * creates if needed and returns the java.io.File that is associated to the current Directory object.
	 * In order to guarantee to the namespaces are independent of each other, it is important that the user must not use <br>
	 * a) the absolute path of the file<br>
	 * b) the getParent() function to obtain a parent file(name). <br>
	 * cluster::: is this sufficient? the olat vfs could have been used here, but it first needs to be improved a bit.
	 * 
	 * @return the File associated with this Directory.
	 */
	public File getFileSystemDirectory();
	
	/**
	 * deletes this directory, including all directories "below" which were created using getDirectoryFor(OLATResourceable ores).
	 * The deletion of all data is postponed until the associated transaction is committed. In case of a rollback, nothing is done.
	 *
	 */
	public void delete();
}
