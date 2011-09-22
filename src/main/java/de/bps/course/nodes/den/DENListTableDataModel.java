package de.bps.course.nodes.den;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;

public class DENListTableDataModel extends DefaultTableDataModel {
	
	public static final String CHANGE_ACTION = "denPartsChange";
	public static final String MAIL_ACTION = "denSendMail";
	public static final String DELETE_ACTION = "denDeleteParticipants";
	
	// subject, date, location, comment, participants, usernames, action 
	private static final int COLUMN_COUNT = 7;
	private DENManager denManager;
	private Translator translator;

	public DENListTableDataModel(List objects, Translator translator) {
		super(objects);
		denManager = DENManager.getInstance();
		this.translator = translator;
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public Object getValueAt(int row, int col) {
		KalendarEvent event = (KalendarEvent)objects.get(row);
		
		switch (col) {
		case 0:
			//subject
			return denManager.format(event.getSubject());
		case 1:
			//begin
			Formatter formatter = Formatter.getInstance(translator.getLocale());
			String formattedDate = formatter.formatDateAndTime(event.getBegin());
			return denManager.format(formattedDate);
		case 2:
			//duration
			Date begin = event.getBegin();
			Date end = event.getEnd();
			long milliSeconds = denManager.getDuration(begin, end);
			
			return denManager.formatDuration(milliSeconds, translator);
		case 3:
			//location
			return denManager.format(event.getLocation());
		case 4:
			//comment
			return event.getComment();
		case 5:
			//participants
			StringBuilder names = new StringBuilder("");
			if(event.getParticipants() == null || event.getParticipants().length < 1)
				return "";
			for( String participant : event.getParticipants() ) {
				Identity identity = BaseSecurityManager.getInstance().findIdentityByName(participant);
				User user = identity.getUser();
				names.append(user.getProperty(UserConstants.LASTNAME, getLocale()) + ", " + user.getProperty(UserConstants.FIRSTNAME, getLocale()) + "<br>");
			}
			return names.toString();
		case 6:
			//usernames
			StringBuilder usernames = new StringBuilder("");
			if(event.getParticipants() == null || event.getParticipants().length < 1)
				return "";
			for( String participant : event.getParticipants() ) {
				Identity identity = BaseSecurityManager.getInstance().findIdentityByName(participant);
				usernames.append(identity.getName() + "<br>");
			}
			return usernames.toString();
		case 7:
			//action
			return Boolean.TRUE;

		default:	return "error";
		}
	}

}
