# MuteBox

**Silenzio per tutto, tranne per chi conta davvero.**

MuteBox è un'app Android open source che mette il telefono in "Non disturbare" e lascia suonare
**solo le app, le persone e i gruppi che scegli tu**. Ideale per la notte, lo studio o il lavoro:
il telefono resta muto, ma se ti scrive la mamma, il partner o il gruppo "Famiglia", lo senti.

> ⚠️ **Avviso importante**: MuteBox è un progetto amatoriale, gratuito e fornito "così com'è", senza garanzie.
> Android o il produttore del telefono possono chiuderla o limitarla. **Non usarla per avvisi da cui
> dipendono salute, sicurezza o emergenze.** Leggi la sezione [Esclusione di responsabilità](#esclusione-di-responsabilità).

## Scarica e installa

1. Dal telefono apri l'ultima versione nella pagina [Releases](https://github.com/makaDv/MuteBox/releases/latest).
2. Scarica il file `MuteBox-vX.Y.apk`.
3. Aprilo e, se Android lo chiede, consenti l'installazione da questa fonte (*Installa app sconosciute*).
4. Avvia MuteBox e segui la guida ai permessi.

Gli APK ufficiali sono firmati con questo certificato (SHA-256):

```
24:FA:47:56:EC:A4:51:FC:DD:3C:3D:03:F6:47:63:1A:81:89:8D:B9:FF:48:A5:C1:36:0C:B3:62:D5:B1:A2:C5
```

Puoi controllarlo con `apksigner verify --print-certs MuteBox-vX.Y.apk`.
Non installare APK di MuteBox presi da altri siti: potrebbero essere modificati.

## Funzioni

- **Interruttore unico**: accendi MuteBox dall'app, dal riquadro nelle Impostazioni rapide o spegnilo dalla notifica.
- **Regole per app**, per ognuna puoi scegliere:
  - *In silenzio*: non suona mai;
  - *Tutte le notifiche*: suona sempre;
  - *Solo alcune persone o gruppi*: suona solo per i nomi che indichi.
- **Suggerimenti automatici**: i mittenti visti di recente nelle notifiche ti vengono proposti con un tocco.
- **Nomi tolleranti**: maiuscole, accenti ed emoji vengono ignorati ("Nicolò 💙" = "nicolo").
- **Orario automatico**: si accende e si spegne da solo (es. dalle 23:00 alle 07:00), anche a cavallo della mezzanotte.
- **Suono personalizzabile**: scegli suoneria, volume e vibrazione; c'è il pulsante "Prova il suono".
- **Sicuro per default**: se Android interrompe l'ascolto delle notifiche, MuteBox toglie il silenzio
  invece di farti perdere messaggi importanti.
- **Nessun accesso a Internet**, nessuna pubblicità, nessun tracciamento.

## Come funziona

1. Quando MuteBox è attivo, imposta il "Non disturbare" di Android in modalità **solo sveglie**: tutto tace.
2. Un `NotificationListenerService` legge le notifiche in arrivo ed estrae mittente e conversazione
   (da `MessagingStyle` o dal titolo, es. `"Famiglia (5 messaggi): Zio Carlo"`).
3. Se la notifica corrisponde a una tua regola, MuteBox riproduce un avviso sul **canale sveglia**,
   l'unico che il "Non disturbare" lascia passare.
4. Allo spegnimento, ripristina il filtro "Non disturbare" che avevi prima.

Le notifiche ripetute della stessa chat entro 3 secondi suonano una volta sola e quelle dei canali
silenziosi non suonano mai.

## Requisiti

- Android 8.0 (API 26) o successivo.
- Permessi da concedere al primo avvio (l'app ti guida):

| Permesso | Perché serve |
|---|---|
| Accesso alle notifiche | Capire chi ti sta scrivendo (obbligatorio) |
| Accesso a "Non disturbare" | Mettere in silenzio il telefono (obbligatorio) |
| Mostrare notifiche | Notifica di stato con il pulsante "Disattiva" |
| Sveglie esatte | Orario automatico preciso |
| Nessuna ottimizzazione batteria | Evitare che il sistema chiuda l'app di notte |

Su Xiaomi, Samsung, Huawei e simili abilita anche **l'avvio automatico** di MuteBox.
Se Android mostra "impostazione con limitazioni", apri *Info app*, menu ⋮, *Consenti impostazioni con limitazioni*.

## Compilare e installare

Serve [Android Studio](https://developer.android.com/studio) (o un JDK 17+ con l'Android SDK).

```bash
git clone https://github.com/makaDv/MuteBox.git
cd MuteBox
./gradlew assembleDebug        # APK in app/build/outputs/apk/debug/
./gradlew testDebugUnitTest    # test unitari
```

Per installarla su un telefono collegato: `./gradlew installDebug`.

### Build release firmata (opzionale)

Crea un file `keystore.properties` nella cartella principale (è già escluso da git):

```properties
storeFile=percorso/della/tua-chiave.jks
storePassword=...
keyAlias=...
keyPassword=...
```

Poi `./gradlew assembleRelease`. Senza questo file l'APK release viene generato non firmato.
**Non pubblicare mai la tua chiave o le password.**

## Struttura del progetto

```
app/src/main/java/io/github/makadv/mutebox/
├── data/       Salvataggio di regole e impostazioni (SharedPreferences)
├── domain/     Logica pura e testata: confronto nomi, fasce orarie, parsing dei titoli
├── dnd/        Controllo del "Non disturbare", riquadro Impostazioni rapide, notifica di stato
├── schedule/   Allarmi per l'orario automatico e ripristino dopo il riavvio
├── service/    Ascolto delle notifiche e riproduzione dell'avviso
└── ui/         Interfaccia in Jetpack Compose (Material 3)
```

Tecnologie: Kotlin, Jetpack Compose, Material 3, Coroutines/StateFlow. Nessuna dipendenza esterna oltre ad AndroidX.

## Privacy

Tutto avviene sul dispositivo. MuteBox non ha il permesso Internet, non raccoglie, non invia e non vende dati,
ed esclude i propri dati dal backup cloud. È progettata secondo i principi di privacy by design del GDPR:

- **informativa al primo avvio**, prima di chiedere l'accesso alle notifiche;
- **minimizzazione**: le notifiche delle app senza regole (banca, salute, ecc.) vengono ignorate subito;
  i nomi dei mittenti si salvano solo per le app in modalità "Solo alcune persone";
- **conservazione limitata**: i nomi visti si cancellano da soli dopo 30 giorni;
  il testo dei messaggi non viene mai salvato;
- **controllo dall'app**: esporta i tuoi dati in JSON, cancella i nomi visti o tutti i dati, revoca il permesso.

Dettagli in [PRIVACY.md](PRIVACY.md).

## Contribuire

Segnalazioni e proposte sono benvenute tramite *issue* e *pull request*. Inviando un contributo accetti
che venga distribuito con la stessa licenza del progetto (MIT).

## Licenza

Distribuito con licenza [MIT](LICENSE): puoi usarlo, modificarlo e ridistribuirlo liberamente,
mantenendo l'avviso di copyright.

## Esclusione di responsabilità

- Il software è fornito **"così com'è", senza garanzie di alcun tipo**. Gli autori non sono responsabili
  di notifiche, chiamate o avvisi persi, né di altri danni derivanti dall'uso dell'app (vedi [LICENSE](LICENSE)).
- MuteBox **non sostituisce** sistemi di allerta, sveglie mediche o servizi di emergenza.
- MuteBox è un progetto indipendente e **non è affiliato, approvato o sponsorizzato** da WhatsApp, Meta,
  Google o da qualsiasi altra app o azienda citata. Tutti i marchi appartengono ai rispettivi proprietari
  e sono citati solo a scopo descrittivo.
- Chi distribuisce versioni modificate o le pubblica su store è responsabile del rispetto delle regole
  dello store e delle leggi applicabili.
