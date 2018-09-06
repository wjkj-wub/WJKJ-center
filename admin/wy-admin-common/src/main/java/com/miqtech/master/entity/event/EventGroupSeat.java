package com.miqtech.master.entity.event;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name="oet_event_group_seat")
public class EventGroupSeat extends IdEntity{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6360175978728054522L;
	@Column(name="group_id")
	private String groupId;
	@Column(name="target_id")
	private Long targetId;
	@Column(name="seat")
	private Integer seat;
	@Column(name="seat_number")
	private Integer seatNumber;
	@Column(name="is_win")
	private Integer isWin;
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public Long getTargetId() {
		return targetId;
	}
	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}
	public Integer getSeat() {
		return seat;
	}
	public void setSeat(Integer seat) {
		this.seat = seat;
	}
	public Integer getSeatNumber() {
		return seatNumber;
	}
	public void setSeatNumber(Integer seatNumber) {
		this.seatNumber = seatNumber;
	}
	public Integer getIsWin() {
		return isWin;
	}
	public void setIsWin(Integer isWin) {
		this.isWin = isWin;
	}
	
}
