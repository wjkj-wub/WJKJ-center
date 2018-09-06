<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/simditor.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/font-awesome.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/simditor.scss">
<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/simditor/editor.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/module.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/hotkeys.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/uploader.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/simditor.min.js"></script>
<style type="text/css">
	.modal-dialog {
		width: 800px;
	}
</style>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>管理系统</strong>
 		</li>
		<li class="active">
			转盘活动设置
		</li>
	</ul>
	
	<#-- 操作按钮 -->
	<div class="mb10">
		<button id="new-record" type="button" class="btn btn-success">新增记录</button>
	</div>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2">
				<input type="text" name="name" value="${(params.name)!}" class="form-control" placeholder="活动名称">
			</div>
			<div class="col-md-2">
				<input type="text" name="beginDate" value="${(params.beginDate)!}" class="form-control" placeholder="创建时间(起)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})">
			</div>
			<div class="col-md-2">
				<input type="text" name="endDate" value="${(params.endDate)!}" class="form-control" placeholder="创建时间(止)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:endPick})">
			</div>
			<div class="col-md-2">
				<select class="form-control" name="valid">
					<option value="1"<#if ((params.valid)!"1") == "1"> selected</#if>>有效活动</option>
					<option value="0"<#if ((params.valid)!"1") == "0"> selected</#if>>无效活动</option>
				</select>
			</div>
			<button type="submit" class="btn btn-success">查询</button>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>#</th>
			<th>名称</th>
			<th>转盘图片</th>
			<th>开始时间</th>
			<th>结束时间</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<td>${o.id!}</td>
				<td>${o.name!}</td>
				<td><#if (o.plateImg)?? && o.plateImg?length gt 0><img src="${imgServer!}${o.plateImg!}" style="width:15px;height:15px;" /></#if></td>
				<td>${o.startDate!}</td>
				<td>${o.endDate!}</td>
				<td>
					<button set-seats="${o.id!}" type="button" class="btn btn-info">设置奖项</button>
					<button edit="${o.id!}" type="button" class="btn btn-info">编辑</button>
					<a href="${ctx}/lottery/history/list/${o.id!}/1" class="btn btn-warning">查看中奖名单</a>
					<a href="${ctx}/lottery/report/history/${o.id!}" class="btn btn-warning">查看统计报表</a>
					<#if ((o.isValid)!0) == 1>
						<button remove="${o.id!}" type="button" class="btn btn-danger">删除</button>
					<#else>
						<button restore="${o.id!}" type="button" class="btn btn-danger">还原</button>
					</#if>
				</td>
			</tr>
		</#list>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑 -->
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">编辑</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<div class="form-group">
							<label class="col-md-2 control-label">活动名称</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="name" value="" placeholder="活动名称" wy-required="活动名称" maxlength="50"> 
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">转盘图片</label>
							<div class="col-md-10">
								<input class="form-control" type="file" name="plateImgFile" value="" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">开始时间</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="startDate" value="" wy-required="开始时间" maxlength="22" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">截止时间</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="endDate" value="" wy-required="截止时间" maxlength="22" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})" />
							</div>
						</div>
						<script type="text/javascript">
							$('input[name="startDate"], input[name="endDate"]').on('change', function() {
								var s = $('input[name="startDate"]').val();
								var e = $('input[name="endDate"]').val();
								
								if(s.length > 0 && e.length > 0) {
									var sd = new Date(s);
									var ed = new Date(e);
									if(sd.getTime() >= ed.getTime()) {
										alert('开始时间不能大于结束时间');
										$(this).val('');
									}
								}
							});
						</script>
						<div class="form-group">
							<label class="col-md-2 control-label">活动介绍</label>
							<div class="col-md-10">
								<textarea id="editor-introduce" name="introduce" placeholder="活动介绍"></textarea>
								<script type="text/javascript">
									_editor = editor($('#editor-introduce'));
								</script>
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">关闭</button>
					<button id="submit" type="button" class="btn btn-primary">保存</button>
				</div>
			</div>
		</div>
	</div>
	<#-- 编辑框操作 -->
	<script type="text/javascript">
		var _$editor = $('#modal-editor');
		function showEditor(title) {
			if(title) {
				_$editor.find('.modal-title').html(title);
			}
			_$editor.modal('show');
		}
		function hideEditor() {
			_$editor.modal('hide');
		}
	</script>
	
	<#-- 增删改操作 -->
	<script type="text/javascript">
		// 新增
		$('#new-record').on('click', function() {
			$.fillForm({
				id: ''
			}, _$editor);
			
			_$editor.find('#setting-seats').hide();
			
			showEditor('新增活动');
		});
		
		// 编辑
		$('button[edit]').on('click', function(event) {
			// 初始化表单数据
			var id = $(this).attr('edit');
			$.api('${ctx}/lottery/option/detail/' + id, {}, function(d) {
				var o = d.object;
				$.fillForm({
					name: o.name,
					startDate: o.startDate ? new Date(o.startDate).Format('yyyy-MM-dd hh:mm:ss') : '',
					endDate: o.endDate ? new Date(o.endDate).Format('yyyy-MM-dd hh:mm:ss') : '',
					plateImgFile: o.plateImg,
					id: o.id,
				}, _$editor);
				setEditorText(_editor, o.introduce);
			}, function(d) {
				alert('请求数据异常：' + d.result);
			}, {
				async: false,
			});
			
			_$editor.find('#setting-seats').show();
			
			showEditor('编辑活动');
		});
		
		// 删除
		$('button[remove], button[restore]').on('click', function(event) {
			var restore = 0;
			var id = $(this).attr('remove');
			if(typeof(id) == 'undefined' || id.length <= 0) {
				id = $(this).attr('restore');
				restore = 1;
			}
			$.confirm('确认' + (restore == 1 ? '还原' : '删除') + '吗?', function() {
				$.api('${ctx!}/lottery/option/delete/' + id, {restore: restore}, function(d) {
					window.location.reload();
				});
			});
		});
		
		// 提交表单
		_$editor.find('#submit').on('click', function(event) {
			var $form = _$editor.find('form');
			var valid = $form.formValid();
			if(valid) {
				var $this = $(this);
				$this.prop('disabled', true);
				
				$form.ajaxSubmit({
					url:'${ctx}/lottery/option/save',
				    type:'post',
				    success: function(d){
				    	if(d.code == 0) {
				    		window.location.reload();
				    	} else {
				    		alert(d.result);
				    	}
					},
					complete: function() {
						$this.prop('disabled', false);
					}
				});
			}
		});
	</script>
	
	<#-- 奖项设置 -->
	<div id="modal-set-seats" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideSeatsEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">编辑</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<div class="form-group">
							<label class="col-md-2 control-label">奖项设置</label>
							<div class="col-md-10" style="padding-top: 6px;">
								<a id="a-awards" href="javascript:void(0)" target="_blank">奖项管理</a>
								<a href="javascript:initSeatSetting()">刷新奖项</a>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">盘格1</label>
							<div class="col-md-10">
								<select class="form-control seats" name="awardSeats[0].awardId"></select>
								<input type="hidden" name="awardSeats[0].id" value="" />
								<input type="hidden" name="awardSeats[0].lotteryId" value="" />
								<input type="hidden" name="awardSeats[0].seat" value="0" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">盘格2</label>
							<div class="col-md-10">
								<select class="form-control seats" name="awardSeats[1].awardId"></select>
								<input type="hidden" name="awardSeats[1].id" value="" />
								<input type="hidden" name="awardSeats[1].lotteryId" value="" />
								<input type="hidden" name="awardSeats[1].seat" value="1" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">盘格3</label>
							<div class="col-md-10">
								<select class="form-control seats" name="awardSeats[2].awardId"></select>
								<input type="hidden" name="awardSeats[2].id" value="" />
								<input type="hidden" name="awardSeats[2].lotteryId" value="" />
								<input type="hidden" name="awardSeats[2].seat" value="2" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">盘格4</label>
							<div class="col-md-10">
								<select class="form-control seats" name="awardSeats[3].awardId"></select>
								<input type="hidden" name="awardSeats[3].id" value="" />
								<input type="hidden" name="awardSeats[3].lotteryId" value="" />
								<input type="hidden" name="awardSeats[3].seat" value="3" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">盘格5</label>
							<div class="col-md-10">
								<select class="form-control seats" name="awardSeats[4].awardId"></select>
								<input type="hidden" name="awardSeats[4].id" value="" />
								<input type="hidden" name="awardSeats[4].lotteryId" value="" />
								<input type="hidden" name="awardSeats[4].seat" value="4" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">盘格6</label>
							<div class="col-md-10">
								<select class="form-control seats" name="awardSeats[5].awardId"></select>
								<input type="hidden" name="awardSeats[5].id" value="" />
								<input type="hidden" name="awardSeats[5].lotteryId" value="" />
								<input type="hidden" name="awardSeats[5].seat" value="5" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">盘格7</label>
							<div class="col-md-10">
								<select class="form-control seats" name="awardSeats[6].awardId"></select>
								<input type="hidden" name="awardSeats[6].id" value="" />
								<input type="hidden" name="awardSeats[6].lotteryId" value="" />
								<input type="hidden" name="awardSeats[6].seat" value="6" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">盘格8</label>
							<div class="col-md-10">
								<select class="form-control seats" name="awardSeats[7].awardId"></select>
								<input type="hidden" name="awardSeats[7].id" value="" />
								<input type="hidden" name="awardSeats[7].lotteryId" value="" />
								<input type="hidden" name="awardSeats[7].seat" value="7" />
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideSeatsEditor()">关闭</button>
					<button id="submit" type="button" class="btn btn-primary">保存</button>
				</div>
			</div>
		</div>
	</div>
	<#-- 设置位置操作 -->
	<script type="text/javascript">
		var _$seatsEditor = $('#modal-set-seats');
		function showSeatsEditor(title) {
			if(title) {
				_$seatsEditor.find('.modal-title').html(title);
			}
			_$seatsEditor.modal('show');
		}
		function hideSeatsEditor() {
			_$seatsEditor.modal('hide');
		}
	</script>
	<script type="text/javascript">
		// 设置奖项位置
		$('button[set-seats]').on('click', function(event) {
			_id = $(this).attr('set-seats');
			initSeatSetting();
			_$seatsEditor.find('#a-awards').attr('href', '${ctx}/award/list/1?valid=1&lotteryId=' + _id);
			showSeatsEditor('设置奖项');
		});
		
		// 初始化设置奖项编辑中的奖项下拉框
		var _id = false;
		function initSeatSetting() {
			// 初始化盘格下拉框
			$.api('${ctx}/lottery/awards/seats/' + _id, {}, function(d) {
				var seats = d.object.seats;
				var awards = d.object.awards;
				
				var $seats = _$seatsEditor.find('.seats');
				$seats.html('');
				$seats.each(function(index, item) {
					$this = $(item);
					
					// 产生一个option
					function option(value, html, selected) {
						return '<option value=":value":selected>:html</option>'
							.replace(':value', value)
							.replace(':html', html)
							.replace(':selected', selected?' selected':'');
					}
					
					// 初始化属性
					var $lotteryId = $this.siblings('input[name="awardSeats[' + index + '].lotteryId"]');
					$lotteryId.val('').val(_id);
					var $id = $this.siblings('input[name="awardSeats[' + index + '].id"]');
					$id.val('');
					
					// 填充活动奖项到下拉框
					for(var ai=0; ai<awards.length; ai++) {
						var a = awards[ai];
						// 检查此位置是否已设置
						var s = false;
						for(var si=0; si<seats.length; si++) {
							var _s = seats[si];
							if(_s.seat == index) {
								s = _s;
								$id.val(s.id);
								break;
							}
						}
						
						var selected = s && s.awardId == a.id;
						$this.append(option(a.id, a.awardName + '(' + a.prizeName + ')', selected));
					}
				});
			});
		}
		
		// 保存盘格设置
		_$seatsEditor.find('#submit').on('click', function(event) {
			var $form = _$seatsEditor.find('form');
			var valid = $form.formValid();
			if(valid) {
				var $this = $(this);
				$this.prop('disabled', true);
				
				$form.ajaxSubmit({
					url:'${ctx}/lottery/awards/update',
				    type:'post',
				    success: function(d){
				    	if(d.code == 0) {
				    		hideSeatsEditor();
				    	} else {
				    		alert(d.result);
				    	}
					},
					complete: function() {
						$this.prop('disabled', false);
					}
				});
			}
		});
	</script>
</body>
</html>