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

import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.selenium.page.core.MenuTreePageFragment;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 16 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21EditorPage {
	private final By elementsMenu = By.cssSelector("ul.o_sel_qti_elements");
	private final By changeMenu = By.cssSelector("ul.o_sel_qti_change_node");
	
	private final WebDriver browser;
	private final MenuTreePageFragment menuTree;
	
	public QTI21EditorPage(WebDriver browser) {
		this.browser = browser;
		menuTree = new MenuTreePageFragment(browser);
	}
	
	public QTI21EditorPage assertOnEditor() {
		By editorBy = By.className("o_assessment_test_editor_and_composer");
		OOGraphene.waitElement(editorBy, 5, browser);
		return this;
	}
	
	public QTI21CSVImportWizard importTable() {
		openElementsMenu();
		
		By importBy = By.xpath("//ul[contains(@class,'o_sel_qti_elements')]//a[contains(@onclick,'import.table')]");
		browser.findElement(importBy).click();
		OOGraphene.waitModalDialog(browser);
		return new QTI21CSVImportWizard(browser);
	}
	
	public QTI21EditorPage openElementsMenu() {
		By toolsMenuCaret = By.cssSelector("a.o_sel_qti_elements");
		browser.findElement(toolsMenuCaret).click();
		OOGraphene.waitElement(elementsMenu, browser);
		return this;
	}
	
	public QTI21SectionEditorPage selectSection() {
		By firstSectionBy = By.xpath("//div[contains(@class,'o_assessment_test_editor_menu')]//a[i[contains(@class,'o_mi_qtisection')]]");
		browser.findElement(firstSectionBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitElement(By.className("o_sel_assessment_section_options"), browser);
		return new QTI21SectionEditorPage(browser);
	}
	
	public QTI21EditorPage selectNode(String title) {
		menuTree.selectWithTitle(title);
		return this;
	}
	
	public QTI21EditorPage selectItem(String title) {
		By itemBy = By.xpath("//li/div/span[contains(@class,'o_tree_level_label_leaf')]/a[span[contains(text(),'" + title + "')]]");
		OOGraphene.waitElement(itemBy, browser);
		browser.findElement(itemBy).click();
		OOGraphene.waitBusyAndScrollTop(browser);
		return this;
	}
	
	public QTI21EditorPage deleteNode() {
		By deleteNodeBy = By.xpath("//div[@class='o_title_cmds']//a[contains(@onclick,'tools.change.delete')]");
		browser.findElement(deleteNodeBy).click();
		OOGraphene.waitModalDialog(browser);
		
		confirm();
		return this;
	}
	
	public QTI21SingleChoiceEditorPage addSingleChoice() {
		addQuestion(QTI21QuestionType.sc);
		return new QTI21SingleChoiceEditorPage(browser);
	}
	
	public QTI21MultipleChoiceEditorPage addMultipleChoice() {
		addQuestion(QTI21QuestionType.mc);
		return new QTI21MultipleChoiceEditorPage(browser);
	}
	
	public QTI21OrderEditorPage addOrder() {
		addQuestion(QTI21QuestionType.order);
		return new QTI21OrderEditorPage(browser);
	}
	
	public QTI21KprimEditorPage addKprim() {
		addQuestion(QTI21QuestionType.kprim);
		return new QTI21KprimEditorPage(browser);
	}
	
	public QTI21MatchEditorPage addMatch() {
		addQuestion(QTI21QuestionType.match);
		return new QTI21MatchEditorPage(browser);
	}
	
	public QTI21MatchEditorPage addMatchDragAndDrop() {
		addQuestion(QTI21QuestionType.matchdraganddrop);
		return new QTI21MatchEditorPage(browser);
	}
	
	public QTI21MatchEditorPage addMatchTrueFalse() {
		addQuestion(QTI21QuestionType.matchtruefalse);
		return new QTI21MatchEditorPage(browser);
	}
	
	public QTI21LobEditorPage addUpload() {
		addQuestion(QTI21QuestionType.upload);
		return new QTI21LobEditorPage(browser);
	}
	
	public QTI21LobEditorPage addEssay() {
		addQuestion(QTI21QuestionType.essay);
		return new QTI21LobEditorPage(browser);
	}
	
	public QTI21LobEditorPage addDrawing() {
		addQuestion(QTI21QuestionType.drawing);
		return new QTI21LobEditorPage(browser);
	}
	
	public QTI21HotspotEditorPage addHotspot() {
		addQuestion(QTI21QuestionType.hotspot);
		return new QTI21HotspotEditorPage(browser);
	}
	
	public QTI21GapEntriesEditorPage addFib() {
		addQuestion(QTI21QuestionType.fib);
		return new QTI21GapEntriesEditorPage(browser);
	}
	
	public QTI21GapEntriesEditorPage addNumerical() {
		addQuestion(QTI21QuestionType.numerical);
		return new QTI21GapEntriesEditorPage(browser);
	}
	
	public QTI21HottextEditorPage addHottext() {
		addQuestion(QTI21QuestionType.hottext);
		return new QTI21HottextEditorPage(browser);
	}
	
	private QTI21EditorPage addQuestion(QTI21QuestionType type) {
		openElementsMenu();
		
		By addBy = By.xpath("//ul[contains(@class,'o_sel_qti_elements')]//a[contains(@onclick,'new.')][i[contains(@class,'" + type.getCssClass() + "')]]");
		browser.findElement(addBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitElement(By.className("o_sel_assessment_item_title"), browser);
		return this;
	}
	
	public QTI21EditorPage openChangeMenu() {
		By changeMenuCaret = By.cssSelector("a.o_sel_qti_change_node");
		browser.findElement(changeMenuCaret).click();
		OOGraphene.waitElement(changeMenu, browser);
		return this;
	}
	
	public QTI21EditorPage confirm() {
		By yesBy = By.xpath("//div[contains(@class,'modal-dialog')]//a[contains(@onclick,'link_0')]");
		browser.findElement(yesBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
}
