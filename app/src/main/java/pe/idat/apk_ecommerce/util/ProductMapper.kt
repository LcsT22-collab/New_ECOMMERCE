package pe.idat.apk_ecommerce.util

import pe.idat.apk_ecommerce.data.remote.dto.ProductDto
import pe.idat.apk_ecommerce.data.local.entity.LocalProduct
import pe.idat.apk_ecommerce.domain.model.Product


object ProductMapper {
    fun dtoToDomain(dto: ProductDto): Product = Product(
        id = dto.id,
        name = dto.name,
        price = dto.price,
        description = dto.description,
        category = dto.category,
        image = dto.image,
        stock = dto.stock,
        rating = dto.rating,
        reviewCount = dto.reviewCount
    )

    fun entityToDomain(entity: LocalProduct): Product = Product(
        id = entity.id,
        name = entity.name,
        price = entity.price,
        description = entity.description,
        category = entity.category,
        image = entity.image,
        stock = entity.stock,
        rating = entity.rating,
        reviewCount = entity.reviewCount,
        isFavorite = entity.isFavorite
    )

    fun domainToEntity(product: Product): LocalProduct = LocalProduct(
        id = product.id,
        name = product.name,
        price = product.price,
        description = product.description,
        category = product.category,
        image = product.image,
        stock = product.stock,
        rating = product.rating,
        reviewCount = product.reviewCount,
        isFavorite = product.isFavorite
    )
}