package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.Set;

public class SolicitudEntrega {
    public static final Set<String> TIPOS_SANGUINEOS_VALIDOS = Set.of(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    );

    private static final String URI = "mongodb://rdominguez2024_db_user:Jose123@ac-15adclh-shard-00-00.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-01.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-02.ddnbvei.mongodb.net:27017/?ssl=true&replicaSet=atlas-t74n7s-shard-0&authSource=admin";
    private static final String DATABASE_NAME = "banco_sangre";
    private static final String COLLECTION_NAME = "solicitudes_entrega";

    private String tipoSangre;
    private int volumenRequeridoMl;
    private LocalDateTime fechaSolicitud;
    private String estado;

    public SolicitudEntrega(String tipoSangre, int volumenRequeridoMl) {
        this.tipoSangre = tipoSangre;
        this.volumenRequeridoMl = volumenRequeridoMl;
        this.fechaSolicitud = LocalDateTime.now();
        this.estado = "PENDIENTE";
    }

    public SolicitudEntrega(String tipoSangre, int volumenRequeridoMl, LocalDateTime fechaSolicitud, String estado) {
        this.tipoSangre = tipoSangre;
        this.volumenRequeridoMl = volumenRequeridoMl;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
    }

    public Document toDocument() {
        return new Document("tipoSangre", this.tipoSangre)
                .append("volumenRequeridoMl", this.volumenRequeridoMl)
                .append("fechaSolicitud", this.fechaSolicitud.toString())
                .append("estado", this.estado);
    }

    public void guardarEnMongo() {
        try (MongoClient mongoClient = MongoClients.create(URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
            collection.insertOne(toDocument());
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar solicitud de entrega en MongoDB: " + e.getMessage(), e);
        }
    }

    public String getTipoSangre() {
        return tipoSangre;
    }

    public int getVolumenRequeridoMl() {
        return volumenRequeridoMl;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
