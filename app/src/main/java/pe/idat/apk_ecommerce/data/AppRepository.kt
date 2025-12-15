package pe.idat.apk_ecommerce.data

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pe.idat.apk_ecommerce.data.local.AppDatabase
import pe.idat.apk_ecommerce.data.remote.ProductService
import pe.idat.apk_ecommerce.domain.model.Product
import pe.idat.apk_ecommerce.util.ProductMapper

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppRepository(context: Context) {
    private val productDao = AppDatabase.getDatabase(context).productDao()
    private val auth = FirebaseAuth.getInstance()

    // Agregar TAG para logs
    private companion object {
        const val TAG = "AppRepository"
    }

    private val api = Retrofit.Builder()
        .baseUrl(ProductService.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ProductService::class.java)

    suspend fun loadProductsFromApi(): Result<List<Product>> = withContext(Dispatchers.IO) {
        runCatching {
            Log.d(TAG, "Intentando cargar productos desde API: ${ProductService.BASE_URL}")

            val response = api.getProducts()
            Log.d(TAG, "Respuesta recibida. Código: ${response.code()}, Éxito: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "Body recibido: ${body != null}, Cantidad de productos: ${body?.products?.size ?: 0}")

                val apiProducts = body?.products?.map { ProductMapper.dtoToDomain(it) } ?: emptyList()

                Log.d(TAG, "Insertando ${apiProducts.size} productos en la base de datos local")
                productDao.insertAll(apiProducts.map { ProductMapper.domainToEntity(it) })
                Log.d(TAG, "Productos insertados exitosamente")

                Result.success(apiProducts)
            } else {
                val errorMessage = "Error API: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        }.getOrElse { exception ->
            Log.e(TAG, "Excepción al cargar productos de API: ${exception.message}", exception)
            Result.failure(exception)
        }
    }

    suspend fun updateProducts(products: List<Product>): Boolean {
        return runCatching {
            Log.d(TAG, "Actualizando ${products.size} productos en la base de datos")
            productDao.insertAll(products.map { ProductMapper.domainToEntity(it) })
            Log.d(TAG, "Productos actualizados exitosamente")
            true
        }.getOrElse { exception ->
            Log.e(TAG, "Error al actualizar productos: ${exception.message}", exception)
            false
        }
    }

    suspend fun getProductsFromLocal(): Result<List<Product>> = withContext(Dispatchers.IO) {
        runCatching {
            Log.d(TAG, "Obteniendo productos desde base de datos local")
            val localProducts = productDao.getAllProducts().map { ProductMapper.entityToDomain(it) }
            Log.d(TAG, "Se encontraron ${localProducts.size} productos locales")
            Result.success(localProducts)
        }.getOrElse { exception ->
            Log.e(TAG, "Error al obtener productos locales: ${exception.message}", exception)
            Result.failure(exception)
        }
    }



    suspend fun updateFavoriteStatus(productId: Int, isFavorite: Boolean) {
        productDao.updateFavoriteStatus(productId, isFavorite)
    }

    suspend fun getFavoriteProducts(): List<Product> {
        return productDao.getFavoriteProducts().map { ProductMapper.entityToDomain(it) }
    }

    suspend fun toggleFavorite(productId: Int): Boolean {
        val currentStatus = productDao.getProductById(productId)?.isFavorite ?: false
        val newStatus = !currentStatus
        updateFavoriteStatus(productId, newStatus)
        return newStatus
    }
    suspend fun login(email: String, password: String): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(true)
        }.getOrElse { Result.failure(it) }
    }

    suspend fun register(name: String, email: String, password: String): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())?.await()
            Result.success(true)
        }.getOrElse { Result.failure(it) }
    }

    fun logout() = auth.signOut()
    fun getCurrentUser() = auth.currentUser
}