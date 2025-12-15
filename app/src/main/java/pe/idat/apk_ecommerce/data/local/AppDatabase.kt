package pe.idat.apk_ecommerce.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import pe.idat.apk_ecommerce.data.local.dao.ProductDao
import pe.idat.apk_ecommerce.data.local.entity.LocalProduct

@Database(entities = [LocalProduct::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // AÑADE ESTAS 3 LÍNEAS:
                Log.d("ROOM_DEBUG", "🟢 CREANDO BASE DE DATOS: ecommerce_db")
                Log.d("ROOM_DEBUG", "🟢 RUTA: ${context.applicationContext.dataDir}/databases/")

                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ecommerce_db"
                ).fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("ROOM_DEBUG", "✅ BD CREADA EXITOSAMENTE")
                        }
                    })
                    .build().also {
                        INSTANCE = it
                        Log.d("ROOM_DEBUG", "✅ BD INICIALIZADA Y LISTA")
                    }
            }
        }
    }
}