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
package org.olat.modules.ceditor.model;

import java.io.Serializable;

/**
 * Represent a file defined by it's path and name stored
 * somewhere. Somewhere is defined by the implementation
 * of the @see org.olat.modules.ceditor.DataStorage and
 * this implementation must handle the relative path
 * constantly.
 * 
 * Initial date: 21 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface StoredData extends Serializable {
	
	/**
	 * 
	 * @return A description of the data (can be null)
	 */
	public String getDescription();
	
	/**
	 * @return The relative path of the file.
	 */
	public String getStoragePath();
	
	public void setStoragePath(String relativePath);

	/**
	 * 
	 * @return The name of the file.
	 */
	public String getRootFilename();
	
	public void setRootFilename(String name);

}
