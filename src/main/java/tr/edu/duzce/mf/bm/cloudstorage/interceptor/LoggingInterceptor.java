package tr.edu.duzce.mf.bm.cloudstorage.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// Slaytlardaki gibi SLF4J importları kullanılıyor
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

public class LoggingInterceptor implements HandlerInterceptor {

    // Slaytlardaki birebir LoggerFactory kullanımı
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        Map<String, String[]> paramMap = request.getParameterMap();

        StringBuilder params = new StringBuilder();
        paramMap.forEach((key, values) -> {
            params.append(key).append("=").append(String.join(",", values)).append("; ");
        });

        logger.info(">>> ISTEK | URI: {} | Parametreler: {}", uri, params.length() > 0 ? params.toString() : "Yok");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (modelAndView != null && modelAndView.getViewName() != null) {
            logger.info("<<< YANIT | View: {}", modelAndView.getViewName());
        }
    }
}