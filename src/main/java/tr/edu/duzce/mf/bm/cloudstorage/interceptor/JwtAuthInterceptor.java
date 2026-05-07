package tr.edu.duzce.mf.bm.cloudstorage.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.UserService;
import tr.edu.duzce.mf.bm.cloudstorage.util.JwtUtil;

import java.util.Arrays;

/**
 * JWT tabanlı kimlik doğrulama kontrolü yapan interceptor.
 * İsteklerdeki 'JWT_TOKEN' isimli çerezi kontrol eder.
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        logger.debug("İstek işleniyor: {}", requestUri);

        // JWT_TOKEN çerezini bul
        String token = null;
        if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(c -> "JWT_TOKEN".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (token != null) {
            try {
                String email = jwtUtil.extractEmail(token);
                User user = userService.findByEmail(email);

                if (user != null && jwtUtil.validateToken(token, user)) {
                    logger.debug("JWT doğrulaması başarılı. Kullanıcı: {}", email);
                    // İstek attribute'una kullanıcıyı ekle (opsiyonel, controller'da kullanılabilir)
                    request.setAttribute("currentUser", user);
                    return true;
                }
            } catch (Exception e) {
                logger.error("JWT doğrulama sırasında hata oluştu: {}", e.getMessage());
            }
        }

        logger.warn("Geçersiz veya eksik JWT! /login sayfasına yönlendiriliyor. URI: {}", requestUri);
        response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }
}
