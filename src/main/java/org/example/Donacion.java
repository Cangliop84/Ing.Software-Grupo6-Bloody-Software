package org.example;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.time.LocalDate;

public class Donacion {
    private String nombreDonante;
    private int volumenMl;
    private LocalDate fechaCaducidad;

    public Donacion(String nombreDonante, int volumenMl, LocalDate fechaCaducidad) {
        this.nombreDonante = nombreDonante;
        this.volumenMl = volumenMl;
        this.fechaCaducidad = fechaCaducidad;
    }

    public String getNombreDonante() { return nombreDonante; }
    public int getVolumenMl() { return volumenMl; }
    public LocalDate getFechaCaducidad() { return fechaCaducidad; }

    //Metodo para guardar en MongoDB Atlas
    public void guardarEnMongo() {
        String uri = "mongodb+srv://rdominguez2024_db_user:VU5eakjWxXqaDJww@grupo6.ddnbvei.mongodb.net/?compressors=zlib";

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("banco_sangre");
            MongoCollection<Document> collection = database.getCollection("donaciones");

            Document doc = new Document("nombreDonante", this.nombreDonante)
                            .append("volumenMl", this.volumenMl)
                            .append("fechaCaducidad", this.fechaCaducidad.toString());

            collection.insertOne(doc);
            System.out.println("¡Donación guardada en MongoDB Atlas con éxito!");
            
        } catch (Exception e) {
            System.out.println("Error al conectar con MongoDB: " + e.getMessage());
        }
    }
}