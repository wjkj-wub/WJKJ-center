package com.miqtech.master.dao.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityNetbarQrcode;

public interface ActivityNetbarQrcodeDao
		extends PagingAndSortingRepository<ActivityNetbarQrcode, Long>, JpaSpecificationExecutor<ActivityNetbarQrcode> {

	ActivityNetbarQrcode findByIdAndValid(long id, int valid);

	List<ActivityNetbarQrcode> findByActivityIdAndValid(long activityId, int valid);

}
