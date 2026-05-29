package com.campus.trade.service;

import com.campus.trade.dto.UserLoginDTO;
import com.campus.trade.entity.User;

public interface UserService {
    User wxLogin (UserLoginDTO userLoginDTO);
}
