package pe.idat.apk_ecommerce.data.local.dao

import androidx.room.*
import pe.idat.apk_ecommerce.data.local.entity.LocalProduct

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<LocalProduct>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): LocalProduct?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<LocalProduct>)

    @Query("UPDATE products SET isFavorite = :isFavorite WHERE id = :productId")
    suspend fun updateFavoriteStatus(productId: Int, isFavorite: Boolean)

    @Query("SELECT * FROM products WHERE isFavorite = 1")
    suspend fun getFavoriteProducts(): List<LocalProduct>
}