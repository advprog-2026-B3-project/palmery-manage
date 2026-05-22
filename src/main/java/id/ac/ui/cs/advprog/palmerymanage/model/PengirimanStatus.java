package id.ac.ui.cs.advprog.palmerymanage.model;

/**
 * Delivery status — tracks the physical shipment progress.
 * Approval statuses are tracked separately via mandorApprovalStatus and adminApprovalStatus.
 */
public enum PengirimanStatus {
    MEMUAT,
    MENGIRIM,
    TIBA_DI_TUJUAN,

    // Legacy values kept for backward compatibility with existing data
    PENDING_MANDOR_REVIEW,
    APPROVED_MANDOR,
    REJECTED_MANDOR,
    PENDING_ADMIN_REVIEW,
    APPROVED_ADMIN,
    REJECTED_ADMIN,
    PARTIAL_REJECTED_ADMIN
}
