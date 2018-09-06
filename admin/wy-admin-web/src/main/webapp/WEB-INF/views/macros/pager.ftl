<#-- 生成页码 -->
<#macro genpage startPage endPage>
	<#list startPage..endPage as p>
		<li<#if currentPage == p> class="active"</#if>>
			<a href="javascript:<#if currentPage == p>void(0)<#else>go(${p})</#if>;">${p}</a>
		</li>
	</#list>
</#macro>

<#-- 生成分页 -->
<#macro pager currentPage totalPage totalCount isLastPage>
<#assign btnCount=5>
<div class="clearfix">
	<div class="pull-right" style="text-align: right;">
		第${(currentPage gt totalPage)?string(totalPage, currentPage)}页,共 ${totalPage}页,共${totalCount}条记录
		<div style="text-align: right;">
			<ul class="pager">
				<#if currentPage gt 1>
					<li class="previous">
						<a href="javascript:go(1);">|&lt;</a>
					</li>
					<li>
						<a href="javascript:go(${currentPage-1});">&lt;</a>
					</li>
				<#else>
					<li class="previous disabled">
						<a href="javascript:void(0);">|&lt;</a>
					</li>
					<li class="disabled">
						<a href="javascript:void(0);">&lt;</a>
					</li>
				</#if>
				
				<#if currentPage lte btnCount>
					<#if (btnCount * 2) lt totalPage>
						<@genpage startPage=1 endPage=(btnCount * 2) />
						<li>
							<a href="javascript:go(${btnCount + 1});">...</a>
						</li>
					<#else>
						<@genpage startPage=1 endPage=(totalPage) />
					</#if>
				<#elseif currentPage gt (totalPage - btnCount)>
					<#if (btnCount * 2) lt totalPage>
						<li>
							<a href="javascript:go(${totalPage - btnCount - 1});">...</a>
						</li>
						<@genpage startPage=(totalPage - btnCount * 2) endPage=totalPage />
					<#else>
						<@genpage startPage=1 endPage=totalPage />
					</#if>
				<#else>
					<li>
						<a href="${currentPage - btnCount}">...</a>
					</li>
					<@genpage startPage=(currentPage - btnCount + 1) endPage=(currentPage + btnCount - 1) />
					<li>
						<a href="javascript:go(${currentPage + btnCount});">...</a>
					</li>
				</#if>
				
				<#if currentPage lt totalPage>
					<li>
						<a href="javascript:go(${currentPage+1});">&gt;</a>
					</li>
					<li class="next">
						<a href="javascript:go(${totalPage});">&gt;|</a>
					</li>
				<#else>
					<li class="disabled">
						<a href="javascript:void(0);">&gt;</a>
					</li>
					<li class="next disabled">
						<a href="javascript:void(0);">&gt;|</a>
					</li>
				</#if>
			</ul>
		</div>
	</div>
</div>
<script type="text/javascript">
	function go(page) {
		var $search = $('#search');
		if($search.length > 0) {
			$search.attr('action', page);
			$search.submit();
		} else {
			window.location.href = page;
		}
	}
</script>
</#macro>