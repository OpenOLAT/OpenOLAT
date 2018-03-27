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
package org.olat.ims.qti21.ui.assessment.model;

import org.olat.core.id.Identity;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;

/**
 * 
 * Initial date: 2 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectionIdentityAssessmentItemRow extends AssessmentItemListEntry {
	
	private final AssessmentItem item;
	private final AssessmentItemRef itemRef;
	private final QTI21QuestionType itemType;
	private final ManifestMetadataBuilder metadata;
	private AssessmentItemSession itemSession;
	private final ItemSessionState itemSessionState;
	private final boolean manualCorrection;
	
	public CorrectionIdentityAssessmentItemRow(Identity assessedIdentity, AssessmentItem item, AssessmentItemRef itemRef,
			ManifestMetadataBuilder metadata, AssessmentTestSession testSession, AssessmentItemSession itemSession,
			ItemSessionState itemSessionState, boolean manualCorrection) {
		super(assessedIdentity, testSession, itemSession, itemRef, item.getTitle(), null);
		this.item = item;
		this.itemRef = itemRef;
		this.metadata = metadata;
		itemType = QTI21QuestionType.getTypeRelax(item);
		this.itemSession = itemSession;
		this.itemSessionState = itemSessionState;
		this.manualCorrection = manualCorrection;
		if(itemType != null) {
			setLabelCssClass(itemType.getCssClass());
		}
	}

	public AssessmentItem getItem() {
		return item;
	}
	
	public String getSectionTitle() {
		return itemRef.getParentSection().getTitle();
	}
	
	public String getItemTitle() {
		return item.getTitle();
	}

	public QTI21QuestionType getItemType() {
		return itemType;
	}
	
	/**
	 * @return The metadata or null
	 */
	public ManifestMetadataBuilder getMetadata() {
		return metadata;
	}
	
	public String getKeywords() {
		return metadata == null ? null : metadata.getGeneralKeywords();
	}
	
	public boolean isManualCorrection() {
		return manualCorrection;
	}
	
	public boolean isAnswered() {
		return itemSessionState.isResponded();
	}
	
	@Override
	public boolean isToReview() {
		return itemSession == null ? false : itemSession.isToReview();
	}
	
	public boolean isCorrected() {
		return !manualCorrection || (manualCorrection && getManualScore() != null);
	}
	
	@Override
	public int hashCode() {
		return itemRef.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CorrectionIdentityAssessmentItemRow) {
			CorrectionIdentityAssessmentItemRow row = (CorrectionIdentityAssessmentItemRow)obj;
			return itemRef.equals(row.itemRef);
		}
		return false;
	}
}
