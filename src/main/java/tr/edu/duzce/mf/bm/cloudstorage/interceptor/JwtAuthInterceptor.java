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
        logger.debug("Interceptor tetiklendi. URI: {}", requestUri);

        // JWT_TOKEN çerezini bul
        String token = null;
        if (request.getCookies() != null) {
            logger.debug("Çerezler bulundu, JWT_TOKEN aranıyor...");
            token = Arrays.stream(request.getCookies())
                    .filter(c -> "JWT_TOKEN".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        } else {
            logger.debug("İstekte hiç çerez bulunamadı.");
        }

        if (token != null) {
            logger.debug("Token bulundu, doğrulanıyor...");
            try {
                String email = jwtUtil.extractEmail(token);
                logger.debug("Token'dan çıkarılan email: {}", email);
                User user = userService.findByEmail(email);

                if (user != null && jwtUtil.validateToken(token, user)) {
                    logger.debug("JWT doğrulaması başarılı. Kullanıcı: {}", email);
                    request.setAttribute("currentUser", user);
                    return true;
                } else {
                    logger.warn("Token geçersiz veya kullanıcı bulunamadı! User null mu? {}", (user == null));
                }
            } catch (Exception e) {
                logger.error("JWT ayrıştırma/doğrulama hatası: {}", e.getMessage());
            }
        } else {
            logger.warn("İstekte JWT_TOKEN çerezi eksik!");
        }

        logger.warn("Yetkisiz erişim denemesi! /login sayfasına yönlendiriliyor. URI: {}", requestUri);
        response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }
}
