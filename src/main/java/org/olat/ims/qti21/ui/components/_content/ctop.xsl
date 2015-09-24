<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="m"
>

<!--

Copyright David Carlisle 2001, 2002, 2008, 2009.

Use and distribution of this code are permitted under the terms of the <a
href="http://www.w3.org/Consortium/Legal/copyright-software-19980720"
>W3C Software Notice and License</a>. Or the MIT or MPL 1.1 or MPL 2.0 licences.
2001-2002 MathML2 version
2008-2009     Updates for MathML3
-->

<xsl:output method="xml" omit-xml-declaration="yes"/>

<xsl:template match="m:semantics" mode="c2p" priority="10">
  <xsl:choose>
    <xsl:when test="not(*[position()!=1 and self::m:annotation-xml])">
      <!-- All annotations are non-XML so remove this wrapper completely (and unwrap a container mrow if required) -->
      <xsl:apply-templates select="if (*[1][self::m:mrow]) then *[1]/* else *[1]" mode="c2p"/>
    </xsl:when>
    <xsl:otherwise>
      <!-- Keep non-XML annotations -->
      <xsl:element name="semantics" namespace="http://www.w3.org/1998/Math/MathML">
        <xsl:apply-templates select="* except m:annotation" mode="c2p"/>
      </xsl:element>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
<!-- Remove math elements included by templates --> 
<xsl:template match="*//m:math" mode="c2p">
  <xsl:apply-templates mode="c2p"/>
</xsl:template>
  


<xsl:template match="/">
  <xsl:apply-templates mode="c2p"/>
</xsl:template>

<xsl:template mode="c2p" match="*">
<xsl:copy>
  <xsl:copy-of select="@*"/>
  <xsl:apply-templates mode="c2p"/>
</xsl:copy>
</xsl:template>


<!-- 4.4.1.1 cn -->

<xsl:template mode="c2p" match="m:cn">
 <m:mn><xsl:apply-templates mode="c2p"/></m:mn>
</xsl:template>

<xsl:template mode="c2p" match="m:cn[@type='complex-cartesian']">
  <m:mrow>
    <m:mn><xsl:apply-templates mode="c2p" select="text()[1]"/></m:mn>
    <m:mo>+</m:mo>
    <m:mn><xsl:apply-templates mode="c2p" select="text()[2]"/></m:mn>
    <m:mo>&#8290;<!--invisible times--></m:mo>
    <m:mi>i<!-- imaginary i --></m:mi>
  </m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='complex_cartesian']]">
  <m:mrow>
    <m:mn><xsl:apply-templates mode="c2p" select="*[2]"/></m:mn>
    <m:mo>+</m:mo>
    <m:mn><xsl:apply-templates mode="c2p" select="*[3]"/></m:mn>
    <m:mo>&#8290;<!--invisible times--></m:mo>
    <m:mi>i<!-- imaginary i --></m:mi>
  </m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:cn[@type='rational']">
  <m:mrow>
    <m:mn><xsl:apply-templates mode="c2p" select="text()[1]"/></m:mn>
    <m:mo>/</m:mo>
    <m:mn><xsl:apply-templates mode="c2p" select="text()[2]"/></m:mn>
  </m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='rational']]">
  <m:mrow>
    <m:mn><xsl:apply-templates mode="c2p" select="*[2]"/></m:mn>
    <m:mo>/</m:mo>
    <m:mn><xsl:apply-templates mode="c2p" select="*[3]"/></m:mn>
  </m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:cn[not(@type) or @type='integer']">
  <xsl:choose>
  <xsl:when test="not(@base) or @base=10">
       <m:mn><xsl:apply-templates mode="c2p"/></m:mn>
  </xsl:when>
  <xsl:otherwise>
  <m:msub>
    <m:mn><xsl:apply-templates mode="c2p"/></m:mn>
    <m:mn><xsl:value-of select="@base"/></m:mn>
  </m:msub>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template mode="c2p" match="m:cn[@type='complex-polar']">
  <m:mrow>
    <m:mn><xsl:apply-templates mode="c2p" select="text()[1]"/></m:mn>
    <m:mo>&#8290;<!--invisible times--></m:mo>
    <m:msup>
    <m:mi>e<!-- exponential e--></m:mi>
    <m:mrow>
     <m:mi>i<!-- imaginary i--></m:mi>
     <m:mo>&#8290;<!--invisible times--></m:mo>
     <m:mn><xsl:apply-templates mode="c2p" select="text()[2]"/></m:mn>
    </m:mrow>
    </m:msup>
  </m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='complex_polar']]">
  <m:mrow>
    <xsl:apply-templates mode="c2p" select="*[2]"/>
    <m:mo>&#8290;<!--invisible times--></m:mo>
    <m:msup>
    <m:mi>e<!-- exponential e--></m:mi>
    <m:mrow>
     <m:mi>i<!-- imaginary i--></m:mi>
     <m:mo>&#8290;<!--invisible times--></m:mo>
     <xsl:apply-templates mode="c2p" select="*[3]"/>
    </m:mrow>
    </m:msup>
  </m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:cn[@type='e-notation']">
  <m:mn>
    <xsl:apply-templates mode="c2p" select="m:sep/preceding-sibling::node()"/>
    <xsl:text>E</xsl:text>
    <xsl:apply-templates mode="c2p" select="m:sep/following-sibling::node()"/>
  </m:mn>
</xsl:template>

<xsl:template mode="c2p" match="m:cn[@type='hexdouble']">
  <m:mn>
    <xsl:text>0x</xsl:text>
    <xsl:apply-templates mode="c2p"/>
  </m:mn>
</xsl:template>

<!-- 4.4.1.1 ci  -->

<xsl:template mode="c2p" match="m:ci/text()">
 <m:mi><xsl:value-of select="."/></m:mi>
</xsl:template>

<xsl:template mode="c2p" match="m:ci">
 <m:mrow><xsl:apply-templates mode="c2p"/></m:mrow>
</xsl:template>

<!-- 4.4.1.2 csymbol -->

<xsl:template mode="c2p" match="m:csymbol/text()">
 <m:mi><xsl:value-of select="."/></m:mi><!-- Robin Green r.d.greenATlancaster.ac.uk, Christoph Lange langecATweb.de-->
</xsl:template>

<xsl:template mode="c2p" match="m:csymbol">
 <m:mrow><xsl:apply-templates mode="c2p"/></m:mrow>
</xsl:template>

<!-- 4.4.2.1 apply 4.4.2.2 reln -->

<xsl:template mode="c2p" match="m:apply|m:reln">
 <m:mrow>
   <xsl:choose>
     <xsl:when test="*[1]/*/*">
       <m:mfenced separators="">
	 <xsl:apply-templates mode="c2p" select="*[1]">
	   <xsl:with-param name="p" select="10"/>
	 </xsl:apply-templates>
       </m:mfenced>
     </xsl:when>
     <xsl:otherwise>       
       <xsl:apply-templates mode="c2p" select="*[1]">
	 <xsl:with-param name="p" select="10"/>
       </xsl:apply-templates>
     </xsl:otherwise>
   </xsl:choose>
 <m:mo>&#8289;<!--function application--></m:mo>
 <m:mfenced open="(" close=")" separators=",">
 <xsl:apply-templates mode="c2p" select="*[position()>1]"/>
 </m:mfenced>
 </m:mrow>
</xsl:template>


<xsl:template mode="c2p" match="m:bind">
 <m:mrow>
   <xsl:choose>
     <xsl:when test="*[1]/*/*">
       <m:mfenced separators="">
	 <xsl:apply-templates mode="c2p" select="*[1]">
	   <xsl:with-param name="p" select="10"/>
	 </xsl:apply-templates>
       </m:mfenced>
     </xsl:when>
     <xsl:otherwise>       
       <xsl:apply-templates mode="c2p" select="*[1]">
	 <xsl:with-param name="p" select="10"/>
       </xsl:apply-templates>
     </xsl:otherwise>
   </xsl:choose>
   <xsl:apply-templates select="bvar/*"/>
   <m:mo>.</m:mo>
   <xsl:apply-templates mode="c2p" select="*[position()>1][not(self::m:bvar)]"/>
 </m:mrow>
</xsl:template>

<!-- 4.4.2.3 fn -->
<xsl:template mode="c2p" match="m:fn">
 <m:mrow><xsl:apply-templates mode="c2p"/></m:mrow>
</xsl:template>

<!-- 4.4.2.4 interval -->
<xsl:template mode="c2p" match="m:interval[*[2]]">
 <m:mfenced open="[" close="]"><xsl:apply-templates mode="c2p"/></m:mfenced>
</xsl:template>
<xsl:template mode="c2p" match="m:interval[*[2]][@closure='open']" priority="2">
 <m:mfenced open="(" close=")"><xsl:apply-templates mode="c2p"/></m:mfenced>
