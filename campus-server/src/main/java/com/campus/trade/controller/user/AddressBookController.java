package com.campus.trade.controller.user;

import com.campus.trade.context.BaseContext;
import com.campus.trade.entity.AddressBook;
import com.campus.trade.result.Result;
import com.campus.trade.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 地址簿控制器
 * 提供C端用户地址簿相关的RESTful API接口
 */
@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "C端地址簿接口")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 查询当前登录用户的所有地址信息
     *
     * @return 返回包含用户所有地址信息的Result对象
     */
    @GetMapping("/list")
    @ApiOperation("查询当前登录用户的所有地址信息")
    public Result<List<AddressBook>> list() {
        // 创建地址簿对象并设置当前用户ID
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        // 调用服务层方法查询地址列表
        List<AddressBook> list = addressBookService.list(addressBook);
        return Result.success(list);
    }

    /**
     * 新增地址
     *
     * @param addressBook 包含地址信息的AddressBook对象
     * @return 返回操作结果的Result对象
     */
    @PostMapping
    @ApiOperation("新增地址")
    public Result save(@RequestBody AddressBook addressBook) {
        // 调用服务层方法保存地址信息
        addressBookService.save(addressBook);
        return Result.success();
    }

    /**
     * 根据id查询地址
     *
     * @param id 地址ID
     * @return 返回包含地址信息的Result对象
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getById(@PathVariable Long id) {
        // 调用服务层方法根据ID查询地址
        AddressBook addressBook = addressBookService.getById(id);
        return Result.success(addressBook);
    }

    /**
     * 根据id修改地址
     *
     * @param addressBook 包含更新后地址信息的AddressBook对象
     * @return 返回操作结果的Result对象
     */
    @PutMapping
    @ApiOperation("根据id修改地址")
    public Result update(@RequestBody AddressBook addressBook) {
        // 调用服务层方法更新地址信息
        addressBookService.update(addressBook);
        return Result.success();
    }

    /**
     * 设置默认地址
     *
     * @param addressBook 包含地址信息的AddressBook对象
     * @return 返回操作结果的Result对象
     */
    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefault(@RequestBody AddressBook addressBook) {
        // 调用服务层方法设置默认地址
        addressBookService.setDefault(addressBook);
        return Result.success();
    }

    /**
     * 根据id删除地址
     *
     * @param id 要删除的地址ID
     * @return 返回操作结果的Result对象
     */
    @DeleteMapping
    @ApiOperation("根据id删除地址")
    public Result deleteById(Long id) {
        // 调用服务层方法删除地址
        addressBookService.deleteById(id);
        return Result.success();
    }

    /**
     * 查询默认地址
     *
     * @return 返回包含默认地址信息的Result对象，如果没有默认地址则返回错误信息
     */
    @GetMapping("default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> getDefault() {
        //SQL:select * from address_book where user_id = ? and is_default = 1
        // 创建地址簿对象并设置默认标识和当前用户ID
        AddressBook addressBook = new AddressBook();
        addressBook.setIsDefault(1);
        addressBook.setUserId(BaseContext.getCurrentId());
        // 查询默认地址列表
        List<AddressBook> list = addressBookService.list(addressBook);

        // 如果查询到且只有一个默认地址，则返回该地址
        if (list != null && list.size() == 1) {
            return Result.success(list.get(0));
        }

        // 没有查询到默认地址时返回错误信息
        return Result.error("没有查询到默认地址");
    }

}
