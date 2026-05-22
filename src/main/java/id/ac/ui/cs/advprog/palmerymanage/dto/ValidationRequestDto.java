package id.ac.ui.cs.advprog.palmerymanage.dto;

import lombok.Data;

@Data
public class ValidationRequestDto {
    private String status; // APPROVED atau REJECTED
    private String rejectionReason;
}