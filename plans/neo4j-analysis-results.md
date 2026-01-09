# Neo4j Analyse: Party Service Abhängigkeiten - ECHTE DATEN

## Zusammenfassung

**Gesamt Party Service Klassen:** 44

**Gesamt Party Service Packages:** 20

### Modul-Übersicht (Aufrufe zu Party Service)

| Module | CallCount |
|--------|----------|
| Other | 436 |
| **GESAMT** | **436** |

### Komplexität pro Modul

| Module | UniqueCallers | TotalCalls | AvgCallsPerMethod |
|--------|---------------|------------|-------------------|
| Other | 63 | 436 | 6.92 |

### Top 20 kritische Party Service Methoden

| Rank | TargetMethod | IncomingCalls |
|------|--------------|---------------|
| 1 | `org.apache.ofbiz.party.party.PartyHelper.getPartyName` | 30 |
| 2 | `org.apache.ofbiz.party.content.PartyContentWrapper.getPartyContentAsText` | 28 |
| 3 | `None` | 21 |
| 4 | `org.apache.ofbiz.party.party.PartyWorker.findPartyLatestContactMech` | 20 |
| 5 | `org.apache.ofbiz.party.party.PartyServices.getPartyId` | 16 |
| 6 | `org.apache.ofbiz.party.contact.ContactMechWorker.getPartyContactMechValueMaps` | 16 |
| 7 | `org.apache.ofbiz.party.party.PartyWorker.makeMatchingString` | 16 |
| 8 | `org.apache.ofbiz.party.party.PartyWorker.findPartiesById` | 16 |
| 9 | `None` | 15 |
| 10 | `org.apache.ofbiz.party.contact.ContactHelper.getContactMech` | 14 |
| 11 | `org.apache.ofbiz.party.contact.ContactMechWorker.getFacilityContactMechByPurpose` | 14 |
| 12 | `org.apache.ofbiz.party.communication.CommunicationEventServices.buildListOfWorkEffortInfoFromEmailAddresses` | 12 |
| 13 | `org.apache.ofbiz.party.communication.CommunicationEventServices.buildListOfPartyInfoFromEmailAddresses` | 12 |
| 14 | `org.apache.ofbiz.party.contact.ContactMechWorker.insertRelatedContactElement` | 12 |
| 15 | `org.apache.ofbiz.party.communication.CommunicationEventServices.createCommunicationEventWorkEffs` | 12 |
| 16 | `org.apache.ofbiz.party.party.PartyWorker.findPartyLatestUserLogin` | 12 |
| 17 | `org.apache.ofbiz.party.communication.CommunicationEventServices.createCommEventRoles` | 12 |
| 18 | `org.apache.ofbiz.party.contact.ContactMechWorker.getFacilityContactMechValueMaps` | 8 |
| 19 | `org.apache.ofbiz.party.contact.ContactMechWorker.getEntityContactMechValueMaps` | 8 |
| 20 | `org.apache.ofbiz.party.party.PartyWorker.findParty` | 8 |

### Top 30 Klassen mit Party Service Aufrufen

| Rank | Class | CallCount |
|------|-------|----------|
| 1 | `org.apache.ofbiz.party.party.PartyWorker` | 76 |
| 2 | `org.apache.ofbiz.party.communication.CommunicationEventServices` | 72 |
| 3 | `org.apache.ofbiz.party.content.PartyContentWrapper` | 52 |
| 4 | `org.apache.ofbiz.party.contact.ContactMechWorker` | 36 |
| 5 | `org.apache.ofbiz.order.order.OrderServices` | 20 |
| 6 | `org.apache.ofbiz.party.party.PartyServices` | 20 |
| 7 | `org.apache.ofbiz.party.party.PartyHelper` | 16 |
| 8 | `org.apache.ofbiz.sfa.vcard.VCard` | 16 |
| 9 | `org.apache.ofbiz.party.contact.ContactMechServices` | 16 |
| 10 | `org.apache.ofbiz.shipment.thirdparty.fedex.FedexServices` | 12 |
| 11 | `org.apache.ofbiz.party.contact.ContactHelper` | 12 |
| 12 | `org.apache.ofbiz.order.shoppingcart.CheckOutHelper` | 12 |
| 13 | `org.apache.ofbiz.humanres.HumanResEvents` | 8 |
| 14 | `org.apache.ofbiz.order.shoppingcart.ShoppingCart` | 8 |
| 15 | `org.apache.ofbiz.order.shoppingcart.shipping.ShippingEvents` | 8 |
| 16 | `org.apache.ofbiz.accounting.tax.TaxAuthorityServices` | 8 |
| 17 | `org.apache.ofbiz.product.store.ProductStoreWorker` | 8 |
| 18 | `org.apache.ofbiz.shipment.shipment.ShipmentServices` | 8 |
| 19 | `org.apache.ofbiz.product.product.ProductSearch$SupplierConstraint` | 4 |
| 20 | `org.apache.ofbiz.accounting.payment.PaymentGatewayServices` | 4 |
| 21 | `org.apache.ofbiz.securityext.login.LoginEvents` | 4 |
| 22 | `org.apache.ofbiz.shipment.thirdparty.usps.UspsServices` | 4 |
| 23 | `org.apache.ofbiz.shipment.thirdparty.ups.UpsServices` | 4 |
| 24 | `org.apache.ofbiz.order.shoppingcart.CheckOutEvents` | 4 |
| 25 | `org.apache.ofbiz.party.party.PartyRelationshipServices` | 4 |

