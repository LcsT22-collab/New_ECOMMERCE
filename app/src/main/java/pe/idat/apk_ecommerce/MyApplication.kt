package pe.idat.apk_ecommerce

import android.app.Application
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import pe.idat.apk_ecommerce.util.CartManager

class MyApplication : Application() {

    private companion object {
        const val TAG = "MyApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Aplicación iniciada")
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "Aplicación terminada")
        FirebaseAuth.getInstance().signOut()
        CartManager.clearCart()
    }
}