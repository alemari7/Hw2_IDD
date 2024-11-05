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
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(dir, config);
    }

    public void indexHtmlFiles(String allHtmlsPath) throws IOException {
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
        Document doc = new Document();
        org.jsoup.nodes.Document htmlDoc = Jsoup.parse(htmlFile, "UTF-8");
        
        String title = htmlDoc.title();
        Element authorElement = htmlDoc.selectFirst("meta[name=author]");
        String author = authorElement != null ? authorElement.attr("content") : "Unknown";
        String content = htmlDoc.text();
        
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
