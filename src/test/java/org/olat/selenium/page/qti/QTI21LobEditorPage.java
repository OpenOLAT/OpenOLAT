/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.qti;

import java.io.File;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Drive the upload, drawing editor.
 * 
 * Initial date: 12 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21LobEditorPage extends QTI21AssessmentItemEditorPage {
	
	public QTI21LobEditorPage(WebDriver browser) {
		super(browser);
	}
	
	public QTI21LobEditorPage setQuestion(String question) {
		OOGraphene.tinymce(question, "div.o_sel_assessment_item_question", browser);
		return this;
	}
	
	public QTI21LobEditorPage updloadDrawingBackground(File file) {
		By inputBy = By.cssSelector(".o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, file, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21LobEditorPage save() {
		By saveBy = By.cssSelector("div.o_sel_lob_save button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21MinimalScoreEditorPage selectScores() {
		selectTab("o_sel_assessment_item_score", By.className("o_sel_assessment_item_options"));
		return new QTI21MinimalScoreEditorPage(browser);
	}
	
	public QTI21FeedbacksEditorPage selectFeedbacks() {
		selectTab("o_sel_assessment_item_feedback", By.className("o_sel_assessment_item_feedbacks"));
		return new QTI21FeedbacksEditorPage(browser);
	}
}
