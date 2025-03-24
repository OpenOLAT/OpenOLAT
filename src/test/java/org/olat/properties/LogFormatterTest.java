/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.properties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

/**
 * Initial date: Mar 05, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */

public class LogFormatterTest {

	@Test
	public void testParseLogValidEntries() {
		String log = """
				Date: 2024-01-18 10:56
				Identity: OpenOLAT Administrator (360448) (coach)
				passed set to: true
				-------------------------------------------------------------------
				Date: 2024-01-18 10:56
				Identity: OpenOLAT Administrator (360448) (coach)
				assessmentId set to: 2711
				-------------------------------------------------------------------
				Date: 2024-01-18 10:56
				Identity: OpenOLAT Administrator (360448) (coach)
				COMMENT set to: Super
				-------------------------------------------------------------------
				Date: 2024-01-18 10:59
				Identity: Anna Schmidt (456789123) (student)
				submission set to: uploaded
				-------------------------------------------------------------------
				Date: 2024-01-18 11:00
				Identity: Michael Berger (567890234) (coach)
				review set to: completed
				-------------------------------------------------------------------
				Date: 2024-01-18 11:01
				Identity: Lisa Meier (678901345) (student)
				visibility set to: visible
				-------------------------------------------------------------------
				Date: 2024-01-18 11:02
				Identity: Jonas Keller (789012456) (coach)
				attempts set to: 3
				-------------------------------------------------------------------
				Date: 2024-01-18 11:03
				Identity: Sarah Fischer (890123567) (student)
				grade set to: A
				-------------------------------------------------------------------
				Date: 2024-01-18 11:04
				Identity: David Braun (901234678) (coach)
				feedback set to: Excellent work!
				-------------------------------------------------------------------
				Date: 2024-01-18 11:05
				Identity: Julia Hoffmann (123456789) (student)
				reset course element
				-------------------------------------------------------------------
				Date: 2024-07-11 12:58
				Identity: Arnold Kieser (424116228) (coach)
				Deadline extension for assignment of 1408270341: Standard date 11.07.2024, 13:00 to 11.07.2024, 13:05
				-------------------------------------------------------------------
				Date: 2023-10-24 14:55
				Identity: Simone Langenegger (424116224)
				Submit of Aufgabe_2.docx: upload document Simone-Langenegger-2023-10-24-14-55-52.mp4
				-------------------------------------------------------------------
				Date: 2023-09-19 16:01
				Identity: Arnold Hoffmann (32079872) (coach)
				Back to submission of Arnold-Hoffmann-2023-09-19-15-53-07.m4a: revert status of task back to submission
				-------------------------------------------------------------------
				Date: 2022-04-14 10:24
				Identity: Simone Hoffmann (13991936)
				COMMENT set to: ksadfjg\r
				sliajsdf\r
				lksdjliasdjfli
				-------------------------------------------------------------------
				Date: 2015-11-04 14:41
				User: mhoffmann
				COMMENT set to: Herzlichen Glückwunsch! Sie haben mit 13 von 18 Punkten bestanden.
				
				Viele Grüße
				Max Hoffmann
				-------------------------------------------------------------------
				Date: 2023-09-19 16:07
				Identity: Max Doe (OrgUnit) (15845124) (coach)
				attempts set to: 4
				-------------------------------------------------------------------
				Date: 2025-03-21 14:43
				Identity: OpenOLAT Administrator (360448) (coach)
				score set to: null
				-------------------------------------------------------------------
				Date: 2025-03-21 14:43
				Identity: OpenOLAT Administrator (360448) (coach)
				passed set to: false
				""";

		LogFormatter logFormatter = new LogFormatter();
		List<LogEntry> entries = logFormatter.parseLog(log).validEntries();
		// expecting 13 instead of 16, because some entries get grouped
		Assert.assertEquals(15, entries.size());

		Assert.assertEquals("OpenOLAT Administrator", entries.get(0).author());
		Assert.assertEquals("coach", entries.get(0).role());
		Assert.assertEquals("passed set to\nassessmentId set to\nCOMMENT set to", entries.get(0).action());
		Assert.assertEquals("true\n2711\nSuper", entries.get(0).details());

		Assert.assertEquals("Anna Schmidt", entries.get(1).author());
		Assert.assertEquals("submission set to", entries.get(1).action());
		Assert.assertEquals("uploaded", entries.get(1).details());

		Assert.assertEquals("Michael Berger", entries.get(2).author());
		Assert.assertEquals("review set to", entries.get(2).action());
		Assert.assertEquals("completed", entries.get(2).details());

		Assert.assertEquals("Lisa Meier", entries.get(3).author());
		Assert.assertEquals("visibility set to", entries.get(3).action());
		Assert.assertEquals("visible", entries.get(3).details());

		Assert.assertEquals("Jonas Keller", entries.get(4).author());
		Assert.assertEquals("attempts set to", entries.get(4).action());
		Assert.assertEquals("3", entries.get(4).details());

		Assert.assertEquals("Sarah Fischer", entries.get(5).author());
		Assert.assertEquals("grade set to", entries.get(5).action());
		Assert.assertEquals("A", entries.get(5).details());

		Assert.assertEquals("David Braun", entries.get(6).author());
		Assert.assertEquals("feedback set to", entries.get(6).action());
		Assert.assertEquals("Excellent work!", entries.get(6).details());

		Assert.assertEquals("Julia Hoffmann", entries.get(7).author());
		Assert.assertEquals("reset course element", entries.get(7).action());
		Assert.assertEquals("", entries.get(7).details());

		Assert.assertEquals("Arnold Kieser", entries.get(8).author());
		Assert.assertEquals("coach", entries.get(8).role());
		Assert.assertEquals("Deadline extension for assignment", entries.get(8).action());
		Assert.assertEquals("userId=1408270341;from=11.07.2024, 13:00;to=11.07.2024, 13:05", entries.get(8).details());

		Assert.assertEquals("Simone Langenegger", entries.get(9).author());
		Assert.assertEquals("", entries.get(9).role());
		Assert.assertEquals("Submit of", entries.get(9).action());
		Assert.assertEquals("Aufgabe_2.docx: upload document Simone-Langenegger-2023-10-24-14-55-52.mp4", entries.get(9).details());

		Assert.assertEquals("Arnold Hoffmann", entries.get(10).author());
		Assert.assertEquals("coach", entries.get(10).role());
		Assert.assertEquals("Back to submission of", entries.get(10).action());
		Assert.assertEquals("Arnold-Hoffmann-2023-09-19-15-53-07.m4a", entries.get(10).details());

		Assert.assertEquals("Simone Hoffmann", entries.get(11).author());
		Assert.assertEquals("", entries.get(11).role());
		Assert.assertEquals("COMMENT set to", entries.get(11).action());
		String commentText =
				"""
						ksadfjg
						sliajsdf
						lksdjliasdjfli""";
		Assert.assertEquals(commentText, entries.get(11).details());

		String commentText2 =
				"""
						Herzlichen Glückwunsch! Sie haben mit 13 von 18 Punkten bestanden.
						
						Viele Grüße
						Max Hoffmann""";

		Assert.assertEquals("mhoffmann", entries.get(12).author());
		Assert.assertEquals("", entries.get(12).orgUnit());
		Assert.assertEquals("", entries.get(12).userId());
		Assert.assertEquals("", entries.get(12).role());
		Assert.assertEquals("COMMENT set to", entries.get(12).action());
		Assert.assertEquals(commentText2, entries.get(12).details());

		Assert.assertEquals("Max Doe", entries.get(13).author());
		Assert.assertEquals("OrgUnit", entries.get(13).orgUnit());
		Assert.assertEquals("15845124", entries.get(13).userId());
		Assert.assertEquals("coach", entries.get(13).role());
		Assert.assertEquals("attempts set to", entries.get(13).action());
		Assert.assertEquals("4", entries.get(13).details());

		Assert.assertEquals("OpenOLAT Administrator", entries.get(14).author());
		Assert.assertEquals("", entries.get(14).orgUnit());
		Assert.assertEquals("360448", entries.get(14).userId());
		Assert.assertEquals("coach", entries.get(14).role());
		Assert.assertEquals("score reset\npassed set to", entries.get(14).action());
		Assert.assertEquals("\nfalse", entries.get(14).details());
	}

