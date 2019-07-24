<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY nbsp "&#160;">
<!ENTITY head_ass_ovw "$t.translate('head.ass.ovw')">
<!ENTITY head_ass_details "$t.translate('head.ass.details')">
<!ENTITY head_ass_summary "$t.translate('head.ass.summary')">
<!ENTITY head_score "$t.translate('head.score')">
<!ENTITY ass_user "$t.translate('ass.user')">
<!ENTITY ass_inst "$t.translate('ass.inst')">
<!ENTITY ass_identifier "$t.translate('ass.identifier')">
<!ENTITY ass_title "$t.translate('ass.title')">
<!ENTITY num_av "$t.translate('num.av')">
<!ENTITY num_pres "$t.translate('num.pres')">
<!ENTITY num_tried "$t.translate('num.tried')">
<!ENTITY score_val "$t.translate('score.val')">
<!ENTITY score_min "$t.translate('score.min')">
<!ENTITY score_max "$t.translate('score.max')">
<!ENTITY score_cut "$t.translate('score.cut')">
<!ENTITY ans_min "$t.translate('ans.min')">
<!ENTITY ans_max "$t.translate('ans.max')">
<!ENTITY ans_correct "$t.translate('ans.correct')">
<!ENTITY ans_your "$t.translate('ans.your')">
<!ENTITY ans_plus "$t.translate('ans.plus')">
<!ENTITY ans_minus "$t.translate('ans.minus')">
<!ENTITY date "$t.translate('date')">
<!ENTITY time "$t.translate('time')">
<!ENTITY dur "$t.translate('dur')">
<!ENTITY days "$t.translate('days')">
<!ENTITY hours "$t.translate('hours')">
<!ENTITY mins "$t.translate('mins')">
<!ENTITY secs "$t.translate('secs')">
<!ENTITY sec "$t.translate('sec')">
]>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="yes" encoding="iso-8859-1" standalone="yes"/>

	<xsl:template match="/">
		<style><![CDATA[
<!--
.o_disabled_input {
padding: 0px;
border-spacing: 0px;
border: 1px solid silver;
margin: 0px;
border-collapse: collapse;
}
-->
		]]></style>
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="result/context">
		<xsl:if test="name = node()">
			&ass_user;: <xsl:value-of select="name"/><br />
		</xsl:if>
		<xsl:for-each select="generic_identifier">
			&ass_inst;: <xsl:value-of select="type_label"/><br />			
			&ass_identifier;: <xsl:value-of select="identifier_string"/><br />
		</xsl:for-each>
		<xsl:for-each select="date">
			<xsl:value-of select="type_label"/> 			
			<xsl:apply-templates select="."/>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="extension_result">
		<!-- block items in extension result from being processed -->
	</xsl:template>
	
	<xsl:template match="summary_result">
		<h3>&head_ass_summary;</h3>
		<xsl:apply-templates/>
	</xsl:template>
	
	<!-- Assessment summary, incl. date, duration and assessment outcomes -->
	<xsl:template match="assessment_result">
		<h3>&head_ass_ovw;</h3>
		<xsl:if test="@asi_title">&ass_title; <xsl:value-of select="@asi_title"/><br /></xsl:if>
		<xsl:apply-templates select="date"/>
		<xsl:apply-templates select="duration"/><p />
		<table border="0" cellpadding="0" cellspacing="0">
			<tbody>
				<tr><td>&num_av;</td><td width="10"/><td><xsl:value-of select="num_items"/></td></tr>
				<tr><td>&num_pres;</td><td width="10"/><td><xsl:value-of select="num_items_presented"/></td></tr>
				<tr><td>&num_tried;</td><td width="10"/><td><xsl:value-of select="num_items_attempted"/></td></tr>
			</tbody>
		</table>
		<p />
		<xsl:apply-templates select="outcomes"/>
		<hr />
		<xsl:if test="section_result|item_result">
			<h3>&head_ass_details;</h3>
			<xsl:apply-templates select="section_result|item_result"/>
		</xsl:if>
	</xsl:template>
	
	<!-- Section summary -->
	<xsl:template match="section_result">
		<xsl:if test="@asi_title"><h3>&sec;&nbsp;<xsl:value-of select="@asi_title"/></h3></xsl:if>
		<table border="0" cellpadding="0" cellspacing="0">
			<tbody>
				<tr><td>&num_av;</td><td width="10"/><td><xsl:value-of select="num_items"/></td></tr>
				<tr><td>&num_pres;</td><td width="10"/><td><xsl:value-of select="num_items_presented"/></td></tr>
				<tr><td>&num_tried;</td><td width="10"/><td><xsl:value-of select="num_items_attempted"/></td></tr>
			</tbody>
		</table>
		<p />
		<xsl:apply-templates/>
	</xsl:template>
	
		<!-- parse dates -->
	<xsl:template match="date">
		&date;: <xsl:value-of select="substring(datetime, 1, 10)"/> &time;: <xsl:value-of select="substring(datetime, 12)"/><br />
		<xsl:apply-templates/>
	</xsl:template>

	<!-- parse duration -->
	<xsl:template match="duration">
	    <xsl:param name="date_and_time"><xsl:value-of select="substring-after(., 'P')"/></xsl:param>
	    <xsl:param name="date"><xsl:value-of select="substring-before($date_and_time, 'T')"/></xsl:param>
	    <xsl:param name="time"><xsl:value-of select="substring-after(., 'T')"/></xsl:param>
	    &dur;:
	    <xsl:value-of select="substring-before(substring-after($date, 'M'), 'D')"/> &days;&nbsp;		
		<xsl:value-of select="substring-before($time, 'H')"/> &hours;&nbsp;
		<xsl:value-of select="substring-before(substring-after($time, 'H'), 'M')"/> &mins;&nbsp;
		<xsl:value-of select="substring-before(substring-after($time, 'M'), 'S')"/> &secs;&nbsp;
		<p />
		<xsl:apply-templates/>
	</xsl:template>
	
	<!-- parse outcomes -->
	<xsl:template match="outcomes">
		<b>&head_score;</b>
		<xsl:apply-templates select="score[@varname='SCORE']"/>
	</xsl:template>
	
	<!-- render scores -->
	<xsl:template match="score">
		<table border="0"><tbody><tr>
			<td width="160">
		<table border="0" cellpadding="0" cellspacing="0" height="12" width="150">
			<tbody>
				<tr>
					<td>
						<div class="progress" style="width:150px;">
							<div class="progress-bar">
								<xsl:choose>
									<xsl:when test="(number(score_max) &gt; 0) and (number(score_value) &lt;= number(score_max))">
										<xsl:attribute name="style">width:<xsl:value-of select="number(score_value) div number(score_max) * 150"/>px</xsl:attribute>
										<xsl:attribute name="title"><xsl:value-of select="number(score_value) div number(score_max) * 100"/>%</xsl:attribute>
									</xsl:when>
									<xsl:otherwise>
										<xsl:attribute name="style">width:150px</xsl:attribute>
										<xsl:attribute name="title">100%</xsl:attribute>
									</xsl:otherwise>
								</xsl:choose>
							</div>
						</div>
					</td>
				</tr>
			</tbody>
		</table>
		</td>
		<td>
			<xsl:choose>
					<xsl:when test="(number(score_max) &gt; 0) and (number(score_value) &lt;= number(score_max))">
						<xsl:value-of select="round(number(score_value) div number(score_max) * 100)"/>
					</xsl:when>
					<xsl:otherwise>0</xsl:otherwise>
			</xsl:choose>
			&#37;
		</td>
		</tr></tbody></table>
		<table border="0" cellpadding="0" cellspacing="0">
			<tbody>
				<tr><td>&score_val;</td><td width="10"/><td><xsl:value-of select="score_value"/></td></tr>
				<xsl:if test="score_min"><tr><td>&score_min;</td><td width="10"/><td><xsl:value-of select="score_min"/></td></tr></xsl:if>
				<xsl:if test="score_max"><tr><td>&score_max;</td><td width="10"/><td><xsl:value-of select="score_max"/></td></tr></xsl:if>
				<xsl:if test="score_cut"><tr><td>&score_cut;</td><td width="10"/><td><xsl:value-of select="score_cut"/></td></tr></xsl:if>
			</tbody>
		</table>
	</xsl:template>
	
	<!-- detailed item results -->
	<xsl:template match="item_result">
		<xsl:variable name="id"><xsl:value-of select="@ident_ref"/></xsl:variable>		
		<!-- jump into original item and do a graphical representation -->
		<xsl:apply-templates select="//item[@ident=$id]"/>
		<!-- display item outcomes -->
		<xsl:apply-templates select="outcomes"/>
		<hr />
	</xsl:template>
	
	<xsl:template match="item">
		<xsl:param name="id"><xsl:value-of select="@ident"/></xsl:param>
		<xsl:param name="score"><xsl:value-of select="//item_result[@ident_ref=$id]/outcomes/score/score_value"/></xsl:param>
		<h4>
    		<i>
			<xsl:if test="$score &gt; 0">
    			<xsl:attribute name="class">o_icon o_passed o_icon_passed</xsl:attribute>
			</xsl:if>
			<xsl:if test="$score &lt;= 0">
    			<xsl:attribute name="class">o_icon o_failed o_icon_failed</xsl:attribute>
			</xsl:if>
    		</i>
			&nbsp;<xsl:value-of select="@title"/>
		</h4>
		<xsl:apply-templates select="presentation"/>
	</xsl:template>
	
	<xsl:template match="presentation/flow">
		<xsl:apply-templates/>
		<xsl:text disable-output-escaping="yes">&nbsp;</xsl:text>
	</xsl:template>
	
	<xsl:template match="presentation">
		<xsl:apply-templates/>
		<p />
	</xsl:template>
	
	<!-- render fib and choice items -->
	<xsl:template match="render_fib | render_choice | render_num | render_str">

		<!-- extract response for further processing (i.e. response_label) -->
		<xsl:param name="this" select="descendant-or-self::*"/>
		<xsl:param name="item_id"><xsl:value-of select="ancestor::item/@ident"/></xsl:param>
		<xsl:param name="lid_id"><xsl:value-of select="../@ident"/></xsl:param>
		<xsl:param name="response" select="//item_result[@ident_ref=$item_id]/response[@ident_ref=$lid_id]"/>
		<xsl:param name="ext_res" select="//item_result[@ident_ref=$item_id]/extension_item_result"/>

		<!-- extract minimum and maximum numbers per item -->
		<xsl:if test="@minnumber | @maxnumber">
			<p>
			<xsl:if test="@minnumber">
				<i>&ans_min;&nbsp;<xsl:value-of select="@minnumber"/></i><br />
			</xsl:if>
			<xsl:if test="@maxnumber">
				<i>&ans_max;&nbsp;<xsl:value-of select="@maxnumber"/></i>
				<br />
			</xsl:if>
			</p>
		</xsl:if>
		<xsl:choose>
			<xsl:when test="parent::flow/@class='Block'">&nbsp;</xsl:when>
			<xsl:otherwise><p /></xsl:otherwise>
		</xsl:choose>
		
		<!-- display correct response (graphically if possible) -->
		<b>&ans_correct;</b><br />
		<xsl:if test="ancestor::item[starts-with(@ident, 'QTIEDIT:KPRIM:')]">
				<b>&ans_plus;&nbsp;&nbsp;&ans_minus;</b><br/>
			</xsl:if>
		<xsl:apply-templates><xsl:with-param name="response" select="$response"/></xsl:apply-templates>
		<p><xsl:if test="$ext_res">
			<!--This item has a complex result, we need to display it first -->
			<!-- Because it was not possible to graphically display the response. -->			
			<xsl:apply-templates select="$ext_res"><xsl:with-param name="item_id" select="$lid_id"/></xsl:apply-templates>			
		</xsl:if></p>

		<!-- display given response -->
		<p><b>&ans_your;</b><br />
		<xsl:if test="../render_choice">
			<xsl:if test="ancestor::item[starts-with(@ident, 'QTIEDIT:KPRIM:')]">
				<b>&ans_plus;&nbsp;&nbsp;&ans_minus;</b><br/>
			</xsl:if>
			<xsl:for-each select=".//response_label">
				<xsl:choose>
					<xsl:when test="ancestor::item[starts-with(@ident, 'QTIEDIT:KPRIM:')]">
						<xsl:choose>
							<xsl:when test="$response/response_value = concat(@ident, ':correct')">
								<i class="o_icon o_icon_radio_on"> </i>
								<i class="o_icon o_icon_radio_off"> </i>
							</xsl:when>
							<xsl:when test="$response/response_value = concat(@ident, ':wrong')">
								<i class="o_icon o_icon_radio_off"> </i>
								<i class="o_icon o_icon_radio_on"> </i>
							</xsl:when>
							<!-- no answer -->
							<xsl:otherwise>
								<i class="o_icon o_icon_radio_off"> </i>
								<i class="o_icon o_icon_radio_off"> </i>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<xsl:choose>
							<!-- Case Multiple Choice -->
							<xsl:when test="ancestor::response_lid/@rcardinality = 'Multiple'">
								<xsl:choose>
									<xsl:when test="$response/response_value = @ident">
										<i class="o_icon o_icon_check_on"> </i>
									</xsl:when>
									<xsl:otherwise>
										<i class="o_icon o_icon_check_off"> </i>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
							<!-- Case Single Choice -->
								<xsl:choose>
									<xsl:when test="$response/response_value = @ident">
										<i class="o_icon o_icon_radio_on"> </i>
									</xsl:when>
									<xsl:otherwise>
										<i class="o_icon o_icon_radio_off"> </i>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:text>&nbsp;</xsl:text> 
				<xsl:apply-templates select="child::*" />
				<br/>
			</xsl:for-each>
		</xsl:if>
		<xsl:if test="../render_fib">
			<xsl:for-each select="$response/response_value">
				<pre style="white-space: pre-wrap; white-space:-moz-pre-wrap; white-space:-pre-wrap; white-space:-o-pre-wrap; word-wrap:break-word;"><xsl:value-of select="."/></pre><br />
			</xsl:for-each>
		</xsl:if></p>
	</xsl:template>
	
	<!-- Display correct answers -->
	<!-- render a single label according to its parent enclosing type -->
	<xsl:template match="response_label">
		<xsl:param name="response"/> <!-- the response for this label -->
		<xsl:choose>
			<!-- case Kprim -->
			<xsl:when test="ancestor::item[starts-with(@ident, 'QTIEDIT:KPRIM:')]">
				<xsl:choose>
					<xsl:when test="$response/response_form/correct_response = concat(@ident, ':correct')">
						<i class="o_icon o_icon_radio_on"> </i>
						<i class="o_icon o_icon_radio_off"> </i>
					</xsl:when>
					<xsl:otherwise>
						<i class="o_icon o_icon_radio_off"> </i>
						<i class="o_icon o_icon_radio_on"> </i>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
			<!-- multiple response label -->
				<xsl:choose>
					<xsl:when test="ancestor::response_lid/@rcardinality = 'Multiple'">
						<xsl:choose>
							<xsl:when test="$response/response_form/correct_response = @ident"> 
								<!-- case correct MC answer -->
								<i class="o_icon o_icon_check_on"> </i>
							</xsl:when>
							<xsl:otherwise>
								<!-- case incorrect MC answer -->
								<i class="o_icon o_icon_check_off"> </i>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
				<!-- single response label -->
					<xsl:when test="ancestor::response_lid/@rcardinality = 'Single'">
						<xsl:choose>
							<xsl:when test="$response/response_form/correct_response = @ident">
								<!-- case correct SC answer -->
								<i class="o_icon o_icon_radio_on"> </i>
							</xsl:when>
							<xsl:otherwise>
								<!-- case incorrect SC answer -->
								<i class="o_icon o_icon_radio_off"> </i>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
				<!-- fill-in-blank -->
					<xsl:when test="ancestor::render_fib">
						<xsl:for-each select="$response/response_form/correct_response">
						<table cellpadding="2" class="o_disabled_input">
						<xsl:if test="string-length(.) &lt; 20"><xsl:attribute name="width">100px</xsl:attribute></xsl:if>
							<tbody>
						<tr><td>
							&nbsp;<font color="gray"><xsl:value-of select="."/></font>
						</td></tr>
							</tbody>
							</table>
						</xsl:for-each>
					</xsl:when>
			<!-- everything else -->
					<xsl:otherwise>
						<xsl:apply-templates/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>&nbsp;</xsl:text>
		<xsl:apply-templates/>
		<xsl:choose>
			<xsl:when test="parent::flow_label/@class = 'Block'"></xsl:when>
			<xsl:otherwise><br /></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- pass on response param -->
	<xsl:template match="flow_label">
		<xsl:param name="response"/>
		<xsl:apply-templates><xsl:with-param name="response" select="$response"/></xsl:apply-templates>
	</xsl:template>
	
	<!-- render any material -->
	<xsl:template match="material">
		<xsl:apply-templates/>
		<xsl:choose>
			<xsl:when test="parent::flow_mat/@class = 'Block'">
				<xsl:text disable-output-escaping="yes">&nbsp;</xsl:text>
			</xsl:when>
			<xsl:when test="parent::flow/@class = 'Block'">
				<xsl:text disable-output-escaping="yes">&nbsp;</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="following-sibling::material"><br /></xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="mattext">
		<xsl:value-of select="." disable-output-escaping="yes"/>
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="matemtext">
		<b><xsl:value-of select="." disable-output-escaping="yes"/></b>
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="matbreak">
		<br /><xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="matimage">		
		<xsl:choose>
			<xsl:when test="//staticspath/@ident">
			<img>
				<xsl:attribute name="src"><xsl:value-of select="//staticspath/@ident"/>/<xsl:value-of select="@uri"/></xsl:attribute>
			</img>
			</xsl:when>
			<xsl:otherwise>[ IMAGE: <xsl:value-of select="@uri"/>]</xsl:otherwise>			
		</xsl:choose>		
	</xsl:template>
	
	<xsl:template match="matvideo">
		[ VIDEO: <xsl:value-of select="@uri"/>]
	</xsl:template>
	
	<xsl:template match="itemfeedback">
		<!-- skip itemfeedback -->
	</xsl:template>

	<xsl:template match="extension_item_result_bak">
		<xsl:param name="item_id"/>
		<xsl:apply-templates select="conditionvar/and/or"><xsl:with-param name="item_id" select="$item_id"/></xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="extension_item_result">
		<xsl:param name="item_id"/>
		<xsl:choose>
			<xsl:when test="conditionvar/and/or">
				<xsl:apply-templates select="conditionvar/and/or"><xsl:with-param name="item_id" select="$item_id"/></xsl:apply-templates>
			</xsl:when>
			<xsl:when test="conditionvar/and">
				<xsl:apply-templates select="conditionvar/and"><xsl:with-param name="item_id" select="$item_id"/></xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="conditionvar">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="vargte">&gt;=&nbsp;<xsl:value-of select="."/>&nbsp;<xsl:apply-templates/></xsl:template>
	<xsl:template match="varlte">&lt;=&nbsp;<xsl:value-of select="."/>&nbsp;<xsl:apply-templates/></xsl:template>
	<xsl:template match="varlt">&lt;&nbsp;<xsl:value-of select="."/>&nbsp;<xsl:apply-templates/></xsl:template>
	<xsl:template match="vargt">&gt;&nbsp;<xsl:value-of select="."/>&nbsp;<xsl:apply-templates/></xsl:template>
	<xsl:template match="vareq">&nbsp;<xsl:apply-templates/></xsl:template>
	<!-- 
	<xsl:template match="varequal">&nbsp;<xsl:value-of select="." disable-output-escaping="yes"/><xsl:apply-templates/></xsl:template>
	-->
	<xsl:template match="not">NICHT&nbsp;<xsl:apply-templates/></xsl:template>
	
	<xsl:template match="and"><xsl:apply-templates/></xsl:template>
	
	<xsl:template match="or">
	<xsl:param name="item_id"/>
	<xsl:apply-templates select="varequal"><xsl:with-param name="item_id" select="$item_id"/></xsl:apply-templates>	
	</xsl:template>
	
	<xsl:template match="varequal">
	   <xsl:param name="item_id"/>
	   <xsl:if test="@respident=$item_id">
		<table cellpadding="2" class="o_disabled_input">
			<xsl:if test="string-length(.) &lt; 20"><xsl:attribute name="width">100px</xsl:attribute></xsl:if>
			<tbody>
			<tr><td>
			&nbsp;<font color="gray"><xsl:value-of select="."/></font>
			</td></tr>
			</tbody>
		</table>	
		</xsl:if>
	</xsl:template>

	<xsl:template match="text()">
	</xsl:template>
	
</xsl:stylesheet>
