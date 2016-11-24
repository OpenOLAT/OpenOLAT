
package org.olat.ims.qti21.ui.editor.interactions;

import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createSimpleAssociableChoice;

import java.io.File;
import java.util.ArrayList;
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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.interactions.MatchAssessmentItemBuilder;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 21 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MatchEditorController extends FormBasicController {

	private static final String[] yesnoKeys = new String[]{ "y", "n"};
	private static final String[] singleMultiKeys = new String[]{ "single", "multi"};
	
	private TextElement titleEl;
	private RichTextElement textEl;
	private FormLayoutContainer answersCont;
	private FormLink addColumnButton, addRowButton;
	private SingleSelection shuffleEl, singleMultiEl;
	
	private int count = 0;
	private VFSContainer itemContainer;
	
	private final boolean restrictedEdit;
	private MatchAssessmentItemBuilder itemBuilder;
	private final List<MatchWrapper> sourceWrappers = new ArrayList<>();
	private final List<MatchWrapper> targetWrappers = new ArrayList<>();
	
	public MatchEditorController(UserRequest ureq, WindowControl wControl, MatchAssessmentItemBuilder itemBuilder,
			File rootDirectory, VFSContainer rootContainer, File itemFile, boolean restrictedEdit) {
		super(ureq, wControl, "simple_choices_editor");
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemBuilder = itemBuilder;
		this.restrictedEdit = restrictedEdit;
		
		String relativePath = rootDirectory.toPath().relativize(itemFile.toPath().getParent()).toString();
		itemContainer = (VFSContainer)rootContainer.resolve(relativePath);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer metadata = FormLayoutContainer.createDefaultFormLayout("metadata", getTranslator());
		metadata.setFormContextHelp("Test editor QTI 2.1 in detail#details_testeditor_fragetypen_kprim");
		metadata.setRootForm(mainForm);
		formLayout.add(metadata);
		formLayout.add("metadata", metadata);
		
		titleEl = uifactory.addTextElement("title", "form.imd.title", -1, itemBuilder.getTitle(), metadata);
		titleEl.setMandatory(true);

		String description = itemBuilder.getQuestion();
		textEl = uifactory.addRichTextElementForQTI21("desc", "form.imd.descr", description, 8, -1, itemContainer,
				metadata, ureq.getUserSession(), getWindowControl());
		
		//shuffle
		String[] yesnoValues = new String[]{ translate("yes"), translate("no") };
		shuffleEl = uifactory.addRadiosHorizontal("shuffle", "form.imd.shuffle", metadata, yesnoKeys, yesnoValues);
		shuffleEl.setEnabled(!restrictedEdit);
		if (itemBuilder.isShuffle()) {
			shuffleEl.select("y", true);
		} else {
			shuffleEl.select("n", true);
		}
		
		//single choice / multiple choice
		String[] singleMultiValues = new String[]{ translate("form.imd.match.single.choice"), translate("form.imd.match.multiple.choice") };
		singleMultiEl = uifactory.addRadiosHorizontal("singleMulti", "form.imd.match.single.multiple", metadata, singleMultiKeys, singleMultiValues);
		singleMultiEl.setEnabled(!restrictedEdit);
		singleMultiEl.addActionListener(FormEvent.ONCHANGE);
		if (itemBuilder.isMultipleChoice()) {
			singleMultiEl.select(singleMultiKeys[0], true);
		} else {
			singleMultiEl.select(singleMultiKeys[1], true);
		}

		//responses
		String page = velocity_root + "/match_choices.html";
		answersCont = FormLayoutContainer.createCustomFormLayout("answers", getTranslator(), page);
		answersCont.setRootForm(mainForm);
		formLayout.add(answersCont);
		formLayout.add("answers", answersCont);

		MatchInteraction interaction = itemBuilder.getMatchInteraction();
		if(interaction != null) {
			List<SimpleAssociableChoice> sourceChoices = itemBuilder.getSourceChoices();
			for(SimpleAssociableChoice sourceChoice:sourceChoices) {
				wrapAnswer(ureq, sourceChoice, sourceWrappers);
			}
			
			List<SimpleAssociableChoice> targetChoices = itemBuilder.getTargetChoices();
			for(SimpleAssociableChoice targetChoice:targetChoices) {
				wrapAnswer(ureq, targetChoice, targetWrappers);
			}
		}
		answersCont.contextPut("sourceChoices", sourceWrappers);
		answersCont.contextPut("targetChoices", targetWrappers);
		answersCont.contextPut("restrictedEdit", restrictedEdit);
		
		uifactory.addFormSubmitButton("submit", answersCont);
		if(!restrictedEdit) {
			addColumnButton = uifactory.addFormLink("add.match.column", answersCont, Link.BUTTON);
			addColumnButton.setIconLeftCSS("o_icon o_icon_add");
			addRowButton = uifactory.addFormLink("add.match.row", answersCont, Link.BUTTON);
			addRowButton.setIconLeftCSS("o_icon o_icon_add");
		}
	}
	
	private void wrapAnswer(UserRequest ureq, SimpleAssociableChoice choice, List<MatchWrapper> wrappers) {
		String choiceContent =  itemBuilder.getHtmlHelper().flowStaticString(choice.getFlowStatics());
		String choiceId = "answer" + count++;
		RichTextElement choiceEl = uifactory.addRichTextElementForQTI21Match(choiceId, "form.imd.answer", choiceContent, 4, -1, itemContainer,
				answersCont, ureq.getUserSession(), getWindowControl());
		choiceEl.setUserObject(choice);
		answersCont.add("choiceId", choiceEl);
		
		FormLink deleteButton = uifactory.addFormLink("del_" + (count++), "delete", "delete", null, answersCont, Link.NONTRANSLATED);
		deleteButton.setIconLeftCSS("o_icon o_icon_delete_item");
		deleteButton.setVisible(!restrictedEdit);
		deleteButton.setI18nKey("");
		
		MatchWrapper wrapper = new MatchWrapper(choice, choiceEl, deleteButton);
		deleteButton.setUserObject(wrapper);
		wrappers.add(wrapper);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		//clear errors
		for(MatchWrapper sourceWrapper:sourceWrappers) {
			sourceWrapper.setErrorSingleChoice(false);
		}
		
		if(singleMultiEl.isOneSelected() && singleMultiEl.isSelected(0)) {
			for(MatchWrapper sourceWrapper:sourceWrappers) {
				String name = sourceWrapper.getIdentifierString();
				String[] targetIds = ureq.getHttpReq().getParameterValues(name);
				if(targetIds == null || targetIds.length == 0 || targetIds.length > 1) {
					sourceWrapper.setErrorSingleChoice(true);
					allOk &= false;
				}
			}
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addColumnButton == source) {
			commitAssociations(ureq);
			doAddTargetColumn(ureq);
		} else if(addRowButton == source) {
			commitAssociations(ureq);
			doAddSourceRow(ureq);
		} else if(singleMultiEl == source) {
			//
		} else if(source instanceof FormLink) {
			FormLink button = (FormLink)source;
			if("delete".equals(button.getCmd())) {
				commitAssociations(ureq);
				MatchWrapper associationWrapper = (MatchWrapper)button.getUserObject();
				doDeleteAssociableChoice(associationWrapper);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//title
		itemBuilder.setTitle(titleEl.getValue());
		//question
		String questionText = textEl.getRawValue();
		itemBuilder.setQuestion(questionText);
		
		if(!restrictedEdit) {
			boolean singleChoice = singleMultiEl.isOneSelected() && singleMultiEl.isSelected(0);
			itemBuilder.setMultipleChoice(!singleChoice);
		}
		
		//shuffle
		if(!restrictedEdit) {
			itemBuilder.setShuffle(shuffleEl.isOneSelected() && shuffleEl.isSelected(0));
		}
		
		//update 
		for(MatchWrapper sourceWrapper:sourceWrappers) {
			SimpleAssociableChoice choice = sourceWrapper.getSimpleChoice();
			String answer = sourceWrapper.getText().getRawValue();
			itemBuilder.getHtmlHelper().appendHtml(choice, answer);
		}
		for(MatchWrapper sourceWrapper:targetWrappers) {
			SimpleAssociableChoice choice = sourceWrapper.getSimpleChoice();
			String answer = sourceWrapper.getText().getRawValue();
			itemBuilder.getHtmlHelper().appendHtml(choice, answer);
		}
		
		//correct answers
		commitAssociations(ureq);

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), QTI21QuestionType.match));
	}
	
	private void commitAssociations(UserRequest ureq) {
		itemBuilder.clearAssociations();
		for(MatchWrapper sourceWrapper:sourceWrappers) {
			String name = sourceWrapper.getIdentifierString();
			Identifier sourceChoiceId = Identifier.assumedLegal(name);
			String[] targetIds = ureq.getHttpReq().getParameterValues(name);
			if(targetIds != null && targetIds.length > 0) {
				for(String targetId:targetIds) {
					Identifier targetChoiceId = Identifier.assumedLegal(targetId);
					itemBuilder.addAssociation(sourceChoiceId, targetChoiceId);
				}
			}
		}
	}
	
	private void doAddTargetColumn(UserRequest ureq) {
		SimpleAssociableChoice newChoice = createSimpleAssociableChoice("Text", itemBuilder.getTargetMatchSet());
		itemBuilder.getTargetMatchSet().getSimpleAssociableChoices().add(newChoice);
		wrapAnswer(ureq, newChoice, targetWrappers);
		answersCont.setDirty(true);
	}
	
	private void doAddSourceRow(UserRequest ureq) {
		SimpleAssociableChoice newChoice = createSimpleAssociableChoice("Text", itemBuilder.getSourceMatchSet());
		itemBuilder.getSourceMatchSet().getSimpleAssociableChoices().add(newChoice);
		wrapAnswer(ureq, newChoice, sourceWrappers);
		answersCont.setDirty(true);
	}
	
	private void doDeleteAssociableChoice(MatchWrapper associationWrapper) {
		if(sourceWrappers.remove(associationWrapper)) {
			itemBuilder.removeSimpleAssociableChoice(associationWrapper.getSimpleChoice());
		} else if(targetWrappers.remove(associationWrapper)) {
			itemBuilder.removeSimpleAssociableChoice(associationWrapper.getSimpleChoice());
		}
		answersCont.setDirty(true);
	}

	public class MatchWrapper {
		
		private FormLink deleteButton;
		private RichTextElement choiceTextEl;
		private final Identifier choiceIdentifier;
		private final SimpleAssociableChoice choice;
		
		private boolean errorSingleChoice;
		
		public MatchWrapper(SimpleAssociableChoice choice, RichTextElement choiceTextEl, FormLink deleteButton) {
			this.choice = choice;
			this.choiceTextEl = choiceTextEl;
			this.choiceIdentifier = choice.getIdentifier();
			this.deleteButton = deleteButton;
		}
		
		public Identifier getIdentifier() {
			return choiceIdentifier;
		}
		
		public String getIdentifierString() {
			return choiceIdentifier.toString();
		}
		
		public SimpleAssociableChoice getSimpleChoice() {
			return choice;
		}
		
		public FormLink getDeleteButton() {
			return deleteButton;
		}
		
		public RichTextElement getText() {
			return choiceTextEl;
		}
		
		public boolean isCorrect(Identifier targetChoiceId) {
			return itemBuilder.isCorrect(choiceIdentifier, targetChoiceId);
		}

		public boolean isErrorSingleChoice() {
			return errorSingleChoice;
		}

		public void setErrorSingleChoice(boolean errorSingleChoice) {
			this.errorSingleChoice = errorSingleChoice;
		}
	}
}
