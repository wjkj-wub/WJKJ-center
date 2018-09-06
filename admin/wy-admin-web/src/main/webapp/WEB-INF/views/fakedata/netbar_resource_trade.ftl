<#import "/information/editCommon.ftl" as ec >
<html>
	<head>
		<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
		<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
		<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/editor.js"></script>
		<style type="text/css">
		div[img] {
			padding: 20px 0;
			border: 1px solid #CCC;
			border-radius: 3px;
		}
		
		img.preview {
			max-width: 200px;
			margin: 5px;
		}
		</style>
	</head>
	<body>
		<article id="editor">
			<form class="form-horizontal" role="form" method="post">
				<legend>购买商品</legend>
				<div class="form-group">
					<label class="col-md-2 control-label">网吧ID</label>
					<div class="col-md-4">
						<input class="form-control" name="netbarId" wy-required="网吧ID" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label">购买时间段(起)</label>
					<div class="col-md-4">
						<input class="form-control" id="beginDate" name="beginDateStr" wy-required="购买时间段(起)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',maxDate:'#F{$dp.$D(\'endDate\',{d:0});}'})" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label">购买时间段(止)</label>
					<div class="col-md-4">
						<input class="form-control" id="endDate" name="endDateStr" wy-required="购买时间段(止)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',minDate:'#F{$dp.$D(\'beginDate\',{d:0});}'})" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label">商品</label>
					<div class="col-md-4">
						<select class="form-control" name="propertyId" wy-required="商品">
							<option value="">请先填写网吧ID及购买时间段</option>
						</select>
					</div>
					<div class="col-md-4">
						<button id="btn-queryProperties" type="button" class="btn btn-info">查询商品</button>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label">数量</label>
					<div class="col-md-4">
						<input class="form-control" name="num" wy-required="数量" />
					</div>
					<div class="col-md-3">
						<input class="form-control" id="consum" wy-required="数量" readonly />
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-offset-2 col-md-10">
						<button id="submit" type="button" class="btn btn-primary">购买</button>
					</div>
				</div>
			</form>
		</article>
		<script type="text/javascript">
			// 定义编辑器及表单
			var $editor = $('#editor'),
				$form = $editor.find('form');
			
			$(function() {
				$queryProperties = $form.find('[name="netbarId"],[name="beginDateStr"],[name="endDateStr"]');
				$('#btn-queryProperties').on('click', function() {
					var $this = $(this);
					var valid = true;
					$queryProperties.each(function() {
						var type = $(this).attr('wy-required-type');
						var fieldName = $(this).attr('wy-required');
						var validResult = $(this).formValidateProperty(fieldName, type);
						if(!validResult) {
							valid = false;
						}
					});
					
					if(valid) {
						$this.prop('disabled', true);
						$form.ajaxSubmit({
							url:'${ctx}/fakedata/netbar/resource/queryCommodities',
							type : 'post',
							dataType: 'json',
							success : function(d) {
								if (d.code == 0) {
									var ps = d.object.properties;
									var netbar = d.object.netbar;
									var $commodity = $('[name="propertyId"]').html('');
									var level = netbar.levels;
									for(var i=0; i<ps.length; i++) {
										var p = ps[i];
										var name = p.name;
										var inventory = p.inventory;
										var rebate = p.rebate;
										if(level == 1 && p.vip_ratio != null) {
											rebate *= p.vip_ratio;
										} else if(level == 2 && p.gold_rebate != null) {
											rebate *= p.gold_rebate;
										}
										$commodity.append('<option value="' + p.id + '" rebate="' + rebate + '">' + name + '(价格:' + rebate + ', 数量:' + inventory + ')' + '</option>');
									}
									$commodity.change();
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
				
				$form.find('[name="propertyId"], [name="num"]').on('change', function(ev) {
					showConsum();
				});
				
				function showConsum() {
					var num = parseInt($form.find('[name="num"]').val());
					var rebate = parseFloat($form.find('[name="propertyId"] option:selected').attr('rebate'));
					
					if(!isNaN(rebate) && !isNaN(num)) {
						$('#consum').val('总额:' + (num * rebate));
					} else {
						$('#consum').val('');
					}
				}
				
				// 保存事件
				$form.find('#submit').on('click', function() {
					var $this = $(this);
					var formValid = $form.formValid();
					if(formValid) {
						// 提交表单
						$this.prop('disabled', true);
						$form.ajaxSubmit({
							url:'${ctx}/fakedata/netbar/resource/trade',
							type : 'post',
							dataType: 'json',
							success : function(d) {
								if (d.code == 0) {
									alert('购买成功');
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
			});
		</script>
	</body>
</html>