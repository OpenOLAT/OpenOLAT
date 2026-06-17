/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Notes;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class NotesDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private NotesDAO notesDao;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private OrganisationService organisationService;
	
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-app-cat-unit-test", "Org-app-cat-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void createNote() {
		Position pos = createRandomPosition(PositionStatus.closedAndInScreening);
		Application app = createRandomApplication(pos);
		Identity committeeMember = JunitTestHelper.createAndPersistIdentityAsRndUser("Committtee-1");
		dbInstance.commitAndCloseSession();
		
		Notes notes = notesDao.createNotes(app.getKey(), committeeMember, "Very good fighter");
		dbInstance.commitAndCloseSession();
		
		Assert.assertEquals("Very good fighter", notes.getContent());
		Assert.assertEquals(app.getKey(), notes.getApplicationKey());
		Assert.assertEquals(committeeMember, notes.getAuthor());
	}
	
	@Test
	public void createAndUpdateNotes() {
		Position pos = createRandomPosition(PositionStatus.closedAndInScreening);
		Application app = createRandomApplication(pos);
		Identity committeeMember = JunitTestHelper.createAndPersistIdentityAsRndUser("Committee-2");
		dbInstance.commitAndCloseSession();
		
		Notes notes = notesDao.createNotes(app.getKey(), committeeMember, "Interessant");
		dbInstance.commitAndCloseSession();
		//check
		Assert.assertEquals("Interessant", notes.getContent());
		Assert.assertEquals(app.getKey(), notes.getApplicationKey());
		Assert.assertEquals(committeeMember, notes.getAuthor());
		
		//reload and check
		Notes reloadedNotes = notesDao.getNotes(app.getKey(), committeeMember);
		Assert.assertEquals("Interessant", reloadedNotes.getContent());
		Assert.assertEquals(app.getKey(), reloadedNotes.getApplicationKey());
		Assert.assertEquals(committeeMember, reloadedNotes.getAuthor());
		
		//update
		Notes updatedNotes = notesDao.updateNotes(app.getKey(), committeeMember, "Too emotive");
		dbInstance.commitAndCloseSession();
		//check
		Assert.assertEquals("Too emotive", updatedNotes.getContent());
		Assert.assertEquals(app.getKey(), updatedNotes.getApplicationKey());
		Assert.assertEquals(committeeMember, updatedNotes.getAuthor());
		
		//reload and check
		Notes reloadUpdatedNotes = notesDao.getNotes(app.getKey(), committeeMember);
		Assert.assertEquals("Too emotive", reloadUpdatedNotes.getContent());
		Assert.assertEquals(app.getKey(), reloadUpdatedNotes.getApplicationKey());
		Assert.assertEquals(committeeMember, reloadUpdatedNotes.getAuthor());
	}
	
	@Test
	public void getNotes() {
		Position pos = createRandomPosition(PositionStatus.closedAndInScreening);
		Application app1 = createRandomApplication(pos);
		Application app2 = createRandomApplication(pos);
		Application app3 = createRandomApplication(pos);
		Identity committeeMember = JunitTestHelper.createAndPersistIdentityAsRndUser("Committee-3");
		dbInstance.commitAndCloseSession();
			
		Notes notes1 = notesDao.createNotes(app1.getKey(), committeeMember, "Interessant");
		Notes notes2 = notesDao.createNotes(app2.getKey(), committeeMember, "Interessant");
		Notes notes3 = notesDao.createNotes(app3.getKey(), committeeMember, "Interessant");
		dbInstance.commitAndCloseSession();
		
		//get the notes of the position
		List<Notes> notes = notesDao.getNotes(pos, committeeMember);
		Assert.assertNotNull(notes);
		Assert.assertEquals(3, notes.size());
		Assert.assertTrue(notes.contains(notes1));
		Assert.assertTrue(notes.contains(notes2));
		Assert.assertTrue(notes.contains(notes3));
	}
	
	@Test
	public void deleteNotes() {
		Position pos = createRandomPosition(PositionStatus.closedAndInScreening);
		Application app1 = createRandomApplication(pos);
		Application app2 = createRandomApplication(pos);
		Identity committeeMember = JunitTestHelper.createAndPersistIdentityAsRndUser("Committee-4");
		dbInstance.commitAndCloseSession();
		
		Notes notes1 = notesDao.createNotes(app1.getKey(), committeeMember, "Interessant");
		Notes notes2 = notesDao.createNotes(app2.getKey(), committeeMember, "Interessant");
		dbInstance.commitAndCloseSession();
		
		//get the notes of the position
		List<Notes> notes = notesDao.getNotes(pos, committeeMember);
		Assert.assertNotNull(notes);
		Assert.assertEquals(2, notes.size());
		Assert.assertTrue(notes.contains(notes1));
		
		//delete
		notesDao.deleteNotes(app1);
		dbInstance.commitAndCloseSession();
		
		//check
		Notes deletedNote = notesDao.getNotes(app1.getKey(), committeeMember);
		Assert.assertNull(deletedNote);
		Notes survivingNotes = notesDao.getNotes(app2.getKey(), committeeMember);
		Assert.assertNotNull(survivingNotes);
		Assert.assertEquals(notes2, survivingNotes);
	}
	

	private Application createRandomApplication(Position pos) {
		Application app = applicationDao.createApplication(pos);
		Person person = app.getPerson();
		person.setFirstName("Kanu " + UUID.randomUUID());
		person.setLastName("Unchou");
		person.setNationality("JP");
		person.setMail("kanu@ikki.co.jp");
		person.setPhone("9435892");
		person.setBirthday(new Date());
		return applicationDao.saveTempApplication(app, true);
	}
	
	private Position createRandomPosition(PositionStatus status) {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("AC-234");
		position.setPositionTitle("Technician in robotic");
		position.setShortTitle("Pilot of robot");
		position.setDepartment("NERV");
		position.setHomepage("http://www.nerv.co.jp");
		position.setApplicationDeadline(new Date());
		position.setStatus(status.name());
		position.setDescription("We search a young pilot for our semi-living robot.");
		return positionDao.savePosition(position);
	}
}