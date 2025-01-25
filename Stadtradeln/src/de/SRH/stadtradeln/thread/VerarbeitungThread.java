package de.SRH.stadtradeln.thread;

import de.SRH.stadtradeln.model.StadtradelnModel;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VerarbeitungThread extends Thread {

    private static final Logger logger = Logger.getLogger(VerarbeitungThread.class.getName());
    private final StadtradelnModel model;
    private final String neueFahrtenPfad = "C:\\Users\\startklar\\IdeaProjects\\Stadtradeln\\src\\de\\SRH\\stadtradeln\\neuefahrten.csv";

    public VerarbeitungThread(StadtradelnModel model) {
        this.model = model;
    }

    @Override
    public void run() {
        logger.info("VerarbeitungThread gestartet.");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                verarbeiteNeueFahrten(); // Verarbeitet neue Fahrten aus der Datei
                Thread.sleep(60000); // Thread pausiert für 60 Sekunden
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Thread wurde unterbrochen.", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void verarbeiteNeueFahrten() {
        File datei = new File(neueFahrtenPfad);
        if (!datei.exists()) {
            logger.info("Keine neue Fahrten-Datei gefunden.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(neueFahrtenPfad))) {
            String zeile;
            while ((zeile = reader.readLine()) != null) {
                try {
                    validiereDaten(zeile);
                    aktualisiereFahrerDaten(zeile);
                } catch (InvalidDataException e) {
                    logger.warning("Fehlerhafte Zeile übersprungen: " + e.getMessage());
                }
            }
            // Datei nach Verarbeitung löschen
            if (!datei.delete()) {
                logger.warning("Die Datei konnte nicht gelöscht werden: " + neueFahrtenPfad);
            }
        } catch (IOException e) {
            logger.severe("Fehler beim Lesen der Datei: " + e.getMessage());
        }
    }

    private void validiereDaten(String zeile) throws InvalidDataException {
        String[] teile = zeile.split(",");
        if (teile.length != 3) throw new InvalidDataException("Ungültiges Format: " + zeile);

        String nickname = teile[0];
        String datum = teile[1];
        String kilometer = teile[2];

        if (model.getFahrer(nickname) == null) {
            throw new InvalidDataException("Fahrer nicht gefunden: " + nickname);
        }
        if (!datumIstGueltig(datum)) {
            throw new InvalidDataException("Ungültiges Datum: " + datum);
        }
        if (!istNumerisch(kilometer)) {
            throw new InvalidDataException("Ungültige Kilometerangabe: " + kilometer);
        }
    }

    private synchronized void aktualisiereFahrerDaten(String zeile) {
        String[] teile = zeile.split(",");
        String nickname = teile[0];
        int kilometer = Integer.parseInt(teile[2]);

        if (model.getFahrer(nickname) != null) {
            model.addFahrt(nickname, kilometer);
            logger.info("Fahrt hinzugefügt: " + zeile);
        } else {
            logger.warning("Fahrer nicht gefunden: " + nickname);
        }
    }

    private boolean datumIstGueltig(String datum) {
        return datum.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    private boolean istNumerisch(String wert) {
        try {
            Integer.parseInt(wert);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static class InvalidDataException extends Exception {
        public InvalidDataException(String message) {
            super(message);
        }
    }
}
