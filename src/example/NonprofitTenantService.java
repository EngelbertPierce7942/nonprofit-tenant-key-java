package example;

import java.util.List;

public final class NonprofitTenantService {
    public record TenantRequest(String name, String contactEmail) {}
    public record TenantPlan(String decision, List<String> workflows, String ownership, List<String> rollback) {}

    public TenantPlan plan(TenantRequest request) {
        if (request.name().isBlank() || request.contactEmail().isBlank()) throw new IllegalArgumentException("tenant name and contact email are required");
        return new TenantPlan(
                "CUTOVER_READY",
                List.of("DONOR_RECEIPTS", "VOLUNTEER_REMINDERS", "CAMPAIGN_REPORTING"),
                "one scoped key owns user " + request.contactEmail(),
                List.of("restore the incumbent in-house key table entry", "revoke the temporary Infrai key"));
    }

    public String keyCreatePath() { return "account.keys.create -> POST /v1/account/keys/create"; }
    public String userCreatePath() { return "auth.user.create -> POST /v1/auth/user/create"; }
    public String temporaryKeyRevokePath() { return "account.keys.revoke -> DELETE /v1/account/keys/revoke/{id}"; }
}
