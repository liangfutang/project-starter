package com.zjut.spring.boot.filter;

import com.zjut.common.constants.CommonConstants;
import com.zjut.spring.boot.autoconfigure.DataSourceAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * 设置访问的唯一id
 */
public class TraceIdFilter extends OncePerRequestFilter {

    private final static Logger logger = LoggerFactory.getLogger(TraceIdFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String traceId = request.getParameter(CommonConstants.TRACE_ID);
        if (StringUtils.isEmpty(traceId)) {
            traceId = UUID.randomUUID().toString();
            logger.info("服务生成的traceId：" + traceId + "!请求开始：" + request.getRequestURI());
        }
        MDC.put(CommonConstants.TRACE_ID, traceId);
        filterChain.doFilter(request, response);
        MDC.clear();
        logger.info("请求结束,traceId：" + traceId);
    }
}
