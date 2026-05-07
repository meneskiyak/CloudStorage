package tr.edu.duzce.mf.bm.cloudstorage.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tr.edu.duzce.mf.bm.cloudstorage.util.JwtUtil;

/**
 * JWT tabanlı kimlik doğrulama kontrolünü yapan interceptor.
 * Her istekte 'JWT_TOKEN' çerezini kontrol eder ve JwtUtil ile doğrular.
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        String token = null;

        // İstek içindeki çerezlerden 'JWT_TOKEN' olanı buluyoruz
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        try {
            // Token mevcut değilse veya süresi dolmuşsa/geçersizse kullanıcıyı giriş sayfasına yönlendiriyoruz
            if (token == null || jwtUtil.isTokenExpired(token)) {
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }

            // Token'dan email bilgisini başarılı bir şekilde alabildiğimizi kontrol ediyoruz
            String email = jwtUtil.extractEmail(token);
            if (email == null || email.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
            
            // Token geçerli, isteğin hedefine ulaşmasına izin veriyoruz
            return true;

        } catch (Exception e) {
            // Token doğrulama sırasında herhangi bir hata oluşursa (geçersiz imza, bozuk format vb.) girişe yönlendiriyoruz
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
    }
}
