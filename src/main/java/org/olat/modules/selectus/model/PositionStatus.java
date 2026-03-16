/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.StringHelper;

public enum PositionStatus {
	preparation("o_preparation"),
	published("o_published"),
	publishedAndInScreening("o_published_screening_back"),
	closedAndInScreening("o_closed_screening_back"),
	closedAndNoRating("o_closed_no_rating"),
	closed("o_closed"),
	reporting("o_reporting");
	
	private final String css;
	
	private PositionStatus(String css) {
		this.css = css;
	}
	
	public String getCss() {
		return css;
	}
	
	public static List<PositionStatus> toList(PositionStatus... status) {
		List<PositionStatus> statusList = new ArrayList<>();
		if(status != null) {
			for(PositionStatus state:status) {
				if(state != null) {
					statusList.add(state);
				}
			}
		}
		return statusList;
	}
	
	public static PositionStatus[] valueOfArray(String status) {
		PositionStatus[] statusArr;
		if(StringHelper.containsNonWhitespace(status)) {
			String[] roleArr = status.split("[,]");
			statusArr = new PositionStatus[roleArr.length];
			for(int i=roleArr.length; i-->0; ) {
				statusArr[i] = PositionStatus.valueOf(roleArr[i]);
			}
		} else {
			statusArr = new PositionStatus[0];
		}
		return statusArr;
	}
}
