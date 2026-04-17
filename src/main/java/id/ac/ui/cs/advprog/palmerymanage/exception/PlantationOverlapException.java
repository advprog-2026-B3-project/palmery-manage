package id.ac.ui.cs.advprog.palmerymanage.exception;

public class PlantationOverlapException extends RuntimeException {

    public PlantationOverlapException() {
        super("Koordinat kebun bertabrakan dengan kebun yang sudah ada");
    }
}
