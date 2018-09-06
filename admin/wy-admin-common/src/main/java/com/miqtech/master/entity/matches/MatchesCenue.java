package com.miqtech.master.entity.matches;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "matches_cenue")
public class MatchesCenue extends IdEntity {

	private static final long serialVersionUID = -8248772421754217225L;

	@Column(name = "netbar_id")
	private Long netbarId;

	@Column(name = "match_process_id")
	private Long matchProcessId;

	@Column(name = "fight_date")
	private Date fightDate;

	@Column(name = "division")
	private String division;

	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	public Long getMatchProcessId() {
		return matchProcessId;
	}

	public void setMatchProcessId(Long matchProcessId) {
		this.matchProcessId = matchProcessId;
	}

	public Date getFightDate() {
		return fightDate;
	}

	public void setFightDate(Date fightDate) {
		this.fightDate = fightDate;
	}

	public String getDivision() {
		return division;
	}

	public void setDivision(String division) {
		this.division = division;
	}

}
