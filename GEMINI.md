# Proje Bağlamı ve Yapay Zeka Talimatları (BM470 Cloud Storage)

## 1. Proje Özeti
Bu proje, kullanıcıların dosya/klasör yükleyebildiği, kotalarının olduğu ve adminlerin kotaları yönetebildiği bir "Bulut Depolama" (Cloud Storage) web uygulamasıdır.

## 2. Teknoloji Yığını (Tech Stack)
* **Dil:** Java 18
* **Framework:** Spring MVC 6 (Spring Boot KESİNLİKLE KULLANILMAYACAK. Saf Spring MVC).
* **ORM:** Hibernate 6+ (Criteria API aktif kullanılıyor).
* **Arayüz:** JSP (JavaServer Pages), JSTL, HTML/CSS.
* **Bağımlılık Yönetimi:** Maven (`pom.xml`).

## 3.Mimaride Uygulanan Bazı Kurallar
Projede birebir uygulanan mimari kurallar şunlardır:

* **Loglama Mimarisi:** Spring'in varsayılan loglaması yerine **SLF4J** ve **log4j.properties** kullanılmaktadır. `src/main/resources` altındaki özellikler dosyasından okunur. Log nesneleri `LoggerFactory.getLogger(...)` ile oluşturulur.
* **Çoklu Dil (i18n):** `WebConfig` içerisinde `ReloadableResourceBundleMessageSource`, `SessionLocaleResolver` ve `LocaleChangeInterceptor` (parametre adı: `lang`) standartları ile yapılandırılmıştır.
* **DAO (Veri Erişim) Katmanı:** Spring Data JPA (`@Repository` interface'leri) KULLANILMAYACAK. Geleneksel DAO sınıfları yazılıp, veritabanı işlemleri `getSession().persist()`, `getSession().remove()`, `getSession().get()` ve karmaşık sorgular için **Criteria API** ile yapılmaktadır.
* **Service Katmanı:** Veritabanını değiştiren (insert, update, delete, soft delete) tüm metotlar kesinlikle `@Transactional` anotasyonu ile işaretlenmelidir. İş mantığı (kota kontrolü vb.) tamamen burada yapılır.

## 4. Test Kuralları
* **Kütüphaneler:** JUnit 5 (Jupiter), Mockito (`mockito-junit-jupiter`), Spring Test.
* **Service Testleri:** Test sınıfları `@ExtendWith(MockitoExtension.class)` ile işaretlenir. DAO katmanı `@Mock`, Service katmanı `@InjectMocks` ile izole edilir.
* **Controller Testleri:** KESİNLİKLE `MockMvc` kullanılarak HTTP durum kodları ve yönlendirmeler test edilir. `MockMvcBuilders.standaloneSetup(controller).build()` yapısı kullanılır.

## 5. Dil ve İsimlendirme
Kod yazılırken değişkenler, sınıf isimleri ve metodlar İngilizce yazılmalıdır (örn: `User`, `uploadFile`). Ancak kullanıcıya gösterilecek hata/başarı mesajları (Exception fırlatmalar dahil) ve JavaDoc yorum satırları Türkçe olmalıdır.

## 6. Arayüz (View / JSP) Kuralları 
* **Şablon Motoru:** Kesinlikle Thymeleaf, React, Vue vb. KULLANILMAYACAK. Sadece **JSP (JavaServer Pages)** kullanılacaktır.
* **JSTL ve Jakarta Sürümü (ÇOK ÖNEMLİ):** Projede Tomcat 10+ kullanıldığı için JSP taglib tanımlarında `javax` YERİNE KESİNLİKLE `jakarta` kullanılacaktır.
  *(Doğru Kullanım: `<%@ taglib prefix="c" uri="jakarta.tags.core" %>`)*
* **Scriptlet Yasağı:** JSP sayfaları içinde spagetti Java kodu (Scriptlet `<% ... %>`) yazılmayacaktır. Döngüler ve şartlar için sadece JSTL etiketleri (`<c:if>`, `<c:forEach>`) ve Expression Language (`${...}`) kullanılacaktır.
* **Çoklu Dil (i18n):** Buton isimleri, başlıklar ve menü metinleri doğrudan HTML içine "sabit (hardcoded)" yazılmayacak; WebConfig'de tanımladığımız i18n yapısına uygun olarak `<spring:message code="..."/>` etiketi ile kullanılacaktır.