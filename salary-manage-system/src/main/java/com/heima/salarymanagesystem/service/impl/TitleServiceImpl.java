package com.heima.salarymanagesystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.salarymanagesystem.entity.Title;
import com.heima.salarymanagesystem.mapper.TitleMapper;
import com.heima.salarymanagesystem.service.TitleService;
import org.springframework.stereotype.Service;

/**
 * 职称服务实现
 */
@Service
public class TitleServiceImpl extends ServiceImpl<TitleMapper, Title> implements TitleService {
}
