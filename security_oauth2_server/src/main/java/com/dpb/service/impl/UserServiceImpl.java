package com.dpb.service.impl;

import com.dpb.domain.UserPojo;
import com.dpb.mapper.UserMapper;
import com.dpb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @program: springboot-54-security-jwt-demo
 * @description:
 * @author: 波波烤鸭
 * @create: 2019-12-03 11:53
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper mapper;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        UserPojo user = mapper.queryByUserName(s);

        return user;
    }
}
