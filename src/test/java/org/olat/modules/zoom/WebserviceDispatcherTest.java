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
package org.olat.modules.zoom;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class WebserviceDispatcherTest extends TestCase {

    @Test
    public void testZoomCalendarEvent() {
        ZoomCalendarEvent zoomCalendarEvent = new ZoomCalendarEvent();
        zoomCalendarEvent.init(
                "TestName",
                "<p><a href=\"https://us05web.zoom.us/j/88188039049?pwd=WWtZSlRMUVFZeTRlNmhETU9nUG9iZz09\" target=\"_blank\">Click here to join Zoom Meeting:881 8803 9049</a></p>",
                "TestContext",
                "1657189092",
                "5400"
                );
        Assert.assertEquals("88188039049", zoomCalendarEvent.getMeetingId());
    }
}