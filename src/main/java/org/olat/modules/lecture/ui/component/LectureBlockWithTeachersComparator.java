package org.olat.modules.lecture.ui.component;

import java.util.Comparator;
import java.util.Date;

import org.olat.modules.lecture.model.LectureBlockWithTeachers;

/**
 * 
 * Initial date: 29 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockWithTeachersComparator implements Comparator<LectureBlockWithTeachers> {

	@Override
	public int compare(LectureBlockWithTeachers o1, LectureBlockWithTeachers o2) {
		Date s1 = o1.getLectureBlock().getStartDate();
		Date s2 = o2.getLectureBlock().getStartDate();
		if(s1 == null && s2 == null) return 0;
		if(s1 == null) return 1;
		if(s2 == null) return -1;
		return s1.compareTo(s2);
	}
}
