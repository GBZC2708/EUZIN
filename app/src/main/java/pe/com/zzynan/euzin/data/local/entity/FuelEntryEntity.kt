package pe.com.zzynan.euzin.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "fuel_entries",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FuelEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val fuelDate: String? = null,
    val gallons: Double = 0.0,
    val pricePerGallon: Double = 0.0,
    val autoCalculatedAmount: Double = 0.0,
    val realPaidAmount: Double = 0.0
)
