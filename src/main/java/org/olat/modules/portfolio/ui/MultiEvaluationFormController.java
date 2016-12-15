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
	
	private Link ownerLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private List<Link> otherEvaluatorLinks = new ArrayList<>();
	
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
}
