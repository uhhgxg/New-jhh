package com.campus.trade.service;

import com.campus.trade.dto.AdminDTO;
import com.campus.trade.dto.AdminLoginDTO;
import com.campus.trade.dto.AdminPageQueryDTO;
import com.campus.trade.dto.PasswordEditDTO;
import com.campus.trade.entity.Admin;
import com.campus.trade.result.PageResult;

public interface AdminService {
    Admin login(AdminLoginDTO adminLoginDTO);

    void save(AdminDTO adminDTO);

    PageResult pageQuery(AdminPageQueryDTO adminPageQueryDTO);

    void updateStatus(Integer status, Long id);

    Admin getById(Long id);

    void update(AdminDTO adminDTO);

    void editPassword(PasswordEditDTO passwordEditDTO);
}
