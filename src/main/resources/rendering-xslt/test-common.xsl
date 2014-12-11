<?xml version="1.0" encoding="UTF-8"?>
<!--

Base templates used in test rendering

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns:saxon="http://saxon.sf.net/"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti xs qw saxon m">

  <!-- ************************************************************ -->

  <xsl:import href="qti-common.xsl"/>

  <!-- URI of the Test being rendered -->
  <xsl:param name="testSystemId" as="xs:string" required="yes"/>

  <!-- State of test being rendered -->
  <xsl:param name="testSessionState" as="element(qw:testSessionState)" required="yes"/>

  <!-- Outcome declarations in test -->
  <xsl:param name="testOutcomeDeclarations" select="()" as="element(qti:outcomeDeclaration)*"/>

  <!-- Action URLs -->
  <xsl:param name="testPartNavigationUrl" as="xs:string" required="yes"/>
  <xsl:param name="selectTestItemUrl" as="xs:string" required="yes"/>
  <xsl:param name="advanceTestItemUrl" as="xs:string" required="yes"/>
  <xsl:param name="endTestPartUrl" as="xs:string" required="yes"/>
  <xsl:param name="reviewTestPartUrl" as="xs:string" required="yes"/>
  <xsl:param name="reviewTestItemUrl" as="xs:string" required="yes"/>
  <xsl:param name="showTestItemSolutionUrl" as="xs:string" required="yes"/>
  <xsl:param name="advanceTestPartUrl" as="xs:string" required="yes"/>
  <xsl:param name="exitTestUrl" as="xs:string" required="yes"/>

  <!-- ************************************************************ -->

  <!-- Current TestPart details in the TestPlan -->
  <xsl:variable name="currentTestPartKey" select="$testSessionState/@currentTestPartKey" as="xs:string"/>
  <xsl:variable name="currentTestPartNode" select="$testSessionState/qw:testPlan/qw:node[@key=$currentTestPartKey]" as="element(qw:node)"/>

  <!-- assesssmentTest details -->
  <xsl:variable name="assessmentTest" select="document($testSystemId)/*[1]" as="element(qti:assessmentTest)"/>
  <xsl:variable name="currentTestPart" select="$assessmentTest/qti:testPart[@identifier=qw:extract-identifier($currentTestPartNode)]" as="element(qti:testPart)"/>
  <xsl:variable name="hasMultipleTestParts" select="count($assessmentTest/qti:testPart) &gt; 1" as="xs:boolean"/>

  <!-- Test outcome values -->
  <xsl:variable name="testOutcomeValues" select="$testSessionState/qw:outcomeVariable" as="element(qw:outcomeVariable)*"/>

  <!-- ************************************************************ -->

  <xsl:variable name="testOrTestPart" as="xs:string"
    select="if ($hasMultipleTestParts) then 'Test Part' else 'Test'"/>

  <xsl:variable name="endTestPartAlertMessage" as="xs:string"
    select="concat('Are you sure? This will commit your answers for this ', $testOrTestPart, '.')"/>

  <xsl:variable name="exitTestPartAlertMessage" as="xs:string"
    select="concat('Are you sure? This will leave this ', $testOrTestPart, ' and you can''t go back in.')"/>

  <xsl:variable name="exitTestAlertMessage" as="xs:string"
    select="'Are you sure? This will leave ths Test and you can''t go back in.'"/>

  <!-- ************************************************************ -->

  <xsl:function name="qw:get-test-outcome-value" as="element(qw:outcomeVariable)?">
    <xsl:param name="identifier" as="xs:string"/>
    <xsl:sequence select="$testOutcomeValues[@identifier=$identifier]"/>
  </xsl:function>

  <xsl:function name="qw:get-test-outcome-declaration" as="element(qti:outcomeDeclaration)?">
    <xsl:param name="identifier" as="xs:string"/>
    <xsl:sequence select="$assessmentTest/qti:outcomeDeclaration[@identifier=$identifier]"/>
  </xsl:function>

  <xsl:function name="qw:extract-identifier" as="xs:string">
    <xsl:param name="testPlanNode" as="element(qw:node)"/>
    <xsl:sequence select="substring-before($testPlanNode/@key, ':')"/>
  </xsl:function>

  <!-- ************************************************************ -->

  <xsl:template match="qti:rubricBlock" as="element(div)">
    <div class="rubric {@view}">
      <xsl:if test="not($view) or ($view = @view)">
        <xsl:apply-templates/>
      </xsl:if>
    </div>
  </xsl:template>

  <!-- ************************************************************ -->

  <!-- printedVariable. Numeric output currently only supports Java String.format formatting. -->
  <xsl:template match="qti:assessmentTest//qti:printedVariable" as="element(span)">
    <xsl:variable name="identifier" select="@identifier" as="xs:string"/>
    <xsl:variable name="testOutcomeValue" select="qw:get-test-outcome-value(@identifier)" as="element(qw:outcomeVariable)?"/>
    <span class="printedVariable">
      <xsl:choose>
        <xsl:when test="exists($testOutcomeValue)">
          <xsl:call-template name="printedVariable">
            <xsl:with-param name="source" select="."/>
            <xsl:with-param name="valueHolder" select="$testOutcomeValue"/>
            <xsl:with-param name="valueDeclaration" select="qw:get-test-outcome-declaration(@identifier)"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          (variable <xsl:value-of select="$identifier"/> was not found)
        </xsl:otherwise>
      </xsl:choose>
    </span>
  </xsl:template>

  <!-- Keep MathML by default -->
  <xsl:template match="m:*" as="element()">
    <xsl:element name="{local-name()}" namespace="http://www.w3.org/1998/Math/MathML">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <!-- MathML parallel markup containers: we'll remove any non-XML annotations, which may
  result in the container also being removed as it's no longer required in that case. -->
  <xsl:template match="m:semantics" as="element()*">
    <xsl:choose>
      <xsl:when test="not(*[position()!=1 and self::m:annotation-xml])">
        <!-- All annotations are non-XML so remove this wrapper completely (and unwrap a container mrow if required) -->
        <xsl:apply-templates select="if (*[1][self::m:mrow]) then *[1]/* else *[1]"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- Keep non-XML annotations -->
        <xsl:element name="semantics" namespace="http://www.w3.org/1998/Math/MathML">
          <xsl:apply-templates select="* except m:annotation"/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="m:*/text()" as="text()">
    <!-- NOTE: The XML input is produced using JQTI's toXmlString() method, which has
    the unfortunate effect of indenting MathML, so we'll renormalise -->
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>

  <!-- mathml (mi) -->
  <!--
  We are extending the spec here in 2 ways:
  1. Allowing MathsContent variables to be substituted
  2. Allowing arbitrary response and outcome variables to be substituted.
  -->
  <xsl:template match="qti:assessmentTest//m:mi" as="element()">
    <xsl:variable name="content" select="normalize-space(text())" as="xs:string"/>
    <xsl:variable name="testOutcomeValue" select="qw:get-test-outcome-value(@identifier)" as="element(qw:outcomeVariable)?"/>
    <xsl:choose>
      <xsl:when test="exists($testOutcomeValue)">
        <xsl:call-template name="substitute-mi">
          <xsl:with-param name="identifier" select="$content"/>
          <xsl:with-param name="value" select="$testOutcomeValue"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="mi" namespace="http://www.w3.org/1998/Math/MathML">
          <xsl:copy-of select="@*"/>
          <xsl:apply-templates/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ************************************************************ -->

  <!-- test feedback -->
  <xsl:template match="qti:testFeedback">
    <xsl:variable name="feedback-content" as="node()*">
      <xsl:choose>
        <xsl:when test="$overrideFeedback">
          <xsl:apply-templates/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="identifierMatch" select="boolean(qw:value-contains(qw:get-test-outcome-value(@outcomeIdentifier), @identifier))" as="xs:boolean"/>
          <xsl:if test="($identifierMatch and @showHide='show') or (not($identifierMatch) and @showHide='hide')">
            <xsl:apply-templates/>
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="exists($feedback-content)">
      <h2>Feedback</h2>
      <xsl:copy-of select="$feedback-content"/>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
