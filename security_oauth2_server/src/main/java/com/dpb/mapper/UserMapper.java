package com.dpb.mapper;

import com.dpb.domain.UserPojo;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    public UserPojo queryByUserName(@Param("userName") String userName);
}
