/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position.model;

import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;

/**
 * 
 * Initial date: 18 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditVisibilityRow {
	
	private final Object userObject;
	private final String title;
	private final MultipleSelectionElement expertsEl;
	private final MultipleSelectionElement refereesEl;
	private final MultipleSelectionElement comparativeExpertsEl;
	private final MultipleSelectionElement[] facultyMembersEls;
	private final MultipleSelectionElement publicFeedbackEl;
	
	public EditVisibilityRow(Object userObject, String title,
			MultipleSelectionElement expertsEl, MultipleSelectionElement refereesEl,
			MultipleSelectionElement comparativeExpertsEl, MultipleSelectionElement[] facultyMembersEls,
			MultipleSelectionElement publicFeedbackEl) {
		this.userObject = userObject;
		this.title = title;
		this.expertsEl = expertsEl;
		this.refereesEl = refereesEl;
		this.comparativeExpertsEl = comparativeExpertsEl;
		this.facultyMembersEls = facultyMembersEls;
		this.publicFeedbackEl = publicFeedbackEl;
	}
	
	public Object getUserObject() {
		return userObject;
	}

	public String getTitle() {
		return title;
	}

	public MultipleSelectionElement expertsEl() {
		return expertsEl;
	}
	
	public boolean isExpertsSelected() {
		return isSelected(expertsEl);
	}

	public MultipleSelectionElement refereesEl() {
		return refereesEl;
	}
	
	public boolean isRefereesElSelected() {
		return isSelected(refereesEl);
	}

	public MultipleSelectionElement comparativeExpertsEl() {
		return comparativeExpertsEl;
	}
	
	public boolean isComparativeExpertsElSelected() {
		return isSelected(comparativeExpertsEl);
	}
	
	public MultipleSelectionElement[] facultyMembersEls() {
		return facultyMembersEls;
	}
	
	public boolean isFacultyMembersElsSelected(int i) {
		if(facultyMembersEls != null && i>= 0 && i< facultyMembersEls.length) {
			return isSelected(facultyMembersEls[i]);
		}
		return false;
	}
	
	public MultipleSelectionElement publicFeedbackEl() {
		return publicFeedbackEl;
	}
	
	private boolean isSelected(MultipleSelectionElement el) {
		return el != null && el.isAtLeastSelected(1);
	}
}
