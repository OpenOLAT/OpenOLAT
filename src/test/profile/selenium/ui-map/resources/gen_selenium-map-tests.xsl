<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:param name="mode"/>
	<xsl:output method="text"/>

	<xsl:template match="ui-map">
		<xsl:choose>
			<xsl:when test="$mode='fast'">
				<xsl:call-template name="fastmode"/>
			</xsl:when>
			<xsl:when test="$mode='slow'">
			    <xsl:apply-templates select="pageset"/>
			</xsl:when>
			<xsl:otherwise>
	   			<xsl:message terminate="no">
					<xsl:text>

--------------------------------
Error in seleniumtests/build.xml
--------------------------------


</xsl:text>
				</xsl:message>
      			<xsl:message terminate="yes">
	      			<xsl:text>Must set parameter $mode to 'slow' or 'fast' when calling gen_selenium-map-tests.xsl! Shame on you!</xsl:text>
	       		</xsl:message>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="fastmode">
		<xsl:variable name="testclassname" select="'GenerateFastdOlatMapTest'"/>
		<xsl:variable name="testmethodname" select="'generatedOlatMapTest'"/>
		<xsl:variable name="filename" select="concat($testclassname, '.java')"/>
		<xsl:result-document href="{$filename}">
			<xsl:text>
package org.olat.test.generated;

import org.olat.test.util.selenium.OlatLoginHelper;
import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.testng.annotations.Test;

@Test(groups = {"generated"})
/**
 * This test was generated from the olat-ui-map.xml by gen_selenium-map-tests.xsl.
 * 
 * This variant is the result of mode=fast in which everything happens within the
 * same browser to speed up things considerably. every testcase does a logout at the
 * end. the downside of this approach is, when something fails, it stops the test
 * and you don't see any results of the subsequent tests.
 * 
 * Therefore you probably want to run in mode=slow until everything is fixed,
 * then you want to switch to mode=fast until something breaks again.
 *
 * Happy Selenium-Map-Testing, Mate!
 **/
public class </xsl:text>
			<xsl:value-of select="$testclassname"/>
			<xsl:text> extends BaseSeleneseTestCase {

	@Test
	public void test_</xsl:text>
			<xsl:value-of select="$testmethodname"/>
			<xsl:text>() throws Exception {
</xsl:text>

		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		selenium = context.createSeleniumAndLogin();

		    <xsl:apply-templates select="pageset"/>
			<xsl:text>
	}
	
}
</xsl:text>			
			
		</xsl:result-document>
	</xsl:template>

	<xsl:template match="pageset">
		<xsl:apply-templates select="pageset"/>
		<xsl:apply-templates select="xpath-ui-element"/>
		<xsl:apply-templates select="link-ui-element"/>
		<xsl:apply-templates select="var-link-ui-element"/>
	</xsl:template>
	
	<xsl:template match="xpath-ui-element">
		<xsl:call-template name="ui-element"/>
	</xsl:template>
	
	<xsl:template match="link-ui-element">
		<xsl:call-template name="ui-element"/>
	</xsl:template>
	
	<xsl:template match="var-link-ui-element">
		<xsl:call-template name="ui-element"/>
	</xsl:template>

	<!--  this template creates the actual test class for the ui-element at which we're at -->
	<xsl:template name="ui-element">
		<xsl:variable name="fullUiElementName">
			<xsl:value-of select="ancestor-or-self::pageset[count(ancestor::pageset)=0]/@name"/>
			<xsl:text>_</xsl:text>
			<xsl:for-each select="ancestor::pageset[count(ancestor::pageset)!=0]">
				<xsl:value-of select="@name"/>
				<xsl:text>_</xsl:text>
			</xsl:for-each>
			<xsl:value-of select="@name"/>
		</xsl:variable>
		<xsl:variable name="fullUiElementName_SeleniumSyntax">
			<xsl:value-of select="ancestor-or-self::pageset[count(ancestor::pageset)=0]/@name"/>
			<xsl:text>::</xsl:text>
			<xsl:for-each select="ancestor::pageset[count(ancestor::pageset)!=0]">
				<xsl:value-of select="@name"/>
				<xsl:text>_</xsl:text>
			</xsl:for-each>
			<xsl:value-of select="@name"/>
		</xsl:variable>

		<xsl:variable name="defaultTestCaseDef" select="../default-testcase"/>

		<xsl:choose>
			<!-- this ui-element explicitly has testcase="" set, meaning that we dont generate a testcase here -->
			<xsl:when test="@testcase and @testcase=''">
				<!--  nothing -->
			</xsl:when>
			
			<!--  this ui-element has its own testcase defined, so use this one -->
			<xsl:when test="@testcase and @testcase!=''">
				<xsl:call-template name="gen-testcase">
					<xsl:with-param name="testcase" select="@testcase"/>
					<xsl:with-param name="fullUiElementName" select="$fullUiElementName"/>
				</xsl:call-template>
			</xsl:when>
			
			<!-- this ui-element doesn't have its own testcase defined. take the nearest <default-testcase/>  -->
			<xsl:when test="count($defaultTestCaseDef)=1">
				<xsl:if test="not($defaultTestCaseDef/@testcase) or $defaultTestCaseDef/@testcase='' or not($defaultTestCaseDef/@final-command) or $defaultTestCaseDef/@final-command=''">
		   			<xsl:message terminate="no">
						<xsl:text>

