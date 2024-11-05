package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class HTMLSearcher {
    private IndexSearcher searcher; // per cercare i documenti nell'indice
    private QueryParser titleParser; // per analizzare le query per il campo "title"
    private QueryParser contentParser; // per analizzare le query per il campo "content"

    public HTMLSearcher(String indexPath) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        IndexReader reader = DirectoryReader.open(dir); // creo un IndexReader per leggere l'indice
        searcher = new IndexSearcher(reader); // per eseguire le ricerche
        titleParser = new QueryParser("title", new StandardAnalyzer()); // analizzatore per il campo "title"
        contentParser = new QueryParser("content", new StandardAnalyzer());
    }

    public void search(String field, String queryStr) throws Exception {
        /* accetta due parametri: il campo da cercare e la stringa di query
           seleziona il parser appropriato in base al campo specificato e analizza la query
           esegue la ricerca e stampa i risultati */
        QueryParser parser = field.equals("title") ? titleParser : contentParser;
        Query query = parser.parse(queryStr);
        TopDocs results = searcher.search(query, 49);
        
        // Accesso corretto al totale dei risultati
        System.out.println("Found " + results.totalHits + " documents."); // utilizza .value

        // Verifica se ci sono risultati
        if (results.totalHits.value() > 0) {
            for (ScoreDoc hit : results.scoreDocs) {
                // Usa searcher.doc() per ottenere il documento
                Document doc = searcher.getIndexReader().storedFields().document(hit.doc);
                if (doc != null) { // Controlla se il documento è valido
                    System.out.println("Path: " + doc.get("path"));
                    System.out.println("Title: " + doc.get("title"));
                    System.out.println("Author: " + doc.get("author"));
                    System.out.println("Score: " + hit.score);
                    System.out.println("------------");
                } else {
                    System.out.println("Document not found for hit: " + hit.doc);
                }
            }
        } else {
            System.out.println("No documents found for the query.");
        }
    }

    public static void main(String[] args) {
        try {
            String indexPath = "indexDir"; // Percorso dell'indice
            HTMLSearcher searcher = new HTMLSearcher(indexPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); // per leggere l'input dell'utente
            System.out.println("Enter your query (e.g., 'title:term' or 'content:\"phrase query\"'):"); // formato della query
            String line;
            while (!(line = br.readLine()).equalsIgnoreCase("exit")) { // fino a quando l'utente non digita "exit"
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    String field = parts[0].trim();
                    String queryStr = parts[1].trim();
                    searcher.search(field, queryStr);
                } else {
                    System.out.println("Invalid query format. Use 'title:term' or 'content:\"phrase query\"'.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}