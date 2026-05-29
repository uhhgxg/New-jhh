package com.campus.trade.mapper;

import com.github.pagehelper.Page;
import com.campus.trade.annotation.AutoFill;
import com.campus.trade.dto.AdminPageQueryDTO;
import com.campus.trade.entity.Admin;
import com.campus.trade.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminMapper {

    @Select("select * from admin_user where username = #{username}")
    Admin getByUsername(String username);

    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into admin_user (username, name, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) "
            + "values" +
            " (#{username}, #{name}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Admin admin);

    @Select("select * from admin_user where id = #{id}")
    Admin getById(Long id);

    Page<Admin> pageQuery(AdminPageQueryDTO adminPageQueryDTO);

    @AutoFill(value = OperationType.UPDATE)
    void update(Admin admin);
}
