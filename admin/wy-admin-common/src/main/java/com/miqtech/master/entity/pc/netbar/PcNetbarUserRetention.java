package com.miqtech.master.entity.pc.netbar;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "pc_netbar_user_retention")
public class PcNetbarUserRetention implements Serializable {

	private static final long serialVersionUID = -3831483164934619879L;

	private Long id; // AUTO_INCREMENT
	private Date date; // 时间
	private Long netbarId; // 网吧ID
	private Integer registerCount; // 注册数
	private Integer activeCount; // 活跃数

	@Id
	@Column(name = "id", nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "date")
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "register_count")
	public Integer getRegisterCount() {
		return registerCount;
	}

	public void setRegisterCount(Integer registerCount) {
		this.registerCount = registerCount;
	}

	@Column(name = "active_count")
	public Integer getActiveCount() {
		return activeCount;
	}

	public void setActiveCount(Integer activeCount) {
		this.activeCount = activeCount;
	}

}
