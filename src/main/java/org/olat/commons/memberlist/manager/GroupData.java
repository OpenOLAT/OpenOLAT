package org.olat.commons.memberlist.manager;

import org.olat.core.id.Identity;

import java.util.List;
import java.util.Map;

public class GroupData {
	private List<Identity> rows;
	private Map<Identity, StringBuilder> members;

	public GroupData(List<Identity> rows, Map<Identity, StringBuilder> members) {
		this.rows = rows;
		this.members = members;
	}

	public List<Identity> getRows() {
		return rows;
	}

	public Map<Identity, StringBuilder> getMembers() {
		return members;
	}

}