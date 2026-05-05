package com.BM470.web;

import com.BM470.dao.KullaniciDAO;
import com.BM470.entity.Kullanici;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private KullaniciDAO kullaniciDAO;

    @GetMapping("/test")
    public String testVeritabani() {
        try {
            // Sisteme sahte bir admin kullanıcısı oluşturuyoruz
            Kullanici admin = new Kullanici();
            admin.setEmail("admin@cloud.com");
            admin.setSifre("12345");
            admin.setRol("ADMIN");
            admin.setKotaBoyutu(1073741824L); // 1 GB kota (byte)
            admin.setKullanilanAlan(0L);

            // DAO üzerinden veritabanına kaydetme emri veriyoruz!
            kullaniciDAO.kaydet(admin);

            return "Harika! Veritabanına kayıt başarılı. Tablolar kesinlikle oluştu! Lütfen IntelliJ Database panelini yenileyin.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Hata oluştu: " + e.getMessage();
        }
    }
}