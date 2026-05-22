package id.ac.ui.cs.advprog.palmerymanage.pengiriman;

import id.ac.ui.cs.advprog.palmerymanage.service.AuthUserClient;

import java.util.List;

// ISP: separate contract for listing drivers by role (dev/auth integration path only).
public interface DriverDirectoryLookup {

    List<AuthUserClient.UserSummary> fetchUsersByRole(String role);
}
