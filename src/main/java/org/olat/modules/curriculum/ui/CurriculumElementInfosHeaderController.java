/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementFileType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.ui.list.AbstractDetailsHeaderController;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.AccessResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 15, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementInfosHeaderController extends AbstractDetailsHeaderController {

	private final CurriculumElement element;
	private final boolean isMember;
	private String startLinkWarning;
	private String startLinkError;
	private final Identity identity;

	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private AccessControlModule acModule;

	public CurriculumElementInfosHeaderController(UserRequest ureq, WindowControl wControl, CurriculumElement element,
												  boolean isMember, Identity identity) {
		super(ureq, wControl);
		this.element = element;
		this.isMember = isMember;
		this.identity = identity;

		initForm(ureq);
	}

	@Override
	protected String getIconCssClass() {
		return "o_icon_curriculum_element";
	}
	
	@Override
	protected String getExternalRef() {
		return element.getIdentifier();
	}

	@Override
	protected String getTitle() {
		return element.getDisplayName();
	}

	@Override
	protected String getAuthors() {
		return element.getAuthors();
	}

	@Override
	protected String getTeaser() {
		return element.getTeaser();
	}

	@Override
	protected VFSLeaf getTeaserImage() {
		return curriculumService.getCurriculumElemenFile(element, CurriculumElementFileType.teaserImage);
	}

	@Override
	protected VFSLeaf getTeaserMovie() {
		return curriculumService.getCurriculumElemenFile(element, CurriculumElementFileType.teaserVideo);
	}

	@Override
	protected RepositoryEntryEducationalType getEducationalType() {
		return element.getEducationalType();
	}

	@Override
	protected void initAccess(UserRequest ureq, FormLayoutContainer layoutCont) {
		if (ureq.getUserSession().getRoles() == null) {
			initOffers(layoutCont, Boolean.TRUE);
			return;
		}
		
		if (isMember && acService.isAccessRefusedByStatus(element, identity)) {
			layoutCont.contextPut("warning", translate("access.denied.not.published"));
			layoutCont.contextPut("warningHint", translate("access.denied.not.published.hint"));
			
			startLink = createStartLink(layoutCont);
			startLink.setEnabled(false);
		} else if (isMember) {
			startLink = createStartLink(layoutCont);
		} else {
			initOffers(layoutCont, null);
		}
	}

	private void initOffers(FormLayoutContainer layoutCont, Boolean webPublish) {
		AccessResult acResult = acService.isAccessible(element, identity, isMember, false, webPublish, false);
		if (acResult.isAccessible()) {
			startLink = createStartLink(layoutCont);
		} else if (!acResult.getAvailableMethods().isEmpty()) {
			updateAccessMaxParticipants();
			
			if (acResult.getAvailableMethods().size() == 1 && acResult.getAvailableMethods().get(0).getOffer().isAutoBooking()) {
				startLink = createStartLink(layoutCont, true);
			} else {
				formatPrice(acResult);
				createGoToOffersLink(layoutCont, false);
			}
		}
	}
	
	private void updateAccessMaxParticipants() {
		if (isMember || element.getMaxParticipants() == null) {
			return;
		}
		
		Long numParticipants = curriculumService.getCurriculumElementKeyToNumParticipants(List.of(element), true).get(element.getKey());
		if (numParticipants != null) {
			if (numParticipants >= element.getMaxParticipants()) {
				startLinkError = "<i class=\"o_icon o_ac_offer_fully_booked_icon\"> </i> " + translate("book.fully.booked.unfortunately");
			} else {
				Double participantsLeftMessagePercentage = acModule.getParticipantsLeftMessagePercentage();
				if (participantsLeftMessagePercentage != null) {
					long leftParticipants = element.getMaxParticipants() - numParticipants;
					double leftParticipantsPercentage = leftParticipants * 100l / element.getMaxParticipants();
					if (leftParticipants == 1) {
						startLinkWarning = "<i class=\"o_icon o_ac_offer_almost_fully_booked_icon\"> </i> " + translate("book.participants.left.single");
					} else if (leftParticipantsPercentage < participantsLeftMessagePercentage) {
						startLinkWarning = "<i class=\"o_icon o_ac_offer_almost_fully_booked_icon\"> </i> " + translate("book.participants.left.multi", String.valueOf(leftParticipants));
					}
				}
			}
		}
	}
	
	@Override
	protected String getStartLinkText() {
		return translate("open.with.type", element.getType().getDisplayName());
	}

	@Override
	protected String getStartLinkWarning() {
		return startLinkWarning;
	}

	@Override
	protected String getStartLinkError() {
		return startLinkError;
	}

	@Override
	protected boolean tryAutoBooking(UserRequest ureq) {
		AccessResult acResult = acService.isAccessible(element, identity, null, false, null, false);
		return acService.tryAutoBooking(identity, element, acResult);
	}

	@Override
	protected Long getResourceKey() {
		return element.getResource().getKey();
	}

}
