<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti qw">

  <xsl:template match="qti:choiceInteraction">
    <input name="qtiworks_presented_{@responseIdentifier}" type="hidden" value="1"/>
    <div class="{local-name()}">
      <xsl:if test="qw:is-invalid-response(@responseIdentifier)">
        <div class="badResponse">
          <xsl:choose>
            <xsl:when test="@minChoices = @maxChoices and @minChoices > 0">
              You must select
              <xsl:value-of select="@minChoices"/>
              <xsl:text> </xsl:text>
              <xsl:value-of select="if (@minChoices = 1) then 'choice' else 'choices'"/>.
            </xsl:when>
            <xsl:otherwise>
              You must select
              <xsl:if test="@minChoices &gt; 0">
                at least <xsl:value-of select="@minChoices"/>
                <xsl:if test="@maxChoices &gt; 0"> and </xsl:if>
              </xsl:if>
              <xsl:if test="@maxChoices &gt; 0">
                at most <xsl:value-of select="@maxChoices"/>
              </xsl:if>
              choices.
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </xsl:if>
      <table id="{if (@id) then @id else concat('choiceInteraction-', @responseIdentifier)}">
        <xsl:if test="qti:prompt">
          <tr class="prompt">
            <td colspan="2">
              <xsl:apply-templates select="qti:prompt"/>
            </td>
          </tr>
        </xsl:if>
        <xsl:if test="@label">
          <tr class="choiceInteractionLabelRow">
            <td class="leftTextLabel">
              <xsl:value-of select="substring-before(@label, '|')"/>
            </td>
            <td class="rightTextLabel">
              <xsl:value-of select="substring-after(@label, '|')"/>
            </td>
          </tr>
        </xsl:if>
        <xsl:apply-templates select="qw:get-visible-ordered-choices(., qti:simpleChoice)"/>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="qti:simpleChoice">
    <tr class="choiceinteraction">
      <xsl:if test="contains(../@class, 'choiceright')">
        <td class="choiceInteraction">
          <xsl:apply-templates/>
        </td>
      </xsl:if>
      <td class="control">
        <input name="qtiworks_response_{../@responseIdentifier}" value="{@identifier}" type="{if (../@maxChoices=1) then 'radio' else 'checkbox'}">
          <xsl:if test="$isItemSessionEnded">
            <xsl:attribute name="disabled">disabled</xsl:attribute>
          </xsl:if>
          <xsl:if test="qw:value-contains(qw:get-response-value(/, ../@responseIdentifier), @identifier)">
            <xsl:attribute name="checked">checked</xsl:attribute>
          </xsl:if>
        </input>
      </td>
      <xsl:if test="not(contains(../@class, 'choiceright'))">
        <td class="choiceInteraction">
          <xsl:apply-templates/>
        </td>
      </xsl:if>
    </tr>
  </xsl:template>

</xsl:stylesheet>
