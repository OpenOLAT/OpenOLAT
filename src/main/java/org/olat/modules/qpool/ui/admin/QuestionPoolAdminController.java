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
package org.olat.modules.qpool.ui.admin;

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

/**
 * 
 * Initial date: 29.11.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QuestionPoolAdminController extends BasicController {
	
	private static final String SEGMENTS_CMP = "segmentCmp";
	
	private final Link configurationLink;
	private final Link reviewProcessLink;
	private final Link taxonomyLink;
	private final Link poolsLink;
	private final Link itemTypesLink;
	private final Link educationalContextLink;
	private final Link licensesLink;
	
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	
	private QuestionPoolAdminConfigurationController configurationCtrl;
	private ReviewProcessAdminController reviewProcessCtrl;
	private TaxonomyAdminController taxonomyCtrl;
	private PoolsAdminController poolsCtrl;
	private QItemTypesAdminController itemTypesCtrl;
	private QEducationalContextsAdminController educationalContextCtrl;
	private QLicensesAdminController licensesCtrl;

	public QuestionPoolAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("segments");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		configurationLink = LinkFactory.createLink("segment.configuration", mainVC, this);
		segmentView.addSegment(configurationLink, true);
		reviewProcessLink = LinkFactory.createLink("segment.review.process", mainVC, this);
		segmentView.addSegment(reviewProcessLink, false);
		taxonomyLink = LinkFactory.createLink("segment.taxonomy", mainVC, this);
		segmentView.addSegment(taxonomyLink, false);
		poolsLink = LinkFactory.createLink("segment.pools", mainVC, this);
		segmentView.addSegment(poolsLink, false);
		itemTypesLink = LinkFactory.createLink("segment.item.types", mainVC, this);
		segmentView.addSegment(itemTypesLink, false);
		educationalContextLink = LinkFactory.createLink("segment.educational.context", mainVC, this);
		segmentView.addSegment(educationalContextLink, false);
		licensesLink = LinkFactory.createLink("segment.licenses", mainVC, this);
		segmentView.addSegment(licensesLink, false);
		
		doOpenConfiguration(ureq);
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView && event instanceof SegmentViewEvent) {
			SegmentViewEvent sve = (SegmentViewEvent)event;
			String segmentCName = sve.getComponentName();
			Component clickedLink = mainVC.getComponent(segmentCName);
			if (clickedLink == configurationLink) {
				doOpenConfiguration(ureq);
			} else if (clickedLink == reviewProcessLink) {
				doOpenReviewProcess(ureq);
			} else if (clickedLink == taxonomyLink) {
				doOpenTaxonomy(ureq);
			} else if (clickedLink == poolsLink) {
				doOpenPools(ureq);
			} else if (clickedLink == itemTypesLink) {
				doOpenItemTypes(ureq);
			} else if (clickedLink == educationalContextLink) {
				doOpenEducationalContext(ureq);
			} else if (clickedLink == licensesLink) {
				doOpenLicenses(ureq);
			}
		}
	}

	private void doOpenConfiguration(UserRequest ureq) {
		if(configurationCtrl == null) {
			configurationCtrl = new QuestionPoolAdminConfigurationController(ureq, getWindowControl());
			listenTo(configurationCtrl);
		}
		mainVC.put(SEGMENTS_CMP, configurationCtrl.getInitialComponent());
	}

	private void doOpenReviewProcess(UserRequest ureq) {
		if(reviewProcessCtrl == null) {
			reviewProcessCtrl = new ReviewProcessAdminController(ureq, getWindowControl());
			listenTo(reviewProcessCtrl);
		}
		mainVC.put(SEGMENTS_CMP, reviewProcessCtrl.getInitialComponent());
	}

	private void doOpenTaxonomy(UserRequest ureq) {
		if(taxonomyCtrl == null) {
			taxonomyCtrl = new TaxonomyAdminController(ureq, getWindowControl());
			listenTo(taxonomyCtrl);
		}
		mainVC.put(SEGMENTS_CMP, taxonomyCtrl.getInitialComponent());
	}

	private void doOpenPools(UserRequest ureq) {
		if(poolsCtrl == null) {
			poolsCtrl = new PoolsAdminController(ureq, getWindowControl());
			listenTo(poolsCtrl);
		}
		mainVC.put(SEGMENTS_CMP, poolsCtrl.getInitialComponent());
	}

	private void doOpenItemTypes(UserRequest ureq) {
		if(itemTypesCtrl == null) {
			itemTypesCtrl = new QItemTypesAdminController(ureq, getWindowControl());
			listenTo(itemTypesCtrl);
		}
		mainVC.put(SEGMENTS_CMP, itemTypesCtrl.getInitialComponent());
	}

	private void doOpenEducationalContext(UserRequest ureq) {
		if(educationalContextCtrl == null) {
			educationalContextCtrl = new QEducationalContextsAdminController(ureq, getWindowControl());
			listenTo(educationalContextCtrl);
		}
		mainVC.put(SEGMENTS_CMP, educationalContextCtrl.getInitialComponent());
	}

	private void doOpenLicenses(UserRequest ureq) {
		if(licensesCtrl == null) {
			licensesCtrl = new QLicensesAdminController(ureq, getWindowControl());
			listenTo(licensesCtrl);
		}
		mainVC.put(SEGMENTS_CMP, licensesCtrl.getInitialComponent());
	}

		
}
