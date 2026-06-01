classDiagram
direction TB

    %% ==========================================
    %% AGREGAT: INSURANCE POLICY (Polisa)
    %% ==========================================
    class InsurancePolicy {
        <<AggregateRoot>>
        -PolicyId id
        -VinNumber vin
        -Money insuredValue
        -PolicyType type
        -PolicyState state
        +assignResidualValue(Money residualValue) void
        +activatePolicy(String insurerReference) void
    }
    class PolicyId { <<ValueObject>> }
    class VinNumber { <<ValueObject>> }
    class Money { <<ValueObject>> }
    class PolicyType {
        <<Enumeration>>
        LIABILITY_OC
        COMPREHENSIVE_AC
        GAP_INSURANCE
    }
    class PolicyState {
        <<Enumeration>>
        CALCULATING
        PENDING_ACTIVATION
        ACTIVE
    }

    InsurancePolicy *-- "1" PolicyId
    InsurancePolicy *-- "1" VinNumber : referencja (Inwentarz)
    InsurancePolicy *-- "1" Money
    InsurancePolicy *-- "1" PolicyType
    InsurancePolicy *-- "1" PolicyState
