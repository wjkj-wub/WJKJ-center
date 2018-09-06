<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li><i class="icon icon-location-arrow mr10"></i> <strong>兑换码</strong>
		</li>
		<li class="active">生成兑换码</li>
	</ul>

	<#-- 生产条件 -->
	<div class="mb10">
		<form id="form-generate" class="form-horizontal" role="form" method="post">
			<div class="form-group">
				<label class="col-md-2 control-label required">用途</label>
				<div class="col-md-4">
					<input type="text" name="production" class="form-control" placeholder="最多12个字,且不可与历史重名" maxlength="12" wy-required="用途" />
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-2 control-label required">过期时间</label>
				<div class="col-md-4">
					<input type="text" name="expireDate" class="form-control" value="${expiredDate!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',minDate: '${.now?string('yyyy-MM-dd HH:mm:ss')}'})" wy-required="过期时间" />
				</div>
			</div>
			<div id="operate" class="form-group">
				<div class="col-md-offset-2 col-md-10">
					<button id="add" type="button" class="btn btn-success">增加</button>
					<button id="submit" type="button" class="btn btn-primary">生成</button>
				</div>
			</div>
		</form>
	</div>
	<script type="text/javascript">
		var $form = $('#form-generate');
		
		// 产生配置框标签
		function genConfig() {
			var html = '<div class="config">'
				 + '	<legend></legend>'
				 + '	<div class="form-group">'
				 + '		<label class="col-md-2 control-label required">类型</label>'
				 + '		<div class="col-md-4">'
				 + '			<select name="type" class="select-3 form-control chosen">'
				 + '				<option value="1">红包</option>'
				 + '				<option value="2"selected>金币</option>'
				 + '			</select>'
				 + '		</div>'
				 + '	</div>'
				 + '	<div class="form-group">'
				 + '		<label class="col-md-2 control-label required">面额</label>'
				 + '		<div class="col-md-4">'
				 + '			<input type="number" name="amount" class="form-control" value="0" wy-required="面额" min="1" />'
				 + '		</div>'
				 + '	</div>'
				 + '	<div class="form-group">'
				 + '		<label class="col-md-2 control-label required">数量</label>' 
				 + '		<div class="col-md-4">'
				 + '			<input type="number" name="number" class="form-control" value="0" wy-required="数量" min="1" />'
				 + '		</div>'
				 + '	</div>'
				 + '	<div class="form-group">'
				 + '		<div class="col-md-5"></div>'
				 + '		<div class="col-md-2">'
				 + '			<button type="button" class="btn btn-primary remove">移除</button>'
				 + '		</div>'
				 + '	</div>'
				 + '</div>';
			return html;
		}
		
		// 增加或移除配置时,初始化配置
		function initConfigs() {
			// 更改类型
			$form.find('[name="type"]').off('change');
			$form.find('[name="type"]').on('change', function() {
				var num = $(this).attr('config');
				var type = $(this).val();
				var html = '';
				if(type === "1") {// 红包
					html = '<select name="amount" class="select-3 form-control chosen">'
						<#if redbags??>
							<#list redbags as r>
						 		+ '	<option value="${r.money}">${r.money}元</option>'
							</#list>
						</#if>
						 + '</select>';
				} else {
					html = '<input type="number" name="amount" class="form-control" value="0" wy-required="额度" />';
				}
				$(this).parents('.config').find('[name="amount"]').parent().html(html);
			});
			
			// 移除配置
			$form.find('button.remove').off('click');
			$form.find('button.remove').on('click', function(ev) {
				$(this).parents('.config').remove();
				initConfigs();
			});
			
			// 无配置时,禁止生成按钮
			if($form.find('.config').length <= 0) {
				$form.find('#submit').prop('disabled', true);
			} else {
				$form.find('#submit').prop('disabled', false);
			}
		}
		
		// 增加配置
		$form.find('#add').on('click', function(ev) {
			$('#operate').before(genConfig());
			initConfigs();
		});
		$form.find('#add').click();
		
		// 生产
		$form.find('#submit').on('click', function(ev) {
			$this = $(this);
			var valid = $form.formValid();
			
			// 检查面额及数量不能小于等于0
			var allGreaterThanZero = true;
			$form.find('[name="amount"], [name="number"]').each(function() {
				var value = parseInt($(this).val());
				if(value == NaN || value < 1) {
					var propertyName = $(this).attr('wy-required');
					$(this).formErr(propertyName + '不能小于1');
					allGreaterThanZero = false;
				} else {
					$(this).formCorr();
				}
			});
			
			if(valid && allGreaterThanZero) {
				var $this = $(this);
				$this.prop('disabled', true);
				
				// 组装配置为json数据
				var configs = [];
				$form.find('.config').each(function() {
					var type = $(this).find('[name="type"]').val();
					var amount = $(this).find('[name="amount"]').val();
					var number = $(this).find('[name="number"]').val();
					configs.push({'type': type, 'amount': amount, 'number': number});
				});
				
				$form.ajaxSubmit({
					url : '${ctx}/boon/cdkey/generate',
					type : 'post',
					data: {
						configs: JSON.stringify(configs)
					},
					success : function(d) {
						var production = $form.find('[name="production"]').val();
						if (d.code == 0) {
							window.location = '${ctx}/boon/cdkey/statis?export=1&production=' + production;
						} else {
							alert(d.result);
						}
					},
					complete : function() {
						$this.prop('disabled', false);
					}
				});
			}
		});
	</script>
</body>
</html>