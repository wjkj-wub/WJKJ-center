package com.miqtech.master.admin.web.controller.api.pc.commodity;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.pc.commodity.PcCommodity;
import com.miqtech.master.service.pc.commodity.PcCommodityService;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author shilina
 */
@Controller
@RequestMapping("api/pc/commodity")
public class PcCommodityController {
    @Autowired
    private PcCommodityService pcCommodityService;

    /**
     *兑换商品保存
     */
    @RequestMapping("save")
    @ResponseBody
	public JsonResponseMsg save(PcCommodity pcCommodity, Integer exchangeType) {
        JsonResponseMsg result=new JsonResponseMsg();

        String name=pcCommodity.getName();
        if(StringUtils.isAnyBlank(name,pcCommodity.getImg(),pcCommodity.getDescription())){
            return result.fill(CommonConstant.CODE_ERROR_PARAM,"必填参数不能为空");
        }
        if(StringUtils.getWordCount(name)>20){
            return result.fill(CommonConstant.CODE_ERROR_PARAM,"奖品名称输入限制为20个字符");
        }
        if(pcCommodity.getType()==null||exchangeType==null||pcCommodity.getIsSale()==null){
            return result.fill(CommonConstant.CODE_ERROR_PARAM,"必填参数不能为空");
        }
        //exchangeType为1，娱币；2时，娱币+现金
        if(exchangeType==1){
            if(pcCommodity.getChip()==null||pcCommodity.getCash()!=null){
                return result.fill(CommonConstant.CODE_ERROR_PARAM,"娱币兑换时，填写娱币兑换金额，且只填写娱币兑换金额");
            }
        }else if(exchangeType==2){
            if(pcCommodity.getChip()==null||pcCommodity.getCash()==null){
                return result.fill(CommonConstant.CODE_ERROR_PARAM,"娱币+现金兑换时，填写娱币以及现金兑换金额");
            }
        }else{
            return result.fill(CommonConstant.CODE_ERROR_PARAM,"兑换条件填写有误");
        }
        pcCommodity.setIsTop(0);
        pcCommodity.setIsValid(true);
        pcCommodityService.save(pcCommodity);
        if(pcCommodity.getIsSale()!=null&&pcCommodity.getIsSale()==1){
            pcCommodityService.setLeftCount(pcCommodity.getId());
        }
        return result.fill(CommonConstant.CODE_SUCCESS,CommonConstant.MSG_SUCCESS);
    }

    /**
     *兑换商品编辑
     */
    @RequestMapping("edit")
    @ResponseBody
    public JsonResponseMsg edit(Long commodityId){
        JsonResponseMsg result=new JsonResponseMsg();
        PcCommodity commodity=pcCommodityService.findById(commodityId);
		Map<String, Object> map = new HashMap<>(16);
        map.put("name",commodity.getName());
        map.put("type",commodity.getType());
        map.put("img",commodity.getImg());
        map.put("description",commodity.getDescription());
		map.put("id", commodity.getId());
        Double cash=commodity.getCash();
        if(cash!=null&&cash>0){
            map.put("exchangeType",2);
            map.put("cash",cash);
        }else{
            map.put("exchangeType",1);
        }
        map.put("chip",commodity.getChip());
        map.put("num",commodity.getNum());
        return result.fill(CommonConstant.CODE_SUCCESS,CommonConstant.MSG_SUCCESS,map);
    }

    /**
     *兑换商品列表
     */
    @RequestMapping("list")
    @ResponseBody
    public JsonResponseMsg list(Integer page, Integer pageSize, String name, Integer isSale) {
        JsonResponseMsg result = new JsonResponseMsg();
        PageVO vo=pcCommodityService.list(page,pageSize,name,isSale);
        return result.fill(CommonConstant.CODE_SUCCESS,CommonConstant.MSG_SUCCESS,vo);
    }

    /**
     *兑换商品删除
     */
    @RequestMapping("delete")
    @ResponseBody
    public JsonResponseMsg delete(Long commodityId) {
        JsonResponseMsg result = new JsonResponseMsg();
        PcCommodity commodity=pcCommodityService.findById(commodityId);
        if(commodity.getIsSale()==1){
            return result.fill(CommonConstant.CODE_ERROR_PARAM,"商品已上架，无法删除");
        }
        commodity.setIsValid(false);
        pcCommodityService.save(commodity);
        return result.fill(CommonConstant.CODE_SUCCESS,CommonConstant.MSG_SUCCESS);
    }

    /**
     * 兑换商品置顶
     */
    @RequestMapping("top")
    @ResponseBody
    public JsonResponseMsg top(Long commodityId) {
        JsonResponseMsg result = new JsonResponseMsg();
        PcCommodity commodity=pcCommodityService.findById(commodityId);
		List<PcCommodity> list = pcCommodityService.getTopList();
		for (PcCommodity c : list) {
			c.setIsTop(0);
		}

        commodity.setIsTop(1);
        commodity.setUpdateDate(new Date());
		list.add(commodity);
		pcCommodityService.save(list);
        return result.fill(CommonConstant.CODE_SUCCESS,CommonConstant.MSG_SUCCESS);
    }

    /**
     * 兑换商品上下架
     */
    @RequestMapping("isSale")
    @ResponseBody
    public JsonResponseMsg isSale(String commodityId,Integer isSale) {
        JsonResponseMsg result = new JsonResponseMsg();
        List<String> commdityIdList= Arrays.asList(commodityId.split(","));
        List<PcCommodity> list=new ArrayList<>();
        for(String idStr:commdityIdList) {
            if(!NumberUtils.isNumber(idStr)){
                continue;
            }
            long id=NumberUtils.toLong(idStr);
            PcCommodity commodity = pcCommodityService.findById(id);
            if (isSale == 0) {
                if (commodity.getIsSale() == 0) {
                    continue;
                }
            }
            if (isSale == 1) {
                if (commodity.getIsSale() == 1) {
                    continue;
                }
                if(pcCommodityService.getLeftCount(id)==0){
                    continue;
                }
            }
            commodity.setIsSale(isSale);
            commodity.setUpdateDate(new Date());
            list.add(commodity);
            pcCommodityService.save(commodity);

            if (isSale == 1) {
                pcCommodityService.setLeftCount(id);
            } else if (isSale == 0) {
                pcCommodityService.clearLeftCount(id);
            }
        }

        if(CollectionUtils.isNotEmpty(list)){
            pcCommodityService.save(list);
        }
        return result.fill(CommonConstant.CODE_SUCCESS,CommonConstant.MSG_SUCCESS);
    }












}
