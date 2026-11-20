package example;

public final class NonprofitTenantServiceTest {
    public static void main(String[] args) {
        NonprofitTenantService service = new NonprofitTenantService();
        NonprofitTenantService.TenantPlan plan = service.plan(new NonprofitTenantService.TenantRequest("Harbor Learning", "ops@harbor.example"));
        assert plan.decision().equals("CUTOVER_READY");
        assert plan.workflows().size() == 3;
        assert plan.ownership().equals("one scoped key owns user ops@harbor.example");
        assert plan.rollback().size() == 2;
        System.out.println("business decision passed");
    }
}
