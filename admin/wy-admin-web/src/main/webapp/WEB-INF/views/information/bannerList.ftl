<#import "/macros/pager.ftl" as p >
<html>
<head>
	<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<script type="text/javascript" src="${ctx}/static/js/app.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>资讯管理</strong>
 		</li>
		<li class="active">
			资讯banner
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2">
				<select class="form-control" name="moduleId">
					<option value="">全部模块</option>
					<#if modules??>
						<#list modules as m>
							<option value="${(m.id)!}"<#if (m.id)?? && moduleId??&&moduleId== m.id> selected</#if>>${(m.name)!}</option>
						</#list>
					</#if>
				</select>
			</div>
			<div class="col-md-2">
				<select class="form-control" name="banner">
					<option value="">全部状态</option>
					<option value="0"<#if banner?exists&&banner==0> selected</#if>>已生效</option>
					<option value="1"<#if banner?exists&&banner==1> selected</#if>>待生效</option>
					<option value="2"<#if banner?exists&&banner==2> selected</#if>>失效</option>
				</select>
			</div>
			<div class="col-md-2">
				<input type="text" class="form-control" name="dateStart" id="dateStart" placeholder="生效时间（起）" value="${dateStart!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})" />
			</div>
			<div class="col-md-2">
				<input type="text" class="form-control" name="dateEnd" id="dateEnd" placeholder="生效时间（止）" value="${dateEnd!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})" />
			</div>
			<button type="submit" class="btn btn-success">查询</button>
			<a class="btn btn-info" href="1">清空</a>
		</form>
	</div>
	
	<#-- 新增 -->
	<div class="mb10">
		<a type="button" class="btn btn-success" href="${ctx}/overActivity/banner/edit">新增</a>
	</div>
	
	
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>排序</th>
			<th>所属模块</th>
			<th>二级模块</th>
			<th>图片</th>
			<th>标题</th>
			<th>类型</th>
			<th>发布时间</th>
			<th>生效时间</th>
			<th>状态</th>
			<th>操作</th>
		</tr>
		<#if result??>
			<#list result as res>
				<tr>
					<td>
						<select name="sortNum" order-num="${(res.banner_sort)!}" bannerid="${(res.bannerID)!}"></select>
					</td>
					<td>
						<#if (res.fname)??>
							<#if res.fname?length gt 5>
								<a href="javascript:void(0)" title="${(res.fname)!}" data-toggle="tooltip" data-placement="top" data-original-title="${(res.fname)!}">${(res.fname?substring(0, 5))!} ...</a>
							<#else>
								${(res.fname)!}
							</#if>
						</#if>
					</td>
					<td>
						<#if (res.sname)??>
							<#if res.sname?length gt 5>
								<a href="javascript:void(0)" title="${(res.sname)!}" data-toggle="tooltip" data-placement="top" data-original-title="${(res.sname)!}">${(res.sname?substring(0, 5))!} ...</a>
							<#else>
								${(res.sname)!}
							</#if>
						</#if>
					</td>
					<td><#if (res.banner_icon)??><img src="${imgServer!}/${res.banner_icon!}" style="width: 50px; height: 50px;" /></#if></td>
					<td>
						${res.banner_title!}
					</td>
					<td>
						<#if (res.type)??>
							<#if res.type=1>
							资讯
							<#elseif res.type=2>
							专题
							<#elseif res.type=3>
							图集
							</#if>
						</#if>
					</td>
					<td>${(res.banner_create_date?string("yyyy-MM-dd HH:mm"))!}</td>
					<td>${(res.banner_timer_date?string("yyyy-MM-dd HH:mm"))!}</td>
					<td>
						<#if (res.banner_valid)?? && res.banner_valid = 1>
							<#if (res.banner_timer_date)?? && res.banner_timer_date lte .now>		
								已生效
							<#else>
								待生效
							</#if>
						<#else>
							无效
						</#if>
					</td>
					<td>
						<a type="button" class="btn btn-info" href="${ctx}/overActivity/banner/edit?id=${(res.bannerID)!}">编辑</a>
						<button delete="${(res.bannerID)!}" type="button" class="btn btn-danger">删除</button>
						<#assign efficient=0>
						<#if (res.banner_valid)?? && res.banner_valid == 1 && (res.banner_timer_date)?? && res.banner_timer_date lte .now>
							<#assign efficient=1>
						</#if>
						<button statusChange="${(res.bannerID)!}" valid="${(efficient == 1)?string('0', '1')}" type="button" class="btn btn-info">${(efficient == 1)?string('失效', '生效')}</button>
					</td>
				</tr>
			</#list>
		</#if>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<script type="text/javascript">
		(function init() {
			$('[data-toggle="tooltip"]').tooltip({});
			
			// 初始化排序列
			var maxSortNum = parseInt('${maxOrderNum!1}');// 排序最大值
			var selectOptions = '';
			for(var i=1; i<=maxSortNum; i++) {
				selectOptions += '<option value="' + i + '">' + i + '</option>';
			}
			$('[name="sortNum"]').each(function() {
				var orderNum = $(this).attr('order-num');
				$(this).html(selectOptions).val(orderNum);
			});
			// 更改排序
			$('[name="sortNum"]').on('change', function(ev) {
				var bannerID = $(this).attr('bannerID');
				var orderNum = $(this).val();
				
				$.api('${ctx}/overActivity/banner/order', {
					'bannerID': bannerID,
					'orderNum': orderNum
				}, function(d) {
					window.location.reload();
				});
			});
		}());
		
		// 初始化模态框
		var _$editor = $('#modal-editor');
		_$editor.modal({
			keyboard : false,
			show : false
		})
		
		// 关闭模态框
		function hideEditor() {
			_$editor.modal('hide');
		}
		
		// 显示新增
		$('button[add]').on('click', function(event) {
		$.fillForm({
				}, _$editor);
			_$editor.modal('show');
		});
		// 提交注册
		function submitAdd() {
			_$add.find('form').ajaxSubmit({
				url:'${ctx}/user/register',
				type:'post',
				success: function(d){
					if(d.code == 0) {
						window.location.reload();
					} else {
						alert(d.result);
					}
				}
			});
		} 
		
		// 提交表单
		function submitEditor() {
			var $form = _$editor.find('form');
			
			// 检查上传文件是否为图片格式
			var types = "image/png,image/jpeg,image/gif";
			var iconFiles = $form.find('[name="file"]').get(0).files;
			if(iconFiles.length > 0) {
				for(var i=0; i<iconFiles.length; i++) {
					var file = iconFiles[i];
					if(types.indexOf(file.type) == -1) {
						alert('图片类型错误');
						return;
					}
				}
			}
			
			$form.ajaxSubmit({
				url:'${ctx}/overActivity/banner/bannerSave',
				type:'post',
				success: function(d){
					if(d.code == 0) {
						window.location.reload();
					} else {
						alert(d.result);
					}
				}
			});
		}
		
		
		
		$('button[delete]').on('click', function(event) {
			var _this = $(this);
			_this.attr('disabled', true);
			
			var id = $(this).attr('delete');
			
			$.confirm('确认删除吗?', function() {
				$.api('${ctx}/overActivity/banner/bannerDelete/' + id, {}, function(d) {
					window.location.reload();
				}, function(d) {
					alert('删除失败：' + d.result);
				}, {
					complete: function() {
						_this.attr('disabled', false);
					}
				});
			}, undefined, {
				complete: function() {
					_this.attr('disabled', false);
				}
			});
		});
		
		//更改状态
		$('button[statusChange]').on('click', function(event) {
			var id = $(this).attr('statusChange');
			var valid= $(this).attr('valid');
			$.ajax({
				type : 'post',
				url : '${ctx}/overActivity/banner/statusChange?id='+id+'&valid='+valid,
				dataType : 'json',
				success : function(d) {
					if (d.code == 0) {
						window.location.reload();
					} else {
						alert(d.result);
					}
				},
			});
		});
	
		// 启用
		$('button[enabled]').on('click', function(event) {
			var _this = $(this);
			_this.prop('disabled', true);
			
			var id = _this.attr('enabled');
			var banner = _this.attr('banner');
			var s;
			if(banner==0){
			s="启用";
			}else if(banner==1){
			s="禁用";
			}
			$.confirm('确认'+s+'吗?', function() {
				$.api('${ctx}/overActivity/banner/enabled/' + id, {}, function(d) {
					window.location.reload();
				}, function(d) {
					alert(d.result);
				}, {
					complete: function() {
						_this.prop('disabled', false);
					}
				});
			}, undefined, {
				complete: function() {
					_this.prop('disabled', false);
				}
			})
		});
	
		function getTarget(type) {
			if (type == undefined) {
				type = $("#type").val();
			}
			$.ajax({
				type : 'post',
				url : '${ctx}/overActivity/banner/infoList?type=' + type,
				dataType : 'json',
				async : false,
				success : function(data) {
					$("#targetId").html("");
					for (var i = 0; i < data.length; i++) {
						$("#targetId").append(
								'<option value="'+data[i].id+'">'
										+ data[i].title + '</option>');
					}
				}
			})
		}
		getTarget(1);

		// 查询事件
		$('#search').on('submit', function(ev) {
			var dateBegin = $(this).find('[name="dateStart"]').val();
			var dateEnd = $(this).find('[name="dateEnd"]').val();
			if (dateBegin.length > 0) {
				if (dateEnd.length <= 0) {
					alert('请输入生效时间(止)');
					return false;
				}
			}
			if (dateEnd.length > 0) {
				if (dateBegin.length <= 0) {
					alert('请输入生效时间(起)');
					return false;
				}
			}
			if (dateBegin > dateEnd) {
				alert('生效时间（起）不能大于大于生效时间止');
				return false;
			}
			return true;
		});
	</script>
</body>
</html>