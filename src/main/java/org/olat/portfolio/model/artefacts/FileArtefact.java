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

package org.olat.portfolio.model.artefacts;

import org.olat.core.gui.util.CSSHelper;

/**
 * Description:<br>
 * The file artefact
 * 
 * <P>
 * Initial Date:  25 jun. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class FileArtefact extends AbstractArtefact {

	private static final long serialVersionUID = -3719232086297906741L;
	// the filename of the linked file, relative to artefact-folder
	private String filename;
	public static final String FILE_ARTEFACT_TYPE = "bc";

	@Override
	public String getIcon() {
		if (getFilename()!=null){
			return CSSHelper.createFiletypeIconCssClassFor(getFilename());
		}
		return "o_filetype_file";
	}


	@Override
	public String getResourceableTypeName() {
		return FILE_ARTEFACT_TYPE;
	}


	/**
	 * @param filename The filename to set.
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}


	/**
	 * @return Returns the filename.
	 */
	public String getFilename() {
		return filename;
	}
}