-------------------------------
Syntax Error in olat-ui-map.xml
-------------------------------


</xsl:text>
					</xsl:message>
	      			<xsl:message terminate="yes">
		      			<xsl:text>a default-testcase is not properly configured (testcase or final-command missing). </xsl:text>
		      			<xsl:value-of select="$fullUiElementName"/>
		       		</xsl:message>
		       	</xsl:if>
				
				<xsl:choose>
					<xsl:when test="contains($defaultTestCaseDef/@final-command, '(') and contains($defaultTestCaseDef/@final-command, ')')">
						<!--  special case: the final-command contains (foobar)  -->
						<!--                convert that into: final-command(ui-elemen,foobar) -->
						<xsl:variable name="rawFinalCommand" select="substring-before($defaultTestCaseDef/@final-command, '(')"/>
						<xsl:variable name="secondFuncParam" select="substring-before(substring-after($defaultTestCaseDef/@final-command, '('), ')')"/>
						<xsl:if test="$secondFuncParam=''">
				   			<xsl:message terminate="no">
								<xsl:text>

-------------------------------
Syntax Error in olat-ui-map.xml
-------------------------------


</xsl:text>
							</xsl:message>
				      			<xsl:message terminate="yes">
				      			<xsl:text>a default-testcase is not properly configured (final-command must not contain () but no params. either (param) or without brackets in the first place). </xsl:text>
				      			<xsl:value-of select="$fullUiElementName"/>
				       		</xsl:message>
						</xsl:if>
						<xsl:call-template name="gen-testcase">
							<xsl:with-param name="testcase" select="concat($defaultTestCaseDef/@testcase, '/', $rawFinalCommand, '(', $fullUiElementName_SeleniumSyntax,',',$secondFuncParam,')')"/>
							<xsl:with-param name="fullUiElementName" select="$fullUiElementName"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<!--  this is the normal case: the final-command does not contain () -->
						<xsl:call-template name="gen-testcase">
							<xsl:with-param name="testcase" select="concat($defaultTestCaseDef/@testcase, '/', $defaultTestCaseDef/@final-command, '(', $fullUiElementName_SeleniumSyntax,')')"/>
							<xsl:with-param name="fullUiElementName" select="$fullUiElementName"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
				
			</xsl:when>
			
			<xsl:otherwise>
				<!--  nothing else yet... allow ui-elements without testcase for now... we might want to restrict this and issue a warn here later -->
			
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="gen-testcase">
		<xsl:param name="testcase"/>
		<xsl:param name="fullUiElementName"/>
		
		<xsl:text>

// Testcase: </xsl:text>
		<xsl:value-of select="$testcase"/>
		<xsl:text>
// FullUiElementName: </xsl:text>
		<xsl:value-of select="$fullUiElementName"/>
		
		<xsl:variable name="testclassname" select="concat('GeneratedOlatMapTest_', $fullUiElementName)"/>
		<xsl:variable name="testmethodname" select="concat('generatedOlatMapTest_', $fullUiElementName)"/>
		<xsl:variable name="filename" select="concat($testclassname, '.java')"/>
		
		<xsl:choose>
			<xsl:when test="$mode='slow'">
				<xsl:result-document href="{$filename}">
					<xsl:text>
