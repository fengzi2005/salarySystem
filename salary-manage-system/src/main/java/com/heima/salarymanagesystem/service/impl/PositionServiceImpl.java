package com.heima.salarymanagesystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.salarymanagesystem.entity.Position;
import com.heima.salarymanagesystem.mapper.PositionMapper;
import com.heima.salarymanagesystem.service.PositionService;
import org.springframework.stereotype.Service;

/**
 * 岗位服务实现
 */
@Service
public class PositionServiceImpl extends ServiceImpl<PositionMapper, Position> implements PositionService {
}
