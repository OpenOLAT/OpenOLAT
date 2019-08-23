/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.ims.qti.navigator;

import java.io.Serializable;
import java.util.Iterator;

import org.olat.core.CoreSpringFactory;
import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.container.AssessmentContext;
import org.olat.ims.qti.container.ItemContext;
import org.olat.ims.qti.container.ItemInput;
import org.olat.ims.qti.container.ItemsInput;
import org.olat.ims.qti.container.Output;
import org.olat.ims.qti.container.SectionContext;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.modules.iq.IQManager;

/**
 */
public abstract class DefaultNavigator implements Navigator, Serializable {

	private AssessmentInstance assessmentInstance;

	private Info info;
	private transient NavigatorDelegate delegate;

	/**
	 * 
	 */
	public DefaultNavigator(AssessmentInstance assessmentInstance, NavigatorDelegate delegate) {
		this.assessmentInstance = assessmentInstance;
		this.delegate = delegate;
		info = new Info();
	}
	
	public void setDelegate(NavigatorDelegate delegate) {
		this.delegate = delegate;
	}

	/**
	 * @return AssessmentContext
	 */
	public AssessmentContext getAssessmentContext() {
		return assessmentInstance.getAssessmentContext();
	}

	/**
		 * @return
		 */
	protected AssessmentInstance getAssessmentInstance() {
		return assessmentInstance;
	}

	/**
	 * 
	 * @param curitsinp
	 * @return the status of the operation like success or error
	 */
	public int submitOneItem(ItemsInput curitsinp) {
		if (info.getStatus() != QTIConstants.ASSESSMENT_RUNNING) throw new RuntimeException("assessment is NOT running yet or anymore");
		int cnt = curitsinp.getItemCount();
		if (cnt == 0) throw new RuntimeException("program bug: not even one iteminput in the answer");
		if (cnt > 1) throw new RuntimeException("may only submit 1 item");
		ItemInput itemInput = curitsinp.getItemInputIterator().next();
		String ident = itemInput.getIdent();
		AssessmentContext ac = getAssessmentContext();
		SectionContext sc = ac.getCurrentSectionContext();
		ItemContext it = sc.getCurrentItemContext();
		ItemContext ict = sc.getItemContext(ident);
		if (ict == null) throw new RuntimeException("submitted item id ("+ident+")not found in xml");
		if (ict != it) throw new RuntimeException("answering to a non-current item");
		if (!ac.isOpen()) {
			// assessment must also be open (=on time)
			return QTIConstants.ERROR_ASSESSMENT_OUTOFTIME;
		}
		if (!sc.onTime()) {
			// section of the current item must also be open (=on time)
			return QTIConstants.ERROR_SUBMITTEDSECTION_OUTOFTIME;
		}
		if (!ict.isOnTime()) {
			// current item must be on time
			return QTIConstants.ERROR_SUBMITTEDITEM_OUTOFTIME;
		}
		if (!ict.isUnderMaxAttempts()) {
			// current item must be below maxattempts
			return QTIConstants.ERROR_SUBMITTEDITEM_TOOMANYATTEMPTS;
		}
		int subres = ict.addItemInput(itemInput);
		ict.eval(); // to have an up-to-date score
		return subres;
	}
	
	public int submitMultipleItems(ItemsInput curitsinp) {
		// = submit a whole section at once
		if (info.getStatus() != QTIConstants.ASSESSMENT_RUNNING) throw new RuntimeException("assessment is NOT running yet or anymore");
		int cnt = curitsinp.getItemCount();
		if (cnt == 0) throw new RuntimeException("bug: not even one iteminput in the answer");
		AssessmentContext ac = getAssessmentContext();
		SectionContext sc = ac.getCurrentSectionContext();

		if (!ac.isOpen())
			return QTIConstants.ERROR_ASSESSMENT_OUTOFTIME;
		if (!sc.isOpen())
			return QTIConstants.ERROR_SUBMITTEDSECTION_OUTOFTIME;

		int sectionResult = QTIConstants.SECTION_SUBMITTED;
		for (Iterator<ItemInput> it_inp = curitsinp.getItemInputIterator(); it_inp.hasNext();) {
			ItemInput itemInput = it_inp.next();
			String ident = itemInput.getIdent();
			ItemContext ict = sc.getItemContext(ident);
			if (ict == null) throw new RuntimeException("submitted item id ("+ident+") not found in section sectioncontext "+sc.getIdent());
			int subres = ict.addItemInput(itemInput);
			ict.eval(); // to be up-to-date with the scores
			if (subres != QTIConstants.ITEM_SUBMITTED) {
				// item had a timelimit or maxattempts, which is nonsense if displaymode = sectionPage
				// throw new RuntimeException("section "+sc.getIdent()+" was submitted, but item "+ict.getIdent()+"  could not be submitted, because it had a timelimit or maxattempts, which is nonsense if displaymode = sectionPage");
				sectionResult = QTIConstants.ERROR_SECTION_PART_OUTOFTIME;
			}
		}
		return sectionResult;
	}
	

	/**
	 * @see org.olat.qti.process.Navigator#submitAssessment()
	 */
	public final void submitAssessment() {
		Output pendingOutput = null;
		boolean pendingFeedback = getInfo().isFeedback();
		boolean alreadyClosed = getAssessmentInstance().isClosed();
		if(pendingFeedback && getAssessmentInstance().getAssessmentContext().getCurrentSectionContext() != null) {
			ItemContext itc = getAssessmentInstance().getAssessmentContext().getCurrentSectionContext().getCurrentItemContext();
			pendingOutput = itc.getOutput();
		}

		getAssessmentInstance().stop();
		if(!getAssessmentInstance().isPreview() && !alreadyClosed) {
			CoreSpringFactory.getImpl(IQManager.class).persistResults(getAssessmentInstance());
		}
		
		AssessmentContext ac = getAssessmentContext();
		
		info.clear();
		if (ac.isFeedbackavailable()) {
			Output outp = ac.getOutput();
			getInfo().setCurrentOutput(outp);
			getInfo().setFeedback(true);
		} else if (pendingFeedback) {
			getInfo().setCurrentOutput(pendingOutput);
			getInfo().setFeedback(true);
		}
		//info.clear();
		info.setMessage(QTIConstants.MESSAGE_ASSESSMENT_SUBMITTED);
		info.setStatus(QTIConstants.ASSESSMENT_FINISHED);
		info.setRenderItems(false);
		
		if(delegate != null && !getAssessmentInstance().isPreview() && !alreadyClosed) {
			delegate.submitAssessment(assessmentInstance);
		}
		
		getAssessmentInstance().cleanUp();
	}

	public final void cancelAssessment() {
		getAssessmentInstance().stop();
		info.clear();
		info.setMessage(QTIConstants.MESSAGE_ASSESSMENT_CANCELED);
		info.setStatus(QTIConstants.ASSESSMENT_CANCELED);
		info.setRenderItems(false);
		
		if(delegate != null) {
			delegate.cancelAssessment(assessmentInstance);
		}
		getAssessmentInstance().cleanUp();
	}
	
	/**
	 * @return Info
	 */
	public Info getInfo() {
		return info;
	}
	
	public void clearInfo() {
		info.clear();
	}

}
