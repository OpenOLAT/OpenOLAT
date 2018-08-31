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
package org.olat.modules.quality.ui;

import static org.olat.modules.quality.ui.QualityUIFactory.formatTopic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityContextToCurriculumElement;
import org.olat.modules.quality.QualityContextToTaxonomyLevel;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityService;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExecutionController extends BasicController {

	private VelocityContainer mainVC;
	private Link back;
	private Controller executionCtrl;
	
	private final QualityExecutorParticipation qualityParticipation;
	
	@Autowired
	private QualityService qualityService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public ExecutionController(UserRequest ureq, WindowControl wControl, QualityExecutorParticipation qualityParticipation) {
		super(ureq, wControl);
		this.qualityParticipation = qualityParticipation;
		
		mainVC = createVelocityContainer("execution");
		initVelocityContainer(ureq);
		putInitialPanel(mainVC);
	}

	protected void initVelocityContainer(UserRequest ureq) {
		back = LinkFactory.createLinkBack(mainVC, this);
		mainVC.put("back", back);
		
		mainVC.contextPut("title", qualityParticipation.getTitle());
		mainVC.contextPut("contexts", createContextWrappers());
		
		EvaluationFormSession session = loadOrCreateSession(qualityParticipation.getParticipationRef());
		executionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), session);
		listenTo(executionCtrl);
		mainVC.put("execution", executionCtrl.getInitialComponent());
	}

	private EvaluationFormSession loadOrCreateSession(EvaluationFormParticipationRef participationRef) {
		EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(participationRef);
		if (session == null) {
			EvaluationFormParticipation participation = evaluationFormManager.loadParticipationByKey(participationRef);
			session = evaluationFormManager.createSession(participation);
		}
		return session;
	}

	private Collection<ContextWrapper> createContextWrappers() {
		List<QualityContext> contexts = qualityService.loadContextByParticipation(qualityParticipation.getParticipationRef());
		ArrayList<ContextWrapper> wrappers = new ArrayList<>();
		for (QualityContext context: contexts) {
			ContextWrapper wrapper = createContextWrapper(context);
			wrappers.add(wrapper);
		}
		return wrappers;
	}

	private ContextWrapper createContextWrapper(QualityContext context) {
		QualityContextRole role = context.getRole();
		String translatedRole = translateRole(role);
		ContextWrapper wrapper = new ContextWrapper(translatedRole);
		KeyValue topicKeyValue = createTopicKeyValue();
		if (topicKeyValue != null) {
			wrapper.add(topicKeyValue);
		}
		KeyValue previousKeyValue = createPreviousKeyValue();
		if (previousKeyValue != null) {
			wrapper.add(previousKeyValue);
		}
		KeyValue roleKeyValue = createRoleKeyValue(context);
		if (roleKeyValue != null) {
			wrapper.add(roleKeyValue);
		}
		KeyValue repositoryKeyValue = createRepositoryEntryValues(context);
		if (repositoryKeyValue != null) {
			wrapper.add(repositoryKeyValue);
		}
		List<KeyValue> curriculumElements = createCurriculumElementValues(context);
		wrapper.addAll(curriculumElements);
		List<KeyValue> taxonomyLevels = createTaxonomyLevels(context);
		wrapper.addAll(taxonomyLevels);
		if (wrapper.getKeyValues().size() % 2 == 1) {
			// Add empty entry to have an even number of entries
			wrapper.add(new KeyValue("", ""));
		}
		return wrapper;
	}

	private KeyValue createTopicKeyValue() {
		KeyValue keyValue = null;
		if (qualityParticipation.getTopic() != null) {
			String key = translate("executor.participation.topic.title");
			String value = formatTopic(qualityParticipation);
			keyValue = new KeyValue(key, value);
		}
		return keyValue;
	}

	private KeyValue createPreviousKeyValue() {
		KeyValue keyValue = null;
		if (qualityParticipation.getPreviousTitle() != null) {
			String key = translate("executor.participation.previous.title");
			String value = qualityParticipation.getPreviousTitle();
			keyValue = new KeyValue(key, value);
		}
		return keyValue;
	}

	private KeyValue createRoleKeyValue(QualityContext context) {
		KeyValue keyValue = null;
		if (context.getRole() != null && !context.getRole().equals(QualityContextRole.none)) {
			String key = translate("executor.participation.rating");
			String value = translateRole(context.getRole());
			keyValue = new KeyValue(key, value);
		}
		return keyValue;
	}

	private String translateRole(QualityContextRole role) {
		switch (role) {
		case owner: return translate("executor.participation.owner");
		case coach: return translate("executor.participation.coach");
		case participant: return translate("executor.participation.participant");
		default: return "";
		}
	}

	private KeyValue createRepositoryEntryValues(QualityContext context) {
		KeyValue keyValue = null;
		if (context.getAudienceRepositoryEntry() != null) {
			String key = translate("executor.participation.repository");
			String value = context.getAudienceRepositoryEntry().getDisplayname();
			keyValue = new KeyValue(key, value);
		}
		return keyValue;
	}

	private List<KeyValue> createCurriculumElementValues(QualityContext context) {
		List<KeyValue> keyValues = new ArrayList<>();
		for (QualityContextToCurriculumElement contextToCurriculumelement: context.getContextToCurriculumElement()) {
			CurriculumElement curriculumElement = contextToCurriculumelement.getCurriculumElement();
			if (curriculumElement != null) {
				CurriculumElementType curriculumElementType = curriculumElement.getType();
				String key;
				if (curriculumElementType != null) {
					key = curriculumElementType.getDisplayName();
				} else {
					key = translate("executor.participation.curriculum.element");
				}
				String value = curriculumElement.getDisplayName();
				KeyValue keyValue = new KeyValue(key, value);
				keyValues.add(keyValue);
			}
		}
		return keyValues;
	}

	private List<KeyValue> createTaxonomyLevels(QualityContext context) {
		List<KeyValue> keyValues = new ArrayList<>();
		for (QualityContextToTaxonomyLevel contextToTaxonomyLevel: context.getContextToTaxonomyLevel()) {
			TaxonomyLevel taxonomyLevel = contextToTaxonomyLevel.getTaxonomyLevel();
			if (taxonomyLevel != null) {
				TaxonomyLevelType taxonomyLevelType = taxonomyLevel.getType();
				String key;
				if (taxonomyLevelType != null) {
					key = taxonomyLevelType.getDisplayName();
				} else {
					key = translate("executor.participation.taxonomy.level");
				}
				String value = taxonomyLevel.getDisplayName();
				KeyValue keyValue = new KeyValue(key, value);
				keyValues.add(keyValue);
			}
		}
		return keyValues;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == executionCtrl && event == Event.DONE_EVENT) {
			fireEvent(ureq, event);
		} 
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == back) {
			fireEvent(ureq, Event.CLOSE_EVENT);
		}
	}

	@Override
	protected void doDispose() {
		removeAsListenerAndDispose(executionCtrl);
		executionCtrl = null;
	}
	
	public static class ContextWrapper {
		
		private final String role;
		private final List<KeyValue> keyValues = new ArrayList<>();

		public ContextWrapper(String role) {
			this.role = role;
		}

		public String getRole() {
			return role;
		}
		
		void add(KeyValue keyValue) {
			keyValues.add(keyValue);
		}
		
		void addAll(Collection<KeyValue> all) {
			keyValues.addAll(all);
		}

		public List<KeyValue> getKeyValues() {
			return keyValues;
		}
		
	}
	
	public class KeyValue {
		
		private final String key;
		private final String value;
		
		public KeyValue(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}
		
	}

}