### Party Service Klassen

| Rank | Class |
|------|-------|
| 1 | `org.apache.ofbiz.party.communication.CommunicationEventServices` |
| 2 | `org.apache.ofbiz.party.communication.CommunicationEventServices` |
| 3 | `org.apache.ofbiz.party.communication.CommunicationEventServices` |
| 4 | `org.apache.ofbiz.party.communication.CommunicationEventServices` |
| 5 | `org.apache.ofbiz.party.contact.ContactHelper` |
| 6 | `org.apache.ofbiz.party.contact.ContactHelper` |
| 7 | `org.apache.ofbiz.party.contact.ContactHelper` |
| 8 | `org.apache.ofbiz.party.contact.ContactHelper` |
| 9 | `org.apache.ofbiz.party.contact.ContactMechServices` |
| 10 | `org.apache.ofbiz.party.contact.ContactMechServices` |
| 11 | `org.apache.ofbiz.party.contact.ContactMechServices` |
| 12 | `org.apache.ofbiz.party.contact.ContactMechServices` |
| 13 | `org.apache.ofbiz.party.contact.ContactMechWorker` |
| 14 | `org.apache.ofbiz.party.contact.ContactMechWorker` |
| 15 | `org.apache.ofbiz.party.contact.ContactMechWorker` |
| 16 | `org.apache.ofbiz.party.contact.ContactMechWorker` |
| 17 | `org.apache.ofbiz.party.content.PartyContentWrapper` |
| 18 | `org.apache.ofbiz.party.content.PartyContentWrapper` |
| 19 | `org.apache.ofbiz.party.content.PartyContentWrapper` |
| 20 | `org.apache.ofbiz.party.content.PartyContentWrapper` |
| 21 | `org.apache.ofbiz.party.party.PartyHelper` |
| 22 | `org.apache.ofbiz.party.party.PartyHelper` |
| 23 | `org.apache.ofbiz.party.party.PartyHelper` |
| 24 | `org.apache.ofbiz.party.party.PartyHelper` |
| 25 | `org.apache.ofbiz.party.party.PartyRelationshipHelper` |
| 26 | `org.apache.ofbiz.party.party.PartyRelationshipHelper` |
| 27 | `org.apache.ofbiz.party.party.PartyRelationshipHelper` |
| 28 | `org.apache.ofbiz.party.party.PartyRelationshipHelper` |
| 29 | `org.apache.ofbiz.party.party.PartyRelationshipServices` |
| 30 | `org.apache.ofbiz.party.party.PartyRelationshipServices` |
| 31 | `org.apache.ofbiz.party.party.PartyRelationshipServices` |
| 32 | `org.apache.ofbiz.party.party.PartyRelationshipServices` |
| 33 | `org.apache.ofbiz.party.party.PartyServices` |
| 34 | `org.apache.ofbiz.party.party.PartyServices` |
| 35 | `org.apache.ofbiz.party.party.PartyServices` |
| 36 | `org.apache.ofbiz.party.party.PartyServices` |
| 37 | `org.apache.ofbiz.party.party.PartyTypeHelper` |
| 38 | `org.apache.ofbiz.party.party.PartyTypeHelper` |
| 39 | `org.apache.ofbiz.party.party.PartyTypeHelper` |
| 40 | `org.apache.ofbiz.party.party.PartyTypeHelper` |
| 41 | `org.apache.ofbiz.party.party.PartyWorker` |
| 42 | `org.apache.ofbiz.party.party.PartyWorker` |
| 43 | `org.apache.ofbiz.party.party.PartyWorker` |
| 44 | `org.apache.ofbiz.party.party.PartyWorker` |

### Packages mit Party Service Aufrufen

| Package | UniqueCallers | TotalCalls |
|---------|---------------|----------|
| Other | 63 | 436 |

