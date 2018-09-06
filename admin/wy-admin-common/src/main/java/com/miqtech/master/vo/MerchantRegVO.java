package com.miqtech.master.vo;

import java.io.Serializable;

import com.miqtech.master.entity.netbar.NetbarInfoTmp;
import com.miqtech.master.entity.netbar.NetbarMerchant;

/**
 * 用途:商户端用户注册传输数据
 */
public class MerchantRegVO implements Serializable {

	private static final long serialVersionUID = 5155348439913296492L;
	private NetbarInfoTmp netbarInfoTmp;
	private NetbarMerchant merchant;

	public NetbarInfoTmp getNetbarInfoTmp() {
		return netbarInfoTmp;
	}

	public void setNetbarInfoTmp(NetbarInfoTmp netbarInfoTmp) {
		this.netbarInfoTmp = netbarInfoTmp;
	}

	public NetbarMerchant getMerchant() {
		return merchant;
	}

	public void setMerchant(NetbarMerchant merchant) {
		this.merchant = merchant;
	}
}
