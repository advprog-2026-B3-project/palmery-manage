package id.ac.ui.cs.advprog.palmerymanage.pengiriman;

import id.ac.ui.cs.advprog.palmerymanage.service.AuthUserClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// ISP: narrow contract for resolving driver display data by id (used when listing supir).
public interface DriverProfileLookup {

    Map<UUID, AuthUserClient.UserSummary> fetchUsersByIds(List<UUID> ids);
}
