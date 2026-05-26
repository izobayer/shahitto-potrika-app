package bd.du.bangla.shahittopotrika

import android.app.Application
import bd.du.bangla.shahittopotrika.data.repository.JournalRepository

class ShahittoPotrikaApplication : Application() {
    val repository by lazy { JournalRepository(this) }
}
