/**
 * 配置项
 */
var config = {
	imgServer: 'http://img.wangyuhudong.com/',
}

var regularTelephone = /^1\d{10}$/;
var regularEmail = /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;
var regularIdcard = /(^\d{15}$)|(^\d{17}[\d|x|X]{1}$)/;

/**
 * jQuery 对象函数扩展
 */
$.fn.extend({
	/**
	 * 取消表单的错误提示
	 */
	formCorr: function() {
		$(this).parent().removeClass('has-error');
		$(this).siblings('.help-block').remove();
		return $(this);
	},
	/**
	 * 表单错误提示
	 */
	formErr: function(msg) {
		$(this).formCorr().parent().addClass('has-error');
		$(this).after('<div class="help-block alert alert-warning">' + msg + '</div>');
		return $(this);
	},
	/**
	 * 验证选中元素的值是否正确
	 */
	formValidateProperty: function(fieldName, type) {
		if(StringUtils.isEmpty(type)) {
			type = 'string';
		}
		
		// 验证数据
		var val = $(this).val();
		var result = false;
		var msg = '格式不正确';
		switch(type) {
			case 'telephone':
				result = regularTelephone.test(val);
				break;
			case 'email':
				result = regularTelephone.test(val);
				break;
			case 'idcard':
				result = regularIdcard.test(val);
				break;
			case 'int':
				var int = parseInt(val);
				result = !isNaN(int);
				break;
			case 'float':
				var float = parseFloat(val);
				result = !isNaN(float);
			default:
				result = StringUtils.isNotEmpty(val);
				msg = '不能为空';
				break;
		}
		
		// 提示验证结果
		if(result) {
			$(this).formCorr();
		} else {
			$(this).formErr((fieldName?fieldName:'') + msg);
		}
		
		return result;
	},
	/**
	 * 验证表单
	 * 	属性：
	 * 		wy-required 验证字段，值即为字段名称，
	 * 		drafts 设置wy-required字段生效 false则wy-required不生效 true则wy-required生效
	 * 		wy-required-type 验证类型：string(默认) telephone email，
	 */
	formValid: function() {
		var result = true;
		$(this).find('[wy-required]').each(function() {
			var type = $(this).attr('wy-required-type');
			var fieldName = $(this).attr('wy-required');
			var $temp = $(this).attr("drafts");
			if(typeof($temp)=="undefined"||$temp=="false"){
				var validResult = $(this).formValidateProperty(fieldName, type);
				if(result && !validResult) {
					result = false;
				}
			}else if($temp=="true"){
				return result;
			}
		});
		return result;
	},
});

/**
 * JQuery全局函数扩展
 */
