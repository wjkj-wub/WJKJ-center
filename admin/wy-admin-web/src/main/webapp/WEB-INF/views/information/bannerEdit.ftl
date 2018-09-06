<html>
	<head>
		<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
		<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.all-3.5.min.js"></script>
		<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.exhide-3.5.min.js"></script>
		<link rel="stylesheet" type="text/css" href="${ctx!}/static/plugin/ztree/css/zTreeStyle.css"/>
	</head>
	<body>
		<article id="editor">
			<form class="form-horizontal" role="form" method="post">
				<legend></legend>
				<input type="hidden" name="moduleId" id="moduleId"/>
				<input type="hidden" name="pid" />
				<input type="hidden" name="sortNum" id="sortNum"/>
				<div class="form-group" id="treemodules">
					<label class="col-md-2 control-label required">模块</label>
					<div class="col-md-4" >
						<ul id="tree-module" class="ztree" style="max-height: 600px;overflow: auto;"></ul>
					</div>
				</div>
			
			
				<div class="form-group">
					<label class="col-md-2 control-label required">类型</label>
					<div class="col-md-4">
						<select class="select-3 form-control chosen" id="type" name="type" wy-required="类型">
							<option value="">请选择</option>
							<option value="1">资讯</option>
							<option value="2">专题</option>
							<option value="3">图集</option>
						</select>
					</div>
				</div>
				<div class="form-group" id="targetDiv">
					<label class="col-md-2 control-label required">目标对象</label>
					<div class="col-md-4">
						<select id="targetId" name="targetId" class="form-control" wy-required="目标对象">
							<option value="">请选择模块或类型</option>
						</select>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">标题</label>
					<div class="col-md-4">
						<input type="text" class="form-control" id="bannerTitle" name="bannerTitle" maxlength="17" placeholder="请输入标题" wy-required="标题" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">图片</label>
					<div class="col-md-4">
						<input type="file" class="form-control" name="imgFile" accept="image/*" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">生效时间</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="bannerTimerDateStr" placeholder="请输入生效时间" wy-required="生效时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',minDate:'${(.now?string('yyyy-MM-dd HH:mm:ss'))!}'})" />
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-offset-2 col-md-10">
						<a type="button" class="btn btn-info" href="${ctx}/overActivity/banner/list/1"><i class="icon-angle-left"></i> 返回列表</a>
						<button id="submit" type="button" class="btn btn-primary">保存</button>
					</div>
				</div>
			</form>
		</article>
		<script type="text/javascript">
			$(function() {
				// 初始化模块选择树
				var $moduleTree = $('#tree-module');
				var zns = JSON.parse('${modulesJson!}');
				var setting = {
					callback: {
						onClick: function(event, treeId, treeNode, clickFlag) {
							moduleTree.checkNode(treeNode);
						},
						onCheck: function(event, treeId, treeNode) {
							initTargetIds();
						}
					},
					check: {
						chkStyle: 'radio',
						enable: true,
						radioType: 'all'
					}
				}
				moduleTree = $.fn.zTree.init($moduleTree, setting, zns);
				<#if moduleIdQuery??>
					<#list moduleIdQuery as i>
						// 初始化勾选状态
						<#if (i.id)??>
							cm = moduleTree.getNodeByParam('id', '${(i.id)!}');
							moduleTree.checkNode(cm, true);
							pn = cm.getParentNode();
							moduleTree.expandNode(pn, true);
						</#if>
					</#list>
				</#if>
				
				// 获取模块选择树中被选中的节点
				function getModuleId() {
					var checkedNodes = moduleTree.getCheckedNodes(true);
					if(typeof(checkedNodes) !== 'undefined' && checkedNodes.length > 0) {
						return checkedNodes[0].id;
					}
					return '';
				}
				
				// 更改类型事件
				$("#type").change(function(){
					initTargetIds();
				}); 
				
				// 初始化目标对象下拉框
				function initTargetIds() {
					var moduleId = getModuleId();
					var type=$("#type").val();
					$.ajax({
						url:'${ctx}/overActivity/banner/goalobjectquery',
						type : 'post',
						dataType: 'json',
						data: { "moduleId": moduleId, "type": type, "infoId": '${(module.id)!}'},
						success : function(data) {	
							$("#targetId").html("");
							if(data && data.length > 0) {
								for(var i=0;i<data.length;i++){
									$("#targetId").append('<option value="'+data[i].id+'">'+data[i].title+'</option>');
								}
								var t = $('#targetId option:selected').html();
								$("#bannerTitle").val(t);
							} else {
								 $("#targetId").append('<option value="">暂无数据</option>');
							}
						},
						async: false,
					});
				}
				
				// 根据目标对象默认标题
				$('#targetId').on('change', function(ev) {
					var t = $('#targetId option:selected').html();
					$("#bannerTitle").val(t);
				});
				
				// 根据目标对象默认标题
				$('#type').on('change', function(ev) {
					var t = $('#targetId option:selected').html();
					$("#bannerTitle").val(t);
				});
				
				
				// 定义编辑器及表单
				var $editor = $('#editor'),
					$form = $editor.find('form');
				
				// 检查是否已选择图片
				function imgFileValid() {
					var $imgFile = $form.find('[name="imgFile"]');
					if($imgFile.val().length <= 0) {
						$imgFile.formErr('请选择图片');
						return false;
					} else {
						$imgFile.formCorr();
						return true;
					}
				}
				
				function moduleValid() {
					var moduleId = getModuleId();
					if(moduleId!=null && moduleId!='') {
						$('#tree-module').formCorr();
						return true;
					} else {
						$('#tree-module').formErr('模块选项不能为空');
						return false;
					}
				}
				
				function titleLengthJudge(){
					var titleLength=$('#bannerTitle').val().length;
					if(titleLength>17){
						$('#bannerTitle').formErr('标题长度不能超过17');
						return false;
					}
					return true;
				}
				
				// 保存事件
				$form.find('#submit').on('click', function() {
					var $this = $(this);
					var formValid = $form.formValid();
					var imgValid = true;
					<#if isInsert>
						imgValid = imgFileValid();
					</#if>
					var mValid = moduleValid();
					var lengthJudge=titleLengthJudge()
					if(formValid && imgValid && mValid && titleLengthJudge) {
						$this.prop('disabled', true);
						$form.ajaxSubmit({
							url:'${ctx}/overActivity/banner/bannerSave',
							type : 'post',
							success : function(d) {
								if (d.code == 0) {
									window.location = '${ctx}/overActivity/banner/list/1';
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
				
				
				// 初始化编辑框
				$form.find('#type').val('${(module.type)!}');
				initTargetIds();
				$.fillForm({
					moduleId: '${(module.id)!}',
					targetId: '${(module.id)!}',
					pid: '${(module.pid)!0}',
					type: '${(module.type)!}',
					imgFile: '${(module.bannerIcon)!}',
					bannerTitle:'${(module.bannerTitle)!}',
					bannerTimerDateStr:'${(module.bannerTimerDate)!.now?string('yyyy-MM-dd HH:mm:ss')}',
					sortNum: '${(module.orderNum)!0}',
				}, $editor);
			});
		</script>
	</body>
</html>