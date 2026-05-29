package com.campus.trade.service;

import com.campus.trade.dto.EvaluationDTO;
import com.campus.trade.vo.EvaluationVO;

import java.util.List;

public interface EvaluationService {
    void createEvaluation(EvaluationDTO evaluationDTO);

    List<EvaluationVO> getEvaluationsByUser(Long revieweeId);
}
