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
package org.olat.modules.ceditor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.miniRandom;
import static org.olat.test.JunitTestHelper.random;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.olat.core.util.FileUtils;
import org.olat.modules.ceditor.model.ContainerColumn;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.forms.handler.FormDataElementStorage;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.Choices;
import org.olat.modules.forms.model.xml.Container;
import org.olat.modules.forms.model.xml.Disclaimer;
import org.olat.modules.forms.model.xml.FileStoredData;
import org.olat.modules.forms.model.xml.FileUpload;
import org.olat.modules.forms.model.xml.HTMLParagraph;
import org.olat.modules.forms.model.xml.Image;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.NameDisplay;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.ScaleType;
import org.olat.modules.forms.model.xml.SessionInformations;
import org.olat.modules.forms.model.xml.SessionInformations.InformationType;
import org.olat.modules.forms.model.xml.SessionInformations.Obligation;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.Spacer;
import org.olat.modules.forms.model.xml.StepLabel;
import org.olat.modules.forms.model.xml.Table;
import org.olat.modules.forms.model.xml.TextInput;
import org.olat.modules.forms.model.xml.Title;
import org.olat.test.JunitTestHelper;

/**
 * 
 * Initial date: 2 May 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CloneElementHandlerTest {
	
	@Test
	public void shouldCloneEvaluationFormContainer() {
		Container element = new Container();
		element.setId(random());
		String name = random();
		ContainerSettings containerSettings = element.getContainerSettings();
		containerSettings.setName(name);
		int numOfColumns = 5;
		containerSettings.setNumOfColumns(numOfColumns);
		ContainerColumn column = containerSettings.getColumn(1);
		column.getElementIds().add(random());
		String settingsXml = ContentEditorXStream.toXml(containerSettings);
		element.setLayoutOptions(settingsXml);
		
		CloneElementHandler handler = new org.olat.modules.forms.handler.ContainerHandler(null);
		Container clone = (Container)handler.clonePageElement(element);
		
		assertThat(clone.getId()).isNotEqualTo(element.getId());
		assertThat(clone.getType()).isEqualTo(element.getType());
		assertThat(clone.getContainerSettings().getName()).isEqualTo(name);
		assertThat(clone.getContainerSettings().getNumOfColumns()).isEqualTo(numOfColumns);
		assertThat(clone.getContainerSettings().getColumn(1).getElementIds()).isEmpty();
	}
	
	@Test
	public void shouldCloneEvaluationFormDisclaimer() {
		Disclaimer element = new Disclaimer();
		element.setId(random());
		String agreement = random();
		element.setAgreement(agreement);
		String text = random();
		element.setText(text);
		
		CloneElementHandler handler = new org.olat.modules.forms.handler.DisclaimerHandler(false);
		Disclaimer clone = (Disclaimer)handler.clonePageElement(element);
		
		assertThat(clone.getId()).isNotEqualTo(element.getId());
		assertThat(clone.getType()).isEqualTo(element.getType());
		assertThat(clone.getAgreement()).isEqualTo(element.getAgreement());
		assertThat(clone.getText()).isEqualTo(element.getText());
	}
	
	@Test
	public void shouldCloneEvaluationFormFileUpload() {
		FileUpload element = new FileUpload();
		element.setId(random());
		element.setMandatory(false);
		long maxUploadSizeKB = 400l;
		element.setMaxUploadSizeKB(maxUploadSizeKB);
		String mimeTypeSetKey = random();
		element.setMimeTypeSetKey(mimeTypeSetKey);
		
		CloneElementHandler handler = new org.olat.modules.forms.handler.FileUploadHandler(false);
		FileUpload clone = (FileUpload)handler.clonePageElement(element);
		
		assertThat(clone.getId()).isNotEqualTo(element.getId());
		assertThat(clone.getType()).isEqualTo(element.getType());
		assertThat(clone.isMandatory()).isFalse();
		assertThat(clone.getMaxUploadSizeKB()).isEqualTo(maxUploadSizeKB);
		assertThat(clone.getMimeTypeSetKey()).isEqualTo(mimeTypeSetKey);
	}
	
	@Test
	public void shouldCloneEvaluationFormHtmlParagraph() {
		HTMLParagraph element = new HTMLParagraph();
		element.setId(random());
		String content = random();
		element.setContent(content);
		String layoutOptions = random();
		element.setLayoutOptions(layoutOptions);
		
		CloneElementHandler handler = new org.olat.modules.forms.handler.HTMLParagraphHandler();
		HTMLParagraph clone = (HTMLParagraph)handler.clonePageElement(element);
		
		assertThat(clone.getId()).isNotEqualTo(element.getId());
		assertThat(clone.getType()).isEqualTo(element.getType());
		assertThat(clone.getContent()).isEqualTo(content);
		assertThat(clone.getLayoutOptions()).isEqualTo(layoutOptions);
	}
	
	@Test
	public void shouldCloneEvaluationFormImageHandler() throws IOException, URISyntaxException {
		File tempDir = FileUtils.createTempDir("cht", null, null);
		DataStorage dataStorage = new FormDataElementStorage(tempDir);
		
		Image element = new Image();
		element.setId(UUID.randomUUID().toString());
		String content = random();
		element.setContent(content);
		FileStoredData storedData = new FileStoredData();
		element.setStoredData(storedData);
		File elementFile = new File(JunitTestHelper.class.getResource("file_resources/IMG_1482.JPG").toURI());
		String elementFilename = miniRandom() + ".jpg";
		dataStorage.save(elementFilename, elementFile, element.getStoredData());
		
		CloneElementHandler handler = new org.olat.modules.forms.handler.ImageHandler(dataStorage);
		Image clone = (Image)handler.clonePageElement(element);
		
		assertThat(clone.getId()).isNotEqualTo(element.getId());
		assertThat(clone.getType()).isEqualTo(element.getType());
		assertThat(clone.getContent()).isEqualTo(content);
		assertThat(clone.getStoredData().getRootFilename()).isNotEqualTo(elementFilename);
		File clonedFile = dataStorage.getFile(clone.getStoredData());
		assertThat(clonedFile).exists();
	}
	
	@Test
	public void shouldCloneEvaluationFormMultipleChoice() {
		MultipleChoice element = new MultipleChoice();
		element.setId(random());
		element.setMandatory(false);
		String name = random();
		element.setName(name);
		MultipleChoice.Presentation presentation = MultipleChoice.Presentation.VERTICAL;
		element.setPresentation(presentation);
		element.setWithOthers(true);
		Choices choices = new Choices();
		element.setChoices(choices);
		Choice choice1 = new Choice();
		choice1.setId(random());
		String value1 = random();
		choice1.setValue(value1);
		choices.addNotPresent(choice1);
		Choice choice2 = new Choice();
		choice2.setId(random());
		String value2 = random();
		choice2.setValue(value2);
		choices.addNotPresent(choice2);
		
		CloneElementHandler handler = new org.olat.modules.forms.handler.MultipleChoiceHandler(false);
		MultipleChoice clone = (MultipleChoice)handler.clonePageElement(element);
		
		assertThat(clone.getId()).isNotEqualTo(element.getId());
		assertThat(clone.getType()).isEqualTo(element.getType());
		assertThat(clone.isMandatory()).isFalse();
		assertThat(clone.getName()).isEqualTo(name);
		assertThat(clone.getPresentation()).isEqualTo(presentation);
		assertThat(clone.isWithOthers()).isTrue();
		List<Choice> clonedChoices = clone.getChoices().asList();
		Choice clonedChoice1 = clonedChoices.get(0);
		assertThat(clonedChoice1.getId()).isNotEqualTo(choice1.getId());
		assertThat(clonedChoice1.getValue()).isEqualTo(choice1.getValue());
		Choice clonedChoice2 = clonedChoices.get(1);
		assertThat(clonedChoice2.getId()).isNotEqualTo(choice2.getId());
		assertThat(clonedChoice2.getValue()).isEqualTo(choice2.getValue());
	}
	
	@Test
	public void shouldCloneEvaluationFormRubric() {
		Rubric element = new Rubric();
		element.setId(random());
		int end = 8;
		element.setEnd(end);
		Double lowerBoundInsufficient = Double.valueOf(100);
		element.setLowerBoundInsufficient(lowerBoundInsufficient);
		Double lowerBoundNeutral = Double.valueOf(99);
		element.setLowerBoundNeutral(lowerBoundNeutral);
		Double lowerBoundSufficient = Double.valueOf(98);
		element.setLowerBoundSufficient(lowerBoundSufficient);
		element.setMandatory(true);
		String name = random();
		element.setName(name);
		List<NameDisplay> nameDisplays = List.of(NameDisplay.execution);
		element.setNameDisplays(nameDisplays);
		element.setNoResponseEnabled(true);
		ScaleType scaleType = ScaleType.maxToZero;
		element.setScaleType(scaleType);
		SliderType sliderType = SliderType.continuous;
		element.setSliderType(sliderType);
		int start = 1;
		element.setStart(start);
		element.setStartGoodRating(true);
		int steps = 5;
		element.setSteps(steps);
		Double upperBoundInsufficient = Double.valueOf(19);
		element.setUpperBoundInsufficient(upperBoundInsufficient);
		Double upperBoundNeutral = Double.valueOf(20);
		element.setUpperBoundNeutral(upperBoundNeutral);
		Double upperBoundSufficient = Double.valueOf(21);
		element.setUpperBoundSufficient(upperBoundSufficient);
		StepLabel stepLabel1 = new StepLabel();
		stepLabel1.setId(random());
		String label1 = random();
		stepLabel1.setLabel(label1);
		StepLabel stepLabel2 = new StepLabel();
		stepLabel2.setId(random());
		String label2 = random();
		stepLabel2.setLabel(label2);
		element.setStepLabels(List.of(stepLabel1, stepLabel2));
		Slider slider1 = new Slider();
		slider1.setId(random());
		String endLabel1 = random();
		slider1.setEndLabel(endLabel1);
		String startLabel1 = random();
		slider1.setStartLabel(startLabel1);
		Integer weight1 = Integer.valueOf(3);
		slider1.setWeight(weight1);
		Slider slider2 = new Slider();
		slider2.setId(random());
		String endLabel2 = random();
		slider2.setEndLabel(endLabel2);
		String startLabel2 = random();
		slider2.setStartLabel(startLabel2);
		Integer weight2 = Integer.valueOf(5);
		slider2.setWeight(weight2);
		element.setSliders(List.of(slider1, slider2));
		
		CloneElementHandler handler = new org.olat.modules.forms.handler.RubricHandler(false, false);
		Rubric clone = (Rubric)handler.clonePageElement(element);

		assertThat(clone.getId()).isNotEqualTo(element.getId());
		assertThat(clone.getType()).isEqualTo(element.getType());
		assertThat(clone.isMandatory()).isTrue();
		assertThat(clone.getEnd()).isEqualTo(end);
		assertThat(clone.getLowerBoundInsufficient()).isEqualTo(lowerBoundInsufficient);
		assertThat(clone.getLowerBoundNeutral()).isEqualTo(lowerBoundNeutral);
		assertThat(clone.getLowerBoundSufficient()).isEqualTo(lowerBoundSufficient);
		assertThat(clone.getName()).isEqualTo(name);
		assertThat(clone.getNameDisplays()).containsAll(nameDisplays);
		assertThat(clone.isNoResponseEnabled()).isTrue();
		assertThat(clone.getScaleType()).isEqualTo(scaleType);
		assertThat(clone.getSliderType()).isEqualTo(sliderType);
		assertThat(clone.getStart()).isEqualTo(start);
		assertThat(clone.isStartGoodRating()).isTrue();
		assertThat(clone.getSteps()).isEqualTo(steps);
		assertThat(clone.getUpperBoundInsufficient()).isEqualTo(upperBoundInsufficient);
		assertThat(clone.getUpperBoundNeutral()).isEqualTo(upperBoundNeutral);
		assertThat(clone.getUpperBoundSufficient()).isEqualTo(upperBoundSufficient);
		assertThat(clone.getStepLabels().get(0).getId()).isNotEqualTo(element.getStepLabels().get(0).getId());
		assertThat(clone.getStepLabels().get(0).getLabel()).isEqualTo(label1);
		assertThat(clone.getStepLabels().get(1).getId()).isNotEqualTo(element.getStepLabels().get(1).getId());
		assertThat(clone.getStepLabels().get(1).getLabel()).isEqualTo(label2);
		assertThat(clone.getSliders().get(0).getId()).isNotEqualTo(element.getSliders().get(0).getId());
		assertThat(clone.getSliders().get(0).getEndLabel()).isEqualTo(endLabel1);
		assertThat(clone.getSliders().get(0).getStartLabel()).isEqualTo(startLabel1);
		assertThat(clone.getSliders().get(0).getWeight()).isEqualTo(weight1);
		assertThat(clone.getSliders().get(1).getId()).isNotEqualTo(element.getSliders().get(1).getId());
		assertThat(clone.getSliders().get(1).getEndLabel()).isEqualTo(endLabel2);
		assertThat(clone.getSliders().get(1).getStartLabel()).isEqualTo(startLabel2);
		assertThat(clone.getSliders().get(1).getWeight()).isEqualTo(weight2);
	}
	
	@Test
	public void shouldCloneEvaluationFormSessionInformations() {
		SessionInformations element = new SessionInformations();
		element.setId(random());
		List<InformationType> informationTypes = List.of(InformationType.AGE, InformationType.USER_GENDER,
				InformationType.USER_FIRSTNAME);
		element.setInformationTypes(informationTypes);
		Obligation obligation = Obligation.autofill;
		element.setObligation(obligation);
		
		CloneElementHandler handler = new org.olat.modules.forms.handler.SessionInformationsHandler(false);
		SessionInformations clone = (SessionInformations)handler.clonePageElement(element);
		
		assertThat(clone.getId()).isNotEqualTo(element.getId());
		assertThat(clone.getType()).isEqualTo(element.getType());
		assertThat(clone.getInformationTypes()).containsExactlyElementsOf(informationTypes);
		assertThat(clone.getObligation()).isEqualTo(obligation);
	}
	
	@Test
	public void shouldCloneEvaluationFormSingleChoice() {
		SingleChoice element = new SingleChoice();
		element.setId(random());
		element.setMandatory(false);
		String name = random();
		element.setName(name);
		SingleChoice.Presentation presentation = SingleChoice.Presentation.VERTICAL;
		element.setPresentation(presentation);
		Choices choices = new Choices();
		element.setChoices(choices);
		Choice choice1 = new Choice();
		choice1.setId(random());
		String value1 = random();
		choice1.setValue(value1);
		choices.addNotPresent(choice1);
		Choice choice2 = new Choice();
		choice2.setId(random());
		String value2 = random();
		choice2.setValue(value2);
		choices.addNotPresent(choice2);
		
		CloneElementHandler handler = new org.olat.modules.forms.handler.SingleChoiceHandler(false);
		SingleChoice clone = (SingleChoice)handler.clonePageElement(element);
		
		assertThat(clone.getId()).isNotEqualTo(element.getId());
		assertThat(clone.getType()).isEqualTo(element.getType());
		assertThat(clone.isMandatory()).isFalse();
		assertThat(clone.getName()).isEqualTo(name);
		assertThat(clone.getPresentation()).isEqualTo(presentation);
		List<Choice> clonedChoices = clone.getChoices().asList();
		Choice clonedChoice1 = clonedChoices.get(0);
		assertThat(clonedChoice1.getId()).isNotEqualTo(choice1.getId());
		assertThat(clonedChoice1.getValue()).isEqualTo(choice1.getValue());
		Choice clonedChoice2 = clonedChoices.get(1);
		assertThat(clonedChoice2.getId()).isNotEqualTo(choice2.getId());
		assertThat(clonedChoice2.getValue()).isEqualTo(choice2.getValue());
	}

	@Test
	public void shouldCloneEvaluationFormSpacer() {
		Spacer element = new Spacer();
		element.setId(random());
		String content = random();
		element.setContent(content);
		
		CloneElementHandler handler = new org.olat.modules.forms.handler.SpacerHandler();
		Spacer clone = (Spacer)handler.clonePageElement(element);
		
		assertThat(clone.getId()).isNotEqualTo(element.getId());
		assertThat(clone.getType()).isEqualTo(element.getType());
		assertThat(clone.getContent()).isEqualTo(content);
	}

	@Test
	public void shouldCloneEvaluationFormTable() {
		Table element = new Table();
		element.setId(random());
		String content = random();
		element.setContent(content);
		String layoutOptions = random();
		element.setLayoutOptions(layoutOptions);
		
		CloneElementHandler handler = new org.olat.modules.forms.handler.TableHandler();
		Table clone = (Table)handler.clonePageElement(element);
		
		assertThat(clone.getId()).isNotEqualTo(element.getId());
		assertThat(clone.getType()).isEqualTo(element.getType());
		assertThat(clone.getContent()).isEqualTo(content);
		assertThat(clone.getLayoutOptions()).isEqualTo(layoutOptions);
	}

	@Test
	public void shouldCloneEvaluationFormTextInput() {
		TextInput element = new TextInput();
		element.setId(random());
		element.setDate(true);
		element.setMandatory(false);
		element.setNumeric(true);
		Double numericMax = Double.valueOf(8);
		element.setNumericMax(numericMax);
		Double numericMin = Double.valueOf(3);
		element.setNumericMin(numericMin);
		int rows = 5;
		element.setRows(rows);
		element.setSingleRow(false);
		
		CloneElementHandler handler = new org.olat.modules.forms.handler.TextInputHandler(false);
		TextInput clone = (TextInput)handler.clonePageElement(element);
		
		assertThat(clone.getId()).isNotEqualTo(element.getId());
		assertThat(clone.getType()).isEqualTo(element.getType());
		assertThat(clone.isDate()).isTrue();
		assertThat(clone.isMandatory()).isFalse();
		assertThat(clone.isNumeric()).isTrue();
		assertThat(clone.getNumericMax()).isEqualTo(numericMax);
		assertThat(clone.getNumericMin()).isEqualTo(numericMin);
		assertThat(clone.getRows()).isEqualTo(rows);
		assertThat(clone.isSingleRow()).isFalse();
	}

	@Test
	public void shouldCloneEvaluationFormTitle() {
		Title element = new Title();
		element.setId(random());
		String content = random();
		element.setContent(content);
		
		CloneElementHandler handler = new org.olat.modules.forms.handler.TitleHandler();
		Title clone = (Title)handler.clonePageElement(element);
		
		assertThat(clone.getId()).isNotEqualTo(element.getId());
		assertThat(clone.getType()).isEqualTo(element.getType());
		assertThat(clone.getContent()).isEqualTo(content);
	}
	
}
