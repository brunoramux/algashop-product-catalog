package contracts.product

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    request {
        method GET()
        headers {
            accept 'application/json'
        }
        url("/api/v1/products/5e3d2c8e-4a8b-4c67-a8f0-9b6c1d7f42e1")
    }
    response {
        status(
                200
        )
        headers {
            contentType 'application/json'
        }
        body([
                id: fromRequest().path(3),
                addedAt: anyIso8601WithOffset(),
                name: 'Notebook X11',
                brand: 'Deep Diver',
                regularPrice: 1500.00,
                salePrice: 1000.00,
                inStock: false,
                enabled: true,
                category: [
                    id: anyUuid(),
                    name: 'Notebook'
                ],
                description: 'A Gamer Notebook'
        ])
    }
}

