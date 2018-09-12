$('.mores').mouseenter(function () {
    $(".nav").finish();
    $('.nav').animate({
        right: "90px"
    },400);
    $('.mores').css({'background':'url(images/common/close.png) no-repeat','background-size':'100% 100%'});
    $('.projectsList').fadeOut();   
 },).mouseleave(function(){
    $(".nav").finish();
    $('.nav').animate({
        right:"-990px"  
    },400);
    $('.mores').css({'background':'url(images/common/nav.png) no-repeat','background-size':'100% 100%'});
 });

//点击项目显示二级项目操作
$(".phover").hover(function(){
    $(".nav").finish();
    $('.projectsList').finish();
   $('.projectsList').fadeIn();
   $('.downUp').css({"transform":"rotate(180deg)"})
},function(){
    $('.projectsList').finish();
    $(".nav").finish();
    $('.projectsList').fadeOut();   
    $('.downUp').css({"transform":"rotate(360deg)"})
});
	//百度地图
    $('.companyimg').on('click',function(){
        window.open("map.html?id=1");
    })