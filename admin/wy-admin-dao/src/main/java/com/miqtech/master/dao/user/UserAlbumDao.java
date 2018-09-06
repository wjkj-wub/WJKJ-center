package com.miqtech.master.dao.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.user.UserAlbum;

/**
 * 用户相册操作DAO
 */
public interface UserAlbumDao extends PagingAndSortingRepository<UserAlbum, Long>, JpaSpecificationExecutor<UserAlbum> {

}