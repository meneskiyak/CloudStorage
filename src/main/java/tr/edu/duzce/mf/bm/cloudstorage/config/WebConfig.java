package tr.edu.duzce.mf.bm.cloudstorage.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import tr.edu.duzce.mf.bm.cloudstorage.interceptor.JwtAuthInterceptor;
import tr.edu.duzce.mf.bm.cloudstorage.interceptor.LoggingInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"tr.edu.duzce.mf.bm.cloudstorage"})
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public InternalResourceViewResolver jspViewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/view/");
        viewResolver.setSuffix(".jsp");
        viewResolver.setContentType("text/html;charset=UTF-8");
        return viewResolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/resources/images/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
        List<MediaType> stringMediaTypes = new ArrayList<>();
        stringMediaTypes.add(new MediaType("text", "plain", StandardCharsets.UTF_8));
        stringMediaTypes.add(new MediaType("text", "html", StandardCharsets.UTF_8));
        stringMediaTypes.add(new MediaType("text", "javascript", StandardCharsets.UTF_8));
        stringConverter.setSupportedMediaTypes(stringMediaTypes);
        converters.add(stringConverter);

        converters.add(new MappingJackson2HttpMessageConverter());
    }

    // --- HOCANIN SLAYTLARINA BİREBİR UYGUN i18n AYARLARI ---

    // 1. Slayttaki "Örnek ResourceBundleMessageSource Tanımı"
    @Bean(name = "messageSource")
    public ReloadableResourceBundleMessageSource getMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages"); // src/main/resources altında aranacak
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(true);
        return messageSource;
    }

    // 2. Slayttaki "Session (Oturum) Locale Çözümleyicisi"
    @Bean
    public SessionLocaleResolver localeResolver(){
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        // Slaytta Locale.forLanguageTag("tr-TR") kullanılmış, aynısını yapıyoruz
        localeResolver.setDefaultLocale(Locale.forLanguageTag("tr-TR"));
        return localeResolver;
    }

    // 3. Slayttaki "Oturum Locale Dinleyicisi/Interceptor'ı"
    @Bean
    public LocaleChangeInterceptor localeInterceptor(){
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // URL'de ?lang=en_US gibi arayacak
        return interceptor;
    }

    // --- INTERCEPTOR KAYITLARI ---
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Dil değişimini izleyen interceptor'ı kaydediyoruz (Slayttaki gibi)
        registry.addInterceptor(localeInterceptor()).addPathPatterns("/*");

        // Aşama 3'te yazdığımız zorunlu Loglama Interceptor'ını kaydediyoruz
        registry.addInterceptor(new LoggingInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/resources/**"); // Statik dosyaları loglama

        // JWT tabanlı kimlik doğrulama interceptor'ını kaydediyoruz
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**") // Tüm yolları koru
                .excludePathPatterns("/login", "/register", "/resources/**"); // Giriş, kayıt ve statik dosyaları hariç tut
    }
}
