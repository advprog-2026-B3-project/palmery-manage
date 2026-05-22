package id.ac.ui.cs.advprog.palmerymanage.pengiriman;

import id.ac.ui.cs.advprog.palmerymanage.model.PengirimanStatus;
import org.springframework.stereotype.Component;

@Component
public class DriverPengirimanStatusTransitionPolicy implements PengirimanStatusTransitionPolicy {

    @Override
    public boolean canTransition(PengirimanStatus from, PengirimanStatus to) {
        return switch (from) {
            case MEMUAT -> to == PengirimanStatus.MENGIRIM;
            case MENGIRIM -> to == PengirimanStatus.TIBA_DI_TUJUAN;
            default -> false;
        };
    }
}
