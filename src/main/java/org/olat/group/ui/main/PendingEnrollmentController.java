/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.group.ui.main;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BGRepositoryEntryRelation;
import org.olat.login.SupportsAfterLoginInterceptor;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.model.ACResourceInfo;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * Resume to the last business path. The controller use the preferences
 * of the user to resume automatically, ask to resume with a popup window
 * or ignore the feature completely.
 * <p>
 * Initial Date:  12 jan. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class PendingEnrollmentController extends FormBasicController implements SupportsAfterLoginInterceptor {

	private final ACService acService;
	private final BusinessGroupService businessGroupService;
	
	private List<ReservationWrapper> reservations;
	private boolean showFormOK;

	public PendingEnrollmentController(UserRequest ureq, WindowControl wControl) { 
		this(ureq, wControl, true);
	}
	
	public PendingEnrollmentController(UserRequest ureq, WindowControl wControl, boolean showFormOK) {
		super(ureq, wControl, "accept_reservations");
		
		this.showFormOK = showFormOK;
		this.acService = CoreSpringFactory.getImpl(ACService.class);
		this.businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		
		
		getAndShowEnrolments();
		initForm(ureq);
	}

	private void getAndShowEnrolments() {
		List<ResourceReservation> resourceReservations = acService.getReservations(getIdentity());
		reservations = new ArrayList<>(resourceReservations.size());

		if(!resourceReservations.isEmpty()) {
			List<Long> groupKeys = new ArrayList<>();
			List<OLATResource> resources = new ArrayList<>();
			for(ResourceReservation reservation: resourceReservations) {
				OLATResource resource = reservation.getResource();
				if("BusinessGroup".equals(resource.getResourceableTypeName())) {
					groupKeys.add(resource.getResourceableId());
				}
				resources.add(resource);
			}
			
			List<ACResourceInfo> resourceInfos = acService.getResourceInfos(resources);
			List<BGRepositoryEntryRelation> relations = businessGroupService.findRelationToRepositoryEntries(groupKeys, 0, -1);
			
			for(ResourceReservation reservation: resourceReservations) {
				OLATResource resource = reservation.getResource();
				ReservationWrapper wrapper = new ReservationWrapper(reservation);
				reservations.add(wrapper);
				
				
				for(ACResourceInfo resourceInfo:resourceInfos) {
					if(resource.equals(resourceInfo.getResource())) {
						wrapper.setName(resourceInfo.getName());
						wrapper.setDescription(resourceInfo.getDescription());
					}
				}
	
				if("BusinessGroup".equals(resource.getResourceableTypeName()) && !relations.isEmpty()) {
					List<String> courseNames = new ArrayList<>();
					
					for(BGRepositoryEntryRelation relation:relations) {
						String courseName = relation.getRepositoryEntryDisplayName();
						courseNames.add(courseName);
					}
					
					if(!courseNames.isEmpty()) {
						wrapper.setCourses(courseNames);
					}
				}
			}
		}
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("reservations", reservations);
		}
		
		for(ReservationWrapper reservation:reservations) {
			FormLink acceptLink = uifactory.addFormLink("accept_" + reservation.getKey(), "accept", null, formLayout, Link.BUTTON);
			acceptLink.setUserObject(reservation);
			acceptLink.setIconLeftCSS("o_icon o_icon_accept");
			FormLink rejectLink = uifactory.addFormLink("reject_" + reservation.getKey(), "reject", null, formLayout, Link.BUTTON);
			rejectLink.setUserObject(reservation);
			rejectLink.setIconLeftCSS("o_icon o_icon_reject");
			formLayout.add(acceptLink.getName(), acceptLink);
			formLayout.add(rejectLink.getName(), rejectLink);
		}

		
		// Button layout
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		
		if (showFormOK) {
			uifactory.addFormSubmitButton("submit", "ok", buttonLayout);
			uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		}
	}
	
	@Override
	public boolean isUserInteractionRequired(UserRequest ureq) {
		return reservations.size() > 0;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink && ((FormLink)source).getUserObject() instanceof ReservationWrapper) {
			ReservationWrapper reservation = (ReservationWrapper)((FormLink)source).getUserObject();
			if(source.getName().startsWith("accept_")) {
				reservation.setAccept(Boolean.TRUE);
			} else if (source.getName().startsWith("reject_")) {
				reservation.setAccept(Boolean.FALSE);
			}
			
			if (!showFormOK) {
				answerReservations(ureq);
				updateReservations(ureq, reservation);
				initForm(ureq);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		answerReservations(ureq);
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	private void answerReservations(UserRequest ureq) {		
		for(ReservationWrapper reservation:reservations) {
			if(reservation.getAccept() != null) {
				if(reservation.getAccept().booleanValue()) {
					acService.acceptReservationToResource(getIdentity(), reservation.getReservation());
				} else {
					acService.removeReservation(getIdentity(), getIdentity(), reservation.getReservation());
				}
			}
		}
	}
	
	private void updateReservations(UserRequest ureq, ReservationWrapper reservation) {
		reservations.remove(reservation);
		
		if (reservations.isEmpty()) {
			fireEvent (ureq, Event.DONE_EVENT);
		}
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT);
	}
	
	public boolean isEnrollmentAvailable() {
		return reservations != null && !reservations.isEmpty();
	}
	
	public static final class ReservationWrapper {
		private Boolean accept;
		private final ResourceReservation reservation;
		
		private String name;
		private String description;
		private List<String> courses;
		
		public ReservationWrapper(ResourceReservation reservation) {
			this.reservation = reservation;
		}
		
		public Long getKey() {
			return reservation.getKey();
		}
		
		public boolean isCoach() {
			return "group_coach".equals(reservation.getType()) || "repo_tutors".equals(reservation.getType());
		}

		public String getName() {
			return name == null ? "" : name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description == null ? "" : description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public boolean isGroupReservation() {
			// group participants and coaches
			return reservation.getType().startsWith("group_");
		}
		
		public boolean isRepositoryEntryReservation() {
			// repo participants and coaches
			return reservation.getType().startsWith("repo_");
		}
		
		public List<String> getCourses() {
			if(courses == null) {
				courses = new ArrayList<>(1);
			}
			return courses;
		}

		public void setCourses(List<String> courses) {
			this.courses = courses;
		}

		public ResourceReservation getReservation() {
			return reservation;
		}
		
		public boolean isAccepted() {
			return accept != null && accept.booleanValue();
		}
		
		public boolean isRefused() {
			return accept != null && !accept.booleanValue();
		}

		public Boolean getAccept() {
			return accept;
		}

		public void setAccept(Boolean accept) {
			this.accept = accept;
		}
	}
}
