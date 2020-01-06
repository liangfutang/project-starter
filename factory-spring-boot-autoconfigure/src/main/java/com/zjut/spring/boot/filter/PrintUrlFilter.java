package com.zjut.spring.boot.filter;

import com.zjut.spring.boot.autoconfigure.DataSourceAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: jsk
 * @date: 2019/8/14 19:45
 */
public class PrintUrlFilter extends OncePerRequestFilter {
    private final static Logger logger = LoggerFactory.getLogger(DataSourceAutoConfiguration.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.info("requestUri:" + request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}