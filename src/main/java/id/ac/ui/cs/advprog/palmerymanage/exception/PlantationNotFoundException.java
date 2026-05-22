package id.ac.ui.cs.advprog.palmerymanage.exception;

import java.util.UUID;

public class PlantationNotFoundException extends RuntimeException {

    public PlantationNotFoundException(UUID id) {
        super("Kebun dengan ID " + id + " tidak ditemukan");
    }
}
