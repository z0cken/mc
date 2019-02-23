# PCS_Economy

Economyplugin für den Z0cken Minecraftserver.

# Implementierte Funktionen:
## Commands:
#### \<> -> Muss
#### \[] -> Optional
#### Alias von /m0ney -> /m
#### Balance-Self
Command: **/m0ney**  
Zweck: Eigenen Kontostand einsehen
#### Balance-Other
Command: **/m0ney balance \<player>**  
Zweck: Kontostand eines fremden Spielers einsehen
#### Account-Clear
Command: **/m0ney account clear \<player>**  
Zweck: Konto eines Spielers auf 0 setzen
#### Account-Delete
Command: **/m0ney account delete \<player>**  
Zweck: Konto eines Spielers löschen. Sollte aber nur bei **höchst unwahrscheinlichen** Fehlern in der Datenverarbeitung angewandt werden
#### Account-Create
Command: **/m0ney account create \<player>**  
Zweck: Konto für einen Spieler erstellen. Sollte ebenso nur bei **höchst unwahrscheinlichen** Fehlern angewandt werden.
#### Account-Set
Command: **/m0ney account set \<player> \<amount>**  
Zweck: Kontostand eines Spielers auf den gewählten Betrag setzen.
#### Account-Add
Command: **/m0ney account add \<player> \<amount>**  
Zweck: Dem Konto eines Spielers den gewählten Betrag hinzufügen.
#### Account-Subtract
Command: **/m0ney account subtract \<player> \<amount>**  
Zweck: Dem Konto eines Spielers den gewählten Betrag abziehen.
#### Pay
Command: **/pay \<player> \<amount>**  
Zweck: Einem Spieler den gewählten Betrag überweisen.
#### Shop-Create-Admin
Command: **/shop create admin \<shopname> [traderLook]**  
Zweck: Einen Adminshop mit dem gewählten Namen erstellen.
#### Shop-Delete
Command: **/shop delete \<shopID>**  
Zweck: Einen Shop mit der gewählten Shop-ID entfernen.
#### Shop-TpTo
Command: **/shop tpto \<shopID>**    
Zweck: Zu einem Shop teleportieren
#### Shop-TpHere
Command **/shop tphere \<shopID>**    
Zweck: Einen Shop zur aktuellen Spielerposition teleportieren
#### Shop-ChangeName
Command: **/shop change name \<shopID> \<traderName>**    
Zweck: Den Namen eines Shops ändern
#### Shop-Career
Command: **/shop career \<shopID> \<traderLook>**    
Zweck: Dem Trader ein neues Aussehen geben.

## NPC-Adminshops:
### Sinn und Zweck:
Adminshops sind dafür da, dem Spieler Items zu Verkaufen oder Abzukaufen. Diese zeichnen sich durch beständige Preise, Materialien sowie Items aus. Sie sollen, so wie die Shops später generell, eine einfache und sichere Art und Weise zu Handeln sicherstellen. Dabei ist natürlich auch die Benutzerfreundlichkeit und damit eine einfache, selbsterklärende Bedienung im Vordergrund.
### Bedienung:
#### Konfiguration in der Datei
Die Datei in der die Items, und die entsprechenden Preise sowie Restriktionen im An- und Verkauf dieser, konfiguriert werden liegt im PCS_Economy-Ordner im Plugins-Ordner. Sie lautet **adminShopConfig.yml**. Diese ist, wie die Endung schon sagt, im YML-Format. Dies ist nützlich für eine einfache, schnelle Konfigurierung.  

Sie ist folgendermaßen strukturiert:
```
...
- STONE_AXE|0|0|00
- STONE_BRICKS|0|0|00
...
```
* Der erste Parameter ist der Name des Materials. Dieser soll **nicht** geändert werden, da dieser Name für die Auflösung des Materials im Plugin notwendig ist.
* Der Zweite ist der Verkaufspreis. Dabei ist zu beachten, dass der Begriff Verkauf aus der **Sicht des Shops** folgt.
* Der Dritte ist der Ankaufspreis. Dabei ist ebenfalls zu beachten, dass der Begriff Ankauf aus der **Sicht des Shops** fogt.

**Bei den Preisen ist zu beachten, dass es sich hierbei um Integers handelt**

Der Vierte ist eine Kombination für zwei Dinge:  
* Der erste Teil steht dafür, ob der Shop das Item zum Verkauf anbietet **10**
* Der zweite Teil steht dafür, ob der Shop das Item kauft **01**
* Dabei ist eine Kombination aus beiden Teilen natürlich möglich **11** oder **00**

