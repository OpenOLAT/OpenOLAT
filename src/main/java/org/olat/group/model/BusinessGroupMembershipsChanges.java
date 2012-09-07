package org.olat.group.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Identity;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupMembershipsChanges {
	

	public List<Identity> addTutors = new ArrayList<Identity>();
	public List<Identity> removeTutors = new ArrayList<Identity>();
	public List<Identity> addParticipants = new ArrayList<Identity>();
	public List<Identity> removeParticipants = new ArrayList<Identity>();
	public List<Identity> addToWaitingList = new ArrayList<Identity>();
	public List<Identity> removeFromWaitingList = new ArrayList<Identity>();

}
