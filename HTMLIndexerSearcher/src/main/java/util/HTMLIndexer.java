package util;

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
        Directory dir = FSDirectory.open(Paths.get(indexPath)); // Crea un oggetto Directory per l'indice
        Analyzer analyzer = new StandardAnalyzer(); // Crea un analizzatore per l'indice
        IndexWriterConfig config = new IndexWriterConfig(analyzer); // Assegna l'analizzatore alla configurazione dell'IndexWriter

        // Imposta l'IndexWriter in modalità CREATE per sovrascrivere l'indice esistente
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        writer = new IndexWriter(dir, config);
    }

    public void indexHtmlFiles(String allHtmlsPath) throws IOException {
        // Metodo che accetta il percorso della directory con i file HTML da indicizzare
        File htmlDir = new File(allHtmlsPath);
        
        if (!htmlDir.exists()) {
            System.out.println("Directory does not exist: " + htmlDir.getAbsolutePath());
            return;
        }

        File[] htmlFiles = htmlDir.listFiles((dir, name) -> name.endsWith(".html"));
        
        if (htmlFiles == null || htmlFiles.length == 0) {
            System.out.println("No HTML files found in directory: " + htmlDir.getAbsolutePath());
            return;
        }
        
        System.out.println("Found " + htmlFiles.length + " HTML files in directory: " + htmlDir.getAbsolutePath());
        
        for (File htmlFile : htmlFiles) {
            if (htmlFile.canRead()) {
                System.out.println("Indexing file: " + htmlFile.getName());
                indexDocument(htmlFile);
            } else {
                System.out.println("Cannot read file: " + htmlFile.getName());
            }
        }
    }

    private void indexDocument(File htmlFile) throws IOException {
        // Metodo che accetta un file HTML e crea un documento Lucene per l'indice
        Document doc = new Document();
        org.jsoup.nodes.Document htmlDoc = Jsoup.parse(htmlFile, "UTF-8"); // Parse del file HTML con Jsoup
    
        String title = htmlDoc.title();  // Ottiene il titolo del documento HTML
        String author = findAuthor(htmlDoc); // Ottiene l'autore del documento HTML con il metodo `findAuthor`
        String content = htmlDoc.text(); // Ottiene il contenuto testuale del documento
        String abstractText = findAbstract(htmlDoc); // Ottiene l'abstract del documento HTML
    
        // Aggiungi i campi del documento al documento Lucene con analizzatori appropriati
        doc.add(new StringField("path", htmlFile.getPath(), Field.Store.YES));
        doc.add(new TextField("title", title, Field.Store.YES)); 
        doc.add(new TextField("content", content, Field.Store.YES)); 
        doc.add(new TextField("abstract", abstractText, Field.Store.YES)); 
        doc.add(new TextField("author", author, Field.Store.YES)); 
        // ... altri campi del documento
        doc.add(new StringField("relevant", "true", Field.Store.YES));  // Imposta rilevanza se appropriato

    
        writer.addDocument(doc);
    }

    // Metodo per trovare l'autore utilizzando selettori multipli e parole chiave
    private String findAuthor(org.jsoup.nodes.Document htmlDoc) {
        // Prova selettori specifici
        Element authorElement = htmlDoc.selectFirst("meta[name=author]");
        if (authorElement != null && authorElement.hasAttr("content")) {
            return truncateIfNeeded(authorElement.attr("content"));
        }
    
        authorElement = htmlDoc.selectFirst("span.ltx_personname");
        if (authorElement != null) {
            return truncateIfNeeded(authorElement.text().trim());
        }
    
        // Aggiungi ulteriori selettori, se necessario
        return "Unknown";
    }
    
    private String truncateIfNeeded(String value) {
        if (value != null && value.length() > 32000) {
            return value.substring(0, 32000); // Tronca se troppo lungo
        }
        return value;
    }
    
    

    // Metodo per estrarre l'abstract
    private String findAbstract(org.jsoup.nodes.Document htmlDoc) {
        // Prima cerca un meta tag che potrebbe contenere l'abstract
        Element abstractElement = htmlDoc.selectFirst("meta[name=description], meta[name=abstract]");
        if (abstractElement != null && abstractElement.hasAttr("content")) {
            return abstractElement.attr("content");
        }

        // Cerca l'abstract all'interno di un elemento specifico del corpo del documento
        abstractElement = htmlDoc.selectFirst("div.abstract, p.abstract, section.abstract, div.ltx_abstract, p.ltx_abstract, section.ltx_abstract");
        if (abstractElement != null) {
            return abstractElement.text();
     }  

        return "No abstract available"; // Nessun abstract trovato
    }


    public void close() throws IOException {
        writer.close();
        System.out.println("IndexWriter closed.");
    }

    public static void main(String[] args) {
        try {
            String indexPath = "indexDir"; // Specifica il percorso per l'indice
            String allHtmlsPath = "all_htmls"; // Specifica il percorso della directory con i file HTML
            HTMLIndexer indexer = new HTMLIndexer(indexPath);
            indexer.indexHtmlFiles(allHtmlsPath);
            indexer.close();
            System.out.println("Indexing complete.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}