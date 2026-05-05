package com.BM470.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "dosya")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dosya implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "orijinal_ad", nullable = false)
    private String orijinalAd; // Örn: fatura.pdf

    @Column(name = "uzanti", nullable = false)
    private String uzanti; // Örn: pdf, jpg, png (Önizleme ayrımı için)

    @Column(name = "boyut", nullable = false)
    private Long boyut; // Kota hesaplaması için

    @Column(name = "sunucu_yolu", nullable = false)
    private String sunucuYolu; // Dosyanın diske kaydedildiği asıl konum

    // OCR / Yapay Zeka ile okunup kaydedilecek uzun metin (İçerik bazlı arama için)
    @Column(name = "yapay_zeka_icerik", columnDefinition = "TEXT")
    private String yapayZekaIcerik;

    // Dosyanın sahibi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sahip_id", nullable = false)
    private Kullanici sahip;

    // Dosyanın bulunduğu klasör (Null ise ana dizindedir)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "klasor_id")
    private Klasor klasor;
}