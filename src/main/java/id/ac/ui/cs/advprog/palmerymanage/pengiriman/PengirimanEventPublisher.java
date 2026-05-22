package id.ac.ui.cs.advprog.palmerymanage.pengiriman;

import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;

// DIP + LSP: callers depend on this abstraction; {@link SpringPengirimanEventPublisher} is substitutable.
public interface PengirimanEventPublisher {

    void publishPengirimanTiba(Pengiriman pengiriman);

    void publishPengirimanApprovedMandor(Pengiriman pengiriman);

    void publishPengirimanApprovedAdmin(Pengiriman pengiriman, int recognizedKg);
}
