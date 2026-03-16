/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Notes;
import org.olat.modules.selectus.model.NotesImpl;
import org.olat.modules.selectus.model.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("notesDAO")
public class NotesDAO {

	@Autowired
	private DB dbInstance;

	public Notes createNotes(Long applicationKey, Identity identity, String content) {
		NotesImpl notes = new NotesImpl();
		notes.setCreationDate(new Date());
		notes.setApplicationKey(applicationKey);
		notes.setAuthor(identity);
		notes.setContent(content);
		dbInstance.getCurrentEntityManager().persist(notes);
		return notes;
	}

	public Notes getNotes(Long applicationKey, Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select notes from rnotes notes")
		  .append(" where notes.applicationKey=:applicationKey and notes.author.key=:authorKey");
		
		List<Notes> notes = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Notes.class)
			.setParameter("applicationKey", applicationKey)
			.setParameter("authorKey", identity.getKey()).getResultList();
		if(notes.isEmpty()) return null;
		return notes.get(0);
	}

	public void deleteNotes(Application application) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from rnotes notes")
		  .append(" where notes.applicationKey=:applicationKey");
		
		dbInstance.getCurrentEntityManager().createQuery(sb.toString())
			.setParameter("applicationKey", application.getKey())
			.executeUpdate();
	}
	
	public List<Notes> getNotes(Position position, Identity identity) {
		String query = """
				select notes from rnotes notes
				where notes.author.key=:authorKey and 
				notes.applicationKey in (
				  select app.key from rapplicationlight app where app.positionKey =:positionKey
				)""";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Notes.class)
				.setParameter("positionKey", position.getKey())
				.setParameter("authorKey", identity.getKey())
				.getResultList();
	}

	public Notes updateNotes(Long applicationKey, Identity identity, String content) {
		Notes notes = getNotes(applicationKey, identity);
		notes.setContent(content);
		return dbInstance.getCurrentEntityManager().merge(notes);
	}

}
