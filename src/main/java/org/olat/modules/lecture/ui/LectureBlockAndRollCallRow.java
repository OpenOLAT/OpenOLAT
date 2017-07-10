package org.olat.modules.lecture.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.lecture.model.LectureBlockAndRollCall;

/**
 * 
 * Initial date: 10 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockAndRollCallRow {
	
	private final LectureBlockAndRollCall row;
	private FormLink appealButton;
	
	public LectureBlockAndRollCallRow(LectureBlockAndRollCall row) {
		this.row = row;
	}

	public LectureBlockAndRollCall getRow() {
		return row;
	}

	public FormLink getAppealButton() {
		return appealButton;
	}

	public void setAppealButton(FormLink appealButton) {
		this.appealButton = appealButton;
	}
}
