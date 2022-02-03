package org.olat.modules.curriculum.ui;

import org.olat.core.gui.translator.Translator;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;

/**
 * 
 * Initial date: 3 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumHelper {
	
	private CurriculumHelper() {
		//
	}
	
	public static String getLabel(CurriculumElement element, Translator translator) {
		Curriculum curriculum = element.getCurriculum();
		CurriculumElement parentElement = element.getParent();
		
		String[] args = new String[] {
			element.getDisplayName(),										// 0
			element.getIdentifier(),										// 1
			parentElement == null ? null : parentElement.getDisplayName(),	// 2
			parentElement == null ? null : parentElement.getIdentifier(),	// 3
			curriculum.getDisplayName(),									// 4
			curriculum.getIdentifier()										// 5
		};

		String i18nKey = parentElement == null ? "select.value.element" : "select.value.element.parent";
		return translator.translate(i18nKey, args);
	}
}
