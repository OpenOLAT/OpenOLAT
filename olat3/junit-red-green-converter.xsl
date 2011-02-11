<?xml version="1.0" encoding="UTF-8"?>

<!-- goes through a specified set of junit result xml files and generates an ant file with which
     there will be a org.olat.catalog.CatalogManagerTest.testGetChildrenOf.jpg for each of
     the test methods containing a predefined red or green jpg -->

<!-- input to this xslt is the TESTS-TestSuites.xml -->

<!-- input parameters are:
     - red.jpg : full path to the red.jpg which will - in the ant file - be copied into the target file
     - green.jpg:full path to the green.jpg
     - testresults.dir: directory path where the input file and its TEST-...xml files are
     - redgreenoutput.dir: directory path where the output files should be stored -->

<!-- output of this xslt is a junit-red-green-converter.xml file which must then be run with ant -->

<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:file="java.io.File">

	<xsl:output method="xml" indent="yes" />
	<xsl:param name="testresults.dir"/>
	<xsl:param name="redgreenoutput.dir"/>
	<xsl:param name="red.gif"/>
	<xsl:param name="green.gif"/>
	
	<xsl:template match="text()|comment()|processing-instruction()" priority="-1">
		<!--  filter those out -->
	</xsl:template>

	<xsl:template match="testsuites">
		<!--  create the ant header -->
		<project name="junit-red-green-converter" default="default">

			<target name="default" >
				<xsl:for-each select="testsuite">
					<xsl:for-each select="testcase">
						<xsl:variable name="classname" select="@classname"/>
						<xsl:variable name="name" select="@name"/>
						<xsl:variable name="testcase.file" select="concat($testresults.dir, '/TEST-', $classname, '.xml')"/>
						<xsl:message terminate="no">
							<xsl:text>processing </xsl:text>
							<xsl:value-of select="$testcase.file"/>
							<xsl:text> (test method </xsl:text>
							<xsl:value-of select="@name"/>
							<xsl:text>)...</xsl:text>
						</xsl:message>
				
						<xsl:variable name="thefile" select="file:new($testcase.file)"/>
						<xsl:if test="file:exists($thefile)">
							<xsl:for-each select="document($testcase.file)/testsuite/testcase[@classname=$classname and @name=$name]">
								<xsl:call-template name="handletestcase">
									<xsl:with-param name="classname" select="$classname"/>
									<xsl:with-param name="name" select="$name"/>
								</xsl:call-template>
							</xsl:for-each>
						</xsl:if>
						<xsl:if test="not(file:exists($thefile))">
							<!--  then manually set it to error -->
							<copy>
								<xsl:attribute name="tofile">
									<xsl:value-of select="concat($redgreenoutput.dir, '/', $classname, '.', $name, '.gif')"/>
								</xsl:attribute>
								<xsl:attribute name="file">
									<xsl:value-of select="$red.gif"/>
								</xsl:attribute>
							</copy>
						</xsl:if>
					</xsl:for-each>
				</xsl:for-each>
			</target>
		</project>
	</xsl:template>
	
	<xsl:template name="handletestcase">
		<xsl:param name="name"/>
		<xsl:param name="classname"/>
		
		<!--  create an ant instruction which looks like this:
		
			<copy tofile="${redgreenoutput.dir}/$classname.gif" file="$red/$green.jpg"/>
		
		 -->
		<copy>
			<xsl:attribute name="tofile">
				<xsl:value-of select="concat($redgreenoutput.dir, '/', $classname, '.', $name, '.gif')"/>
			</xsl:attribute>
			<xsl:attribute name="file">
				<xsl:choose>
					<xsl:when test="failure">
						<xsl:value-of select="$red.gif"/>
					</xsl:when>
					<xsl:when test="error">
						<xsl:value-of select="$red.gif"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$green.gif"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
		</copy>
	</xsl:template>

</xsl:stylesheet>