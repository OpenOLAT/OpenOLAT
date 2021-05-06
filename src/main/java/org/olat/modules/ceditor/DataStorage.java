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
package org.olat.modules.ceditor;

import java.io.File;
import java.io.IOException;

import org.olat.modules.ceditor.model.StoredData;

/**
 * 
 * Initial date: 21 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface DataStorage {
	
	public File getFile(StoredData metadata);
	
	/**
	 * The method will save the file and enrich the specified
	 * stored object with the path and the file name.
	 * 
	 * @param file The file to store
	 * @param metadata The metadata object
	 * @return 
	 */
	public StoredData save(String filename, File file, StoredData metadata) throws IOException;
	
	/**
	 * The method will copy the file from the origin to a new file in the same
	 * folder with a unique name.
	 * 
	 * @param original
	 * @param copy 
	 * @return the copy StoredDate with enriched data
	 * @throws IOException
	 */
	public StoredData copy(StoredData original, StoredData copy) throws IOException;

}
