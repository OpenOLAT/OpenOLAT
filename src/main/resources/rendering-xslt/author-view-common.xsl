<?xml version="1.0" encoding="UTF-8"?>
<!--

Common templates for item & test author views

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

  <xsl:import href="qti-common.xsl"/>

  <!-- ************************************************************ -->

  <xsl:template name="errorStatusPanel" as="element(ul)?">
    <xsl:if test="exists($notifications) or ($validated and not($valid))">
      <xsl:variable name="errors" select="$notifications[@level='ERROR']" as="element(qw:notification)*"/>
      <xsl:variable name="warnings" select="$notifications[@level='WARNING']" as="element(qw:notification)*"/>
      <xsl:variable name="infos" select="$notifications[@level='INFO']" as="element(qw:notification)*"/>
      <ul class="summary">
        <xsl:if test="exists($errors)">
          <li class="errorSummary">
            <a href="#notifications"><xsl:value-of select="count($errors)"/> Runtime Error<xsl:if test="count($errors)!=1">s</xsl:if></a>
          </li>
        </xsl:if>
        <xsl:if test="exists($warnings)">
          <li class="warnSummary">
            <a href="#notifications"><xsl:value-of select="count($warnings)"/> Runtime Warning<xsl:if test="count($warnings)!=1">s</xsl:if></a>
          </li>
        </xsl:if>
        <xsl:if test="exists($infos)">
          <li class="infoSummary">
            <a href="#notifications"><xsl:value-of select="count($infos)"/> Runtime Information Notification<xsl:if test="count($notifications)!=1">s</xsl:if></a>
          </li>
        </xsl:if>
        <xsl:if test="$validated and not($valid)">
          <li class="errorSummary">
            <a href="{$webappContextPath}{$validationUrl}">This assessment has validation errors or warnings</a>
          </li>
        </xsl:if>
      </ul>
    </xsl:if>
  </xsl:template>


  <!-- ************************************************************ -->

  <xsl:template match="qw:itemSessionState" as="element(div)+">
    <xsl:param name="includeNotifications" as="xs:boolean" select="false()"/>
    <div class="resultPanel info">
      <h4>Key item session status information</h4>
      <div class="details">
        <ul>
          <li>Entry time: <xsl:value-of select="qw:format-optional-date(@entryTime, '(Not Yet Entered)')"/></li>
          <li>End time: <xsl:value-of select="qw:format-optional-date(@endTime, '(Not Yet Ended)')"/></li>
          <li>Duration accumulated: <xsl:value-of select="@durationAccumulated div 1000.0"/> s</li>
          <li>Initialized: <xsl:value-of select="@initialized"/></li>
          <li>Responded: <xsl:value-of select="@responded"/></li>
          <li><code>sessionStatus</code>: <xsl:value-of select="@sessionStatus"/></li>
          <li><code>numAttempts</code>: <xsl:value-of select="@numAttempts"/></li>
          <li><code>completionStatus</code>: <xsl:value-of select="@completionStatus"/></li>
        </ul>
      </div>
    </div>
    <div class="resultPanel info">
      <h4>Variable state</h4>
      <div class="details">
        <p>The values of all variables are shown below.</p>
        <xsl:apply-templates select="." mode="variableValuesPanel"/>
      </div>
    </div>
    <xsl:if test="@responded='true'">
      <div class="resultPanel info">
        <h4>Response state</h4>
        <div class="details">
          <xsl:apply-templates select="." mode="unboundResponsesPanel"/>
          <xsl:apply-templates select="." mode="invalidResponsesPanel"/>
        </div>
      </div>
    </xsl:if>
    <xsl:apply-templates select="." mode="shuffleStatePanel"/>
    <xsl:if test="$includeNotifications">
      <xsl:call-template name="notificationsPanel"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="qw:itemSessionState" mode="unboundResponsesPanel" as="element(div)">
    <div class="resultPanel {if (exists(@unboundResponseIdentifiers)) then 'failure' else 'success'}">
      <h4>Unbound responses (<xsl:value-of select="count(@unboundResponseIdentifiers)"/>)</h4>
      <div class="details">
        <xsl:choose>
          <xsl:when test="exists(@unboundResponseIdentifiers)">
            <p>
              The responses listed below were not successfully bound to their corresponding variables.
              This might happen, for example, if you bind a <code>&lt;textEntryInteraction&gt;</code> to
              a numeric variable and the candidate enters something that is not a number.
            </p>
            <ul>
              <xsl:for-each select="@unboundResponseIdentifiers">
                <li>
                  <span class="variableName">
                    <xsl:value-of select="."/>
                  </span>
                </li>
              </xsl:for-each>
            </ul>
          </xsl:when>
          <xsl:otherwise>
            <p>
              All responses were successfully bound to response variables.
            </p>
          </xsl:otherwise>
        </xsl:choose>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="qw:itemSessionState" mode="invalidResponsesPanel" as="element(div)">
    <div class="resultPanel {if (exists(@invalidResponseIdentifiers)) then 'failure' else 'success'}">
      <h4>Invalid responses (<xsl:value-of select="count(@invalidResponseIdentifiers)"/>)</h4>
      <div class="details">
        <xsl:choose>
          <xsl:when test="exists(@invalidResponseIdentifiers)">
            <p>
              The responses were successfully bound to their corresponding variables,
              but failed to satisfy the constraints specified by their corresponding interactions:
            </p>
            <ul>
              <xsl:for-each select="@invalidResponseIdentifiers">
                <li>
                  <span class="variableName">
                    <xsl:value-of select="."/>
                  </span>
                </li>
              </xsl:for-each>
            </ul>
          </xsl:when>
          <xsl:otherwise>
            <p>
              All responses satisfied the constraints specified by their correpsonding interactions.
            </p>
          </xsl:otherwise>
        </xsl:choose>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="qw:itemSessionState" mode="variableValuesPanel" as="element(div)*">
    <xsl:if test="exists(qw:outcomeVariable)">
      <div class="resultPanel">
        <h4>Outcome values (<xsl:value-of select="count(qw:outcomeVariable)"/>)</h4>
        <div class="details">
          <xsl:call-template name="dumpValues">
            <xsl:with-param name="valueHolders" select="qw:outcomeVariable"/>
          </xsl:call-template>
        </div>
      </div>
    </xsl:if>
    <xsl:if test="exists(qw:responseVariable)">
      <div class="resultPanel">
        <h4>Response values (<xsl:value-of select="count(qw:responseVariable)"/>)</h4>
        <div class="details">
          <xsl:call-template name="dumpValues">
            <xsl:with-param name="valueHolders" select="qw:responseVariable"/>
          </xsl:call-template>
        </div>
      </div>
    </xsl:if>
    <xsl:if test="exists(qw:templateVariable)">
      <div class="resultPanel">
        <h4>Template values (<xsl:value-of select="count(qw:templateVariable)"/>)</h4>
        <div class="details">
          <xsl:call-template name="dumpValues">
            <xsl:with-param name="valueHolders" select="qw:templateVariable"/>
          </xsl:call-template>
        </div>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template name="notificationsPanel" as="element(div)">
    <div class="resultPanel {if ($notifications[not(@level='INFO')]) then 'failure' else 'success'}">
      <h4><a name="notifications">Processing notifications (<xsl:value-of select="count($notifications)"/>)</a></h4>
      <div class="details">
        <xsl:choose>
          <xsl:when test="count($notifications) > 0">
            <p>
              The following notifications were recorded during this processing run on this item.
              These may indicate issues with your item that need fixed.
            </p>
            <xsl:call-template name="notificationsLevelPanel">
              <xsl:with-param name="level" select="'ERROR'"/>
              <xsl:with-param name="title" select="'Errors'"/>
              <xsl:with-param name="class" select="'failure'"/>
            </xsl:call-template>
            <xsl:call-template name="notificationsLevelPanel">
              <xsl:with-param name="level" select="'WARNING'"/>
              <xsl:with-param name="title" select="'Warnings'"/>
              <xsl:with-param name="class" select="'warnings'"/>
            </xsl:call-template>
            <xsl:call-template name="notificationsLevelPanel">
              <xsl:with-param name="level" select="'INFO'"/>
              <xsl:with-param name="title" select="'Informational'"/>
              <xsl:with-param name="class" select="'success'"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <p>
              No notifications were recorded during this processing run on this item.
            </p>
          </xsl:otherwise>
        </xsl:choose>
      </div>
    </div>
  </xsl:template>

  <xsl:template name="notificationsLevelPanel" as="element(div)?">
    <xsl:param name="level" as="xs:string"/>
    <xsl:param name="title" as="xs:string"/>
    <xsl:param name="class" as="xs:string"/>
    <xsl:variable name="notificationsAtLevel" select="$notifications[@level=$level]" as="element(qw:notification)*"/>
    <xsl:if test="exists($notificationsAtLevel)">
      <div class="resultPanel{if (exists($notificationsAtLevel)) then concat(' ', $class) else ''}">
        <h4><xsl:value-of select="concat($title, ' (', count($notificationsAtLevel), ')')"/></h4>
        <div class="details">
          <table class="notificationsTable">
            <thead>
              <tr>
                <th>Type</th>
                <th>QTI Class</th>
                <th>Attribute</th>
                <th>Line Number</th>
                <th>Column Number</th>
                <th>Message</th>
              </tr>
            </thead>
            <tbody>
              <xsl:for-each select="$notificationsAtLevel">
                <tr>
                  <td><xsl:value-of select="@type"/></td>
                  <td><xsl:value-of select="if (exists(@nodeQtiClassName)) then @nodeQtiClassName else 'N/A'"/></td>
                  <td><xsl:value-of select="if (exists(@attrLocalName)) then @attrLocalName else 'N/A'"/></td>
                  <td><xsl:value-of select="if (exists(@lineNumber)) then @lineNumber else 'Unknown'"/></td>
                  <td><xsl:value-of select="if (exists(@columnNumber)) then @columnNumber else 'Unknown'"/></td>
                  <td><xsl:value-of select="."/></td>
                </tr>
              </xsl:for-each>
            </tbody>
          </table>
        </div>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template match="qw:itemSessionState" mode="shuffleStatePanel" as="element(div)">
    <div class="resultPanel info">
      <h4>Interaction shuffle state</h4>
      <div class="details">
        <xsl:choose>
          <xsl:when test="exists(qw:shuffledInteractionChoiceOrder)">
            <ul>
              <xsl:for-each select="qw:shuffledInteractionChoiceOrder">
                <li>
                  <span class="variableName">
                    <xsl:value-of select="@responseIdentifier"/>
                  </span>
                  <xsl:text> = [</xsl:text>
                  <xsl:value-of select="tokenize(@choiceSequence, ' ')" separator=", "/>
                  <xsl:text>]</xsl:text>
                </li>
              </xsl:for-each>
            </ul>
          </xsl:when>
          <xsl:otherwise>
            <p>There are no shuffled interactions in this item.</p>
          </xsl:otherwise>
        </xsl:choose>
      </div>
    </div>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template name="dumpValues" as="element(ul)">
    <xsl:param name="valueHolders" as="element()*"/>
    <ul>
      <xsl:for-each select="$valueHolders">
        <xsl:call-template name="dumpValue">
          <xsl:with-param name="valueHolder" select="."/>
        </xsl:call-template>
      </xsl:for-each>
    </ul>
  </xsl:template>

  <xsl:template name="dumpValue" as="element(li)">
    <xsl:param name="valueHolder" as="element()"/>
    <li>
      <span class="variableName">
        <xsl:value-of select="@identifier"/>
      </span>
      <xsl:text> = </xsl:text>
      <xsl:choose>
        <xsl:when test="not(*)">
          <xsl:text>NULL</xsl:text>
        </xsl:when>
        <xsl:when test="qw:is-maths-content-value($valueHolder)">
          <!-- We'll handle MathsContent variables specially to help question authors -->
          <span class="type">MathsContent :: </span>
          <xsl:copy-of select="qw:extract-maths-content-pmathml($valueHolder)"/>

          <!-- Make the raw record fields available via a toggle -->
          <xsl:text> </xsl:text>
          <a id="qtiworks_id_toggle_debugMathsContent_{@identifier}" class="debugButton"
            href="javascript:void(0)">Toggle Details</a>
          <div id="qtiworks_id_debugMathsContent_{@identifier}" class="debugMathsContent">
            <xsl:call-template name="dumpRecordEntries">
              <xsl:with-param name="valueHolders" select="$valueHolder/qw:value"/>
            </xsl:call-template>
          </div>
          <script>
            jQuery(document).ready(function() {
              jQuery('a#qtiworks_id_toggle_debugMathsContent_<xsl:value-of select="@identifier"/>').click(function() {
                jQuery('#qtiworks_id_debugMathsContent_<xsl:value-of select="@identifier"/>').toggle();
              })
            });
          </script>
        </xsl:when>
        <xsl:otherwise>
          <!-- Other variables will be output in a fairly generic way -->
          <span class="type">
            <xsl:value-of select="(@cardinality, @baseType, ':: ')" separator=" "/>
          </span>
          <xsl:choose>
            <xsl:when test="@cardinality='single'">
              <xsl:variable name="singleValue" select="$valueHolder/qw:value" as="element(qw:value)"/>
              <xsl:choose>
                <xsl:when test="@baseType='file'">
                  <xsl:value-of select="$singleValue/@fileName"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:variable name="text" select="$singleValue" as="xs:string"/>
                  <xsl:choose>
                    <xsl:when test="contains($text, '&#x0a;')">
                      <pre><xsl:value-of select="$text"/></pre>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="$text"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:when test="@cardinality='multiple'">
              <xsl:text>{</xsl:text>
              <xsl:value-of select="$valueHolder/qw:value" separator=", "/>
              <xsl:text>}</xsl:text>
            </xsl:when>
            <xsl:when test="@cardinality='ordered'">
              <xsl:text>[</xsl:text>
              <xsl:value-of select="$valueHolder/qw:value" separator=", "/>
              <xsl:text>]</xsl:text>
            </xsl:when>
            <xsl:when test="@cardinality='record'">
              <xsl:text>(</xsl:text>
              <xsl:call-template name="dumpRecordEntries">
                <xsl:with-param name="valueHolders" select="$valueHolder/qw:value"/>
              </xsl:call-template>
              <xsl:text>)</xsl:text>
            </xsl:when>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </li>
  </xsl:template>

  <xsl:template name="dumpRecordEntries" as="element(ul)">
    <xsl:param name="valueHolders" as="element()*"/>
    <ul>
      <xsl:for-each select="$valueHolders">
        <li>
          <span class="variableName">
            <xsl:value-of select="@fieldIdentifier"/>
          </span>
          <xsl:text> = </xsl:text>
          <xsl:choose>
            <xsl:when test="not(*)">
              <xsl:text>NULL</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <!-- Other variables will be output in a fairly generic way -->
              <span class="type">
                <xsl:value-of select="(@baseType, ':: ')" separator=" "/>
              </span>
              <xsl:variable name="text" select="qw:value" as="xs:string"/>
              <xsl:choose>
                <xsl:when test="contains($text, '&#x0a;')">
                  <pre><xsl:value-of select="$text"/></pre>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$text"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </li>
      </xsl:for-each>
    </ul>
  </xsl:template>

</xsl:stylesheet>

