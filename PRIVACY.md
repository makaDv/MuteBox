# Informativa sulla privacy di MuteBox

Ultimo aggiornamento: 25 novembre 2026

MuteBox è un'app open source che funziona **interamente sul tuo dispositivo**. Questa informativa spiega quali
dati tratta, perché e quali diritti hai, secondo il Regolamento (UE) 2016/679 ("GDPR") e le altre normative
sulla protezione dei dati.

## In breve

- I dati restano sul telefono. MuteBox **non ha il permesso di accesso a Internet** e non può inviarli altrove.
- Lo sviluppatore **non riceve, non vede e non conserva** alcun dato degli utenti.
- Nessuna pubblicità, analytics, tracciamento o libreria di terze parti che raccoglie dati.
- Puoi esportare o cancellare tutti i dati dall'app, in qualsiasi momento.

## Chi tratta i dati

Tutto il trattamento avviene sul tuo dispositivo, sotto il tuo controllo e per un uso personale. Lo sviluppatore
distribuisce il software ma non ha accesso ai dati: non agisce quindi come titolare né come responsabile del
trattamento per i dati elaborati dall'app (art. 4 GDPR). L'uso dell'app da parte tua, per scopi personali, rientra
nell'attività a carattere esclusivamente personale o domestico (art. 2, par. 2, lett. c GDPR).

## Quali dati tratta e perché

| Dato | Perché | Dove e per quanto |
|---|---|---|
| Mittente e nome della conversazione delle notifiche, **solo** per le app a cui hai dato una regola | Decidere se far suonare il telefono | Elaborati in memoria, non salvati |
| Testo della notifica, **solo** per le app con regola | Riconoscere le notifiche ripubblicate identiche ed evitare suoni doppi | Ne resta in memoria solo un'impronta numerica (hash), mai il testo; mai salvato su disco |
| Nomi dei mittenti visti di recente (max 40 per app), **solo** per le app in modalità "Solo alcune persone" | Suggerirti i nomi da aggiungere con un tocco | Memoria privata dell'app, **cancellati in automatico dopo 30 giorni** |
| Regole per app e persone, orario automatico, suono, volume, vibrazione, accettazione dell'informativa | Far funzionare l'app come hai scelto | Memoria privata dell'app, finché non li cancelli o disinstalli |
| Elenco delle app installate | Mostrarti le app tra cui scegliere | Letto al momento, non salvato |

Le notifiche delle app **senza regole** (per esempio banca, salute, email) vengono ignorate subito: MuteBox non ne
legge il contenuto e non salva nulla.

## Minimizzazione e sicurezza

- I dati sono salvati nella memoria privata dell'app (`MODE_PRIVATE`), che le altre app non possono leggere.
- I dati sono **esclusi** dal backup cloud di Android e dal trasferimento tra dispositivi.
- L'app non registra nei log contenuti delle notifiche né nomi.
- Il codice sorgente è pubblico e verificabile.

## Dove vanno i dati

Da nessuna parte. MuteBox:

- **non** richiede il permesso di accesso a Internet;
- **non** vende, condivide o trasferisce dati a terzi, né fuori dall'Unione europea;
- **non** crea profili e non prende decisioni automatizzate con effetti giuridici su di te.

## Dati di terzi

I nomi salvati sono quelli che compaiono nelle notifiche che ricevi già sul tuo telefono. Restano sul dispositivo,
scadono dopo 30 giorni e servono solo a te per scegliere chi può farti suonare il telefono.

## I tuoi diritti e come esercitarli

Dato che i dati sono solo sul tuo telefono, puoi esercitare i tuoi diritti direttamente dall'app, nella sezione
**"Privacy e dati"** della schermata principale:

- **Accesso e portabilità** (artt. 15 e 20): "Esporta i miei dati" crea un file JSON con tutti i dati salvati.
- **Cancellazione** (art. 17): "Cancella i nomi visti" o "Cancella tutti i dati". Puoi anche usare Impostazioni di
  Android, App, MuteBox, Spazio di archiviazione, "Cancella dati", oppure disinstallare l'app.
- **Rettifica** (art. 16): modifica o rimuovi regole e persone dalle schermate dell'app.
- **Revoca del permesso**: puoi togliere in qualsiasi momento l'"Accesso alle notifiche" dalle impostazioni di
  Android. L'app smetterà di leggere le notifiche.

Hai inoltre diritto di proporre reclamo all'autorità di controllo, in Italia il
[Garante per la protezione dei dati personali](https://www.garanteprivacy.it).

## Minori

MuteBox non è rivolta in modo specifico ai minori e non raccoglie dati per conto dello sviluppatore, nemmeno
quelli dei minori.

## Altre normative

- **Google Play – Sicurezza dei dati**: nessun dato raccolto, nessun dato condiviso.
- **California (CCPA/CPRA) e altre leggi statunitensi**: MuteBox non vende né condivide informazioni personali e
  non le raccoglie per conto dello sviluppatore.
- **ePrivacy**: l'app non usa cookie né identificatori pubblicitari.

## Modifiche a questa informativa

Ogni modifica viene pubblicata in questo file, con la data di aggiornamento. Le modifiche importanti vengono
indicate nelle note di rilascio.

## Contatti

Domande o segnalazioni: apri una issue nel repository GitHub del progetto
([makaDv/MuteBox](https://github.com/makaDv/MuteBox/issues)).
