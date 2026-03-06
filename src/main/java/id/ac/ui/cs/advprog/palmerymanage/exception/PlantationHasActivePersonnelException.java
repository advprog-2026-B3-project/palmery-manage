package id.ac.ui.cs.advprog.palmerymanage.exception;

import java.util.UUID;

public class PlantationHasActivePersonnelException extends RuntimeException {

    public PlantationHasActivePersonnelException(UUID plantationId) {
        super("Kebun dengan ID " + plantationId + " masih memiliki mandor aktif dan tidak dapat dihapus");
    }
}
