//顶部导航动画			
$('.nav_more').hover(function(){
	// showNav();
	// $(this).attr("data-open", true);
	$(".nav").finish();
	$('.nav').animate({
		right: "90px"
	},300);
	$('.mores').css({'background':'url(../images/common/close.png) no-repeat','background-size':'100% 100%'});
 },function(){
	$(".nav").finish();
	$('.nav').animate({
		right:"-990px"  
	},300);
	$('.mores').css({'background':'url(../images/common/nav.png) no-repeat','background-size':'100% 100%'});
 });
//点击导航按钮
// $(document).on("click", ".nav_more", function (event) {
//     var isOpen = $(this).data("open");
//     if(isOpen) {
//     	//阻止点击顶部导航时的冒泡事件
// 		event.stopPropagation();
// 		hideNav();
// 	    $(this).data("open", false);
//     } else {
//     	//阻止点击顶部导航时的冒泡事件
//     	event.stopPropagation();
//     	showNav();
// 	    $(this).data("open", true);
//     }
// });
// //点击隐藏顶部导航栏
// $(document).click(function () {
// 	hideNav()
//     $(this).data("open", false);
// })
// //显示顶部导航栏函数
// function showNav() {
// 	$(".nav").finish();
// 	$('.nav').animate({
//         right: "90px"
//     },500);
//     $('.mores').css({'background':'url(../images/common/close.png) no-repeat','background-size':'100% 100%'});
// }
// //隐藏顶部导航栏函数
// function hideNav() {
// 	$(".nav").finish();
// 	$('.nav').animate({
//         right:"-990px"  
//     },500);
//     $('.mores').css({'background':'url(../images/common/nav.png) no-repeat','background-size':'100% 100%'});
// }
//导航隐藏		
$(window).scroll(function() {
    if($(document).scrollTop()>0){
		$('.nav_more').fadeOut();
		$('.goTop').fadeIn();
	}else{
		$('.nav_more').fadeIn();
		$('.goTop').fadeOut();
	}
});
//点击图标跳转
$(".logo").click(function () {
    window.location.href = "../index.html";
});
//点击项目显示二级项目操作
$(".phover").hover(function(){
   $('.projectsList').fadeIn();
   $('.downUp').css({"transform":"rotate(180deg)"})
},function(){
	$('.projectsList').finish();
    $('.projectsList').fadeOut();   
    $('.downUp').css({"transform":"rotate(360deg)"})
});
//百度地图
$('.companyimg').on('click',function(){
	window.open("../map.html?id=1");
})

//回到顶部
$('.con_fixed .con_right .con_right_iphone ').hover(function(){
	$('.con_fixed .con_left .con_weichat').finish();
	$('.con_fixed .con_left .con_iphone').fadeIn();
},function(){
	$('.con_fixed .con_left .con_iphone').fadeOut();
})

$('.con_fixed .con_right .con_right_weichat').hover(function(){
	$('.con_fixed .con_left .con_iphone').finish();
	$('.con_fixed .con_left .con_weichat').fadeIn();
},function(){
	$('.con_fixed .con_left .con_weichat').fadeOut();
})
$('.goTop').on('click',function(){
	// document.documentElement.scrollTop = document.body.scrollTop =0;
	$('html,body').animate({scrollTop: '0px'}, 500);
})
