<?xml version="1.0" encoding="UTF-8"?>
<!--

Renders the author/debug view of a standalone assessmentItem

Input document: doesn't matter

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xs qti qw m">

  <!-- ************************************************************ -->

  <xsl:import href="author-view-common.xsl"/>

  <!-- State of test being rendered -->
  <xsl:param name="testSessionState" as="element(qw:testSessionState)"/>

  <xsl:function name="qw:formatNodeType" as="xs:string">
    <xsl:param name="testPlanNode" as="element(qw:node)"/>
    <xsl:choose>
      <xsl:when test="$testPlanNode/@type='TEST_PART'">
        <xsl:sequence select="'testPart'"/>
      </xsl:when>
      <xsl:when test="$testPlanNode/@type='ASSESSMENT_SECTION'">
        <xsl:sequence select="'assessmentSection'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="'assessmentItemRef'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="qw:formatNodeKey" as="xs:string">
    <xsl:param name="testPlanNode" as="element(qw:node)"/>
    <xsl:variable name="components" select="tokenize($testPlanNode/@key, ':')" as="xs:string*"/>
    <xsl:variable name="identifier" select="$components[1]" as="xs:string"/>
    <xsl:variable name="globalIndex" select="$components[2]" as="xs:string"/>
    <xsl:variable name="instance" select="$components[3]" as="xs:string"/>
    <xsl:sequence select="concat($identifier,
      if ($instance!='1') then concat('&#xa0;(Instance&#xa0;', $instance, ')') else ())"/>
  </xsl:function>

  <!-- ************************************************************ -->

  <xsl:template match="/" as="element(html)">
    <html>
      <head>
        <title>Author Debug View</title>
        <xsl:call-template name="includeQtiWorksJsAndCss"/>
      </head>
      <body class="page authorInfo">
        <div class="container_12">
          <header class="pageHeader">
            <h1>QTIWorks</h1>
          </header>
          <h2>QTI test author's feedback</h2>

          <xsl:call-template name="errorStatusPanel"/>
          <xsl:call-template name="buttonBar"/>
          <xsl:apply-templates select="$testSessionState"/>
        </div>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="buttonBar">
    <div class="buttonBar">
      <ul class="controls">
        <li>
          <form action="{$webappContextPath}{$sourceUrl}" method="get" target="oaa0" class="showXmlInDialog" title="Test Source XML">
            <input type="submit" value="View Test source XML" class="btn btn-default"/>
          </form>
        </li>
        <li>
          <form action="{$webappContextPath}{$stateUrl}" method="get" target="oaa0" class="showXmlInDialog" title="Test State XML">
            <input type="submit" value="View Test state XML" class="btn btn-default"/>
          </form>
        </li>
        <li>
          <form action="{$webappContextPath}{$resultUrl}" method="get" target="oaa0" class="showXmlInDialog" title="Test Result XML">
            <input type="submit" value="View Test &lt;assessmentResult&gt; XML" class="btn btn-default"/>
          </form>
        </li>
      </ul>
    </div>
  </xsl:template>

  <xsl:template match="qw:testSessionState">
    <div class="resultPanel info">
      <h4>Key status information</h4>
      <div class="details">
        <ul>
          <li>Entry time: <xsl:value-of select="qw:format-optional-date(@entryTime, '(Not Yet Entered)')"/></li>
          <li>End time: <xsl:value-of select="qw:format-optional-date(@endTime, '(Not Yet Ended)')"/></li>
          <li>Duration accumulated: <xsl:value-of select="@durationAccumulated div 1000.0"/> s</li>
          <li>Initialized: <xsl:value-of select="@initialized"/></li>
          <li>Current testPart key: <xsl:value-of select="if (exists(@currentTestPartKey)) then @currentTestPartKey else '(Not in a testPart)'"/></li>
          <li>Current item key: <xsl:value-of select="if (exists(@currentItemKey)) then @currentItemKey else '(No item selected)'"/></li>
        </ul>
      </div>
    </div>
    <xsl:apply-templates select="." mode="variableValuesPanel"/>
    <xsl:apply-templates select="qw:testPlan" mode="testPlan"/>
    <xsl:apply-templates select="qw:testPlan" mode="drillDown"/>
    <xsl:call-template name="notificationsPanel"/>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="qw:testSessionState" mode="variableValuesPanel" as="element(div)*">
    <div class="resultPanel info">
      <h4>Outcome values (<xsl:value-of select="count(qw:outcomeVariable)"/>)</h4>
      <div class="details">
        <xsl:choose>
          <xsl:when test="exists(qw:outcomeVariable)">
            <xsl:call-template name="dumpValues">
              <xsl:with-param name="valueHolders" select="qw:outcomeVariable"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            (None)
          </xsl:otherwise>
        </xsl:choose>
      </div>
    </div>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="qw:testPlan" mode="testPlan">
    <div class="resultPanel info">
      <h4>Test plan</h4>
      <div class="details">
        <p>
          This shows the shape of the test being delivered, after any selection and ordering rules have been applied.
          It also shows the effective values of itemSessionControl for each selected testPart, assessmentSection
          and assessmentItemRef.
        </p>
        <table>
          <thead>
            <tr>
              <th>Node</th>
              <th>Type</th>
              <th>max<br/>Attempts</th>
              <th>validate<br/>Responses</th>
              <th>allow<br/>Comment</th>
              <th>allow<br/>Skipping</th>
              <th>show<br/>Solution</th>
              <th>show<br/>Feedback</th>
              <th>allow<br/>Review</th>
            </tr>
          </thead>
          <tbody>
            <xsl:apply-templates select="qw:node" mode="testPlan"/>
          </tbody>
        </table>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="qw:testPlan//qw:node" mode="testPlan">
    <tr>
      <td>
        <xsl:for-each select="ancestor::qw:node">&#x21b3;</xsl:for-each>
        <xsl:value-of select="qw:formatNodeKey(.)"/>
      </td>
      <td align="center"><xsl:value-of select="qw:formatNodeType(.)"/></td>
      <td align="center"><xsl:value-of select="@maxAttempts"/></td>
      <td align="center"><xsl:value-of select="@validateResponses"/></td>
      <td align="center"><xsl:value-of select="@allowComment"/></td>
      <td align="center"><xsl:value-of select="@allowSkipping"/></td>
      <td align="center"><xsl:value-of select="@showSolution"/></td>
      <td align="center"><xsl:value-of select="@showFeedback"/></td>
      <td align="center"><xsl:value-of select="@allowReview"/></td>
    </tr>
    <xsl:apply-templates select="qw:node" mode="testPlan"/>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="qw:testPlan" mode="drillDown">
    <div class="resultPanel info">
      <h4>Node state drilldown</h4>
      <div class="details">
        <p>
          This shows the state of each testPart, assessmentSection and assessmentItemRef instance.
        </p>
        <xsl:apply-templates select="qw:node" mode="drillDown"/>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="qw:testPlan//qw:node" mode="drillDown">
    <div class="resultPanel expandable">
      <h4>
        <xsl:sequence select="qw:formatNodeType(.)"/>
        <xsl:sequence select="qw:formatNodeKey(.)"/>
      </h4>
      <div class="details">
        <xsl:choose>
          <xsl:when test="@type='TEST_PART'">
            <xsl:apply-templates select="//qw:testPart[@key=current()/@key]/qw:testPartSessionState"/>
          </xsl:when>
          <xsl:when test="@type='ASSESSMENT_SECTION'">
            <xsl:apply-templates select="//qw:assessmentSection[@key=current()/@key]/qw:assessmentSectionSessionState"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="//qw:item[@key=current()/@key]/qw:itemSessionState">
              <xsl:with-param name="includeNotifications" select="false()"/>
            </xsl:apply-templates>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="qw:node" mode="drillDown"/>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="qw:assessmentSectionSessionState | qw:testPartSessionState">
    <ul>
      <li>Entry time: <xsl:value-of select="qw:format-optional-date(@entryTime, '(Not Yet Entered)')"/></li>
      <xsl:if test="exists(@entryTime)">
        <li>End time: <xsl:value-of select="qw:format-optional-date(@endTime, '(Not Yet Ended)')"/></li>
        <xsl:if test="exists(@exitTime)">
          <li>Exit time: <xsl:value-of select="qw:format-optional-date(@endTime, '(Not Yet Exited)')"/></li>
        </xsl:if>
      </xsl:if>
      <li>Duration accumulated: <xsl:value-of select="@durationAccumulated div 1000.0"/> s</li>
      <li>preCondition failed?: <xsl:value-of select="@preConditionFailed"/></li>
      <li>Jumped by branchRule?: <xsl:value-of select="@jumpedByBranchRule"/></li>
      <xsl:if test="@branchRuleTarget">
        <li>branchRule target: <xsl:value-of select="@branchRuleTarget"/></li>
      </xsl:if>
    </ul>
  </xsl:template>

</xsl:stylesheet>
