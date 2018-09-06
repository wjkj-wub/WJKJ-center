<#import "/information/editCommon.ftl" as ec >
<html>
	<head>
		<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
		<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
	</head>
	<body>
		<article id="editor">
			<form class="form-horizontal" role="form" method="post">
				<legend>充值资金</legend>
				<div class="form-group">
					<div class="form-group">
						<label class="col-md-2 control-label">网吧ID</label>
						<div class="col-md-4">
							<input class="form-control" name="netbarId" wy-required="网吧ID" />
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-2 control-label">金额</label>
						<div class="col-md-4">
							<input class="form-control" name="amount" wy-required="金额" />
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-2 control-label">充值时间段(起)</label>
						<div class="col-md-4">
							<input class="form-control" id="beginDate" name="beginDateStr" wy-required="充值时间段(起)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',maxDate:'#F{$dp.$D(\'endDate\',{d:0});}'})" />
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-2 control-label">充值时间段(止)</label>
						<div class="col-md-4">
							<input class="form-control" id="endDate" name="endDateStr" wy-required="充值时间段(止)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',minDate:'#F{$dp.$D(\'beginDate\',{d:0});}'})" />
						</div>
					</div>
					<div class="col-md-offset-2 col-md-10">
						<button id="submit" type="button" class="btn btn-primary">充值</button>
					</div>
				</div>
			</form>
		</article>
		<script type="text/javascript">
			// 定义编辑器及表单
			var $editor = $('#editor'),
				$form = $editor.find('form');
			
			$(function() {
				// 保存事件
				$form.find('#submit').on('click', function() {
					var $this = $(this);
					var formValid = $form.formValid();
					if(formValid) {
						// 提交表单
						$this.prop('disabled', true);
						$form.ajaxSubmit({
							url:'${ctx}/fakedata/netbar/resource/recharge',
							type : 'post',
							dataType: 'json',
							success : function(d) {
								if (d.code == 0) {
									var fund = d.object;
									alert('充值完成,当前资金:' + fund.accounts + ';配额:' + fund.usableQuota);
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