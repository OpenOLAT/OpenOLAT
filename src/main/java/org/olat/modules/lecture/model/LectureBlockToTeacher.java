package org.olat.modules.lecture.model;

import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;

/**
 * 
 * Initial date: 10 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockToTeacher {
	
	private final Identity teacher;
	private final LectureBlock lectureBlock;
	
	public LectureBlockToTeacher(Identity teacher, LectureBlock lectureBlock) {
		this.teacher = teacher;
		this.lectureBlock = lectureBlock;
	}

	public Identity getTeacher() {
		return teacher;
	}

	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}
}
