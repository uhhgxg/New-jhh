package com.campus.trade.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.campus.trade.constant.MessageConstant;
import com.campus.trade.constant.PasswordConstant;
import com.campus.trade.constant.StatusConstant;
import com.campus.trade.dto.AdminDTO;
import com.campus.trade.dto.AdminLoginDTO;
import com.campus.trade.dto.AdminPageQueryDTO;
import com.campus.trade.dto.PasswordEditDTO;
import com.campus.trade.entity.Admin;
import com.campus.trade.exception.AccountLockedException;
import com.campus.trade.exception.AccountNotFoundException;
import com.campus.trade.exception.PasswordEditFailedException;
import com.campus.trade.exception.PasswordErrorException;
import com.campus.trade.mapper.AdminMapper;
import com.campus.trade.result.PageResult;
import com.campus.trade.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    /**
     * 管理员登录
     */
    @Override
    public Admin login(AdminLoginDTO adminLoginDTO) {
        String username = adminLoginDTO.getUsername();
        String password = adminLoginDTO.getPassword();

        // 根据用户名查询数据库中的数据
        Admin admin = adminMapper.getByUsername(username);

        // 处理各种异常情况
        if (admin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 密码比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(admin.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        // 检查账号状态
        if (admin.getStatus() != null && admin.getStatus().equals(StatusConstant.DISABLE)) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        return admin;
    }

    /**
     * 新增管理员
     */
    @Override
    public void save(AdminDTO adminDTO) {
        log.info("新增管理员：{}", adminDTO.getUsername());
        Admin admin = Admin.builder()
                .status(StatusConstant.ENABLE)
                .password(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()))
                .build();
        BeanUtils.copyProperties(adminDTO, admin);
        adminMapper.insert(admin);
        log.info("管理员新增成功，id：{}", admin.getId());
    }

    /**
     * 管理员分页查询
     */
    @Override
    public PageResult pageQuery(AdminPageQueryDTO adminPageQueryDTO) {
        PageHelper.startPage(adminPageQueryDTO.getPage(), adminPageQueryDTO.getPageSize());
        Page<Admin> page = adminMapper.pageQuery(adminPageQueryDTO);
        long total = page.getTotal();
        List<Admin> records = page.getResult();
        return new PageResult(total, records);
    }

    /**
     * 修改管理员状态
     */
    @Override
    public void updateStatus(Integer status, Long id) {
        log.info("修改管理员状态，id：{}，status：{}", id, status);
        Admin admin = Admin.builder()
                .id(id)
                .status(status)
                .build();
        adminMapper.update(admin);
    }

    /**
     * 根据ID查询管理员
     */
    @Override
    public Admin getById(Long id) {
        return adminMapper.getById(id);
    }

    /**
     * 修改管理员信息
     */
    @Override
    public void update(AdminDTO adminDTO) {
        log.info("修改管理员信息，id：{}", adminDTO.getId());
        Admin admin = Admin.builder().build();
        BeanUtils.copyProperties(adminDTO, admin);
        adminMapper.update(admin);
    }

    /**
     * 修改密码
     */
    @Override
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        log.info("修改密码，adminId：{}", passwordEditDTO.getAdminId());

        Admin admin = adminMapper.getById(passwordEditDTO.getAdminId());
        if (admin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 校验旧密码
        String oldPassword = DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes());
        if (!oldPassword.equals(admin.getPassword())) {
            throw new PasswordEditFailedException(MessageConstant.PASSWORD_ERROR);
        }

        // 设置新密码
        String newPassword = DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes());
        Admin updateAdmin = Admin.builder()
                .id(passwordEditDTO.getAdminId())
                .password(newPassword)
                .build();
        adminMapper.update(updateAdmin);
        log.info("密码修改成功");
    }
}
