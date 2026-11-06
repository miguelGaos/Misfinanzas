package com.example.misfinanzas.modelo;

import com.google.firebase.firestore.DocumentId;

public class Transaccion {
    @DocumentId
    private String id;
    private String concepto;
    private double monto;
    private String tipo;

    private double latitud;
    private double longitud;
    private String direccion;

    private String userId;

    public Transaccion() {}

    public Transaccion(String concepto, double monto, String tipo, double latitud, double longitud, String direccion, String userId) {
        this.concepto = concepto;
        this.monto = monto;
        this.tipo = tipo;
        this.latitud = latitud;
        this.longitud = longitud;
        this.direccion = direccion;
        this.userId = userId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}