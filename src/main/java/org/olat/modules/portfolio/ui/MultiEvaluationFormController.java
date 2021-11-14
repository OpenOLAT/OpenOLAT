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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.handler.EvaluationFormReportHandler;
import org.olat.modules.forms.handler.EvaluationFormReportProvider;
import org.olat.modules.forms.handler.FileUploadListingHandler;
import org.olat.modules.forms.handler.HTMLParagraphHandler;
import org.olat.modules.forms.handler.HTMLRawHandler;
import org.olat.modules.forms.handler.MultipleChoiceLegendTextHandler;
import org.olat.modules.forms.handler.RubricRadarHandler;
import org.olat.modules.forms.handler.SingleChoiceLegendTextHandler;
import org.olat.modules.forms.handler.SpacerHandler;
import org.olat.modules.forms.handler.TextInputLegendTextHandler;
import org.olat.modules.forms.handler.TitleHandler;
import org.olat.modules.forms.model.xml.FileUpload;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.HTMLParagraph;
import org.olat.modules.forms.model.xml.HTMLRaw;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.model.xml.Spacer;
import org.olat.modules.forms.model.xml.TextInput;
import org.olat.modules.forms.model.xml.Title;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.modules.forms.ui.EvaluationFormReportController;
import org.olat.modules.forms.ui.LegendNameGenerator;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MultiEvaluationFormController extends BasicController {
	
	private int count = 0;
	private final Identity owner;
	private final boolean readOnly;
	private final boolean doneFirst;
	private final EvaluationFormSurvey survey;
	
	private Link ownerLink;
	private Link compareLink;
	private VelocityContainer mainVC;
	private SegmentViewComponent segmentView;
	
	private List<Evaluator> evaluators = new ArrayList<>();
	private List<Link> otherEvaluatorLinks = new ArrayList<>();
	
	private EvaluationFormExecutionController currentEvalutionFormCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private PortfolioService portfolioService;
	
	public MultiEvaluationFormController(UserRequest ureq, WindowControl wControl,
			Identity owner, List<Identity> otherEvaluators, EvaluationFormSurvey survey,
			boolean doneFirst, boolean readOnly, boolean onePage, boolean anonym) {
		super(ureq, wControl);
		this.owner = owner;
		this.survey = survey;
		this.readOnly = readOnly;
		this.doneFirst = doneFirst;
		
		if(onePage) {
			initOnePageView(ureq, otherEvaluators, anonym);
		} else {
			initSegmentView(ureq, otherEvaluators, anonym);
		}
		putInitialPanel(mainVC);
	}
	
	private void initOnePageView(UserRequest ureq, List<Identity> otherEvaluators, boolean anonym) {
		mainVC = createVelocityContainer("multi_evaluation_one_page");
		List<EvaluatorPanel> panels = new ArrayList<>();
		mainVC.contextPut("panels", panels);

		boolean viewOthers = isViewOthers();
		
		if(owner != null) {
			String ownerFullname = userManager.getUserDisplayName(owner);
			Evaluator evaluator = new Evaluator(owner, ownerFullname);
			evaluators.add(evaluator);
			boolean me = owner.equals(getIdentity());
			if(me || viewOthers) {
				Controller ctrl = createEvalutationForm(ureq, owner);
				String componentName = "panel_" + (++count);
				panels.add(new EvaluatorPanel(evaluator, componentName, ctrl.getInitialComponent()));
				mainVC.put(componentName, ctrl.getInitialComponent());
			}
		}

		if(otherEvaluators != null && otherEvaluators.size() > 0) {
			int countEva = 1;
			for(Identity evaluator:otherEvaluators) {
				boolean me = evaluator.equals(ureq.getIdentity());
				
				String evaluatorFullname;
				if(!me && anonym) {
					evaluatorFullname = translate("anonym.evaluator", new String[] { Integer.toString(countEva++) });
				} else {
					evaluatorFullname = userManager.getUserDisplayName(evaluator);
				}
				Evaluator eval = new Evaluator(evaluator, evaluatorFullname);
				evaluators.add(eval);
				if(me || viewOthers) {
					Controller ctrl = createEvalutationForm(ureq, evaluator);
					String componentName = "panel_" + (++count);
					panels.add(new EvaluatorPanel(eval, componentName, ctrl.getInitialComponent()));
					mainVC.put(componentName, ctrl.getInitialComponent());
				}
			}
		}
		
		if(viewOthers && (owner != null && otherEvaluators != null && otherEvaluators.size() > 0) || (otherEvaluators != null && otherEvaluators.size() > 1)) {
			Controller ctrl = createReportController(ureq);
			Evaluator eval = new Evaluator(null, translate("compare.evaluations"));
			String componentName = "panel_" + (++count);
			panels.add(new EvaluatorPanel(eval, componentName, ctrl.getInitialComponent()));
			mainVC.put(componentName, ctrl.getInitialComponent());
		}
	}
	
	private Controller createEvalutationForm(UserRequest ureq, Identity evaluator) {
		boolean ro = readOnly || !evaluator.equals(getIdentity());
		boolean doneButton = !ro && evaluator.equals(getIdentity()) && (owner == null || !owner.equals(evaluator));
		EvaluationFormSession session = portfolioService.loadOrCreateSession(survey, evaluator);
		Controller evalutionFormCtrl =  new EvaluationFormExecutionController(ureq, getWindowControl(), session, ro, doneButton, false, null);
		listenTo(evalutionFormCtrl);
		return evalutionFormCtrl;
	}

	private void initSegmentView(UserRequest ureq, List<Identity> otherEvaluators, boolean anonym) {
		mainVC = createVelocityContainer("multi_evaluation_form");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		
		boolean viewOthers = isViewOthers();
		
		if(owner != null) {
			String ownerFullname = userManager.getUserDisplayName(owner);
			evaluators.add(new Evaluator(owner, ownerFullname));
			
			String id = "eva-" + (count++);
			ownerLink = LinkFactory.createCustomLink(id, id, ownerFullname, Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
			ownerLink.setUserObject(owner);
			boolean me = owner.equals(ureq.getIdentity());
			segmentView.addSegment(ownerLink, me);
			if(me) {
				doOpenEvalutationForm(ureq, owner);
			}
		}
		
		if(otherEvaluators != null && otherEvaluators.size() > 0) {
			int countEva = 1;
			for(Identity evaluator:otherEvaluators) {
				boolean me = evaluator.equals(ureq.getIdentity());
				
				String evaluatorFullname;
				if(!me && anonym) {
					evaluatorFullname = translate("anonym.evaluator", new String[] { Integer.toString(countEva++) });
				} else {
					evaluatorFullname = userManager.getUserDisplayName(evaluator);
				}
				evaluators.add(new Evaluator(evaluator, evaluatorFullname));
				
				String id = "eva-" + (count++);
				Link evaluatorLink = LinkFactory.createCustomLink(id, id, evaluatorFullname, Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
				evaluatorLink.setUserObject(evaluator);
				otherEvaluatorLinks.add(evaluatorLink);
				segmentView.addSegment(evaluatorLink, me);
				if(me) {
					doOpenEvalutationForm(ureq, evaluator);
				}
			}
		}
		
		if((owner != null && otherEvaluators != null && otherEvaluators.size() > 0) || (otherEvaluators != null && otherEvaluators.size() > 1)) {
			compareLink = LinkFactory.createLink("compare.evaluations", mainVC, this);
			compareLink.setUserObject(owner);
			segmentView.addSegment(compareLink, false);
		}
		
		segmentView.setVisible(viewOthers);
		mainVC.put("segments", segmentView);
	}
	
	private boolean isViewOthers() {
		boolean viewOthers;
		if(doneFirst) {
			EvaluationFormParticipation participation = evaluationFormManager.loadParticipationByExecutor(survey, getIdentity());
			viewOthers = participation == null ? false : participation.getStatus() == EvaluationFormParticipationStatus.done;
		} else {
			viewOthers = true;
		}
		return viewOthers;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(currentEvalutionFormCtrl == source) {
			if(event == Event.DONE_EVENT) {
				if(doneFirst) {
					// check if it's really done
					EvaluationFormParticipation participation = evaluationFormManager.loadParticipationByExecutor(survey, getIdentity());
					if(participation != null && participation.getStatus() == EvaluationFormParticipationStatus.done) {
						segmentView.setVisible(true);
					}
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == ownerLink) {
					doOpenEvalutationForm(ureq, owner);
				} else if(clickedLink == compareLink) {
					doOpenOverview(ureq);
				} else if (clickedLink instanceof Link) {
					Link link = (Link)clickedLink;
					Object uobject = link.getUserObject();
					if(uobject instanceof Identity) {
						doOpenEvalutationForm(ureq, (Identity)uobject);
					}
				}
			}
		}
	}

	private void doOpenEvalutationForm(UserRequest ureq, Identity evaluator) {
		boolean ro = readOnly || !evaluator.equals(getIdentity());
		boolean doneButton = !ro && evaluator.equals(getIdentity()) && (owner == null || !owner.equals(evaluator));
		EvaluationFormSession session = portfolioService.loadOrCreateSession(survey, evaluator);
		currentEvalutionFormCtrl =  new EvaluationFormExecutionController(ureq, getWindowControl(), session, ro, doneButton, !doneButton, null);
		listenTo(currentEvalutionFormCtrl);
		mainVC.put("segmentCmp", currentEvalutionFormCtrl.getInitialComponent());
	}
	
	private void doOpenOverview(UserRequest ureq) {
		Controller ctrl = createReportController(ureq);
		mainVC.put("segmentCmp", ctrl.getInitialComponent());
	}
	
	private EvaluationFormReportController createReportController(UserRequest ureq) {
		Form form = evaluationFormManager.loadForm(survey.getFormEntry());
		DataStorage storage = evaluationFormManager.loadStorage(survey.getFormEntry());
				
		SessionFilter surveyFilter = SessionFilterFactory.createSelectDone(survey);
		List<EvaluationFormSession> sessions = evaluationFormManager.loadSessionsFiltered(surveyFilter, 0, -1);
		sessions.removeIf(this::notEvaluator);
		SessionFilter filter = SessionFilterFactory.create(sessions);
		
		EvaluationFormReportProvider provider = new ReportProvider();

		LegendNameGenerator legendNameGenerator = new EvaluatorNameGenerator(evaluators);
		ReportHelper reportHelper = ReportHelper.builder(getLocale())
				.withColors()
				.withLegendNameGenrator(legendNameGenerator)
				.build();
		return new EvaluationFormReportController(ureq, getWindowControl(), form, storage, filter, provider, reportHelper);
	}
	
	private boolean notEvaluator(EvaluationFormSession session) {
		if (session != null && session.getParticipation() != null && session.getParticipation().getExecutor() != null) {
			Identity executor = session.getParticipation().getExecutor();
			List<Identity> evaluatorIdentities = evaluators.stream().map(Evaluator::getIdentity).collect(Collectors.toList());
			if (evaluatorIdentities.contains(executor)) {
				return false;
			}
		}
		return true;
	}

	private static final class ReportProvider implements EvaluationFormReportProvider {
		
		private final Map<String, EvaluationFormReportHandler> handlers = new HashMap<>();
		
		public ReportProvider() {
			handlers.put(Title.TYPE, new TitleHandler());
			handlers.put(Spacer.TYPE, new SpacerHandler());
			handlers.put(HTMLRaw.TYPE, new HTMLRawHandler());
			handlers.put(HTMLParagraph.TYPE, new HTMLParagraphHandler());
			handlers.put(Rubric.TYPE, new RubricRadarHandler());
			handlers.put(TextInput.TYPE, new TextInputLegendTextHandler());
			handlers.put(FileUpload.TYPE, new FileUploadListingHandler());
			handlers.put(SingleChoice.TYPE, new SingleChoiceLegendTextHandler());
			handlers.put(MultipleChoice.TYPE, new MultipleChoiceLegendTextHandler());
		}

		@Override
		public EvaluationFormReportHandler getReportHandler(PageElement element) {
			return handlers.get(element.getType());
		}
	}
	
	private static final class EvaluatorNameGenerator implements LegendNameGenerator {

		private final Map<Identity, String> identityToName;
		
		public EvaluatorNameGenerator(List<Evaluator> evaluators) {
			super();
			identityToName = evaluators.stream()
					.collect(Collectors.toMap(Evaluator::getIdentity, Evaluator::getFullName, (u, v) -> u));
		}

		@Override
		public String getName(EvaluationFormSession session, Identity identity) {
			String name = identityToName.get(identity);
			return StringHelper.containsNonWhitespace(name)? name: "???";
		}
		
	}
	
	private static final class Evaluator {
		
		private final Identity identity;
		private String fullName;
		
		public Evaluator(Identity identity, String fullName) {
			this.identity = identity;
			this.fullName = fullName;
		}

		public Identity getIdentity() {
			return identity;
		}

		public String getFullName() {
			return fullName;
		}
	}

	public static class EvaluatorPanel {
		
		
		private final Evaluator evaluator;
		private final Component component;
		private final String componentName;
		
		public EvaluatorPanel(Evaluator evaluator, String componentName, Component component) {
			this.evaluator = evaluator;
			this.component = component;
			this.componentName = componentName;
		}

		public Evaluator getEvaluator() {
			return evaluator;
		}

		public Component getComponent() {
			return component;
		}

		public String getComponentName() {
			return componentName;
		}
	}
}
