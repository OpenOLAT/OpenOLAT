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

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.IdentityOrganisationsRow;
import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.ui.member.MemberDetailsController;
import org.olat.modules.curriculum.ui.member.MembershipModification;
import org.olat.modules.curriculum.ui.member.ModificationStatus;
import org.olat.modules.curriculum.ui.member.ModificationStatusSummary;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.ui.BillingAddressCellRenderer.BillingAddressCellValue;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 6 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserRow extends UserPropertiesRow implements IdentityOrganisationsRow, BillingAddressCellValue {

	private String roles;
	private final Identity identity;
	private List<OrganisationWithParents> organisations;
	private ModificationStatus modificationStatus;
	private List<MembershipModification> modifications;
	private ModificationStatusSummary modificationSummary;
	private BillingAddress billingAddress;
	
	private FormLink toolsLink;
	
	private MemberDetailsController detailsCtrl;
	
	public UserRow(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		this.identity = identity;
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	public List<OrganisationWithParents> getOrganisations() {
		return organisations;
	}

	@Override
	public void setOrganisations(List<OrganisationWithParents> organisations) {
		this.organisations = organisations;
	}

	public List<MembershipModification> getModifications() {
		return modifications;
	}

	public void setModifications(List<MembershipModification> modifications) {
		this.modifications = modifications;
	}

	public ModificationStatus getModificationStatus() {
		return modificationStatus;
	}

	public void setModificationStatus(ModificationStatus modificationStatus) {
		this.modificationStatus = modificationStatus;
	}

	public ModificationStatusSummary getModificationSummary() {
		return modificationSummary;
	}

	public void setModificationSummary(ModificationStatusSummary modificationSummary) {
		this.modificationSummary = modificationSummary;
	}

	public BillingAddress getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(BillingAddress billingAddress) {
		this.billingAddress = billingAddress;
	}

	@Override
	public boolean isBillingAddressAvailable() {
		return billingAddress != null ;
	}

	@Override
	public boolean isBillingAddressProposal() {
		return billingAddress != null && (billingAddress.getOrganisation() == null && billingAddress.getIdentity() == null);
	}

	@Override
	public String getBillingAddressIdentifier() {
		return billingAddress != null? billingAddress.getIdentifier(): null;
	}
	
	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	public boolean isDetailsControllerAvailable() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().isVisible();
		}
		return false;
	}

	public MemberDetailsController getDetailsController() {
		return detailsCtrl;
	}
	
	public String getDetailsControllerName() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().getComponent().getComponentName();
		}
		return null;
	}
	
	public void setDetailsController(MemberDetailsController detailsCtrl) {
		this.detailsCtrl = detailsCtrl;
	}
}