package org.olat.test.generated;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.testng.annotations.Test;

@Test(groups = {"generated"})
/**
 * This test was generated from the olat-ui-map.xml by gen_selenium-map-tests.xsl.
 * 
 * It's testing the ui-element: </xsl:text><xsl:value-of select="$fullUiElementName"/><xsl:text>.
 * 
 * The testcase is generated from this string: </xsl:text><xsl:value-of select="$testcase"/><xsl:text>.
 **/
public class </xsl:text>
					<xsl:value-of select="$testclassname"/>
					<xsl:text> extends BaseSeleneseTestCase {

	@Test
	public void test_</xsl:text>
					<xsl:value-of select="$testmethodname"/>
					<xsl:text>() throws Exception {
</xsl:text>			
					<xsl:call-template name="code-for-expression">
						<xsl:with-param name="expr" select="$testcase"/>
					</xsl:call-template>
					<xsl:text>
	}
	
}
</xsl:text>			
				</xsl:result-document>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="code-for-expression">
					<xsl:with-param name="expr" select="$testcase"/>
				</xsl:call-template>
				
				<xsl:text>
		
		// Cleanup at the end of an individual link-test
		if (selenium.isElementPresent("ui=tabs::logOut()")) {
			System.out.println("[CLEANUP] Clicking logOut now...");
			selenium.click("ui=tabs::logOut()");
			selenium.waitForPageToLoad("30000");
			System.out.println("[CLEANUP] Done with logOut.");
		} else if (!"OLAT - Online Learning And Training".equals(selenium.getTitle())) {
			System.out.println("[CLEANUP] Can't click logOut, there is no logOut link available...");
			// emergency 
			System.out.println("[CLEANUP] EMERGENCY HERE: opening the original url again: "+context.getStandardAdminOlatLoginInfos().getFullOlatServerUrl());
			selenium.open(context.getStandardAdminOlatLoginInfos().getFullOlatServerUrl());
			System.out.println("[CLEANUP] Did that, now let's wait a few sec... like 5 or so should be fine");
			Thread.sleep(5000);
		}
		System.out.println("[CLEANUP] Title now: "+selenium.getTitle());
		assertEquals("We're now in DMZ - did the logOut now work?", "OLAT - Online Learning And Training", selenium.getTitle());
		// Done with Cleanup

</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="code-for-expression">
		<xsl:param name="expr"/>
		
		<xsl:choose>
			<xsl:when test="contains($expr, '/')">
				<xsl:variable name="step" select="substring-before($expr, '/')"/>
				<xsl:call-template name="code-for-expression">
					<xsl:with-param name="expr" select="$step"/>
				</xsl:call-template>
				<xsl:variable name="remainder" select="substring-after($expr, '/')"/>
				<xsl:call-template name="code-for-expression">
					<xsl:with-param name="expr" select="$remainder"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$expr=''">
				<!--  do nothing -->
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="(not(contains($expr, '(')) and contains($expr, '::'))">
						<!--  then it's a selenium clickAndWait command -->
						<xsl:text>
		// Step-Start: selenium.clickAndWait("</xsl:text>
						<xsl:value-of select="$expr"/>
						<xsl:text>()");
