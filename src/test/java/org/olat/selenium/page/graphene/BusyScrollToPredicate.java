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

import java.util.function.Function;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Observe the scrolling flag of the o_scrollToElement method.
 * 
 * Initial date: 23.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusyScrollToPredicate implements Function<WebDriver,Boolean> {
	
	private int count = 0;
	private Number y;

	@Override
	public Boolean apply(WebDriver driver) {
        y = (Number)((JavascriptExecutor)driver).executeScript("return (((typeof window.o_info === 'undefined') || window.o_info.linkbusy) ? -1 : window.pageYOffset);");
        if(y.intValue() != 0) {
        	count = 0;
        } else if(y.intValue() == 0) {
        	count++;
        }
        return count > 3;
    }
	
	public Number getY() {
		return y;
	}
}
