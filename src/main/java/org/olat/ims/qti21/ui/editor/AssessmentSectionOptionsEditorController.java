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
package org.olat.ims.qti21.ui.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.model.xml.AssessmentHtmlBuilder;
import org.olat.ims.qti21.model.xml.QtiMaxScoreEstimator;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.editor.events.AssessmentSectionEvent;
import org.olat.ims.qti21.ui.editor.events.OpenTestConfigurationOverviewEvent;

import uk.ac.ed.ph.jqtiplus.node.content.variable.RubricBlock;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.Ordering;
import uk.ac.ed.ph.jqtiplus.node.test.Selection;
import uk.ac.ed.ph.jqtiplus.node.test.View;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Initial date: 23 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentSectionOptionsEditorController extends FormBasicController {

	private TextElement titleEl;
	private SingleSelection shuffleEl;
	private SingleSelection randomSelectedEl;
	private FormLink openTestConfigurationOverviewLink;
	private FormLayoutContainer warningRandomSelectedCont;
	private List<RichTextElement> rubricEls = new ArrayList<>();
	
	private final File testFile;
	private final File rootDirectory;
	private final VFSContainer rootContainer;
	
	private final AssessmentSection section;
	private final AssessmentHtmlBuilder htmlBuilder;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	
	private int counter = 0;
	private final boolean editable;
	private final boolean restrictedEdit;
	private static final String[] yesnoKeys = new String[]{ "y", "n"};
	
	public AssessmentSectionOptionsEditorController(UserRequest ureq, WindowControl wControl,
			AssessmentSection section, ResolvedAssessmentTest resolvedAssessmentTest,
			File rootDirectory, VFSContainer rootContainer, File testFile,
			boolean restrictedEdit, boolean editable) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentTestDisplayController.class, ureq.getLocale()));
		this.section = section;
		this.editable = editable;
		this.testFile = testFile;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		this.restrictedEdit = restrictedEdit;
		this.resolvedAssessmentTest = resolvedAssessmentTest;
		htmlBuilder = new AssessmentHtmlBuilder();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_assessment_section_options");
		setFormContextHelp("Configure tests#testeditor_section");
		if(!editable) {
			setFormWarning("warning.alien.assessment.test");
		}
		
		String title = section.getTitle();
		titleEl = uifactory.addTextElement("title", "form.metadata.title", 255, title, formLayout);
		titleEl.setEnabled(editable);
		titleEl.setMandatory(true);

		String relativePath = rootDirectory.toPath().relativize(testFile.toPath().getParent()).toString();
		VFSContainer itemContainer = (VFSContainer)rootContainer.resolve(relativePath);
		if(section.getRubricBlocks().isEmpty()) {
			RichTextElement rubricEl = uifactory.addRichTextElementForQTI21("rubric" + counter++, "form.imd.rubric", "", 12, -1, itemContainer,
					formLayout, ureq.getUserSession(), getWindowControl());
			rubricEl.getEditorConfiguration().setFileBrowserUploadRelPath("media");
			rubricEl.setEnabled(editable);
			rubricEls.add(rubricEl);
		} else {
			for(RubricBlock rubricBlock:section.getRubricBlocks()) {
				String rubric = htmlBuilder.blocksString(rubricBlock.getBlocks());
				RichTextElement rubricEl = uifactory.addRichTextElementForQTI21("rubric" + counter++, "form.imd.rubric", rubric, 12, -1, itemContainer,
						formLayout, ureq.getUserSession(), getWindowControl());
				rubricEl.getEditorConfiguration().setFileBrowserUploadRelPath("media");
				rubricEl.setEnabled(editable);
				rubricEl.setUserObject(rubricBlock);
				rubricEls.add(rubricEl);
			}
		}
		
		
		//shuffle
		String[] yesnoValues = new String[]{ translate("yes"), translate("no") };
		shuffleEl = uifactory.addRadiosHorizontal("shuffle", "form.section.shuffle", formLayout, yesnoKeys, yesnoValues);
		shuffleEl.setHelpTextKey("form.section.position.hint", null);
		if (section.getOrdering() != null && section.getOrdering().getShuffle()) {
			shuffleEl.select("y", true);
		} else {
			shuffleEl.select("n", true);
		}
		shuffleEl.setEnabled(!restrictedEdit && editable);

		
		int numOfItems = getNumOfQuestions(section);
		String[] theKeys = new String[numOfItems + 1];
		String[] theValues = new String[numOfItems + 1];
		theKeys[0] = "0";
		theValues[0] = translate("form.section.selection_all");
		for(int i=0; i<numOfItems; i++) {
			theKeys[i+1] = Integer.toString(i+1);
			theValues[i+1] = Integer.toString(i+1);
		}
		
		randomSelectedEl = uifactory.addDropdownSingleselect("form.section.selection_pre", formLayout, theKeys, theValues, null);
		randomSelectedEl.addActionListener(FormEvent.ONCHANGE);
		randomSelectedEl.setHelpText(translate("form.section.selection_pre.hover"));
		randomSelectedEl.setEnabled(!restrictedEdit && editable);
		
		int currentNum = section.getSelection() != null ? section.getSelection().getSelect() : 0;
		if(currentNum <= numOfItems) {
			randomSelectedEl.select(theKeys[currentNum], true);
		} else if(currentNum > numOfItems) {
			randomSelectedEl.select(theKeys[numOfItems], true);
		} else {
			randomSelectedEl.select(theKeys[0], true);
		}
		
		String warningVC = velocity_root + "/warning_section.html";
		warningRandomSelectedCont = FormLayoutContainer.createCustomFormLayout("warn.section.selection_pre", getTranslator(), warningVC);
		formLayout.add(warningRandomSelectedCont);
		openTestConfigurationOverviewLink = uifactory.addFormLink("section.open.test.configuration", warningRandomSelectedCont, Link.LINK);
		updateSameMaxScoreWarning();
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("butons", getTranslator());
		formLayout.add(buttonsCont);
		FormSubmit submit = uifactory.addFormSubmitButton("save", "save", buttonsCont);
		submit.setEnabled(editable);
	}
	
	private void updateSameMaxScoreWarning() {
		String val = randomSelectedEl.getSelectedKey();
		if(!"0".equals(val)) {
			boolean sameMaxScore = QtiMaxScoreEstimator.sameMaxScore(section, resolvedAssessmentTest);
			warningRandomSelectedCont.setVisible(!sameMaxScore);
		} else {
			warningRandomSelectedCont.setVisible(false);
		}
	}
	
	/**
	 * The sub-sections count for 1, always. 
	 * @param assessmentSection
	 * @return
	 */
	private int getNumOfQuestions(AssessmentSection assessmentSection) {
		return assessmentSection.getSectionParts().size();
	}
	
	public String getTitle() {
		return titleEl.getValue();
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		randomSelectedEl.clearError();
		if(!randomSelectedEl.isOneSelected()) {
			randomSelectedEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(randomSelectedEl == source) {
			updateSameMaxScoreWarning();
		} else if(openTestConfigurationOverviewLink == source) {
			fireEvent(ureq, new OpenTestConfigurationOverviewEvent());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		section.setTitle(titleEl.getValue());
		
		//rubrics
		List<RubricBlock> rubricBlocks = new ArrayList<>();
		for(RichTextElement rubricEl:rubricEls) {
			String rubric = rubricEl.getRawValue();
			if(htmlBuilder.containsSomething(rubric)) {
				RubricBlock rubricBlock = (RubricBlock)rubricEl.getUserObject();
				if(rubricBlock == null) {
					rubricBlock = new RubricBlock(section);
					rubricBlock.setViews(Collections.singletonList(View.CANDIDATE));
				}
				rubricBlock.getBlocks().clear();
				htmlBuilder.appendHtml(rubricBlock, rubric);
				rubricBlocks.add(rubricBlock);
			}
		}
		section.getRubricBlocks().clear();
		section.getRubricBlocks().addAll(rubricBlocks);
		
		//shuffle
		boolean shuffle = (shuffleEl.isOneSelected() && shuffleEl.isSelected(0));
		if(shuffle) {
			if(section.getOrdering() == null) {
				section.setOrdering(new Ordering(section));
			}
			section.getOrdering().setShuffle(shuffle);
		} else {
			section.setOrdering(null);
		}
		
		//number of selected questions
		Integer randomSelection = null;
		if(StringHelper.containsNonWhitespace(randomSelectedEl.getSelectedKey())) {
			randomSelection = Integer.valueOf(randomSelectedEl.getSelectedKey());
		}
		if(randomSelection == null || randomSelection.intValue() < 1) {
			section.setSelection(null);
		} else {
			if(section.getSelection() == null) {
				section.setSelection(new Selection(section));
			}
			section.getSelection().setSelect(randomSelection);
		}

		fireEvent(ureq, new AssessmentSectionEvent(AssessmentSectionEvent.ASSESSMENT_SECTION_CHANGED, section));
	}
}
