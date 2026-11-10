package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class Inventario {

    public List<Document> obtenerInventario() {
        List<Document> listaInventario = new ArrayList<>();
        
        // Conexión a MongoDB Atlas
        try (MongoClient mongoClient = MongoClients.create("mongodb://rdominguez2024_db_user:Jose123@ac-15adclh-shard-00-00.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-01.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-02.ddnbvei.mongodb.net:27017/?ssl=true&replicaSet=atlas-t74n7s-shard-0&authSource=admin")) {
            MongoDatabase database = mongoClient.getDatabase("banco_sangre");
            MongoCollection<Document> coleccion = database.getCollection("donaciones");
            
            try (MongoCursor<Document> cursor = coleccion.find().iterator()) {
                while (cursor.hasNext()) {
                    listaInventario.add(cursor.next());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return listaInventario;
    }

    public static void main(String[] args) {
        Inventario inv = new Inventario();
        List<Document> resultados = inv.obtenerInventario();
        
        System.out.println("--- RESULTADOS OBTENIDOS DE MONGODB ---");
        for (Document doc : resultados) {
            System.out.println(doc.toJson());
        }
    }
}