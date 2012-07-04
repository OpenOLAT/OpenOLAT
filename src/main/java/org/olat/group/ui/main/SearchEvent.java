package org.olat.group.ui.main;

import org.olat.core.gui.control.Event;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SearchEvent extends Event {

	private static final long serialVersionUID = 6630250536374073143L;
	
	private Long id;
	private String name;
	private String description;
	private String ownerName;
	private boolean owner;
	private boolean attendee;
	private boolean waiting;
	public boolean publicGroups;
	
	public SearchEvent() {
		super("search");
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String owner) {
		this.ownerName = owner;
	}

	public boolean isOwner() {
		return owner;
	}

	public void setOwner(boolean owner) {
		this.owner = owner;
	}

	public boolean isAttendee() {
		return attendee;
	}

	public void setAttendee(boolean attendee) {
		this.attendee = attendee;
	}

	public boolean isWaiting() {
		return waiting;
	}

	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}

	public boolean isPublicGroups() {
		return publicGroups;
	}

	public void setPublicGroups(boolean publicGroups) {
		this.publicGroups = publicGroups;
	}
}
