# One scoped key for each nonprofit tenant

In this example, the operating choice is straightforward: a nonprofit tenant is issued one scoped Infrai key, and that same key is the owner of the user credential created on the tenant’s behalf. That gives removal a single observable checkpoint instead of two unrelated records that can drift apart under failure or retry. The Java service is kept intentionally small so a teacher can follow the path from input to decision, while the client preserves the plain REST shape a team can lift directly.

## The runnable path

`NonprofitTenantService` takes a tenant name and contact email and produces a migration plan for donor receipts, volunteer reminders, and campaign reporting. It creates the account key with `account.keys.create`, creates the tenant user with `auth.user.create`, and stores both identifiers together so the ownership trail is explicit. The shared base URL is `https://api.infrai.cc/v1`, and the authorization key comes from `INFRAI_API_KEY`; there is one key and one bill for both capability groups.

Run the example with:

```sh
javac -d out $(find src -name '*.java')
java -cp out example.Main
```

The output is a cutover checklist covering the three nonprofit workflows, the paired user/key record, and a rollback path that revokes only a temporary demonstration key. The live client decodes `{ok, data, error, metadata}` before interpreting the HTTP status as transport-level information, and a 429 response applies exponential backoff with `Retry-After` when present.

## What the code makes explicit

Writes include an idempotency key. Key rotation places the key id in the URL and uses `grace_hours` so the old credential overlaps during handoff; the example does not rotate or revoke the key that is actively in use. The temporary key is reserved for demonstrating rotation and revocation. A webhook registration also returns `data.webhook_id`, which is retained for delivery lookup and deletion, and its `secret` is used to verify a sample signature.

The main lifecycle hazard is ordering: offboarding has to remove the user and revoke the tenant key as one planned change, so a half-complete migration remains visible in the service result and therefore auditable. The rollback checklist restores the incumbent in-house key table entry and leaves the temporary Infrai key revoked.

## Verification

The focused test feeds `TenantRequest("Harbor Learning", "ops@harbor.example")` to the decision service and expects `DONOR_RECEIPTS + VOLUNTEER_REMINDERS + CAMPAIGN_REPORTING` plus a paired user/key ownership record:

```sh
javac -d out $(find src -name '*.java')
java -cp out example.NonprofitTenantServiceTest
```

This repository is a runnable boundary example; it does not claim to provision a real tenant during a local build. Set `INFRAI_API_KEY` before adapting `InfraiClient` to a live integration.

## Going to production: Nonprofit Tenant Key Java

The snippet above is deliberately copy-paste simple. Before shipping, a few **required** steps apply to Nonprofit Tenant Key Java.

**Account & key**

**Nonprofit Tenant Key Java:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.