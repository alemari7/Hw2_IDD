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

    private IndexWriter writer;  //utilizzato per scrivere i documenti nell'indice

    public HTMLIndexer(String indexPath) throws IOException {
        /*Qui viene aperta una directory utilizzando il percorso specificato, e 
        viene creato un Analyzer standard per analizzare il testo. Successivamente, si configura un IndexWriter 
        con la directory e l'analizzatore. */
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(dir, config);
    }

    public void indexHtmlFiles(String all_htmls) throws IOException {
        /*prende un percorso di directory contenente file HTML e recupera tutti i file .html. 
        Se ci sono file nella directory,
        per ciascun file viene chiamato il metodo indexDocument */
        File htmlDir = new File(all_htmls);
        File[] htmlFiles = htmlDir.listFiles((dir, name) -> name.endsWith(".html"));
        if (htmlFiles != null) {
            for (File htmlFile : htmlFiles) {
                indexDocument(htmlFile);
            }
        }
    }

    private void indexDocument(File htmlFile) throws IOException {
        /*il file HTML viene analizzato utilizzando Jsoup. 
        Viene creato un nuovo Document Lucene per contenere i dati estratti. 
        Si estrae il titolo, l'autore (da un meta tag) e il contenuto testuale del file HTML. 
        I dati vengono quindi aggiunti come campi nel documento */
        Document doc = new Document();
        
        // Parsing HTML file con Jsoup
        org.jsoup.nodes.Document htmlDoc = Jsoup.parse(htmlFile, "UTF-8");
        
        // Estrazione titolo, autore e contenuto
        String title = htmlDoc.title();
        Element authorElement = htmlDoc.selectFirst("meta[name=author]");
        String author = authorElement != null ? authorElement.attr("content") : "Unknown";
        String content = htmlDoc.text();
        
        // Aggiungo i campi al documento
        doc.add(new StringField("path", htmlFile.getPath(), Field.Store.YES));
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("author", author, Field.Store.YES));
        doc.add(new TextField("content", content, Field.Store.YES));
        
        writer.addDocument(doc);
    }

    public void close() throws IOException { //per chiudere l'IndexWriter
        writer.close();
    }

    public static void main(String[] args) {
        /*Qui si specificano il percorso dell'indice e il percorso della directory
         contenente i file HTML. 
         Viene creato un oggetto HTMLIndexer, che indicizza i file HTML
         nella directory specificata e chiude l'indice 
         al termine dell'operazione, 
         stampando un messaggio di completamento. */
        try {
            String indexPath = "indexDir";
            String all_htmls = "all_htmls";
            HTMLIndexer indexer = new HTMLIndexer(indexPath);
            indexer.indexHtmlFiles(all_htmls);
            indexer.close();
            System.out.println("Indexing complete.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
