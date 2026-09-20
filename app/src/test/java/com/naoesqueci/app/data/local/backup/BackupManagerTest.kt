package com.naoesqueci.app.data.local.backup

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.naoesqueci.app.data.local.database.AppDatabase
import com.naoesqueci.app.data.local.database.entity.CheckStateEntity
import com.naoesqueci.app.data.local.database.entity.TripEntity
import com.naoesqueci.app.data.local.database.entity.TripItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupManagerTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    private fun backupFile(name: String): File {
        return File(context.filesDir, name).also { if (it.exists()) it.delete() }
    }

    @Test
    fun `export e import fazem round-trip com itens e checks`() = runTest {
        val db = AppDatabase.getDatabase(context)
        val now = System.currentTimeMillis()
        val tripId = db.tripDao().insert(
            TripEntity(name = "Serra", departureDateTime = now + 1000, returnDateTime = now + 2000)
        )
        val itemId = db.tripItemDao().insert(
            TripItemEntity(tripId = tripId, name = "Bota", displayOrder = 0)
        )
        db.tripItemDao().insert(
            TripItemEntity(tripId = tripId, name = "Meia", displayOrder = 1)
        )
        db.checkStateDao().upsert(
            CheckStateEntity(tripId = tripId, itemId = itemId, tripType = "DEPARTURE", isChecked = true, checkedAt = now)
        )

        val manager = BackupManager(context)
        val file = backupFile("roundtrip.json")
        val exported = manager.exportTo(Uri.fromFile(file))
        assertThat(exported).isEqualTo(1)
        assertThat(file.exists()).isTrue()

        // Apaga tudo e restaura.
        db.checkStateDao().deleteByTripId(tripId)
        db.tripItemDao().deleteByTripId(tripId)
        db.tripDao().deleteById(tripId)
        assertThat(db.tripDao().getAll().first()).isEmpty()

        val result = manager.importFrom(Uri.fromFile(file))
        assertThat(result.trips).isEqualTo(1)
        assertThat(result.items).isEqualTo(2)

        val restored = db.tripDao().getAll().first().single()
        assertThat(restored.name).isEqualTo("Serra")
        val items = db.tripItemDao().getByTripId(restored.id).first()
        assertThat(items.map { it.name }).containsExactly("Bota", "Meia").inOrder()
        val checks = db.checkStateDao().getByTripAndType(restored.id, "DEPARTURE").first()
        assertThat(checks.filter { it.isChecked }.map { it.itemId })
            .containsExactly(items.first { it.name == "Bota" }.id)
    }

    @Test
    fun `arquivo invalido gera erro legivel`() = runTest {
        val file = backupFile("invalid.json")
        file.writeText("isso nao e json")
        val manager = BackupManager(context)

        var failed = false
        try {
            manager.importFrom(Uri.fromFile(file))
        } catch (e: IllegalArgumentException) {
            failed = true
        }
        assertThat(failed).isTrue()
    }

    @Test
    fun `versao desconhecida gera erro`() = runTest {
        val file = backupFile("badversion.json")
        file.writeText("""{"version":99,"trips":[]}""")
        val manager = BackupManager(context)

        var failed = false
        try {
            manager.importFrom(Uri.fromFile(file))
        } catch (e: IllegalArgumentException) {
            failed = true
        }
        assertThat(failed).isTrue()
    }
}
