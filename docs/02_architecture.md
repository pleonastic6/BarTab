# Architektur

## Package-Idee

- `data/model` – Entities / DTOs
- `data/local` – Room DB, DAO, Migrations
- `domain` – Use Cases / Geschäftslogik
- `ui/screens/sales` – Verkauf und Warenkorb
- `ui/screens/history` – Historie und Verkaufsdetail
- `ui/screens/products` – Produktverwaltung
- `ui/components` – wiederverwendbare Compose-Komponenten
- `ui/theme` – Farben, Typo, Shapes

## Datenmodell

### Category
- id
- name
- sortOrder

### Product
- id
- name
- priceCents
- categoryId
- active
- sortOrder

### Sale
- id
- createdAt
- updatedAt
- status (`draft`, `completed`, `cancelled`)
- totalCents
- note

### SaleItem
- id
- saleId
- productId
- productNameSnapshot
- unitPriceSnapshot
- quantity
- lineTotalCents

### SaleEvent
- id
- saleId
- timestamp
- action
- details
