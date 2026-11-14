package com.bachillerato.buscaminas.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "partida")
public class Partida {

    public Partida() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "nivel", nullable = false, length = 20)
    private String nivel;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPartida estado = EstadoPartida.EN_CURSO;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "movimientos", nullable = false)
    private Integer movimientos = 0;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "estrellas", nullable = false)
    private Short estrellas = 0;

    @JdbcTypeCode(SqlTypes.INTEGER)
    @Column(name = "tiempo_ms")
    private Long tiempoMs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tablero", nullable = false, columnDefinition = "json")
    private String tablero;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "fecha_inicio", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public Integer getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(Integer movimientos) {
        this.movimientos = movimientos;
    }

    public Short getEstrellas() {
        return estrellas;
    }

    public void setEstrellas(Short estrellas) {
        this.estrellas = estrellas;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public void setEstado(EstadoPartida estado) {
        this.estado = estado;
    }

    public Long getTiempoMs() {
        return tiempoMs;
    }

    public void setTiempoMs(Long tiempoMs) {
        this.tiempoMs = tiempoMs;
    }

    public String getTablero() {
        return tablero;
    }

    public void setTablero(String tablero) {
        this.tablero = tablero;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }
}