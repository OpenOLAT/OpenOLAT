package org.olat.test.load;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Performance test; it measures the time needed to open course, click few times (numOfIterations)
 * in course, close course, and repeat for all courses on the first page (LearningResources/Courses).
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class OpenAllCoursesOnFirstPageTest extends BaseSeleneseTestCase {

	private final int courseElemIterations = 10;
	private int visitedCourseElements = 0;
	private int testIterations = 3; //repeat all this times 

	/**
	 * Open learning resources, select courses menu item select each course on first page, repeat testIteration.
	 * When course run is open select first courseElemIterations elements, and close course tab.
	 * 
	 * @throws Exception
	 */
	public void testVisitAllCourses() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);		
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));		
		LearningResources learningResources = workflow.getLearningResources();

		long initialTime = System.currentTimeMillis();

		//go to courses and open each course on each table page, then close the course
		int iteration = 0;	
		int courseIndex = 1;
		int visitedCourses = 0;
		int pageLength = 20;
		while(iteration<testIterations) {		
			System.out.println("iteration: " + iteration);
			while(courseIndex<=pageLength) {
				CourseRun courseRun = learningResources.showCourseContent(false, courseIndex);
				if(courseRun!=null) {
					visitCourse(courseRun);
					courseIndex++;	
					visitedCourses++;
				} else if(courseRun==null) {
					courseIndex = 1;
					break;
				}					
			}	
			iteration++;
		}

		long endTime = System.currentTimeMillis();
		long duration = endTime - initialTime;

		System.out.println("$$$$$$ testVisitAllCourses took: " + duration/1000 + " s");
		courseIndex--;
		System.out.println("$$$$$$ visited courses: " + visitedCourses);
		System.out.println("$$$$$$ visited course elements: " + visitedCourseElements);
	}

	/**
	 * Select first numOfIterations elements in this course.
	 * 
	 * @param courseRun
	 */
	public void visitCourse(CourseRun courseRun) throws Exception {
		System.out.println("visit course");
		//select node in course
		for(int i=1; i<=courseElemIterations; i++) {
			boolean elemFound = courseRun.selectCourseElement(i);
			Thread.sleep(200);
			if(!elemFound) {
				System.out.println("Could not select course element with index: " + i);
				break;
			} else {
				visitedCourseElements++;
			}
		}		
		courseRun.closeAny();
		System.out.println("closed course");
	}

}
