package org.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Inventario {

    private static final String URI = "mongodb://rdominguez2024_db_user:Jose123@ac-15adclh-shard-00-00.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-01.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-02.ddnbvei.mongodb.net:27017/?ssl=true&replicaSet=atlas-t74n7s-shard-0&authSource=admin";
    private static final String DATABASE_NAME = "banco_sangre";
    private static final String COLLECTION_NAME = "donaciones";
    
     // metodo para obtener el inventario desde mongo atlas
    // mapea los documentos a objetos de tipo Donacion
    public List<Donacion> obtenerInventario() {
        List<Donacion> inventario = new ArrayList<>();

        try (MongoClient mongoClient = MongoClients.create(URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            for (Document doc : collection.find()) {
                String nombre = doc.getString("nombreDonante");
                int volumen = doc.getInteger("volumenMl");
                String grupoSanguineo = doc.getString("grupoSanguineo");
                LocalDate fecha = LocalDate.parse(doc.getString("fechaCaducidad"));
                
                Donacion donacion = new Donacion(nombre, volumen, grupoSanguineo, fecha);
                inventario.add(donacion);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return inventario;
    }
}