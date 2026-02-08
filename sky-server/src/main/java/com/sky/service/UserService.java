package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;


public interface UserService {
    User wxLogin(UserLoginDTO userLoginDTO);
}
