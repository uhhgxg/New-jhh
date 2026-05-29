package com.campus.trade.service.impl;

import com.campus.trade.context.BaseContext;
import com.campus.trade.dto.EvaluationDTO;
import com.campus.trade.entity.Evaluation;
import com.campus.trade.mapper.EvaluationMapper;
import com.campus.trade.service.EvaluationService;
import com.campus.trade.vo.EvaluationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class EvaluationServiceImpl implements EvaluationService {

    @Autowired
    private EvaluationMapper evaluationMapper;

    /**
     * 创建评价
     */
    @Override
    @Transactional
    public void createEvaluation(EvaluationDTO evaluationDTO) {
        log.info("创建评价，orderId：{}，revieweeId：{}", evaluationDTO.getOrderId(), evaluationDTO.getRevieweeId());

        Long reviewerId = BaseContext.getCurrentId();

        Evaluation evaluation = Evaluation.builder()
                .orderId(evaluationDTO.getOrderId())
                .reviewerId(reviewerId)
                .revieweeId(evaluationDTO.getRevieweeId())
                .rating(evaluationDTO.getRating())
                .content(evaluationDTO.getContent())
                .type(evaluationDTO.getType())
                .createTime(LocalDateTime.now())
                .build();

        evaluationMapper.insert(evaluation);
        log.info("评价创建成功，id：{}", evaluation.getId());
    }

    /**
     * 获取对某用户的评价列表
     */
    @Override
    public List<EvaluationVO> getEvaluationsByUser(Long revieweeId) {
        log.info("获取用户评价列表，revieweeId：{}", revieweeId);
        return evaluationMapper.getByRevieweeId(revieweeId);
    }
}
