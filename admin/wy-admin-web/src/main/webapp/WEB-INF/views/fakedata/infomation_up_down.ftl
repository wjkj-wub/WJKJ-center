<html>
	<body>
		<article id="editor">
			<form class="form-horizontal" role="form" method="post" action="${ctx}/fakedata/information_up_down">
				<div class="form-group">
					<label class="col-md-2 control-label required">资讯ids(空格隔开)</label>
					<div class="col-md-4">
						<textarea rows="10" cols="20" name="ids" placeholder="资讯ids(逗号隔开)"></textarea>
					</div>
				</div>
				<button id="submit" type="submit" class="btn btn-primary">保存</button>
			</form>
		</article>
	</body>
</html>