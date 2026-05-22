package id.ac.ui.cs.advprog.palmerymanage.pengiriman;

import id.ac.ui.cs.advprog.palmerymanage.model.PengirimanStatus;

// OCP: new transition rules can be added via new implementations without changing {@code PengirimanService}.
public interface PengirimanStatusTransitionPolicy {

    boolean canTransition(PengirimanStatus from, PengirimanStatus to);
}
