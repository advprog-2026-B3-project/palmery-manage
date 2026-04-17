package id.ac.ui.cs.advprog.palmerymanage.exception;

public class PlantationCodeAlreadyExistsException extends RuntimeException {

    public PlantationCodeAlreadyExistsException(String code) {
        super("Kode kebun '" + code + "' sudah digunakan");
    }
}
