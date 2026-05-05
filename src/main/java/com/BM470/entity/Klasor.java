package com.BM470.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "klasor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Klasor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "klasor_adi", nullable = false)
    private String klasorAdi;

    // Klasörün sahibi olan kullanıcı
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sahip_id", nullable = false)
    private Kullanici sahip;

    // Hiyerarşi: Bu klasör hangi klasörün içinde? (Ana dizindeyse null olur)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ust_klasor_id")
    private Klasor ustKlasor;

    // Bu klasörün içindeki alt klasörler
    @OneToMany(mappedBy = "ustKlasor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Klasor> altKlasorler;

    // Bu klasörün içindeki dosyalar
    @OneToMany(mappedBy = "klasor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dosya> dosyalar;
}