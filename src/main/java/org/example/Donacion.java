package org.example;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.time.LocalDate;

/**
 * Representa una donación de sangre dentro del sistema del banco de sangre.
 * Gestiona la información principal del donante, los mililitros recolectados,
 * su grupo sanguíneo, la fecha de caducidad y la persistencia directa en MongoDB Atlas.
 */
public class Donacion {
    private String nombreDonante;
    private int volumenMl;
    private String grupoSanguineo;
    private LocalDate fechaCaducidad;

    /**
     * Constructor principal para inicializar una nueva instancia de Donación
     * con todos sus atributos requeridos en el orden establecido.
     */
    public Donacion(String nombreDonante, int volumenMl, String grupoSanguineo, LocalDate fechaCaducidad) {
        this.nombreDonante = nombreDonante;
        this.volumenMl = volumenMl;
        this.grupoSanguineo = grupoSanguineo;
        this.fechaCaducidad = fechaCaducidad;
    }

    public String getNombreDonante() { return nombreDonante; }
    public int getVolumenMl() { return volumenMl; }
    public String getGrupoSanguineo() { return grupoSanguineo; }
    public LocalDate getFechaCaducidad() { return fechaCaducidad; }

    /**
     * Establece la conexión con el clúster de MongoDB Atlas y almacena 
     * el registro actual de la donación en la base de datos y colección correspondientes.
     */
    public void guardarEnMongo() {
        String uri = "mongodb://rdominguez2024_db_user:Jose123@ac-15adclh-shard-00-00.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-01.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-02.ddnbvei.mongodb.net:27017/?ssl=true&replicaSet=atlas-t74n7s-shard-0&authSource=admin";
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("banco_sangre");
            MongoCollection<Document> collection = database.getCollection("donaciones");

            Document doc = new Document("nombreDonante", this.nombreDonante)
                            .append("volumenMl", this.volumenMl)
                            .append("grupoSanguineo", this.grupoSanguineo)
                            .append("fechaCaducidad", this.fechaCaducidad.toString());

            collection.insertOne(doc);
            System.out.println("¡Donación guardada en MongoDB Atlas con éxito!");

        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con MongoDB: " + e.getMessage(), e);
        }
    }
}