</xsl:text>
						<xsl:text>		selenium.click("</xsl:text>
						<xsl:call-template name="normalizeUiElement">
							<xsl:with-param name="uielement" select="$expr"/>
						</xsl:call-template>
						<xsl:text>");
		selenium.waitForPageToLoad("30000");
		// Step-End
</xsl:text>
					</xsl:when>
					<xsl:when test="(contains(substring-before($expr, '('), '::'))">
						<!--  then it's a selenium clickAndWait command -->
						<xsl:text>
		// Step-Start: selenium.clickAndWait("</xsl:text>
						<xsl:value-of select="$expr"/>
						<xsl:text>");
</xsl:text>
						<xsl:text>		selenium.click("</xsl:text>
						<xsl:call-template name="normalizeUiElement">
							<xsl:with-param name="uielement" select="$expr"/>
						</xsl:call-template>
						<xsl:text>");
		selenium.waitForPageToLoad("30000");
		// Step-End
</xsl:text>
					</xsl:when>
					<xsl:when test="contains($expr, '(')">
						<!--  then it's a direct selenium command -->
						<xsl:text>
		// Step-Start: selenium.</xsl:text>
						<xsl:value-of select="$expr"/>
						<xsl:text>
</xsl:text>
						<xsl:call-template name="expandDirectSeleniumCommand">
							<xsl:with-param name="expr" select="$expr"/>
							<xsl:with-param name="node" select="."/>
						</xsl:call-template>
						<xsl:text>		// Step-End
</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<!--  then it's a macro call -->
						<xsl:call-template name="resolveMacro">
							<xsl:with-param name="macro" select="$expr"/>
							<xsl:with-param name="node" select="."/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="expandDirectSeleniumCommand">
		<xsl:param name="expr"/>
		<xsl:param name="node"/>
		<xsl:choose>
			<xsl:when test="starts-with($expr, 'type(') and ends-with($expr, ')')">
				<!--  type(dmz::username,foo)	 -->
				<!--  selenium.type("ui=dmz::username()", username); -->
				<xsl:text>		selenium.type("</xsl:text>
				<xsl:call-template name="normalizeUiElement">
					<xsl:with-param name="uielement" select="substring-before(substring-after($expr, 'type('),',')"/>
				</xsl:call-template>
				<xsl:text>", "</xsl:text>
				<xsl:value-of select="substring-before(substring-after($expr, ','),')')"/>
				<xsl:text>");
</xsl:text>
			</xsl:when>
			<xsl:when test="starts-with($expr, 'click(') and ends-with($expr, ')')">
				<!--  clickAndWait(dmz::login)	 -->
				<!-- selenium.click("ui=tabs::learningResources()"); -->
				<!-- selenium.waitForPageToLoad("30000"); -->
				<xsl:text>		selenium.click("</xsl:text>
				<xsl:call-template name="normalizeUiElement">
					<xsl:with-param name="uielement" select="substring-before(substring-after($expr, 'click('),')')"/>
				</xsl:call-template>
				<xsl:text>");
</xsl:text>
			</xsl:when>
			<xsl:when test="starts-with($expr, 'check(') and ends-with($expr, ')')">
				<!--  clickAndWait(dmz::login)	 -->
				<!-- selenium.click("ui=tabs::learningResources()"); -->
				<!-- selenium.waitForPageToLoad("30000"); -->
				<xsl:text>		selenium.check("</xsl:text>
				<xsl:call-template name="normalizeUiElement">
					<xsl:with-param name="uielement" select="substring-before(substring-after($expr, 'check('),')')"/>
				</xsl:call-template>
				<xsl:text>");
</xsl:text>
			</xsl:when>
			<xsl:when test="starts-with($expr, 'clickAndWait(') and ends-with($expr, ')')">
				<!--  clickAndWait(dmz::login)	 -->
				<!-- selenium.click("ui=tabs::learningResources()"); -->
				<!-- selenium.waitForPageToLoad("30000"); -->
				<xsl:text>		selenium.click("</xsl:text>
				<xsl:call-template name="normalizeUiElement">
					<xsl:with-param name="uielement" select="substring-before(substring-after($expr, 'clickAndWait('),')')"/>
				</xsl:call-template>
				<xsl:text>");
		selenium.waitForPageToLoad("30000");
</xsl:text>
			</xsl:when>
			<xsl:when test="starts-with($expr, 'select(') and ends-with($expr, ')')">
				<!--  select(dmz::language,English)	 -->
				<!-- selenium.select("ui=dmz::language()", "English"); -->
				<xsl:text>		selenium.select("</xsl:text>
				<xsl:call-template name="normalizeUiElement">
					<xsl:with-param name="uielement" select="substring-before(substring-after($expr, 'select('),',')"/>
				</xsl:call-template>
				<xsl:text>", "</xsl:text>
				<xsl:value-of select="substring-before(substring-after($expr, ','),')')"/>
				<xsl:text>");
</xsl:text>
			</xsl:when>
			
			<xsl:otherwise>
	   			<xsl:message terminate="no">
					<xsl:text>

-------------------------------
Syntax Error in olat-ui-map.xml
-------------------------------


</xsl:text>
				</xsl:message>
	      			<xsl:message terminate="yes">
	      			<xsl:text>Selenium-Shortcut not defined in gen_selenium-map.test.xsl: </xsl:text>
	      			<xsl:value-of select="$expr"/>
	      			<xsl:text> in element=</xsl:text>
	      			<xsl:value-of select="name($node)"/>
	      			<xsl:for-each select="$node/@*">
	      				<xsl:text> attribute: </xsl:text>
	      				<xsl:value-of select="name()"/>
	      				<xsl:text>=</xsl:text>
	      				<xsl:value-of select="."/>
	      			</xsl:for-each>
	       		</xsl:message>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="resolveMacro">
		<xsl:param name="macro"/>
		<xsl:param name="node"/>
		
		<xsl:text>
		// Step-Start: MACRO: </xsl:text>
		<xsl:value-of select="$macro"/>

		<xsl:choose>
			<xsl:when test="$macro='adminlogin'">
				<xsl:call-template name="macroAdminLogin"/>
			</xsl:when>
			<xsl:when test="$macro='adminloginDeutsch'">
				<xsl:call-template name="macroAdminLoginDeutsch"/>
			</xsl:when>
			<xsl:when test="$macro='dmz'">
				<xsl:call-template name="macroDmz"/>
			</xsl:when>
			<xsl:otherwise>

				<xsl:variable name="macrodef" select="$node/ancestor::*[testcase-macro/@name=$macro][1]/testcase-macro[@name=$macro]"/>
		
				<xsl:if test="count($macrodef)=0">
		   			<xsl:message terminate="no">
						<xsl:text>

-------------------------------
Syntax Error in olat-ui-map.xml
-------------------------------


</xsl:text>
					</xsl:message>
		      			<xsl:message terminate="yes">
		      			<xsl:text>Macro not defined: </xsl:text>
		      			<xsl:value-of select="$macro"/>
		      			<xsl:text> in scope of: element=</xsl:text>
		      			<xsl:value-of select="name($node)"/>
		      			<xsl:for-each select="$node/@*">
		      				<xsl:text> attribute: </xsl:text>
		      				<xsl:value-of select="name()"/>
		      				<xsl:text>=</xsl:text>
		      				<xsl:value-of select="."/>
		      			</xsl:for-each>
		       		</xsl:message>
				</xsl:if>

				<xsl:for-each select="$macrodef">
					<xsl:if test="count(child::*)=0">
						<xsl:text>		// (!!EMPTY MACRO!!)
</xsl:text>
					</xsl:if>
					<xsl:for-each select="child::*">
						<xsl:choose>
							<xsl:when test="name()='selenium'">
								<xsl:call-template name="macro-step-selenium"/>
							</xsl:when>
							<xsl:when test="name()='call-macro' and @name=$macro">
								<xsl:call-template name="resolveMacro">
									<xsl:with-param name="macro" select="@name"/>
									<xsl:with-param name="node" select="$node/.."/>
								</xsl:call-template>
							</xsl:when>
							<xsl:when test="name()='call-macro' and @name!=$macro">
								<xsl:call-template name="resolveMacro">
									<xsl:with-param name="macro" select="@name"/>
									<xsl:with-param name="node" select="$node"/>
								</xsl:call-template>
							</xsl:when>
						</xsl:choose>
					</xsl:for-each>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>		// Step-End
</xsl:text>
		
	</xsl:template>

	<xsl:template name="macro-step-selenium">

		<xsl:text>
		// Step-Start: SELENIUM: </xsl:text>
		
		
		<xsl:text>command=</xsl:text>
		<xsl:value-of select="@command"/>
		
		<xsl:text>, target=</xsl:text>
		<xsl:value-of select="@target"/>

		<xsl:text>, value=</xsl:text>
		<xsl:value-of select="@value"/>
		
		<xsl:text>
</xsl:text>

		<xsl:choose>
			<xsl:when test="@command='click'">
				<xsl:text>		selenium.click("</xsl:text>
				<xsl:call-template name="normalizeUiElement">
					<xsl:with-param name="uielement" select="@target"/>
				</xsl:call-template>
				<xsl:text>");
</xsl:text>
			</xsl:when>
			<xsl:when test="@command='clickAndWait'">
				<xsl:text>		selenium.click("</xsl:text>
				<xsl:call-template name="normalizeUiElement">
					<xsl:with-param name="uielement" select="@target"/>
				</xsl:call-template>
				<xsl:text>");
		selenium.waitForPageToLoad("30000");
</xsl:text>
			</xsl:when>
			<xsl:when test="@command='type'">
				<xsl:text>		selenium.type("</xsl:text>
				<xsl:call-template name="normalizeUiElement">
					<xsl:with-param name="uielement" select="@target"/>
				</xsl:call-template>
				<xsl:text>","</xsl:text>
				<xsl:value-of select="@value"/>
				<xsl:text>");
</xsl:text>
			</xsl:when>
			<xsl:otherwise>
	   			<xsl:message terminate="no">
					<xsl:text>

----------------------------------------------------------------
Missing API in gen_selenium-map-tests.xsl used by olatui-map.xml
----------------------------------------------------------------


</xsl:text>
				</xsl:message>
      			<xsl:message terminate="yes">
	      			<xsl:text>selenium command not yet defined for translation: </xsl:text>
	      			<xsl:value-of select="@command"/>
	      			<xsl:text> called with target=</xsl:text>
	      			<xsl:value-of select="@target"/>
	       		</xsl:message>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>

	<xsl:template name="normalizeUiElement">
		<xsl:param name="uielement"/>
		<xsl:choose>
			<xsl:when test="not(contains($uielement, '::'))">
				<xsl:value-of select="$uielement"/>
			</xsl:when>
			<xsl:when test="starts-with($uielement, 'ui=') and ends-with($uielement, ')')">
				<xsl:value-of select="$uielement"/>
			</xsl:when>
			<xsl:when test="not(starts-with($uielement, 'ui=')) and ends-with($uielement, ')')">
				<xsl:text>ui=</xsl:text>
				<xsl:value-of select="$uielement"/>
			</xsl:when>
			<xsl:when test="starts-with($uielement, 'ui=') and not(ends-with($uielement, ')'))">
				<xsl:value-of select="$uielement"/>
				<xsl:text>()</xsl:text>
			</xsl:when>
			<xsl:when test="not(starts-with($uielement, 'ui=')) and not(ends-with($uielement, ')'))">
				<xsl:text>ui=</xsl:text>
				<xsl:value-of select="$uielement"/>
				<xsl:text>()</xsl:text>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="macroAdminLogin">
		<!--  system macro -->
		<xsl:choose>
			<xsl:when test="$mode='slow'">
				<xsl:text>
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		selenium = context.createSeleniumAndLogin();
		assertEquals("OLAT - Home", selenium.getTitle());
</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>
		OlatLoginHelper.olatLogin(selenium, context.getStandardAdminOlatLoginInfos());
		assertEquals("OLAT - Home", selenium.getTitle());
</xsl:text>				
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="macroAdminLoginDeutsch">
		<!--  system macro -->
		<xsl:choose>
			<xsl:when test="$mode='slow'">
				<xsl:text>
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		OlatLoginInfos loginInfos = context.getStandardAdminOlatLoginInfos();
		loginInfos.setLanguage("Deutsch");
		selenium = context.createSeleniumAndLogin(loginInfos);
		assertEquals("OLAT - Home", selenium.getTitle());
</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>
		OlatLoginInfos loginInfos = context.getStandardAdminOlatLoginInfos();
		loginInfos.setLanguage("Deutsch");
		OlatLoginHelper.olatLogin(selenium, loginInfos);
		assertEquals("OLAT - Home", selenium.getTitle());
</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="macroDmz">
		<!--  system macro -->
		<xsl:choose>
			<xsl:when test="$mode='slow'">
				<xsl:text>
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		selenium = context.createSelenium();
		assertEquals("OLAT - Online Learning And Training", selenium.getTitle());
</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>
		if (selenium.isElementPresent("ui=tabs::logOut()")) {
			selenium.click("ui=tabs::logOut()");
			selenium.waitForPageToLoad("30000");
		}
		assertEquals("OLAT - Online Learning And Training", selenium.getTitle());
</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>