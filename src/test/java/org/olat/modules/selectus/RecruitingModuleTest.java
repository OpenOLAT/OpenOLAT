/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.modules.selectus.model.MailSettingEnum;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.OrganisationUnitImpl;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 3 mai 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecruitingModuleTest extends OlatTestCase {
	
	@Autowired
	private RecruitingModule recruitingModule;
	
	
	@Test
	@Ignore //TODO selectus need organisation implementation
	public void getStaffMail_position_atPosition() {
		OrganisationUnit orgUnit = new OrganisationUnitImpl();
		orgUnit.setStaffMail("org-staff@frentix.com");
		orgUnit.setStaffBcc("org-staff-bcc@frentix.com");
		orgUnit.setSystemConfiguration(false);

		Position position = new PositionImpl();
		position.setOrganisationUnit(orgUnit);
		position.setSenderMail("sender-pos@frentix.com");
		position.setBccMail("sender-pos-bcc@frentix.com");
		position.setMailSetting(MailSettingEnum.position);

		String mail = recruitingModule.getStaffMail(position);
		Assert.assertEquals("sender-pos@frentix.com", mail);
		String bcc = recruitingModule.getBccStaffMail(position);
		Assert.assertEquals("sender-pos-bcc@frentix.com", bcc);
	}
	
	@Test
	@Ignore //TODO selectus need organisation implementation
	public void getStaffMail_position_atPosition_noBCC() {
		OrganisationUnit orgUnit = new OrganisationUnitImpl();
		orgUnit.setStaffMail("org-staff@frentix.com");
		orgUnit.setStaffBcc("org-staff-bcc@frentix.com");
		orgUnit.setSystemConfiguration(false);

		Position position = new PositionImpl();
		position.setOrganisationUnit(orgUnit);
		position.setSenderMail("sender-pos@frentix.com");
		position.setMailSetting(MailSettingEnum.position);

		String mail = recruitingModule.getStaffMail(position);
		Assert.assertEquals("sender-pos@frentix.com", mail);
		String bcc = recruitingModule.getBccStaffMail(position);
		Assert.assertNull(bcc);
	}
	
	@Test
	public void getStaffMail_organisation_noOrgBcc() {
		OrganisationUnit orgUnit = new OrganisationUnitImpl();
		orgUnit.setStaffMail("org-staff@frentix.com");
		orgUnit.setStaffBcc(null);
		orgUnit.setSystemConfiguration(false);

		Position position = new PositionImpl();
		position.setOrganisationUnit(orgUnit);
		position.setSenderMail("sender-pos@frentix.com");
		position.setBccMail("sender-pos-bcc@frentix.com");
		position.setMailSetting(MailSettingEnum.organisationUnit);

		String mail = recruitingModule.getStaffMail(position);
		Assert.assertEquals("org-staff@frentix.com", mail);
		String bcc = recruitingModule.getBccStaffMail(position);
		Assert.assertNull(bcc);
	}
	
	@Test
	@Ignore //TODO selectus need organisation implementation
	public void getStaffMail_org_system() {
		OrganisationUnit orgUnit = new OrganisationUnitImpl();
		orgUnit.setStaffMail("org-staff@frentix.com");
		orgUnit.setStaffBcc("org-staff-bcc@frentix.com");
		orgUnit.setSystemConfiguration(true);

		Position position = new PositionImpl();
		position.setOrganisationUnit(orgUnit);
		position.setSenderMail("sender-pos@frentix.com");
		position.setMailSetting(MailSettingEnum.organisationUnit);

		String mail = recruitingModule.getStaffMail(position);
		Assert.assertEquals("stephane.rosse@frentix.com", mail);
		String bcc = recruitingModule.getBccStaffMail(position);
		Assert.assertEquals("stephane.rosse.bcc@frentix.com", bcc);
	}
}
