package com.miqtech.master.entity.netbar;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 业主交接班详情
 */
@Entity
@Table(name = "netbar_r_owner_batch_order")
public class NetbarOwnerBatchOrder implements Serializable {

	private static final long serialVersionUID = -5830178138612152662L;

	private Long id;
	private Long staffBatchId;
	private Long batchId;

	@Id
	@Column(name = "id", nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "staff_batch_id")
	public Long getStaffBatchId() {
		return staffBatchId;
	}

	public void setStaffBatchId(Long staffBatchId) {
		this.staffBatchId = staffBatchId;
	}

	@Column(name = "batch_id")
	public Long getBatchId() {
		return batchId;
	}

	public void setBatchId(Long batchId) {
		this.batchId = batchId;
	}

}