	@Test
	public void testParseLogMalformedEntries() {
		String log = """
				Date: 2024-01-18 10:56
				Identity: OpenOLAT Administrator (360448) (coach)
				passed set to: true
				
				-------------------------------------------------------------------
				Date: 2024-01-18 10:56
				Identity: OpenOLAT Administrator (360448) (coach)
				assessmentId set to: 2711
				-------------------------------------------------------------------
				
				Date: 2024-01-18 10:56
				Identity: OpenOLAT Administrator (360448) (coach)
				COMMENT set to: Super
				-------------------------------------------------------------------
				
				Date: 2024-01-18 10:59
				Identity: Anna Schmidt (456789123) (student)
				submission set to: uploaded
				-------------------------------------------------------------------
				
				Date: 2024-01-18 11:01
				Identity: Lisa Meier (678901345) (student)
				visibility set to: visible
				-------------------------------------------------------------------
				
				Date: 2024-01-18 11:02
				Identity: Jonas Keller (789012456) (coach)
				
				-------------------------------------------------------------------
				
				Date: 2024-01-18 11:03
				Identity: Sarah Fischer (890123567) (student)
				grade set to: A
				-------------------------------------------------------------------
				
				Date: 2024-01-18 11:04
				Identity: David Braun (901234678) (coach)
				feedback set to: Excellent work!
				-------------------------------------------------------------------
				-------------------------------------------------------------------
				-------------------------------------------------------------------
				
				Date: 2024-01-18 11:05
				Identity: Julia Hoffmann (123456789) (student)
				
				reset course element
				-------------------------------------------------------------------
				-------------------------------------------------------------------
				-------------------------------------------------------------------
				Date: 2024-10-01 14:41
				passed set to "undefined"
				-------------------------------------------------------------------
				Date: 2024-10-01 14:41
				Identity: Simone Langenegger (424116224)
				-------------------------------------------------------------------
				Date: 2024-10-01 14:41
				Identity: Julia Hoffmann (123456789) (student)
				passed set to "undefined"
				""";

		LogFormatter logFormatter = new LogFormatter();
		List<LogEntry> entries = logFormatter.parseLog(log).validEntries();

		// skipping three malformed entries from Jonas Keller, Simone Langenegger and the one without an Identity
		Assert.assertEquals(7, entries.size());

		LogEntry e0 = entries.get(0);
		Assert.assertEquals("OpenOLAT Administrator", e0.author());
		Assert.assertEquals("coach", e0.role());
		Assert.assertEquals("passed set to\nassessmentId set to\nCOMMENT set to", e0.action());
		Assert.assertEquals("true\n2711\nSuper", e0.details());

		LogEntry e1 = entries.get(1);
		Assert.assertEquals("Anna Schmidt", e1.author());
		Assert.assertEquals("student", e1.role());
		Assert.assertEquals("submission set to", e1.action());
		Assert.assertEquals("uploaded", e1.details());

		LogEntry e2 = entries.get(2);
		Assert.assertEquals("Lisa Meier", e2.author());
		Assert.assertEquals("student", e2.role());
		Assert.assertEquals("visibility set to", e2.action());
		Assert.assertEquals("visible", e2.details());

		LogEntry e3 = entries.get(3);
		Assert.assertEquals("Sarah Fischer", e3.author());
		Assert.assertEquals("student", e3.role());
		Assert.assertEquals("grade set to", e3.action());
		Assert.assertEquals("A", e3.details());

		LogEntry e4 = entries.get(4);
		Assert.assertEquals("David Braun", e4.author());
		Assert.assertEquals("coach", e4.role());
		Assert.assertEquals("feedback set to", e4.action());
		Assert.assertEquals("Excellent work!", e4.details());

		LogEntry e5 = entries.get(5);
		Assert.assertEquals("Julia Hoffmann", e5.author());
		Assert.assertEquals("student", e5.role());
		Assert.assertEquals("reset course element", e5.action());
		Assert.assertEquals("", e5.details());

		LogEntry e6 = entries.get(6);
		Assert.assertEquals("Julia Hoffmann", e6.author());
		Assert.assertEquals("student", e6.role());
		Assert.assertEquals("passed undefined", e6.action());
		Assert.assertEquals("", e6.details());
	}

