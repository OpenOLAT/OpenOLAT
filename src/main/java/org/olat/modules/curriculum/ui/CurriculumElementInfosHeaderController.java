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
	private final Identity bookedIdentity;

	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private AccessControlModule acModule;

	public CurriculumElementInfosHeaderController(UserRequest ureq, WindowControl wControl, CurriculumElement element,
			boolean isMember, Identity bookedIdentity) {
		super(ureq, wControl);
		this.element = element;
		this.isMember = isMember;
		this.bookedIdentity = bookedIdentity;
		
		init(ureq);
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
	protected void initAccess(UserRequest ureq) {
		if (ureq.getUserSession().getRoles() == null) {
			initOffers(ureq, Boolean.TRUE);
			return;
		}
		
		if (isMember && acService.isAccessRefusedByStatus(element, bookedIdentity)) {
			startCtrl.getInitialComponent().setVisible(true);
			startCtrl.getStartLink().setEnabled(false);
			setWarning(translate("access.denied.not.published"), translate("access.denied.not.published.hint"));
		} else if (isMember) {
			startCtrl.getInitialComponent().setVisible(true);
		} else {
			initOffers(ureq, null);
		}
	}

	private void initOffers(UserRequest ureq, Boolean webPublish) {
		AccessResult acResult = acService.isAccessible(element, bookedIdentity, isMember, false, webPublish, false);
		if (acResult.isAccessible()) {
			startCtrl.getInitialComponent().setVisible(true);
		} else if (!acResult.getAvailableMethods().isEmpty()) {
			ParticipantsLeftResult result = updateAccessMaxParticipants();
			if (result.left == ParticipantsLeft.fullyBooked) {
				startCtrl.getInitialComponent().setVisible(true);
				startCtrl.getStartLink().setEnabled(false);
				startCtrl.getStartLink().setIconRightCSS(null);
				startCtrl.getStartLink().setCustomDisplayText(translate("book"));
				startCtrl.setError(result.message);
				return;
			}
			
			if (acResult.getAvailableMethods().size() == 1 && acResult.getAvailableMethods().get(0).getOffer().isAutoBooking()) {
				startCtrl.getInitialComponent().setVisible(true);
				startCtrl.setAutoBooking(true);
				if (result.left == ParticipantsLeft.almostFullyBooked) {
					startCtrl.setWarning(result.message);
				}
			} else {
				showOffers(ureq, acResult.getAvailableMethods(), false, webPublish != null && webPublish, bookedIdentity);
				if (result.left == ParticipantsLeft.almostFullyBooked) {
					if (offersCtrl != null) {
						offersCtrl.setWarning(result.message);
					}
				}
			}
		}
	}
	
	private ParticipantsLeftResult updateAccessMaxParticipants() {
		if (isMember || element.getMaxParticipants() == null) {
			return new ParticipantsLeftResult(ParticipantsLeft.many, null);
		}
		
		Long numParticipants = curriculumService.getCurriculumElementKeyToNumParticipants(List.of(element), true).get(element.getKey());
		if (numParticipants != null) {
			if (numParticipants >= element.getMaxParticipants()) {
				return new ParticipantsLeftResult(ParticipantsLeft.fullyBooked,
						"<i class=\"o_icon o_ac_offer_fully_booked_icon\"> </i> " + translate("book.fully.booked.unfortunately"));
			}
			Double participantsLeftMessagePercentage = acModule.getParticipantsLeftMessagePercentage();
			if (participantsLeftMessagePercentage != null) {
				long leftParticipants = element.getMaxParticipants() - numParticipants;
				double leftParticipantsPercentage = leftParticipants * 100l / element.getMaxParticipants();
				if (leftParticipants == 1) {
					return new ParticipantsLeftResult(ParticipantsLeft.almostFullyBooked,
							"<i class=\"o_icon o_ac_offer_almost_fully_booked_icon\"> </i> " + translate("book.participants.left.single"));
				} else if (leftParticipantsPercentage < participantsLeftMessagePercentage) {
					return new ParticipantsLeftResult(ParticipantsLeft.almostFullyBooked,
							"<i class=\"o_icon o_ac_offer_almost_fully_booked_icon\"> </i> " + translate("book.participants.left.multi", String.valueOf(leftParticipants)));
				}
			}
		}
		
		return new ParticipantsLeftResult(ParticipantsLeft.many, null);
	}
	
	private record ParticipantsLeftResult(ParticipantsLeft left, String message) {}
	private enum ParticipantsLeft { fullyBooked, almostFullyBooked, many }

	@Override
	protected String getStartLinkText() {
		return translate("open.with.type", element.getType().getDisplayName());
	}

	@Override
	protected boolean tryAutoBooking(UserRequest ureq) {
		AccessResult acResult = acService.isAccessible(element, bookedIdentity, null, false, null, false);
		return acService.tryAutoBooking(bookedIdentity, element, acResult);
	}

	@Override
	protected Long getResourceKey() {
		return element.getResource().getKey();
	}
	
}
