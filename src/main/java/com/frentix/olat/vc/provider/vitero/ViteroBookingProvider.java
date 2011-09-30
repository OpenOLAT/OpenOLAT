package com.frentix.olat.vc.provider.vitero;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.logging.LogDelegator;

import de.bps.course.nodes.vc.VCConfiguration;
import de.bps.course.nodes.vc.provider.VCProvider;

/**
 * 
 * Description:<br>
 * Implementation of the Virtual Classroom for the Vitero Booking System
 * 
 * <P>
 * Initial Date:  26 sept. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroBookingProvider extends LogDelegator implements VCProvider {
	
	private boolean enabled;

	@Override
	public VCProvider newInstance() {
		return new ViteroBookingProvider();
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String getProviderId() {
		return "vitero";
	}

	@Override
	public String getDisplayName() {
		return "Vitero";
	}

	@Override
	public Map<String, String> getTemplates() {
		return new HashMap<String,String>();
	}

	@Override
	public boolean isProviderAvailable() {
		return true;
	}

	@Override
	public boolean createClassroom(String roomId, String name, String description, Date begin, Date end, VCConfiguration config) {
		return false;
	}

	@Override
	public boolean updateClassroom(String roomId, String name, String description, Date begin, Date end, VCConfiguration config) {
		return false;
	}

	@Override
	public boolean removeClassroom(String roomId, VCConfiguration config) {
		return false;
	}

	@Override
	public URL createClassroomUrl(String roomId, Identity identity, VCConfiguration config) {
		return null;
	}

	@Override
	public URL createClassroomGuestUrl(String roomId, Identity identity, VCConfiguration config) {
		return null;
	}

	@Override
	public boolean existsClassroom(String roomId, VCConfiguration config) {
		return false;
	}

	@Override
	public boolean login(Identity identity, String password) {
		return false;
	}

	@Override
	public boolean createModerator(Identity identity, String roomId) {
		return false;
	}

	@Override
	public boolean createUser(Identity identity, String roomId) {
		return false;
	}

	@Override
	public boolean createGuest(Identity identity, String roomId) {
		return false;
	}

	@Override
	public Controller createDisplayController(UserRequest ureq, WindowControl wControl, String roomId, String name, String description,
			boolean isModerator, VCConfiguration config) {
		return new ViteroDisplayController(ureq, wControl, roomId, name, description, isModerator, (ViteroBookingConfiguration)config, this);
	}

	@Override
	public Controller createConfigController(UserRequest ureq, WindowControl wControl, String roomId, VCConfiguration config) {
		return new ViteroConfigController(ureq, wControl, roomId, this, (ViteroBookingConfiguration)config);
	}

	@Override
	public VCConfiguration createNewConfiguration() {
		return new ViteroBookingConfiguration();
	}

	
}
