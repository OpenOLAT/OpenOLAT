/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.copy;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyElementSetting;
import org.olat.modules.curriculum.site.ComparableCurriculumElementRow;

/**
 * 
 * Initial date: 18 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CopyElementRow implements ComparableCurriculumElementRow {
	
	private boolean hasChildren;
	private CopyElementRow parentElementRow;
	private final CurriculumElementType type;
	private final CurriculumElement curriculumElement;
	
	private final long numOfResources;
	private final long numOfLectureBlocks;
	
	private DateChooser beginDateEl;
	private DateChooser endDateEl;
	private final CopyElementSetting setting;
	
	private CopyElementDetailsController detailsCtrl;
	
	public CopyElementRow(CurriculumElement curriculumElement, CopyElementSetting setting,
			long numOfResources, long numOfLectureBlocks) {
		this.curriculumElement = curriculumElement;
		this.type = curriculumElement.getType();
		this.numOfLectureBlocks = numOfLectureBlocks;
		this.numOfResources = numOfResources;
		this.setting = setting;
	}

	@Override
	public Long getKey() {
		return curriculumElement.getKey();
	}

	@Override
	public String getCrump() {
		return getDisplayName();
	}

	@Override
	public Integer getPos() {
		return curriculumElement.getPos();
	}

	@Override
	public Integer getPosCurriculum() {
		return curriculumElement.getPosCurriculum();
	}

	@Override
	public String getDisplayName() {
		return StringHelper.containsNonWhitespace(setting.identifier())
				? setting.displayName()
				: curriculumElement.getDisplayName();
	}

	@Override
	public String getIdentifier() {
		return StringHelper.containsNonWhitespace(setting.identifier())
				? setting.identifier()
				: curriculumElement.getIdentifier();
	}

	@Override
	public Date getBeginDate() {
		return curriculumElement.getBeginDate();
	}

	public DateChooser getBeginDateEl() {
		return beginDateEl;
	}

	public void setBeginDateEl(DateChooser beginDateEl) {
		this.beginDateEl = beginDateEl;
	}

	public DateChooser getEndDateEl() {
		return endDateEl;
	}

	public void setEndDateEl(DateChooser endDateEl) {
		this.endDateEl = endDateEl;
	}

	public long getNumOfResources() {
		return numOfResources;
	}

	public long getNumOfLectureBlocks() {
		return numOfLectureBlocks;
	}

	@Override
	public Long getParentKey() {
		return curriculumElement.getParent() == null
				? null
				: curriculumElement.getParent().getKey();
	}

	@Override
	public CopyElementRow getParent() {
		return parentElementRow;
	}
	
	public void setParent(CopyElementRow row) {
		this.parentElementRow = row;
		if(row != null) {
			row.setHasChildren(true);
		}
	}
	
	public boolean isHasChildren() {
		return hasChildren;
	}
	
	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}
	
	public String getTypeDisplayName() {
		return type == null ? null : type.getDisplayName();
	}
	
	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}
	
	public CopyElementSetting getSetting() {
		return setting;
	}

	public boolean isDetailsControllerAvailable() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().isVisible();
		}
		return false;
	}

	public CopyElementDetailsController getDetailsController() {
		return detailsCtrl;
	}
	
	public String getDetailsControllerName() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().getComponent().getComponentName();
		}
		return null;
	}
	
	public void setDetailsController(CopyElementDetailsController detailsCtrl) {
		this.detailsCtrl = detailsCtrl;
	}
}
