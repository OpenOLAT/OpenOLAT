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

/**
 * Description:<br>
 * Artefact of type text
 * 
 * <P>
 * Initial Date:  01.09.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPTextArtefact extends AbstractArtefact {

	private static final long serialVersionUID = -3705686575442377717L;
	public static final String TEXT_ARTEFACT_TYPE = "text";
	
	/**
	 * @see org.olat.core.id.OLATResourceable#getResourceableTypeName()
	 */
	@Override
	public String getResourceableTypeName() {
		return TEXT_ARTEFACT_TYPE;
	}

	/**
	 * @see org.olat.portfolio.model.artefacts.AbstractArtefact#getIcon()
	 */
	@Override
	public String getIcon() {
		return "o_filetype_txt";
	}
}