</xsl:template>
<xsl:template mode="c2p" match="m:interval[*[2]][@closure='open-closed']" priority="2">
 <m:mfenced open="(" close="]"><xsl:apply-templates mode="c2p"/></m:mfenced>
</xsl:template>
<xsl:template mode="c2p" match="m:interval[*[2]][@closure='closed-open']" priority="2">
 <m:mfenced open="[" close=")"><xsl:apply-templates mode="c2p"/></m:mfenced>
</xsl:template>

<xsl:template mode="c2p" match="m:interval">
 <m:mfenced open="{{" close="}}"><xsl:apply-templates mode="c2p"/></m:mfenced>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='integer_interval']]">
 <m:mfenced open="[" close="]"><xsl:apply-templates mode="c2p" select="*[position()!=1]"/></m:mfenced>
</xsl:template>
<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='interval']]">
 <m:mfenced open="[" close="]"><xsl:apply-templates mode="c2p" select="*[position()!=1]"/></m:mfenced>
</xsl:template>
<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='interval-cc']]">
 <m:mfenced open="[" close="]"><xsl:apply-templates mode="c2p" select="*[position()!=1]"/></m:mfenced>
</xsl:template>
<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='interval-oo']]">
 <m:mfenced open="(" close=")"><xsl:apply-templates mode="c2p" select="*[position()!=1]"/></m:mfenced>
</xsl:template>
<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='oriented_interval']]">
 <m:mfenced open="(" close=")"><xsl:apply-templates mode="c2p" select="*[position()!=1]"/></m:mfenced>
</xsl:template>

<!-- 4.4.2.5 inverse -->

<xsl:template mode="c2p" match="m:apply[*[1][self::m:inverse]]
                       |m:apply[*[1][self::m:csymbol='inverse']]">
 <m:msup>
  <xsl:apply-templates mode="c2p" select="*[2]"/>
  <m:mrow><m:mo>(</m:mo><m:mn>-1</m:mn><m:mo>)</m:mo></m:mrow>
 </m:msup>
</xsl:template>

<!-- 4.4.2.6 sep -->

<!-- 4.4.2.7 condition -->
<xsl:template mode="c2p" match="m:condition">
 <m:mrow><xsl:apply-templates mode="c2p"/></m:mrow>
</xsl:template>

<!-- 4.4.2.8 declare -->
<xsl:template mode="c2p" match="m:declare"/>

<!-- 4.4.2.9 lambda -->
<xsl:template mode="c2p" match="m:lambda
				|m:apply[*[1][self::m:csymbol='lambda']]
				|m:bind[*[1][self::m:csymbol='lambda']]"><!--dpc-->
 <m:mrow>
  <m:mi>&#955;<!--lambda--></m:mi>
 <m:mrow><xsl:apply-templates mode="c2p" select="m:bvar/*"/></m:mrow>
 <m:mo>.</m:mo>
 <m:mfenced>
  <xsl:apply-templates mode="c2p" select="*[last()]"/>
 </m:mfenced>
</m:mrow>
</xsl:template>


<!-- 4.4.2.10 compose -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:compose]]
                       |m:apply[*[1][self::m:csymbol='left_compose']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="1"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&#8728;<!-- o --></m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>


<!-- 4.4.2.11` ident -->
<xsl:template mode="c2p" match="m:ident">
<m:mi>id</m:mi>
</xsl:template>

<!-- 4.4.2.12` domain -->
<xsl:template mode="c2p" match="m:domain">
<m:mi>domain</m:mi>
</xsl:template>

<!-- 4.4.2.13` codomain -->
<xsl:template mode="c2p" match="m:codomain">
<m:mi>codomain</m:mi>
</xsl:template>

<!-- 4.4.2.14` image -->
<xsl:template mode="c2p" match="m:image">
<m:mi>image</m:mi>
</xsl:template>

<!-- 4.4.2.15` domainofapplication -->
<xsl:template mode="c2p" match="m:domainofapplication">
 <m:merror><m:mtext>unexpected domainofapplication</m:mtext></m:merror>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[2][self::m:bvar]][m:domainofapplication]" priority="0.4">
 <m:mrow>
  <m:munder>
   <xsl:apply-templates mode="c2p" select="*[1]"/>
   <m:mrow>
    <xsl:apply-templates mode="c2p" select="m:bvar/*"/>
    <m:mo>&#8712;<!-- in --></m:mo>
    <xsl:apply-templates mode="c2p" select="m:domainofapplication/*"/>
   </m:mrow>
  </m:munder>
  <m:mfenced>
   <xsl:apply-templates mode="c2p" select="m:domainofapplication/following-sibling::*"/>
  </m:mfenced>
 </m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[m:domainofapplication]" priority="0.3">
 <m:mrow>
  <m:mrow><m:mi>restriction</m:mi>
  <m:mfenced>
   <xsl:apply-templates mode="c2p" select="*[1]"/>
   <xsl:apply-templates mode="c2p" select="m:domainofapplication/*"/>
  </m:mfenced>
  </m:mrow>
  <m:mfenced>
   <xsl:apply-templates mode="c2p" select="m:domainofapplication/following-sibling::*"/>
  </m:mfenced>
 </m:mrow>
</xsl:template>

<!-- 4.4.2.16` piecewise -->
<xsl:template mode="c2p" match="m:piecewise">
  <m:mrow>
    <m:mo>{</m:mo>
    <m:mtable>
      <xsl:for-each select="m:piece|m:otherwise">
	<m:mtr>
	  <m:mtd><xsl:apply-templates mode="c2p" select="*[1]"/></m:mtd>
	  <xsl:choose><!--dpc-->
	    <xsl:when  test="self::m:piece">
	      <m:mtd columnalign="left"><m:mtext>&#160; if &#160;</m:mtext></m:mtd>
	      <m:mtd><xsl:apply-templates mode="c2p" select="*[2]"/></m:mtd>
	    </xsl:when>
	    <xsl:otherwise>
	      <m:mtd colspan="2" columnalign="left"><m:mtext>&#160; otherwise</m:mtext></m:mtd>
	    </xsl:otherwise>
	  </xsl:choose>
	</m:mtr>
      </xsl:for-each>
    </m:mtable>
  </m:mrow>
</xsl:template>


<!-- 4.4.3.1 quotient -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:quotient]]
                       |m:apply[*[1][self::m:csymbol='quotient']]">
<m:mrow>
<m:mo>&#8970;<!-- lfloor--></m:mo>
<xsl:apply-templates mode="c2p" select="*[2]"/>
<m:mo>/</m:mo>
<xsl:apply-templates mode="c2p" select="*[3]"/>
<m:mo>&#8971;<!-- rfloor--></m:mo>
</m:mrow>
</xsl:template>



<!-- 4.4.3.2 factorial -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:factorial]]
				|m:apply[*[1][self::m:csymbol='factorial']]">
<m:mrow>
<xsl:apply-templates mode="c2p" select="*[2]">
  <xsl:with-param name="p" select="7"/>
</xsl:apply-templates>
<m:mo>!</m:mo>
</m:mrow>
</xsl:template>


<!-- 4.4.3.3 divide -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:divide]]
				|m:apply[*[1][self::m:csymbol='divide']]">
  <xsl:param name="p" select="0"/>
<xsl:call-template name="binary">
  <xsl:with-param name="mo"><m:mo>/</m:mo></xsl:with-param>
  <xsl:with-param name="p" select="$p"/>
  <xsl:with-param name="this-p" select="3"/>
</xsl:call-template>
</xsl:template>


<!-- 4.4.3.4 max  min-->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:max]]
				|m:apply[*[1][self::m:csymbol='max']]">
<m:mrow>
  <m:mi>max</m:mi>
  <xsl:call-template name="set"/>
</m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:min]]|m:reln[*[1][self::m:min]]">
<m:mrow>
  <m:mi>min</m:mi><!--dpc-->
  <xsl:call-template name="set"/>
</m:mrow>
</xsl:template>

<!-- 4.4.3.5  minus-->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:minus] and count(*)=2]
				|m:apply[*[1][self::m:csymbol='unary_minus']]">
<m:mrow>
  <m:mo>&#8722;<!--minus--></m:mo>
  <xsl:apply-templates mode="c2p" select="*[2]">
      <xsl:with-param name="p" select="5"/>
  </xsl:apply-templates>
</m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:minus] and count(*)&gt;2]
				|m:apply[*[1][self::m:csymbol='minus']]">
  <xsl:param name="p" select="0"/>
<xsl:call-template name="binary">
  <xsl:with-param name="mo"><m:mo>&#8722;<!--minus--></m:mo></xsl:with-param>
  <xsl:with-param name="p" select="$p"/>
  <xsl:with-param name="this-p" select="2"/>
</xsl:call-template>
</xsl:template>

