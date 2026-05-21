package id.ac.ui.cs.advprog.palmerymanage.pengiriman;

import id.ac.ui.cs.advprog.palmerymanage.model.PengirimanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriverPengirimanStatusTransitionPolicyTest {

    private PengirimanStatusTransitionPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new DriverPengirimanStatusTransitionPolicy();
    }

    @Test
    void allowsMemuatToMengirim() {
        assertTrue(policy.canTransition(PengirimanStatus.MEMUAT, PengirimanStatus.MENGIRIM));
    }

    @Test
    void allowsMengirimToTibaDiTujuan() {
        assertTrue(policy.canTransition(PengirimanStatus.MENGIRIM, PengirimanStatus.TIBA_DI_TUJUAN));
    }

    @Test
    void rejectsInvalidTransitions() {
        assertFalse(policy.canTransition(PengirimanStatus.MEMUAT, PengirimanStatus.TIBA_DI_TUJUAN));
        assertFalse(policy.canTransition(PengirimanStatus.PENDING_MANDOR_REVIEW, PengirimanStatus.MENGIRIM));
    }
}
