package salon.financing.infrastructure.out.mock;

import salon.financing.application.port.out.BankIntegrationAcl;

/**
 * ADAPTER WYJŚCIOWY (ACL, Rysunek 42: BankService) – atrapa integracji z systemem bankowym.
 *
 * Symuluje asynchroniczne złożenie wniosku: bank jest jedynym decydentem, a decyzja wraca
 * później osobnym zdarzeniem (UC-FIN-02). Wariant rzucający wyjątek odpowiadałby A2
 * (natychmiastowe odrzucenie walidacyjne po stronie banku).
 */
public class BankIntegrationMockAdapter implements BankIntegrationAcl {

    @Override
    public void submitFinancingApplication(String orderId) {
        System.out.println("[BankIntegrationMockAdapter] Wniosek o finansowanie dla zamówienia "
                + orderId + " wysłany do banku — oczekiwanie na asynchroniczną decyzję.");
    }
}
