package com.campus.trade.mapper;

import com.campus.trade.entity.Evaluation;
import com.campus.trade.vo.EvaluationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EvaluationMapper {

    void insert(Evaluation evaluation);

    Evaluation getByOrderId(@Param("orderId") Long orderId);

    List<EvaluationVO> getByRevieweeId(@Param("revieweeId") Long revieweeId);
}
