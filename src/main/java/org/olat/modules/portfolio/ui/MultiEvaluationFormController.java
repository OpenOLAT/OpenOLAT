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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.modules.forms.ui.CompareEvaluationsFormController;
import org.olat.modules.forms.ui.EvaluationFormController;
import org.olat.modules.portfolio.PageBody;
import org.olat.repository.RepositoryEntry;
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
	private final PageBody anchor;
	private final Identity owner;
	private final boolean readOnly;
	private final RepositoryEntry formEntry;
	private final List<Identity> otherEvaluators;
	
	private Link ownerLink;
	private Link compareLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private List<Link> otherEvaluatorLinks = new ArrayList<>();
	
	private CompareEvaluationsFormController compareEvaluationCtrl;
	
	@Autowired
	private UserManager userManager;
	
	public MultiEvaluationFormController(UserRequest ureq, WindowControl wControl,
			Identity owner, List<Identity> otherEvaluators, PageBody anchor,
			RepositoryEntry formEntry, boolean readOnly, boolean anonym) {
		super(ureq, wControl);
		this.owner = owner;
		this.anchor = anchor;
		this.readOnly = readOnly;
		this.formEntry = formEntry;
		this.otherEvaluators = otherEvaluators;

		mainVC = createVelocityContainer("multi_evaluation_form");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		
		if(owner != null) {
			String ownerFullname = userManager.getUserDisplayName(owner);
			String id = "eva-" + (count++);
			ownerLink = LinkFactory.createCustomLink(id, id, ownerFullname, Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
			ownerLink.setUserObject(owner);
			boolean selected = owner.equals(ureq.getIdentity());
			segmentView.addSegment(ownerLink, selected);
			if(selected) {
				doOpenEvalutationForm(ureq, owner);
			}
		}
		
		if(otherEvaluators != null && otherEvaluators.size() > 0) {
			int countEva = 1;
			for(Identity evaluator:otherEvaluators) {
				boolean selected = evaluator.equals(ureq.getIdentity());
				
				String evaluatorFullname;
				if(!selected && anonym) {
					evaluatorFullname = translate("anonym.evaluator", new String[] { Integer.toString(++countEva) });
				} else {
					evaluatorFullname = userManager.getUserDisplayName(evaluator);
				}
				
				String id = "eva-" + (count++);
				Link evaluatorLink = LinkFactory.createCustomLink(id, id, evaluatorFullname, Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
				evaluatorLink.setUserObject(evaluator);
				otherEvaluatorLinks.add(evaluatorLink);
				segmentView.addSegment(evaluatorLink, selected);
				if(selected) {
					doOpenEvalutationForm(ureq, evaluator);
				}
			}
		}
		
		if((owner != null && otherEvaluators != null && otherEvaluators.size() > 0) || (otherEvaluators != null && otherEvaluators.size() > 1)) {
			compareLink = LinkFactory.createLink("compare.evaluations", mainVC, this);
			compareLink.setUserObject(owner);
			segmentView.addSegment(compareLink, false);
		}
		
		mainVC.put("segments", segmentView);
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
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
		EvaluationFormController evalutionFormCtrl =  new EvaluationFormController(ureq, getWindowControl(), evaluator, anchor, formEntry, ro, doneButton);
		listenTo(evalutionFormCtrl);
		mainVC.put("segmentCmp", evalutionFormCtrl.getInitialComponent());
	}
	
	private void doOpenOverview(UserRequest ureq) {
		removeAsListenerAndDispose(compareEvaluationCtrl);
		
		List<Identity> evaluators = new ArrayList<>();
		if(owner != null) {
			evaluators.add(owner);
		}
		if(otherEvaluators != null) {
			evaluators.addAll(otherEvaluators);
		}
		compareEvaluationCtrl = new CompareEvaluationsFormController(ureq, getWindowControl(), evaluators, anchor, formEntry);
		listenTo(compareEvaluationCtrl);
		mainVC.put("segmentCmp", compareEvaluationCtrl.getInitialComponent());
	}
}