jQuery.extend({
	/**
	 * 请求接口
	 */
	api: function(url, params, success, fail, opts) {
    	var opt = {
    		url: url,
    		data: params,
    		type: 'post',
    		dataType: 'json',
    		success: function(d) {
    			if(d.code == 0) {
    				if(success) {
    					success(d);
    				}
    			} else {
    				if(fail) {
    					fail(d);
    				} else {
    					alert(d.result);
    				}
    			}
    		},
    		error: function(e) {
    			console.log(e);
    		}
    	}
    	
    	if(opts) {
    		for(var k in opts) {
    			opt[k] = opts[k];
    		}
    	}
    	
    	return $.ajax(opt);
    },
    initConfirm: function(text, okBtn, cancelBtn) {
    	var $confirm = $('#confirm-global');
    	if($confirm.length <= 0) {
			$('body').append('<div id="confirm-global" class="modal fade">'
				+ '<div class="modal-dialog">'
				+ '  <div class="modal-content">'
				+ '    <div class="modal-header">'
				+ '      <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">×</span><span class="sr-only">关闭</span></button>'
				+ '      <h4 class="modal-title">提示</h4>'
				+ '    </div>'
				+ '    <div class="modal-body">'
				+ '      <p id="msg">' + (text?text:'') + '</p>'
				+ '    </div>'
				+ '    <div class="modal-footer">'
				+ '      <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">' + (cancelBtn?cancelBtn:'取消') + '</button>'
				+ '      <button id="ok" type="button" class="btn btn-primary">' + (okBtn?okBtn:'确定') + '</button>'
				+ '    </div>' + '  </div>'
				+ '</div>' + '</div>');
    	} else {
    		if(text) {
    			$confirm.find('#msg').html(text);
    		}
    		if(okBtn) {
    			$confirm.find('#ok').html(okBtn);
    		}
    		if(cancelBtn) {
    			$confirm.find('#cancelBtn').html(cancelBtn);
    		}
    	}
    },
    confirm : function(text, ok, cancel, opts) {
    	function enableBtns(enabled) {
    		$confirm.find('#cancel, #ok').prop('disabled', !enabled);
    	}
    	
		this.initConfirm(text);
		var $confirm = $('#confirm-global');
		$confirm.find('#ok').off('click');
		$confirm.find('#ok').on('click', function() {
			$confirm.modal('hide');
			enableBtns(false);
			if (ok) {
				ok();
			}
			if (opts && opts.complete) {
				opts.complete();
			}
		});
		$confirm.find('#cancel').off('click');
		$confirm.find('#cancel').on('click', function() {
			$confirm.modal('hide');
			enableBtns(false);
			if (cancel) {
				cancel();
			}
			if (opts && opts.complete) {
				opts.complete();
			}
		});
		if (opts && opts.complete) {
			$confirm.on('hidden.zui.modal', function() {
				opts.complete();
			});
		}
		enableBtns(true);
		$confirm.modal('show', 'center');
	},
	/**
	 * 根据键值对，自动填充表单
	 * 	支持：
	 * 		input: text checkbox file(图片) radio
	 * 		textarea
	 * 		select
	 * 	参数说明：
	 * 		column - name\value 键值对
	 * 		editor - 编辑窗口（modal）
	 */
	fillForm: function(columns, editor) {
		if(!editor) {
			return false;
		}
		
		var $form = editor.find('form'); 
		$form.get(0).reset();// 重置表单
		// 重置reset方法不自动处理的元素
		$form.find('img[preview]').remove();
		$form.find('input[type="hidden"][name]').val('');
		$form.find('textarea[name]').html('');
		for(var k in columns) {
			var $dom = editor.find('[name="' + k + '"]');
			if($dom.length > 0) {
				// 匹配出字段在表单中的dom类型
				var domType = '';
				var tagName = $dom.get(0).tagName.toUpperCase();
				switch(tagName) {
					case 'INPUT':
						domType = $dom.attr('type');
						break;
					case 'SELECT':
						domType = 'select';
						break;
					case 'TEXTAREA':
						domType = 'textarea';
						break;
					defautl:
						break;
				}
				
				// 根据dom类型做相应赋值
				var $field = editor.find('input[name="' + k + '"]');
				var value = columns[k];
				switch(domType) {
					case 'number':
						$field.val(value);
						break;
					case 'text':
						$field.val(value);
						break;
					case 'hidden':
						$field.val(value);
						break;
					case 'textarea':
						editor.find('textarea[name="' + k + '"]').html(value);
						break;
					case 'checkbox':
						if(value == 1) {
							$field.prop('checked', true);
						}
						break;
					case 'select':
						editor.find('select[name="' + k + '"]').val(value);
						break;
					case 'file':
						$field.siblings('img[preview]').remove();
						if(StringUtils.isNotEmpty(value)) {
							var icon= config.imgServer + value;
							if(value.indexOf('http://pili-static.wangyuhudong.com/snapshots/')>=0){ //直播icon图片地址
								icon = value;
							}
							$field.before('<img preview src="' + icon + '" style="width:100px;height:100px;" />');
						}
						break;
					case 'radio':
						editor.find('input[name="' + k + '"][value="' + value + '"]').prop('checked', true);
						break;
				}
			}
		}
	},
	loading: function(show) {
		if($('#modal-loading').length <= 0) {
			$('body').append('<div id="modal-loading" class="modal modal-trigger fade modal-loading in" aria-hidden="false" style="display: block;">'
			+ '	<div class="icon-spinner icon-spin loader"/>'
			+ '</div>');
		}
		
		if(typeof(show) === 'undefined') {
			show = true;
		}
		
		$('#modal-loading').modal(show ? 'show' : 'hide');
		
		// 防止加载中动画不停止
		if(!show) {
			setTimeout(function() {
				// $('.modal-backdrop').hide();
				$('#modal-loading').css('display', 'none').removeClass('in');
			}, 500);
		}
	},
	/**
	 * 创建异步模态框
	 * 参数说明：
	 * 	模态框内容
	 */
	loadPage: function(opt) {
		$.loading();
		
		function initModal(body) {
			if($('#modal-load-page').length <= 0) {
				$('body').append('<div id="modal-load-page" class="modal fade">'
				+ '		<div class="modal-dialog">'
				+ '		<div class="modal-content">'
				+ '			<div class="modal-header">'
				+ '				<button type="button" class="close" data-dismiss="modal">'
				+ '					<span>×</span><span class="sr-only">关闭</span>'
				+ '				</button>'
				+ '				<h4 class="modal-title">编辑赛事</h4>'
				+ '			</div>'
				+ '			<div class="modal-body">' + body
				+ '			</div>'
				+ '			<div class="modal-footer">'
				+ '				<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>'
				+ '			</div>'
				+ '		</div>'
				+ '	</div>'
				+ '</div>');
			} else {
				$('#modal-load-page').find('modal-body').html(body);
			}
			return $pageModal = $('#modal-load-page');
		}
		
		var url = opt.url;
		var width = opt.width;
		if(!width) {
			width = 1000;
		}
		
		if(url) {
			$.ajax({
				'url': url,
				'async': false,
				'success': function(d) {
					$pageModal = initModal(d);
					$pageModal.find('.modal-dialog').css('width', width);
					$pageModal.modal('show');
				},
				complete: function(XHR, TS) {
					$.loading(false);
				}
			});
		} else {
			$.loading(false);
		}
	}
});
var $pageModal = false;// 异步模态框所需变量

