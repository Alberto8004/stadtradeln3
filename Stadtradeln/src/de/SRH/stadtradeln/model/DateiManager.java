package de.SRH.stadtradeln.model;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class DateiManager {
    public Map<String, Object> ladeDaten() {
        File file = new File("stadtradeln.dat");
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<String, Object>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public void speichereDaten(Map<String, Object> daten) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("stadtradeln.dat"))) {
            oos.writeObject(daten);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
// Die Methode speichereFahrt im DateiManager stellt sicher, dass jede Fahrt zusätzlich in fahrten.dat persistiert wird:
    public void speichereFahrt(String nickname, int kilometer, LocalDate datum) {
        File file = new File("fahrten.dat");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(nickname + ";" + kilometer + ";" + datum);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Fahrt: " + e.getMessage());
        }
    }


    public List<String[]> ladeFahrten() {
        File file = new File("fahrten.dat");
        List<String[]> fahrten = new ArrayList<>();
        if (!file.exists()) {
            return fahrten;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fahrt = line.split(";");
                if (fahrt.length == 3) {
                    fahrten.add(fahrt);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fahrten;
    }

    // Neue Methode: Verarbeitung von neuefahrten.csv
    public List<String[]> ladeNeueFahrten(String filePath) {
        File file = new File(filePath);
        List<String[]> neueFahrten = new ArrayList<>();

        if (!file.exists()) {
            System.out.println("Die Datei " + filePath + " existiert nicht.");
            return neueFahrten;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] daten = line.split(",");
                if (daten.length == 3) { // Validierung: 3 Spalten erwartet
                    neueFahrten.add(daten);
                } else {
                    System.out.println("Ungültige Zeile in neuefahrten.csv: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return neueFahrten;
    }

    // Neue Methode: Löschen der Datei neuefahrten.csv nach Verarbeitung
    public boolean loescheDatei(String filePath) {
        File file = new File(filePath);
        return file.delete();
    }
}
