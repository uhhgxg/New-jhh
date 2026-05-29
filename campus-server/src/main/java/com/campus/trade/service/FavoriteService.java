package com.campus.trade.service;

import com.campus.trade.dto.FavoriteDTO;
import com.campus.trade.result.PageResult;
import com.campus.trade.vo.FavoriteVO;

public interface FavoriteService {
    void addFavorite(FavoriteDTO favoriteDTO);

    void removeFavorite(Long itemId);

    PageResult pageQuery(int page, int pageSize);

    boolean isFavorited(Long itemId);
}
