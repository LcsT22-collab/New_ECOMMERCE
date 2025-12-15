package pe.idat.apk_ecommerce.data.remote.dto

data class ProductDto(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val stock: Int,
    val rating: Float,
    val reviewCount: Int
)