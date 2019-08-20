package org.olat.modules.lecture.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.modules.lecture.AbsenceNoticeToLectureBlock;
import org.olat.modules.lecture.AbsenceNoticeToRepositoryEntry;
import org.olat.modules.lecture.LectureBlockRollCall;

/**
 * 
 * Initial date: 20 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceNoticeRelationsAuditImpl {
	
	private List<LectureBlockRollCall> rollCalls;
	private List<AbsenceNoticeToLectureBlock> noticeToBlocks;
	private List<AbsenceNoticeToRepositoryEntry> noticeToEntries;
	
	public List<AbsenceNoticeToLectureBlock> getNoticeToBlocks() {
		return noticeToBlocks;
	}
	
	public void setNoticeToBlocks(List<AbsenceNoticeToLectureBlock> noticeToBlocks) {
		this.noticeToBlocks = noticeToBlocks == null ? new ArrayList<>(1) : new ArrayList<>(noticeToBlocks);
	}
	
	public List<AbsenceNoticeToRepositoryEntry> getNoticeToEntries() {
		return noticeToEntries;
	}
	
	public void setNoticeToEntries(List<AbsenceNoticeToRepositoryEntry> noticeToEntries) {
		this.noticeToEntries = noticeToEntries == null ? new ArrayList<>(1) : new ArrayList<>(noticeToEntries);
	}

	public List<LectureBlockRollCall> getRollCalls() {
		return rollCalls;
	}

	public void setRollCalls(List<LectureBlockRollCall> rollCalls) {
		this.rollCalls = rollCalls == null ? new ArrayList<>(1) : new ArrayList<>(rollCalls);
	}
	
	
}
