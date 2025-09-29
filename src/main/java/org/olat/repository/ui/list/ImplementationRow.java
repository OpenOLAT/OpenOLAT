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
package org.olat.repository.ui.list;

import java.util.Date;

import org.olat.core.commons.services.mark.Mark;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;

/**
 * 
 * Initial date: 23 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImplementationRow implements CurriculumElementRef {
	
	private final FormLink markLink;
	private final Curriculum curriculum;
	private final CurriculumElement curriculumElement;
	
	public ImplementationRow(CurriculumElement curriculumElement, Curriculum curriculum, FormLink markLink) {
		this.curriculumElement = curriculumElement;
		this.curriculum = curriculum;
		this.markLink = markLink;
	}

	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}
	
	@Override
	public Long getKey() {
		return curriculumElement.getKey();
	}
	
	public Curriculum getCurriculum() {
		return curriculum;
	}
	
	public String getDisplayName() {
		return curriculumElement.getDisplayName();
	}
	
	public String getIdentifier() {
		return curriculumElement.getIdentifier();
	}
	
	public Date getBeginDate() {
		return curriculumElement.getBeginDate();
	}
	
	public Date getEndDate() {
		return curriculumElement.getEndDate();
	}
	
	public CurriculumElementStatus getStatus() {
		return curriculumElement.getElementStatus();
	}
	
	public boolean isMarked() {
		String css = markLink.getComponent().getIconLeftCSS();
		return Mark.MARK_CSS_ICON.equals(css) || Mark.MARK_CSS_LARGE.equals(css);
	}
	
	public FormLink getMarkLink() {
		return markLink;
	}

}
