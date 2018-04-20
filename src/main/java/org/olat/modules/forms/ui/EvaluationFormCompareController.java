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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.handler.AllHandlerPageProvider;
import org.olat.modules.forms.handler.EvaluationFormElementHandler;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.FormXStream;
import org.olat.modules.forms.ui.model.CompareResponse;
import org.olat.modules.forms.ui.model.Evaluator;
import org.olat.modules.portfolio.PageBody;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormCompareController extends FormBasicController {
	
	private static final String[] colors = new String[]{
			"#EDC951", "#CC333F", "#00A0B0", "#4E4E6C", "#8DC1A1",
			"#F7BC00", "#BB6511", "#B28092", "#003D40", "#FF69D1"
		};
	
	private final Map<String, EvaluationFormElementHandler> handlerMap = new HashMap<>();
	private final List<CompareFragment> fragments = new ArrayList<>();
	private final Form form;
	private final PageBody anchor;
	private final List<Evaluator> evaluators;
	private List<EvaluationFormResponse> loadedResponses;
	private final Map<Long, String> sessionToLegend = new HashMap<>();
	private final Map<Long, String> sessionToColor = new HashMap<>();
	
	private int colorIndex = 0;
	
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public EvaluationFormCompareController(UserRequest ureq, WindowControl wControl,
			List<Evaluator> evaluators, PageBody anchor, RepositoryEntry formEntry) {
		super(ureq, wControl, "compare");
		this.anchor = anchor;
		this.evaluators = evaluators;
		
		File repositoryDir = new File(FileResourceManager.getInstance().getFileResourceRoot(formEntry.getOlatResource()), FileResourceManager.ZIPDIR);
		File formFile = new File(repositoryDir, FORM_XML_FILE);
		form = (Form)XStreamHelper.readObject(FormXStream.getXStream(), formFile);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		AllHandlerPageProvider provider = new AllHandlerPageProvider(form);
		for(EvaluationFormElementHandler handler: provider.getAvailableHandlers()) {
			handlerMap.put(handler.getType(), handler);
		}
		
		loadResponses();
		loadElements(ureq);
	}
	
	private void loadResponses() {
		List<Identity> evaluatorIdentities = evaluators.stream().map(evaluator -> evaluator.getIdentity()).collect(Collectors.toList());
		loadedResponses = evaluationFormManager.getResponsesFromPortfolioEvaluation(evaluatorIdentities, anchor, EvaluationFormSessionStatus.done);
		for (EvaluationFormResponse response: loadedResponses) {
			EvaluationFormSession session = response.getSession();
			if (!sessionToLegend.containsKey(session.getKey())) {
				sessionToLegend.put(session.getKey(), getLegendName(session));
			}
			if (!sessionToColor.containsKey(session.getKey())) {
				sessionToColor.put(session.getKey(), getColor());
			}
		}
	}

	private String getLegendName(EvaluationFormSession session) {
		Evaluator evaluator = getEvaluator(session);
		String legendName = evaluator != null? evaluator.getFullName(): null;
		if (!StringHelper.containsNonWhitespace(legendName)) {
			legendName = "???";
		}
		return legendName;
	}

	private Evaluator getEvaluator(EvaluationFormSession session) {
		for (Evaluator evaluator : evaluators) {
			if (evaluator.getIdentity().equals(session.getIdentity())) {
				return evaluator;
			}
		}
		return null;
	}

	private String getColor() {
		String color = colors[colorIndex];
		colorIndex++;
		if (colorIndex >= colors.length) {
			colorIndex = 0;
		}
		return color;
	}
	
	private void loadElements(UserRequest ureq) {
		fragments.clear();
		for(AbstractElement element: form.getElements()) {
			EvaluationFormElementHandler handler = handlerMap.get(element.getType());
			if(handler != null) {
				List<String> responseIdentifiers = handler.getCompareResponseIdentifiers(element);
				List<CompareResponse> compareResponses = getCompareResponses(responseIdentifiers);
				Component component = handler.getCompareComponent(ureq, getWindowControl(), element, compareResponses);
				String cmpId = "cpt-" + CodeHelper.getRAMUniqueID();
				fragments.add(new CompareFragment(handler.getType(), cmpId));
				flc.put(cmpId, component);
			}
		}
		flc.contextPut("fragments", fragments);
	}

	private List<CompareResponse> getCompareResponses(List<String> responseIdentifiers) {
		List<CompareResponse> compareResponses = new ArrayList<>();
		for (Long sessionKey: sessionToLegend.keySet()) {
			List<EvaluationFormResponse> responses = getResponses(sessionKey, responseIdentifiers);
			String legendName = sessionToLegend.get(sessionKey);
			String color = sessionToColor.get(sessionKey);
			CompareResponse compareResponse = new CompareResponse(responses, legendName, color);
			compareResponses.add(compareResponse);
		}
		
		return compareResponses;
	}

	private List<EvaluationFormResponse> getResponses(Long sessionKey, List<String> responseIdentifiers) {
		List<EvaluationFormResponse> responses = new ArrayList<>();
		for (String responseIdentifier: responseIdentifiers) {
			List<EvaluationFormResponse> response = getResponses(sessionKey, responseIdentifier);
			responses.addAll(response);
		}
		return responses;
	}

	private List<EvaluationFormResponse> getResponses(Long sessionKey, String responseIdentifier) {
		List<EvaluationFormResponse> responses = new ArrayList<>();
		for (EvaluationFormResponse response: loadedResponses) {
			if (sessionKey.equals(response.getSession().getKey())
					&& responseIdentifier.equals(response.getResponseIdentifier())) {
				responses.add(response);
			}
		}
		return responses;
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
	
	public static final class CompareFragment {

		private final String type;
		private final String componentName;
		
		public CompareFragment(String type, String componentName) {
			this.type = type;
			this.componentName = componentName;
		}
		
		public String getCssClass() {
			return "o_ed_".concat(type);
		}
		
		public String getComponentName() {
			return componentName;
		}
	}

}
