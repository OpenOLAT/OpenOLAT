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

import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.container.AssessmentContext;
import org.olat.ims.qti.container.ItemContext;
import org.olat.ims.qti.container.ItemsInput;
import org.olat.ims.qti.container.Output;
import org.olat.ims.qti.container.SectionContext;
import org.olat.ims.qti.process.AssessmentInstance;

/**
 * Navigator used for the cases: <br>
 * 1. Navigation (via Menu) not visible, one question per page <br>
 * 2. Navigation visible but disabled, one question per page <br>
 * 
 * @author Felix Jost
 */
public class SequentialItemNavigator extends DefaultNavigator {

	/**
	 * @param assessmentInstance
	 */
	public SequentialItemNavigator(AssessmentInstance assessmentInstance, NavigatorDelegate delegate) {
		super(assessmentInstance, delegate);
	}

	/**
	 * starts the assessment: assuming we have at least one section with at least
	 * one item
	 */
	public void startAssessment() {
		AssessmentContext ac = getAssessmentContext();
		ac.setCurrentSectionContextPos(0);
		ac.getCurrentSectionContext().setCurrentItemContextPos(0);
		// start assessment, section, and item
		getAssessmentInstance().start();
		ac.getCurrentSectionContext().start();
		ac.getCurrentSectionContext().getCurrentItemContext().start();
		getInfo().setStatus(QTIConstants.ASSESSMENT_RUNNING);
		if (!ac.isOpen()) {
			getInfo().setError(QTIConstants.ERROR_ASSESSMENT_OUTOFTIME);
			getInfo().setRenderItems(false);
		} else {			
			getInfo().setMessage(QTIConstants.MESSAGE_ASSESSMENT_INFODEMANDED); //show test title and description first
			getInfo().setRenderItems(false); // do not show items as first step						
		}
		getAssessmentInstance().persist();
	}

	/**
	 * @see org.olat.ims.qti.navigator.Navigator#submitItems(org.olat.ims.qti.container.ItemsInput)
	 */
	public void submitItems(final ItemsInput curitsinp) {
		clearInfo();
		final AssessmentContext ac = getAssessmentContext();
		final SectionContext sc = ac.getCurrentSectionContext();
		final int st = submitOneItem(curitsinp);
		boolean gotoNextItem = true;
		if (st != QTIConstants.ITEM_SUBMITTED) {
			// time expired or too many attempts-> display a message above the next
			// item or assessment-finished-text
			if (st == QTIConstants.ERROR_SUBMITTEDITEM_TOOMANYATTEMPTS) {
				throw new RuntimeException("import check failed: there was a maxattempts in a item, but mode is sequential/item");
			} else if (st == QTIConstants.ERROR_ASSESSMENT_OUTOFTIME) {
				getInfo().setError(st);
				getInfo().setRenderItems(false);
				gotoNextItem = false;
			} else if (st == QTIConstants.ERROR_SUBMITTEDITEM_OUTOFTIME) {
				getInfo().setError(st);
				getInfo().setRenderItems(true); // still continue to next item
			}
		} else { // submitting of item was successful
			final ItemContext itc = sc.getCurrentItemContext();
			final Output outp = itc.getOutput();
			if (outp != null) getInfo().setCurrentOutput(outp);
			getInfo().setMessage(QTIConstants.MESSAGE_ITEM_SUBMITTED);
			getInfo().setRenderItems(true);
			// display feedback for CURRENT item so don't proceed to next item
			if (itc.isFeedback()) {
				getInfo().setFeedback(itc.getOutput().hasItem_Responses());
				gotoNextItem = false;
			}
		}

		// find next item
		if (gotoNextItem) {
			goToNextItem();
		}
		getAssessmentInstance().persist();
	}

	public void goToNextItem() {
		// see comment for method call in class IQDisplayController
		if (!hasNextItem()) {
			submitAssessment();
			// we want to see feedback for last item as well!
			getInfo().setRenderItems(true);
			return;
		}

		clearInfo();
		getInfo().setRenderItems(true);
		getInfo().setMessage(QTIConstants.MESSAGE_NONE);

		final AssessmentContext ac = getAssessmentContext();
		final SectionContext sc = ac.getCurrentSectionContext();
		int itpos = sc.getCurrentItemContextPos();
		if (itpos < sc.getItemContextCount() - 1 && sc.isOpen()) {
			//there are still further items in the current section
			sc.setCurrentItemContextPos(++itpos);
			sc.getCurrentItemContext().start();
		} else { // fetch next section			
			if (!sc.isOpen()) {
				getInfo().setError(QTIConstants.ERROR_SECTION_OUTOFTIME);
				getInfo().setRenderItems(true);
			}
			ac.getCurrentSectionContext().sectionWasSubmitted();

			int secPos = ac.getCurrentSectionContextPos();
			int secPosMax = ac.getSectionContextCount() - 1;

			if (!ac.isOpen()) {
				getInfo().setError(QTIConstants.ERROR_ASSESSMENT_OUTOFTIME);
				getInfo().setRenderItems(false);
				submitAssessment();
			} else if (secPos == secPosMax) {
				submitAssessment();
			} else {
				while (secPos < secPosMax) { // there are still further section(s)
					secPos++;
					if (ac.getSectionContext(secPos).getItemContextCount() != 0) break;
				}

				if (secPos == secPosMax && ac.getSectionContext(secPos).getItemContextCount() == 0) {
					// reached last section but section is empty -> finish assessment
					submitAssessment();
				} else {
					// new section starts
					ac.setCurrentSectionContextPos(secPos);
					ac.getCurrentSectionContext().setCurrentItemContextPos(0);
					ac.getCurrentSectionContext().start();
					ac.getCurrentSectionContext().getCurrentItemContext().start();

					getInfo().setMessage(QTIConstants.MESSAGE_SECTION_INFODEMANDED);
					getInfo().setRenderItems(false);//since new section starts, show next the section title and description
				}
			}
		}
	}

	private boolean hasNextItem() {
		final AssessmentContext ac = getAssessmentContext();
		if (!ac.isOpen()) {
			return false;
		}

		// another item in current section?
		final SectionContext sc = ac.getCurrentSectionContext();
		int itpos = sc.getCurrentItemContextPos();
		if (itpos < sc.getItemContextCount() - 1 && sc.isOpen()) {
			return true;
		}

		// another section with items?
		int secPos = ac.getCurrentSectionContextPos();
		int secPosMax = ac.getSectionContextCount() - 1;
		while (secPos < secPosMax) { // there are still further section(s)
			secPos++;
			if (ac.getSectionContext(secPos).getItemContextCount() > 0 && ac.getSectionContext(secPos).isOpen()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @see org.olat.ims.qti.navigator.Navigator#goToItem(int, int)
	 */
	public void goToItem(int sectionPos, int itemPos) {			
		AssessmentContext ac = getAssessmentContext();
		ac.setCurrentSectionContextPos(sectionPos);
		SectionContext sc = ac.getCurrentSectionContext();
		sc.setCurrentItemContextPos(itemPos);
		getInfo().setRenderItems(true);
		getInfo().setMessage(QTIConstants.MESSAGE_NONE);
	}

	/**
	 * @see org.olat.ims.qti.navigator.Navigator#goToSection(int)
	 */
	public void goToSection(int sectionPos) {		
		AssessmentContext ac = getAssessmentContext();
		ac.setCurrentSectionContextPos(sectionPos);
		getInfo().setMessage(QTIConstants.MESSAGE_SECTION_INFODEMANDED);
	}

}