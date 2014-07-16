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

package org.olat.portfolio;

/**
 * 
 * Description:<br>
 * Standard implementation where admin and owner has the right to edit
 * 
 * <P>
 * Initial Date:  12 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPSecurityCallbackOwner implements EPSecurityCallback {

	private final boolean admin;
	private final boolean owner;
	private final boolean comments;
	
	protected EPSecurityCallbackOwner(boolean admin, boolean owner, boolean comments) {
		this.admin = admin;
		this.owner = owner;
		this.comments = comments;
	}
	
	private final boolean canEdit() {
		return admin || owner;
	}
	
	@Override
	public boolean isOwner() {
		return owner;
	}

	@Override
	public boolean isRestrictionsEnabled() {
		return false;
	}

	@Override
	public boolean canEditStructure() {
		return canEdit();
	}
	
	@Override
	public boolean canEditReflexion() {
		return owner;
	}

	@Override
	public boolean canEditTags() {
		return owner;
	}

	@Override
	public boolean canShareMap() {
		return canEdit();
	}

	@Override
	public boolean canAddArtefact() {
		return canEdit();
	}


	@Override
	public boolean canAddStructure() {
		return canEdit();
	}


	@Override
	public boolean canAddPage() {
		return canEdit();
	}

	@Override
	public boolean canCommentAndRate() {
		return comments;
	}
	
	@Override
	public boolean canSubmitAssess() {
		return false;
	}

	@Override
	public boolean canView() {
		return admin || owner;
	}

	@Override
	public boolean canRemoveArtefactFromStruct() {
		return admin || owner;
	}
}