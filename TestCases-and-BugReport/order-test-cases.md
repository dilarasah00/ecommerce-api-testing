### Order Management

| ID | Title | Method / Endpoint | Steps to Reproduce / Payload | Expected Result | Actual Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **ORD-01** | Create a new order with valid customer and stock (Positive) | `POST` `/api/orders` | | 201 Created. Order status should be set to `PENDING`. Product stock must decrease by the ordered quantity, and line items should be recorded in the `order_item` table. | | **UNTY** |
| **ORD-02** | Verify automatic total price calculation on order creation (Positive) | `POST` `/api/orders` | | 201 Created. The total price in the order record must be automatically calculated as the exact sum of all item prices multiplied by their respective quantities. | | **UNTY** |
| **ORD-03** | Try to create an order with a non-existing customerId (Negative) | `POST` `/api/orders` | | 404 Not Found (`NotFoundException`). The global exception handler should return a clean error schema stating that the customer could not be found. No records should be created. | | **UNTY** |
| **ORD-04** | Try to create an order with insufficient product stock (Negative) | `POST` `/api/orders` | | 400 Bad Request (`BusinessException`). The operation must fail, database transaction must rollback, and the product stock must remain unchanged. | | **UNTY** |
| **ORD-05** | Update order status from PENDING to SHIPPED (Positive) | `PATCH` `/api/orders/{id}/status` | | 200 OK. The order status in the database must successfully transition to `SHIPPED`. | | **UNTY** |
| **ORD-06** | Cancel an order and verify stock restoration (Positive/Integration) | `PATCH` `/api/orders/{id}/status` | | 200 OK. The order status must become `CANCELLED`, and the quantities of all products in this order must be automatically added back to their stock levels. | | **UNTY** |
| **ORD-07** | Delete an existing order and check cascade behavior (Positive) | `DELETE` `/api/orders/{id}` | | 200 OK or 204 No Content. The order must be removed from the `orders` table, and all corresponding items in the `order_item` table must be automatically deleted (Cascade Delete). Products must not be deleted. | | **UNTY** |
| **ORD-08** | Try to create an order with a missing (null) customerId (Negative Validation) | `POST` `/api/orders` | | 400 Bad Request. Validation error should block the request at the controller level before reaching business logic. | | **UNTY** |
| **ORD-09** | Try to create an order with an empty product list (Negative Validation) | `POST` `/api/orders` | | 400 Bad Request. System should reject the request, stating that an order must contain at least one product item. | | **UNTY** |
| **ORD-10** | Try to create an order with an invalid data type for customerId (Negative Validation) | `POST` `/api/orders` | | 400 Bad Request. Spring's parsing mechanism or global exception handler should gracefully catch the type mismatch without crashing. | | **UNTY** |
| **ORD-11** | Try to pass a negative product quantity in order items (Negative Validation) | `POST` `/api/orders` | | 400 Bad Request. Validation constraint (e.g., `@Min(1)`) should trigger and reject the request immediately. | | **UNTY** |
| **ORD-12** | Try to pass a ZERO (0) product quantity in order items (Zero-State Boundary) | `POST` `/api/orders` | | 400 Bad Request. The system must explicitly reject zero-quantity orders at the schema/validation level, regardless of the product's actual database stock. | | **UNTY** |
| **ORD-13** | Try to update an order status using an invalid ID data type | `PATCH` `/api/orders/{id}/status` | | 400 Bad Request. The request must be rejected by the controller layer due to a path variable method argument type mismatch. | | **UNTY** |
| **ORD-INT-01** | Verify n8n Order Tracking polling data consistency (Integration) | `GET` `/api/orders` | | 200 OK. Dönen JSON listesi; Sipariş ID, Müşteri Email, Toplam Tutar ve Statü alanlarını eksiksiz içermelidir (n8n Google Sheets entegrasyonunun kırılmaması için). | | **UNTY** |
| **ORD-INT-02** | Verify response body completeness on status update for n8n alerts (Integration) | `PATCH` `/api/orders/{id}/status` | | 200 OK. Dönen response gövdesi güncel statüyü ve müşteri e-postasını eksiksiz dönmelidir (n8n'in Gmail üzerinden tetikleyeceği e-posta akışını besleyebilmesi için). | | **UNTY** |

### Summary of the Report

* **Total Cases:** 15
* **Passed:** 0
* **Failed:** 0
* **Untested (UNTY):** 15