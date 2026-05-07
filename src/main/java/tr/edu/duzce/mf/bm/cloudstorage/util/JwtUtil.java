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
 * JWT (JSON Web Token) işlemlerini yürüten yardımcı bileşen.
 * Kullanıcı kimlik doğrulama ve yetkilendirme işlemleri için kullanılır.
 */
@Component
public class JwtUtil {

    // Güvenlik anahtarı - Gerçek senaryoda bu değer external bir config dosyasından okunmalıdır.
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    // Varsayılan token geçerlilik süresi: 1 saat
    private static final long DEFAULT_EXPIRATION = 1000 * 60 * 60;
    
    // Hatırla beni süresi: 7 gün
    public static final long REMEMBER_ME_EXPIRATION = 1000 * 60 * 60 * 24 * 7;

    /**
     * Verilen User nesnesi için JWT üretir. Varsayılan süreyi kullanır.
     */
    public String generateToken(User user) {
        return generateToken(user, DEFAULT_EXPIRATION);
    }

    /**
     * Verilen User nesnesi için belirlenen sürede geçerli bir JWT üretir.
     *
     * @param user       JWT üretilecek kullanıcı
     * @param expiration Milisaniye cinsinden geçerlilik süresi
     * @return Üretilen JWT
     */
    public String generateToken(User user, long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        return createToken(claims, user.getEmail(), expiration);
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Token içerisinden kullanıcı email bilgisini (subject) ayıklar.
     *
     * @param token İşlenecek JWT
     * @return Email adresi
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Token içerisinden son kullanma tarihini ayıklar.
     *
     * @param token İşlenecek JWT
     * @return Son kullanma tarihi
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Token'ın geçerliliğini ve kullanıcı bilgisiyle eşleşmesini doğrular.
     *
     * @param token İşlenecek JWT
     * @param user  Doğrulanacak kullanıcı nesnesi
     * @return Token geçerli ise true, aksi halde false
     */
    public Boolean validateToken(String token, User user) {
        final String email = extractEmail(token);
        return (email.equals(user.getEmail()) && !isTokenExpired(token));
    }
}
