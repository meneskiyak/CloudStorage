package tr.edu.duzce.mf.bm.cloudstorage.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT (JSON Web Token) işlemlerini yöneten yardımcı sınıf.
 * HS256 algoritması kullanılarak token üretimi, doğrulama ve veri çıkarma işlemlerini yapar.
 */
@Component
public class JwtUtil {

    // Gerçek uygulamalarda bu değer bir yapılandırma dosyasından (properties) okunmalıdır.
    private static final String SECRET_KEY_STRING = "bu_cok_gizli_ve_guclu_bir_anahtar_olmalidir_en_az_256_bit";
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());

    // Token geçerlilik süresi: 1 saat
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;

    /**
     * Kullanıcı nesnesinden JWT üretir.
     * @param user Token üretilecek kullanıcı
     * @return Üretilen JWT string'i
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        return createToken(claims, user.getEmail());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Token'dan email (subject) bilgisini okur.
     * @param token JWT string'i
     * @return Email adresi
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Token'ın süresinin dolup dolmadığını kontrol eder.
     * @param token JWT string'i
     * @return Süresi dolmuşsa true, dolmamışsa false
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Token'ın geçerliliğini doğrular.
     * @param token JWT string'i
     * @param user Karşılaştırılacak kullanıcı nesnesi
     * @return Geçerli ise true
     */
    public boolean validateToken(String token, User user) {
        final String email = extractEmail(token);
        return (email.equals(user.getEmail()) && !isTokenExpired(token));
    }
}
