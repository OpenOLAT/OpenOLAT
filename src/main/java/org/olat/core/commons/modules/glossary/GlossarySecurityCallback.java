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
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.core.commons.modules.glossary;

/**
 * 
 * Description:<br>
 * SecurityCallback for glossar
 * 
 * <P>
 * Initial Date:  16 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http.//www.frentix.com
 */
public interface GlossarySecurityCallback {
	
	public boolean isUserAllowToEditEnabled();
	
	/**
	 * Can add new glossary items
	 * @return
	 */
	public boolean canAdd();
	
	/**
	 * Can edit a glossary item
	 * @param gi The glossary item
	 * @return true if can edit the item
	 */
	public boolean canEdit(GlossaryItem gi);
	
	/**
	 * Can edit a glossary item
	 * @param gi The glossary item
	 * @return true if can edit the item
	 */
	public boolean canDelete(GlossaryItem gi);

}
