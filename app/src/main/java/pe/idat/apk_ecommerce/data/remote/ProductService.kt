package pe.idat.apk_ecommerce.data.remote


import pe.idat.apk_ecommerce.data.remote.dto.ProductsResponse
import retrofit2.Response
import retrofit2.http.GET

interface ProductService {
    @GET("products.json")
    suspend fun getProducts(): Response<ProductsResponse>

    companion object {
        const val BASE_URL = "https://json-tienda.vercel.app/"
    }
}