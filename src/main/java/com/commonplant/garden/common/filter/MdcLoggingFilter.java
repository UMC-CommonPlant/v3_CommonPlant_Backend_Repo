package com.commonplant.garden.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 모든 요청에 traceId를 부여해 로그 추적을 가능하게 하는 필터
 *
 * - traceId는 요청 헤더 "X-Trace-Id"가 있으면 그것을 사용 (API Gateway 연동)
 * - 없으면 UUID를 새로 발급
 * - 응답 헤더에도 "X-Trace-Id"를 추가해 클라이언트가 추적 가능하도록 함
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = resolveTraceId(request);

        try {
            MDC.put(MDC_KEY, traceId);
            response.addHeader(TRACE_ID_HEADER, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String header = request.getHeader(TRACE_ID_HEADER);
        return (header != null && !header.isBlank())
                ? header
                : UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}