	@Test
	public void testGroupLogsByTime() throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		List<LogEntry> logs = List.of(
				new LogEntry(sdf.parse("2024-01-18 10:56"), "Admin", "", "1234", "coach", "passed", "true"),
				new LogEntry(sdf.parse("2024-01-18 10:56"), "Admin", "", "1234", "coach", "score", "5.0"),
				new LogEntry(sdf.parse("2024-01-18 10:57"), "User", "", "5678", "student", "COMMENT", "Nice work")
		);

		LogFormatter logFormatter = new LogFormatter();
		Map<String, List<LogEntry>> groupedLogs = logFormatter.groupLogsByTime(logs);
		Assert.assertEquals(2, groupedLogs.size());
		Assert.assertEquals(2, groupedLogs.get("2024-01-18 10:56").size());
		Assert.assertEquals(1, groupedLogs.get("2024-01-18 10:57").size());
	}

	@Test
	public void testParseAndGroupLogsByTimeAndAuthor() {
		String log = """
				Date: 2024-01-18 10:56
				Identity: OpenOLAT Administrator (360448) (coach)
				passed set to: true
				-------------------------------------------------------------------
				Date: 2024-01-18 10:56
				Identity: OpenOLAT Administrator (360448) (coach)
				assessmentId set to: 2711
				-------------------------------------------------------------------
				Date: 2024-01-18 10:56
				Identity: OpenOLAT Administrator (360448) (coach)
				COMMENT set to: Super
				-------------------------------------------------------------------
				Date: 2024-01-18 10:59
				Identity: Anna Schmidt (456789123) (student)
				submission set to: uploaded
				-------------------------------------------------------------------
				Date: 2024-01-18 11:00
				Identity: Michael Berger (567890234) (coach)
				review set to: completed
				-------------------------------------------------------------------
				Date: 2024-01-18 10:56
				Identity: OpenOLAT Administrator (360448) (coach)
				score set to: 5.0
				-------------------------------------------------------------------
				Date: 2024-01-18 11:01
				Identity: Lisa Meier (678901345) (student)
				visibility set to: visible
				-------------------------------------------------------------------
				Date: 2024-01-18 11:02
				Identity: Jonas Keller (789012456) (coach)
				attempts set to: 3
				-------------------------------------------------------------------
				Date: 2024-01-18 11:03
				Identity: Sarah Fischer (890123567) (student)
				grade set to: A
				-------------------------------------------------------------------
				Date: 2024-01-18 11:04
				Identity: David Braun (901234678) (coach)
				feedback set to: Excellent work!
				-------------------------------------------------------------------
				Date: 2024-01-18 11:05
				Identity: Julia Hoffmann (123456789) (student)
				reset course element
				""";

		LogFormatter logFormatter = new LogFormatter();

		// Parse logs
		List<LogEntry> groupedLogs = logFormatter.parseLog(log).validEntries();

		// Verify that logs are grouped correctly
		Assert.assertEquals(8, groupedLogs.size()); // We should have 8 distinct grouped logs

		// all Admin logs at 10:56
		LogEntry firstGroup = groupedLogs.get(0);
		Assert.assertEquals("OpenOLAT Administrator", firstGroup.author());
		Assert.assertEquals("coach", firstGroup.role());
		Assert.assertEquals("passed set to\nassessmentId set to\nCOMMENT set to\nscore set to", firstGroup.action());
		Assert.assertEquals("true\n2711\nSuper\n5.0", firstGroup.details());

		LogEntry secondGroup = groupedLogs.get(1);
		Assert.assertEquals("Anna Schmidt", secondGroup.author());
		Assert.assertEquals("student", secondGroup.role());
		Assert.assertEquals("submission set to", secondGroup.action());
		Assert.assertEquals("uploaded", secondGroup.details());

		LogEntry thirdGroup = groupedLogs.get(2);
		Assert.assertEquals("Michael Berger", thirdGroup.author());
		Assert.assertEquals("coach", thirdGroup.role());
		Assert.assertEquals("review set to", thirdGroup.action());
		Assert.assertEquals("completed", thirdGroup.details());

		LogEntry fourthGroup = groupedLogs.get(3);
		Assert.assertEquals("Lisa Meier", fourthGroup.author());
		Assert.assertEquals("student", fourthGroup.role());
		Assert.assertEquals("visibility set to", fourthGroup.action());
		Assert.assertEquals("visible", fourthGroup.details());

		LogEntry fifthGroup = groupedLogs.get(4);
		Assert.assertEquals("Jonas Keller", fifthGroup.author());
		Assert.assertEquals("coach", fifthGroup.role());
		Assert.assertEquals("attempts set to", fifthGroup.action());
		Assert.assertEquals("3", fifthGroup.details());

		LogEntry sixthGroup = groupedLogs.get(5);
		Assert.assertEquals("Sarah Fischer", sixthGroup.author());
		Assert.assertEquals("student", sixthGroup.role());
		Assert.assertEquals("grade set to", sixthGroup.action());
		Assert.assertEquals("A", sixthGroup.details());

		LogEntry seventhGroup = groupedLogs.get(6);
		Assert.assertEquals("David Braun", seventhGroup.author());
		Assert.assertEquals("coach", seventhGroup.role());
		Assert.assertEquals("feedback set to", seventhGroup.action());
		Assert.assertEquals("Excellent work!", seventhGroup.details());

		LogEntry eighthGroup = groupedLogs.get(7);
		Assert.assertEquals("Julia Hoffmann", eighthGroup.author());
		Assert.assertEquals("student", eighthGroup.role());
		Assert.assertEquals("reset course element", eighthGroup.action());
		Assert.assertEquals("", eighthGroup.details());
	}

	@Test
	public void testFilterAllDuplicates() {
		String log = """
				Date: 2024-01-18 10:56
				Identity: User One (111) (role1)
				actionA set to: value1
				-------------------------------------------------------------------
				Date: 2024-01-18 10:57
				Identity: User Two (222) (role2)
				actionB set to: value2
				-------------------------------------------------------------------
				Date: 2024-01-18 10:58
				Identity: User One (333) (role3)
				actionA set to: value1
				-------------------------------------------------------------------
				Date: 2024-01-18 10:59
				Identity: User Four (444) (role4)
				actionC set to: value3
				-------------------------------------------------------------------
				Date: 2024-01-18 11:00
				Identity: User Five (555) (role5)
				actionB set to: value2
				-------------------------------------------------------------------
				Date: 2024-01-18 11:01
				Identity: User Six (666) (role6)
				actionD set to: value4
				-------------------------------------------------------------------
				Date: 2024-01-18 11:02
				Identity: User Seven (777) (role7)
				actionA set to: value1
				-------------------------------------------------------------------
				""";

		LogFormatter logFormatter = new LogFormatter();
		List<LogEntry> entries = logFormatter.parseLog(log).validEntries();

		// remove all duplicates
		List<LogEntry> filteredEntries = logFormatter.filterAllDuplicates(entries);

		// We expect unique entries for actionA, actionB, actionC, and actionD
		// Although actionA appears three times and actionB twice, only the first occurrence should remain, since the value remains the same
		Assert.assertEquals("Expected 4 unique entries", 4, filteredEntries.size());

		// For extra validation, we can check that the set of unique keys contains the expected values
		Set<String> uniqueKeys = filteredEntries.stream()
				.map(e -> e.action() + "|" + e.details())
				.collect(Collectors.toSet());
		Assert.assertTrue(uniqueKeys.contains("actionA set to|value1"));
		Assert.assertTrue(uniqueKeys.contains("actionB set to|value2"));
		Assert.assertTrue(uniqueKeys.contains("actionC set to|value3"));
		Assert.assertTrue(uniqueKeys.contains("actionD set to|value4"));
	}

	@Test
	public void testParseLogFromFile() {
		InputStream is = getClass().getResourceAsStream("logEntriesTestData/testData.txt");
		Assert.assertNotNull("Testdata file not found", is);

		StringBuilder logBuilder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String line;
			int lineCount = 0;
			while ((line = reader.readLine()) != null && lineCount < 3000000) { // limit to 1M lines, for really large test files
				logBuilder.append(line).append('\n');
				lineCount++;
			}
		} catch (IOException e) {
			Assert.fail("IOException while reading test data: " + e.getMessage());
		}

		String log = logBuilder.toString();
		LogFormatter logFormatter = new LogFormatter();
		LogParseResultWrapper result = logFormatter.parseLog(log);
		List<LogEntry> entries = result.validEntries();

		// Bob reviewer gets grouped and Bob rev gets discarded because redundant information (passed set to true from true)
		Assert.assertEquals(18, entries.size());

		LogEntry e0 = entries.get(0);
		Assert.assertEquals("Max Mustermann", e0.author());
		Assert.assertEquals("coach", e0.role());
		Assert.assertEquals("Submit of", e0.action());
		Assert.assertEquals("Homework1.zip: submit documents", e0.details());

		LogEntry e1 = entries.get(1);
		Assert.assertEquals("Max Mustermann", e1.author());
		Assert.assertEquals("coach", e1.role());
		Assert.assertEquals("Review of", e1.action());
		Assert.assertEquals("PeerReview1.pdf", e1.details());

		LogEntry e2 = entries.get(2);
		Assert.assertEquals("Identity", e2.author());
		Assert.assertEquals("automatic", e2.role());
		Assert.assertEquals("COMMENT set to", e2.action());
		Assert.assertTrue(e2.details().contains("All students should review section 5."));

		LogEntry e3 = entries.get(3);
		Assert.assertEquals("John Doe", e3.author());
		Assert.assertEquals("coach", e3.role());
		Assert.assertEquals("Submit of:", e3.action());
		Assert.assertEquals("Aufgabe2.pdf", e3.details());

		LogEntry e4 = entries.get(4);
		Assert.assertEquals("Jane Smith", e4.author());
		Assert.assertEquals("coach", e4.role());
		Assert.assertEquals("Revision of:", e4.action());
		Assert.assertEquals("Chapter3.docx", e4.details());

		LogEntry e5 = entries.get(5);
		Assert.assertEquals("Alice Admin", e5.author());
		Assert.assertEquals("admin", e5.role());
		Assert.assertEquals("score reset", e5.action());
		Assert.assertEquals("", e5.details());

		LogEntry e6 = entries.get(6);
		Assert.assertEquals("Bob Reviewer", e6.author());
		Assert.assertEquals("reviewer", e6.role());
		Assert.assertEquals("SCORE set to\npassed set to", e6.action());
		Assert.assertEquals("85.5\ntrue", e6.details());

		LogEntry e7 = entries.get(7);
		Assert.assertEquals("Charlie Coach", e7.author());
		Assert.assertEquals("coach", e7.role());
		Assert.assertEquals("Corrections of", e7.action());
		Assert.assertEquals("Task2.docx", e7.details());

		LogEntry e8 = entries.get(8);
		Assert.assertEquals("Emily Editor", e8.author());
		Assert.assertEquals("editor", e8.role());
		Assert.assertEquals("Evaluation finshed", e8.action());
		Assert.assertEquals("", e8.details());

		LogEntry e9 = entries.get(9);
		Assert.assertEquals("Frank Facilitator", e9.author());
		Assert.assertEquals("coach", e9.role());
		Assert.assertEquals("Allow reset task of", e9.action());
		Assert.assertEquals("Project1.zip", e9.details());

		LogEntry e10 = entries.get(10);
		Assert.assertEquals("George Grader", e10.author());
		Assert.assertEquals("grader", e10.role());
		Assert.assertEquals("Back to submission of", e10.action());
		Assert.assertEquals("Homework2.pdf", e10.details());

		LogEntry e11 = entries.get(11);
		Assert.assertEquals("Arnold Kieser", e11.author());
		Assert.assertEquals("coach", e11.role());
		Assert.assertEquals("Deadline extension for assignment", e11.action());
		Assert.assertEquals("userId=1408270341;from=11.07.2024, 13:00;to=11.07.2024, 13:05", e11.details());

		LogEntry e12 = entries.get(12);
		Assert.assertEquals("Simone Hoffmann", e12.author());
		Assert.assertEquals("", e12.role());
		Assert.assertEquals("COMMENT set to", e12.action());
		Assert.assertTrue(e12.details().contains("Wie es weitergeht: Weil zuvor schon der Ständerat dem Geschäft zugestimmt hatte"));

		LogEntry e13 = entries.get(13);
		Assert.assertEquals("Simone Langenegger", e13.author());
		Assert.assertEquals("", e13.role());
		Assert.assertEquals("Submit of", e13.action());
		Assert.assertEquals("Aufgabe_2.docx: upload document Simone-Langenegger-2023-10-24-14-55-52.mp4", e13.details());

		LogEntry e14 = entries.get(14);
		Assert.assertEquals("Arnold Hoffmann", e14.author());
		Assert.assertEquals("coach", e14.role());
		Assert.assertEquals("Back to submission of", e14.action());
		Assert.assertEquals("Arnold-Hoffmann-2023-09-19-15-53-07.m4a", e14.details());

		LogEntry e15 = entries.get(15);
		Assert.assertEquals("Sebastian Schmitt", e15.author());
		Assert.assertEquals("coach", e15.role());
		Assert.assertEquals("reset course element", e15.action());
		Assert.assertEquals("", e15.details());

		String commentText =
				"""
						Herzlichen Glückwunsch! Sie haben mit 13 von 18 Punkten bestanden.
						
						Viele Grüße
						Max Hoffmann""";

		LogEntry e16 = entries.get(16);
		Assert.assertEquals("mhoffmann", e16.author());
		Assert.assertEquals("", e16.orgUnit());
		Assert.assertEquals("", e16.userId());
		Assert.assertEquals("", e16.role());
		Assert.assertEquals("COMMENT set to", e16.action());
		Assert.assertEquals(commentText, e16.details());

		LogEntry e17 = entries.get(17);
		Assert.assertEquals("Max Doe", e17.author());
		Assert.assertEquals("OrgUnit", e17.orgUnit());
		Assert.assertEquals("15845124", e17.userId());
		Assert.assertEquals("coach", e17.role());
		Assert.assertEquals("attempts set to", e17.action());
		Assert.assertEquals("3", e17.details());
	}
}