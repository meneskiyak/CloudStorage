package com.BM470.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "kullanici")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Kullanici implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "sifre", nullable = false)
    private String sifre;

    @Column(name = "rol", nullable = false)
    private String rol; // "ADMIN" veya "USER"

    @Column(name = "kota_boyutu", nullable = false)
    private Long kotaBoyutu; // Verilecek upload limiti (byte)

    @Column(name = "kullanilan_alan", nullable = false)
    private Long kullanilanAlan = 0L;

    // Bir kullanıcının birden çok klasörü ve dosyası olabilir
    @OneToMany(mappedBy = "sahip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Klasor> klasorler;

    @OneToMany(mappedBy = "sahip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dosya> dosyalar;
}