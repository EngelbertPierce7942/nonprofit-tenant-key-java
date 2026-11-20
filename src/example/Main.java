package example;

import java.util.Map;
import java.util.UUID;

public final class Main {
    public static void main(String[] args) {
        NonprofitTenantService service = new NonprofitTenantService();
        String email = args.length > 0 ? args[0] : System.getenv("INFRAI_DEMO_EMAIL");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("provide an explicit recipient email as the first argument or INFRAI_DEMO_EMAIL");
        }
        NonprofitTenantService.TenantPlan plan = service.plan(new NonprofitTenantService.TenantRequest("Harbor Learning", email));
        System.out.println(plan.decision());
        System.out.println("workflows=" + plan.workflows());
        System.out.println("ownership=" + plan.ownership());
        System.out.println("rollback=" + plan.rollback());
        System.out.println(service.keyCreatePath());
        System.out.println(service.userCreatePath());
        System.out.println(service.temporaryKeyRevokePath());

        InfraiClient client = new InfraiClient();
        String suffix = UUID.randomUUID().toString();
        String userId = null;
        String keyId = null;
        try {
            String keyResponse = client.request("POST", "/account/keys/create",
                    "{\"name\":\"Harbor Learning demo " + suffix + "\",\"scopes\":[\"account.keys.revoke\"],\"idempotency_key\":\"" + suffix + "-key\"}",
                    Map.of());
            keyId = extractId(keyResponse, "key_id");

            String userResponse = client.request("POST", "/auth/user/create",
                    "{\"email\":\"" + json(email) + "\",\"password\":\"" + json("Demo-" + suffix + "-Aa1!") + "\",\"name\":\"Harbor Learning\",\"vendor\":\"infrai\",\"mode\":\"default_vendor\",\"idempotency_key\":\"" + suffix + "-user\"}",
                    Map.of());
            userId = extractId(userResponse, "user_id");
            System.out.println("created user=" + userId + " key=" + keyId);
        } finally {
            if (userId != null) client.request("DELETE", "/auth/user/delete/" + userId, "", Map.of());
            if (keyId != null) client.request("DELETE", "/account/keys/revoke/" + keyId, "", Map.of());
        }
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String extractId(String response, String field) {
        String marker = "\"" + field + "\":\"";
        int start = response.indexOf(marker);
        if (start < 0) {
            marker = "\"id\":\"";
            start = response.indexOf(marker);
        }
        if (start < 0) throw new IllegalStateException("response did not contain a resource id: " + response);
        start += marker.length();
        int end = response.indexOf('\"', start);
        if (end < 0) throw new IllegalStateException("response contained an invalid resource id: " + response);
        return response.substring(start, end);
    }
}
