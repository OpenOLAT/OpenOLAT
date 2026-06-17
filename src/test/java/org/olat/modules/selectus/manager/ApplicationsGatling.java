/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.AcademicalBackground;
import org.olat.modules.selectus.model.Address;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.BusinessInformations;
import org.olat.modules.selectus.model.BusinessInformationsImpl;
import org.olat.modules.selectus.model.HighestDegreeType;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.PersonMaritalStatus;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Project;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationsGatling extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private RecruitingService recruitingFrontendManager;
	
	@Test
	@Ignore
	public void fillPosition() {
		int numOfApplications = 1000;
		Long positionKey = 207552512l;

		Position position = positionDao.loadPositionByKey(positionKey);
		List<Identity> committeeMembers = recruitingFrontendManager.getCommitteeMembers(position);

		List<Application> apps = new ArrayList<>();
		for(int i=0; i<numOfApplications; i++) {
			Application app = createApplication(position, i);
			apps.add(app);
			
			for(int j=0; j<committeeMembers.size(); j++) {
				int rating = (j + i) % 4;
			
				try {
					recruitingFrontendManager.setRating(app, committeeMembers.get(j), rating);
				} catch (RatingClosedException e) {
					e.printStackTrace();
				}
			}
			
		}
		dbInstance.commitAndCloseSession();
	}
	
	private Application createApplication(Position position, int i) {
		Application app = applicationDao.createApplication(position);
		
		Person person = app.getPerson();
		person.setFirstName("Rei_" + i);
		person.setLastName("Ayanami_" + i);
		person.setNationality("JP_" + i);
		person.setMail("rei." + i +"@nerv.co.jp");
		person.setPhone("" + 9435892 + i);
		person.setBirthday(new Date());
		person.setAcademicTitle("Dr.");
		person.setDisability(Boolean.FALSE);
		person.setMaritalStatus(PersonMaritalStatus.values()[ i % 5 ].name());
		if(i % 2 == 0) {
			person.setGender("m");
		} else {
			person.setGender("f");
		}
		
		Address add = app.getAddress();
		add.setAddressLine1("My private address " + i);
		add.setAddressLine2("A very little road " + i);
		add.setZipCode("405" + i);
		add.setCity("Tokyo");
		add.setCountry("Japan");
		
		BusinessInformations bi = app.getBusinessInformations();
		((BusinessInformationsImpl)bi).setAffiliation("NERV");
		bi.setOrganization("Neon genesis evangelion");
		bi.setUnit("Research and development");
		bi.setCurrentPosition("Pilot");

		AcademicalBackground bg = app.getAcademicalBackground();
		bg.setHighestDegreeType(HighestDegreeType.master.name());
		bg.setHighestDegreeDate(new Date());
		bg.setHighestDegreeInstitution("NERV Institut");
		
		bg.setCitations(i % 20);
		bg.setDissertationDate(new Date());
		bg.setDissertationInstitution("Very good " + i + " but very bright institution");
		bg.setDissertationKeyword1(UUID.randomUUID().toString() + " " + UUID.randomUUID().toString());
		bg.setDissertationKeyword2(UUID.randomUUID().toString() + " " + UUID.randomUUID().toString());
		bg.setDissertationKeyword3(UUID.randomUUID().toString() + " " + UUID.randomUUID().toString());
		
		bg.setDissertationTitle("A very long title to be in " + i + " memory for a long time. Or not. I don't know really why?");
		bg.setHFactor((i % 100) * 1.0d); 
		bg.setCareerDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis elementum semper viverra. Sed ut elit vitae urna lacinia eleifend ut id ipsum. Mauris condimentum leo et tortor posuere dignissim. Curabitur elementum dui neque, sit amet sodales nisi accumsan vel. Nam convallis lacus ante, et mollis arcu sagittis laoreet. Sed suscipit, " + i + " ligula vitae blandit aliquet, risus quam vehicula enim, ut commodo nibh metus non lectus. Curabitur a tincidunt mauris. Maecenas mattis ultrices purus, sed sodales dolor mattis nec.");
		bg.setHabilitationDate(new Date());
		bg.setHabilitationInstitution("Unversity of ABC at " + i + " randomized factor");
		bg.setHighestDegreeDate(new Date());
		bg.setHighestDegreeDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis elementum semper viverra. Sed ut elit vitae urna lacinia eleifend ut id ipsum. Mauris condimentum leo et tortor posuer " + UUID.randomUUID());
		bg.setNumberOfFirstAuthorships(i % 18);
		bg.setNumberOfLastAuthorships(i % 20 + 1);
		bg.setNumberOfOriginalPublications(i % 17 + 1);
		bg.setOrcid(UUID.randomUUID().toString());
		bg.setWorkedInAcademiaSince("20" + (i % 20));
		bg.setWorkedOutAcademiaCareSince("20" + (i % 20));
		
		app.setCommitteeComment("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis elementum semper viverra. Sed ut elit vitae urna lacinia eleifend ut id ipsum. Mauris condimentum leo et tortor posuere dignissim. Curabitur elementum dui neque, sit amet sodales nisi accumsan vel. Nam convallis lacus ante, et mollis arcu sagittis laoreet. Sed suscipit, " + UUID.randomUUID() + " ligula vitae blandit aliquet, risus quam vehicula enim, ut commodo nibh metus non lectus. Curabitur a tincidunt mauris. Maecenas mattis ultrices purus, sed sodales dolor mattis nec.");
		
		Project project = app.getProject();
		project.setAcronym(UUID.randomUUID().toString());
		project.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis elementum semper viverra. Sed ut elit vitae urna lacinia eleifend ut id ipsum. Mauris condimentum leo et tortor posuere dignissim. Curabitur elementum dui neque, sit amet sodales nisi accumsan vel. Nam convallis lacus ante, et mollis arcu sagittis laoreet. Sed suscipit, " + UUID.randomUUID() + " ligula vitae blandit aliquet, risus quam vehicula enim, ut commodo nibh metus non lectus. Curabitur a tincidunt mauris. Maecenas mattis ultrices purus, sed sodales dolor mattis nec.");
		project.setFinancialImpact1("100k");
		project.setFinancialImpact2("100'000.00");
		project.setFinancialImpact3("2000km");
		project.setFinancialImpact4("A few millions");
		project.setFinancialImpact5("A billions $$$$");
		project.setKeywords(UUID.randomUUID().toString() + " " + UUID.randomUUID().toString());
		
		app = applicationDao.saveTempApplication(app, true);
		dbInstance.commit();
		return app;
	}
	
	

}