#### Konfiguration der einzelnen Shops (Ingame)
Die Konfiguration dessen, welcher Shop nun welche Items anbietet, wird Ingame durch **Shift-Rechtsklick** auf einen Shop-NPC bewerkstelligt.  
Hierbei ist es möglich, Items in das geöffnete Inventarfenster zu legen. Die nun in dem Inventar vorhandenen Items stehen da nun, je nach Konfiguration, zum An- und Verkauf bereit.  

**Natürlich geht selbst das nicht ohne etwas dabei zu beachten:**  
Adminshops können unendlich viele Items verkaufen. Solltet ihr einen Stack da rein tun, wird daraus automatisch ein Item und der Rest des Stacks liegt nun im digitalen Nirvana.
#### Ein- und Verkauf
Durch Rechtsklick auf einen Shop öffnet sich das Auswahlfenster. Dort sind alle Items aufgelistet, die der Shop zum An- und Verkauf anbietet. Dabei wird im Lore zudem noch der An- und Verkaufspreis für ein einzelnes Item angezeigt.  

Klickt man nun dabei auf eines der Items, öffnet sich die nächste Ansicht. Dies ist die Handelsansicht. In der Mitte ist das Item zu sehen, um das es geht. Links kann der User Gegenstände verkaufen, rechts kaufen. 

Im Lore der jeweiligen Felder steht natürlich auch, was diese machen, wie viele Items dort Ver- oder Gekauft werden, sowie der Preis.  

Sollte der Spieler für das Vollkaufen des Inventars kein Geld haben, wird eine entsprechende Meldung im Lore angezeigt und die Handlung blockiert. Dies gilt für einzelne, sowie Stackweise Items, diese haben aber ihre eigenen Fehlermeldungen, und die werden auch da bei nicht entsprechender Kontodeckung nicht verkauft.

Das Selbe, nur für die Items im Inventar gilt für den Ankauf von Items. Sprich: Sollte der Spieler beispielsweise keinen Stack Items vom Typ Stein im Inventar haben, wird er auch keinen Stack verkaufen können. Jedoch besteht da die Möglichkeit, einen einzelnen Stein, sowie alle restlichen Steine an den Shop zu verkaufen.

Drückt man die E oder Escape-Taste, kommt man wieder zur Auswahlansicht zurück.

#### Shop-ID herausfinden
Die Shop-ID kann man ganz einfach in der Ingame-Shopkonfiguration finden. Es ist die Zahl hinter dem Namen des Shops.

### Grundlegende Konfiguration
#### config.yml
Die Hauptkonfiguration ist wie gewohnt in der config.yml, ebenfalls zu finden im PCS_Economy-Ordner im Plugins-Ordner.  
Dort kann man den Währungsnamen einstellen, den Prefix und jede Meldung.  

Es gibt in der Konfiguration zwei statische Platzhalter:
* **{CSYMBOL}** ist das Symbol der Währung
* **{PREFIX}** ist der Prefix

Dann gibt es noch zwei dynamische Platzhalter:
* **{PLAYER}** ist der Spielername. Dies geht natürlich nur dort, wo auch jetzt {PLAYER} steht.
* **{AMOUNT}** ist die Menge, sei es Geld, oder die Menge von Items in der GUI. Dort gilt das Selbe wie für {PLAYER}

#### traders.json
Das ist die Datei, in der die einzelnen Trader gespeichert werden. Dort sollte man im besten Fall nichts ändern.

### Sonstige Funktionen:
* Überziehen des Kontos beim Kauf von Items oder beim Überweisen ist unmöglich
* Die Datenbank wird für die Transaktionen asynchron angesprochen. Im Plugin selbst sind immer alle Kontostände der anwesenden Spieler synchron
* Automatisches Updaten der adminShopConfig.yml im Falle eines Minecraft-Updates. So muss darin nichts komplett neu konfiguriert werden
* Anbindung mit höchster Priorität an die Vault-API

# Sonstiges
## Datenbanktabelle
Das Plugin erstellt die Tabelle in der Datenbank automatisch.

## Dependencies
Das Plugin benötigt den aktuellen PCS_Core von Flare. Dort sind der im Plugin genutzte MessageBuilder sowie der DB-Connectionpool enthalten

## Das Plugin als Dependency verwenden
Noch besteht kein Sinn, das Plugin direkt als API für die Economy oder generell als Dependency zu verwenden, da es sich mit höchster Priorität an Vault bindet. Nutzt daher bitte Vault und greift von dort auf die Economy zu. Beachtet dabei, dass über die Vault-API keine Checks stattfinden, ob das Konto eines Spielers mit dem Abzug von Geld ins Minus gerät. Dafür müsst ihr selber sorgen.

## Es folgt noch mehr. Keine Ahnung wie ich darüber so viel schreiben konnte.

