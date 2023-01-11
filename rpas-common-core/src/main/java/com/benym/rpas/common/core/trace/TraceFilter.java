package com.benym.rpas.common.core.trace;

import cn.hutool.json.JSONUtil;
import com.benym.rpas.common.core.logger.LoggerHttp;
import com.benym.rpas.common.dto.enums.Trace;
import com.benym.rpas.common.dto.exception.ExceptionFactory;
import com.benym.rpas.common.dto.request.RequestLog;
import com.benym.rpas.common.utils.SnowflakeUtils;
import com.benym.rpas.common.utils.TraceIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author benym
 * @date 2022/7/7 22:01
 */
@Order(1)
@WebFilter(filterName = "traceFilter", urlPatterns = {"/*"})
@Component
public class TraceFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(TraceFilter.class);


    @Value("${trace.log.activate:true}")
    private boolean traceLogActivate;

    public TraceFilter() {
        // 空实例化
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        StopWatch stopWatch = new StopWatch("Http Trace");
        stopWatch.start();
        Trace trace = TraceIdUtils.getTrace();
        TraceRequestWrapper traceRequestWrapper;
        RequestLog requestLog = null;
        TraceResponseWrapper traceResponseWrapper = new TraceResponseWrapper((HttpServletResponse) response);
        traceRequestWrapper = new TraceRequestWrapper((HttpServletRequest) request);
        try {
            if (traceLogActivate) {
                requestLog = LoggerHttp.init(traceRequestWrapper);
            }
            // 初始化入参信息
            traceRequestWrapper.putHeader(Trace.TRACE_ID, trace.getTraceId());
            traceRequestWrapper.putHeader(Trace.SPAN_ID, trace.getSpanId());
            chain.doFilter(traceRequestWrapper, traceResponseWrapper);
            // doFilter之后表示当前请求结束，下一次属于另外一个Span
            MDC.put(Trace.SPAN_ID, String.valueOf(SnowflakeUtils.get().next()));
            stopWatch.stop();
            if (traceLogActivate) {
                // 更新出参信息
                assert requestLog != null;
                LoggerHttp.update(requestLog, traceResponseWrapper, stopWatch.getTotalTimeMillis());
                // 打印请求日志，包括入参、出参
                String requests = JSONUtil.toJsonStr(requestLog);
                logger.info(requests);
            } else {
                logger.info("请求url:{}, 耗时(ms):{}", traceRequestWrapper.getRequestURI(), stopWatch.getTotalTimeMillis());
            }
        } catch (Exception e) {
            throw ExceptionFactory.sysException("traceId Filter unkonwn exception", e);
        } finally {
            // 请求完成之后同步清理traceId
            TraceIdUtils.clearTrace();
        }
    }
}
