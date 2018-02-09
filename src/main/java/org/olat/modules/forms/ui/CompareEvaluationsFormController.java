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
package org.olat.modules.forms.ui;

import static org.olat.modules.forms.handler.EvaluationFormResource.FORM_XML_FILE;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.chart.RadarChartComponent.Format;
import org.olat.core.gui.components.chart.RadarChartElement;
import org.olat.core.gui.components.chart.RadarSeries;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.FileUpload;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.FormXStream;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.TextInput;
import org.olat.modules.forms.ui.component.SliderOverviewElement;
import org.olat.modules.forms.ui.component.SliderPoint;
import org.olat.modules.forms.ui.model.EvaluationFormElementWrapper;
import org.olat.modules.forms.ui.model.Evaluator;
import org.olat.modules.forms.ui.model.FileUploadCompareWrapper;
import org.olat.modules.forms.ui.model.SliderWrapper;
import org.olat.modules.forms.ui.model.TextInputWrapper;
import org.olat.modules.portfolio.PageBody;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CompareEvaluationsFormController extends FormBasicController {
	
	private static final String[] colors = new String[]{
			"#EDC951", "#CC333F", "#00A0B0", "#4E4E6C", "#8DC1A1",
			"#F7BC00", "#BB6511", "#B28092", "#003D40", "#FF69D1"
		};

	private int count = 0;
	private final Form form;
	private PageBody anchor;
	private final List<Evaluator> evaluators;
	
	private EvaluationFormSession session;
	private final Map<String, List<EvaluationFormResponse>> identifierToResponses = new HashMap<>();
	private final Map<Identity,String> evaluatorToColors = new HashMap<>();
	private final Map<Identity,Evaluator> evaluatorToNumbers = new HashMap<>();
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	/**
	 * The responses are saved and linked to the anchor.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param form
	 * @param anchor The database object which hold the evaluation.
	 */
	public CompareEvaluationsFormController(UserRequest ureq, WindowControl wControl,
			List<Evaluator> evaluators, PageBody anchor, RepositoryEntry formEntry) {
		super(ureq, wControl, "run");
		this.anchor = anchor;
		this.evaluators = evaluators;
		
		int colorCount = 0;
		for(Evaluator evaluator:evaluators) {
			int i = (colorCount++) % colors.length;
			evaluatorToColors.put(evaluator.getIdentity(), colors[i]);
			evaluatorToNumbers.put(evaluator.getIdentity(), evaluator);
		}
		
		File repositoryDir = new File(FileResourceManager.getInstance().getFileResourceRoot(formEntry.getOlatResource()), FileResourceManager.ZIPDIR);
		File formFile = new File(repositoryDir, FORM_XML_FILE);
		form = (Form)XStreamHelper.readObject(FormXStream.getXStream(), formFile);
		
		loadResponses();
		initForm(ureq);
	}
	
	public EvaluationFormSession getSession() {
		return session;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		updateElements(ureq);
	}
	
	private void updateElements(UserRequest ureq) {
		List<EvaluationFormElementWrapper> elementWrappers = new ArrayList<>();
		for(AbstractElement element:form.getElements()) {
			appendsElement(ureq, element, elementWrappers);
		}

		flc.contextPut("elements", elementWrappers);
	}
	
	private void loadResponses() {
		flc.contextPut("messageNotDone", Boolean.FALSE);
		
		List<Identity> evaluatorIdentities = evaluators.stream().map(evaluator -> evaluator.getIdentity()).collect(Collectors.toList());
		List<EvaluationFormResponse> responses = evaluationFormManager.getResponsesFromPortfolioEvaluation(evaluatorIdentities, anchor, EvaluationFormSessionStatus.done);
		for(EvaluationFormResponse response:responses) {
			List<EvaluationFormResponse> responseList = identifierToResponses.get(response.getResponseIdentifier());
			if(responseList == null) {
				responseList = new ArrayList<>();
				identifierToResponses.put(response.getResponseIdentifier(), responseList);
			}
			responseList.add(response);
		}
	}
	
	private void appendsElement(UserRequest ureq, AbstractElement element, List<EvaluationFormElementWrapper> wrappers) {
		String type = element.getType();
		switch(type) {
			case "formhtitle":
			case "formhr":
			case "formhtmlraw":
				wrappers.add(new EvaluationFormElementWrapper(element));
				break;
			case "formrubric":
				Rubric rubric = (Rubric)element;
				EvaluationFormElementWrapper sliderWrapper;
				if(rubric.getSliders().size() > 2) {
					sliderWrapper = forgeRadarRubric((Rubric)element);
				} else {
					sliderWrapper = forgeRubric((Rubric)element);
				}
				if(sliderWrapper != null) {
					wrappers.add(sliderWrapper);
				}
				break;
			case "formtextinput":
				List<EvaluationFormElementWrapper> inputWrappers = forgeTextInput((TextInput)element);
				if(inputWrappers != null && !inputWrappers.isEmpty()) {
					wrappers.addAll(inputWrappers);
				}
				break;
			case "formfileupload":
				List<EvaluationFormElementWrapper> fileUploadWrappers = forgeFileUpload(ureq, (FileUpload)element);
				if(fileUploadWrappers != null && !fileUploadWrappers.isEmpty()) {
					wrappers.addAll(fileUploadWrappers);
				}
				break;
		}
	}

	private List<EvaluationFormElementWrapper> forgeFileUpload(UserRequest ureq, FileUpload element) {
		List<EvaluationFormResponse> responses = identifierToResponses.get(element.getId());
		if (responses == null) {
			return new ArrayList<>();
		}
		List<EvaluationFormElementWrapper> fileUploadWrappers = new ArrayList<>(responses.size());
		for (EvaluationFormResponse response:responses) {
			if (response.getFileResponse() != null) {
				FileUploadCompareWrapper fileUploadWrapper = createFileUploadCompareWrapper(ureq, element, response);
				EvaluationFormElementWrapper wrapper = new EvaluationFormElementWrapper(element);
				wrapper.setFileUploadCompareWrapper(fileUploadWrapper);
				fileUploadWrappers.add(wrapper);
			}
		}
		return fileUploadWrappers;
	}

	private FileUploadCompareWrapper createFileUploadCompareWrapper(UserRequest ureq, FileUpload element,
			EvaluationFormResponse response) {
		Identity evaluator = response.getSession().getIdentity();
		String color = evaluatorToColors.get(evaluator);
		String evaluatorName = getLegend(evaluator);
		String filename = response.getStringuifiedResponse();
		String filesize = null;
		String mapperUri = null;
		String iconCss = null;
		String thumbUri = null;
		VFSLeaf leaf = evaluationFormManager.loadResponseLeaf(response);
		if (leaf != null) {
			filename = leaf.getName();
			filesize = Formatter.formatBytes((leaf).getSize());
			mapperUri = registerCacheableMapper(ureq, "file-upload-" + element.getId() + "-" + leaf.getLastModified(), new VFSMediaMapper(leaf));
			iconCss = CSSHelper.createFiletypeIconCssClassFor(leaf.getName());
			if (leaf instanceof MetaTagged) {
				MetaTagged metaTaggedLeaf = (MetaTagged) leaf;
				MetaInfo meta = metaTaggedLeaf.getMetaInfo();
				if (meta != null && meta.isThumbnailAvailable()) {
					VFSLeaf thumb = meta.getThumbnail(200, 200, false);
					if (thumb != null) {
						thumbUri = registerCacheableMapper(ureq, "file-upload-thumb" + element.getId() + "-" + leaf.getLastModified(), new VFSMediaMapper(thumb));;
					}
				}
			}
		}
		return new FileUploadCompareWrapper(color, evaluatorName, filename, filesize, mapperUri, iconCss, thumbUri);
	}

	private List<EvaluationFormElementWrapper> forgeTextInput(TextInput element) {
		List<EvaluationFormResponse> responses = identifierToResponses.get(element.getId());
		if (responses == null) {
			// in review - selbstreview ??
			return new ArrayList<>();
		}
		List<EvaluationFormElementWrapper> inputWrappers = new ArrayList<>(responses.size());
		for(EvaluationFormResponse response:responses) {
			if(StringHelper.containsNonWhitespace(response.getStringuifiedResponse())) {
				String initialValue = response.getStringuifiedResponse();
				if(initialValue != null) {
					initialValue = Formatter.stripTabsAndReturns(initialValue).toString();
				}
				Identity evaluator = response.getSession().getIdentity();
				String legend = getLegend(evaluator);
				String color = evaluatorToColors.get(evaluator);
		
				TextInputWrapper textInputWrapper = new TextInputWrapper(legend, color, initialValue, null);
				EvaluationFormElementWrapper wrapper = new EvaluationFormElementWrapper(element);
				wrapper.setTextInputWrapper(textInputWrapper);
				inputWrappers.add(wrapper);
			}
		}
		return inputWrappers;
	}
	
	private EvaluationFormElementWrapper forgeRubric(Rubric element) {
		EvaluationFormElementWrapper wrapper = new EvaluationFormElementWrapper(element);
		wrapper.setSliderOverview(true);
		List<Slider> sliders = element.getSliders();
		List<SliderWrapper> sliderWrappers = new ArrayList<>(sliders.size());
		for(Slider slider:sliders) {
			String responseIdentifier = slider.getId();
			List<EvaluationFormResponse> responses = identifierToResponses.get(responseIdentifier);
			SliderWrapper sliderWrapper = forgeSliderStats(slider, element, responses);
			sliderWrappers.add(sliderWrapper);
		}
		wrapper.setSliders(sliderWrappers);
		return wrapper;
	}
	
	private EvaluationFormElementWrapper forgeRadarRubric(Rubric element) {
		EvaluationFormElementWrapper wrapper = new EvaluationFormElementWrapper(element);
		wrapper.setRadarOverview(true);
		
		List<String> axisList = new ArrayList<>();
		List<Slider> sliders = element.getSliders();
		Map<EvaluationFormSession,RadarSeries> series = new HashMap<>();
		for(Slider slider:sliders) {
			String axis;
			 if(StringHelper.containsNonWhitespace(slider.getEndLabel())) {
				axis = slider.getEndLabel();
			} else if(StringHelper.containsNonWhitespace(slider.getStartLabel())) {
				axis = slider.getStartLabel();
			} else {
				axis = "";
			}
			axisList.add(axis);
			
			String responseIdentifier = slider.getId();
			List<EvaluationFormResponse> responses = identifierToResponses.get(responseIdentifier);
			if(responses != null && responses.size() > 0) {
				for(EvaluationFormResponse response:responses) {
					EvaluationFormSession responseSession = response.getSession();
					if(!series.containsKey(responseSession)) {
						Identity identity = responseSession.getIdentity();
						String legend = getLegend(identity);
						String color = evaluatorToColors.get(identity);
						series.put(responseSession, new RadarSeries(legend, color));
					}
					if(response.getNumericalResponse() != null ) {
						double value = response.getNumericalResponse().doubleValue();
						series.get(responseSession).addPoint(axis, value);
					}
				}
			}
		}
		
		String id = "radar_" + (count++);
		RadarChartElement radarEl = new RadarChartElement(id);
		radarEl.setSeries(new ArrayList<>(series.values()));
		radarEl.setShowLegend(true);
		radarEl.setAxis(axisList);
		if(element.getSliderType() == SliderType.discrete || element.getSliderType() == SliderType.discrete_slider) {
			radarEl.setLevels(element.getSteps());
			radarEl.setMaxValue(element.getSteps());
			radarEl.setFormat(Format.integer);
		} else if(element.getSliderType() == SliderType.continuous) {
			radarEl.setLevels(10);
			radarEl.setMaxValue(100);
			radarEl.setFormat(Format.integer);
		}
		wrapper.setRadarEl(radarEl);
		flc.add(id, radarEl);
		return wrapper;
	}
	
	private SliderWrapper forgeSliderStats(Slider slider, Rubric element, List<EvaluationFormResponse> responses) {
		String id = "overview_" + (count++);
		SliderOverviewElement overviewEl = new SliderOverviewElement(id);
		overviewEl.setMinValue(element.getStart());
		overviewEl.setMaxValue(element.getEnd());
		flc.add(id, overviewEl);
		
		List<SliderPoint> values = new ArrayList<>();
		if(responses != null && responses.size() > 0) {
			for(EvaluationFormResponse response:responses) {
				if(response.getNumericalResponse() != null) {
					Identity evaluator = response.getSession().getIdentity();
					String color = evaluatorToColors.get(evaluator);
					double value = response.getNumericalResponse().doubleValue();
					values.add(new SliderPoint(color, value));
				}
			}
		}
		overviewEl.setValues(values);
		
		return new SliderWrapper(slider, overviewEl);
	}
	
	private String getLegend(Identity identity) {
		String legend;
		if(evaluatorToNumbers.containsKey(identity)) {
			legend = evaluatorToNumbers.get(identity).getFullName();
		} else {
			legend = "???";
		}
		return legend;
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//super.propagateDirtinessToContainer(fiSrc, fe);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
}
