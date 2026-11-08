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
        String uri = "mongodb://rdominguez2024_db_user:VU5eakJwxXqaDJww@grupo6-shard-00-00.ddnbvei.mongodb.net:27017,grupo6-shard-00-01.ddnbvei.mongodb.net:27017,grupo6-shard-00-02.ddnbvei.mongodb.net:27017/?ssl=true&replicaSet=atlas-ddnbvei-shard-0&authSource=admin&retryWrites=true&w=majority";
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