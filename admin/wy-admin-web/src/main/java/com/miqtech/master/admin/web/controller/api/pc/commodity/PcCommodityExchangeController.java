package com.miqtech.master.admin.web.controller.api.pc.commodity;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.pcCommodity.PcCommodityConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.pc.commodity.PcCommodityExchange;
import com.miqtech.master.service.pc.commodity.PcCommodityExchangeService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.*;


/**
 * @author shilina
 */
@Controller
@RequestMapping("api/pc/commodity/exchange")
public class PcCommodityExchangeController {
    @Autowired
    private PcCommodityExchangeService pcCommodityExchangeService;


    /**
     *订单列表
     */
    @RequestMapping("list")
    @ResponseBody
    public JsonResponseMsg list(Integer status, Integer type, String start, String end, String nickname,Integer page,Integer pageSize){
        JsonResponseMsg result=new JsonResponseMsg();
        PageVO vo=pcCommodityExchangeService.list(status, type, start, end, nickname,page,pageSize);
        return result.fill(CommonConstant.CODE_SUCCESS,CommonConstant.MSG_SUCCESS,vo);
    }

    /**
     *更改订单状态
     */
    @RequestMapping("updateStatus")
    @ResponseBody
    public JsonResponseMsg updateStatus(String commodityIds){
        JsonResponseMsg result=new JsonResponseMsg();
        List<String> ids=Arrays.asList(commodityIds.split(","));
        List<PcCommodityExchange> list=new ArrayList<>();
        for(String idStr:ids){
            if(NumberUtils.isNumber(idStr)){
                Long id=NumberUtils.toLong(idStr);
                PcCommodityExchange pcCommodityExchange=pcCommodityExchangeService.findById(id);
                if(pcCommodityExchange.getStatus().equals(PcCommodityConstant.COMMODITY_STATUS_EXCHANGE)){
                    pcCommodityExchange.setStatus(PcCommodityConstant.COMMODITY_STATUS_EXCHANGE_SUCCESS);
                }else if(pcCommodityExchange.getStatus().equals(PcCommodityConstant.COMMODITY_STATUS_REFUND_WAIT)){
                    pcCommodityExchange.setStatus(PcCommodityConstant.COMMODITY_STATUS_REFUND_SUCCESS);
                }

                list.add(pcCommodityExchange);
            }
        }
        if(CollectionUtils.isNotEmpty(list)){
            pcCommodityExchangeService.save(list);
        }
        return result.fill(CommonConstant.CODE_SUCCESS,CommonConstant.MSG_SUCCESS);
    }

    /**
     *订单管理导出
     */
    @RequestMapping("export")
    @ResponseBody
    public void export(HttpServletResponse response,Integer status, Integer type, String start, String end, String nickname)
            throws UnsupportedEncodingException {
        int page=1;
        int pageSize=Integer.MAX_VALUE;
        PageVO vo=pcCommodityExchangeService.list(status, type, start, end, nickname,page,pageSize);
        String title = "订单管理列表_"+ DateUtils.dateToString(new Date(),DateUtils.YYYY_MM_DD);
        response.setHeader("content-disposition",
                "attachment;filename=" + new String(title.getBytes(), "ISO8859-1") + ".xls");
        // 编辑excel内容
        List<Map<String, Object>> list = vo.getList();
        String[][] contents = new String[list.size() + 1][];

        // 设置标题行
        String[] contentTitle = new String[8];
        contentTitle[0] = "订单时间";
        contentTitle[1] = "用户昵称";
        contentTitle[2] = "奖品类别";
        contentTitle[3] = "奖品名称";
        contentTitle[4] = "数量";
        contentTitle[5] = "订单总金额";
        contentTitle[6] = "用户联系方式";
        contentTitle[7] = "状态";
        contents[0] = contentTitle;
        // 设置内容
        if (CollectionUtils.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> obj = list.get(i);
                String[] row = new String[8];
                row[0] = MapUtils.getString(obj, "create_date");
                row[1] = MapUtils.getString(obj, "nickname");
                row[2] = MapUtils.getString(obj, "type").equals(PcCommodityConstant.COMMODITY_TYPE_Q_COIN)?"Q币":
                        MapUtils.getString(obj, "type").equals(PcCommodityConstant.COMMODITY_TYPE_TELE_FARE)?
                        "话费":MapUtils.getString(obj, "type").equals(PcCommodityConstant.COMMODITY_TYPE_TELE_OBJECT)? "实物":"英雄联盟点劵";
                row[3] = MapUtils.getString(obj, "name");
                row[4] = MapUtils.getString(obj, "num");
                row[5] = MapUtils.getString(obj, "totalSum");
                row[6] = StringUtils.isNotBlank(MapUtils.getString(obj, "qq"))?"qq:"+MapUtils.getString(obj, "qq"):
                        "手机:"+MapUtils.getString(obj, "telephone");
				row[7] = ("1").equals(MapUtils.getString(obj, "status")) ? "充值中" : "交易完成";
                contents[i + 1] = row;
            }
        }

        try {
            ExcelUtils.exportExcel2(title, contents, false, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
