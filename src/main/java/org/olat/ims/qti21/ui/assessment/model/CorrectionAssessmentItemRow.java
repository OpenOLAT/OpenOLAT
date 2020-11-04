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

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;

/**
 * 
 * Initial date: 26 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectionAssessmentItemRow extends CorrectionRow {
	
	private final AssessmentItem item;
	private final AssessmentItemRef itemRef;
	private final QTI21QuestionType itemType;
	private final ManifestMetadataBuilder metadata;
	
	private final FormLink toolsLink;
	
	public CorrectionAssessmentItemRow(AssessmentItemRef itemRef, AssessmentItem item, ManifestMetadataBuilder metadata, FormLink toolsLink) {
		this.item = item;
		this.itemRef = itemRef;
		this.metadata = metadata;
		this.toolsLink = toolsLink;
		itemType = QTI21QuestionType.getTypeRelax(item);
	}
	
	public String getSectionTitle() {
		return itemRef.getParentSection().getTitle();
	}

	public AssessmentItemRef getItemRef() {
		return itemRef;
	}

	public AssessmentItem getItem() {
		return item;
	}
	
	public String getItemTitle() {
		return item == null ? "ERROR" : item.getTitle();
	}
	
	public QTI21QuestionType getItemType() {
		return itemType;
	}
	
	public FormLink getToolsLink() {
		return toolsLink;
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

	@Override
	public int hashCode() {
		return itemRef.getIdentifier().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CorrectionAssessmentItemRow) {
			CorrectionAssessmentItemRow row = (CorrectionAssessmentItemRow)obj;
			return itemRef.equals(row.itemRef);
		}
		return false;
	}
}