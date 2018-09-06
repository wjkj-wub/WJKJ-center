<html>
	<head>
		<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/editor.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
		<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
		<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.all-3.5.min.js"></script>
		<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.exhide-3.5.min.js"></script>
		<link rel="stylesheet" type="text/css" href="${ctx!}/static/plugin/ztree/css/zTreeStyle.css"/>
	</head>
	<body>
		<article id="editor">
			<form class="form-horizontal" role="form" method="post">
				<input type="hidden" id="gameIds" name="gameIds">
				<input type="hidden" id="id" name="id">
				<input type="hidden" id="isInsert" name="isInsert">
				<div class="form-group">
					<label class="col-md-2 control-label required">主办方官名</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="name" maxlength="13" placeholder="请输入主办方官名" wy-required="主办方官名" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">logo</label>
					<div class="col-md-4">
						<input type="file" class="form-control" name="logo" accept="image/*" />
					</div>
				</div>
				<div class="form-group">
					<div class="form-group" id="div-areas">
						<label class="col-md-2 control-label">游戏</label>
						<div class="col-md-10">
							<ul id="treeDemo" class="ztree"></ul>
						</div>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-offset-2 col-md-10">
						<a type="button" class="btn btn-info" href="${ctx}/organiser/list/1"><i class="icon-angle-left"></i> 返回列表</a>
						<button id="submit" type="button" class="btn btn-primary">保存</button>
					</div>
				</div>
			</form>
		</article>
		<script type="text/javascript">
			// 定义编辑器及表单
			var $editor = $('#editor'),
			$form = $editor.find('form');
			function imgValidate() {
				var $iconFile = $form.find('[name="logo"]');
				if($iconFile.val().length <= 0) {
					if($("#isInsert").val()=="0"){
						$iconFile.formErr('请选择图标');
						return false;
					}
					return true;
				}	
				return true;
			}
			// 填充表单内容
			$.fillForm({
				name:'${(info.name)!""}',
				logo:'${(info.logo)!""}',
				isInsert:'${isInsert}',
				id:'${(info.id)!}'
			}, $editor);
			// 保存事件
			$form.find('#submit').on('click', function() {
				var checkedNodes=zTreeObj.getCheckedNodes();
				if(checkedNodes.length>0){
					var ids="";
					for(var i=0;i<checkedNodes.length;i++){
						ids+=checkedNodes[i].id+",";
					}
					$("#gameIds").val(ids);
				}else{
					alert("请选择游戏")
				}
				
				var $this = $(this);
				var formValid = $form.formValid();
				var imgValid = imgValidate();
				if(formValid && imgValid) {
					// 提交表单
					$this.prop('disabled', true);
					$form.ajaxSubmit({
						url:'${ctx}/organiser/save',
						type : 'post',
						dataType: 'json',
						success : function(d) {
							if (d.code == 0) {
								window.location = '${ctx}/organiser/list/1';
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
		   var zTreeObj;
		   var setting = {
				callback: {
					onClick: function(event, treeId, treeNode, clickFlag) {
					},
				},
				check: {
					enable: true,
				}
			}
		   var zNodes = [];
		   var data="";
		   try {
				data = JSON.parse('${itemList!""}');
			} catch(expect) {
			}
			if(data.length>0){
				for(var i=0;i<data.length;i++){
					var p={};
					var game=data[i];
					p.id=game.id;
					p.name=game.name;
					p.checked=game.nocheck;
					zNodes.push(p);
				}
			}
		   $(document).ready(function(){
		      zTreeObj = $.fn.zTree.init($("#treeDemo"), setting, zNodes);
		   });
		</script>
	</body>
</html>