package org.olat.ims.qti21.ui.assessment;

import java.util.Date;

import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.audit.CandidateEvent;
import org.olat.ims.qti21.ui.CandidateSessionContext;

/**
 * 
 * Initial date: 16.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TerminatedStaticCandidateSessionContext implements CandidateSessionContext {
	
	private final AssessmentTestSession testSession;
	
	public TerminatedStaticCandidateSessionContext(AssessmentTestSession testSession) {
		this.testSession = testSession;
	}

	@Override
	public boolean isTerminated() {
		return true;
	}

	@Override
	public AssessmentTestSession getCandidateSession() {
		return testSession;
	}

	@Override
	public CandidateEvent getLastEvent() {
		return null;
	}

	@Override
	public Date getCurrentRequestTimestamp() {
		return null;
	}

	@Override
	public boolean isMarked(String itemKey) {
		return false;
	}
}
