package pe.idat.apk_ecommerce.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class LocalProduct(
    @PrimaryKey val id: Int,
    val name: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val stock: Int,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val isFavorite: Boolean = false
)