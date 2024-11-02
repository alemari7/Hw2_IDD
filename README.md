# Hw2_IDD

## HTMLSearcher-Funzionamento
1. Inizializzazione: Quando avvii il programma, viene creato un oggetto *HTMLSearcher* con il percorso dell'indice.
2. Input dell'Utente: Il programma attende l'input dell'utente per la query di ricerca.
3. Esecuzione della Ricerca: Quando l'utente inserisce una query valida, viene eseguita la ricerca e vengono stampati i risultati.
4. Visualizzazione dei Risultati: Il programma mostra il percorso del documento, il titolo, l'autore e il punteggio di rilevanza.
 5. Uscita: Il programma continua a ricevere query finché l'utente non decide di uscire digitando "exit".

 ## HTMLIndexer-Funzionamento
 1. Creazione della Classe *HTMLIndexer*: Questa classe gestisce l'intero processo di indicizzazione. Ha un campo writer di tipo IndexWriter, che è l'oggetto principale per scrivere documenti nell'indice.
 2. Costruttore della Classe: Il costruttore accetta un percorso per l'indice (dove verranno memorizzati i dati) e crea un *IndexWriter* che utilizza un analizzatore standard (*StandardAnalyzer*). Questo analizzatore aiuta a segmentare il testo in termini utili per la ricerca.
 3. Indicizzazione di File HTML: Il metodo *indexHtmlFiles* si occupa di trovare tutti i file HTML in una directory specificata. Utilizza un filtro per selezionare solo i file con estensione .html.
4. Estrazione dei Dati dal File HTML:
 Nel metodo *indexDocument*, viene utilizzato Jsoup per analizzare il file HTML. Jsoup è una libreria Java per il parsing di HTML e permette di navigare e manipolare il documento come un albero di nodi.
Vengono estratti il titolo del documento, il nome dell'autore (se presente) e il contenuto testuale. Dati alla fine memorizzati in un oggetto *Document*.
5. Scrittura nell' Indice: Una volta che il documento è stato popolato con le informazioni estratte, viene aggiunto all'indice tramite *writer.addDocument(doc)*.
6. Chiusura dell'Indice: Dopo aver indicizzato tutti i file, il metodo close viene chiamato per chiudere l'*IndexWriter*. Questo è importante per liberare risorse e garantire che tutte le scritture vengano completate.