<!-- 4.4.3.6  plus-->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:plus]]
				|m:apply[*[1][self::m:csymbol='plus']]">
  <xsl:param name="p" select="0"/>
  <m:mrow>
  <xsl:if test="$p &gt; 2"><m:mo>(</m:mo></xsl:if>
  <xsl:for-each select="*[position()&gt;1]">
   <xsl:if test="position() &gt; 1">
    <m:mo>
    <xsl:choose>
      <xsl:when test="self::m:apply[*[1][self::m:times] and
      *[2][self::m:apply/*[1][self::m:minus] or self::m:cn[not(m:sep) and
      (number(.) &lt; 0)]]]">&#8722;<!--minus--></xsl:when>
      <xsl:otherwise>+</xsl:otherwise>
    </xsl:choose>
    </m:mo>
   </xsl:if>   
    <xsl:choose>
      <xsl:when test="self::m:apply[*[1][self::m:times] and
      *[2][self::m:cn[not(m:sep) and (number(.) &lt;0)]]]">
     <m:mrow>
     <m:mn><xsl:value-of select="-(*[2])"/></m:mn>
      <m:mo>&#8290;<!--invisible times--></m:mo>
     <xsl:apply-templates mode="c2p" select=".">
     <xsl:with-param name="first" select="2"/>
     <xsl:with-param name="p" select="2"/>
   </xsl:apply-templates>
     </m:mrow>
      </xsl:when>
      <xsl:when test="self::m:apply[*[1][self::m:times] and
      *[2][self::m:apply/*[1][self::m:minus]]]">
     <m:mrow>
     <xsl:apply-templates mode="c2p" select="./*[2]/*[2]"/>
     <xsl:apply-templates mode="c2p" select=".">
     <xsl:with-param name="first" select="2"/>
     <xsl:with-param name="p" select="2"/>
   </xsl:apply-templates>
     </m:mrow>
      </xsl:when>
      <xsl:otherwise>
     <xsl:apply-templates mode="c2p" select=".">
     <xsl:with-param name="p" select="2"/>
   </xsl:apply-templates>
   </xsl:otherwise>
    </xsl:choose>
  </xsl:for-each>
  <xsl:if test="$p &gt; 2"><m:mo>)</m:mo></xsl:if>
  </m:mrow>
</xsl:template>


<!-- 4.4.3.7 power -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:power]]
				|m:apply[*[1][self::m:csymbol='power']]">
<m:msup>
<xsl:apply-templates mode="c2p" select="*[2]">
  <xsl:with-param name="p" select="5"/>
</xsl:apply-templates>
<xsl:apply-templates mode="c2p" select="*[3]">
  <xsl:with-param name="p" select="5"/>
</xsl:apply-templates>
</m:msup>
</xsl:template>

<!-- 4.4.3.8 remainder -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:rem]]
                       |m:apply[*[1][self::m:csymbol='rem']]">
  <xsl:param name="p" select="0"/>
<xsl:call-template name="binary">
  <xsl:with-param name="mo"><m:mo>mod</m:mo></xsl:with-param>
  <xsl:with-param name="p" select="$p"/>
  <xsl:with-param name="this-p" select="3"/>
</xsl:call-template>
</xsl:template>

<!-- 4.4.3.9  times-->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:times]]
				|m:apply[*[1][self::m:csymbol='times']]"
	      name="times">
  <xsl:param name="p" select="0"/>
  <xsl:param name="first" select="1"/>
  <m:mrow>
  <xsl:if test="$p &gt; 3"><m:mo>(</m:mo></xsl:if>
  <xsl:for-each select="*[position()&gt;1]">
   <xsl:if test="position() &gt; 1">
    <m:mo>
    <xsl:choose>
      <xsl:when test="self::m:cn">&#215;<!-- times --></xsl:when>
      <xsl:otherwise>&#8290;<!--invisible times--></xsl:otherwise>
    </xsl:choose>
    </m:mo>
   </xsl:if> 
   <xsl:if test="position()&gt;= $first">
   <xsl:apply-templates mode="c2p" select=".">
     <xsl:with-param name="p" select="3"/>
   </xsl:apply-templates>
   </xsl:if>
  </xsl:for-each>
  <xsl:if test="$p &gt; 3"><m:mo>)</m:mo></xsl:if>
  </m:mrow>
</xsl:template>


<!-- 4.4.3.10 root -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:root] and not(m:degree) or m:degree=2]" priority="4">
<m:msqrt>
<xsl:apply-templates mode="c2p" select="*[position()&gt;1]"/>
</m:msqrt>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:root]]">
<m:mroot>
<xsl:apply-templates mode="c2p" select="*[position()&gt;1 and not(self::m:degree)]"/>
<m:mrow><xsl:apply-templates mode="c2p" select="m:degree/*"/></m:mrow>
</m:mroot>
</xsl:template>


<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='root']]">
<m:mroot>
  <xsl:apply-templates mode="c2p" select="*[position()!=1]"/>
</m:mroot>
</xsl:template>

<!-- 4.4.3.11 gcd -->
<xsl:template mode="c2p" match="m:gcd">
<m:mi>gcd</m:mi>
</xsl:template>

<!-- 4.4.3.12 and -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:and]]
				|m:reln[*[1][self::m:and]]
				|m:apply[*[1][self::m:csymbol='and']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="2"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&#8743;<!-- and --></m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>


<!-- 4.4.3.13 or -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:or]]
                       |m:apply[*[1][self::m:csymbol='or']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="3"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&#8744;<!-- or --></m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 4.4.3.14 xor -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:xor]]
                       |m:apply[*[1][self::m:csymbol='xor']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="3"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>xor</m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>


<!-- 4.4.3.15 not -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:not]]
                       |m:apply[*[1][self::m:csymbol='not']]">
<m:mrow>
<m:mo>&#172;<!-- not --></m:mo>
<xsl:apply-templates mode="c2p" select="*[2]">
  <xsl:with-param name="p" select="7"/>
</xsl:apply-templates>
</m:mrow>
</xsl:template>




<!-- 4.4.3.16 implies -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:implies]]
				|m:reln[*[1][self::m:implies]]
				|m:apply[*[1][self::m:csymbol='implies']]">
  <xsl:param name="p" select="0"/>
<xsl:call-template name="binary">
  <xsl:with-param name="mo"><m:mo>&#8658;<!-- Rightarrow --></m:mo></xsl:with-param>
  <xsl:with-param name="p" select="$p"/>
  <xsl:with-param name="this-p" select="3"/>
</xsl:call-template>
</xsl:template>


<!-- 4.4.3.17 forall -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:forall]]
                       |m:apply[*[1][self::m:csymbol='forall']]
                       |m:bind[*[1][self::m:forall]]
                       |m:bind[*[1][self::m:csymbol='forall']]">
 <m:mrow>
  <m:mo>&#8704;<!--forall--></m:mo>
 <m:mrow><xsl:apply-templates mode="c2p" select="m:bvar[not(current()/m:condition)]/*|m:condition/*"/></m:mrow>
 <m:mo>.</m:mo>
 <m:mfenced>
  <xsl:apply-templates mode="c2p" select="*[last()]"/>
 </m:mfenced>
</m:mrow>
</xsl:template>



<!-- 4.4.3.18 exists -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:exists]]
                       |m:apply[*[1][self::m:csymbol='exists']]
                       |m:bind[*[1][self::m:exists]]
                       |m:bind[*[1][self::m:csymbol='exists']]">
 <m:mrow>
  <m:mo>&#8707;<!--exists--></m:mo>
 <m:mrow><xsl:apply-templates mode="c2p" select="m:bvar[not(current()/m:condition)]/*|m:condition/*"/></m:mrow>
 <m:mo>.</m:mo>
 <m:mfenced separators="">
   <xsl:choose>
     <xsl:when test="m:condition">
       <xsl:apply-templates mode="c2p" select="m:condition/*"/>
       <m:mo>&#8743;<!-- and --></m:mo>
     </xsl:when>
     <xsl:when test="m:domainofapplication">
       <m:mrow>
       <m:mrow>
	 <xsl:for-each select="m:bvar">
	   <xsl:apply-templates mode="c2p"/>
	   <xsl:if test="position()!=last()">
	     <m:mo>,</m:mo>
	   </xsl:if>
	 </xsl:for-each>
       </m:mrow>
       <m:mo>&#8712;<!-- in --></m:mo>
       <xsl:apply-templates mode="c2p" select="m:domainofapplication/*"/>
       </m:mrow>
       <m:mo>&#8743;<!-- and --></m:mo>
     </xsl:when>
   </xsl:choose>
  <xsl:apply-templates mode="c2p" select="*[last()]"/>
 </m:mfenced>
</m:mrow>
</xsl:template>



<!-- 4.4.3.19 abs -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:abs]]
                       |m:apply[*[1][self::m:csymbol='abs']]">
<m:mrow>
<m:mo>|</m:mo>
<xsl:apply-templates mode="c2p" select="*[2]"/>
<m:mo>|</m:mo>
</m:mrow>
</xsl:template>



<!-- 4.4.3.20 conjugate -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:conjugate]]
                       |m:apply[*[1][self::m:csymbol='conjugate']]">
<m:mover>
<xsl:apply-templates mode="c2p" select="*[2]"/>
<m:mo>&#175;<!-- overline --></m:mo>
</m:mover>
</xsl:template>

<!-- 4.4.3.21 arg -->
<xsl:template mode="c2p" match="m:arg">
 <m:mi>arg</m:mi>
</xsl:template>


<!-- 4.4.3.22 real -->
<xsl:template mode="c2p" match="m:real|m:csymbol[.='real']">
 <m:mo>&#8475;<!-- real --></m:mo>
</xsl:template>

<!-- 4.4.3.23 imaginary -->
<xsl:template mode="c2p" match="m:imaginary|m:csymbol[.='imaginary']">
 <m:mo>&#8465;<!-- imaginary --></m:mo>
</xsl:template>

<!-- 4.4.3.24 lcm -->
<xsl:template mode="c2p" match="m:lcm">
 <m:mi>lcm</m:mi>
</xsl:template>


<!-- 4.4.3.25 floor -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:floor]]
                       |m:apply[*[1][self::m:csymbol='floor']]">
<m:mrow>
<m:mo>&#8970;<!-- lfloor--></m:mo>
<xsl:apply-templates mode="c2p" select="*[2]"/>
<m:mo>&#8971;<!-- rfloor--></m:mo>
</m:mrow>
</xsl:template>


<!-- 4.4.3.25 ceiling -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:ceiling]]
                       |m:apply[*[1][self::m:csymbol='ceiling']]">
<m:mrow>
<m:mo>&#8968;<!-- lceil--></m:mo>
<xsl:apply-templates mode="c2p" select="*[2]"/>
<m:mo>&#8969;<!-- rceil--></m:mo>
</m:mrow>
</xsl:template>

<!-- 4.4.4.1 eq -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:eq]]
				|m:reln[*[1][self::m:eq]]
				|m:apply[*[1][self::m:csymbol='eq']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="1"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>=</m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 4.4.4.2 neq -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:neq]]
                       |m:apply[*[1][self::m:csymbol='neq']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="1"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&#8800;<!-- neq --></m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 4.4.4.3 eq -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:gt]]
				|m:reln[*[1][self::m:gt]]
				|m:apply[*[1][self::m:csymbol='gt']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="1"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&gt;</m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 4.4.4.4 lt -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:lt]]
				|m:reln[*[1][self::m:lt]]
				|m:apply[*[1][self::m:csymbol='lt']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="1"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&lt;</m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 4.4.4.5 geq -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:geq]]
				|m:apply[*[1][self::m:csymbol='geq']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="1"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&#8805;</m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 4.4.4.6 geq -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:leq]]
                       |m:apply[*[1][self::m:csymbol='leq']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="1"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&#8804;</m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 4.4.4.7 equivalent -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:equivalent]]
                       |m:apply[*[1][self::m:csymbol='equivalent']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="1"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&#8801;</m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 4.4.4.8 approx -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:approx]]
                       |m:apply[*[1][self::m:csymbol='approx']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="1"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&#8771;</m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>


<!-- 4.4.4.9 factorof -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:factorof]]
                       |m:apply[*[1][self::m:csymbol='factorof']]">
  <xsl:param name="p" select="0"/>
<xsl:call-template name="binary">
  <xsl:with-param name="mo"><m:mo>|</m:mo></xsl:with-param>
  <xsl:with-param name="p" select="$p"/>
  <xsl:with-param name="this-p" select="3"/>
</xsl:call-template>
</xsl:template>

<!-- 4.4.5.1 int -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:int]]
                       |m:apply[*[1][self::m:csymbol='int']]
                       |m:bind[*[1][self::m:int]]
                       |m:bind[*[1][self::m:csymbol='int']]">
 <m:mrow>
 <m:msubsup>
  <m:mi>&#8747;<!--int--></m:mi>
 <m:mrow><xsl:apply-templates mode="c2p" select="m:lowlimit/*|m:interval/*[1]|m:condition/*|m:domainofapplication/*"/></m:mrow>
 <m:mrow><xsl:apply-templates mode="c2p" select="m:uplimit/*|m:interval/*[2]"/></m:mrow>
 </m:msubsup>
 <xsl:apply-templates mode="c2p" select="*[last()]"/>
 <xsl:if test="m:bvar">
   <m:mi>d</m:mi><xsl:apply-templates mode="c2p" select="m:bvar"/>
 </xsl:if>
</m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='defint']]">
<m:mrow>
<m:munder><m:mi>&#8747;<!--int--></m:mi>
<xsl:apply-templates mode="c2p" select="*[2]"/>
</m:munder>
 <xsl:apply-templates mode="c2p" select="*[last()]"/>
</m:mrow>
</xsl:template>

<!-- 4.4.5.2 diff -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:diff] and not(m:bvar)]|
				m:apply[*[1][self::m:csymbol='diff']]" priority="2">
 <m:msup>
 <m:mrow><xsl:apply-templates mode="c2p" select="*[2]"/></m:mrow>
 <m:mo>&#8242;<!--prime--></m:mo>
 </m:msup>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:diff]]" priority="1">
 <m:mfrac>
 <xsl:choose>
 <xsl:when test="m:bvar/m:degree">
 <m:mrow><m:msup><m:mi>d</m:mi><xsl:apply-templates mode="c2p" select="m:bvar/m:degree/node()"/></m:msup>
     <xsl:apply-templates mode="c2p"  select="*[last()]"/></m:mrow>
 <m:mrow><m:mi>d</m:mi><m:msup><xsl:apply-templates mode="c2p"
 select="m:bvar/node()"/><xsl:apply-templates mode="c2p"
 select="m:bvar/m:degree/node()"/></m:msup>
</m:mrow>
</xsl:when>
<xsl:otherwise>
 <m:mrow><m:mi>d</m:mi><xsl:apply-templates mode="c2p" select="*[last()]"/></m:mrow>
 <m:mrow><m:mi>d</m:mi><xsl:apply-templates mode="c2p" select="m:bvar"/></m:mrow>
</xsl:otherwise>
 </xsl:choose>
 </m:mfrac>
</xsl:template>


<!-- 4.4.5.3 partialdiff -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:partialdiff] and m:list and m:ci and count(*)=3]" priority="2">
<m:mrow>
 <m:msub><m:mi>D</m:mi><m:mrow>
<xsl:for-each select="m:list[1]/*">
<xsl:apply-templates mode="c2p" select="."/>
<xsl:if test="position()&lt;last()"><m:mo>,</m:mo></xsl:if>
</xsl:for-each>
</m:mrow></m:msub>
 <m:mrow><xsl:apply-templates mode="c2p" select="*[3]"/></m:mrow>
</m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:partialdiff]]" priority="1">
  <m:mfrac>
    <m:mrow>
      <xsl:choose><!--dpc-->
	<xsl:when test="not(m:bvar/m:degree) and not(m:bvar[2])">
	  <m:mo>&#8706;<!-- partial --></m:mo>
	</xsl:when>
	<xsl:otherwise>
	  <m:msup><m:mo>&#8706;<!-- partial --></m:mo>
	  <m:mrow>
	    <xsl:choose>
	      <xsl:when test="m:degree">
		<xsl:apply-templates mode="c2p" select="m:degree/node()"/>
	      </xsl:when>
	      <xsl:when test="m:bvar/m:degree[string(number(.))='NaN']">
		<xsl:for-each select="m:bvar/m:degree">
		  <xsl:apply-templates mode="c2p" select="node()"/>
		  <xsl:if test="position()&lt;last()"><m:mo>+</m:mo></xsl:if>
		</xsl:for-each>
		<xsl:if test="count(m:bvar[not(m:degree)])&gt;0">
		  <m:mo>+</m:mo><m:mn><xsl:value-of select="count(m:bvar[not(m:degree)])"/></m:mn>
		</xsl:if>
	      </xsl:when>
	      <xsl:otherwise>
		<m:mn><xsl:value-of select="number(sum(m:bvar/m:degree))+count(m:bvar[not(m:degree)])"/></m:mn>
	      </xsl:otherwise>
	    </xsl:choose>
	  </m:mrow>
	  </m:msup>
	</xsl:otherwise>
      </xsl:choose>
    <xsl:apply-templates mode="c2p"  select="*[last()]"/></m:mrow>
    <m:mrow>
      <xsl:for-each select="m:bvar">
	<m:mrow>
	  <m:mo>&#8706;<!-- partial --></m:mo><m:msup><xsl:apply-templates mode="c2p" select="node()"/>
	  <m:mrow><xsl:apply-templates mode="c2p" select="m:degree/node()"/></m:mrow>
	</m:msup>
	</m:mrow>
      </xsl:for-each>
    </m:mrow>
  </m:mfrac>
</xsl:template>


<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='partialdiffdegree']]">
  <m:mrow>
   <m:msub>
    <m:mo>&#8706;<!-- partial --></m:mo>
    <m:mrow>
     <xsl:apply-templates mode="c2p" select="*[2]"/>
    </m:mrow>
   </m:msub>
   <m:mfenced>
     <xsl:apply-templates mode="c2p" select="*[4]"/>
   </m:mfenced>
  </m:mrow>
</xsl:template>


<!-- 4.4.5.4  lowlimit-->
<xsl:template mode="c2p" match="m:lowlimit"/>

<!-- 4.4.5.5 uplimit-->
<xsl:template mode="c2p" match="m:uplimit"/>

<!-- 4.4.5.6  bvar-->
<xsl:template mode="c2p" match="m:bvar">
 <m:mi><xsl:apply-templates mode="c2p"/></m:mi>
 <xsl:if test="following-sibling::m:bvar"><m:mo>,</m:mo></xsl:if>
</xsl:template>

<!-- 4.4.5.7 degree-->
<xsl:template mode="c2p" match="m:degree"/>

<!-- 4.4.5.8 divergence-->
<xsl:template mode="c2p" match="m:divergence">
<m:mi>div</m:mi>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:divergence]and m:bvar and m:vector]">
<xsl:variable name="v" select="m:bvar"/>
<m:mrow>
<m:mi>div</m:mi>
<m:mo>&#8289;<!--function application--></m:mo>
<m:mo>(</m:mo>
<m:mtable>
<xsl:for-each select="m:vector/*">
<xsl:variable name="p" select="position()"/>
<m:mtr><m:mtd>
<xsl:apply-templates mode="c2p" select="$v[$p]/*"/>
<m:mo>&#x21a6;<!-- map--></m:mo>
<xsl:apply-templates mode="c2p" select="."/>
</m:mtd></m:mtr>
</xsl:for-each>
</m:mtable>
<m:mo>)</m:mo>
</m:mrow>
</xsl:template>

<!-- 4.4.5.9 grad-->
<xsl:template mode="c2p" match="m:grad">
<m:mi>grad</m:mi>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:grad]and m:bvar]">
<m:mrow>
<m:mi>grad</m:mi>
<m:mo>&#8289;<!--function application--></m:mo>
<m:mrow>
<m:mo>(</m:mo>
<m:mfenced>
<xsl:apply-templates mode="c2p" select="m:bvar/*"/>
</m:mfenced>
<m:mo>&#x21a6;<!-- map--></m:mo>
<xsl:apply-templates mode="c2p" select="*[position()!=1][not(self::m:bvar)]"/>
<m:mo>)</m:mo>
</m:mrow>
</m:mrow>
</xsl:template>

<!-- 4.4.5.10 curl -->
<xsl:template mode="c2p" match="m:curl">
<m:mi>curl</m:mi>
</xsl:template>


<!-- 4.4.5.11 laplacian-->
<xsl:template mode="c2p" match="m:laplacian">
<m:msup><m:mo>&#8711;<!-- nabla --></m:mo><m:mn>2</m:mn></m:msup>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:laplacian]and m:bvar]">
<m:mrow>
<xsl:apply-templates mode="c2p" select="*[1]"/>
<m:mo>&#8289;<!--function application--></m:mo>
<m:mrow>
<m:mo>(</m:mo>
<m:mfenced>
<xsl:apply-templates mode="c2p" select="m:bvar/*"/>
</m:mfenced>
<m:mo>&#x21a6;<!-- map--></m:mo>
<xsl:apply-templates mode="c2p" select="*[position()!=1][not(self::m:bvar)]"/>
<m:mo>)</m:mo>
</m:mrow>
</m:mrow>
</xsl:template>

<!-- 4.4.6.1 set -->

<xsl:template mode="c2p" match="m:set">
  <xsl:call-template name="set"/>
</xsl:template>

<xsl:template mode="c2p"  match="m:apply[*[1][self::m:csymbol='set']]">
<m:mfenced open="{{" close="}}" separators=",">
  <xsl:apply-templates mode="c2p" select="*[position()!=1]"/>
</m:mfenced>
</xsl:template>

<!-- 4.4.6.2 list -->

<xsl:template mode="c2p" match="m:list">
  <xsl:call-template name="set">
   <xsl:with-param name="o" select="'('"/>
   <xsl:with-param name="c" select="')'"/>
  </xsl:call-template>
</xsl:template>


<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='list']]">
<m:mfenced open="(" close=")" separators=",">
  <xsl:apply-templates mode="c2p" select="*[position()!=1]"/>
</m:mfenced>
</xsl:template>

<!-- 4.4.6.3 union -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:union]]
                       |m:apply[*[1][self::m:csymbol='union']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="2"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&#8746;<!-- union --></m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:union]][m:bvar]
				|m:apply[*[1][self::m:csymbol='union']][m:bvar]"
	      priority="2"
>
  <xsl:call-template name="sum">
    <xsl:with-param name="mo"><m:mo>&#x22C3;</m:mo></xsl:with-param>
  </xsl:call-template>
</xsl:template>

<!-- 4.4.6.4 intersect -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:intersect]]
                       |m:apply[*[1][self::m:csymbol='intersect']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="3"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&#8745;<!-- intersect --></m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>


<xsl:template mode="c2p" match="m:apply[*[1][self::m:intersect]][m:bvar]
				|m:apply[*[1][self::m:csymbol='intersect']][m:bvar]"
	      priority="2"
>
  <xsl:call-template name="sum">
    <xsl:with-param name="mo"><m:mo>&#x22C2;</m:mo></xsl:with-param>
  </xsl:call-template>
</xsl:template>



<!-- 4.4.6.5 in -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:in]]
                       |m:apply[*[1][self::m:csymbol='in']]">
  <xsl:param name="p" select="0"/>
<xsl:call-template name="binary">
  <xsl:with-param name="mo"><m:mo>&#8712;<!-- in --></m:mo></xsl:with-param>
  <xsl:with-param name="p" select="$p"/>
  <xsl:with-param name="this-p" select="3"/>
</xsl:call-template>
</xsl:template>

<!-- 4.4.6.5 notin -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:notin]]|m:reln[*[1][self::m:notin]]
                       |m:apply[*[1][self::m:csymbol='notin']]">
  <xsl:param name="p" select="0"/>
<xsl:call-template name="binary">
  <xsl:with-param name="mo"><m:mo>&#8713;<!-- not in --></m:mo></xsl:with-param>
  <xsl:with-param name="p" select="$p"/>
  <xsl:with-param name="this-p" select="3"/>
</xsl:call-template>
</xsl:template>

<!-- 4.4.6.7 subset -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:subset]]
                       |m:apply[*[1][self::m:csymbol='subset']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="2"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&#8838;<!-- subseteq --></m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 4.4.6.8 prsubset -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:prsubset]]
                       |m:apply[*[1][self::m:csymbol='prsubset']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="2"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&#8834;<!-- prsubset --></m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 4.4.6.9 notsubset -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:notsubset]]
                       |m:apply[*[1][self::m:csymbol='notsubset']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="binary">
 <xsl:with-param name="this-p" select="2"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&#8840;<!-- notsubseteq --></m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 4.4.6.10 notprsubset -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:notprsubset]]
                       |m:apply[*[1][self::m:csymbol='notprsubset']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="binary">
 <xsl:with-param name="this-p" select="2"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&#8836;<!-- prsubset --></m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 4.4.6.11 setdiff -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:setdiff]]
                       |m:apply[*[1][self::m:csymbol='setdiff']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="binary">
 <xsl:with-param name="this-p" select="2"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&#8726;<!-- setminus --></m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 4.4.6.12 card -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:card]]
                       |m:apply[*[1][self::m:csymbol='card']]">
<m:mrow>
<m:mo>|</m:mo>
<xsl:apply-templates mode="c2p" select="*[2]"/>
<m:mo>|</m:mo>
</m:mrow>
</xsl:template>

<!-- 4.4.6.13 cartesianproduct -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:cartesianproduct or self::m:vectorproduct]]
				|m:apply[*[1][self::m:csymbol[.='cartesian_product' or . = 'vectorproduct']]]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="2"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&#215;<!-- times --></m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<xsl:template
match="m:apply[*[1][self::m:cartesianproduct][count(following-sibling::m:reals)=count(following-sibling::*)]]"
priority="2">
<m:msup>
<xsl:apply-templates mode="c2p" select="*[2]">
  <xsl:with-param name="p" select="5"/>
</xsl:apply-templates>
<m:mn><xsl:value-of select="count(*)-1"/></m:mn>
</m:msup>
</xsl:template>


<!-- 4.4.7.1 sum -->
<xsl:template name="sum"  mode="c2p" match="m:apply[*[1][self::m:sum]]">
  <xsl:param name="mo"><m:mo>&#8721;<!--sum--></m:mo></xsl:param>
 <m:mrow>
 <m:munderover>
  <xsl:copy-of select="$mo"/>
 <m:mrow><xsl:apply-templates mode="c2p" select="m:lowlimit|m:interval/*[1]|m:condition/*|m:domainofapplication/*"/></m:mrow><!-- Alexey Shamrin shamrinATmail.ru -->
 <m:mrow><xsl:apply-templates mode="c2p" select="m:uplimit/*|m:interval/*[2]"/></m:mrow>
 </m:munderover>
 <xsl:apply-templates mode="c2p" select="*[last()]"/>
</m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='sum']]">
<m:mrow>
<m:munder><m:mo>&#8721;<!--sum--></m:mo>
<xsl:apply-templates mode="c2p" select="*[2]"/>
</m:munder>
 <xsl:apply-templates mode="c2p" select="*[last()]"/>
</m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:apply/m:lowlimit" priority="3">
<m:mrow>
<xsl:if test="../m:bvar">
  <xsl:apply-templates mode="c2p" select="../m:bvar/node()"/>
  <m:mo>=</m:mo>
</xsl:if>
<xsl:apply-templates mode="c2p"/>
</m:mrow>
</xsl:template>

<!-- 4.4.7.2 product -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:product]]">
  <xsl:call-template name="sum">
    <xsl:with-param name="mo"><m:mo>&#8719;<!--product--></m:mo></xsl:with-param>
  </xsl:call-template>
</xsl:template>


<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='product']]">
<m:mrow>
<m:munder><m:mo>&#8719;<!--product--></m:mo>
<xsl:apply-templates mode="c2p" select="*[2]"/>
</m:munder>
 <xsl:apply-templates mode="c2p" select="*[last()]"/>
</m:mrow>
</xsl:template>

<!-- 4.4.7.3 limit -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:limit]]">
 <m:mrow>
 <m:munder>
  <m:mi>lim</m:mi> <!-- Alexey Shamrin shamrinATmail.ru -->
 <m:mrow><xsl:apply-templates mode="c2p" select="m:lowlimit|m:condition/*"/></m:mrow>
 </m:munder>
 <xsl:apply-templates mode="c2p" select="*[last()]"/>
</m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='limit']][m:bind]">
 <m:mrow>
 <m:munder>
  <m:mi>lim</m:mi>
 <m:mrow>
 <xsl:apply-templates mode="c2p" select="m:bind/m:bvar/*"/>
    <m:mo>
      <xsl:choose>
	<xsl:when test="*[3]='above'">&#8600;<!--searrow--></xsl:when>
	<xsl:when test="*[3]='below'">&#8599;<!--nearrow--></xsl:when>
	<xsl:otherwise>&#8594;<!--rightarrow--></xsl:otherwise>
      </xsl:choose>
    </m:mo>
 <xsl:apply-templates mode="c2p" select="*[2]"/>    
</m:mrow>
 </m:munder>
 <xsl:apply-templates mode="c2p" select="m:bind/*[last()]"/>
</m:mrow>
</xsl:template>



<xsl:template mode="c2p" match="m:apply[m:limit]/m:lowlimit" priority="4">
<m:mrow>
<xsl:apply-templates mode="c2p" select="../m:bvar/node()"/>
<m:mo>&#8594;<!--rightarrow--></m:mo>
<xsl:apply-templates mode="c2p"/>
</m:mrow>
</xsl:template>


<!-- 4.4.7.4 tendsto -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:tendsto]]|m:reln[*[1][self::m:tendsto]]">
 <xsl:param name="p"/>
<xsl:call-template name="binary">
 <xsl:with-param name="this-p" select="2"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>
  <xsl:choose>
   <xsl:when test="@type='above'">&#8600;<!--searrow--></xsl:when>
   <xsl:when test="@type='below'">&#8599;<!--nearrow--></xsl:when>
   <xsl:when test="@type='two-sided'">&#8594;<!--rightarrow--></xsl:when>
   <xsl:otherwise>&#8594;<!--rightarrow--></xsl:otherwise>
  </xsl:choose>
  </m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='tendsto']]">
  <m:mrow>
    <xsl:apply-templates mode="c2p" select="*[3]"/>
    <m:mo>
      <xsl:choose>
	<xsl:when test="*[1][self::above]">&#8600;<!--searrow--></xsl:when>
	<xsl:when test="*[1][self::below]">&#8599;<!--nearrow--></xsl:when>
	<xsl:when test="*[1][self::two-sided]">&#8594;<!--rightarrow--></xsl:when>
	<xsl:otherwise>&#8594;<!--rightarrow--></xsl:otherwise>
      </xsl:choose>
    </m:mo>
    <xsl:apply-templates mode="c2p" select="*[4]"/>
  </m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:semantics/m:ci='tendsto']]">
  <m:mrow>
    <xsl:apply-templates mode="c2p" select="*[2]"/>
    <m:mo>&#8594;<!--rightarrow--></m:mo>
    <xsl:apply-templates mode="c2p" select="*[3]"/>
  </m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:tendsto">
 <m:mi>tendsto</m:mi>
</xsl:template>

<!-- 4.4.8.1 trig -->
<xsl:template mode="c2p" match="m:apply[*[1][
 self::m:sin or self::m:cos or self::m:tan or self::m:sec or
 self::m:csc or self::m:cot or self::m:sinh or self::m:cosh or
 self::m:tanh or self::m:sech or self::m:csch or self::m:coth or
 self::m:arcsin or self::m:arccos or self::m:arctan or self::m:arccosh
 or self::m:arccot or self::m:arccoth or self::m:arccsc or
 self::m:arccsch or self::m:arcsec or self::m:arcsech or
 self::m:arcsinh or self::m:arctanh or self::m:ln]]">
  <m:mrow>
    <m:mi><xsl:value-of select="local-name(*[1])"/></m:mi>
    <m:mo>&#8289;<!--function application--></m:mo>
    <xsl:apply-templates mode="c2p" select="*[2]">
      <xsl:with-param name="p" select="7"/>
    </xsl:apply-templates>
  </m:mrow>
</xsl:template>

<!-- Vasil I. Yaroshevich -->
<xsl:template mode="c2p" match="
 m:sin | m:cos | m:tan | m:sec |
 m:csc | m:cot | m:sinh | m:cosh |
 m:tanh | m:sech | m:csch | m:coth |
 m:arcsin | m:arccos | m:arctan | m:arccosh
 | m:arccot | m:arccoth | m:arccsc |
 m:arccsch | m:arcsec | m:arcsech |
 m:arcsinh | m:arctanh | m:ln|m:mean|
 m:plus|m:minus">
<m:mi><xsl:value-of select="local-name()"/></m:mi>
</xsl:template>




<!-- 4.4.8.2 exp -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:exp]]
                       |m:apply[*[1][self::m:csymbol='exp']]">
<m:msup>
<m:mi>e<!-- exponential e--></m:mi>
<m:mrow><xsl:apply-templates mode="c2p" select="*[2]"/></m:mrow>
</m:msup>
</xsl:template>

<!-- 4.4.8.3 ln -->
<!-- with trig -->

<!-- 4.4.8.4 log -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:log]]
                       |m:apply[*[1][self::m:csymbol='log']]">
<m:mrow>
<xsl:choose>
<xsl:when test="not(m:logbase) or m:logbase=10">
<m:mi>log</m:mi>
</xsl:when>
<xsl:otherwise>
<m:msub>
<m:mi>log</m:mi>
<m:mrow><xsl:apply-templates mode="c2p" select="m:logbase/node()"/></m:mrow>
</m:msub>
</xsl:otherwise>
</xsl:choose>
<m:mo>&#8289;<!--function application--></m:mo>
<xsl:apply-templates mode="c2p" select="*[last()]">
  <xsl:with-param name="p" select="7"/>
</xsl:apply-templates>
</m:mrow>
</xsl:template>


<!-- 4.4.9.1 mean -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:mean]]
                       |m:apply[*[1][self::m:csymbol='mean']]">
<m:mrow>
 <m:mo>&#9001;<!--langle--></m:mo>
    <xsl:for-each select="*[position()&gt;1]">
      <xsl:apply-templates mode="c2p" select="."/>
      <xsl:if test="position() !=last()"><m:mo>,</m:mo></xsl:if>
    </xsl:for-each>
<m:mo>&#9002;<!--rangle--></m:mo>
</m:mrow>
</xsl:template>


<!-- 4.4.9.2 sdef -->
<xsl:template mode="c2p" match="m:sdev|m:csymbol[.='sdev']">
<m:mo>&#963;<!--sigma--></m:mo>
</xsl:template>

<!-- 4.4.9.3 variance -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:variance]]
                       |m:apply[*[1][self::m:csymbol='variance']]">
<m:msup>
<m:mrow>
<m:mo>&#963;<!--sigma--></m:mo>
 <m:mo>&#8289;<!--function application--></m:mo>
<m:mfenced>
<xsl:apply-templates mode="c2p" select="*[position()!=1]"/>
</m:mfenced>
</m:mrow>
<m:mn>2</m:mn>
</m:msup>
</xsl:template>


<!-- 4.4.9.4 median -->
<xsl:template mode="c2p" match="m:median">
<m:mi>median</m:mi>
</xsl:template>


<!-- 4.4.9.5 mode -->
<xsl:template mode="c2p" match="m:mode">
<m:mi>mode</m:mi>
</xsl:template>

<!-- 4.4.9.5 moment -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:moment]]">
  <m:mrow>
    <m:mo>&#9001;<!--langle--></m:mo>
    <m:msup>
      <xsl:variable name="data" 
		    select="*[not(position()=1)]
			    [not(self::m:degree or self::m:momentabout)]"/>
      <xsl:choose>
	<xsl:when test="$data[2]">
	  <m:mfenced>
	    <xsl:apply-templates mode="c2p" select="$data"/>
	  </m:mfenced>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:apply-templates mode="c2p" select="$data"/>
	</xsl:otherwise>
      </xsl:choose>
      <m:mrow><xsl:apply-templates mode="c2p" select="m:degree/node()"/></m:mrow>
    </m:msup>
    <m:mo>&#9002;<!--rangle--></m:mo>
  </m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='moment']]">
<m:msub>
  <m:mrow>
    <m:mo>&#9001;<!--langle--></m:mo>
    <m:msup>
	  <xsl:apply-templates mode="c2p" select="*[4]"/>
	  <xsl:apply-templates mode="c2p" select="*[2]"/>
    </m:msup>
    <m:mo>&#9002;<!--rangle--></m:mo>
  </m:mrow>
  <xsl:apply-templates mode="c2p" select="*[3]"/>	  
</m:msub>
</xsl:template>

<!-- 4.4.9.5 momentabout -->
<xsl:template mode="c2p" match="m:momentabout"/>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:moment]][m:momentabout]" priority="2">
  <m:msub>
    <m:mrow>
      <m:mo>&#9001;<!--langle--></m:mo>
      <m:msup>
	<xsl:variable name="data" 
		      select="*[not(position()=1)]
			      [not(self::m:degree or self::m:momentabout)]"/>
	<xsl:choose>
	  <xsl:when test="$data[2]">
	    <m:mfenced>
	      <xsl:apply-templates mode="c2p" select="$data"/>
	    </m:mfenced>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:apply-templates mode="c2p" select="$data"/>
	  </xsl:otherwise>
	</xsl:choose>
	<m:mrow><xsl:apply-templates mode="c2p" select="m:degree/node()"/></m:mrow>
      </m:msup>
      <m:mo>&#9002;<!--rangle--></m:mo>
    </m:mrow>
    <m:mrow>
      <xsl:apply-templates mode="c2p" select="m:momentabout/*"/>
    </m:mrow>
  </m:msub>
</xsl:template>

<!-- 4.4.10.1 vector  -->
<xsl:template mode="c2p" match="m:vector">
<m:mrow>
<m:mo>(</m:mo>
<m:mtable>
<xsl:for-each select="*">
<m:mtr><m:mtd><xsl:apply-templates mode="c2p" select="."/></m:mtd></m:mtr>
</xsl:for-each>
</m:mtable>
<m:mo>)</m:mo>
</m:mrow>
</xsl:template>


<xsl:template mode="c2p" match="m:vector[m:condition]">
  <m:mrow>
    <m:mo>[</m:mo>
    <xsl:apply-templates mode="c2p" select="*[last()]"/>
    <m:mo>|</m:mo>
    <xsl:apply-templates mode="c2p" select="m:condition"/>
    <m:mo>]</m:mo>
  </m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:vector[m:domainofapplication]">
  <m:mrow>
    <m:mo>[</m:mo>
    <xsl:apply-templates mode="c2p" select="*[last()]"/>
    <m:mo>|</m:mo>
    <xsl:apply-templates mode="c2p" select="m:bvar/*"/>
    <m:mo>&#x2208;</m:mo>
    <xsl:apply-templates mode="c2p" select="m:domainofapplication/*"/>
    <m:mo>]</m:mo>
  </m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='vector']]">
<m:mrow>
<m:mo>(</m:mo>
<m:mtable>
<xsl:for-each select="*[position()!=1]">
<m:mtr>
  <m:mtd><xsl:apply-templates mode="c2p" select="."/></m:mtd>
</m:mtr>
</xsl:for-each>
</m:mtable>
<m:mo>)</m:mo>
</m:mrow>
</xsl:template>

<!-- 4.4.10.2 matrix  -->
<xsl:template mode="c2p" match="m:matrix">
<m:mrow>
<m:mo>(</m:mo>
<m:mtable>
<xsl:apply-templates mode="c2p"/>
</m:mtable>
<m:mo>)</m:mo>
</m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:matrix[m:condition]">
  <m:mrow>
    <m:mo>[</m:mo>
    <m:msub>
      <m:mi>m</m:mi>
      <m:mrow>
	<xsl:for-each select="m:bvar">
	  <xsl:apply-templates mode="c2p"/>
	  <xsl:if test="position()!=last()"><m:mo>,</m:mo></xsl:if>
	</xsl:for-each>
      </m:mrow>
    </m:msub>
    <m:mo>|</m:mo>
    <m:mrow>
      <m:msub>
	<m:mi>m</m:mi>
	<m:mrow>
	  <xsl:for-each select="m:bvar">
	    <xsl:apply-templates mode="c2p"/>
	    <xsl:if test="position()!=last()"><m:mo>,</m:mo></xsl:if>
	  </xsl:for-each>
	</m:mrow>
      </m:msub>
      <m:mo>=</m:mo>
      <xsl:apply-templates mode="c2p" select="*[last()]"/>
    </m:mrow>
    <m:mo>;</m:mo>
    <xsl:apply-templates mode="c2p" select="m:condition"/>
    <m:mo>]</m:mo>
  </m:mrow>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol='matrix']]">
<m:mrow>
<m:mo>(</m:mo>
<m:mtable>
<xsl:apply-templates mode="c2p" select="*[position()!=1]"/>
</m:mtable>
<m:mo>)</m:mo>
</m:mrow>
</xsl:template>


<!-- 4.4.10.3 matrixrow  -->
<xsl:template mode="c2p" match="m:matrix/m:matrixrow">
<m:mtr>
<xsl:for-each select="*">
<m:mtd><xsl:apply-templates mode="c2p" select="."/></m:mtd>
</xsl:for-each>
</m:mtr>
</xsl:template>

<xsl:template mode="c2p" match="m:matrixrow">
<m:mtable>
<m:mtr>
<xsl:for-each select="*">
<m:mtd><xsl:apply-templates mode="c2p" select="."/></m:mtd>
</xsl:for-each>
</m:mtr>
</m:mtable>
</xsl:template>

<xsl:template mode="c2p" match="m:apply[*[1][self::m:csymbol.='matrixrow']]">
<m:mtr>
<xsl:for-each select="*[position()!=1]">
<m:mtd><xsl:apply-templates mode="c2p" select="."/></m:mtd>
</xsl:for-each>
</m:mtr>
</xsl:template>

<!-- 4.4.10.4 determinant  -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:determinant]]
                       |m:apply[*[1][self::m:csymbol='determinant']]">
<m:mrow>
<m:mi>det</m:mi>
 <m:mo>&#8289;<!--function application--></m:mo>
<xsl:apply-templates mode="c2p" select="*[2]">
  <xsl:with-param name="p" select="7"/>
</xsl:apply-templates>
</m:mrow>
</xsl:template>

<xsl:template
match="m:apply[*[1][self::m:determinant]][*[2][self::m:matrix]]" priority="2">
<m:mrow>
<m:mo>|</m:mo>
<m:mtable>
<xsl:apply-templates mode="c2p" select="m:matrix/*"/>
</m:mtable>
<m:mo>|</m:mo>
</m:mrow>
</xsl:template>

<!-- 4.4.10.5 transpose -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:transpose]]
                       |m:apply[*[1][self::m:csymbol='transpose']]">
<m:msup>
<xsl:apply-templates mode="c2p" select="*[2]">
  <xsl:with-param name="p" select="7"/>
</xsl:apply-templates>
<m:mi>T</m:mi>
</m:msup>
</xsl:template>

<!-- 4.4.10.5 selector -->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:selector]]
                       |m:apply[*[1][self::m:csymbol='selector']]">
<m:msub>
<xsl:apply-templates mode="c2p" select="*[2]">
  <xsl:with-param name="p" select="7"/>
</xsl:apply-templates>
<m:mrow>
    <xsl:for-each select="*[position()&gt;2]">
      <xsl:apply-templates mode="c2p" select="."/>
      <xsl:if test="position() !=last()"><m:mo>,</m:mo></xsl:if>
    </xsl:for-each>
</m:mrow>
</m:msub>
</xsl:template>

<!-- *** -->
<!-- 4.4.10.6 vectorproduct see cartesianproduct -->


<!-- 4.4.10.7 scalarproduct-->
<xsl:template mode="c2p" match="m:apply[*[1][self::m:scalarproduct]]
                       |m:apply[*[1][self::m:csymbol='scalarproduct']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="2"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>.</m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 4.4.10.8 outerproduct-->

<xsl:template mode="c2p" match="m:apply[*[1][self::m:outerproduct]]
                       |m:apply[*[1][self::m:csymbol='outerproduct']]">
<xsl:param name="p" select="0"/>
<xsl:call-template name="infix">
 <xsl:with-param name="this-p" select="2"/>
 <xsl:with-param name="p" select="$p"/>
 <xsl:with-param name="mo"><m:mo>&#x2297;</m:mo></xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 4.4.11.2 semantics -->
<xsl:template mode="c2p" match="m:semantics">
 <xsl:apply-templates mode="c2p" select="*[1]"/>
</xsl:template>
<xsl:template mode="c2p" match="m:semantics[m:annotation-xml/@encoding='MathML-Presentation']">
 <xsl:apply-templates mode="c2p" select="m:annotation-xml[@encoding='MathML-Presentation']/node()"/>
</xsl:template>

<!-- 4.4.12.1 integers -->
<xsl:template mode="c2p" match="m:integers">
<m:mi mathvariant="double-struck">Z</m:mi>
</xsl:template>

<!-- 4.4.12.2 reals -->
<xsl:template mode="c2p" match="m:reals">
<m:mi mathvariant="double-struck">R</m:mi>
</xsl:template>

<!-- 4.4.12.3 rationals -->
<xsl:template mode="c2p" match="m:rationals">
<m:mi mathvariant="double-struck">Q</m:mi>
</xsl:template>

<!-- 4.4.12.4 naturalnumbers -->
<xsl:template mode="c2p" match="m:naturalnumbers">
<m:mi mathvariant="double-struck">N</m:mi>
</xsl:template>

<!-- 4.4.12.5 complexes -->
<xsl:template mode="c2p" match="m:complexes">
<m:mi mathvariant="double-struck">C</m:mi>
</xsl:template>

<!-- 4.4.12.6 primes -->
<xsl:template mode="c2p" match="m:primes">
<m:mi mathvariant="double-struck">P</m:mi>
</xsl:template>

<!-- 4.4.12.7 exponentiale -->
<xsl:template mode="c2p" match="m:exponentiale">
  <m:mi>e<!-- exponential e--></m:mi>
</xsl:template>

<!-- 4.4.12.8 imaginaryi -->
<xsl:template mode="c2p" match="m:imaginaryi">
  <m:mi>i<!-- imaginary i--></m:mi>
</xsl:template>

<!-- 4.4.12.9 notanumber -->
<xsl:template mode="c2p" match="m:notanumber">
  <m:mi>NaN</m:mi>
</xsl:template>

<!-- 4.4.12.10 true -->
<xsl:template mode="c2p" match="m:true">
  <m:mi>true</m:mi>
</xsl:template>

<!-- 4.4.12.11 false -->
<xsl:template mode="c2p" match="m:false">
  <m:mi>false</m:mi>
</xsl:template>

<!-- 4.4.12.12 emptyset -->
<xsl:template mode="c2p" match="m:emptyset|m:csymbol[.='emptyset']">
  <m:mi>&#8709;<!-- emptyset --></m:mi>
</xsl:template>


<!-- 4.4.12.13 pi -->
<xsl:template mode="c2p" match="m:pi|m:csymbol[.='pi']">
  <m:mi>&#960;<!-- pi --></m:mi>
</xsl:template>

<!-- 4.4.12.14 eulergamma -->
<xsl:template mode="c2p" match="m:eulergamma|m:csymbol[.='gamma']">
  <m:mi>&#947;<!-- gamma --></m:mi>
</xsl:template>

<!-- 4.4.12.15 infinity -->
<xsl:template mode="c2p" match="m:infinity|m:csymbol[.='infinity']">
  <m:mi>&#8734;<!-- infinity --></m:mi>
</xsl:template>


<!-- ****************************** -->
<xsl:template name="infix" >
  <xsl:param name="mo"/>
  <xsl:param name="p" select="0"/>
  <xsl:param name="this-p" select="0"/>
  <xsl:variable name="dmo">
    <xsl:choose>
     <xsl:when test="m:domainofapplication">
      <m:munder>
       <xsl:copy-of select="$mo"/>
       <m:mrow>
	<xsl:apply-templates mode="c2p" select="m:domainofapplication/*"/>
       </m:mrow>
      </m:munder>
     </xsl:when>
     <xsl:otherwise>
       <xsl:copy-of select="$mo"/>
     </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <m:mrow>
  <xsl:if test="$this-p &lt; $p"><m:mo>(</m:mo></xsl:if>
  <xsl:for-each select="*[not(self::m:domainofapplication)][position()&gt;1]">
   <xsl:if test="position() &gt; 1">
    <xsl:copy-of select="$dmo"/>
   </xsl:if>   
   <xsl:apply-templates mode="c2p" select=".">
     <xsl:with-param name="p" select="$this-p"/>
   </xsl:apply-templates>
  </xsl:for-each>
  <xsl:if test="$this-p &lt; $p"><m:mo>)</m:mo></xsl:if>
  </m:mrow>
</xsl:template>

<xsl:template name="binary" >
  <xsl:param name="mo"/>
  <xsl:param name="p" select="0"/>
  <xsl:param name="this-p" select="0"/>
  <m:mrow>
  <xsl:if test="$this-p &lt; $p"><m:mo>(</m:mo></xsl:if>
   <xsl:apply-templates mode="c2p" select="*[2]">
     <xsl:with-param name="p" select="$this-p"/>
   </xsl:apply-templates>
   <xsl:copy-of select="$mo"/>
   <xsl:apply-templates mode="c2p" select="*[3]">
     <xsl:with-param name="p" select="$this-p"/>
   </xsl:apply-templates>
  <xsl:if test="$this-p &lt; $p"><m:mo>)</m:mo></xsl:if>
  </m:mrow>
</xsl:template>

<xsl:template name="set" >
  <xsl:param name="o" select="'{'"/>
  <xsl:param name="c" select="'}'"/>
  <m:mrow>
   <m:mo><xsl:value-of select="$o"/></m:mo>
   <xsl:choose>
   <xsl:when test="m:condition">
   <m:mrow><xsl:apply-templates mode="c2p" select="m:condition/following-sibling::*"/></m:mrow>
   <m:mo>|</m:mo>
   <m:mrow><xsl:apply-templates mode="c2p" select="m:condition/node()"/></m:mrow>
   </xsl:when>
   <xsl:when test="m:domainofapplication">
    <m:mrow><xsl:apply-templates mode="c2p" select="m:domainofapplication/following-sibling::*"/></m:mrow>
    <m:mo>|</m:mo>
    <m:mrow><xsl:apply-templates mode="c2p" select="m:bvar/node()"/></m:mrow>
    <m:mo>&#8712;<!-- in --></m:mo>
    <m:mrow><xsl:apply-templates mode="c2p" select="m:domainofapplication/node()"/></m:mrow>
   </xsl:when>
   <xsl:otherwise>
    <xsl:for-each select="*[not(position()=1 and parent::m:apply)]">
      <xsl:apply-templates mode="c2p" select="."/>
      <xsl:if test="position() !=last()"><m:mo>,</m:mo></xsl:if>
    </xsl:for-each>
   </xsl:otherwise>
   </xsl:choose>
   <m:mo><xsl:value-of select="$c"/></m:mo>
  </m:mrow>
</xsl:template>


<!-- mathml 3 addtitions -->

<xsl:template mode="c2p" match="m:cs">
  <m:ms>
   <xsl:value-of select="
			 translate(.,
			 '&#9;&#10;&#13;&#32;',
			 '&#160;&#160;&#160;&#160;')"/>
 </m:ms>
</xsl:template>

<xsl:template mode="c2p" match="m:cbytes">
 <m:mrow/>
</xsl:template>

<xsl:template mode="c2p" match="m:cerror">
 <m:merror>
   <xsl:apply-templates mode="c2p"/>
 </m:merror>
</xsl:template>
 
<xsl:template  mode="c2p" match="m:share" priority="4">
 <m:mi href="{@href}">share<xsl:value-of select="substring-after(@href,'#')"/></m:mi>
</xsl:template>

</xsl:stylesheet>

