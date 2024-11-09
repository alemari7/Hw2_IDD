package util;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class HTMLSearcher {
    private IndexSearcher searcher;
    private QueryParser titleParser;
    private QueryParser abstractParser;
    private QueryParser authorParser;

    public HTMLSearcher(String indexPath) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        IndexReader reader = DirectoryReader.open(dir);
        searcher = new IndexSearcher(reader);

        // Analizzatore personalizzato per campi case-insensitive con separatori di parole
        Analyzer customAnalyzer = CustomAnalyzer.builder()
            .withTokenizer(WhitespaceTokenizerFactory.class)
            .addTokenFilter(LowerCaseFilterFactory.class)
            .addTokenFilter(WordDelimiterGraphFilterFactory.class)
            .build();

        // Analizzatore specifico per il campo "author" con LowerCaseFilter
        Analyzer authorAnalyzer = CustomAnalyzer.builder()
            .withTokenizer(WhitespaceTokenizerFactory.class)
            .addTokenFilter(LowerCaseFilterFactory.class)
            .build();

        // Creazione degli analizzatori specifici per i campi
        titleParser = new QueryParser("title", customAnalyzer);
        abstractParser = new QueryParser("abstract", new StandardAnalyzer());
        authorParser = new QueryParser("author", authorAnalyzer); // Usa il custom analyzer per gli autori
    }

    public void search(String queryStr) throws Exception {
        // Prepariamo una query booleana
        BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

        // Analizziamo la query e aggiungiamo le condizioni per ogni campo
        String[] terms = queryStr.split(",");
        for (String term : terms) {
            String[] parts = term.split(":", 2);
            if (parts.length == 2) {
                String field = parts[0].trim();
                String fieldQuery = parts[1].trim();

                Query query = null;

                switch (field) {
                    case "title":
                        query = titleParser.parse(fieldQuery);
                        break;
                    case "abstract":
                        query = abstractParser.parse(fieldQuery);
                        break;
                    case "author":
                        query = authorParser.parse(fieldQuery);
                        break;
                    default:
                        System.out.println("Campo non valido. I campi validi sono: title, content, abstract, author.");
                        return;
                }

                // Aggiungiamo la query al boolean query (AND per default)
                booleanQueryBuilder.add(query, BooleanClause.Occur.MUST);
            } else {
                System.out.println("Formato della query non valido. Usa 'title:term', 'content:\"phrase query\"', 'abstract:term', o 'author:term'.");
                return;
            }
        }

        // Esegui la ricerca e mostra i risultati
        BooleanQuery finalQuery = booleanQueryBuilder.build();
        TopDocs results = searcher.search(finalQuery, 10); // Limita i risultati a 10
        System.out.println("Trovati " + results.totalHits.value() + " documenti.");

        if (results.totalHits.value() > 0) {
            for (ScoreDoc hit : results.scoreDocs) {
                Document doc = searcher.getIndexReader().storedFields().document(hit.doc);
                System.out.println("Path: " + doc.get("path"));
                System.out.println("Title: " + doc.get("title"));
                System.out.println("Author: " + doc.get("author"));
                System.out.println("Abstract: " + doc.get("abstract"));
                String content = doc.get("content");

                // Rimuovi il markup HTML dal contenuto
                if (content != null) {
                    // Usa Jsoup per estrarre solo il testo, senza tag HTML
                    content = Jsoup.parse(content).text();
                }
                System.out.println("Content: " + content); // Solo il testo pulito
                System.out.println("Score: " + hit.score);
                System.out.println("------------");
            }
        } else {
            System.out.println("Nessun documento trovato per la query.");
        }
    }

    public void close() throws IOException {
        searcher.getIndexReader().close();
    }

    public static void main(String[] args) {
        try {
            String indexPath = "indexDir"; // Percorso dell'indice
            HTMLSearcher searcher = new HTMLSearcher(indexPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Inserisci la tua query (es. 'title:term', 'author:\"nome cognome\"', 'abstract:term').");

            String line;
            while (!(line = br.readLine()).equalsIgnoreCase("exit")) {
                searcher.search(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
