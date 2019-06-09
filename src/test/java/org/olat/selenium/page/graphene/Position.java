/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.graphene;

import java.util.List;

import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 5 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Position {
	
	private final int x;
	private final int y;
	
	private Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public static Position valueOf(String coords, Dimension dimension, WebDriver browser) {
		List<Integer> coordList = AssessmentItemFactory.coordsList(coords);
		
		int x = 0;
		int y = 0;
		if(coordList.size() == 3) {// circle
			x = coordList.get(0).intValue();
			y = coordList.get(1).intValue();
		} else if(coordList.size() == 4) {// rectangle
			int x1 = coordList.get(0).intValue();
			int y1 = coordList.get(1).intValue();
			int x2 = coordList.get(2).intValue();
			int y2 = coordList.get(3).intValue();
			x = (x2 + x1) / 2;
			y = (y2 + y1) / 2;
		}

		x = x - Math.round(dimension.getWidth() / 2.0f);
		y = y - Math.round(dimension.getHeight() / 2.0f);
		return new Position(x, y);
	}
	
	public static Position valueOf(int x, int y, Dimension dimension, WebDriver browser) {
		x = x - Math.round(dimension.getWidth() / 2.0f);
		y = y - Math.round(dimension.getHeight() / 2.0f);
		return new Position(x, y);
	}
	
	public static Position valueOf(int x, int y, int firefoxCorrection, Dimension dimension, WebDriver browser) {
		x = x - Math.round(dimension.getWidth() / 2.0f);
		y = y - Math.round(dimension.getHeight() / 2.0f);
		x += firefoxCorrection;
		y += firefoxCorrection;
		return new Position(x, y);
	}
	
	public static Position valueOf(int x, int y, int width, int height, WebDriver browser) {
		x = x - Math.round(width / 2.0f);
		y = y - Math.round(height / 2.0f);
		return new Position(x, y);
	}

}
