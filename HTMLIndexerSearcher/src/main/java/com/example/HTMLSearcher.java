package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class HTMLSearcher {
    private IndexSearcher searcher; // per cercare i documenti nell'indice
    private QueryParser titleParser; // per analizzare le query per il campo "title"
    private QueryParser contentParser; // per analizzare le query per il campo "content"
    private QueryParser abstractParser; // per analizzare le query per il campo "abstract"
    private QueryParser authorParser; // per analizzare le query per il campo "author"

    public HTMLSearcher(String indexPath) throws IOException {
    Directory dir = FSDirectory.open(Paths.get(indexPath));
    IndexReader reader = DirectoryReader.open(dir); // creo un IndexReader per leggere l'indice
    searcher = new IndexSearcher(reader); // per eseguire le ricerche

    // Creazione degli analizzatori specifici per i campi
    titleParser = new QueryParser("title", new StandardAnalyzer()); // Analizzatore per il campo "title"
    contentParser = new QueryParser("content", new EnglishAnalyzer()); // Analizzatore per il campo "content"
    abstractParser = new QueryParser("abstract", new EnglishAnalyzer()); // Analizzatore per il campo "abstract"
    authorParser = new QueryParser("author", new KeywordAnalyzer()); // Analizzatore per il campo "author"
}


public void search(String field, String queryStr) throws Exception {
    // Seleziona il parser appropriato in base al campo specificato
    Query query = null;

    // Rimuovi eventuali spazi extra e gestisci la ricerca in modo case-insensitive
    queryStr = queryStr.trim().toLowerCase();

    switch (field) {
        case "title":
            query = titleParser.parse(queryStr);
            break;
        case "content":
            query = contentParser.parse(queryStr);
            break;
        case "abstract":
            // Usa QueryParser per l'abstract con analizzatore per l'inglese
            query = new QueryParser("abstract", new EnglishAnalyzer()).parse(queryStr);
            break;
        case "author":
            // Usa QueryParser per l'autore, ma questa volta senza modificare la stringa in minuscolo
            // Questo è importante per una ricerca case-insensitive, se necessario
            query = new WildcardQuery(new Term("author", queryStr + "*"));
            break;
        
        default:
            System.out.println("Invalid field. Valid fields are: title, content, abstract, author.");
            return;
    }

    // Esegui la ricerca
    TopDocs results = searcher.search(query, 5);
    System.out.println("Found " + results.totalHits.value() + " documents.");

    if (results.totalHits.value() > 0) {
        for (ScoreDoc hit : results.scoreDocs) {
            Document doc = searcher.getIndexReader().storedFields().document(hit.doc);
            if (doc != null) {
                System.out.println("Path: " + doc.get("path"));
                System.out.println("Title: " + doc.get("title"));
                System.out.println("Author: " + doc.get("author"));
                System.out.println("Abstract: " + doc.get("abstract"));
                System.out.println("Content: " + doc.get("content"));
                System.out.println("Score: " + hit.score);
                System.out.println("------------");
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
                    System.out.println("Invalid query format. Use 'title:term', 'content:\"phrase query\"', 'abstract:term', or 'author:term'.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
