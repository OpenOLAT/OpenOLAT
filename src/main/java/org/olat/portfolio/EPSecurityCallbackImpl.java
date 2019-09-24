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
 * Very basic implementation of the EPSecurityCallback
 * 
 * <P>
 * Initial Date:  12 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPSecurityCallbackImpl implements EPSecurityCallback {
	
	private final boolean canEditStructure;
	private final boolean canEditReflexion;
	private final boolean canEditTags;
	private final boolean canShare;
	private final boolean canAddArtefact;
	private final boolean canRemoveArtefactFromStruct;
	private final boolean canAddStructure;
	private final boolean canAddPage;
	private final boolean canView;
	private final boolean canCommentAndRate;
	private final boolean canSubmitAssess;
	private final boolean restrictionsEnabled;
	private final boolean isOwner;
	
	public EPSecurityCallbackImpl(boolean canEdit, boolean canView) {
		this.canEditStructure = canEdit;
		this.canEditReflexion = canEdit;
		this.canEditTags = canEdit;
		this.canShare = canEdit;
		this.canAddArtefact = canEdit;
		this.canRemoveArtefactFromStruct = canEdit;
		this.canAddStructure = canEdit;
		this.canAddPage = canEdit;
		this.canView = canView;
		this.canCommentAndRate = canView;
		this.canSubmitAssess = false;
		this.restrictionsEnabled = false;
		this.isOwner = false;
	}
	
	protected EPSecurityCallbackImpl(boolean canEditStructure, boolean canEditReflexion, boolean canEditTags, boolean canShare, boolean canAddArtefact, boolean canRemoveArtefactFromStruct, boolean canAddStructure, boolean canAddPage,
			boolean canView, boolean canCommentAndRate, boolean canSubmitAssess, boolean restrictionsEnabled, boolean isOwner) {
		this.canEditStructure = canEditStructure;
		this.canEditReflexion = canEditReflexion;
		this.canEditTags = canEditTags;
		this.canShare = canShare;
		this.canAddArtefact = canAddArtefact;
		this.canRemoveArtefactFromStruct = canRemoveArtefactFromStruct;
		this.canAddStructure = canAddStructure;
		this.canAddPage = canAddPage;
		this.canView = canView;
		this.canCommentAndRate = canCommentAndRate;
		this.canSubmitAssess = canSubmitAssess;
		this.restrictionsEnabled = restrictionsEnabled;
		this.isOwner = isOwner;
	}
	
	

	@Override
	public boolean isOwner() {
		return isOwner;
	}

	@Override
	public boolean isRestrictionsEnabled() {
		return restrictionsEnabled;
	}

	@Override
	public boolean canEditStructure() {
		return canEditStructure;
	}

	@Override
	public boolean canEditReflexion() {
		return canEditReflexion;
	}
	
	@Override
	public boolean canEditTags() {
		return canEditTags;
	}

	@Override
	public boolean canShareMap() {
		return canShare;
	}

	@Override
	public boolean canAddArtefact() {
		return canAddArtefact;
	}

	@Override
	public boolean canAddStructure() {
		return canAddStructure;
	}

	@Override
	public boolean canAddPage() {
		return canAddPage;
	}

	@Override
	public boolean canCommentAndRate() {
		return canCommentAndRate;
	}

	@Override
	public boolean canSubmitAssess() {
		return canSubmitAssess;
	}

	@Override
	public boolean canView() {
		return canView;
	}

	@Override
	public boolean canRemoveArtefactFromStruct() {
		return canRemoveArtefactFromStruct;
	}
}