/**
 * String 扩展
 */
String.prototype.startWith = function(str) {
	if (str == null || str == "" || this.length == 0 || str.length > this.length) {
		return false;
	}
	
	if (this.substr(0, str.length) == str) {
		return true;
	} else {
		return false;
	}
	
	return true;
}

/**
 * Array 扩展
 */
Array.prototype.contains = function(ele) {
	for (var i = 0; i < this.length; i++) {
		if (this[i] == ele) {
			return true;
		}
	}
	return false;
}
// 移除一个元素
Array.prototype.removeEle = function(ele) {
	for(var i in this) {
		if(this[i] == ele) {
			this.splice(i, 1);
		}
	}
}
// 将数组拼接成字符串
Array.prototype.toSplitString = function(split) {
	if(StringUtils.isEmpty) {
		split = ',';
	}
	
	var result = '';
	for(var i=0; i<this.length; i++) {
		if(result.length > 0) result += split;
		result += this[i];
	}
	return result;
}

/**
 * Date 扩展
 */
Date.prototype.Format = function (fmt) { // author: meizz
	var o = {
			"M+": this.getMonth() + 1, // 月份
			"d+": this.getDate(), // 日
			"h+": this.getHours(), // 小时
			"m+": this.getMinutes(), // 分
			"s+": this.getSeconds(), // 秒
			"q+": Math.floor((this.getMonth() + 3) / 3), // 季度
			"S": this.getMilliseconds() // 毫秒
	};
	if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
	for (var k in o)
		if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
	return fmt;
}

/**
 * String 工具类
 */
var StringUtils = {
	isEmpty: function(text) {
		return typeof(text) == 'undefined' || text == null || text.length <= 0;
	},
	isNotEmpty: function(text) {
		return !this.isEmpty(text);
	},
}

/**
 * 检查日期空间的开始时间不能大于结束时间
 */
var lastPicker = false;
var beginDate = false;
var beginPick = function() {
	beginDate = $dp.cal.newdate;
	lastPicker = $(this);
	checkDatePicker(beginDate);
}
var endDate = false;
var endPick = function() {
	endDate = $dp.cal.newdate;
	lastPicker = $(this);
	checkDatePicker(endDate);
}
function checkDatePicker(date) {
	if(beginDate && endPick) {
		beginDate.H = 0;beginDate.m = 0;beginDate.s = 0;
		endDate.H = 0;endDate.m = 0;endDate.s = 0;
		if(beginDate.compareWith(endDate) > 0) {
			alert('开始时间不能大于结束时间');
			lastPicker.val('');
			date = false;
		}
	}
}