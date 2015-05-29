package org.olat.ims.qti21.repository.handlers;

import org.junit.Test;

public class QTI21AssessmentTestHandlerTest {
	
	@Test
	public void createImsManfest() {
		
		QTI21AssessmentTestHandler handler = new QTI21AssessmentTestHandler();
		handler.createMinimalAssessmentTest();
		
	}

}
