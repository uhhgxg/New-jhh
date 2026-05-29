package com.campus.trade.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.campus.trade.constant.MessageConstant;
import com.campus.trade.context.BaseContext;
import com.campus.trade.dto.FavoriteDTO;
import com.campus.trade.entity.Favorite;
import com.campus.trade.exception.DeletionNotAllowedException;
import com.campus.trade.mapper.FavoriteMapper;
import com.campus.trade.result.PageResult;
import com.campus.trade.service.FavoriteService;
import com.campus.trade.vo.FavoriteVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    /**
     * 添加收藏
     */
    @Override
    @Transactional
    public void addFavorite(FavoriteDTO favoriteDTO) {
        log.info("添加收藏，itemId：{}", favoriteDTO.getItemId());

        Long userId = BaseContext.getCurrentId();

        // 检查是否已收藏
        Favorite existFavorite = favoriteMapper.getByUserIdAndItemId(userId, favoriteDTO.getItemId());
        if (existFavorite != null) {
            throw new DeletionNotAllowedException(MessageConstant.ALREADY_EXISTS);
        }

        Favorite favorite = Favorite.builder()
                .userId(userId)
                .itemId(favoriteDTO.getItemId())
                .createTime(LocalDateTime.now())
                .build();

        favoriteMapper.insert(favorite);
        log.info("收藏成功，userId：{}，itemId：{}", userId, favoriteDTO.getItemId());
    }

    /**
     * 取消收藏
     */
    @Override
    @Transactional
    public void removeFavorite(Long itemId) {
        log.info("取消收藏，itemId：{}", itemId);

        Long userId = BaseContext.getCurrentId();
        favoriteMapper.delete(userId, itemId);

        log.info("取消收藏成功，userId：{}，itemId：{}", userId, itemId);
    }

    /**
     * 分页查询收藏列表
     */
    @Override
    public PageResult pageQuery(int page, int pageSize) {
        log.info("分页查询收藏列表，page：{}，pageSize：{}", page, pageSize);

        Long userId = BaseContext.getCurrentId();

        PageHelper.startPage(page, pageSize);
        List<FavoriteVO> list = favoriteMapper.pageQuery(userId);

        long total = list instanceof Page ? ((Page<FavoriteVO>) list).getTotal() : list.size();
        return new PageResult(total, list);
    }

    /**
     * 判断是否已收藏
     */
    @Override
    public boolean isFavorited(Long itemId) {
        Long userId = BaseContext.getCurrentId();
        Favorite favorite = favoriteMapper.getByUserIdAndItemId(userId, itemId);
        return favorite != null;
    }
}
