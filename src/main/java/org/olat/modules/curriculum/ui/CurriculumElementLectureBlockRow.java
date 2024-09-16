package org.olat.modules.curriculum.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.lecture.LectureBlock;

/**
 * 
 * Initial date: 9 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementLectureBlockRow {
	
	private final LectureBlock lectureBlock;
	private FormLink toolsLink;
	
	public CurriculumElementLectureBlockRow(LectureBlock lectureBlock) {
		this.lectureBlock = lectureBlock;
	}
	
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
}
