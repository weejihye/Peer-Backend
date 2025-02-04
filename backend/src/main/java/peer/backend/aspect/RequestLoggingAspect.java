package peer.backend.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Aspect
@Slf4j
public class RequestLoggingAspect {

    private String paramMapToString(Map<String, String[]> paramMap) {
        return paramMap.entrySet().stream()
            .map(entry -> String.format("%s -> (%s)",
                entry.getKey(), String.join(",", entry.getValue())))
            .collect(Collectors.joining(", "));
    }

    private String getRequestInfo(ProceedingJoinPoint pjp, HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();

        Map<String, String[]> paramMap = request.getParameterMap();
        String params = "";
        if (!paramMap.isEmpty()) {
            params = " [" + paramMapToString(paramMap) + "]";
        }

        return builder
            .append(pjp.getSignature())
            .append(" ")
            .append(request.getMethod())
            .append(" ")
            .append(request.getRequestURI())
            .append(" ")
            .append(params)
            .toString();
    }

    @Pointcut("execution(public * peer.backend.controller..*(..))")
    private void onRequest() {
    }

    @Pointcut("@annotation(peer.backend.annotation.NoLogging)")
    private static void noLogging() {

    }

    @Around("peer.backend.aspect.RequestLoggingAspect.onRequest() && !peer.backend.aspect.RequestLoggingAspect.noLogging()")
    public Object loggingApi(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        ObjectMapper mapper = new ObjectMapper();
        String requestInfo = this.getRequestInfo(pjp, request);

        try {
            long start = System.currentTimeMillis();
            log.info("[Request] {}", requestInfo);
            Object result = pjp.proceed();
            long end = System.currentTimeMillis();
            log.info("[Response] {}: {} < ({}ms)", requestInfo,
                mapper.writeValueAsString(result), end - start);
            return result;
        } catch (Exception e) {
            StringBuilder message = new StringBuilder();

            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                message.append(System.lineSeparator()).append(stackTraceElement.toString());
            }

            log.error("[Error] {} {} {}", requestInfo, e, message.toString());
            throw e;
        }
    }
}
