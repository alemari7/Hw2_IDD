package com.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class HTMLIndexer {

    private IndexWriter writer;

    public HTMLIndexer(String indexPath) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexPath)); //crea un oggetto Directory per l'indice
        Analyzer analyzer = new StandardAnalyzer(); //crea un analizzatore per l'indice
        IndexWriterConfig config = new IndexWriterConfig(analyzer); //assegna l'analizzatore alla configurazione dell'IndexWriter

        // Imposta l'IndexWriter in modalità CREATE per sovrascrivere l'indice esistente
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        writer = new IndexWriter(dir, config);
    }

    public void indexHtmlFiles(String allHtmlsPath) throws IOException {
        //Questo metodo accetta il percorso della directory che contiene i file HTML da indicizzare
        File htmlDir = new File(allHtmlsPath);
        
        if (!htmlDir.exists()) { //se la directory non esiste, stampa un messaggio e ritorna
            System.out.println("Directory does not exist: " + htmlDir.getAbsolutePath());
            return;
        }
        //sennò, cerca tutti i file HTML nella directory
        File[] htmlFiles = htmlDir.listFiles((dir, name) -> name.endsWith(".html"));
        
        if (htmlFiles == null || htmlFiles.length == 0) { //se non ci sono file HTML, stampa un messaggio e ritorna
            System.out.println("No HTML files found in directory: " + htmlDir.getAbsolutePath());
            return;
        }
         //sennò, stampa il numero di file HTML trovati
        System.out.println("Found " + htmlFiles.length + " HTML files in directory: " + htmlDir.getAbsolutePath());
        
        for (File htmlFile : htmlFiles) { //per ogni file HTML trovato, controlla se è leggibile
            if (htmlFile.canRead()) { //se è leggibile, stampa un messaggio e indica il file
                System.out.println("Indexing file: " + htmlFile.getName());
                indexDocument(htmlFile);
            } else {
                System.out.println("Cannot read file: " + htmlFile.getName());
            }
        }
    }

    private void indexDocument(File htmlFile) throws IOException {
        //questa funzione accetta un file HTML e crea un documento Lucene per l'indice
        Document doc = new Document();
        org.jsoup.nodes.Document htmlDoc = Jsoup.parse(htmlFile, "UTF-8"); //parsa il file HTML con Jsoup
        
        String title = htmlDoc.title();  //ottiene il titolo del documento HTML
        Element authorElement = htmlDoc.selectFirst("meta[name='authors']"); //ottiene l'autore del documento HTML
        String author = authorElement != null ? authorElement.attr("content") : "Unknown"; //se l'autore non è presente, imposta "Unknown"
        String content = htmlDoc.text();

        //aggiunge i campi del documento al documento Lucene
        doc.add(new StringField("path", htmlFile.getPath(), Field.Store.YES));
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("author", author, Field.Store.YES));
        doc.add(new TextField("content", content, Field.Store.YES));
        
        writer.addDocument(doc);
    }

    public void close() throws IOException {
        writer.close();
        System.out.println("IndexWriter closed.");
    }

    public static void main(String[] args) {
        try {
            String indexPath = "indexDir";
            String allHtmlsPath = "all_htmls";  // Specifica il percorso corretto della directory HTML
            HTMLIndexer indexer = new HTMLIndexer(indexPath);
            indexer.indexHtmlFiles(allHtmlsPath);
            indexer.close();
            System.out.println("Indexing complete.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
