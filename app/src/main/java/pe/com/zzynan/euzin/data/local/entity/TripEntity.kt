package pe.com.zzynan.euzin.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trips",
    indices = [Index(value = ["gretNumber"], unique = true)]
)
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gretNumber: String,
    val driverName: String,
    val truckPlate: String,
    val dateStart: String,
    val dateEnd: String?,
    val viaticAmount: Double,
    val loadingCost: Double = 0.0,
    val unloadingCost: Double = 0.0,
    val weighingCost: Double = 0.0,
    val parkingCost: Double = 0.0,
    val tollsCost: Double = 0.0,
    val taxiCost: Double = 0.0,
    val washingCost: Double = 0.0,
    val copiesCost: Double = 0.0,
    val helperCost: Double = 0.0,
    val securityCost: Double = 0.0,
    val otherCost: Double = 0.0,
    val otherDescription: String? = null,
    val totalExpenses: Double = 0.0,
    val totalFuelAutoAmount: Double = 0.0,
    val totalFuelRealAmount: Double = 0.0,
    val balance: Double = 0.0,
    val balanceType: String = "NEUTRO",
    val status: String = "ABIERTO",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
