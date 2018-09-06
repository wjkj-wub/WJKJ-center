<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<nav class="col-sm-3 col-md-2 menu rel" data-toggle="menu">
	<ul class="nav nav-primary">
	<c:forEach var="o" items="${sessionScope.menu}">
		<c:choose>
			<c:when test="${!empty o.children}">
				<li class="nav-parent menu"><a href="#">${o.name}<c:if test="${o.name=='用户反馈'}"><span class="tips_dot tdpos1"></span></c:if></a>
					<ul class="nav">
						<c:forEach var="c" items="${o.children}">
							<c:choose>
								<c:when test="${!empty c.children}">
									<li class="nav-parent"><a href="#">${c.name}</a>
										<ul class="nav">
											<c:forEach var="gc" items="${c.children}">
												<li><a href="${ctx}${gc.url}">${gc.name}</a></li>
											</c:forEach>
										</ul>
									</li>
								</c:when>
								<c:otherwise>
									<li>
										<c:choose>
											<c:when test="${c.name=='app用户反馈'}">
												<a href="${ctx}${c.url}" class="orderNav">${c.name}</a><span class="tips_dot tdpos1"></span>
											</c:when>
											<c:otherwise>
												<a href="${ctx}${c.url}">${c.name}</a>
											</c:otherwise>
										</c:choose>
									</li>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</ul>
				</li>
			</c:when>
			<c:otherwise>
				<li><a href="${ctx}${o.url}">${o.name}</a></li>
			</c:otherwise>
		</c:choose>
	</c:forEach>
	</ul>
</nav>
<script type="text/javascript">
	var global = 0;
	function afterPageLoad() {
		var location = window.location.pathname + window.location.search;
		if(location.indexOf("tab=")>0){
			var x=location.substr(location.length-1,location.length);
			if(x<5){
				x=1
			}else{
				x=9
			}
			location=location.substr(0,location.length-1)+x;
		}
		var ls = location.split('/');
		var page = parseInt(ls[ls.length-1]);
		if (page > 0) {
			location = location.substring(0, location.length - page.toString().length);
		}
		$('.menu .nav li:not(".nav-parent") a').each( function() {
			var $this = $(this);
			var link = $(this).attr("href");
			if (link.indexOf(location) == 0) {
				var arrow = '<i class="iconfont m-arrow"></i>';
				$this.closest('li').addClass('current').append(arrow);
				var parent = $this.closest('.nav-parent');
				if (parent.length) {
					parent.addClass('show');
				}
				return false;
			}
		});
		/* var location = window.location.pathname;
		var ls = location.split('/');
		var page = parseInt(ls[ls.length-1]);
		if (page > 0) {
			var regex = /\d*$/;
			location = location.replace(regex, 1);
		}
		location += window.location.search;
		$('.menu .nav li:not(".nav-parent") a').each( function() {
			var $this = $(this);
			var link = $(this).attr("href");
			if (link == location) {
				var arrow = '<i class="iconfont m-arrow"></i>';
				$this.closest('li').addClass('current').append(arrow);
				var parent = $this.closest('.nav-parent');
				if (parent.length) {
					parent.addClass('show');
					
					var gp = parent.parents('.nav-parent');
					if (gp.length) {
						gp.addClass('show');
					}
				}
			}
		}); */
		
		$('.orderNav').on('click', function() {
			$.ajax({
				url : '${ctx}/delNum?userId=${userId}',
				type : 'get',
				dataType : 'json',
				success : function(data) {
					if (data.object != 0) {
						$('.tips_dot').html('');
					}
				},
				error : function(e) {
					console.log(e);
				}
			})
		});
	}
	afterPageLoad();
	
	function auto() {
// 		setTimeout(auto, 1000 * 60);
		$.ajax({
			url : '${ctx}/unhandleNum?userId=${userId}',
			type : 'get',
			dataType : 'json',
			success : function(data) {
				if (data.object != 0) {
					$(".tips_dot").html(data.object);
				}
			},
			error : function(e) {
				console.log(e);
			}
		})
	}
	auto();
</script>
