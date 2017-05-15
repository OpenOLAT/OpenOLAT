package org.olat.modules.lecture.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;

/**
 * 
 * Initial date: 12 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockWithTeachers {
	
	private final LectureBlock lectureBlock;
	private final List<Identity> teachers = new ArrayList<>(3);
	
	public LectureBlockWithTeachers(LectureBlock lectureBlock) {
		this.lectureBlock = lectureBlock;
	}
	
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}
	
	public List<Identity> getTeachers() {
		return teachers;
	}
}
