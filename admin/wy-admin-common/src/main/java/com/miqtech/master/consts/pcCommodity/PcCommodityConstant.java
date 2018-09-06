package com.miqtech.master.consts.pcCommodity;

/**
 * @author shilina
 */
public class PcCommodityConstant {

    private PcCommodityConstant() {
        super();
    }

    public static final String PC_COMMODITY_NUM="pc_commodity_num_";//兑换商品数量

    public static final int COMMODITY_STATUS_EXCHANGE_WAITING=0; //待支付
    public static final int COMMODITY_STATUS_EXCHANGE=1; //充值中
    public static final int COMMODITY_STATUS_EXCHANGE_SUCCESS=2; //交易完成
    public static final int COMMODITY_STATUS_REFUND_WAIT=3; //待退款
    public static final int COMMODITY_STATUS_REFUND_SUCCESS=4; //退款成功

    public static final int PC_COMMODITY_COUNT_INFINITE=-99;//商品数量无限时将值设为-99

    public static final String COMMODITY_TYPE_Q_COIN="1"; //Q币
    public static final String COMMODITY_TYPE_TELE_FARE="2"; //话费
    public static final String COMMODITY_TYPE_TELE_OBJECT="3"; //实物
    public static final String COMMODITY_TYPE_TELE_STAMPS="4"; //英雄联盟点券
}
