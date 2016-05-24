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

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import com.google.common.base.Predicate;

/**
 * 
 * Observe the scrolling flag of the o_scrollToElement method.
 * 
 * Initial date: 23.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ScrollToPredicate implements Predicate<WebDriver> {
	
	private boolean started = false;
	
	@Override
	public boolean apply(WebDriver driver) {
        Object busy = ((JavascriptExecutor)driver)
        		.executeScript("return (window.o_info.scrolling)");
        if(started) {
        	return Boolean.FALSE.equals(busy);
        }
        if(Boolean.TRUE.equals(busy)) {
        	started = true; //start to scroll
        }
        return false;
    }
}
