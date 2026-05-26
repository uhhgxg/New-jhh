package com.sky.context;

/**
 * BaseContext类用于管理线程局部变量(ThreadLocal)的工具类
 * 主要用于在多线程环境下存储和获取当前线程的用户ID
 */
public class BaseContext {

    // 使用ThreadLocal存储Long类型的用户ID，确保每个线程有自己的独立副本
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前线程的用户ID
     * @param id 要设置的用户ID
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * 获取当前线程的用户ID
     * @return 当前线程的用户ID，如果未设置则返回null
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    /**
     * 移除当前线程的用户ID
     * 通常在请求处理完成后调用，防止内存泄漏
     */
    public static void removeCurrentId() {
        threadLocal.remove();
    }

}
