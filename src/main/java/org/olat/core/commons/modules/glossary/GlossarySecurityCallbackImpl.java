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

package org.olat.core.commons.modules.glossary;

import java.util.List;

/**
 * 
 * Description:<br>
 * SecurityCallback for glossar
 * 
 * <P>
 * Initial Date:  16 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http.//www.frentix.com
 */
public class GlossarySecurityCallbackImpl implements GlossarySecurityCallback {
	
	private final boolean hasGlossaryRights;
	private final boolean owner;
	private final boolean editByUserEnabled;
	private final Long meKey;
	
	/**
	 * Constructor for read-only glossary
	 */
	public GlossarySecurityCallbackImpl() {
		this(false, false, false, Long.valueOf(0));
	}
	
	public GlossarySecurityCallbackImpl(boolean hasGlossaryRights, boolean owner, boolean editByUserEnabled, Long identityKey) {
		this.hasGlossaryRights = hasGlossaryRights;
		this.owner = owner;
		this.editByUserEnabled = editByUserEnabled;
		this.meKey = identityKey;
	}
	
	
	
	@Override
	public boolean isUserAllowToEditEnabled() {
		return editByUserEnabled;
	}

	/**
	 * @see org.olat.core.commons.modules.glossary.GlossarySecurityCallback#canAdd()
	 */
	@Override
	public boolean canAdd() {
		return hasGlossaryRights || owner || editByUserEnabled;
	}

	/**
	 * @see org.olat.core.commons.modules.glossary.GlossarySecurityCallback#canEdit(org.olat.core.commons.modules.glossary.GlossaryItem)
	 */
	public boolean canEdit(GlossaryItem gi) {
		if(hasGlossaryRights || canUserEdit(gi) || owner) {
			return true;
		}
		return false;
	}
	
	/**
	 * @see org.olat.core.commons.modules.glossary.GlossarySecurityCallback#canDelete(org.olat.core.commons.modules.glossary.GlossaryItem)
	 */
	@Override
	public boolean canDelete(GlossaryItem gi) {
		//same as edit permission
		return canEdit(gi);
	}
	
	private boolean canUserEdit(GlossaryItem gi) {
		if(editByUserEnabled) {
			List<Revision> revisions = gi.getRevHistory();
			if(revisions != null) {
				for(Revision revision:revisions) {
					Author author = revision.getAuthor();
					Long authorKey = author.extractKey();
					if(authorKey != null && meKey.equals(authorKey)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}