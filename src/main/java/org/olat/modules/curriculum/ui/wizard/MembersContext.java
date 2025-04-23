/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.mail.MailTemplate;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.ui.member.ConfirmationMembershipEnum;
import org.olat.modules.curriculum.ui.member.MembershipModification;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.model.OrderAdditionalInfos;

/**
 * 
 * Initial date: 6 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MembersContext {
	
	private List<Identity> searchedIdentities;
	private List<Identity> selectedIdentities;
	private Map<Long, List<OrganisationWithParents>> identityKeyToUserOrganisations;
	
	private final Curriculum curriculum;
	private final CurriculumElement curriculumElement;
	private final List<CurriculumElement> descendants;
	
	private final List<Offer> offers;
	private AccessInfos selectedOffer;
	private boolean needBillingAddress;
	private BillingAddress billingAddress;
	private Map<Long, BillingAddress> identityKeyToBillingAddress;
	private String purchaseOrderNumber;
	private String orderComment;
	
	private boolean addAsTeacher;

	private List<MembershipModification> modifications;
	
	private String adminNote;

	private final CurriculumRoles roleToModify;
	
	private MailTemplate mailTemplate;
	
	public MembersContext(CurriculumRoles roleToModify, Curriculum curriculum,
			CurriculumElement curriculumElement, List<CurriculumElement> descendants,
			List<Offer> offers) {
		this.offers = offers == null ? List.of() : new ArrayList<>(offers);
		this.roleToModify = roleToModify;
		this.curriculum = curriculum;
		this.curriculumElement = curriculumElement;
		this.descendants = descendants == null ? List.of() : new ArrayList<>(descendants);
	}
	
	public CurriculumRoles getRoleToModify() {
		return roleToModify;
	}

	public List<Identity> getSearchedIdentities() {
		return searchedIdentities;
	}

	public void setSearchedIdentities(List<Identity> searchedIdentities) {
		this.searchedIdentities = searchedIdentities;
	}

	public List<Identity> getSelectedIdentities() {
		return selectedIdentities;
	}

	public void setSelectedIdentities(List<Identity> selectedIdentities) {
		this.selectedIdentities = selectedIdentities;
	}

	public Map<Long, List<OrganisationWithParents>> getIdentityKeyToUserOrganisations() {
		return identityKeyToUserOrganisations;
	}

	public void setIdentityKeyToUserOrganisations(Map<Long, List<OrganisationWithParents>> identityKeyToUserOrganisations) {
		this.identityKeyToUserOrganisations = identityKeyToUserOrganisations;
	}

	public Curriculum getCurriculum() {
		return curriculum;
	}

	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	public List<CurriculumElement> getDescendants() {
		return descendants;
	}
	
	public List<CurriculumElement> getAllCurriculumElements() {
		List<CurriculumElement> elements = new ArrayList<>();
		elements.add(curriculumElement);
		if(descendants != null && !descendants.isEmpty()) {
			elements.addAll(descendants);
		}
		return elements;
	}
	
	public List<OLATResource> getAllCurriculumElementResources() {
		List<OLATResource> resources = new ArrayList<>();
		if(curriculumElement.getResource() != null) {	
			resources.add(curriculumElement.getResource());
		}
		if(descendants != null && !descendants.isEmpty()) {
			for(CurriculumElement descendant:descendants) {
				if(descendant.getResource() != null) {
					resources.add(descendant.getResource());
				}
			}
		}
		return resources;
	}
	
	public List<Offer> getOffers() {
		return offers;
	}
	
	public boolean isOffersAvailable() {
		return offers != null && !offers.isEmpty();
	}

	public AccessInfos getSelectedOffer() {
		return selectedOffer;
	}

	public void setSelectedOffer(AccessInfos selectedOffer) {
		this.selectedOffer = selectedOffer;
	}

	public String getPurchaseOrderNumber() {
		return purchaseOrderNumber;
	}

	public void setPurchaseOrderNumber(String purchaseOrderNumber) {
		this.purchaseOrderNumber = purchaseOrderNumber;
	}

	public String getOrderComment() {
		return orderComment;
	}

	public void setOrderComment(String orderComment) {
		this.orderComment = orderComment;
	}
	
	public boolean isNeedBillingAddress() {
		return needBillingAddress;
	}

	public void setNeedBillingAddress(boolean needBillingAddress) {
		this.needBillingAddress = needBillingAddress;
	}

	public BillingAddress getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(BillingAddress billingAddress) {
		this.billingAddress = billingAddress;
	}
	
	public Map<Long, BillingAddress> getIdentityKeyToBillingAddress() {
		return identityKeyToBillingAddress;
	}

	public void setIdentityKeyToBillingAddress(Map<Long, BillingAddress> identityKeyToBillingAddress) {
		this.identityKeyToBillingAddress = identityKeyToBillingAddress;
	}

	/**
	 * @param identity 
	 * @param hasCode Has access code for Token Access Offer?
	 * @return
	 */
	public OrderAdditionalInfos createOrderInfos(Identity identity, boolean hasCode) {
		BillingAddress orderBillingAddress = null;
		if (needBillingAddress) {
			if (identityKeyToBillingAddress != null) {
				orderBillingAddress = identityKeyToBillingAddress.get(identity.getKey());
			}
			if (orderBillingAddress == null) {
				orderBillingAddress = billingAddress;
			}
		}
		return new OrderAdditionalInfos(purchaseOrderNumber, orderComment, orderBillingAddress, hasCode);
	}

	public boolean hasModifications() {
		return modifications != null && !modifications.isEmpty();
	}
	
	public boolean hasModificationsWithConfirmation() {
		if(modifications != null && !modifications.isEmpty()) {
			for(MembershipModification modification:modifications) {
				if(modification.confirmation() == ConfirmationMembershipEnum.WITH) {
					return true;
				}
			}
		}
		return false;
	}

	public List<MembershipModification> getModifications() {
		return modifications;
	}

	public void setModifications(List<MembershipModification> modifications) {
		this.modifications = modifications;
	}
	
	public boolean isAddAsTeacher() {
		return addAsTeacher;
	}

	public void setAddAsTeacher(boolean addAsTeacher) {
		this.addAsTeacher = addAsTeacher;
	}

	public String getAdminNote() {
		return adminNote;
	}

	public void setAdminNote(String adminNote) {
		this.adminNote = adminNote;
	}

	public MailTemplate getMailTemplate() {
		return mailTemplate;
	}

	public void setMailTemplate(MailTemplate mailTemplate) {
		this.mailTemplate = mailTemplate;
	}
	
	public record AccessInfos(Offer offer, OfferAccess offerAccess, List<Organisation> organisations) {
		//
	}
}
