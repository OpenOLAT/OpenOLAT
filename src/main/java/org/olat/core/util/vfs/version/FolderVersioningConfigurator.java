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

import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * Description:<br>
 * Interface for the configuration of versioning.
 * 
 * <P>
 * Initial Date:  21 sept. 2009 <br>
 *
 * @author srosse
 */
public interface FolderVersioningConfigurator extends ConfigOnOff {
	
	/**
	 * The absolut limit for this instance
	 * @return -1 for versioning without limit, 0 for no versioning, 1 - n is the maximum number of revision per file
	 */
	public int getMaxNumOfVersionsAllowed();

	/**
	 * @param relPath
	 * @return -1 for versioning without limit, 0 for no versioning, 1 - n is the maximum number of revision per file
	 */
	public int versionAllowed(String relPath);

	/**
	 * @param container
	 * @return true if versioning is enabled for the container given as parameter
	 */
	public boolean versionEnabled(VFSContainer container);

}
